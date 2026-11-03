import luna.ClientPaths;
import luna.ClientConfigurationFixture;
import sign.signlink;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Verifies configured cache placement, separate root preferences and preservation of existing stores.
 *
 * @author lare96
 */
public final class CacheLocationTest {

    public static void main(String[] args) throws Exception {
        try (ClientConfigurationFixture configuration = ClientConfigurationFixture.install()) {
            Path fixture = configuration.directory();
            Path home = Path.of(System.getProperty("user.home"));
            try {
                Path expected = home.resolve("luna").resolve("cache");
                check(ClientPaths.cacheDirectory().equals(expected), "cache resolves exactly beneath user.home/luna/cache");
                check(Path.of(signlink.findcachedir()).equals(expected), "legacy cache lookup uses the same root");
                check(Files.isDirectory(expected), "cache lookup creates its directory before stores are opened");

                ClientSettings settings = new ClientSettings();
                settings.save();
                check(Files.isRegularFile(configuration.path())
                                && !Files.exists(expected.resolve("client.properties")),
                        "saved client settings use the selected root file rather than the cache");
                Files.writeString(expected.resolve("client.properties"),
                        "clientSize=1\nwidth=1800\nheight=1000\ncoloredGameMessages=false\n");
                ClientSettings rootSettings = ClientSettings.load();
                check(rootSettings.clientSize == 0 && rootSettings.width == 1280
                                && rootSettings.height == 800 && rootSettings.coloredGameMessages,
                        "a stale cache preference file cannot override the selected root configuration");

                Path data = expected.resolve("main_file_cache.dat");
                long preservedLength = 0x3200000L + 32;
                try (RandomAccessFile file = new RandomAccessFile(data.toFile(), "rw")) {
                    file.setLength(preservedLength);
                    file.writeLong(0x4c554e4143414348L);
                    file.seek(preservedLength - 1);
                    file.write(0x5a);
                }
                signlink.startpriv(InetAddress.getLoopbackAddress());
                check(signlink.cache_dat != null, "startup waits for its cache store to open");
                check(signlink.cache_dat.length() == preservedLength, "startup never deletes an existing large cache store");
                signlink.cache_dat.seek(0);
                check(signlink.cache_dat.readLong() == 0x4c554e4143414348L,
                        "startup preserves the existing cache contents");
                for (int index = 0; index < signlink.cache_idx.length; index++)
                    check(signlink.cache_idx[index] != null
                                    && Files.isRegularFile(expected.resolve("main_file_cache.idx" + index)),
                            "every cache index is opened directly in the canonical directory");
                check(Files.isRegularFile(expected.resolve("uid.dat")), "client UID is stored beside cache files");
                check(!Files.exists(expected.resolve("cache")), "startup never creates cache/cache");

                Path blockedHome = fixture.resolve("blocked-user");
                Files.createDirectories(blockedHome);
                Files.writeString(blockedHome.resolve("luna"), "a file prevents this directory");
                System.setProperty("user.home", blockedHome.toString());
                try {
                    signlink.findcachedir();
                    throw new AssertionError("an unwritable cache root must fail explicitly");
                } catch (UncheckedIOException exception) {
                    check(exception.getMessage().contains(blockedHome.resolve("luna").resolve("cache").toString()),
                            "cache initialization failure identifies the resolved directory");
                }
            } finally {
                signlink.threadliveid++;
                closeStores();
                signlink.musicPlayer.close();
                signlink.wavePlayer.close();
            }
        }
        System.out.println("CacheLocationTest passed");
    }

    private static void closeStores() throws IOException {
        if (signlink.cache_dat != null) {
            signlink.cache_dat.close();
            signlink.cache_dat = null;
        }
        for (int index = 0; index < signlink.cache_idx.length; index++) {
            if (signlink.cache_idx[index] != null) {
                signlink.cache_idx[index].close();
                signlink.cache_idx[index] = null;
            }
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
