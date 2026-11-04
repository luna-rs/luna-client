package luna;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import luna.browser.io.BrowserIO;

/**
 * Reads browser-hosted runtime assets while sharing their downloaded bytes within the session.
 *
 * @author lare96
 */
public final class ClientResources {
    private static final Map<String, byte[]> resources = new HashMap<>();

    private ClientResources() {
    }

    public static InputStream open(String resource) throws IOException {
        return openBundled(resource);
    }

    public static InputStream openBundled(String resource) throws IOException {
        String name = resource.startsWith("/") ? resource.substring(1) : resource;
        byte[] bytes = resources.get(name);
        if (bytes == null) {
            bytes = BrowserIO.fetchBytes(name);
            resources.put(name, bytes);
        }
        return new ByteArrayInputStream(bytes);
    }
}
