import luna.Constants;
import sign.signlink;

/**
 * Verifies that standalone startup and network URL selection use the packaged beta endpoint.
 *
 * @author lare96
 */
public final class BetaConfigurationTest {

    public static void main(String[] args) {
        int previousOffset = client.portOffset;
        try {
            String expectedTitle = "luna-rs #377" + (Boolean.getBoolean("luna.beta") ? " (beta)" : "");
            check(expectedTitle.equals(Constants.CLIENT_NAME), "window branding identifies the selected release");
            String expectedHost = Boolean.getBoolean("luna.local") ? "127.0.0.1"
                    : Boolean.getBoolean("luna.beta") ? "85.155.184.204" : "127.0.0.1";
            check(expectedHost.equals(Constants.CONNECT_HOST) && Constants.CONNECT_PORT == 43594,
                    "the selected endpoint retains the existing game port");
            check(Constants.USE_LOCAL_SERVER == Boolean.getBoolean("luna.local"),
                    "the localhost toggle is applied before the endpoint is selected");
            check(signlink.mainapp == null, "the fixture exercises standalone mode");
            client game = new client();
            client.portOffset = 0;
            check(Constants.CONNECT_HOST.equals(game.getCodeBase().getHost())
                            && game.getCodeBase().getPort() == 80,
                    "standalone resource requests resolve the selected host without an applet or window");
            check(game.method37(-42588).equals(game.getCodeBase().getHost()),
                    "startup host validation admits the same configured host");
            client.portOffset = 7;
            check(game.getCodeBase().getPort() == 87, "existing HTTP port-offset behavior is preserved");
        } finally {
            client.portOffset = previousOffset;
        }
        System.out.println("BetaConfigurationTest passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
