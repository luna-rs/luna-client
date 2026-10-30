package luna;

public final class Constants {

    /**
     * Overrides the selected release's endpoint with localhost when launched with -Dluna.local=true.
     */

    public static final boolean USE_LOCAL_SERVER = Boolean.getBoolean("luna.local");
    public static final String BETA_HOST = "85.155.184.204";
    public static final String LOCAL_HOST = "127.0.0.1";
    public static final String CONNECT_HOST = USE_LOCAL_SERVER ? LOCAL_HOST
       : BETA_HOST ;
    public static final String CLIENT_NAME = "luna-rs #377" + (!USE_LOCAL_SERVER ? " (beta)" : "");

    public static final int CONNECT_PORT = 43594;

    public static final boolean DECODE_RSA = true;
    public static final int NPC_BITS = 14;
    public static final boolean DEBUG = true;

    private Constants() {
    }
}

