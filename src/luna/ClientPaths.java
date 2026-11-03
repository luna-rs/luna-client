package luna;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Defines the single writable cache directory shared by client services.
 *
 * @author lare96
 */
public final class ClientPaths {

    private ClientPaths() {
    }

    /**
     * Resolves the cache path selected in the root properties file.
     */
    public static Path cacheDirectory() {
        return ClientConfiguration.get().cacheDirectory();
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
