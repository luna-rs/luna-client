import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Checks frame-rate changes and lifecycle deadlines without opening a client window.
 *
 * @author lare96
 */
public final class JagAppletTimingTest {

    public static void main(String[] args) throws Exception {
        JagApplet applet = new JagApplet();
        check(JagApplet.TARGET_FPS == 60, "default presentation target is 60 FPS");
        for (int target : new int[] {60, 120, 144, 1}) {
            applet.setFramerate(target);
            check(applet.delayTime == 20, "render changes retain the 20 millisecond game step");
            check(field("targetFps").getInt(applet) == target, "requested render rate is applied");
        }
        long before = System.nanoTime();
        applet.stop();
        long after = System.nanoTime();
        long deadline = field("stopDeadlineNanos").getLong(applet);
        check(deadline - before >= 4_000_000_000L && deadline - after <= 4_000_000_000L,
                "stop uses a four second deadline even with rendering set to one FPS");
        check(applet.gameState > 0, "stop requests delayed shutdown");
        applet.start();
        check(applet.gameState == 0 && field("stopDeadlineNanos").getLong(applet) == 0L,
                "start cancels the pending shutdown");
        applet.gameState = -1;
        applet.start();
        applet.stop();
        check(applet.gameState == -1, "lifecycle callbacks do not revive a destroyed applet");
        immutableQueuedFrames();
        priorityBeforeCallback();
        System.out.println("JagAppletTimingTest passed");
    }

    private static void priorityBeforeCallback() throws Exception {
        JagApplet applet = new JagApplet();
        Thread caller = Thread.currentThread();
        int previousPriority = caller.getPriority();
        CountDownLatch completed = new CountDownLatch(1);
        AtomicInteger observedPriority = new AtomicInteger();
        AtomicReference<Thread> started = new AtomicReference<>();
        try {
            caller.setPriority(Thread.MIN_PRIORITY);
            applet.startThread(() -> {
                started.set(Thread.currentThread());
                observedPriority.set(Thread.currentThread().getPriority());
                completed.countDown();
            }, Thread.NORM_PRIORITY);
            check(completed.await(2, TimeUnit.SECONDS), "the client callback starts promptly");
            started.get().join(2000);
            check(!started.get().isAlive() && observedPriority.get() == Thread.NORM_PRIORITY,
                    "the callback begins at normal priority instead of inheriting a low-priority caller");
        } finally {
            caller.setPriority(previousPriority);
        }
    }

    private static void immutableQueuedFrames() throws Exception {
        JagApplet applet = new JagApplet();
        BufferedImage source = new BufferedImage(8, 6, BufferedImage.TYPE_INT_RGB);
        BufferedImage screen = new BufferedImage(8, 6, BufferedImage.TYPE_INT_RGB);
        Graphics2D screenGraphics = screen.createGraphics();
        field("backBuffer").set(applet, source);
        field("screenGraphics").set(applet, screenGraphics);
        try {
            Arrays.fill(pixels(source), 0x123456);
            check(call(applet, "queueFrame"), "a completed software frame can be queued");
            BufferedImage snapshot = (BufferedImage) field("presentationBuffer").get(applet);
            Arrays.fill(pixels(source), 0xabcdef);
            check(call(applet, "presentQueuedFrame") && screen.getRGB(0, 0) == 0xff123456,
                    "game-update drawing cannot alter an already completed queued image");
            check(call(applet, "queueFrame") && field("presentationBuffer").get(applet) == snapshot,
                    "unchanged dimensions reuse one presentation snapshot");
            check(call(applet, "presentQueuedFrame") && screen.getRGB(0, 0) == 0xffabcdef,
                    "a later frame presents its own completed pixels");

            call(applet, "queueFrame");
            field("backBuffer").set(applet, new BufferedImage(9, 6, BufferedImage.TYPE_INT_RGB));
            check(!call(applet, "queuedFrameIsValid") && !call(applet, "presentQueuedFrame"),
                    "replacing the back buffer cancels a queued image from the previous window size");
            call(applet, "queueFrame");
            applet.resizedWidth = 10;
            check(!call(applet, "queuedFrameIsValid"),
                    "an AWT resize cancels an old image before the game thread rebuilds its layout");
            applet.resizedWidth = -1;

            call(applet, "queueFrame");
            applet.invalidateFramePresentation();
            check(!call(applet, "queuedFrameIsValid"),
                    "a layout change can invalidate an image without replacing pixel storage");
            call(applet, "queueFrame");
            applet.presentBackBuffer();
            check(!call(applet, "presentQueuedFrame"),
                    "immediate loading progress cannot be overwritten by an older queued frame");
        } finally {
            screenGraphics.dispose();
        }
    }

    private static int[] pixels(BufferedImage image) {
        return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    private static boolean call(JagApplet applet, String name) throws Exception {
        Method method = JagApplet.class.getDeclaredMethod(name);
        method.setAccessible(true);
        return (boolean) method.invoke(applet);
    }

    private static Field field(String name) throws Exception {
        Field field = JagApplet.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
