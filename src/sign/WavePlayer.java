package sign;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Bounds sound effect resources, reserves mix headroom and releases completed or muted clips.
 * The signlink thread produces sounds; audio callbacks and shutdown may run concurrently.
 *
 * @author lare96
 */
public final class WavePlayer implements AutoCloseable {
    private static final double MIX_HEADROOM_GAIN = 0.125;

    interface Factory {
        Clip createClip() throws Exception;

        AudioInputStream openStream(File file) throws Exception;
    }

    private final Object lock = new Object();
    private final Set<Clip> clips = new LinkedHashSet<Clip>();
    private final Factory factory;
    private final int limit;
    private int volume;
    private long generation;
    private boolean enabled = true;
    private boolean closed;

    public WavePlayer() {
        this(new Factory() {
            public Clip createClip() throws Exception {
                return AudioSystem.getClip();
            }

            public AudioInputStream openStream(File file) throws Exception {
                return AudioSystem.getAudioInputStream(file);
            }
        }, 16);
    }

    WavePlayer(Factory factory, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Clip limit must be positive");
        }
        this.factory = factory;
        this.limit = limit;
    }

    /**
     * Sets attenuation in hundredths of a decibel for current and future sounds.
     */
    public void setVolume(int hundredthsDb) {
        synchronized (lock) {
            if (closed) {
                return;
            }
            volume = Math.max(-9600, Math.min(0, hundredthsDb));
            applyVolumes();
        }
    }

    /**
     * Mutes current sounds and invalidates queued or opening sounds before a later unmute.
     */
    public void setEnabled(boolean value) {
        List<Clip> stopping;
        synchronized (lock) {
            if (closed || enabled == value) {
                return;
            }
            enabled = value;
            generation++;
            if (value) {
                return;
            }
            stopping = new ArrayList<Clip>(clips);
            clips.clear();
        }
        for (Clip clip : stopping) {
            closeClip(clip);
        }
    }

    long generation() {
        synchronized (lock) {
            return generation;
        }
    }

    /**
     * Loads and starts a sound, closing its input stream after loading.
     */
    public void play(File file) {
        play(file, generation());
    }

    void play(File file, long requestedGeneration) {
        Clip oldest = null;
        synchronized (lock) {
            if (closed || !enabled || requestedGeneration != generation) {
                return;
            }
            if (clips.size() >= limit) {
                oldest = clips.iterator().next();
                clips.remove(oldest);
            }
        }
        closeClip(oldest);

        Clip clip = null;
        try {
            clip = factory.createClip();
            try (AudioInputStream stream = factory.openStream(file)) {
                clip.open(stream);
            }
            final Clip playing = clip;
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    release(playing);
                }
            });
            boolean accepted;
            synchronized (lock) {
                accepted = !closed && enabled && requestedGeneration == generation;
                if (accepted) {
                    clips.add(clip);
                    applyVolumes();
                    clip.start();
                }
            }
            if (!accepted) {
                closeClip(clip);
            }
        } catch (Exception failure) {
            synchronized (lock) {
                clips.remove(clip);
                applyVolumes();
            }
            closeClip(clip);
            System.err.println("Unable to play sound: " + failure);
        }
    }

    private void applyVolume(Clip clip) {
        // Native effects are much hotter than music. Bound their combined requested gain,
        // leaving room for the separately attenuated music even with coherent overlapping effects.
        double gain = Math.pow(10.0, volume / 2000.0) * MIX_HEADROOM_GAIN / Math.max(1, clips.size());
        FloatControl control;
        float value;
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            value = (float) (20.0 * Math.log10(gain));
        } else if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
            control = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
            value = (float) gain;
        } else {
            throw new IllegalStateException("Audio device does not support sound volume control");
        }
        control.setValue(Math.max(control.getMinimum(), Math.min(control.getMaximum(), value)));
    }

    private void applyVolumes() {
        for (Clip clip : clips) {
            applyVolume(clip);
        }
    }

    private void release(Clip clip) {
        boolean removed;
        synchronized (lock) {
            removed = clips.remove(clip);
            if (removed) {
                applyVolumes();
            }
        }
        if (removed) {
            closeClip(clip);
        }
    }

    private void closeClip(Clip clip) {
        if (clip != null) {
            try {
                clip.close();
            } catch (RuntimeException failure) {
                System.err.println("Unable to close sound: " + failure);
            }
        }
    }

    @Override
    public void close() {
        List<Clip> closing;
        synchronized (lock) {
            closed = true;
            closing = new ArrayList<Clip>(clips);
            clips.clear();
        }
        for (Clip clip : closing) {
            closeClip(clip);
        }
    }
}
