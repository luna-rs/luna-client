package luna;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Installs the bundled game stores before any cache handles are opened.
 * Existing game stores are replaced on every launch; user settings and identifiers are retained.
 *
 * @author lare96
 */
public final class CacheBootstrap {

    static final List<String> STORE_NAMES = List.of("main_file_cache.dat", "main_file_cache.idx0",
            "main_file_cache.idx1", "main_file_cache.idx2", "main_file_cache.idx3", "main_file_cache.idx4");
    private static final long MAX_CACHE_BYTES = 512L * 1024 * 1024;
    private static final String LOCK_FILE = ".cache-bootstrap.lock";

    private CacheBootstrap() {
    }

    /**
     * Replaces the six game stores with verified packaged copies at the shared user-home cache path.
     * Direct IDE class-directory launches may use the source-tree seed when no packaged pair exists.
     */
    public static void installBundledCache() throws IOException {
        ClassLoader loader = CacheBootstrap.class.getClassLoader();
        boolean archivePresent = loader.getResource("bootstrap/cache.zip") != null;
        boolean manifestPresent = loader.getResource("bootstrap/cache.sha256") != null;
        if (archivePresent != manifestPresent)
            throw new FileNotFoundException("The packaged cache seed is incomplete");
        if (archivePresent) {
            try (InputStream archive = ClientResources.openBundled("bootstrap/cache.zip");
                 InputStream manifest = ClientResources.openBundled("bootstrap/cache.sha256")) {
                install(ClientPaths.cacheDirectory(), archive, manifest);
            }
        } else {
            if (!runningFromClassesDirectory())
                throw new FileNotFoundException("Missing packaged cache seed: bootstrap/cache.zip and bootstrap/cache.sha256");
            Path seed = Path.of("data", "bootstrap");
            try (InputStream archive = Files.newInputStream(seed.resolve("cache.zip"));
                 InputStream manifest = Files.newInputStream(seed.resolve("cache.sha256"))) {
                install(ClientPaths.cacheDirectory(), archive, manifest);
            }
        }
    }

    private static boolean runningFromClassesDirectory() {
        try {
            CodeSource source = CacheBootstrap.class.getProtectionDomain().getCodeSource();
            return source != null && Files.isDirectory(Path.of(source.getLocation().toURI()));
        } catch (URISyntaxException | RuntimeException exception) {
            return false;
        }
    }

    static void install(Path directory, InputStream archive, InputStream manifest) throws IOException {
        install(directory, archive, manifest, Files::move);
    }

    static void install(Path directory, InputStream archive, InputStream manifest, FileMover mover) throws IOException {
        Path cache = directory.toAbsolutePath().normalize();
        Path parent = cache.getParent();
        if (parent == null || parent.getParent() == null || !"cache".equals(cache.getFileName().toString())
                || !"luna".equals(parent.getFileName().toString()))
            throw new IOException("Unexpected Luna cache directory: " + cache);
        Files.createDirectories(parent);
        Path expectedParent = parent.getParent().toRealPath().resolve("luna");
        if (Files.isSymbolicLink(parent) || !parent.toRealPath().equals(expectedParent))
            throw new IOException("The Luna cache parent must not be a link: " + parent);
        validateCacheDirectory(cache, expectedParent);
        Path lockPath = parent.resolve(LOCK_FILE);
        if (Files.exists(lockPath, LinkOption.NOFOLLOW_LINKS)
                && !Files.isRegularFile(lockPath, LinkOption.NOFOLLOW_LINKS))
            throw new IOException("Invalid cache installation lock: " + lockPath);
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = acquireLock(channel, cache)) {
            installLocked(cache, expectedParent, archive, manifest, mover);
        }
    }

    private static FileLock acquireLock(FileChannel channel, Path cache) throws IOException {
        try {
            FileLock lock = channel.tryLock();
            if (lock != null) return lock;
        } catch (OverlappingFileLockException exception) {
            throw new IOException("Another client is installing the Luna cache: " + cache, exception);
        }
        throw new IOException("Another client is installing the Luna cache: " + cache);
    }

    private static void installLocked(Path cache, Path expectedParent, InputStream archive,
                                      InputStream manifest, FileMover mover) throws IOException {
        Map<String, String> hashes = readManifest(manifest);
        Path temporary = Files.createTempDirectory(cache.getParent(), ".cache-install-");
        boolean safeToDelete = true;
        Throwable failure = null;
        try {
            Path staged = Files.createDirectory(temporary.resolve("new"));
            Path backup = Files.createDirectory(temporary.resolve("old"));
            extract(archive, staged, hashes);
            validateCacheDirectory(cache, expectedParent);
            Files.createDirectories(cache);
            for (String name : STORE_NAMES) {
                Path target = cache.resolve(name);
                if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)
                        && !Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS))
                    throw new IOException("Cache store must be a regular file: " + target);
            }
            List<String> backedUp = new ArrayList<>();
            List<String> installed = new ArrayList<>();
            safeToDelete = false;
            try {
                for (String name : STORE_NAMES) {
                    Path target = cache.resolve(name);
                    if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                        backedUp.add(name);
                        mover.move(target, backup.resolve(name));
                    }
                    installed.add(name);
                    mover.move(staged.resolve(name), target);
                }
                safeToDelete = true;
            } catch (IOException | RuntimeException | Error exception) {
                safeToDelete = rollback(cache, backup, installed, backedUp, exception);
                if (!safeToDelete)
                    exception.addSuppressed(new IOException("Previous cache files are retained in " + backup));
                throw exception;
            }
        } catch (IOException | RuntimeException | Error exception) {
            failure = exception;
            throw exception;
        } finally {
            if (safeToDelete) {
                try {
                    deleteTemporary(temporary);
                } catch (IOException exception) {
                    if (failure == null) throw exception;
                    failure.addSuppressed(exception);
                }
            }
        }
    }

    private static void validateCacheDirectory(Path cache, Path expectedParent) throws IOException {
        if (Files.exists(cache, LinkOption.NOFOLLOW_LINKS)
                && (!Files.isDirectory(cache, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(cache) || !cache.toRealPath().equals(expectedParent.resolve("cache"))))
            throw new IOException("The Luna cache must be a real directory: " + cache);
    }

    private static Map<String, String> readManifest(InputStream input) throws IOException {
        byte[] bytes = input.readNBytes(4097);
        if (bytes.length > 4096) throw new IOException("Bundled cache manifest is oversized");
        Map<String, String> hashes = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(bytes), StandardCharsets.US_ASCII))) {
            for (String line; (line = reader.readLine()) != null;) {
                if (line.length() < 67 || !line.substring(0, 64).matches("[0-9a-f]{64}")
                        || !line.substring(64, 66).equals("  "))
                    throw new IOException("Invalid bundled cache checksum entry");
                String name = line.substring(66);
                if (!STORE_NAMES.contains(name) || hashes.putIfAbsent(name, line.substring(0, 64)) != null)
                    throw new IOException("Unexpected or duplicate bundled cache checksum: " + name);
            }
        }
        if (hashes.size() != STORE_NAMES.size()) throw new IOException("Bundled cache checksums are incomplete");
        return hashes;
    }

    private static void extract(InputStream input, Path staged, Map<String, String> hashes) throws IOException {
        Set<String> extracted = new HashSet<>();
        long total = 0;
        byte[] buffer = new byte[8192];
        try (ZipInputStream zip = new ZipInputStream(input, StandardCharsets.UTF_8)) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {
                String name = entry.getName();
                if (entry.isDirectory() || !hashes.containsKey(name) || !extracted.add(name))
                    throw new IOException("Unexpected or duplicate bundled cache entry: " + name);
                MessageDigest digest = sha256();
                long size = 0;
                try (OutputStream output = Files.newOutputStream(staged.resolve(name), StandardOpenOption.CREATE_NEW)) {
                    for (int count; (count = zip.read(buffer)) != -1;) {
                        total += count;
                        size += count;
                        if (total > MAX_CACHE_BYTES) throw new IOException("Bundled cache exceeds its extraction limit");
                        digest.update(buffer, 0, count);
                        output.write(buffer, 0, count);
                    }
                }
                if (size == 0 || !hashes.get(name).equals(HexFormat.of().formatHex(digest.digest())))
                    throw new IOException("Bundled cache checksum failed: " + name);
                zip.closeEntry();
            }
        }
        if (extracted.size() != STORE_NAMES.size()) throw new IOException("Bundled cache archive is incomplete");
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("The Java runtime does not provide SHA-256", exception);
        }
    }

    private static boolean rollback(Path cache, Path backup, List<String> installed,
                                    List<String> backedUp, Throwable failure) {
        boolean complete = true;
        for (int index = installed.size() - 1; index >= 0; index--) {
            try {
                Files.deleteIfExists(cache.resolve(installed.get(index)));
            } catch (IOException | RuntimeException exception) {
                failure.addSuppressed(exception);
                complete = false;
            }
        }
        for (int index = backedUp.size() - 1; index >= 0; index--) {
            String name = backedUp.get(index);
            try {
                if (Files.exists(backup.resolve(name), LinkOption.NOFOLLOW_LINKS)) {
                    Files.move(backup.resolve(name), cache.resolve(name));
                } else if (!Files.isRegularFile(cache.resolve(name), LinkOption.NOFOLLOW_LINKS)) {
                    throw new IOException("The previous cache store could not be restored: " + name);
                }
            } catch (IOException | RuntimeException exception) {
                failure.addSuppressed(exception);
                complete = false;
            }
        }
        return complete;
    }

    private static void deleteTemporary(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path path, IOException exception) throws IOException {
                if (exception != null) throw exception;
                Files.delete(path);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Allows publication failures to be exercised without changing filesystem permissions.
     *
     * @author lare96
     */
    @FunctionalInterface
    interface FileMover {
        Path move(Path source, Path target) throws IOException;
    }
}
