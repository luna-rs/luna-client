import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Distributes independent world scanlines while retaining painter order within every row.
 * Recording, configuration and frame completion belong to the game thread.
 *
 * @author lare96
 */
public final class ParallelRasterizer {

    public static final int DEFAULT_THREAD_COUNT = 4;
    public static final int MIN_PARALLEL_PIXELS = 900 * 600;
    static final int CLIPPED = 4;
    static final int JAGGED = 8;
    static final int LOW_MEMORY = 16;
    static final int OPAQUE_TEXTURE = 32;
    private static final int GOURAUD = 1;
    private static final int FLAT = 2;
    private static final int TEXTURED = 3;
    private static final int STRIDE = 16;
    private static final int MAX_THREADS = 4;
    private static final int MAX_SPANS_PER_QUEUE = 32768;
    private static final SpanQueue[] queues = new SpanQueue[MAX_THREADS];
    private static int threadCount = Math.min(DEFAULT_THREAD_COUNT,
            Math.max(1, Runtime.getRuntime().availableProcessors()));
    private static ExecutorService executor;
    private static Frame frame;

    static {
        for (int index = 0; index < queues.length; index++) queues[index] = new SpanQueue();
    }

    private ParallelRasterizer() {
    }

    /**
     * Chooses the total raster lanes, including the game thread. Zero and one select serial drawing.
     */
    public static void setThreadCount(int threads) {
        if (threads < 0 || threads > MAX_THREADS)
            throw new IllegalArgumentException("Raster thread count must be between 0 and " + MAX_THREADS);
        end();
        threadCount = threads;
    }

    /**
     * Returns the configured total raster lanes, before the available processor limit.
     */
    public static int getThreadCount() {
        return threadCount;
    }

    /**
     * Reports whether the current world frame is recording deferred scanlines.
     */
    public static boolean isActive() {
        return frame != null;
    }

    /**
     * Starts optional world recording. Fixed mode and small viewports retain immediate drawing.
     */
    public static void begin(boolean allowParallel, int[] pixels, int width, int height) {
        begin(allowParallel, pixels, width, height, false);
    }

    static void begin(boolean allowParallel, int[] pixels, int width, int height, boolean force) {
        end();
        int lanes = force ? threadCount
                : Math.min(threadCount, Math.max(1, Runtime.getRuntime().availableProcessors()));
        if (!allowParallel || lanes < 2 || pixels == null || width <= 0 || height <= 0
                || (long) width * height > pixels.length
                || !force && (long) width * height < MIN_PARALLEL_PIXELS)
            return;
        reserveTextureBuffers();
        frame = new Frame(pixels, width, height, lanes);
    }

    /**
     * Completes all queued pixel writes before overlays, texture animation or presentation proceeds.
     */
    public static void end() {
        Frame current = frame;
        if (current == null) return;
        try {
            flush(current);
        } finally {
            frame = null;
        }
    }

    /**
     * Finishes pending spans before a texture or color lookup table is changed.
     */
    public static void flushBeforeTextureChange() {
        if (frame != null) flush(frame);
    }

    /**
     * Releases the daemon workers after pending writes have completed.
     */
    public static void shutdown() {
        end();
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    static boolean recordGouraud(int[] pixels, int i, int j, int k, int l, int i1, int j1, int k1) {
        if (frame == null) return false;
        if (l >= i1) return true;
        if (ThreeDimensionalCanvas.jagged && !ThreeDimensionalCanvas.hClip
                && (i1 - l >> 2) >= ThreeDimensionalCanvas.divTable.length) {
            flush(frame);
            return false;
        }
        SpanQueue queue = prepare(pixels, i, l, i1, ThreeDimensionalCanvas.anInt1531 != 0);
        if (queue == null) return false;
        int at = queue.append(flags(GOURAUD));
        int[] data = queue.data;
        data[at + 1] = i;
        data[at + 2] = j;
        data[at + 3] = k;
        data[at + 4] = l;
        data[at + 5] = i1;
        data[at + 6] = j1;
        data[at + 7] = k1;
        data[at + 14] = ThreeDimensionalCanvas.anInt1531;
        return true;
    }

    static boolean recordFlat(int[] pixels, int i, int j, int k, int l, int i1) {
        if (frame == null) return false;
        if (l >= i1) return true;
        SpanQueue queue = prepare(pixels, i, l, i1, ThreeDimensionalCanvas.anInt1531 != 0);
        if (queue == null) return false;
        int at = queue.append(flags(FLAT));
        int[] data = queue.data;
        data[at + 1] = i;
        data[at + 2] = j;
        data[at + 3] = k;
        data[at + 4] = l;
        data[at + 5] = i1;
        data[at + 14] = ThreeDimensionalCanvas.anInt1531;
        return true;
    }

    static boolean recordTexture(int[] pixels, int[] texels, int i, int j, int k, int l, int i1,
                                 int j1, int k1, int l1, int i2, int j2, int k2, int l2, int i3) {
        if (frame == null) return false;
        if (l >= i1) return true;
        if (!ThreeDimensionalCanvas.hClip && (i1 - l >> 3) >= ThreeDimensionalCanvas.divTable.length) {
            flush(frame);
            return false;
        }
        SpanQueue queue = prepare(pixels, k, l, i1, false);
        if (queue == null) return false;
        int span = queue.count;
        int at = queue.append(flags(TEXTURED));
        int[] data = queue.data;
        queue.texels[span] = texels;
        data[at + 1] = i;
        data[at + 2] = j;
        data[at + 3] = k;
        data[at + 4] = l;
        data[at + 5] = i1;
        data[at + 6] = j1;
        data[at + 7] = k1;
        data[at + 8] = l1;
        data[at + 9] = i2;
        data[at + 10] = j2;
        data[at + 11] = k2;
        data[at + 12] = l2;
        data[at + 13] = i3;
        data[at + 14] = ThreeDimensionalCanvas.anInt1531;
        return true;
    }

    private static int flags(int type) {
        return type | (ThreeDimensionalCanvas.hClip ? CLIPPED : 0)
                | (ThreeDimensionalCanvas.jagged ? JAGGED : 0)
                | (ThreeDimensionalCanvas.lowMemory ? LOW_MEMORY : 0)
                | (ThreeDimensionalCanvas.aBoolean1529 ? OPAQUE_TEXTURE : 0);
    }

    private static SpanQueue prepare(int[] pixels, int rowBase, int left, int right, boolean readsNeighbor) {
        Frame current = frame;
        int row = rowBase / current.width;
        if (pixels != current.pixels || row < 0 || row >= current.height || row * current.width != rowBase
                || Drawable.width != current.width || Drawable.lastPixelX != current.lastPixelX
                || ThreeDimensionalCanvas.halfParentWidth != current.halfParentWidth
                || ThreeDimensionalCanvas.divTable != current.divTable
                || ThreeDimensionalCanvas.anIntArray1548 != current.palette
                || !ThreeDimensionalCanvas.hClip
                && (left < 0 || right > current.width || readsNeighbor && right >= current.width)) {
            flush(current);
            return null;
        }
        SpanQueue queue = queues[row % current.lanes];
        if (queue.count == MAX_SPANS_PER_QUEUE) flush(current);
        current.pending = true;
        return queue;
    }

    private static void reserveTextureBuffers() {
        int target = ThreeDimensionalCanvas.activeTexels.length;
        if (ThreeDimensionalCanvas.texelPool == null) {
            ThreeDimensionalCanvas.initTextureBufferPool(target);
            return;
        }
        if (ThreeDimensionalCanvas.texelPool.length < target)
            ThreeDimensionalCanvas.texelPool = Arrays.copyOf(ThreeDimensionalCanvas.texelPool, target);
        int allocated = ThreeDimensionalCanvas.textureBufferSize;
        for (int[] texels : ThreeDimensionalCanvas.activeTexels) if (texels != null) allocated++;
        while (allocated < target) {
            ThreeDimensionalCanvas.texelPool[ThreeDimensionalCanvas.textureBufferSize++] =
                    new int[ThreeDimensionalCanvas.lowMemory ? 16384 : 65536];
            allocated++;
        }
    }

    private static ExecutorService executor() {
        if (executor == null) {
            AtomicInteger index = new AtomicInteger();
            ThreadFactory factory = task -> {
                Thread worker = new Thread(task, "Luna raster " + index.incrementAndGet());
                worker.setDaemon(true);
                worker.setPriority(Thread.NORM_PRIORITY);
                return worker;
            };
            executor = Executors.newFixedThreadPool(MAX_THREADS - 1, factory);
        }
        return executor;
    }

    private static void flush(Frame current) {
        if (!current.pending) return;
        current.pending = false;
        Throwable failure = null;
        int submitted = 0;
        try {
            for (int lane = 1; lane < current.lanes; lane++) {
                SpanQueue queue = queues[lane];
                queue.executionFrame = current;
                current.futures[lane - 1] = executor().submit(queue);
                submitted++;
            }
        } catch (Throwable exception) {
            failure = exception;
        }
        try {
            queues[0].drain(current);
            for (int lane = submitted + 1; lane < current.lanes; lane++) queues[lane].drain(current);
        } catch (Throwable exception) {
            if (failure == null) failure = exception;
        }
        boolean interrupted = false;
        for (int task = 0; task < submitted; task++) {
            boolean complete = false;
            while (!complete) {
                try {
                    current.futures[task].get();
                    complete = true;
                } catch (InterruptedException exception) {
                    interrupted = true;
                } catch (ExecutionException exception) {
                    if (failure == null) failure = exception.getCause();
                    complete = true;
                }
            }
            current.futures[task] = null;
        }
        for (int lane = 0; lane < current.lanes; lane++) queues[lane].clear();
        if (interrupted) Thread.currentThread().interrupt();
        if (failure instanceof Error) throw (Error) failure;
        if (failure instanceof RuntimeException) throw (RuntimeException) failure;
        if (failure != null) throw new IllegalStateException("Parallel rasterization failed", failure);
    }

    /**
     * Holds immutable presentation state until all lanes have completed their pixel writes.
     *
     * @author lare96
     */
    private static final class Frame {
        private final int[] pixels;
        private final int width;
        private final int height;
        private final int lanes;
        private final int lastPixelX = Drawable.lastPixelX;
        private final int halfParentWidth = ThreeDimensionalCanvas.halfParentWidth;
        private final int[] divTable = ThreeDimensionalCanvas.divTable;
        private final int[] palette = ThreeDimensionalCanvas.anIntArray1548;
        private final Future<?>[] futures = new Future<?>[MAX_THREADS - 1];
        private boolean pending;

        private Frame(int[] pixels, int width, int height, int lanes) {
            this.pixels = pixels;
            this.width = width;
            this.height = height;
            this.lanes = lanes;
        }
    }

    /**
     * Reuses a primitive command buffer for rows owned by one lane.
     *
     * @author lare96
     */
    private static final class SpanQueue implements Runnable {
        private int[] data = new int[1024 * STRIDE];
        private int[][] texels = new int[1024][];
        private int count;
        private final SpanRasterizer rasterizer = new SpanRasterizer();
        private Frame executionFrame;

        private int append(int flags) {
            if (count == texels.length) {
                int capacity = Math.min(MAX_SPANS_PER_QUEUE, count * 2);
                data = Arrays.copyOf(data, capacity * STRIDE);
                texels = Arrays.copyOf(texels, capacity);
            }
            int at = count++ * STRIDE;
            data[at] = flags;
            return at;
        }

        @Override
        public void run() {
            drain(executionFrame);
        }

        private void drain(Frame current) {
            rasterizer.configure(current.lastPixelX, current.halfParentWidth, current.divTable, current.palette);
            for (int span = 0, at = 0; span < count; span++, at += STRIDE) {
                int flags = data[at];
                rasterizer.state(flags, data[at + 14]);
                switch (flags & 3) {
                    case GOURAUD:
                        rasterizer.gouraudRaster(current.pixels, data[at + 1], data[at + 2], data[at + 3],
                                data[at + 4], data[at + 5], data[at + 6], data[at + 7]);
                        break;
                    case FLAT:
                        rasterizer.flatRaster(current.pixels, data[at + 1], data[at + 2], data[at + 3],
                                data[at + 4], data[at + 5]);
                        break;
                    case TEXTURED:
                        rasterizer.textureRaster(current.pixels, texels[span], data[at + 1], data[at + 2],
                                data[at + 3], data[at + 4], data[at + 5], data[at + 6], data[at + 7],
                                data[at + 8], data[at + 9], data[at + 10], data[at + 11], data[at + 12], data[at + 13]);
                        break;
                    default:
                        throw new IllegalStateException("Unknown raster span");
                }
            }
        }

        private void clear() {
            Arrays.fill(texels, 0, count, null);
            count = 0;
            executionFrame = null;
        }
    }
}
