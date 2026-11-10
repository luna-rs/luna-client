package sign;

import java.util.function.LongConsumer;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSByRef;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Plays shared cache music through the browser's gesture-unlocked SoundFont sequencer.
 *
 * @author lare96
 */
public final class MidiPlayer implements AutoCloseable {
    private boolean closed;

    public void play(byte[] data, boolean loop) {
        play(data, loop, null);
    }

    public void play(byte[] data, boolean loop, LongConsumer started) {
        if (closed || data == null || data.length == 0 || data.length > 2 * 1024 * 1024) {
            if (!closed) stopBrowser();
            if (started != null) started.accept(-1);
            return;
        }
        playBrowser(data, loop, duration -> {
            if (started != null) started.accept(Double.isFinite(duration) && duration >= 0 ? (long) duration : -1);
        });
    }

    public void stop() {
        if (!closed) stopBrowser();
    }

    public void prepareTransition() {
        if (!closed) transitionBrowser();
    }

    public void setVolume(int hundredthsDb) {
        if (!closed) volumeBrowser(hundredthsDb);
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        closeBrowser();
    }

    @JSBody(params = {"data", "loop", "started"}, script = """
            if (window.LunaMidi) window.LunaMidi.play(data, loop, started);
            else started(-1);
            """)
    private static native void playBrowser(@JSByRef(optional = true) byte[] data, boolean loop, Started started);

    @JSBody(script = "if (window.LunaMidi) window.LunaMidi.stop();")
    private static native void stopBrowser();

    @JSBody(script = "if (window.LunaMidi) window.LunaMidi.prepareTransition();")
    private static native void transitionBrowser();

    @JSBody(params = "volume", script = "if (window.LunaMidi) window.LunaMidi.setVolume(volume);")
    private static native void volumeBrowser(int volume);

    @JSBody(script = "if (window.LunaMidi) window.LunaMidi.close();")
    private static native void closeBrowser();

    /**
     * Reports the duration only after playback begins, or a concrete failure.
     *
     * @author lare96
     */
    @JSFunctor
    private interface Started extends JSObject {
        void accept(double duration);
    }
}
