import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import sign.signlink;

/**
 * Remembers the client mode (fixed or resizable) and the size of the resizable window between launches.
 * Stored in the cache directory as client.properties.
 */
public final class ClientSettings {

    private static final String FILE_NAME = "client.properties";

    /** Size of the resizable window the first time it is used. */
    public static final int DEFAULT_WIDTH = 900, DEFAULT_HEIGHT = 600;

    /** 0 for the fixed mode, 1 for the resizable mode. */
    public int clientSize = 0;
    public int width = DEFAULT_WIDTH;
    public int height = DEFAULT_HEIGHT;

    private static File file() {
        return new File(signlink.findcachedir(), FILE_NAME);
    }

    /**
     * Reads the saved settings. A missing or unreadable file gives the defaults (fixed mode).
     */
    public static ClientSettings load() {
        ClientSettings settings = new ClientSettings();
        File file = file();
        if (!file.isFile()) {
            return settings;
        }
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            properties.load(in);
            settings.clientSize = Integer.parseInt(properties.getProperty("clientSize", "0")) == 1 ? 1 : 0;
            settings.width = Integer.parseInt(properties.getProperty("width", String.valueOf(DEFAULT_WIDTH)));
            settings.height = Integer.parseInt(properties.getProperty("height", String.valueOf(DEFAULT_HEIGHT)));
        } catch (IOException | NumberFormatException e) {
            return new ClientSettings();
        }
        settings.width = clampWidth(settings.width);
        settings.height = clampHeight(settings.height);
        return settings;
    }

    public void save() {
        Properties properties = new Properties();
        properties.setProperty("clientSize", String.valueOf(clientSize));
        properties.setProperty("width", String.valueOf(width));
        properties.setProperty("height", String.valueOf(height));
        File file = file();
        try (OutputStream out = new FileOutputStream(file)) {
            properties.store(out, "Luna client settings");
        } catch (IOException e) {
            // the settings are not important enough to bother the player
        }
    }

    /** Keeps a saved window size between the fixed size and the size of the screen. */
    public static int clampWidth(int width) {
        return Math.max(JagApplet.MIN_CONTENT_WIDTH, Math.min(width, screenSize().width));
    }

    public static int clampHeight(int height) {
        return Math.max(JagApplet.MIN_CONTENT_HEIGHT, Math.min(height, screenSize().height));
    }

    private static Dimension screenSize() {
        if (GraphicsEnvironment.isHeadless()) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
}
