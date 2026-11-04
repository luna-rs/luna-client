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
        playBytes(lastSound);
        return true;
    }

    public boolean replay() {
        if (closed || lastSound == null) return false;
        playBytes(lastSound);
        return true;
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
            try {
                var a = window._lunaAudio;
                if (!a || a.state !== 'running') return;
                if (!window._lunaGain) {
                    window._lunaGain = a.createGain();
                    window._lunaGain.gain.value=typeof window._lunaVolume === 'number' ? window._lunaVolume : 1;
                    window._lunaGain.connect(a.destination);
                }
                if (!window._lunaSounds) window._lunaSounds = new Set();
                var generation = window._lunaSoundGeneration || 0;
                a.decodeAudioData(data.buffer.slice(data.byteOffset, data.byteOffset + data.byteLength))
                    .then(function(buffer) {
                    if ((window._lunaSoundGeneration || 0) !== generation) return;
                    var s = a.createBufferSource(); s.buffer = buffer; s.connect(window._lunaGain);
                    if (window._lunaSounds.size >= 16) {
                        var oldest = window._lunaSounds.values().next().value;
                        window._lunaSounds.delete(oldest);
                        oldest.stop(); oldest.disconnect();
                    }
                    window._lunaSounds.add(s);
                    s.onended = function() { window._lunaSounds.delete(s); s.disconnect(); };
                    s.start();
                }).catch(function(error) { console.warn('Browser effect playback: ' + error); });
            } catch (error) { console.warn('Browser effect playback: ' + error); }
            """)
    private static native void playBytes(@JSByRef(optional = true) byte[] data);

    @JSBody(params = "gain", script = """
            window._lunaVolume=Math.max(0,Math.min(1,gain));
            if(window._lunaGain)window._lunaGain.gain.value=window._lunaVolume;
            """)
    private static native void setGain(double gain);

    @JSBody(script = """
            window._lunaSoundGeneration=(window._lunaSoundGeneration||0)+1;
            if(window._lunaSounds){
                window._lunaSounds.forEach(function(sound) {sound.stop();sound.disconnect();});
                window._lunaSounds.clear();
            }
            """)
    private static native void stopAll();
}
