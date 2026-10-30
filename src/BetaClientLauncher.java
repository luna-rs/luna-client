/**
 * Selects beta branding and the beta endpoint before the client initializes its configuration.
 *
 * @author lare96
 */
public final class BetaClientLauncher {

    private BetaClientLauncher() {
    }

    public static void main(String[] args) {
        System.setProperty("luna.beta", "true");
        ClientLauncher.main(args);
    }
}
