import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Map;

import luna.ClientConfiguration;

/**
 * Remembers window and game-message preferences in the selected root configuration.
 *
 * @author lare96
 */
public final class ClientSettings {
    public static final int DEFAULT_WIDTH = ClientConfiguration.get().integer("defaultWidth", 765, 32768);
    public static final int DEFAULT_HEIGHT = ClientConfiguration.get().integer("defaultHeight", 503, 32768);

    /**
     * Zero selects fixed mode and one selects resizable mode.
     */
    public int clientSize = ClientConfiguration.get().integer("clientSize", 0, 1);
    public int width = clampWidth(ClientConfiguration.get().integer("width", 765, 32768));
    public int height = clampHeight(ClientConfiguration.get().integer("height", 503, 32768));
    public boolean coloredGameMessages = ClientConfiguration.get().bool("coloredGameMessages");

    /**
     * Restores the configured resizable preset while leaving the selected mode unchanged.
     */
    public void resetResizableSize() {
        width = clampWidth(DEFAULT_WIDTH);
        height = clampHeight(DEFAULT_HEIGHT);
    }

    /**
     * Reads saved preferences independently of the game cache.
     */
    public static ClientSettings load() {
        ClientSettings settings = new ClientSettings();
        ClientConfiguration selected = ClientConfiguration.get();
        ClientConfiguration configuration = java.nio.file.Files.exists(selected.path())
                ? ClientConfiguration.load(selected.path()) : selected;
        settings.clientSize = configuration.integer("clientSize", 0, 1);
        settings.width = clampWidth(configuration.integer("width", 765, 32768));
        settings.height = clampHeight(configuration.integer("height", 503, 32768));
        settings.coloredGameMessages = configuration.bool("coloredGameMessages");
        return settings;
    }

    public void save() {
        try {
            ClientConfiguration.get().savePreferences(Map.of(
                    "clientSize", String.valueOf(clientSize), "width", String.valueOf(width),
                    "height", String.valueOf(height), "coloredGameMessages", String.valueOf(coloredGameMessages)));
        } catch (IOException exception) {
            System.err.println("Unable to save client preferences to " + ClientConfiguration.get().path());
        }
    }

    /**
     * Keeps a saved window width between the native minimum and the current screen.
     */
    public static int clampWidth(int width) {
        return Math.max(JagApplet.MIN_CONTENT_WIDTH, Math.min(width, screenSize().width));
    }

    /**
     * Keeps a saved window height between the native minimum and the current screen.
     */
    public static int clampHeight(int height) {
        return Math.max(JagApplet.MIN_CONTENT_HEIGHT, Math.min(height, screenSize().height));
    }

    private static Dimension screenSize() {
        if (GraphicsEnvironment.isHeadless()) return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
}
