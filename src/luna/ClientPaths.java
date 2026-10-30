package luna;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Defines the single writable cache directory shared by client services.
 *
 * @author lare96
 */
public final class ClientPaths {

    private ClientPaths() {
    }

    /**
     * Resolves the cache directly beneath the current user's Luna directory.
     */
    public static Path cacheDirectory() {
        return Paths.get(System.getProperty("user.home"), "luna", "cache").toAbsolutePath().normalize();
    }

    /**
     * Creates the exact cache directory without appending another cache component.
     */
    public static Path ensureCacheDirectory() throws IOException {
        Path directory = cacheDirectory();
        try {
            return Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new IOException("Unable to create Luna cache directory " + directory, exception);
        }
    }
}
