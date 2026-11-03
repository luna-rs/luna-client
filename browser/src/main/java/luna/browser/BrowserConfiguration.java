package luna.browser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import luna.ClientResources;
import org.teavm.jso.JSBody;

/**
 * Loads the released beta properties before client initialization and isolates browser preferences.
 *
 * @author lare96
 */
public final class BrowserConfiguration {
    private static String contents;

    private BrowserConfiguration() {
    }

    /**
     * Downloads the complete release profile without replacing its endpoint or public RSA settings.
     */
    public static void load() throws IOException {
        if (contents != null) return;
        try (InputStream input = ClientResources.openBundled("client.properties")) {
            byte[] bytes = input.readAllBytes();
            if (bytes.length == 0 || bytes.length > 65536)
                throw new IOException("The released browser client.properties is empty or oversized.");
            contents = new String(bytes, StandardCharsets.UTF_8);
        }
    }

    public static String contents() {
        if (contents == null) throw new IllegalStateException("Browser client.properties must load before the client.");
        return contents;
    }

    @JSBody(script = """
            try { return localStorage.getItem('luna.web.v05beta.client.preferences'); }
            catch (lunaPreferencesReadFailure) { return null; }
            """)
    public static native String savedPreferences();

    @JSBody(script = """
            try { localStorage.removeItem('luna.web.v05beta.client.preferences'); }
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
            try { localStorage.setItem('luna.web.v05beta.client.preferences', preferences); return null; }
            catch (lunaPreferencesWriteFailure) { return String(lunaPreferencesWriteFailure); }
            """)
    private static native String storePreferences(String preferences);
}
