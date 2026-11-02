package sign;

import java.util.function.LongConsumer;

/**
 * Reports unavailable browser MIDI playback without blocking shared music state transitions.
 *
 * @author lare96
 */
public final class MidiPlayer implements AutoCloseable {
    private boolean reported;

    public void play(byte[] data, boolean loop) {
        play(data, loop, null);
    }

    public void play(byte[] data, boolean loop, LongConsumer started) {
        if (!reported) {
            reported = true;
            System.out.println("Browser MIDI playback is not implemented in the TeaVM prototype.");
        }
        if (started != null) started.accept(-1);
    }

    public void stop() {
    }

    public void prepareTransition() {
    }

    public void setVolume(int hundredthsDb) {
    }

    @Override
    public void close() {
    }
}
