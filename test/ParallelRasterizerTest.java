import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Random;

/**
 * Compares complete parallel world images with the original immediate scanline kernels.
 *
 * @author lare96
 */
public final class ParallelRasterizerTest {

    public static void main(String[] args) throws Exception {
        try {
            palette();
            for (boolean lowMemory : new boolean[] {false, true}) {
                for (int[] size : new int[][] {{127, 79}, {1280, 720}}) {
                    int[] expected = workload(size[0], size[1], 1, lowMemory, 12000);
                    for (int threads : new int[] {2, 3, 4}) {
                        check(Arrays.equals(expected, workload(size[0], size[1], threads, lowMemory, 12000)),
                                "all span types retain exact pixels at " + threads + " lanes");
                    }
                }
                texturePressure(lowMemory);
            }
            check(Arrays.equals(triangleFrame(1), triangleFrame(4)),
                    "original triangle setup and clipping retain exact painter pixels");
            fallbackOrdering();
            queuePressure();
            textureMutationBarrier();
            interruptionBarrier();
            failureBarrier();
            serialSelection();
        } finally {
            ParallelRasterizer.shutdown();
        }
        System.out.println("ParallelRasterizerTest passed");
    }

    static void palette() {
        for (int color = 0; color < ThreeDimensionalCanvas.anIntArray1548.length; color++)
            ThreeDimensionalCanvas.anIntArray1548[color] = color * 0x314159 & 0xffffff;
    }

    private static void setup(int width, int height, int[] pixels, int threads, boolean lowMemory) {
        ParallelRasterizer.end();
        ThreeDimensionalCanvas.clearTexels();
        ThreeDimensionalCanvas.lowMemory = lowMemory;
        ThreeDimensionalCanvas.initTextureBufferPool(20);
        Drawable.putPixels(width, height, pixels);
        ThreeDimensionalCanvas.init3D(width, height);
        ParallelRasterizer.setThreadCount(threads);
        ParallelRasterizer.begin(true, pixels, width, height, true);
    }

    static int[] workload(int width, int height, int threads, boolean lowMemory, int spans) {
        int[] pixels = background(width * height);
        setup(width, height, pixels, threads, lowMemory);
        Random random = new Random(0x377L + width * 31L + height);
        int[] opaque = texels(lowMemory, false);
        int[] transparent = texels(lowMemory, true);
        for (int span = 0; span < spans; span++) {
            int row = random.nextInt(height);
            int left = random.nextInt(width + 40) - 20;
            int right = left + 1 + random.nextInt(width + 30);
            ThreeDimensionalCanvas.hClip = left < 0 || right >= width - 1 || random.nextBoolean();
            ThreeDimensionalCanvas.jagged = random.nextBoolean();
            ThreeDimensionalCanvas.anInt1531 = new int[] {0, 64, 128, 192, 255, 256}[span % 6];
            int first = 256 + random.nextInt(30000);
            int second = first + random.nextInt(127) - 63;
            switch (span % 3) {
                case 0:
                    ThreeDimensionalCanvas.gouraudRaster(pixels, row * width, 0, 0, left, right,
                            first << 8, second << 8);
                    break;
                case 1:
                    ThreeDimensionalCanvas.flatRaster(pixels, row * width, first * 0x537 & 0xffffff,
                            0, left, right);
                    break;
                default:
                    boolean holes = (span & 1) == 0;
                    ThreeDimensionalCanvas.aBoolean1529 = !holes;
                    int denominator = lowMemory ? 256 : 64;
                    ThreeDimensionalCanvas.textureRaster(pixels, holes ? transparent : opaque, 0, 0,
                            row * width, left, right, (32 + span % 60) << 8, (80 - span % 40) << 8,
                            700 * denominator, 800 * denominator, 1 << 20,
                            11 * denominator, 7 * denominator, 0);
            }
        }
        ParallelRasterizer.end();
        return pixels;
    }

    private static void texturePressure(boolean lowMemory) throws Exception {
        int[] expected = textureFrame(1, lowMemory);
        for (int threads : new int[] {2, 4}) {
            check(Arrays.equals(expected, textureFrame(threads, lowMemory)),
                    "queued references survive more than the original twenty cached textures");
            check(ThreeDimensionalCanvas.anInt1547 == 80,
                    "parallel drawing retains one texture use stamp per request");
        }
    }

    private static int[] textureFrame(int threads, boolean lowMemory) throws Exception {
        int width = 127;
        int height = 43;
        int[] pixels = background(width * height);
        ParallelRasterizer.end();
        ThreeDimensionalCanvas.clearTexels();
        ThreeDimensionalCanvas.loadedTextures = 50;
        ThreeDimensionalCanvas.anInt1547 = 0;
        for (int texture = 0; texture < 50; texture++) {
            IndexedSprite sprite = new IndexedSprite(new TextureArchive(texture + 1), "texture", 0);
            ThreeDimensionalCanvas.textures[texture] = sprite;
            ThreeDimensionalCanvas.anIntArrayArray1549[texture] = sprite.pixelColours_1517.clone();
        }
        setup(width, height, pixels, threads, lowMemory);
        ThreeDimensionalCanvas.hClip = true;
        ThreeDimensionalCanvas.anInt1531 = 0;
        for (int texture = 0; texture < 80; texture++) {
            int id = texture % 50;
            int row = texture % (height - 3);
            ThreeDimensionalCanvas.textureTriangle(row, row + 3, row + 2, 0, width - 1, width / 2,
                    64, 96, 80, -64, 64, 0, -64, -64, 64, 512, 512, 512, id);
        }
        ParallelRasterizer.end();
        return pixels;
    }

    private static int[] triangleFrame(int threads) {
        int width = 127;
        int height = 79;
        int[] pixels = background(width * height);
        setup(width, height, pixels, threads, false);
        ThreeDimensionalCanvas.hClip = true;
        for (int triangle = 0; triangle < 100; triangle++) {
            ThreeDimensionalCanvas.jagged = (triangle & 1) == 0;
            ThreeDimensionalCanvas.anInt1531 = triangle % 3 == 0 ? 128 : 0;
            ThreeDimensionalCanvas.gouraudTriangle(-3 + triangle % 15, height + 3, height / 2,
                    -7, width + 10, width / 2, 1000 + triangle, 1030 + triangle, 1060 + triangle);
            ThreeDimensionalCanvas.flatTriangle(triangle % height, height - 1, height / 2,
                    width - 1, -4, width / 2, triangle * 0x51379 & 0xffffff);
        }
        ParallelRasterizer.end();
        return pixels;
    }

    private static void fallbackOrdering() {
        int[][] expected = fallbackFrame(1);
        int[][] actual = fallbackFrame(4);
        check(Arrays.deepEquals(expected, actual),
                "foreign buffers, misaligned rows and cross-row alpha reads retain their original order");
    }

    private static int[][] fallbackFrame(int threads) {
        int width = 63;
        int height = 29;
        int[] pixels = background(width * height);
        int[] other = background(width * height);
        setup(width, height, pixels, threads, false);
        ThreeDimensionalCanvas.hClip = false;
        ThreeDimensionalCanvas.anInt1531 = 0;
        ThreeDimensionalCanvas.flatRaster(pixels, 0, 0x123456, 0, 0, width - 1);
        ThreeDimensionalCanvas.flatRaster(other, 0, 0xabcdef, 0, 0, width - 1);
        ThreeDimensionalCanvas.flatRaster(pixels, width / 2, 0x765432, 0, 0, width - 1);
        ThreeDimensionalCanvas.anInt1531 = 128;
        ThreeDimensionalCanvas.flatRaster(pixels, width, 0x654321, 0, 0, width);
        ThreeDimensionalCanvas.flatRaster(pixels, width * 2, 0xfedcba, 0, 0, width - 1);
        ParallelRasterizer.end();
        return new int[][] {pixels, other};
    }

    private static void queuePressure() {
        int[] expected = repeatedRow(1);
        check(Arrays.equals(expected, repeatedRow(4)),
                "bounded command batches retain painter order when a queue fills");
    }

    private static int[] repeatedRow(int threads) {
        int width = 31;
        int height = 11;
        int[] pixels = background(width * height);
        setup(width, height, pixels, threads, false);
        ThreeDimensionalCanvas.hClip = true;
        ThreeDimensionalCanvas.anInt1531 = 128;
        for (int span = 0; span < 40000; span++)
            ThreeDimensionalCanvas.flatRaster(pixels, width * 4, span * 0x5137 & 0xffffff, 0, -3, width + 3);
        ParallelRasterizer.end();
        return pixels;
    }

    private static void textureMutationBarrier() {
        int[] expected = paletteFrame(1);
        check(Arrays.equals(expected, paletteFrame(4)),
                "palette changes finish previous pixel writes before updating lookup colors");
        palette();
    }

    private static int[] paletteFrame(int threads) {
        palette();
        int[] pixels = background(63 * 29);
        setup(63, 29, pixels, threads, false);
        ThreeDimensionalCanvas.hClip = true;
        ThreeDimensionalCanvas.jagged = false;
        ThreeDimensionalCanvas.anInt1531 = 0;
        ThreeDimensionalCanvas.gouraudRaster(pixels, 0, 0, 0, 0, 62, 1000 << 8, 1000 << 8);
        ParallelRasterizer.flushBeforeTextureChange();
        ThreeDimensionalCanvas.anIntArray1548[1000] = 0xabcdef;
        ThreeDimensionalCanvas.gouraudRaster(pixels, 63, 0, 0, 0, 62, 1000 << 8, 1000 << 8);
        ParallelRasterizer.end();
        return pixels;
    }

    private static void interruptionBarrier() {
        int[] pixels = new int[127 * 79];
        setup(127, 79, pixels, 4, false);
        ThreeDimensionalCanvas.hClip = true;
        ThreeDimensionalCanvas.anInt1531 = 0;
        for (int row = 0; row < 79; row++)
            ThreeDimensionalCanvas.flatRaster(pixels, row * 127, 0xabcdef, 0, 0, 126);
        Thread.currentThread().interrupt();
        ParallelRasterizer.end();
        check(Thread.interrupted(), "frame completion retains the caller's interruption state");
        for (int row = 0; row < 79; row++)
            for (int x = 0; x < 126; x++)
                check(pixels[row * 127 + x] == 0xabcdef, "all lanes finish before an interrupted end returns");
    }

    private static void failureBarrier() {
        int[] pixels = new int[127 * 79];
        setup(127, 79, pixels, 4, false);
        ThreeDimensionalCanvas.hClip = true;
        ThreeDimensionalCanvas.jagged = true;
        ThreeDimensionalCanvas.anInt1531 = 0;
        ThreeDimensionalCanvas.gouraudRaster(pixels, 127, 0, 0, 0, 126, 70000 << 8, 70000 << 8);
        boolean failed = false;
        try {
            ParallelRasterizer.end();
        } catch (ArrayIndexOutOfBoundsException expected) {
            failed = true;
        }
        check(failed, "worker failures reach the game thread after the completion barrier");
        check(Arrays.equals(workload(127, 79, 1, false, 500), workload(127, 79, 4, false, 500)),
                "a failed frame cannot leave queued commands or active workers in the next frame");
    }

    private static void serialSelection() {
        int[] pixels = new int[127 * 79];
        setup(127, 79, pixels, 0, false);
        ThreeDimensionalCanvas.hClip = true;
        ThreeDimensionalCanvas.anInt1531 = 0;
        ThreeDimensionalCanvas.flatRaster(pixels, 0, 0xabcdef, 0, 0, 126);
        check(pixels[0] == 0xabcdef, "zero selects immediate serial rendering");
        ParallelRasterizer.end();
        ParallelRasterizer.setThreadCount(4);
        ParallelRasterizer.begin(false, pixels, 127, 79, true);
        ThreeDimensionalCanvas.flatRaster(pixels, 127, 0x123456, 0, 0, 126);
        check(pixels[127] == 0x123456, "fixed mode retains immediate serial rendering");
        ParallelRasterizer.end();
        ParallelRasterizer.begin(true, pixels, 127, 79);
        ThreeDimensionalCanvas.flatRaster(pixels, 127 * 2, 0x654321, 0, 0, 126);
        check(pixels[127 * 2] == 0x654321, "small viewports avoid parallel queue and scheduling overhead");
        ParallelRasterizer.end();
    }

    private static int[] background(int count) {
        int[] pixels = new int[count];
        for (int index = 0; index < count; index++) pixels[index] = index * 0x5371 & 0xffffff;
        return pixels;
    }

    private static int[] texels(boolean lowMemory, boolean holes) {
        int[] pixels = new int[lowMemory ? 16384 : 65536];
        for (int index = 0; index < pixels.length; index++)
            pixels[index] = holes && index % 7 == 0 ? 0 : index * 0x314159 & 0xffffff;
        return pixels;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /**
     * Supplies fifty distinct real indexed textures without requiring an installed cache.
     *
     * @author lare96
     */
    private static final class TextureArchive extends Archive {
        private final byte[] index;
        private final byte[] data = new byte[2 + 64 * 64];

        private TextureArchive(int color) throws Exception {
            super(new byte[8]);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            output.writeShort(64);
            output.writeShort(64);
            output.writeByte(2);
            output.writeByte(color * 13 & 255);
            output.writeByte(color * 31 & 255);
            output.writeByte(color * 47 & 255);
            output.writeByte(0);
            output.writeByte(0);
            output.writeShort(64);
            output.writeShort(64);
            output.writeByte(0);
            index = bytes.toByteArray();
            Arrays.fill(data, 2, data.length, (byte) 1);
        }

        @Override
        public byte[] get(String name) {
            return name.equals("index.dat") ? index : data;
        }
    }
}
