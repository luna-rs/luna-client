/**
 * Keeps the browser at the classic fixed dimensions without changing desktop preferences.
 *
 * @author lare96
 */
public final class ClientSettings {
    public static final int DEFAULT_WIDTH = 765;
    public static final int DEFAULT_HEIGHT = 503;
    public int clientSize;
    public int width = DEFAULT_WIDTH;
    public int height = DEFAULT_HEIGHT;

    public static ClientSettings load() {
        return new ClientSettings();
    }

    public void save() {
        clientSize = 0;
        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
    }

    public void resetResizableSize() {
        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
    }

    public static int clampWidth(int width) {
        return DEFAULT_WIDTH;
    }

    public static int clampHeight(int height) {
        return DEFAULT_HEIGHT;
    }
}
