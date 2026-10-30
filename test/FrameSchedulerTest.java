/**
 * Exercises independent rendering and update timing with a deterministic monotonic clock.
 *
 * @author lare96
 */
public final class FrameSchedulerTest {

    public static void main(String[] args) {
        independentRates(60, 120, 100);
        independentRates(120, 240, 100);
        independentRates(144, 288, 100);
        independentRates(30, 60, 100);
        independentRates(10, 20, 100);
        independentRates(1, 2, 100);
        boundedCatchUp();
        renderStallsAndRateChanges();
        expensiveFramesDoNotAddAnExtraWait();
        varyingRenderTimesKeepPresentationCadence();
        cancelledFramesCanBeReplaced();
        renderBudgetRecoversAfterStalls();
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
                scheduler.frameStarted(now);
                scheduler.frameRendered(now);
            }
            if (scheduler.isPresentationDue(now)) {
                frames++;
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
        scheduler.frameRendered(0L);
        scheduler.frameCompleted(0L, true);
        scheduler.frameStarted(1_000_000L);
        scheduler.frameRendered(2_000_000L);
        check(!scheduler.isPresentationDue(16_666_665L), "60 FPS does not round down to 16 milliseconds");
        check(scheduler.isPresentationDue(16_666_666L), "first fractional presentation deadline is due");
        scheduler.frameCancelled(200_000_000L);
        scheduler.frameStarted(200_000_000L);
        scheduler.frameRendered(200_000_000L);
        scheduler.frameCompleted(200_000_000L, true);
        check(!scheduler.isPresentationDue(200_000_000L), "missed frames do not cause a presentation burst");
        scheduler.pendingUpdates(200_000_000L);
        scheduler.setTargetFps(1, 200_000_000L);
        scheduler.frameStarted(200_000_000L);
        scheduler.frameRendered(200_000_000L);
        scheduler.frameCompleted(200_000_000L, true);
        scheduler.frameStarted(300_000_000L);
        scheduler.frameRendered(300_000_000L);
        check(!scheduler.isPresentationDue(1_199_999_999L), "error screen can retain its one FPS cap");
        check(scheduler.isPresentationDue(1_200_000_000L), "one FPS is an exact second");
        check(scheduler.pendingUpdates(220_000_000L) == 1,
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
            scheduler.frameRendered(finish);
            check(scheduler.isPresentationDue(finish), "over-budget frames present immediately when finished");
            scheduler.frameCompleted(finish, true);
            check(scheduler.nanosUntilWork(finish) == 0,
                    "render time exceeding the budget does not add another idle frame");
            check(scheduler.isRenderDue(finish + 1L), "over-budget rendering does not quantize to 30 FPS");
        }
    }

    private static void varyingRenderTimesKeepPresentationCadence() {
        FrameScheduler scheduler = new FrameScheduler(0L, 60);
        int[] costs = {3, 7, 11};
        long now = 0;
        int renders = 0;
        int presentations = 0;
        int updates = 0;
        int updatesWhilePending = 0;
        boolean pending = false;
        while (now < 2_000_000_000L) {
            int due = scheduler.pendingUpdates(now);
            updates += due;
            if (pending) updatesWhilePending += due;
            if (scheduler.isRenderDue(now)) {
                scheduler.frameStarted(now);
                now += costs[renders++ % costs.length] * 1_000_000L;
                scheduler.frameRendered(now);
                pending = true;
            }
            if (scheduler.isPresentationDue(now)) {
                if (presentations > 0) {
                    check(now == presentations * 1_000_000_000L / 60,
                            "variable 3/7/11 millisecond renders retain evenly spaced presentations");
                }
                presentations++;
                scheduler.frameCompleted(now, true);
                pending = false;
            }
            long wait = scheduler.nanosUntilWork(now);
            now += wait;
        }
        check(presentations == 120 && updates == 100,
                "variable drawing retains 60 presentations and 50 game updates each second");
        check(updatesWhilePending > 0, "game updates continue while a completed image waits for presentation");
    }

    private static void cancelledFramesCanBeReplaced() {
        FrameScheduler scheduler = new FrameScheduler(0L, 60);
        scheduler.frameStarted(0L);
        scheduler.frameRendered(3_000_000L);
        scheduler.frameCompleted(3_000_000L, true);
        scheduler.frameStarted(4_000_000L);
        scheduler.frameRendered(7_000_000L);
        check(!scheduler.isRenderDue(7_000_000L), "a queued frame prevents drawing over its snapshot");
        scheduler.frameCancelled(8_000_000L);
        check(scheduler.isRenderDue(8_000_000L), "invalidated snapshots can be redrawn immediately");
        check(!scheduler.isPresentationDue(20_000_000L), "a cancelled image is never presented");
        scheduler.setTargetFps(120, 8_000_000L);
        check(scheduler.isRenderDue(8_000_000L) && !scheduler.isPresentationDue(8_000_000L),
                "changing target discards the pending cadence and begins a fresh frame");
    }

    private static void renderBudgetRecoversAfterStalls() {
        FrameScheduler scheduler = new FrameScheduler(0L, 60);
        scheduler.frameStarted(0L);
        scheduler.frameRendered(100_000_000L);
        check(scheduler.getRenderBudgetNanos() <= 16_666_666L,
                "one slow frame cannot reserve more than one presentation interval");
        for (int frame = 0; frame < 256; frame++) {
            long start = 100_000_000L + frame * 20_000_000L;
            scheduler.frameCancelled(start);
            scheduler.frameStarted(start);
            scheduler.frameRendered(start + 1_000_000L);
        }
        check(scheduler.getRenderBudgetNanos() < 3_000_000L,
                "render lead time decays again after a temporary stall");
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
            scheduler.frameRendered(origin);
            scheduler.frameCompleted(origin, true);
            long renderDelay = scheduler.nanosUntilWork(origin);
            check(renderDelay > 0 && renderDelay < 16_666_666L,
                    "negative origins and nanoTime overflow retain a valid early render deadline");
            scheduler.frameStarted(origin + renderDelay);
            scheduler.frameRendered(origin + renderDelay);
            long next = origin + 16_666_666L;
            check(!scheduler.isPresentationDue(next - 1L) && scheduler.isPresentationDue(next),
                    "presentation deadlines survive nanoTime overflow");
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
