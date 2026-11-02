package luna;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import luna.browser.io.BrowserIO;
import luna.browser.io.RandomAccessFile;

/**
 * Installs verified game stores in TeaVM's virtual filesystem before shared cache decoding starts.
 *
 * @author lare96
 */
public final class CacheBootstrap {
    private static final String[] STORES = {"main_file_cache.dat", "main_file_cache.idx0",
            "main_file_cache.idx1", "main_file_cache.idx2", "main_file_cache.idx3", "main_file_cache.idx4"};

    private CacheBootstrap() {
    }

    public static void installBundledCache() throws IOException {
        String manifest = new String(BrowserIO.fetchBytes("cache/cache.sha256"), StandardCharsets.US_ASCII);
        Map<String, String> hashes = new LinkedHashMap<>();
        for (String line : manifest.split("\\r?\\n")) {
            if (line.length() < 67 || !line.substring(0, 64).matches("[0-9a-f]{64}")
                    || !line.substring(64, 66).equals("  "))
                throw new IOException("Invalid browser cache manifest");
            String name = line.substring(66);
            boolean expected = false;
            for (String store : STORES) if (store.equals(name)) expected = true;
            if (!expected || hashes.put(name, line.substring(0, 64)) != null)
                throw new IOException("Unexpected browser cache store: " + name);
        }
        if (hashes.size() != STORES.length) throw new IOException("Incomplete browser cache manifest");
        byte[][] stores = new byte[STORES.length][];
        for (int index = 0; index < STORES.length; index++) {
            String name = STORES[index];
            System.out.println("Loading browser cache: " + name);
            stores[index] = BrowserIO.fetchCheckedBytes("cache/" + name, hashes.get(name));
            if (stores[index].length == 0) throw new IOException("Empty browser cache store: " + name);
        }
        String directory = ClientPaths.ensureCacheDirectory();
        for (int index = 0; index < STORES.length; index++) {
            RandomAccessFile.register(directory + "/" + STORES[index], stores[index]);
        }
        System.out.println("Browser cache ready: " + directory + " (six SHA-256 verified stores)");
    }
}
