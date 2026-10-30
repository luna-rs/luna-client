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
            check("Luna Beta".equals(Constants.CLIENT_NAME), "the window branding is Luna Beta");
            check("85.155.184.204".equals(Constants.CONNECT_HOST) && Constants.CONNECT_PORT == 43594,
                    "the packaged beta endpoint retains the existing game port");
            check(signlink.mainapp == null, "the fixture exercises standalone mode");
            client game = new client();
            client.portOffset = 0;
            check(Constants.CONNECT_HOST.equals(game.getCodeBase().getHost())
                            && game.getCodeBase().getPort() == 80,
                    "standalone resource requests resolve the beta host without an applet or window");
            check(game.method37(-42588).equals(game.getCodeBase().getHost()),
                    "startup host validation admits the same configured beta host");
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
