package luna;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Opens packaged runtime assets with optional working-directory overrides for development.
 *
 * @author lare96
 */
public final class ClientResources {

    private ClientResources() {
    }

    /**
     * Opens an existing local asset or the equivalent resource bundled in the executable.
     */
    public static InputStream open(String resource) throws IOException {
        Path local = Paths.get(resource);
        if (Files.isRegularFile(local))
            return new BufferedInputStream(Files.newInputStream(local));
        return openBundled(resource);
    }

    /**
     * Opens a bundled asset independently of the process working directory.
     */
    public static InputStream openBundled(String resource) throws IOException {
        String name = resource.startsWith("/") ? resource.substring(1) : resource;
        InputStream stream = ClientResources.class.getClassLoader().getResourceAsStream(name);
        if (stream == null)
            throw new FileNotFoundException("Missing bundled client resource: " + name);
        return new BufferedInputStream(stream);
    }
}
