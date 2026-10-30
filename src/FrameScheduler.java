/**
 * Schedules independent fixed-rate game updates and paced rendering using a monotonic clock.
 *
 * @author lare96
 */
final class FrameScheduler {

    static final long UPDATE_NANOS = 20_000_000L;
    static final int MAX_CATCH_UP_UPDATES = 5;
    private static final long SECOND_NANOS = 1_000_000_000L;

    private long nextUpdate;
    private long nextFrame;
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
        return now - nextFrame >= 0;
    }

    void frameStarted(long now) {
        long behind = Math.max(0L, now - nextFrame);
        long frames = behind / SECOND_NANOS * targetFps
                + behind % SECOND_NANOS * targetFps / SECOND_NANOS + 1;
        advanceFrames(frames);
        if (now - nextFrame >= 0) {
            advanceFrames(1);
        }
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
    }

    long nanosUntilWork(long now) {
        return Math.max(0L, Math.min(nextUpdate - now, nextFrame - now));
    }

    void setTargetFps(int targetFps, long now) {
        if (targetFps < 1 || targetFps > 1000) {
            throw new IllegalArgumentException("Frame rate must be between 1 and 1000");
        }
        this.targetFps = targetFps;
        frameNanos = SECOND_NANOS / targetFps;
        frameRemainder = SECOND_NANOS % targetFps;
        remainder = 0;
        nextFrame = now;
    }

    int getFramesPerSecond() {
        return framesPerSecond;
    }

    private void advanceFrames(long frames) {
        long fractions = remainder + frames * frameRemainder;
        nextFrame += frames * frameNanos + fractions / targetFps;
        remainder = fractions % targetFps;
    }
}
