package luna;

/**
 * Keeps the shared startup hooks available without a native launcher or local socket in the browser.
 *
 * @author lare96
 */
public final class ClientStartup {
    private ClientStartup() {
    }

    public static void initialize() {
    }

    public static boolean hidden() {
        return false;
    }

    public static boolean managed() {
        return false;
    }

    public static void showWhenReady(Runnable reveal) {
        reveal.run();
    }

    public static void prepared() {
    }

    public static void failed() {
    }

    public static float presentationAlpha() {
        return 1f;
    }

    public static void frameShown() {
    }
}
