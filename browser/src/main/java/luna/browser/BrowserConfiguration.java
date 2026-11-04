package luna.browser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import luna.browser.io.BrowserIO;
import org.teavm.jso.JSBody;

/**
 * Loads the selected properties before client initialization and isolates browser preferences.
 *
 * @author lare96
 */
public final class BrowserConfiguration {
    private static String contents;

    private BrowserConfiguration() {
    }

    /**
     * Downloads the complete selected profile without replacing its endpoint or public RSA settings.
     */
    public static void load() throws IOException {
        if (contents != null) return;
        String sha256 = propertiesSha256();
        if (sha256 == null || !sha256.matches("[a-f0-9]{64}"))
            throw new IOException("The page contains outdated client files. Reload the page to update the client.");
        byte[] bytes = BrowserIO.fetchCheckedBytes("client.properties", sha256);
        if (bytes.length == 0 || bytes.length > 65536)
            throw new IOException("The selected browser client.properties is empty or oversized.");
        contents = new String(bytes, StandardCharsets.UTF_8);
    }

    @JSBody(script = """
            var checksum = window.LUNA_CLIENT_CONFIG && window.LUNA_CLIENT_CONFIG.propertiesSha256;
            return typeof checksum === 'string' ? checksum : null;
            """)
    private static native String propertiesSha256();

    public static String contents() {
        if (contents == null) throw new IllegalStateException("Browser client.properties must load before the client.");
        return contents;
    }

    @JSBody(script = """
            var config = window.LUNA_CLIENT_CONFIG;
            var key = 'luna.web.preferences.' + JSON.stringify([config.clientName, config.host, config.port]);
            try { return localStorage.getItem(key); }
            catch (lunaPreferencesReadFailure) { return null; }
            """)
    public static native String savedPreferences();

    @JSBody(script = """
            var config = window.LUNA_CLIENT_CONFIG;
            var key = 'luna.web.preferences.' + JSON.stringify([config.clientName, config.host, config.port]);
            try { localStorage.removeItem(key); }
            catch (lunaPreferencesRemoveFailure) { }
            """)
    public static native void clearPreferences();

    /**
     * Saves already-validated mutable preferences in one browser-storage transaction.
     */
    public static void savePreferences(String preferences) throws IOException {
        String failure = storePreferences(preferences);
        if (failure != null) throw new IOException("Unable to save browser client preferences: " + failure);
    }

    @JSBody(params = "preferences", script = """
            var config = window.LUNA_CLIENT_CONFIG;
            var key = 'luna.web.preferences.' + JSON.stringify([config.clientName, config.host, config.port]);
            try { localStorage.setItem(key, preferences); return null; }
            catch (lunaPreferencesWriteFailure) { return String(lunaPreferencesWriteFailure); }
            """)
    private static native String storePreferences(String preferences);
}
