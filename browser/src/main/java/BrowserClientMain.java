import luna.CacheBootstrap;
import luna.ClientPaths;
import luna.Constants;
import luna.RsaParser;
import org.teavm.jso.JSBody;
import sign.signlink;

/**
 * Boots the shared Java client using the browser platform without the desktop process lifecycle.
 *
 * @author lare96
 */
public final class BrowserClientMain {

    private static boolean started;
    private static long startedAt;

    private BrowserClientMain() {
    }

    public static void main(String[] args) {
        if (started) return;
        started = true;
        startedAt = System.currentTimeMillis();
        boolean beta = betaProfile();
        System.setProperty("luna.beta", Boolean.toString(beta));
        System.setProperty("luna.local", Boolean.toString(!beta));
        System.setProperty("luna.host", configuredHost(beta));
        System.setProperty("luna.port", Integer.toString(configuredPort(beta)));
        System.setProperty("user.home", "/files");
        System.out.println("TeaVM: shared Java client starting");
        System.out.println(Constants.CLIENT_NAME);
        System.out.println("Server: " + Constants.CONNECT_HOST + ":" + Constants.CONNECT_PORT);
        System.out.println("Browser cache: " + ClientPaths.cacheDirectory());
        try {
            CacheBootstrap.installBundledCache();
            RsaParser.parse();
            client.world = 10;
            client.portOffset = 0;
            client.switchToHighMem();
            client.memberServer = true;
            signlink.storeid = 32;
            signlink.startpriv(luna.browser.net.InetAddress.getByName(Constants.CONNECT_HOST));
            byte[] title = new FileStore(1, 0x927c0, signlink.cache_dat, signlink.cache_idx[0]).get(1);
            if (title == null) {
                throw new IllegalStateException("The verified browser cache could not read its title archive");
            }
            Archive titleArchive = new Archive(title);
            if (titleArchive.get("p11_full.dat") == null)
                throw new IllegalStateException("The browser cache title archive is missing its font data");
            System.out.println("Shared cache decoder ready: " + title.length + " title archive bytes");
            client.settings = ClientSettings.load();
            SceneGraph.setResizableDrawDistanceIncrease(0);
            ParallelRasterizer.setThreadCount(1);
            client game = new client();
            game.start(765, 503);
        } catch (Exception exception) {
            exception.printStackTrace();
            showStatus(exception.toString());
        }
    }

    public static void clientReady() {
        System.out.println("TeaVM client ready in " + (System.currentTimeMillis() - startedAt) + " ms");
        showStatus("Client running");
    }

    public static void clientFailed() {
        showStatus("Client could not load; see startup log");
    }

    @JSBody(script = "return document.getElementById('server').value === 'beta';")
    private static native boolean betaProfile();

    @JSBody(params = "beta", script = """
            var c=window.LUNA_CLIENT_CONFIG;
            return c?c[beta?'beta':'development'].host:(beta?'85.155.184.204':'127.0.0.1');
            """)
    private static native String configuredHost(boolean beta);

    @JSBody(params = "beta", script = """
            var c=window.LUNA_CLIENT_CONFIG;
            return c?c[beta?'beta':'development'].port:43594;
            """)
    private static native int configuredPort(boolean beta);

    @JSBody(params = "message", script = "document.getElementById('status').textContent = message;")
    private static native void showStatus(String message);
}
