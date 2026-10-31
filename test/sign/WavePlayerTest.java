package sign;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
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
 * Exercises sound volume and resource ownership without requiring an audio device.
 *
 * @author lare96
 */
public final class WavePlayerTest {
    private static final File SOUND = new File("test.wav");

    public static void main(String[] args) throws Exception {
        soundLevelsApplyBeforePlayback();
        volumeChangesUpdateActiveAndNewSounds();
        volumeChangeDuringOpenUsesLatestSetting();
        volumeAndProviderRangesAreClamped();
        linearVolumeFallback();
        masterGainTakesPriority();
        unsupportedVolumeReleasesSound();
        completedSoundsReleaseResources();
        overlappingSoundsAreBounded();
        failuresReleaseResources();
        shutdownReleasesAndRejectsSounds();
        shutdownDuringOpenReleasesSound();
        System.out.println("WavePlayerTest passed");
    }

    private static void soundLevelsApplyBeforePlayback() {
        FakeFactory factory = new FakeFactory();
        WavePlayer player = new WavePlayer(factory, 16);
        int[] levels = {-1200, -800, -400, 0};
        float[] decibels = {-12.0f, -8.0f, -4.0f, 0.0f};
        for (int i = 0; i < levels.length; i++) {
            player.setVolume(levels[i]);
            player.play(SOUND);
            FakeClip clip = factory.last();
            check(clip.started, "Each enabled sound level plays");
            checkClose(clip.gainAtStart, decibels[i], "Selected attenuation applies before playback starts");
            clip.finish();
        }
        player.close();
        check(factory.openClips == 0, "Volume-controlled clips release resources");
    }

    private static void volumeChangesUpdateActiveAndNewSounds() {
        FakeFactory factory = new FakeFactory();
        WavePlayer player = new WavePlayer(factory, 16);
        player.play(SOUND);
        FakeClip first = factory.last();
        player.play(SOUND);
        FakeClip second = factory.last();
        player.setVolume(-800);
        checkClose(first.masterGain.getValue(), -8.0f, "First active sound updates immediately");
        checkClose(second.masterGain.getValue(), -8.0f, "Overlapping active sound updates immediately");
        player.play(SOUND);
        checkClose(factory.last().gainAtStart, -8.0f, "New sound inherits selected volume");
        first.finish();
        player.setVolume(-400);
        checkClose(first.masterGain.getValue(), -8.0f, "Completed sound no longer receives volume updates");
        checkClose(second.masterGain.getValue(), -4.0f, "Remaining active sound continues receiving updates");
        factory.callbackOnAnotherThread = true;
        player.close();
        for (FakeClip clip : factory.created) {
            check(clip.closes == 1, "Volume updates preserve single resource release");
        }
    }

    private static void volumeChangeDuringOpenUsesLatestSetting() throws Exception {
        FakeFactory factory = new FakeFactory();
        factory.openEntered = new CountDownLatch(1);
        factory.openAllowed = new CountDownLatch(1);
        WavePlayer player = new WavePlayer(factory, 2);
        player.setVolume(-400);
        Thread producer = new Thread(() -> player.play(SOUND));
        producer.start();
        try {
            check(factory.openEntered.await(2, TimeUnit.SECONDS), "Producer entered open before volume change");
            player.setVolume(-1200);
        } finally {
            factory.openAllowed.countDown();
            producer.join(2000);
        }
        check(!producer.isAlive() && factory.last().started, "Producer starts after opening completes");
        checkClose(factory.last().gainAtStart, -12.0f, "Opening sound starts at latest selected volume");
        player.close();
    }

    private static void volumeAndProviderRangesAreClamped() {
        FakeFactory factory = new FakeFactory();
        WavePlayer player = new WavePlayer(factory, 2);
        player.setVolume(Integer.MIN_VALUE);
        player.play(SOUND);
        FakeClip clip = factory.last();
        checkClose(clip.gainAtStart, -96.0f, "Attenuation is bounded at minus 96 decibels");
        player.setVolume(Integer.MAX_VALUE);
        checkClose(clip.masterGain.getValue(), 0.0f, "Volume never boosts above full volume");
        player.close();

        factory = new FakeFactory();
        factory.gainMinimum = -10.0f;
        factory.gainMaximum = -2.0f;
        player = new WavePlayer(factory, 2);
        player.play(SOUND);
        clip = factory.last();
        checkClose(clip.gainAtStart, -2.0f, "Full volume respects provider maximum");
        player.setVolume(-1200);
        checkClose(clip.masterGain.getValue(), -10.0f, "Attenuation respects provider minimum");
        player.close();
    }

    private static void linearVolumeFallback() {
        FakeFactory factory = new FakeFactory();
        factory.masterGainSupported = false;
        factory.volumeSupported = true;
        WavePlayer player = new WavePlayer(factory, 2);
        int[] levels = {-1200, -800, -400, 0};
        float[] amplitudes = {0.25118864f, 0.39810717f, 0.63095734f, 1.0f};
        for (int i = 0; i < levels.length; i++) {
            player.setVolume(levels[i]);
            player.play(SOUND);
            check(factory.last().started, "Linear-volume provider plays sound");
            checkClose(factory.last().volumeAtStart, amplitudes[i], "Fallback uses linear amplitude before playback");
            factory.last().finish();
        }
        player.play(SOUND);
        player.setVolume(-1200);
        checkClose(factory.last().volume.getValue(), amplitudes[0], "Fallback updates active sounds");
        player.close();

        factory = new FakeFactory();
        factory.masterGainSupported = false;
        factory.volumeSupported = true;
        factory.volumeMinimum = 0.3f;
        factory.volumeMaximum = 0.75f;
        player = new WavePlayer(factory, 2);
        player.play(SOUND);
        checkClose(factory.last().volumeAtStart, 0.75f, "Fallback respects provider maximum");
        player.setVolume(-1200);
        checkClose(factory.last().volume.getValue(), 0.3f, "Fallback respects provider minimum");
        player.close();
    }

    private static void masterGainTakesPriority() {
        FakeFactory factory = new FakeFactory();
        factory.volumeSupported = true;
        WavePlayer player = new WavePlayer(factory, 2);
        player.setVolume(-400);
        player.play(SOUND);
        checkClose(factory.last().gainAtStart, -4.0f, "Master gain is preferred when both controls exist");
        checkClose(factory.last().volumeAtStart, 1.0f, "Linear volume does not add a second attenuation");
        player.close();
    }

    private static void unsupportedVolumeReleasesSound() {
        FakeFactory factory = new FakeFactory();
        factory.masterGainSupported = false;
        WavePlayer player = new WavePlayer(factory, 2);
        player.setVolume(-1200);
        player.play(SOUND);
        check(!factory.last().started, "Uncontrollable sound never starts at full volume");
        check(factory.last().closes == 1 && factory.openClips == 0 && factory.streamsClosed == 1,
                "Unsupported volume releases clip and input stream");
        factory.masterGainSupported = true;
        player.play(SOUND);
        check(factory.last().started, "Playback recovers when volume control is available");
        checkClose(factory.last().gainAtStart, -12.0f, "Recovered sound retains selected volume");
        player.close();
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

    private static void checkClose(float actual, float expected, String message) {
        check(Math.abs(actual - expected) < 0.00001f, message + ": expected " + expected + ", got " + actual);
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
        boolean masterGainSupported = true;
        boolean volumeSupported;
        float gainMinimum = -120.0f;
        float gainMaximum = 6.0f;
        float volumeMinimum;
        float volumeMaximum = 1.0f;
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
        final FloatControl masterGain;
        final FloatControl volume;
        boolean open;
        boolean started;
        int closes;
        float gainAtStart;
        float volumeAtStart;

        FakeClip(FakeFactory factory) {
            this.factory = factory;
            masterGain = new FakeFloatControl(FloatControl.Type.MASTER_GAIN, factory.gainMinimum,
                    factory.gainMaximum, Math.max(factory.gainMinimum, Math.min(factory.gainMaximum, 0.0f)), "dB");
            volume = new FakeFloatControl(FloatControl.Type.VOLUME, factory.volumeMinimum,
                    factory.volumeMaximum, Math.max(factory.volumeMinimum, Math.min(factory.volumeMaximum, 1.0f)), "");
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
            } else if (name.equals("isControlSupported")) {
                check(open, "Controls are queried after opening");
                return args[0] == FloatControl.Type.MASTER_GAIN && factory.masterGainSupported
                        || args[0] == FloatControl.Type.VOLUME && factory.volumeSupported;
            } else if (name.equals("getControl")) {
                check(open, "Controls are accessed after opening");
                if (args[0] == FloatControl.Type.MASTER_GAIN && factory.masterGainSupported) {
                    return masterGain;
                }
                if (args[0] == FloatControl.Type.VOLUME && factory.volumeSupported) {
                    return volume;
                }
                throw new IllegalArgumentException("Unsupported control: " + args[0]);
            } else if (name.equals("start")) {
                check(open, "Only open clips start");
                gainAtStart = masterGain.getValue();
                volumeAtStart = volume.getValue();
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

    /**
     * Exposes provider control ranges and applied volume without using a sound device.
     *
     * @author lare96
     */
    private static final class FakeFloatControl extends FloatControl {
        FakeFloatControl(Type type, float minimum, float maximum, float initialValue, String units) {
            super(type, minimum, maximum, 0.01f, -1, initialValue, units);
        }
    }
}
