package io.luna.build;

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Verifies release archives and prepares only the six native cache stores for client builds.
 *
 * @author lare96
 */
public final class ReleaseCache {
    public static final long MAX_ARCHIVE_BYTES = 128L * 1024 * 1024;
    private static final long MAX_STORE_BYTES = 128L * 1024 * 1024;
    private static final long MAX_EXPANDED_BYTES = 256L * 1024 * 1024;
    public static final List<String> STORES = List.of("main_file_cache.dat", "main_file_cache.idx0",
            "main_file_cache.idx1", "main_file_cache.idx2", "main_file_cache.idx3", "main_file_cache.idx4");

    private ReleaseCache() {
    }

    /**
     * Captures the public metadata needed to verify one selected GitHub release asset.
     *
     * @author lare96
     */
    public record Asset(String tag, long id, long size, String sha256, java.net.URI downloadUrl) {
    }

    public static Asset selectLatest(List<?> releases, String assetName) throws IOException {
        List<Map<?, ?>> candidates = new ArrayList<>();
        for (Object value : releases) {
            if (!(value instanceof Map<?, ?> release)) throw new IOException("Invalid release metadata.");
            if (!Boolean.TRUE.equals(release.get("draft")) && hasAsset(release, assetName)) candidates.add(release);
        }
        if (candidates.isEmpty()) throw new IOException("No published release contains the requested cache asset.");
        candidates.sort(Comparator.comparing(ReleaseCache::published).reversed());
        return selectTagged(candidates.getFirst(), assetName);
    }

    public static Asset selectTagged(Map<?, ?> release, String assetName) throws IOException {
        if (Boolean.TRUE.equals(release.get("draft"))) throw new IOException("Draft cache releases cannot be used.");
        Object values = release.get("assets");
        if (!(values instanceof List<?> assets)) throw new IOException("Release assets are missing.");
        List<Map<?, ?>> matching = new ArrayList<>();
        for (Object value : assets) {
            if (value instanceof Map<?, ?> asset && assetName.equals(asset.get("name"))) matching.add(asset);
        }
        if (matching.size() != 1) throw new IOException("The release must contain exactly one requested cache asset.");
        Map<?, ?> selected = matching.getFirst();
        Object digest = selected.get("digest");
        if (!(digest instanceof String text) || !text.matches("sha256:[a-fA-F0-9]{64}")) {
            throw new IOException("GitHub must provide a SHA-256 digest for the cache asset.");
        }
        long size = positive(selected.get("size"));
        if (size > MAX_ARCHIVE_BYTES) throw new IOException("The release cache archive is too large.");
        Object tag = release.get("tag_name"), url = selected.get("browser_download_url");
        if (!(tag instanceof String tagText) || tagText.isBlank() || !(url instanceof String urlText)) {
            throw new IOException("Invalid cache release identity.");
        }
        java.net.URI uri;
        try {
            uri = java.net.URI.create(urlText);
        } catch (IllegalArgumentException invalid) {
            throw new IOException("Invalid release asset URL.");
        }
        if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null) {
            throw new IOException("Release assets require a public HTTPS URL.");
        }
        return new Asset(tagText, positive(selected.get("id")), size, text.substring(7).toLowerCase(), uri);
    }

    private static boolean hasAsset(Map<?, ?> release, String name) {
        return release.get("assets") instanceof List<?> values && values.stream()
                .anyMatch(value -> value instanceof Map<?, ?> asset && name.equals(asset.get("name")));
    }

    private static Instant published(Map<?, ?> release) {
        Object value = release.get("published_at");
        if (!(value instanceof String)) value = release.get("created_at");
        try {
            return value instanceof String text ? Instant.parse(text) : Instant.EPOCH;
        } catch (RuntimeException invalid) {
            return Instant.EPOCH;
        }
    }

    private static long positive(Object value) throws IOException {
        if (!(value instanceof Number number) || number.longValue() <= 0
                || number.doubleValue() != number.longValue()) throw new IOException("Invalid release asset size or ID.");
        return number.longValue();
    }

    public static void verifyArchive(Path archive, String expectedHash, long expectedBytes) throws IOException {
        regular(archive);
        if (expectedHash == null || !expectedHash.matches("[a-f0-9]{64}") || expectedBytes <= 0
                || expectedBytes > MAX_ARCHIVE_BYTES || Files.size(archive) != expectedBytes
                || !hash(archive).equals(expectedHash)) throw new IOException("Cache archive size or SHA-256 mismatch.");
    }

    public static void prepare(Path archive, Path output, String expectedHash, long expectedBytes,
                               Map<String, ?> source) throws IOException {
        verifyArchive(archive, expectedHash, expectedBytes);
        Path absolute = output.toAbsolutePath().normalize();
        if (absolute.getParent() == null || absolute.getFileName() == null) throw new IOException("Invalid cache output path.");
        noLinks(absolute);
        if (Files.exists(absolute, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(absolute)) throw new IOException("Cache output must be a directory.");
        Files.createDirectories(absolute.getParent());
        Path stage = Files.createDirectory(absolute.resolveSibling(".cache-prepare-" + UUID.randomUUID()));
        Path previous = absolute.resolveSibling(".cache-previous-" + UUID.randomUUID());
        boolean movedPrevious = false;
        boolean published = false;
        try {
            rejectSpecialZipEntries(archive);
            extract(archive, stage);
            Map<String, String> stores = storeHashes(stage);
            Files.writeString(stage.resolve("cache.sha256"), manifest(stores), StandardCharsets.US_ASCII);
            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(stage.resolve("cache.zip")))) {
                for (String name : STORES) {
                    ZipEntry entry = new ZipEntry(name);
                    entry.setTimeLocal(java.time.LocalDateTime.of(1980, 1, 1, 0, 0));
                    zip.putNextEntry(entry);
                    Files.copy(stage.resolve(name), zip);
                    zip.closeEntry();
                }
            }
            Map<String, Object> receipt = new LinkedHashMap<>();
            receipt.put("formatVersion", 1);
            receipt.put("source", new LinkedHashMap<>(source));
            receipt.put("archiveSha256", expectedHash);
            receipt.put("archiveBytes", expectedBytes);
            receipt.put("stores", stores);
            receipt.put("normalizedZipSha256", hash(stage.resolve("cache.zip")));
            receipt.put("normalizedZipBytes", Files.size(stage.resolve("cache.zip")));
            Files.writeString(stage.resolve("release-cache.json"), JsonOutput.prettyPrint(JsonOutput.toJson(receipt)) + "\n");
            if (!verifiedOutput(stage, expectedHash)) throw new IOException("Prepared cache verification failed.");
            if (verifiedOutput(absolute, expectedHash)
                    && Files.mismatch(stage.resolve("release-cache.json"), absolute.resolve("release-cache.json")) == -1) return;
            if (Files.exists(absolute, LinkOption.NOFOLLOW_LINKS)) {
                Files.move(absolute, previous, StandardCopyOption.ATOMIC_MOVE);
                movedPrevious = true;
            }
            try {
                Files.move(stage, absolute, StandardCopyOption.ATOMIC_MOVE);
                published = true;
            } catch (IOException failure) {
                if (movedPrevious) Files.move(previous, absolute, StandardCopyOption.ATOMIC_MOVE);
                throw failure;
            }
        } finally {
            clean(stage);
            if (published) clean(previous);
        }
    }

    private static void extract(Path archive, Path stage) throws IOException {
        long total = 0;
        Map<String, Boolean> imported = new LinkedHashMap<>();
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            var entries = zip.entries();
            int count = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (++count > 4096) throw new IOException("Too many cache archive entries.");
                String name = safeName(entry.getName());
                if (entry.isDirectory()) continue;
                String basename = name.substring(name.lastIndexOf('/') + 1);
                if (!STORES.contains(basename)) continue;
                if (imported.put(basename, true) != null) throw new IOException("Duplicate cache store in archive: " + basename);
                if (entry.getSize() < 0 || entry.getSize() > MAX_STORE_BYTES) throw new IOException("Invalid cache store size.");
                CRC32 crc = new CRC32();
                long bytes = 0;
                try (InputStream input = zip.getInputStream(entry); OutputStream target = Files.newOutputStream(stage.resolve(basename))) {
                    byte[] buffer = new byte[65536];
                    for (int read; (read = input.read(buffer)) != -1;) {
                        bytes += read;
                        total += read;
                        if (bytes > MAX_STORE_BYTES || total > MAX_EXPANDED_BYTES) throw new IOException("Expanded cache exceeds its size limit.");
                        crc.update(buffer, 0, read);
                        target.write(buffer, 0, read);
                    }
                }
                if (bytes != entry.getSize() || crc.getValue() != entry.getCrc()) throw new IOException("Cache store ZIP integrity mismatch.");
            }
        }
        if (!imported.keySet().equals(new java.util.HashSet<>(STORES))) throw new IOException("Archive must contain one copy of every core cache store.");
    }

    private static String safeName(String input) throws IOException {
        String name = input.replace('\\', '/');
        if (name.isBlank() || name.length() > 4096 || name.startsWith("/") || name.indexOf(':') >= 0
                || name.chars().anyMatch(value -> value < 32 || value == 127)) throw new IOException("Unsafe cache archive path.");
        String stripped = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
        for (String part : stripped.split("/", -1)) {
            if (part.isEmpty() || part.equals(".") || part.equals("..")) throw new IOException("Unsafe cache archive path.");
        }
        return name;
    }

    private static void rejectSpecialZipEntries(Path archive) throws IOException {
        try (var input = java.nio.channels.FileChannel.open(archive)) {
            long size = input.size();
            int length = (int) Math.min(size, 65557);
            ByteBuffer tail = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
            readFully(input, tail, size - length);
            int end = -1;
            for (int index = length - 22; index >= 0; index--) {
                if (tail.getInt(index) == 0x06054b50 && index + 22 + Short.toUnsignedInt(tail.getShort(index + 20)) == length) {
                    end = index;
                    break;
                }
            }
            if (end < 0 || tail.getShort(end + 4) != 0 || tail.getShort(end + 6) != 0
                    || tail.getShort(end + 8) != tail.getShort(end + 10)) throw new IOException("Unsupported cache ZIP directory.");
            int entries = Short.toUnsignedInt(tail.getShort(end + 10));
            long directorySize = Integer.toUnsignedLong(tail.getInt(end + 12));
            long offset = Integer.toUnsignedLong(tail.getInt(end + 16));
            if (entries > 4096 || directorySize > 8 * 1024 * 1024 || offset + directorySize > size - length + end) {
                throw new IOException("Unsupported cache ZIP directory limits.");
            }
            ByteBuffer central = ByteBuffer.allocate((int) directorySize).order(ByteOrder.LITTLE_ENDIAN);
            readFully(input, central, offset);
            int cursor = 0;
            for (int entry = 0; entry < entries; entry++) {
                if (cursor + 46 > central.capacity() || central.getInt(cursor) != 0x02014b50) throw new IOException("Invalid cache ZIP directory entry.");
                int mode = (central.getInt(cursor + 38) >>> 16) & 0xf000;
                if (mode != 0 && mode != 0x8000 && mode != 0x4000) throw new IOException("Links and special files are not allowed in cache archives.");
                if ((central.getShort(cursor + 8) & 1) != 0) throw new IOException("Encrypted cache archives are unsupported.");
                cursor += 46 + Short.toUnsignedInt(central.getShort(cursor + 28))
                        + Short.toUnsignedInt(central.getShort(cursor + 30)) + Short.toUnsignedInt(central.getShort(cursor + 32));
            }
            if (cursor != central.capacity()) throw new IOException("Invalid cache ZIP directory length.");
        }
    }

    private static void readFully(java.nio.channels.FileChannel input, ByteBuffer buffer, long offset) throws IOException {
        while (buffer.hasRemaining()) {
            int read = input.read(buffer, offset + buffer.position());
            if (read < 0) throw new IOException("Truncated cache archive.");
        }
    }

    public static boolean verifiedOutput(Path output, String expectedArchiveHash) {
        try {
            Map<?, ?> receipt = receipt(output);
            if (!Integer.valueOf(1).equals(receipt.get("formatVersion")) || !expectedArchiveHash.equals(receipt.get("archiveSha256"))) return false;
            Map<String, String> actual = storeHashes(output);
            regular(output.resolve("cache.sha256"));
            if (!actual.equals(receipt.get("stores")) || !manifest(actual).equals(Files.readString(output.resolve("cache.sha256"), StandardCharsets.US_ASCII))) return false;
            Object bytes = receipt.get("normalizedZipBytes"), digest = receipt.get("normalizedZipSha256");
            if (!(bytes instanceof Number number) || !(digest instanceof String text)) return false;
            verifyArchive(output.resolve("cache.zip"), text, number.longValue());
            return true;
        } catch (IOException | RuntimeException invalid) {
            return false;
        }
    }

    public static void verifyOffline(Path output, String repository, String tag, String assetName) throws IOException {
        Map<?, ?> receipt = receipt(output);
        if (!(receipt.get("source") instanceof Map<?, ?> source) || !"release".equals(source.get("mode"))
                || !repository.equals(source.get("repository"))
                || !tag.equals(source.get("requestedTag")) || !assetName.equals(source.get("assetName"))
                || !(receipt.get("archiveSha256") instanceof String digest) || !verifiedOutput(output, digest)) {
            throw new IOException("No verified prepared cache matches these offline build settings. Run prepareReleaseCache online first or supply -PcacheZip.");
        }
    }

    private static Map<?, ?> receipt(Path output) throws IOException {
        noLinks(output);
        Path file = output.resolve("release-cache.json");
        regular(file);
        if (Files.size(file) > 65536) throw new IOException("Invalid prepared cache receipt.");
        Object parsed;
        try {
            parsed = new JsonSlurper().parseText(Files.readString(file));
        } catch (RuntimeException invalid) {
            throw new IOException("Invalid prepared cache receipt.");
        }
        if (!(parsed instanceof Map<?, ?> map)) throw new IOException("Invalid prepared cache receipt.");
        return map;
    }

    private static Map<String, String> storeHashes(Path directory) throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
        long total = 0;
        for (String name : STORES) {
            Path file = directory.resolve(name);
            regular(file);
            long size = Files.size(file);
            total += size;
            if (size > MAX_STORE_BYTES || total > MAX_EXPANDED_BYTES) throw new IOException("Prepared cache exceeds its size limit.");
            result.put(name, hash(file));
        }
        return result;
    }

    private static String manifest(Map<String, String> stores) {
        StringBuilder result = new StringBuilder();
        STORES.forEach(name -> result.append(stores.get(name)).append("  ").append(name).append('\n'));
        return result.toString();
    }

    public static String hash(Path file) throws IOException {
        regular(file);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(file)) {
                byte[] buffer = new byte[65536];
                for (int read; (read = input.read(buffer)) != -1;) digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }

    private static void regular(Path path) throws IOException {
        noLinks(path);
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Expected an ordinary cache file.");
    }

    private static void noLinks(Path path) throws IOException {
        for (Path current = path.toAbsolutePath().normalize(); current != null; current = current.getParent()) {
            if (Files.isSymbolicLink(current)) throw new IOException("Symbolic links are not allowed in cache build paths.");
        }
    }

    private static void clean(Path directory) throws IOException {
        if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) return;
        String name = directory.getFileName().toString();
        if (!(name.startsWith(".cache-prepare-") || name.startsWith(".cache-previous-")) || Files.isSymbolicLink(directory)) {
            throw new IOException("Refusing to clean an unrelated cache path.");
        }
        try (var files = Files.walk(directory)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(file);
        }
    }
}
