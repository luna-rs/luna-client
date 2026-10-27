package sign;

import java.util.function.IntPredicate;
import java.util.function.LongConsumer;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Coordinates downloaded songs, one-shot jingles and cancellation on the game thread.
 *
 * @author lare96
 */
public final class MusicController {

    /**
     * Submits playback and reports its duration after starting, or -1 on failure.
     *
     * @author lare96
     */
    @FunctionalInterface
    public interface Playback {
        void play(byte[] data, boolean loop, LongConsumer started);
    }

    private final IntPredicate request;
    private final Playback playback;
    private final Runnable stop;
    private final Runnable prepareTransition;
    private boolean enabled = true;
    private int song = -1;
    private int requested = -1;
    private boolean downloading;
    private boolean jingle;
    private long generation;
    private long delay;
    private long remaining = -1;
    private long loadingTime;
    private final AtomicReference<Started> started = new AtomicReference<>();

    public MusicController(IntPredicate request, Playback playback, Runnable stop, Runnable prepareTransition) {
        this.request = request;
        this.playback = playback;
        this.stop = stop;
        this.prepareTransition = prepareTransition;
    }

    public void setSong(int id) {
        if (id == 65535 || id == -1) {
            reset();
            return;
        }
        if (song == id && (jingle || requested == id)) {
            return;
        }
        song = id;
        if (enabled && !jingle) {
            request(id);
        }
    }

    public void playJingle(int id, int milliseconds) {
        if (!enabled || id < 0 || id == 65535) {
            return;
        }
        jingle = true;
        delay = Math.max(0, milliseconds);
        request(id);
    }

    public void setEnabled(boolean value) {
        if (enabled == value) {
            return;
        }
        enabled = value;
        cancel();
        if (enabled && song >= 0) {
            request(song);
        }
    }

    public void reset() {
        song = -1;
        cancel();
    }

    public void accept(int id, byte[] data) {
        if (!enabled || !downloading || id != requested) {
            return;
        }
        downloading = false;
        if (data == null) {
            failed();
            return;
        }
        final long token = generation;
        playback.play(data, !jingle, duration -> started.set(new Started(token, duration)));
    }

    public void tick(int milliseconds) {
        Started result = started.getAndSet(null);
        if (result != null) {
            if (result.generation == generation) {
                if (result.duration < 0) {
                    failed();
                } else if (jingle) {
                    remaining = delay > 0 ? delay : Math.max(20, result.duration);
                    return;
                }
            }
        }
        if (downloading) {
            loadingTime += milliseconds;
            if (loadingTime >= 30000) {
                System.err.println("Music file " + requested + " did not load within 30 seconds; check cache/update server.");
                failed();
            }
        } else if (jingle && remaining >= 0) {
            remaining -= milliseconds;
            if (remaining <= 0) {
                resumeSong();
            }
        }
    }

    private void request(int id) {
        generation++;
        started.set(null);
        remaining = -1;
        loadingTime = 0;
        requested = id;
        downloading = true;
        if (jingle) {
            stop.run();
        } else {
            prepareTransition.run();
        }
        if (!request.test(id)) {
            System.err.println("Invalid music file id: " + id);
            failed();
        }
    }

    private void failed() {
        if (jingle) {
            resumeSong();
        } else {
            cancel();
        }
    }

    private void resumeSong() {
        cancel();
        if (enabled && song >= 0) {
            request(song);
        }
    }

    private void cancel() {
        generation++;
        started.set(null);
        requested = -1;
        downloading = false;
        jingle = false;
        remaining = -1;
        stop.run();
    }

    /**
     * Transfers a worker result without sharing mutable game state.
     *
     * @author lare96
     */
    private static final class Started {
        private final long generation;
        private final long duration;

        private Started(long generation, long duration) {
            this.generation = generation;
            this.duration = duration;
        }
    }
}
