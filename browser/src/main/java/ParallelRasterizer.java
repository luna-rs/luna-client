/**
 * Keeps the shared software renderer on the browser's JavaScript execution thread.
 *
 * @author lare96
 */
public final class ParallelRasterizer {

    public static final int DEFAULT_THREAD_COUNT = 1;
    public static final int MIN_PARALLEL_PIXELS = 900 * 600;
    static final int CLIPPED = 4;
    static final int JAGGED = 8;
    static final int LOW_MEMORY = 16;
    static final int OPAQUE_TEXTURE = 32;

    private ParallelRasterizer() {
    }

    public static void setThreadCount(int threads) {
        if (threads < 0 || threads > 4) throw new IllegalArgumentException("Invalid raster thread count");
    }

    public static int getThreadCount() {
        return 1;
    }

    public static boolean isActive() {
        return false;
    }

    public static void begin(boolean allowParallel, int[] pixels, int width, int height) {
    }

    static void begin(boolean allowParallel, int[] pixels, int width, int height, boolean force) {
    }

    public static void end() {
    }

    public static void flushBeforeTextureChange() {
    }

    public static void shutdown() {
    }

    static boolean recordGouraud(int[] pixels, int i, int j, int k, int l, int i1, int j1, int k1) {
        return false;
    }

    static boolean recordFlat(int[] pixels, int i, int j, int k, int l, int i1) {
        return false;
    }

    static boolean recordTexture(int[] pixels, int[] texels, int i, int j, int k, int l, int i1,
                                 int j1, int k1, int l1, int i2, int j2, int k2, int l2, int i3) {
        return false;
    }
}
