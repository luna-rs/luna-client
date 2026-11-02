package luna.browser.awt;

import luna.browser.awt.image.BufferedImage;
import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSByRef;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.Int32Array;

/**
 * Presents the shared Java framebuffer and translates browser input and image decoding.
 *
 * @author lare96
 */
public final class BrowserDisplay {
    private BrowserDisplay() { }

    @JSBody(params = "title", script = "document.title=title+' · TeaVM';document.getElementById('title').textContent=title+' · TeaVM';")
    public static native void setTitle(String title);

    /**
     * Delivers canvas input to the shared client's existing state fields.
     *
     * @author lare96
     */
    @JSFunctor
    public interface Input extends JSObject {
        void handle(int type, int x, int y, int keyCode, int character);
    }

    /**
     * Receives browser-rasterized loading text pixels.
     *
     * @author lare96
     */
    @JSFunctor
    public interface PixelSink extends JSObject {
        void set(int x, int y, int color);
    }

    /**
     * Transfers decoded image pixels back into the waiting Java continuation.
     *
     * @author lare96
     */
    @JSFunctor
    private interface ImageCallback extends JSObject {
        void complete(int width, int height, JSObject pixels, String failure);
    }

    @JSBody(params = {"width", "height", "input"}, script = "window.LunaBrowser.bind(width,height,input);")
    public static native void bind(int width, int height, Input input);

    @JSBody(params = {"resizable", "width", "height"}, script = "window.LunaBrowser.layout(resizable,width,height);")
    public static native void layout(boolean resizable, int width, int height);

    @JSBody(script = "return document.hidden;")
    public static native boolean hidden();

    @JSBody(params = {"updates", "frames", "totalUpdates", "loggedIn", "loadingStage", "outboundBytes", "opcode", "lastOpcode", "packetSize", "silentTicks"}, script = """
            var socket = window._lunaSocket;
            var queued = 0;
            if (socket && socket.queue) {
                for (var index = 0; index < socket.queue.length; index++) queued += socket.queue[index].length;
                queued -= socket.offset || 0;
            }
            var status = {updates: updates, frames: frames, totalUpdates: totalUpdates, loggedIn: loggedIn,
                loadingStage: loadingStage, outboundBytes: outboundBytes, opcode: opcode, lastOpcode: lastOpcode,
                packetSize: packetSize, silentTicks: silentTicks, socketQueuedBytes: queued,
                socketReceivedBytes: socket ? socket.receivedBytes || 0 : 0,
                socketSentBytes: socket ? socket.sentBytes || 0 : 0,
                socketReadyState: socket && socket.socket ? socket.socket.readyState : -1};
            window.LUNA_RUNTIME_STATUS = status;
            var label = document.getElementById('runtime-stats');
            if (label) label.textContent = updates + ' updates/s · ' + frames + ' FPS · ' + (loggedIn ? 'logged in' : 'login screen');
            console.log('TeaVM runtime: ' + JSON.stringify(status));
            """)
    public static native void stats(int updates, int frames, int totalUpdates, boolean loggedIn,
                                   int loadingStage, int outboundBytes, int opcode, int lastOpcode,
                                   int packetSize, int silentTicks);

    @JSBody(script = "const c=document.getElementById('game');if(c)c.focus();")
    public static native void focus();

    @JSBody(params = {"pixels", "width", "height"}, script = "window.LunaBrowser.present(pixels,width,height);")
    public static native void present(@JSByRef int[] pixels, int width, int height);

    @JSBody(params = {"text", "font"}, script = "const c=document.createElement('canvas');const g=c.getContext('2d');g.font=font;return Math.ceil(g.measureText(text).width);")
    public static native int measureText(String text, String font);

    @JSBody(params = {"text", "font", "size", "color", "sink"}, script = "const c=document.createElement('canvas');let g=c.getContext('2d');g.font=font;"
            + "c.width=Math.ceil(g.measureText(text).width)+4;c.height=size*2+8;g=c.getContext('2d');g.font=font;g.textBaseline='alphabetic';"
            + "g.fillStyle='#'+(color&0xffffff).toString(16).padStart(6,'0');g.fillText(text,0,size+2);"
            + "const data=g.getImageData(0,0,c.width,c.height).data;for(let y=0;y<c.height;y++)for(let x=0;x<c.width;x++){const i=(y*c.width+x)*4;"
            + "if(data[i+3])sink(x,y-size-2,(data[i+3]<<24)|(data[i]<<16)|(data[i+1]<<8)|data[i+2]);}")
    public static native void rasterText(String text, String font, int size, int color, PixelSink sink);

    @Async
    public static native BufferedImage decodeBytes(byte[] bytes);

    private static void decodeBytes(byte[] bytes, AsyncCallback<BufferedImage> callback) {
        decodeBytesNative(bytes, callback(callback));
    }

    @Async
    public static native BufferedImage decodeUrl(String url);

    private static void decodeUrl(String url, AsyncCallback<BufferedImage> callback) {
        decodeUrlNative(url, callback(callback));
    }

    private static ImageCallback callback(AsyncCallback<BufferedImage> callback) {
        return (width, height, pixels, failure) -> {
            if (failure != null) {
                callback.error(new IllegalArgumentException(failure));
                return;
            }
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Int32Array packed = (Int32Array) pixels;
            for (int index = 0; index < image.pixels.length; index++) image.pixels[index] = packed.get(index);
            callback.complete(image);
        };
    }

    @JSBody(params = {"bytes", "callback"}, script = "const url=URL.createObjectURL(new Blob([bytes]));const img=new Image();"
            + "img.onload=()=>{try{const c=document.createElement('canvas');c.width=img.width;c.height=img.height;const g=c.getContext('2d');g.drawImage(img,0,0);"
            + "const data=g.getImageData(0,0,c.width,c.height).data;const out=new Int32Array(c.width*c.height);"
            + "for(let i=0,p=0;i<out.length;i++,p+=4)out[i]=(data[p+3]<<24)|(data[p]<<16)|(data[p+1]<<8)|data[p+2];callback(c.width,c.height,out,null);}"
            + "catch(e){callback(0,0,null,String(e));}finally{URL.revokeObjectURL(url);}};img.onerror=()=>{URL.revokeObjectURL(url);callback(0,0,null,'Unable to decode cached image');};img.src=url;")
    private static native void decodeBytesNative(@JSByRef byte[] bytes, ImageCallback callback);

    @JSBody(params = {"url", "callback"}, script = "const img=new Image();img.onload=()=>{try{const c=document.createElement('canvas');c.width=img.width;c.height=img.height;"
            + "const g=c.getContext('2d');g.drawImage(img,0,0);const data=g.getImageData(0,0,c.width,c.height).data;const out=new Int32Array(c.width*c.height);"
            + "for(let i=0,p=0;i<out.length;i++,p+=4)out[i]=(data[p+3]<<24)|(data[p]<<16)|(data[p+1]<<8)|data[p+2];callback(c.width,c.height,out,null);}"
            + "catch(e){callback(0,0,null,String(e));}};img.onerror=()=>callback(0,0,null,'Unable to load '+url);img.src=url;")
    private static native void decodeUrlNative(String url, ImageCallback callback);
}
