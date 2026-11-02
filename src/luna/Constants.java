package luna;

public final class Constants {

    /**
     * Selects the beta release while allowing isolated development endpoints through JVM properties.
     */

    public static final boolean BETA_MODE = Boolean.getBoolean("luna.beta");
    public static final boolean USE_LOCAL_SERVER = Boolean.getBoolean("luna.local");
    public static final String BETA_HOST = "85.155.184.204";
    public static final String LOCAL_HOST = "127.0.0.1";
    public static final String CONNECT_HOST = System.getProperty("luna.host",
            USE_LOCAL_SERVER || !BETA_MODE ? LOCAL_HOST : BETA_HOST);
    public static final String CLIENT_NAME = "luna-rs #377" + (BETA_MODE ? " (beta)" : "");

    public static final int CONNECT_PORT = Integer.parseInt(System.getProperty("luna.port", "43594"));

    public static final boolean DECODE_RSA = true;
    public static final int NPC_BITS = 14;
    public static final boolean DEBUG = true;

    private Constants() {
    }
}

