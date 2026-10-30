package luna;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Verifies packaged cache replacement, archive validation and rollback without using the real user cache.
 *
 * @author lare96
 */
public final class CacheBootstrapTest {

    public static void main(String[] args) throws Exception {
        Path fixture = Files.createTempDirectory("luna-cache-bootstrap-test-");
        String previousHome = System.getProperty("user.home");
        try {
            freshAndRepeatedInstall(fixture.resolve("fresh"));
            rejectedArchives(fixture.resolve("invalid"));
            publicationRollback(fixture.resolve("rollback-before-new"), "before-new");
            publicationRollback(fixture.resolve("rollback-after-new"), "after-new");
            publicationRollback(fixture.resolve("rollback-before-old"), "before-old");
            publicationRollback(fixture.resolve("rollback-after-old"), "after-old");
            blockedStore(fixture.resolve("blocked"));
            concurrentInstall(fixture.resolve("concurrent"));
            linkedStore(fixture.resolve("links"));
            packagedPayload(fixture.resolve("packaged"));
        } finally {
            System.setProperty("user.home", previousHome);
            try (Stream<Path> paths = Files.walk(fixture)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toArray(Path[]::new)) Files.delete(path);
            }
        }
        System.out.println("CacheBootstrapTest passed");
    }

    private static void freshAndRepeatedInstall(Path home) throws Exception {
        Path cache = home.resolve("luna/cache");
        Map<String, byte[]> seed = stores("bundled");
        install(cache, seed);
        assertStores(cache, seed);
        check(!Files.exists(cache.resolve("cache")), "fresh installation has no nested cache directory");
        byte[] settings = "width=1280\nheight=800\n".getBytes(StandardCharsets.UTF_8);
        byte[] uid = {1, 2, 3, 4};
        Files.write(cache.resolve("client.properties"), settings);
        Files.write(cache.resolve("uid.dat"), uid);
        Files.write(cache.resolve("sound0.wav"), new byte[] {5, 6, 7});
        for (String name : CacheBootstrap.STORE_NAMES) Files.writeString(cache.resolve(name), "existing " + name);
        install(cache, seed);
        assertStores(cache, seed);
        check(Arrays.equals(settings, Files.readAllBytes(cache.resolve("client.properties")))
                        && Arrays.equals(uid, Files.readAllBytes(cache.resolve("uid.dat")))
                        && Arrays.equals(new byte[] {5, 6, 7}, Files.readAllBytes(cache.resolve("sound0.wav"))),
                "replacement retains each tester's settings, UID and unrelated runtime files");
        Files.writeString(cache.resolve("main_file_cache.dat"), "changed since the previous launch");
        install(cache, seed);
        assertStores(cache, seed);
        assertNoTemporary(home);
    }

    private static void rejectedArchives(Path home) throws Exception {
        Path cache = home.resolve("luna/cache");
        Map<String, byte[]> old = stores("existing");
        Map<String, byte[]> seed = stores("bundled");
        Files.createDirectories(cache);
        for (Map.Entry<String, byte[]> entry : old.entrySet()) Files.write(cache.resolve(entry.getKey()), entry.getValue());
        byte[] validManifest = manifest(seed);

        Map<String, byte[]> missing = new LinkedHashMap<>(seed);
        missing.remove("main_file_cache.idx4");
        rejected(cache, zip(missing), validManifest, old);
        Map<String, byte[]> extra = new LinkedHashMap<>(seed);
        extra.put("../escaped.dat", new byte[] {1});
        rejected(cache, zip(extra), validManifest, old);
        check(!Files.exists(home.resolve("escaped.dat")) && !Files.exists(home.resolve("luna/escaped.dat")),
                "archive paths never escape the installation staging directory");
        Map<String, byte[]> nested = new LinkedHashMap<>(seed);
        nested.remove("main_file_cache.dat");
        nested.put("cache/main_file_cache.dat", new byte[] {1});
        rejected(cache, zip(nested), validManifest, old);
        check(!Files.exists(cache.resolve("cache")), "nested archive entries never create cache/cache");

        String wrong = "0".repeat(64) + new String(validManifest, StandardCharsets.US_ASCII).substring(64);
        rejected(cache, zip(seed), wrong.getBytes(StandardCharsets.US_ASCII), old);
        rejected(cache, Arrays.copyOf(zip(seed), zip(seed).length / 4), validManifest, old);
        rejected(cache, zip(seed), manifest(missing), old);
        byte[] duplicateManifest = (new String(validManifest, StandardCharsets.US_ASCII)
                + new String(validManifest, StandardCharsets.US_ASCII).lines().findFirst().orElseThrow() + "\n")
                .getBytes(StandardCharsets.US_ASCII);
        rejected(cache, zip(seed), duplicateManifest, old);

        Map<String, byte[]> duplicate = new LinkedHashMap<>(seed);
        duplicate.put("main_file_cache.idx5", new byte[] {9});
        byte[] duplicateZip = zip(duplicate);
        replace(duplicateZip, "main_file_cache.idx5", "main_file_cache.idx0");
        rejected(cache, duplicateZip, validManifest, old);
        assertNoTemporary(home);
    }

    private static void publicationRollback(Path home, String failureStage) throws Exception {
        Path cache = home.resolve("luna/cache");
        Map<String, byte[]> old = stores("existing");
        Map<String, byte[]> seed = stores("bundled");
        Files.createDirectories(cache);
        for (Map.Entry<String, byte[]> entry : old.entrySet()) Files.write(cache.resolve(entry.getKey()), entry.getValue());
        try {
            CacheBootstrap.install(cache, new ByteArrayInputStream(zip(seed)), new ByteArrayInputStream(manifest(seed)),
                    (source, target) -> {
                        boolean candidate = target.getFileName().toString().equals("main_file_cache.idx2")
                                && (failureStage.endsWith("new") && source.getParent().getFileName().toString().equals("new")
                                || failureStage.endsWith("old") && target.getParent().getFileName().toString().equals("old"));
                        if (candidate && failureStage.startsWith("before"))
                            throw new IOException("injected replacement failure after earlier stores were published");
                        Path moved = Files.move(source, target);
                        if (candidate)
                            throw new IOException("injected replacement failure reported after moving a store");
                        return moved;
                    });
            throw new AssertionError("injected publication failure was ignored");
        } catch (IOException expected) {
            check(expected.getMessage().contains("injected replacement"), "publication failures remain actionable");
        }
        assertStores(cache, old);
        assertNoTemporary(home);
    }

    private static void blockedStore(Path home) throws Exception {
        Path cache = home.resolve("luna/cache");
        Files.createDirectories(cache.resolve("main_file_cache.idx2"));
        byte[] old = "existing data".getBytes(StandardCharsets.UTF_8);
        Files.write(cache.resolve("main_file_cache.dat"), old);
        Map<String, byte[]> seed = stores("bundled");
        try {
            install(cache, seed);
            throw new AssertionError("a directory at a store path was replaced");
        } catch (IOException expected) {
            check(expected.getMessage().contains("regular file"), "invalid destinations fail before publication");
        }
        check(Arrays.equals(old, Files.readAllBytes(cache.resolve("main_file_cache.dat"))),
                "preflight validation preserves existing stores");
        assertNoTemporary(home);
    }

    private static void concurrentInstall(Path home) throws Exception {
        Path parent = home.resolve("luna");
        Files.createDirectories(parent);
        try (FileChannel channel = FileChannel.open(parent.resolve(".cache-bootstrap.lock"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = channel.lock()) {
            try {
                install(parent.resolve("cache"), stores("bundled"));
                throw new AssertionError("overlapping cache installation was admitted");
            } catch (IOException expected) {
                check(expected.getMessage().contains("Another client"), "concurrent installation is rejected clearly");
            }
        }
    }

    private static void linkedStore(Path home) throws Exception {
        Path cache = home.resolve("luna/cache");
        Files.createDirectories(cache);
        Path outside = home.resolve("outside.dat");
        byte[] old = "outside content".getBytes(StandardCharsets.UTF_8);
        Files.write(outside, old);
        Path linked = cache.resolve("main_file_cache.dat");
        try {
            Files.createLink(linked, outside);
        } catch (UnsupportedOperationException | IOException unavailable) {
            System.out.println("CacheBootstrapTest: hard-link fixture unavailable on this filesystem");
            return;
        }
        install(cache, stores("bundled"));
        check(Arrays.equals(old, Files.readAllBytes(outside)),
                "replacing an existing hard-linked store does not modify its external alias");
    }

    private static void packagedPayload(Path home) throws Exception {
        Files.createDirectories(home);
        System.setProperty("user.home", home.toString());
        CacheBootstrap.installBundledCache();
        Path cache = ClientPaths.cacheDirectory();
        check(cache.equals(home.toAbsolutePath().resolve("luna/cache")), "public startup uses the shared cache path");
        for (String name : CacheBootstrap.STORE_NAMES)
            check(Files.size(cache.resolve(name)) > 0, "the executable includes a usable store: " + name);
        check(!Files.exists(cache.resolve("uid.dat")) && !Files.exists(cache.resolve("client.properties")),
                "the bundled seed contains no developer identity or window preferences");
        Files.writeString(cache.resolve("main_file_cache.idx0"), "an old cache");
        CacheBootstrap.installBundledCache();
        check(Files.size(cache.resolve("main_file_cache.idx0")) > "an old cache".length(),
                "the real packaged seed replaces old game stores on repeated launches");
        assertNoTemporary(home);
    }

    private static void rejected(Path cache, byte[] archive, byte[] hashes, Map<String, byte[]> old) throws Exception {
        try {
            CacheBootstrap.install(cache, new ByteArrayInputStream(archive), new ByteArrayInputStream(hashes));
            throw new AssertionError("malformed cache seed was accepted");
        } catch (IOException expected) {
            assertStores(cache, old);
        }
    }

    private static void install(Path cache, Map<String, byte[]> seed) throws Exception {
        CacheBootstrap.install(cache, new ByteArrayInputStream(zip(seed)), new ByteArrayInputStream(manifest(seed)));
    }

    private static Map<String, byte[]> stores(String value) {
        Map<String, byte[]> stores = new LinkedHashMap<>();
        for (String name : CacheBootstrap.STORE_NAMES)
            stores.put(name, (value + " " + name).getBytes(StandardCharsets.UTF_8));
        return stores;
    }

    private static byte[] manifest(Map<String, byte[]> stores) throws Exception {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, byte[]> entry : stores.entrySet()) {
            result.append(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(entry.getValue())))
                    .append("  ").append(entry.getKey()).append('\n');
        }
        return result.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private static byte[] zip(Map<String, byte[]> stores) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, byte[]> entry : stores.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
        return output.toByteArray();
    }

    private static void replace(byte[] data, String before, String after) {
        byte[] source = before.getBytes(StandardCharsets.UTF_8);
        byte[] destination = after.getBytes(StandardCharsets.UTF_8);
        check(source.length == destination.length, "fixture names retain ZIP record lengths");
        for (int offset = 0; offset <= data.length - source.length; offset++) {
            if (Arrays.equals(data, offset, offset + source.length, source, 0, source.length))
                System.arraycopy(destination, 0, data, offset, destination.length);
        }
    }

    private static void assertStores(Path cache, Map<String, byte[]> expected) throws IOException {
        for (Map.Entry<String, byte[]> entry : expected.entrySet())
            check(Arrays.equals(entry.getValue(), Files.readAllBytes(cache.resolve(entry.getKey()))),
                    "cache bytes match for " + entry.getKey());
    }

    private static void assertNoTemporary(Path home) throws IOException {
        try (Stream<Path> children = Files.list(home.resolve("luna"))) {
            check(children.noneMatch(path -> path.getFileName().toString().startsWith(".cache-install-")),
                    "completed or rolled-back installations clean their own temporary files");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
