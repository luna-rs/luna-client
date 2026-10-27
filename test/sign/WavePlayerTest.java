package sign;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Exercises sound resource ownership without requiring an audio device.
 *
 * @author lare96
 */
public final class WavePlayerTest {
    private static final File SOUND = new File("test.wav");

    public static void main(String[] args) throws Exception {
        completedSoundsReleaseResources();
        overlappingSoundsAreBounded();
        failuresReleaseResources();
        shutdownReleasesAndRejectsSounds();
        shutdownDuringOpenReleasesSound();
        System.out.println("WavePlayerTest passed");
    }

    private static void completedSoundsReleaseResources() {
        FakeFactory factory = new FakeFactory();
        WavePlayer player = new WavePlayer(factory, 16);
        for (int i = 0; i < 1000; i++) {
            player.play(SOUND);
            FakeClip clip = factory.last();
            check(clip.started && factory.streamsClosed == i + 1, "Sound and input stream lifecycle");
            clip.finish();
            check(clip.closes == 1 && factory.openClips == 0, "Completed clip released");
        }
        player.close();
        check(factory.last().closes == 1, "Completed clips are no longer retained");
    }

    private static void overlappingSoundsAreBounded() {
        FakeFactory factory = new FakeFactory();
        WavePlayer player = new WavePlayer(factory, 2);
        for (int i = 0; i < 100; i++) {
            player.play(SOUND);
            check(factory.openClips <= 2 && factory.maximumOpen <= 2, "Clip cap enforced before opening");
            if (i >= 2) {
                check(factory.created.get(i - 2).closes == 1, "Oldest clip evicted");
            }
        }
        player.close();
        check(factory.openClips == 0, "Overlapping clips closed");
    }

    private static void failuresReleaseResources() {
        FakeFactory factory = new FakeFactory();
        WavePlayer player = new WavePlayer(factory, 2);
        factory.failOpen = true;
        player.play(SOUND);
        check(factory.last().closes == 1 && factory.streamsClosed == 1, "Failed open released clip and stream");
        factory.failOpen = false;
        factory.failStream = true;
        player.play(SOUND);
        check(factory.last().closes == 1, "Failed stream creation released clip");
        factory.failStream = false;
        player.play(SOUND);
        check(factory.last().started, "Playback recovers after failure");
        player.close();
    }

    private static void shutdownReleasesAndRejectsSounds() {
        FakeFactory factory = new FakeFactory();
        WavePlayer player = new WavePlayer(factory, 2);
        player.play(SOUND);
        player.play(SOUND);
        factory.callbackOnAnotherThread = true;
        player.close();
        player.close();
        player.play(SOUND);
        check(factory.openClips == 0 && factory.created.size() == 2, "Shutdown closes and rejects clips");
        for (FakeClip clip : factory.created) {
            check(clip.closes == 1, "Shutdown is idempotent");
        }
    }

    private static void shutdownDuringOpenReleasesSound() throws Exception {
        FakeFactory factory = new FakeFactory();
        factory.openEntered = new CountDownLatch(1);
        factory.openAllowed = new CountDownLatch(1);
        WavePlayer player = new WavePlayer(factory, 2);
        Thread producer = new Thread(() -> player.play(SOUND));
        producer.start();
        check(factory.openEntered.await(2, TimeUnit.SECONDS), "Producer entered open");
        player.close();
        factory.openAllowed.countDown();
        producer.join(2000);
        check(!producer.isAlive(), "Producer finishes after shutdown");
        check(factory.last().closes == 1 && !factory.last().started && factory.openClips == 0,
                "Clip opened during shutdown is released without starting");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * Supplies observable clips and input streams for lifecycle tests.
     *
     * @author lare96
     */
    private static final class FakeFactory implements WavePlayer.Factory {
        final List<FakeClip> created = new ArrayList<FakeClip>();
        int streamsClosed;
        int openClips;
        int maximumOpen;
        boolean failOpen;
        boolean failStream;
        boolean callbackOnAnotherThread;
        CountDownLatch openEntered;
        CountDownLatch openAllowed;

        public Clip createClip() {
            FakeClip clip = new FakeClip(this);
            created.add(clip);
            return clip.proxy;
        }

        public AudioInputStream openStream(File file) throws IOException {
            if (failStream) {
                throw new IOException("Expected test stream failure");
            }
            return new AudioInputStream(new ByteArrayInputStream(new byte[4]),
                    new AudioFormat(22050, 16, 1, true, false), 2) {
                public void close() throws IOException {
                    super.close();
                    streamsClosed++;
                }
            };
        }

        FakeClip last() {
            return created.get(created.size() - 1);
        }
    }

    /**
     * Models clip events and resource allocation with a dynamic proxy.
     *
     * @author lare96
     */
    private static final class FakeClip implements InvocationHandler {
        final FakeFactory factory;
        final Clip proxy;
        final List<LineListener> listeners = new ArrayList<LineListener>();
        boolean open;
        boolean started;
        int closes;

        FakeClip(FakeFactory factory) {
            this.factory = factory;
            proxy = (Clip) Proxy.newProxyInstance(Clip.class.getClassLoader(), new Class<?>[]{Clip.class}, this);
        }

        public Object invoke(Object target, Method method, Object[] args) throws Exception {
            String name = method.getName();
            if (name.equals("hashCode")) {
                return System.identityHashCode(proxy);
            }
            if (name.equals("equals")) {
                return proxy == args[0];
            }
            if (name.equals("open")) {
                if (factory.openEntered != null) {
                    factory.openEntered.countDown();
                    check(factory.openAllowed.await(2, TimeUnit.SECONDS), "Opening permitted");
                }
                if (factory.failOpen) {
                    throw new LineUnavailableException("Expected test open failure");
                }
                open = true;
                factory.openClips++;
                factory.maximumOpen = Math.max(factory.maximumOpen, factory.openClips);
            } else if (name.equals("addLineListener")) {
                listeners.add((LineListener) args[0]);
            } else if (name.equals("start")) {
                check(open, "Only open clips start");
                started = true;
            } else if (name.equals("close")) {
                closes++;
                if (open) {
                    open = false;
                    factory.openClips--;
                }
                if (factory.callbackOnAnotherThread) {
                    Thread callback = new Thread(this::finish);
                    callback.setDaemon(true);
                    callback.start();
                    callback.join(2000);
                    check(!callback.isAlive(), "Clip close callback must not block on player lock");
                } else {
                    finish();
                }
            } else {
                throw new UnsupportedOperationException(name);
            }
            return null;
        }

        void finish() {
            for (LineListener listener : listeners) {
                listener.update(new LineEvent(proxy, LineEvent.Type.STOP, 0));
            }
        }
    }
}
