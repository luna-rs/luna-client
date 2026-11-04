package luna.browser;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSExceptions;
import org.teavm.jso.JSObject;

/**
 * Reports native JavaScript frames associated with browser client failures.
 *
 * @author lare96
 */
public final class BrowserDiagnostics {

    private BrowserDiagnostics() {
    }

    public static void report(Throwable exception) {
        JSObject nativeException = JSExceptions.getJSException(exception);
        if (nativeException != null) {
            reportNative(nativeException);
        }
    }

    @JSBody(params = "exception", script = "console.error('Browser exception stack: ' + (exception.stack || String(exception)));")
    private static native void reportNative(JSObject exception);
}
