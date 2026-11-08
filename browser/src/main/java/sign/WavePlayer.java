package sign;

import java.io.File;
import java.util.Arrays;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSByRef;

/**
 * Plays the shared client's generated WAV effects through browser audio.
 *
 * @author lare96
 */
public final class WavePlayer implements AutoCloseable {
    private byte[] lastSound;
    private boolean closed;

    public void play(File file) {
        throw new UnsupportedOperationException("Browser effects use WAV bytes rather than disk files");
    }

    public boolean play(byte[] data, int length) {
        if (closed || data == null || length <= 0 || length > data.length || length > 2 * 1024 * 1024) return false;
        lastSound = Arrays.copyOf(data, length);
        return playBytes(lastSound);
    }

    public boolean replay() {
        if (closed || lastSound == null) return false;
        return playBytes(lastSound);
    }

    public void setVolume(int hundredthsDb) {
        if (!closed) setGain(Math.pow(10, hundredthsDb / 2000.0));
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        lastSound = null;
        stopAll();
    }

    @JSBody(params = "data", script = """
            return !!(window.LunaBrowser && window.LunaBrowser.playEffect && window.LunaBrowser.playEffect(data));
            """)
    private static native boolean playBytes(@JSByRef(optional = true) byte[] data);

    @JSBody(params = "gain", script = """
            window._lunaVolume=Math.max(0,Math.min(1,gain));
            if(window._lunaGain)window._lunaGain.gain.value=window._lunaVolume;
            """)
    private static native void setGain(double gain);

    @JSBody(script = """
            if (window.LunaBrowser && window.LunaBrowser.stopEffects) window.LunaBrowser.stopEffects();
            """)
    private static native void stopAll();
}
