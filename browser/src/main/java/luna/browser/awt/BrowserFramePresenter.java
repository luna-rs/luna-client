package luna.browser.awt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import org.teavm.interop.PlatformMarker;
import org.teavm.interop.Platforms;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSBuffer;
import org.teavm.jso.JSBufferType;
import org.teavm.jso.JSByRef;

/**
 * Presents the same software framebuffer using the buffer representation supported by the selected browser backend.
 *
 * @author lare96
 */
public final class BrowserFramePresenter {
    private static IntBuffer directPixels;

    private BrowserFramePresenter() {
    }

    static void present(int[] pixels, int width, int height) {
        if (isWasmGC()) {
            if (directPixels == null || directPixels.capacity() != pixels.length) {
                directPixels = ByteBuffer.allocateDirect(pixels.length * Integer.BYTES)
                        .order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
            }
            directPixels.clear();
            directPixels.put(pixels);
            directPixels.flip();
            presentDirect(directPixels, width, height);
        } else {
            presentJavaScript(pixels, width, height);
        }
    }

    /**
     * Selects the buffer path at compile time so the other backend does not retain its implementation.
     */
    @PlatformMarker(Platforms.WEBASSEMBLY_GC)
    public static boolean isWasmGC() {
        return false;
    }

    @JSBody(params = {"pixels", "width", "height"}, script = "window.LunaBrowser.present(pixels,width,height);")
    private static native void presentJavaScript(@JSByRef(optional = true) int[] pixels, int width, int height);

    @JSBody(params = {"pixels", "width", "height"}, script = "window.LunaBrowser.present(pixels,width,height);")
    private static native void presentDirect(@JSBuffer(JSBufferType.INT32) IntBuffer pixels, int width, int height);
}
