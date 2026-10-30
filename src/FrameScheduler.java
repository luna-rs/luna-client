/**
 * Schedules independent fixed-rate game updates and paced rendering using a monotonic clock.
 *
 * @author lare96
 */
final class FrameScheduler {

    static final long UPDATE_NANOS = 20_000_000L;
    static final int MAX_CATCH_UP_UPDATES = 5;
    private static final long SECOND_NANOS = 1_000_000_000L;
    private static final long RENDER_MARGIN_NANOS = 1_000_000L;

    private long nextUpdate;
    private long nextPresentation;
    private long nextRender;
    private long renderStarted;
    private long renderBudgetNanos;
    private boolean framePending;
    private int targetFps;
    private long frameNanos;
    private long frameRemainder;
    private long remainder;
    private long fpsWindowStart;
    private int fpsWindowFrames;
    private int framesPerSecond;
    private boolean hasPresentedFrame;

    FrameScheduler(long now, int targetFps) {
        nextUpdate = now;
        setTargetFps(targetFps, now);
    }

    int pendingUpdates(long now) {
        if (now - nextUpdate < 0) {
            return 0;
        }
        long updates = (now - nextUpdate) / UPDATE_NANOS + 1;
        if (updates > MAX_CATCH_UP_UPDATES) {
            nextUpdate = now + UPDATE_NANOS;
            return MAX_CATCH_UP_UPDATES;
        }
        nextUpdate += updates * UPDATE_NANOS;
        return (int) updates;
    }

    boolean isRenderDue(long now) {
        return !framePending && now - nextRender >= 0;
    }

    void frameStarted(long now) {
        renderStarted = now;
    }

    void frameRendered(long now) {
        long measured = Math.max(0L, now - renderStarted);
        long requestedBudget = Math.min(frameNanos,
                measured >= frameNanos - RENDER_MARGIN_NANOS ? frameNanos : measured + RENDER_MARGIN_NANOS);
        if (requestedBudget >= renderBudgetNanos) {
            renderBudgetNanos = requestedBudget;
        } else {
            renderBudgetNanos -= Math.max(1L, (renderBudgetNanos - requestedBudget) / 64L);
        }
        framePending = true;
    }

    boolean isPresentationDue(long now) {
        return framePending && presentationDeadlineReached(now);
    }

    boolean presentationDeadlineReached(long now) {
        return now - nextPresentation >= 0;
    }

    void frameCancelled(long now) {
        framePending = false;
        nextRender = now;
    }

    void frameCompleted(long now, boolean presented) {
        if (presented) {
            if (!hasPresentedFrame) {
                fpsWindowStart = now;
                hasPresentedFrame = true;
            } else {
                fpsWindowFrames++;
                long elapsed = now - fpsWindowStart;
                if (elapsed >= SECOND_NANOS) {
                    framesPerSecond = (int) ((fpsWindowFrames * SECOND_NANOS + elapsed / 2) / elapsed);
                    fpsWindowFrames = 0;
                    fpsWindowStart = now;
                }
            }
        }
        framePending = false;
        advanceFrames(1);
        if (now - nextPresentation >= 0) {
            long behind = now - nextPresentation;
            long missedFrames = behind / SECOND_NANOS * targetFps
                    + behind % SECOND_NANOS * targetFps / SECOND_NANOS + 1;
            advanceFrames(missedFrames);
            if (now - nextPresentation >= 0) {
                advanceFrames(1);
            }
        }
        nextRender = nextPresentation - renderBudgetNanos;
    }

    long nanosUntilWork(long now) {
        long nextFrameWork = framePending ? nextPresentation : nextRender;
        return Math.max(0L, Math.min(nextUpdate - now, nextFrameWork - now));
    }

    void setTargetFps(int targetFps, long now) {
        if (targetFps < 1 || targetFps > 1000) {
            throw new IllegalArgumentException("Frame rate must be between 1 and 1000");
        }
        this.targetFps = targetFps;
        frameNanos = SECOND_NANOS / targetFps;
        frameRemainder = SECOND_NANOS % targetFps;
        remainder = 0;
        nextPresentation = now;
        nextRender = now;
        renderBudgetNanos = frameNanos;
        framePending = false;
    }

    int getFramesPerSecond() {
        return framesPerSecond;
    }

    long getRenderBudgetNanos() {
        return renderBudgetNanos;
    }

    private void advanceFrames(long frames) {
        long fractions = remainder + frames * frameRemainder;
        nextPresentation += frames * frameNanos + fractions / targetFps;
        remainder = fractions % targetFps;
    }
}
