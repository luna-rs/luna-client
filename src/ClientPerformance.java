import java.util.Arrays;

/**
 * Records bounded, opt-in frame and stage samples without changing client timing or simulation.
 *
 * @author lare96
 */
public final class ClientPerformance {
    public static final int UPDATE = 0, SCENE_PREP = 1, SCENE = 2, TEXTURES = 3, OVERLAYS = 4, UI = 5;
    public static final int FRAMEBUFFER_COPY = 6, PRESENT = 7, CONVERSION = 8, UPLOAD = 9;
    public static final int RASTER = 10, MODEL = 11, VISIBILITY = 12;
    private static final String[] STAGES = {"updates", "scenePreparation", "sceneAndModels", "textures", "overlays",
            "interfaces", "framebufferCopy", "presentationSubmission", "pixelConversion", "canvasUpload",
            "rasterization", "modelProcessing", "visibility"};
    private static final long SECOND = 1_000_000_000L;
    private static final int MAX_SECONDS = 120;
    private static boolean enabled;
    private static boolean complete;
    private static String label = "", environment = "", configuration = "";
    private static long started, stopped, deadline, previousRender, previousPresentation;
    private static long activePending;
    private static boolean renderedInLoop;
    private static int targetFps, renderCount, presentationCount, intervalCount, presentationIntervalCount;
    private static int activeCount, droppedFrameSamples;
    private static int minimumObservedFps, maximumObservedFps;
    private static long reducedRateLoops;
    private static long updateCount, activeTotal;
    private static long[] renderDurations, renderIntervals, presentationIntervals, activeDurations;
    private static long[][] stageDurations;
    private static int[] stageCounts;
    private static long[] stageTotals, stageCalls;

    private ClientPerformance() {
    }

    /**
     * Arms a bounded capture. Buffers are allocated once here, never in a frame's recording path.
     */
    public static void start(int seconds, String captureLabel, int fps, String runtime, String settings) {
        int duration = Math.max(1, Math.min(MAX_SECONDS, seconds));
        int capacity = duration * 240 + 32;
        int stageCapacity = duration * 1000 + 32;
        renderDurations = new long[capacity];
        renderIntervals = new long[capacity];
        presentationIntervals = new long[capacity];
        activeDurations = new long[capacity];
        stageDurations = new long[STAGES.length][stageCapacity];
        stageCounts = new int[STAGES.length];
        stageTotals = new long[STAGES.length];
        stageCalls = new long[STAGES.length];
        targetFps = Math.max(1, fps);
        minimumObservedFps = maximumObservedFps = targetFps;
        reducedRateLoops = 0;
        label = captureLabel == null ? "" : captureLabel;
        environment = runtime == null ? "" : runtime;
        configuration = settings == null ? "" : settings;
        renderCount = presentationCount = intervalCount = presentationIntervalCount = activeCount = 0;
        droppedFrameSamples = 0;
        updateCount = activeTotal = activePending = previousRender = previousPresentation = 0;
        renderedInLoop = false;
        complete = false;
        started = System.nanoTime();
        deadline = started + duration * SECOND;
        stopped = 0;
        enabled = true;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isComplete() {
        return complete;
    }

    public static void observeTargetFps(int fps) {
        if (!enabled) return;
        minimumObservedFps = Math.min(minimumObservedFps, fps);
        maximumObservedFps = Math.max(maximumObservedFps, fps);
        if (fps < targetFps) reducedRateLoops++;
    }

    /**
     * Returns zero while disabled, allowing callers to avoid reading the clock in ordinary gameplay.
     */
    public static long begin(int stage) {
        return enabled ? System.nanoTime() : 0;
    }

    public static void end(int stage, long stageStarted) {
        if (enabled && stageStarted != 0) addStageNanos(stage, System.nanoTime() - stageStarted);
    }

    public static void addStageNanos(int stage, long nanos) {
        if (!enabled || stage < 0 || stage >= STAGES.length) return;
        long duration = Math.max(0, nanos);
        stageTotals[stage] += duration;
        stageCalls[stage]++;
        int index = stageCounts[stage];
        if (index < stageDurations[stage].length) {
            stageDurations[stage][index] = duration;
            stageCounts[stage] = index + 1;
        }
    }

    public static long loopStarted() {
        return enabled ? System.nanoTime() : 0;
    }

    /**
     * Counts active loop work before any cap wait; work between renders belongs to the next render sample.
     */
    public static void loopFinished(long loopStarted) {
        if (!enabled || loopStarted == 0) return;
        long now = System.nanoTime();
        long active = Math.max(0, now - loopStarted);
        activeTotal += active;
        activePending += active;
        if (renderedInLoop) {
            if (activeCount < activeDurations.length) activeDurations[activeCount++] = activePending;
            else droppedFrameSamples++;
            activePending = 0;
            renderedInLoop = false;
        }
        if (now >= deadline) stop();
    }

    public static void updated(int count) {
        if (enabled) updateCount += count;
    }

    public static long frameStarted() {
        if (!enabled) return 0;
        long now = System.nanoTime();
        if (previousRender != 0 && intervalCount < renderIntervals.length) {
            renderIntervals[intervalCount++] = now - previousRender;
        }
        previousRender = now;
        return now;
    }

    public static void frameRendered(long frameStarted) {
        if (!enabled || frameStarted == 0) return;
        if (renderCount < renderDurations.length) renderDurations[renderCount] = System.nanoTime() - frameStarted;
        else droppedFrameSamples++;
        renderCount++;
        renderedInLoop = true;
    }

    /**
     * Records a completed canvas upload or native window presentation rather than a render submission.
     */
    public static void presented() {
        if (!enabled) return;
        long now = System.nanoTime();
        if (previousPresentation != 0 && presentationIntervalCount < presentationIntervals.length) {
            presentationIntervals[presentationIntervalCount++] = now - previousPresentation;
        }
        previousPresentation = now;
        presentationCount++;
    }

    public static void stop() {
        if (!enabled) return;
        stopped = System.nanoTime();
        enabled = false;
        complete = true;
    }

    /**
     * Produces summary percentiles and raw frame samples after capture; sorting never occurs in a live frame.
     */
    public static String json() {
        if (renderDurations == null) return "{\"state\":\"idle\"}";
        long end = stopped == 0 ? System.nanoTime() : stopped;
        double seconds = Math.max(1, end - started) / (double) SECOND;
        StringBuilder json = new StringBuilder(32_768);
        json.append("{\"schema\":1,\"state\":").append(quote(enabled ? "capturing" : "complete"));
        json.append(",\"label\":").append(quote(label));
        json.append(",\"environment\":").append(quote(environment));
        json.append(",\"configuration\":").append(quote(configuration));
        json.append(",\"durationSeconds\":").append(seconds);
        json.append(",\"targetFps\":").append(targetFps);
        json.append(",\"minimumObservedTargetFps\":").append(minimumObservedFps);
        json.append(",\"maximumObservedTargetFps\":").append(maximumObservedFps);
        json.append(",\"reducedRateLoops\":").append(reducedRateLoops);
        json.append(",\"renderedFrames\":").append(renderCount);
        json.append(",\"presentedFrames\":").append(presentationCount);
        json.append(",\"renderedFps\":").append(renderCount / seconds);
        json.append(",\"presentedFps\":").append(presentationCount / seconds);
        json.append(",\"updates\":").append(updateCount);
        json.append(",\"updatesPerSecond\":").append(updateCount / seconds);
        json.append(",\"activeLoopMs\":").append(activeTotal / 1_000_000.0);
        json.append(",\"activeLoopFraction\":").append(activeTotal / (seconds * SECOND));
        json.append(",\"droppedFrameSamples\":").append(droppedFrameSamples);
        json.append(",\"renderFrameIntervals\":").append(summary(renderIntervals, intervalCount));
        json.append(",\"presentationIntervals\":").append(summary(presentationIntervals, presentationIntervalCount));
        json.append(",\"renderProcessing\":").append(summary(renderDurations,
                Math.min(renderCount, renderDurations.length)));
        json.append(",\"activeProcessingPerFrame\":").append(summary(activeDurations, activeCount));
        long budget = SECOND / targetFps;
        json.append(",\"presentationIntervalsOverBudget\":")
                .append(countOver(presentationIntervals, presentationIntervalCount, budget + 1_000_000L));
        json.append(",\"presentationIntervalsOver1_5Budgets\":")
                .append(countOver(presentationIntervals, presentationIntervalCount, budget * 3 / 2));
        json.append(",\"processingFramesOverBudget\":")
                .append(countOver(activeDurations, activeCount, budget));
        json.append(",\"stages\":{");
        for (int index = 0; index < STAGES.length; index++) {
            if (index > 0) json.append(',');
            json.append(quote(STAGES[index])).append(':');
            json.append(summary(stageDurations[index], stageCounts[index]));
            json.setLength(json.length() - 1);
            json.append(",\"totalMs\":").append(stageTotals[index] / 1_000_000.0);
            json.append(",\"calls\":").append(stageCalls[index]);
            json.append(",\"droppedSamples\":").append(stageCalls[index] - stageCounts[index]).append('}');
        }
        json.append("},\"rawMs\":{\"renderIntervals\":");
        appendSamples(json, renderIntervals, intervalCount);
        json.append(",\"presentationIntervals\":");
        appendSamples(json, presentationIntervals, presentationIntervalCount);
        json.append(",\"activeProcessing\":");
        appendSamples(json, activeDurations, activeCount);
        json.append(",\"renderProcessing\":");
        appendSamples(json, renderDurations, Math.min(renderCount, renderDurations.length));
        return json.append("}}").toString();
    }

    private static String summary(long[] samples, int count) {
        long[] sorted = Arrays.copyOf(samples, count);
        Arrays.sort(sorted);
        long total = 0;
        for (long sample : sorted) total += sample;
        return "{\"samples\":" + count + ",\"meanMs\":" + (count == 0 ? 0 : total / (count * 1_000_000.0))
                + ",\"medianMs\":" + percentile(sorted, .50) + ",\"p95Ms\":" + percentile(sorted, .95)
                + ",\"p99Ms\":" + percentile(sorted, .99) + ",\"maxMs\":" + percentile(sorted, 1) + "}";
    }

    private static double percentile(long[] sorted, double fraction) {
        if (sorted.length == 0) return 0;
        return sorted[Math.max(0, (int) Math.ceil(sorted.length * fraction) - 1)] / 1_000_000.0;
    }

    private static long countOver(long[] values, int count, long limit) {
        long exceeded = 0;
        for (int index = 0; index < count; index++) if (values[index] > limit) exceeded++;
        return exceeded;
    }

    private static void appendSamples(StringBuilder json, long[] values, int count) {
        json.append('[');
        for (int index = 0; index < count; index++) {
            if (index > 0) json.append(',');
            json.append(values[index] / 1_000_000.0);
        }
        json.append(']');
    }

    private static String quote(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 2).append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '"' || character == '\\') escaped.append('\\');
            if (character == '\n') escaped.append("\\n");
            else if (character == '\r') escaped.append("\\r");
            else if (character == '\t') escaped.append("\\t");
            else if (character < 32) escaped.append('?');
            else escaped.append(character);
        }
        return escaped.append('"').toString();
    }
}
