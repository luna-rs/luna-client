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

    @JSBody(params = "title", script = """
            document.title=title;
            document.getElementById('title').textContent=title;
            """)
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

    @JSBody(script = "return window.LunaBrowser.initialMode();")
    public static native int initialMode();

    @JSBody(script = "return window.LunaBrowser.availableWidth();")
    public static native int availableWidth();

    @JSBody(script = "return window.LunaBrowser.availableHeight();")
    public static native int availableHeight();

    @JSBody(script = "return document.hidden;")
    public static native boolean hidden();

    @JSBody(params = {"updates", "frames", "totalUpdates", "loggedIn", "loadingStage", "outboundBytes", "opcode",
            "lastOpcode", "packetSize", "silentTicks"}, script = """
            var lunaFrameSocket = window._lunaSocket;
            var lunaQueuedBytes = 0;
            if (lunaFrameSocket && lunaFrameSocket.queue) {
                for (var lunaQueueIndex = 0; lunaQueueIndex < lunaFrameSocket.queue.length; lunaQueueIndex++) {
                    lunaQueuedBytes += lunaFrameSocket.queue[lunaQueueIndex].length;
                }
                lunaQueuedBytes -= lunaFrameSocket.offset || 0;
            }
            var lunaRuntimeStatus = {updates: updates, frames: frames, totalUpdates: totalUpdates, loggedIn: loggedIn,
                loadingStage: loadingStage, outboundBytes: outboundBytes, opcode: opcode, lastOpcode: lastOpcode,
                packetSize: packetSize, silentTicks: silentTicks, socketQueuedBytes: lunaQueuedBytes,
                socketReceivedBytes: lunaFrameSocket ? lunaFrameSocket.receivedBytes || 0 : 0,
                socketSentBytes: lunaFrameSocket ? lunaFrameSocket.sentBytes || 0 : 0,
                socketReadyState: lunaFrameSocket && lunaFrameSocket.socket ? lunaFrameSocket.socket.readyState : -1};
            window.LUNA_RUNTIME_STATUS = lunaRuntimeStatus;
            var lunaRuntimeLabel = document.getElementById('runtime-stats');
            if (lunaRuntimeLabel) lunaRuntimeLabel.textContent = updates + ' updates/s · ' + frames + ' FPS · '
                + (loggedIn ? 'logged in' : 'login screen');
            console.log('TeaVM runtime: ' + JSON.stringify(lunaRuntimeStatus));
            """)
    public static native void stats(int updates, int frames, int totalUpdates, boolean loggedIn,
                                   int loadingStage, int outboundBytes, int opcode, int lastOpcode,
                                   int packetSize, int silentTicks);

    @JSBody(script = """
            var lunaCanvasElement=document.getElementById('game');
            if(lunaCanvasElement)lunaCanvasElement.focus();
            """)
    public static native void focus();

    public static void present(int[] pixels, int width, int height) {
        BrowserFramePresenter.present(pixels, width, height);
    }

    @JSBody(params = {"text", "font"}, script = """
            var lunaCanvasElement=document.createElement('canvas');
            var lunaCanvasGraphics=lunaCanvasElement.getContext('2d');
            lunaCanvasGraphics.font=font;
            return Math.ceil(lunaCanvasGraphics.measureText(text).width);
            """)
    public static native int measureText(String text, String font);

    @JSBody(params = {"text", "font", "size", "color", "sink"}, script = """
            var lunaCanvasElement=document.createElement('canvas');
            var lunaCanvasGraphics=lunaCanvasElement.getContext('2d');
            lunaCanvasGraphics.font=font;
            lunaCanvasElement.width=Math.ceil(lunaCanvasGraphics.measureText(text).width)+4;
            lunaCanvasElement.height=size*2+8;
            lunaCanvasGraphics=lunaCanvasElement.getContext('2d');
            lunaCanvasGraphics.font=font;
            lunaCanvasGraphics.textBaseline='alphabetic';
            lunaCanvasGraphics.fillStyle='#'+(color&0xffffff).toString(16).padStart(6,'0');
            lunaCanvasGraphics.fillText(text,0,size+2);
            var lunaDecodedPixels=lunaCanvasGraphics.getImageData(0,0,
                lunaCanvasElement.width,lunaCanvasElement.height).data;
            for(var lunaPixelY=0;lunaPixelY<lunaCanvasElement.height;lunaPixelY++){
                for(var lunaPixelX=0;lunaPixelX<lunaCanvasElement.width;lunaPixelX++){
                    var lunaPixelIndex=(lunaPixelY*lunaCanvasElement.width+lunaPixelX)*4;
                    if(lunaDecodedPixels[lunaPixelIndex+3]){
                        sink(lunaPixelX,lunaPixelY-size-2,(lunaDecodedPixels[lunaPixelIndex+3]<<24)
                            |(lunaDecodedPixels[lunaPixelIndex]<<16)|(lunaDecodedPixels[lunaPixelIndex+1]<<8)
                            |lunaDecodedPixels[lunaPixelIndex+2]);
                    }
                }
            }
            """)
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

    @JSBody(params = {"bytes", "callback"}, script = """
            var lunaImageObjectUrl=URL.createObjectURL(new Blob([bytes]));
            var lunaImageElement=new Image();
            lunaImageElement.onload=function(){
                try{
                    var lunaCanvasElement=document.createElement('canvas');
                    lunaCanvasElement.width=lunaImageElement.width;
                    lunaCanvasElement.height=lunaImageElement.height;
                    var lunaCanvasGraphics=lunaCanvasElement.getContext('2d');
                    lunaCanvasGraphics.drawImage(lunaImageElement,0,0);
                    var lunaDecodedPixels=lunaCanvasGraphics.getImageData(0,0,
                        lunaCanvasElement.width,lunaCanvasElement.height).data;
                    var lunaDecodedWords=new Int32Array(lunaCanvasElement.width*lunaCanvasElement.height);
                    for(var lunaPixelIndex=0,lunaByteIndex=0;lunaPixelIndex<lunaDecodedWords.length;
                            lunaPixelIndex++,lunaByteIndex+=4){
                        lunaDecodedWords[lunaPixelIndex]=(lunaDecodedPixels[lunaByteIndex+3]<<24)
                            |(lunaDecodedPixels[lunaByteIndex]<<16)|(lunaDecodedPixels[lunaByteIndex+1]<<8)
                            |lunaDecodedPixels[lunaByteIndex+2];
                    }
                    callback(lunaCanvasElement.width,lunaCanvasElement.height,lunaDecodedWords,null);
                }catch(lunaImageFailure){callback(0,0,null,String(lunaImageFailure));}
                finally{URL.revokeObjectURL(lunaImageObjectUrl);}
            };
            lunaImageElement.onerror=function(){
                URL.revokeObjectURL(lunaImageObjectUrl);
                callback(0,0,null,'Unable to decode cached image');
            };
            lunaImageElement.src=lunaImageObjectUrl;
            """)
    private static native void decodeBytesNative(@JSByRef(optional = true) byte[] bytes, ImageCallback callback);

    @JSBody(params = {"url", "callback"}, script = """
            var lunaImageElement=new Image();
            lunaImageElement.onload=function(){
                try{
                    var lunaCanvasElement=document.createElement('canvas');
                    lunaCanvasElement.width=lunaImageElement.width;
                    lunaCanvasElement.height=lunaImageElement.height;
                    var lunaCanvasGraphics=lunaCanvasElement.getContext('2d');
                    lunaCanvasGraphics.drawImage(lunaImageElement,0,0);
                    var lunaDecodedPixels=lunaCanvasGraphics.getImageData(0,0,
                        lunaCanvasElement.width,lunaCanvasElement.height).data;
                    var lunaDecodedWords=new Int32Array(lunaCanvasElement.width*lunaCanvasElement.height);
                    for(var lunaPixelIndex=0,lunaByteIndex=0;lunaPixelIndex<lunaDecodedWords.length;
                            lunaPixelIndex++,lunaByteIndex+=4){
                        lunaDecodedWords[lunaPixelIndex]=(lunaDecodedPixels[lunaByteIndex+3]<<24)
                            |(lunaDecodedPixels[lunaByteIndex]<<16)|(lunaDecodedPixels[lunaByteIndex+1]<<8)
                            |lunaDecodedPixels[lunaByteIndex+2];
                    }
                    callback(lunaCanvasElement.width,lunaCanvasElement.height,lunaDecodedWords,null);
                }catch(lunaImageFailure){callback(0,0,null,String(lunaImageFailure));}
            };
            lunaImageElement.onerror=function(){callback(0,0,null,'Unable to load '+url);};
            lunaImageElement.src=typeof window.LunaAssetUrl==='function'?window.LunaAssetUrl(url):url;
            """)
    private static native void decodeUrlNative(String url, ImageCallback callback);
}
