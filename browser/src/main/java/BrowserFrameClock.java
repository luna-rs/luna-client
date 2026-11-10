import luna.browser.awt.BrowserFramePresenter;
import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Waits for browser presentation opportunities while retaining independent fixed-rate game updates.
 *
 * @author lare96
 */
public final class BrowserFrameClock {
    private static boolean enabled;

    private BrowserFrameClock() {
    }

    /**
     * Resumes one waiting Java continuation after either a display tick or the next update deadline.
     *
     * @author lare96
     */
    @JSFunctor
    public interface Resume extends JSObject {
        void run();
    }

    public static void start() {
        enabled = startPacing(BrowserFramePresenter.isWasmGC());
    }

    public static boolean enabled() {
        return enabled;
    }

    public static boolean frameDue(int targetFps) {
        return takeFrame(targetFps);
    }

    public static void stop() {
        enabled = false;
        stopPacing();
    }

    @Async
    public static native void awaitWork(double updateDelayMilliseconds);

    private static void awaitWork(double updateDelayMilliseconds, AsyncCallback<Void> callback) {
        waitForWork(updateDelayMilliseconds, () -> callback.complete(null));
    }

    @JSBody(params = "wasm", script = "return window.LunaBrowser.startPacing(wasm);")
    private static native boolean startPacing(boolean wasm);

    @JSBody(params = "fps", script = "return window.LunaBrowser.takeFrame(fps);")
    private static native boolean takeFrame(int fps);

    @JSBody(params = {"milliseconds", "resume"},
            script = "window.LunaBrowser.waitForWork(milliseconds,resume);")
    private static native void waitForWork(double milliseconds, Resume resume);

    @JSBody(script = "window.LunaBrowser.stopPacing();")
    private static native void stopPacing();
}
