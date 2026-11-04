package luna.browser.net;

import java.io.IOException;

/**
 * Retains the configured endpoint label without attempting unavailable browser DNS operations.
 *
 * @author lare96
 */
public final class InetAddress {
    private final String host;

    private InetAddress(String host) {
        this.host = host;
    }

    public static InetAddress getByName(String host) throws IOException {
        if (host == null || host.isEmpty()) throw new IOException("Missing server hostname");
        return new InetAddress(host);
    }

    public String getHostName() {
        return host;
    }

    public String getHostAddress() {
        return host;
    }
}
