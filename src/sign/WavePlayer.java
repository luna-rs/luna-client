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
 * Bounds sound effect resources and releases clips when playback completes.
 * The signlink thread produces sounds; audio callbacks and shutdown may run concurrently.
 *
 * @author lare96
 */
public final class WavePlayer implements AutoCloseable {
    interface Factory {
        Clip createClip() throws Exception;

        AudioInputStream openStream(File file) throws Exception;
    }

    private final Object lock = new Object();
    private final Set<Clip> clips = new LinkedHashSet<Clip>();
    private final Factory factory;
    private final int limit;
    private int volume;
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
            for (Clip clip : clips) {
                applyVolume(clip);
            }
        }
    }

    /**
     * Loads and starts a sound, closing its input stream after loading.
     */
    public void play(File file) {
        Clip oldest = null;
        synchronized (lock) {
            if (closed) {
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
                accepted = !closed;
                if (accepted) {
                    applyVolume(clip);
                    clips.add(clip);
                }
            }
            if (accepted) {
                clip.start();
            } else {
                closeClip(clip);
            }
        } catch (Exception failure) {
            synchronized (lock) {
                clips.remove(clip);
            }
            closeClip(clip);
            System.err.println("Unable to play sound: " + failure);
        }
    }

    private void applyVolume(Clip clip) {
        FloatControl control;
        float value;
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            value = volume / 100.0f;
        } else if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
            control = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
            value = (float) Math.pow(10.0, volume / 2000.0);
        } else {
            throw new IllegalStateException("Audio device does not support sound volume control");
        }
        control.setValue(Math.max(control.getMinimum(), Math.min(control.getMaximum(), value)));
    }

    private void release(Clip clip) {
        boolean removed;
        synchronized (lock) {
            removed = clips.remove(clip);
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
