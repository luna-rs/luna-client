import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.lang.management.ManagementFactory;
import java.util.Properties;

/**
 * Arms an optional native capture after login and saves its completed report outside the rendering sample.
 *
 * @author lare96
 */
public final class NativePerformance {
    private static final String OUTPUT = System.getProperty("luna.performance.output", "");
    private static final int SECONDS = Integer.getInteger("luna.performance.seconds", 30);
    private static final int WARMUP_SECONDS = Integer.getInteger("luna.performance.warmup.seconds", 15);
    private static long readyAt;
    private static boolean started;
    private static boolean written;
    private static long heapStart, processCpuStart, allocatedStart, gcCountStart, gcMillisStart;
    private static String activeOutput = OUTPUT;
    private static String activeLabel = System.getProperty("luna.performance.label", "native");
    private static int activeSeconds = SECONDS, activeWarmupSeconds = WARMUP_SECONDS;
    private static long nextControlPoll;

    private NativePerformance() {
    }

    public static void poll(JagApplet applet, boolean loggedIn) {
        if (OUTPUT.isEmpty()) return;
        if (written) {
            pollControl();
            return;
        }
        if (!started) {
            if (!loggedIn || !applet.awtFocus) {
                readyAt = 0;
                return;
            }
            long now = System.nanoTime();
            if (readyAt == 0) readyAt = now + Math.max(0, activeWarmupSeconds) * 1_000_000_000L;
            if (now < readyAt) return;
            String environment = "Java " + System.getProperty("java.runtime.version") + "; "
                    + System.getProperty("java.vm.name") + "; " + System.getProperty("os.name") + " "
                    + System.getProperty("os.arch");
            String settings = "native; framebuffer=" + applet.getContentWidth() + "x" + applet.getContentHeight()
                    + "; renderThreads=" + ParallelRasterizer.getThreadCount();
            if (applet instanceof client game) {
                settings += "; clientSize=" + game.clientSize + "; viewport=" + game.layout.viewport.width
                        + "x" + game.layout.viewport.height;
                settings += "; camera1216..1220=" + game.anInt1216 + "," + game.anInt1217 + ","
                        + game.anInt1218 + "," + game.anInt1219 + "," + game.anInt1220;
                settings += "; localPlayers=" + game.localPlayerCount + "; localNpcs=" + game.localNpcCount;
            }
            processCpu();
            allocatedBytes();
            garbageCollection();
            ClientPerformance.start(activeSeconds, activeLabel,
                    applet.getEffectiveFramerate(), environment, settings);
            long[] garbageCollection = garbageCollection();
            gcCountStart = garbageCollection[0];
            gcMillisStart = garbageCollection[1];
            heapStart = heapUsed();
            allocatedStart = allocatedBytes();
            processCpuStart = processCpu();
            started = true;
            System.out.println("Performance capture started: " + activeOutput);
        } else if (ClientPerformance.isComplete()) {
            written = true;
            try {
                String report = report();
                Path destination = Path.of(activeOutput).toAbsolutePath();
                Path parent = destination.getParent();
                if (parent != null) Files.createDirectories(parent);
                Files.writeString(destination, report, StandardCharsets.UTF_8);
                System.out.println("Performance capture saved: " + destination);
            } catch (Exception failure) {
                System.err.println("Performance capture could not be saved: " + failure);
            }
        }
    }

    /**
     * Reads an explicit local benchmark request at most once per second, only while capture is idle.
     */
    private static void pollControl() {
        if (ClientPerformance.isEnabled()) return;
        long now = System.nanoTime();
        if (now < nextControlPoll) return;
        nextControlPoll = now + 1_000_000_000L;
        try {
            Path originalOutput = Path.of(OUTPUT).toAbsolutePath().normalize();
            Path control = Path.of(OUTPUT + ".control").toAbsolutePath().normalize();
            if (!Files.isRegularFile(control)) return;
            Properties request = new Properties();
            try (var reader = Files.newBufferedReader(control, StandardCharsets.UTF_8)) {
                request.load(reader);
            }
            Files.move(control, control.resolveSibling(control.getFileName() + ".last"),
                    StandardCopyOption.REPLACE_EXISTING);
            String filename = request.getProperty("filename", "native-repeat.json");
            if (!filename.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,100}\\.json")) {
                throw new IllegalArgumentException("Benchmark filename must be a plain JSON filename");
            }
            Path destination = originalOutput.resolveSibling(filename).normalize();
            if (!originalOutput.getParent().equals(destination.getParent())) {
                throw new IllegalArgumentException("Benchmark output must remain beside its original report");
            }
            int seconds = Integer.parseInt(request.getProperty("seconds", "30"));
            int warmup = Integer.parseInt(request.getProperty("warmup", "5"));
            if (seconds < 1 || seconds > 120 || warmup < 0 || warmup > 300) {
                throw new IllegalArgumentException("Capture seconds must be 1..120 and warmup 0..300");
            }
            activeLabel = request.getProperty("label", filename.substring(0, filename.length() - 5));
            activeOutput = destination.toString();
            activeSeconds = seconds;
            activeWarmupSeconds = warmup;
            readyAt = 0;
            started = written = false;
            System.out.println("Performance capture rearmed: " + activeOutput);
        } catch (Exception failure) {
            System.err.println("Performance capture request rejected: " + failure);
        }
    }

    /**
     * Reads runtime counters before JSON sorting and serialization allocate the exported report.
     */
    private static String report() {
        long heapEnd = heapUsed();
        long cpuEnd = processCpu();
        long allocationEnd = allocatedBytes();
        long[] garbageCollection = garbageCollection();
        String json = ClientPerformance.json();
        return json.substring(0, json.length() - 1) + ",\"nativeRuntime\":{\"heapUsedStartBytes\":" + heapStart
                + ",\"heapUsedEndBytes\":" + heapEnd + ",\"processCpuDeltaNanos\":"
                + difference(processCpuStart, cpuEnd) + ",\"gameThreadAllocatedBytes\":"
                + difference(allocatedStart, allocationEnd) + ",\"garbageCollections\":"
                + difference(gcCountStart, garbageCollection[0]) + ",\"garbageCollectionMillis\":"
                + difference(gcMillisStart, garbageCollection[1]) + "}}";
    }

    private static long heapUsed() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static long processCpu() {
        if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean bean) {
            return bean.getProcessCpuTime();
        }
        return -1;
    }

    private static long allocatedBytes() {
        if (ManagementFactory.getThreadMXBean() instanceof com.sun.management.ThreadMXBean bean
                && bean.isThreadAllocatedMemorySupported() && bean.isThreadAllocatedMemoryEnabled()) {
            return bean.getThreadAllocatedBytes(Thread.currentThread().threadId());
        }
        return -1;
    }

    private static long[] garbageCollection() {
        long count = 0, millis = 0;
        for (var collector : ManagementFactory.getGarbageCollectorMXBeans()) {
            count += Math.max(0, collector.getCollectionCount());
            millis += Math.max(0, collector.getCollectionTime());
        }
        return new long[] {count, millis};
    }

    private static long difference(long start, long end) {
        return start < 0 || end < 0 ? -1 : Math.max(0, end - start);
    }
}
