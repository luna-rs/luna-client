import org.teavm.jso.JSBody;

/**
 * Stores browser layout preferences separately from the desktop client's settings file.
 *
 * @author lare96
 */
public final class ClientSettings {
    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 800;
    public int clientSize;
    public int width = DEFAULT_WIDTH;
    public int height = DEFAULT_HEIGHT;

    public static ClientSettings load() {
        ClientSettings settings = new ClientSettings();
        String saved = read();
        if (saved != null) {
            try {
                String[] parts = saved.split(",");
                settings.clientSize = Integer.parseInt(parts[0]) == 1 ? 1 : 0;
                settings.width = clampWidth(Integer.parseInt(parts[1]));
                settings.height = clampHeight(Integer.parseInt(parts[2]));
            } catch (RuntimeException ignored) {
                return new ClientSettings();
            }
        }
        return settings;
    }

    public void save() {
        write(clientSize + "," + width + "," + height);
    }

    public void resetResizableSize() {
        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
    }

    public static int clampWidth(int width) {
        return Math.max(JagApplet.MIN_CONTENT_WIDTH, Math.min(width, 1920));
    }

    public static int clampHeight(int height) {
        return Math.max(JagApplet.MIN_CONTENT_HEIGHT, Math.min(height, 1080));
    }

    @JSBody(script = "try { return localStorage.getItem('luna.teavm.settings'); } catch (error) { return null; }")
    private static native String read();

    @JSBody(params = "value", script = "try { localStorage.setItem('luna.teavm.settings', value); } catch (error) {}")
    private static native void write(String value);
}
