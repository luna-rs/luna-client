package luna.browser.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Supplies shared mutable cache bytes and independent file cursors without TeaVM's truncating VFS open.
 *
 * @author lare96
 */
public final class RandomAccessFile implements AutoCloseable {
    private static final int MAX_FILE_BYTES = 64 * 1024 * 1024;
    private static final Map<String, Store> stores = new HashMap<>();
    private final Store store;
    private final boolean writable;
    private int position;
    private boolean closed;

    public RandomAccessFile(File file, String mode) throws FileNotFoundException {
        if (!"r".equals(mode) && !"rw".equals(mode) && !"rws".equals(mode) && !"rwd".equals(mode))
            throw new IllegalArgumentException("Invalid browser file mode: " + mode);
        writable = !"r".equals(mode);
        String path = normalize(file.getPath());
        Store existing = stores.get(path);
        if (existing == null) {
            if (!writable) throw new FileNotFoundException(path);
            existing = new Store(new byte[0]);
            stores.put(path, existing);
        }
        store = existing;
    }

    public RandomAccessFile(String path, String mode) throws FileNotFoundException {
        this(new File(path), mode);
    }

    /**
     * Publishes one verified store without copying or modifying its source bytes.
     */
    public static void register(String path, byte[] bytes) {
        Objects.requireNonNull(bytes);
        if (bytes.length > MAX_FILE_BYTES) throw new IllegalArgumentException("Browser cache store is oversized");
        stores.put(normalize(path), new Store(bytes));
    }

    private static String normalize(String path) {
        StringBuilder normalized = new StringBuilder(path.length());
        boolean slash = false;
        for (int index = 0; index < path.length(); index++) {
            char value = path.charAt(index);
            if (value == '\\') value = '/';
            if (value != '/' || !slash) normalized.append(value);
            slash = value == '/';
        }
        return normalized.toString();
    }

    public void seek(long offset) throws IOException {
        ensureOpen();
        if (offset < 0 || offset > MAX_FILE_BYTES) throw new IOException("Invalid browser cache offset: " + offset);
        position = (int) offset;
    }

    public long length() throws IOException {
        ensureOpen();
        return store.length;
    }

    public long getFilePointer() throws IOException {
        ensureOpen();
        return position;
    }

    public int read() throws IOException {
        ensureOpen();
        return position >= store.length ? -1 : store.bytes[position++] & 255;
    }

    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    public int read(byte[] bytes, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, bytes.length);
        ensureOpen();
        if (length == 0) return 0;
        int count = Math.min(length, store.length - position);
        if (count <= 0) return -1;
        System.arraycopy(store.bytes, position, bytes, offset, count);
        position += count;
        return count;
    }

    public void write(int value) throws IOException {
        write(new byte[] {(byte) value}, 0, 1);
    }

    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }

    public void write(byte[] bytes, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, bytes.length);
        ensureWritable();
        if (length == 0) return;
        long end = (long) position + length;
        if (end > MAX_FILE_BYTES) throw new IOException("Browser cache store exceeds its size limit");
        ensureCapacity((int) end);
        if (position > store.length) Arrays.fill(store.bytes, store.length, position, (byte) 0);
        System.arraycopy(bytes, offset, store.bytes, position, length);
        position = (int) end;
        store.length = Math.max(store.length, position);
    }

    public void setLength(long length) throws IOException {
        ensureWritable();
        if (length < 0 || length > MAX_FILE_BYTES) throw new IOException("Invalid browser cache length: " + length);
        int count = (int) length;
        ensureCapacity(count);
        if (count > store.length) Arrays.fill(store.bytes, store.length, count, (byte) 0);
        store.length = count;
        if (position > count) position = count;
    }

    private void ensureCapacity(int length) {
        if (length <= store.bytes.length) return;
        int capacity = Math.max(length, Math.max(16, store.bytes.length + store.bytes.length / 2));
        store.bytes = Arrays.copyOf(store.bytes, Math.min(MAX_FILE_BYTES, capacity));
    }

    private void ensureOpen() throws IOException {
        if (closed) throw new IOException("Browser cache file is closed");
    }

    private void ensureWritable() throws IOException {
        ensureOpen();
        if (!writable) throw new IOException("Browser cache file is read-only");
    }

    @Override
    public void close() {
        closed = true;
    }

    /**
     * Keeps contents and logical length shared while each handle owns its cursor and access mode.
     *
     * @author lare96
     */
    private static final class Store {
        private byte[] bytes;
        private int length;

        private Store(byte[] bytes) {
            this.bytes = bytes;
            length = bytes.length;
        }
    }
}
