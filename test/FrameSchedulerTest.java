/**
 * Exercises independent rendering and update timing with a deterministic monotonic clock.
 *
 * @author lare96
 */
public final class FrameSchedulerTest {

    public static void main(String[] args) {
        independentRates(60, 120, 100);
        independentRates(144, 288, 100);
        independentRates(1, 2, 100);
        boundedCatchUp();
        renderStallsAndRateChanges();
        expensiveFramesDoNotAddAnExtraWait();
        measuredFrameRate();
        monotonicClockOrigins();
        invalidRates();
        System.out.println("FrameSchedulerTest passed");
    }

    private static void independentRates(int target, int expectedFrames, int expectedUpdates) {
        FrameScheduler scheduler = new FrameScheduler(0L, target);
        int frames = 0;
        int updates = 0;
        long now = 0;
        while (now < 2_000_000_000L) {
            updates += scheduler.pendingUpdates(now);
            if (scheduler.isRenderDue(now)) {
                frames++;
                scheduler.frameStarted(now);
                scheduler.frameCompleted(now, true);
            }
            long wait = scheduler.nanosUntilWork(now);
            check(wait > 0, "finished work always schedules a blocking wait");
            now += wait;
        }
        check(frames == expectedFrames, "exact nanosecond frame pacing at " + target + " FPS");
        check(updates == expectedUpdates, "game updates remain 50 Hz at " + target + " FPS");
    }

    private static void boundedCatchUp() {
        FrameScheduler scheduler = new FrameScheduler(0L, 60);
        check(scheduler.pendingUpdates(0L) == 1, "initial update is immediate");
        check(scheduler.pendingUpdates(80_000_000L) == 4, "brief stalls preserve their due updates");
        check(scheduler.pendingUpdates(100_000_000L) == 1, "brief catch-up preserves update phase");
        check(scheduler.pendingUpdates(5_000_000_000L) == FrameScheduler.MAX_CATCH_UP_UPDATES,
                "long stalls have a bounded update budget");
        check(scheduler.pendingUpdates(5_000_000_000L) == 0, "excess backlog is discarded");
        check(scheduler.pendingUpdates(5_019_999_999L) == 0, "next update waits a full fixed interval");
        check(scheduler.pendingUpdates(5_020_000_000L) == 1, "updates resume at their original rate");
    }

    private static void renderStallsAndRateChanges() {
        FrameScheduler scheduler = new FrameScheduler(0L, 60);
        scheduler.pendingUpdates(0L);
        scheduler.frameStarted(0L);
        scheduler.frameCompleted(0L, true);
        check(!scheduler.isRenderDue(16_666_665L), "60 FPS does not round down to 16 milliseconds");
        check(scheduler.isRenderDue(16_666_666L), "first fractional deadline is due");
        scheduler.frameStarted(200_000_000L);
        scheduler.frameCompleted(200_000_000L, true);
        check(!scheduler.isRenderDue(200_000_000L), "missed frames do not cause a render burst");
        check(scheduler.isRenderDue(216_666_666L), "render pacing keeps the original phase after a stall");
        scheduler.setTargetFps(1, 200_000_000L);
        scheduler.frameStarted(200_000_000L);
        scheduler.frameCompleted(200_000_000L, true);
        check(!scheduler.isRenderDue(1_199_999_999L), "error screen can retain its one FPS cap");
        check(scheduler.isRenderDue(1_200_000_000L), "one FPS is an exact second");
        check(scheduler.pendingUpdates(220_000_000L) == FrameScheduler.MAX_CATCH_UP_UPDATES,
                "changing render rate does not rewrite the game update clock");
        check(scheduler.pendingUpdates(240_000_000L) == 1, "logic still resumes after 20 milliseconds");
    }

    private static void expensiveFramesDoNotAddAnExtraWait() {
        FrameScheduler scheduler = new FrameScheduler(0L, 60);
        for (long start = 0; start < 100_000_000L; start += 20_000_000L) {
            check(scheduler.isRenderDue(start), "slow rendering remains limited by its actual cost");
            scheduler.pendingUpdates(start);
            scheduler.frameStarted(start);
            long finish = start + 20_000_000L;
            scheduler.frameCompleted(finish, true);
            check(scheduler.nanosUntilWork(finish) == 0,
                    "render time exceeding the budget does not add another idle frame");
        }
    }

    private static void measuredFrameRate() {
        FrameScheduler scheduler = new FrameScheduler(0L, 60);
        for (int frame = 0; frame <= 60; frame++) {
            scheduler.frameCompleted(frame * 1_000_000_000L / 60, true);
        }
        check(scheduler.getFramesPerSecond() == 60, "FPS measures completed frame intervals");
        scheduler = new FrameScheduler(0L, 60);
        for (int frame = 0; frame <= 60; frame++) {
            scheduler.frameCompleted(frame * 1_000_000_000L / 60, frame % 2 == 0);
        }
        check(scheduler.getFramesPerSecond() == 30, "unpresented frames do not inflate FPS");
        scheduler = new FrameScheduler(0L, 60);
        for (int frame = 0; frame <= 30; frame++) {
            scheduler.frameCompleted(frame * 1_000_000_000L / 30, true);
        }
        check(scheduler.getFramesPerSecond() == 30, "slow rendering reports its measured rate instead of target");
    }

    private static void monotonicClockOrigins() {
        for (long origin : new long[] {-23_000_000_000L, Long.MAX_VALUE - 5_000_000L}) {
            FrameScheduler scheduler = new FrameScheduler(origin, 60);
            check(scheduler.pendingUpdates(origin) == 1, "arbitrary clock origin starts immediately");
            scheduler.frameStarted(origin);
            scheduler.frameCompleted(origin, true);
            check(scheduler.nanosUntilWork(origin) == 16_666_666L,
                    "negative origins and nanoTime overflow retain the same delay");
            long next = origin + 16_666_666L;
            check(scheduler.isRenderDue(next), "frame deadlines survive nanoTime overflow");
            check(scheduler.pendingUpdates(next) == 0, "update deadline is still separate after overflow");
        }
    }

    private static void invalidRates() {
        for (int rate : new int[] {Integer.MIN_VALUE, 0, 1001, Integer.MAX_VALUE}) {
            boolean rejected = false;
            try {
                new FrameScheduler(0L, rate);
            } catch (IllegalArgumentException expected) {
                rejected = true;
            }
            check(rejected, "invalid rate rejected: " + rate);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
