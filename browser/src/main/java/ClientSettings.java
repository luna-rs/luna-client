import java.io.IOException;
import java.util.Map;

import luna.ClientConfiguration;
import luna.browser.awt.BrowserDisplay;

/**
 * Starts the browser in the selected page layout without changing desktop preferences.
 *
 * @author lare96
 */
public final class ClientSettings {
    public static final boolean DESKTOP_FULLSCREEN_TRIM = false;
    public static final int LOGIN_CONTROLS_OFFSET_Y = 0;
    public static final int DEFAULT_WIDTH = 765;
    public static final int DEFAULT_HEIGHT = 503;
    public int clientSize = BrowserDisplay.initialMode();
    public int width = clampWidth(BrowserDisplay.availableWidth());
    public int height = clampHeight(BrowserDisplay.availableHeight());
    public boolean coloredGameMessages = ClientConfiguration.get().bool("coloredGameMessages");

    public static ClientSettings load() {
        return new ClientSettings();
    }

    public void save() {
        try {
            ClientConfiguration.get().savePreferences(Map.of("clientSize", String.valueOf(clientSize),
                    "width", String.valueOf(width), "height", String.valueOf(height),
                    "coloredGameMessages", Boolean.toString(coloredGameMessages)));
        } catch (IOException exception) {
            System.err.println("Unable to save browser client preferences.");
        }
    }

    public void resetResizableSize() {
        width = clampWidth(BrowserDisplay.availableWidth());
        height = clampHeight(BrowserDisplay.availableHeight());
    }

    public static int clampWidth(int width) {
        return Math.max(DEFAULT_WIDTH, Math.min(32768, width));
    }

    public static int clampHeight(int height) {
        return Math.max(DEFAULT_HEIGHT, Math.min(32768, height));
    }
}
