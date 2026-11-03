import luna.CacheBootstrap;
import luna.ClientConfiguration;
import luna.ClientPaths;
import luna.browser.BrowserConfiguration;
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
        System.setProperty("user.home", "/files");
        try {
            BrowserConfiguration.load();
            ClientConfiguration configuration = ClientConfiguration.get();
            String host = configuration.string("connectHost");
            int portOffset = configuration.integer("portOffset", 0, 65534);
            int port = configuration.integer("connectPort", 1, 65535) + portOffset;
            if (!gatewayMatches(host, port)) {
                throw new IllegalStateException("The browser gateway must match the released beta client.properties.");
            }
            System.out.println("TeaVM: shared Java client starting");
            System.out.println(configuration.string("clientName"));
            System.out.println("Server: " + host + ":" + port);
            System.out.println("Browser cache: " + ClientPaths.cacheDirectory());
            CacheBootstrap.installBundledCache();
            client.world = configuration.integer("world", 0, 65535);
            client.portOffset = portOffset;
            if (configuration.bool("lowMemory")) client.switchToLowMem();
            else client.switchToHighMem();
            client.memberServer = configuration.bool("members");
            signlink.storeid = configuration.integer("cacheStoreId", 0, 255);
            signlink.startpriv(luna.browser.net.InetAddress.getByName(host));
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

    @JSBody(params = {"host", "port"}, script = """
            var lunaBetaGatewayConfig = window.LUNA_CLIENT_CONFIG;
            return !!window.LUNA_WEBSOCKET_URL && !!lunaBetaGatewayConfig
                && !!lunaBetaGatewayConfig.beta && lunaBetaGatewayConfig.beta.host === host
                && lunaBetaGatewayConfig.beta.port === port;
            """)
    private static native boolean gatewayMatches(String host, int port);

    @JSBody(params = "message", script = "document.getElementById('status').textContent = message;")
    private static native void showStatus(String message);
}
