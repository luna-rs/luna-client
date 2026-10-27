import javax.sound.midi.MidiSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

/**
 * Standalone regression tests for music decompression and request validation.
 *
 * @author lare96
 */
public final class OnDemandFetcherTest {

    public static void main(String[] args) throws Exception {
        requestBounds();
        responseReads();
        byte[] largeMidi = midi(70_000);
        check(MidiSystem.getSequence(new ByteArrayInputStream(largeMidi)).getTracks().length == 1,
                "large fixture must be valid MIDI");
        check(Arrays.equals(largeMidi, complete(2, gzip(largeMidi)).buf),
                "valid MIDI larger than 65,000 bytes must decompress");

        byte[] maximumMidi = midi(2 * 1024 * 1024);
        check(Arrays.equals(maximumMidi, complete(2, gzip(maximumMidi)).buf),
                "exact music size limit must be accepted");
        check(complete(2, gzip(midi(2 * 1024 * 1024 + 1))).buf == null,
                "oversized music must fail without throwing into the game thread");
        check(complete(2, new byte[]{1, 2, 3, 4}).buf == null,
                "invalid music gzip must fail without throwing into the game thread");
        byte[] truncated = gzip(largeMidi);
        check(complete(2, Arrays.copyOf(truncated, truncated.length / 2)).buf == null,
                "truncated music gzip must fail without throwing into the game thread");
        check(complete(2, null).buf == null, "missing music must remain missing");

        byte[] smallModel = new byte[128];
        Arrays.fill(smallModel, (byte) 17);
        check(Arrays.equals(smallModel, complete(0, gzip(smallModel)).buf),
                "nonmusic decompression must remain unchanged");
        expectRuntime(() -> complete(0, new byte[]{1, 2, 3, 4}), "error unzipping");
        byte[] oversizedModel = gzip(new byte[65_000]);
        expectRuntime(() -> complete(0, oversizedModel), "buffer overflow!");
        System.out.println("OnDemandFetcherTest: all checks passed");
    }

    private static void requestBounds() {
        OnDemandFetcher fetcher = new OnDemandFetcher();
        fetcher.anIntArrayArray1377[2] = new int[]{0, 1};
        fetcher.request(-1, 0);
        fetcher.request(4, 0);
        fetcher.request(5, 0);
        fetcher.request(1, 0);
        fetcher.request(2, -1);
        fetcher.request(2, 2);
        fetcher.request(2, 3);
        fetcher.request(2, 0);
        check(fetcher.method333() == 0, "invalid requests must not be queued");
        fetcher.request(2, 1);
        fetcher.request(2, 1);
        check(fetcher.method333() == 1, "valid requests must still be deduplicated");
    }

    private static FileNode complete(int type, byte[] data) {
        OnDemandFetcher fetcher = new OnDemandFetcher();
        FileNode node = new FileNode();
        node.type = type;
        node.id = 17;
        node.buf = data;
        fetcher.completed.addLast(node);
        check(fetcher.method330() == node, "completion must preserve its request identity");
        check(fetcher.method330() == null, "completion must be removed from the queue");
        return node;
    }

    private static void responseReads() throws Exception {
        byte[] expected = {10, 20, 30, 40};
        byte[] actual = new byte[6];
        InputStream partial = new ByteArrayInputStream(expected) {
            private boolean firstRead = true;

            @Override
            public synchronized int read(byte[] data, int offset, int length) {
                if (firstRead) {
                    firstRead = false;
                    return 0;
                }
                return super.read(data, offset, Math.min(length, 1));
            }
        };
        OnDemandFetcher.readFully(partial, actual, 1, expected.length);
        check(Arrays.equals(expected, Arrays.copyOfRange(actual, 1, 5)),
                "zero-progress and short reads must produce the full response");
        check(actual[0] == 0 && actual[5] == 0, "read must respect target bounds");
        try {
            OnDemandFetcher.readFully(new ByteArrayInputStream(new byte[]{1}), new byte[8], 0, 8);
            throw new AssertionError("truncated response must throw EOFException");
        } catch (EOFException expectedFailure) {
            // A truncated update response must not spin or move the offset backwards.
        }
    }

    private static byte[] gzip(byte[] data) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(data);
        }
        // Cache entries include a two-byte version after the gzip member.
        output.write(0);
        output.write(1);
        return output.toByteArray();
    }

    private static byte[] midi(int size) throws Exception {
        // A type-zero MIDI containing one large text event and end-of-track.
        int textLength = size - 32;
        check(textLength >= 16_384 && textLength < 2_097_152,
                "fixture requires a three-byte variable-length size");
        ByteArrayOutputStream output = new ByteArrayOutputStream(size);
        DataOutputStream data = new DataOutputStream(output);
        data.writeInt(0x4d546864);
        data.writeInt(6);
        data.writeShort(0);
        data.writeShort(1);
        data.writeShort(96);
        data.writeInt(0x4d54726b);
        data.writeInt(size - 22);
        data.writeByte(0);
        data.writeByte(0xff);
        data.writeByte(1);
        data.writeByte((textLength >> 14) | 0x80);
        data.writeByte(((textLength >> 7) & 0x7f) | 0x80);
        data.writeByte(textLength & 0x7f);
        data.write(new byte[textLength]);
        data.writeInt(0x00ff2f00);
        check(output.size() == size, "fixture must have the requested size");
        return output.toByteArray();
    }

    private static void expectRuntime(Runnable action, String message) {
        try {
            action.run();
            throw new AssertionError("Expected " + message);
        } catch (RuntimeException exception) {
            check(message.equals(exception.getMessage()), "unexpected nonmusic failure");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }
}
