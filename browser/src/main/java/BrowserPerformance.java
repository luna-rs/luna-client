import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import luna.browser.awt.BrowserFramePresenter;

/**
 * Exposes optional benchmark controls and completed-canvas timing to the browser host page.
 *
 * @author lare96
 */
public final class BrowserPerformance {
    private static JagApplet applet;
    private static boolean reported;

    private BrowserPerformance() {
    }

    /**
     * Receives explicit capture commands from the demo's buttons or a developer console.
     *
     * @author lare96
     */
    @JSFunctor
    public interface Control extends JSObject {
        String command(String action, int seconds, String label);
    }

    /**
     * Receives completed presentation-stage durations measured with the browser's monotonic clock.
     *
     * @author lare96
     */
    @JSFunctor
    public interface Presentation extends JSObject {
        void complete(double conversionMilliseconds, double uploadMilliseconds);
    }

    public static void install(JagApplet owner) {
        if (!requested()) return;
        applet = owner;
        install(BrowserPerformance::command, (conversion, upload) -> {
            ClientPerformance.addStageNanos(ClientPerformance.CONVERSION, (long) (conversion * 1_000_000));
            ClientPerformance.addStageNanos(ClientPerformance.UPLOAD, (long) (upload * 1_000_000));
            ClientPerformance.presented();
        });
    }

    private static String command(String action, int seconds, String label) {
        if ("start".equals(action)) {
            String settings = "browser; backend=" + (BrowserFramePresenter.isWasmGC() ? "wasm" : "js")
                    + "; framebuffer=" + applet.getContentWidth() + "x" + applet.getContentHeight()
                    + "; renderThreads=1; framePacing=" + (BrowserFrameClock.enabled() ? "raf" : "timer");
            if (applet instanceof client game) {
                settings += "; clientSize=" + game.clientSize + "; viewport=" + game.layout.viewport.width
                        + "x" + game.layout.viewport.height;
                settings += "; camera1216..1220=" + game.anInt1216 + "," + game.anInt1217 + ","
                        + game.anInt1218 + "," + game.anInt1219 + "," + game.anInt1220;
                settings += "; localPlayers=" + game.localPlayerCount + "; localNpcs=" + game.localNpcCount;
            }
            ClientPerformance.start(seconds, label, applet.getEffectiveFramerate(), environment(), settings);
            reported = false;
            return "{\"state\":\"capturing\"}";
        }
        if ("stop".equals(action)) ClientPerformance.stop();
        return ClientPerformance.json();
    }

    public static void poll() {
        if (reported || !ClientPerformance.isComplete()) return;
        reported = true;
        publish(ClientPerformance.json());
    }

    @JSBody(script = "return navigator.userAgent+'; hardwareConcurrency='+navigator.hardwareConcurrency;")
    private static native String environment();

    @JSBody(script = "return new URLSearchParams(location.search).has('profile');")
    private static native boolean requested();

    @JSBody(params = "json", script = "window.LunaPerformance.completed(JSON.parse(json));")
    private static native void publish(String json);

    @JSBody(params = {"control", "presentation"}, script = "window.LunaPerformanceInstall(control,presentation);")
    private static native void install(Control control, Presentation presentation);
}
