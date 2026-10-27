import sign.MusicController;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

/**
 * Exercises music packet ordering without an audio device or an applet.
 *
 * @author lare96
 */
public final class MusicControllerTest {
    public static void main(String[] args) {
        protocol();
        cancellation();
        jingles();
        failures();
        System.out.println("MusicControllerTest passed");
    }

    private static void protocol() {
        check(SizeConstants.INCOMING_PACKET_LENGTHS[220] == 2, "song packet length");
        check(SizeConstants.INCOMING_PACKET_LENGTHS[249] == 5, "jingle packet length");
        JagBuffer song = new JagBuffer();
        song.buffer = new byte[]{0x7f, (byte) 0xff};
        check(song.getLEShortA() == 65535, "stop sentinel payload");
        song.position = 0;
        song.buffer = new byte[]{(byte) 0x83, 0x01};
        check(song.getLEShortA() == 259, "song LE-add encoding");
        JagBuffer jingle = new JagBuffer();
        jingle.buffer = new byte[]{0x23, 0x01, 0x34, 0x12, 0x56};
        check(jingle.getLEShort() == 0x123, "jingle LE encoding");
        check(jingle.method554() == 0x123456, "jingle middle/high/low delay");
    }

    private static void cancellation() {
        Fixture f = new Fixture();
        f.music.setSong(10);
        f.music.setSong(11);
        f.music.accept(10, new byte[1]);
        check(f.callbacks.isEmpty(), "late song must not play");
        f.music.setSong(65535);
        f.music.accept(11, new byte[1]);
        check(f.callbacks.isEmpty(), "late data after stop must not play");
        f.music.setSong(11);
        f.music.accept(11, new byte[1]);
        check(f.loops.get(0), "background must loop");
        int requested = f.requests.size();
        f.music.setSong(11);
        check(f.requests.size() == requested, "duplicate background must not restart");
        f.music.setEnabled(false);
        f.music.setSong(12);
        f.music.accept(12, new byte[1]);
        check(f.callbacks.size() == 1, "mute rejects downloads");
        f.music.setEnabled(true);
        check(f.lastRequest() == 12, "unmute requests newest background");
        f.music.reset();
        f.music.accept(12, new byte[1]);
        check(f.callbacks.size() == 1, "logout invalidates downloads");
    }

    private static void jingles() {
        Fixture f = new Fixture();
        f.music.setSong(10);
        f.music.playJingle(20, 100);
        for (int i = 0; i < 100; i++) f.music.tick(20);
        check(f.lastRequest() == 20, "download latency must not consume jingle delay");
        f.music.accept(20, new byte[1]);
        check(!f.loops.get(0), "jingle must play once");
        f.music.tick(1000);
        check(f.lastRequest() == 20, "device initialization must not consume delay");
        f.callbacks.get(0).accept(80);
        f.music.tick(20);
        f.music.setSong(11);
        f.music.tick(80);
        check(f.lastRequest() == 20, "jingle protects playback until delay expires");
        f.music.tick(20);
        check(f.lastRequest() == 11, "jingle resumes newest background");

        f.music.playJingle(21, 0);
        f.music.accept(21, new byte[1]);
        f.callbacks.get(1).accept(60);
        f.music.tick(20);
        f.music.tick(40);
        check(f.lastRequest() == 21, "zero delay uses MIDI duration");
        f.music.tick(20);
        check(f.lastRequest() == 11, "zero-delay jingle resumes");

        f.music.playJingle(22, 100);
        f.music.accept(22, new byte[1]);
        LongConsumer stale = f.callbacks.get(2);
        f.music.setSong(-1);
        stale.accept(100);
        f.music.tick(1000);
        check(f.lastRequest() == 22, "stopped jingle must not resume background");
        f.music.playJingle(23, 20);
        f.music.accept(23, new byte[1]);
        f.callbacks.get(3).accept(20);
        f.music.tick(20);
        f.music.tick(20);
        check(f.lastRequest() == 23, "jingle with no background returns to silence");
    }

    private static void failures() {
        Fixture f = new Fixture();
        f.music.setSong(10);
        f.music.playJingle(20, 100);
        f.music.accept(20, null);
        check(f.lastRequest() == 10, "failed download resumes background");
        f.music.playJingle(21, 100);
        f.music.accept(21, new byte[1]);
        f.callbacks.get(0).accept(-1);
        f.music.tick(20);
        check(f.lastRequest() == 10, "invalid MIDI resumes background");
        f.music.playJingle(22, 100);
        f.music.tick(30000);
        check(f.lastRequest() == 10, "missing jingle must not hold music forever");
        f.music.setSong(999);
        int size = f.requests.size();
        f.music.setSong(999);
        check(f.requests.size() == size + 1, "failed song can be retried");
        f.music.setSong(10);
        f.music.accept(10, new byte[1]);
        check(f.loops.get(f.loops.size() - 1), "valid song recovers after failures");
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    /**
     * Records cache requests and worker callbacks independently of real hardware.
     *
     * @author lare96
     */
    private static final class Fixture {
        final List<Integer> requests = new ArrayList<>();
        final List<Boolean> loops = new ArrayList<>();
        final List<LongConsumer> callbacks = new ArrayList<>();
        int stops;
        final MusicController music = new MusicController(id -> {
            requests.add(id);
            return id >= 0 && id < 647;
        }, (data, loop, callback) -> {
            loops.add(loop);
            callbacks.add(callback);
        }, () -> stops++);

        int lastRequest() {
            return requests.get(requests.size() - 1);
        }
    }
}
