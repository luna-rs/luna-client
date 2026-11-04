package luna.browser.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import luna.browser.BrowserDiagnostics;
import luna.browser.io.BrowserIO;
import org.teavm.jso.JSObject;

/**
 * Presents a binary WebSocket gateway as the byte streams used by the shared client protocol.
 *
 * @author lare96
 */
public final class Socket {
    private final JSObject state;
    private int timeout = 30000;
    private boolean closed;
    private final InputStream input = new InputStream() {
        @Override
        public int read() throws IOException {
            byte[] bytes = readSome(1);
            return bytes == null ? -1 : bytes[0] & 255;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            Objects.checkFromIndexSize(offset, length, bytes.length);
            if (length == 0) return 0;
            byte[] received = readSome(length);
            if (received == null) return -1;
            System.arraycopy(received, 0, bytes, offset, received.length);
            return received.length;
        }

        @Override
        public int available() {
            return closed ? 0 : BrowserIO.available(state);
        }

        @Override
        public void close() {
            Socket.this.close();
        }
    };
    private final OutputStream output = new OutputStream() {
        @Override
        public void write(int value) throws IOException {
            write(new byte[] {(byte) value}, 0, 1);
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            try {
                Objects.checkFromIndexSize(offset, length, bytes.length);
                if (closed) throw new IOException("Browser socket is closed");
                if (length > 0) BrowserIO.writeSocket(state, bytes, offset, length);
            } catch (IOException | RuntimeException failure) {
                report("write offset=" + offset + " length=" + length + " buffer=" + (bytes == null ? -1 : bytes.length), failure);
                throw failure;
            }
        }

        @Override
        public void close() {
            Socket.this.close();
        }
    };

    public Socket(InetAddress address, int port) throws IOException {
        state = BrowserIO.openSocket(address.getHostAddress(), port);
    }

    public Socket(String host, int port) throws IOException {
        this(InetAddress.getByName(host), port);
    }

    private byte[] readSome(int length) throws IOException {
        if (closed) return null;
        try {
            return BrowserIO.readSocket(state, length, timeout);
        } catch (IOException | RuntimeException failure) {
            report("read length=" + length + " timeout=" + timeout, failure);
            throw failure;
        }
    }

    private static void report(String operation, Throwable failure) {
        System.err.println("Browser socket " + operation + ": " + failure);
        failure.printStackTrace();
        BrowserDiagnostics.report(failure);
    }

    public InputStream getInputStream() {
        return input;
    }

    public OutputStream getOutputStream() {
        return output;
    }

    public void setSoTimeout(int milliseconds) {
        if (milliseconds < 0) throw new IllegalArgumentException("Negative socket timeout");
        timeout = milliseconds;
    }

    public void setTcpNoDelay(boolean enabled) {
    }

    public void close() {
        if (!closed) {
            closed = true;
            BrowserIO.close(state);
        }
    }
}
