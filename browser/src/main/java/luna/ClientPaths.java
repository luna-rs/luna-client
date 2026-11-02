package luna;

import java.io.File;
import java.io.IOException;

/**
 * Resolves the TeaVM virtual cache without accessing the user's desktop filesystem.
 *
 * @author lare96
 */
public final class ClientPaths {
    private ClientPaths() {
    }

    public static String cacheDirectory() {
        return System.getProperty("user.home", "/files") + "/luna/cache";
    }

    public static String ensureCacheDirectory() throws IOException {
        File directory = new File(cacheDirectory());
        if (!directory.isDirectory() && !directory.mkdirs())
            throw new IOException("Cannot create browser cache " + directory);
        return directory.getPath();
    }
}
