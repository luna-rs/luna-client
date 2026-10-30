import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
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
        idleFramerates();
        scheduledPresentation();
        priorityBeforeCallback();
        System.out.println("JagAppletTimingTest passed");
    }

    private static void idleFramerates() {
        client game = new client();
        check(game.getEffectiveFramerate() == 30, "the focused title screen is capped at 30 FPS");
        game.isLoggedIn = true;
        check(game.getEffectiveFramerate() == 60, "logging in restores the foreground game rate");
        game.setFramerate(120);
        check(game.getEffectiveFramerate() == 120, "foreground play retains a custom requested rate");
        game.focusLost(null);
        check(game.getEffectiveFramerate() == 10, "an unfocused game is capped at 10 FPS");
        game.isLoggedIn = false;
        check(game.getEffectiveFramerate() == 10, "an unfocused title screen retains the lower background cap");
        game.windowIconified(null);
        check(game.getEffectiveFramerate() == 1, "a minimized window is capped at one FPS");
        game.focusGained(null);
        check(game.getEffectiveFramerate() == 1, "minimization takes precedence over focus");
        game.windowDeiconified(null);
        check(game.getEffectiveFramerate() == 30, "restoring a focused title screen resumes its title cap");
        game.isLoggedIn = true;
        check(game.getEffectiveFramerate() == 120, "restoring the game retains the original requested rate");
        game.setFramerate(1);
        check(game.getEffectiveFramerate() == 1, "an explicit low rate is never raised by the foreground policy");
        game.focusLost(null);
        check(game.getEffectiveFramerate() == 1, "background throttling never raises an explicit lower rate");
        game.isLoggedIn = false;
        game.focusGained(null);
        check(game.getEffectiveFramerate() == 1, "the title cap preserves an explicit lower rate");
        check(game.delayTime == 20, "visibility and login transitions leave the fixed game step unchanged");
    }

    private static void scheduledPresentation() throws Exception {
        BufferedImage source = new BufferedImage(8, 6, BufferedImage.TYPE_INT_RGB);
        BufferedImage screen = new BufferedImage(8, 6, BufferedImage.TYPE_INT_RGB);
        Graphics2D screenGraphics = screen.createGraphics();
        PresentingApplet applet = new PresentingApplet(source, screen);
        field("backBuffer").set(applet, source);
        field("screenGraphics").set(applet, screenGraphics);
        Method render = JagApplet.class.getDeclaredMethod("renderScheduledFrame");
        render.setAccessible(true);
        try {
            render.invoke(applet);
            check(screen.getRGB(0, 0) == 0xff000000,
                    "nested title drawing cannot present before the scheduled frame is complete");
            check(call(applet, "queueFrame") && call(applet, "presentQueuedFrame")
                            && screen.getRGB(0, 0) == 0xff123456,
                    "the completed scheduled image reaches the screen through the paced presentation");
            Arrays.fill(pixels(screen), 0);
            applet.fail = true;
            try {
                render.invoke(applet);
                throw new AssertionError("the fixture must propagate its drawing failure");
            } catch (InvocationTargetException exception) {
                check(exception.getCause() instanceof IllegalStateException,
                        "a drawing failure reaches the caller");
            }
            Arrays.fill(pixels(source), 0xabcdef);
            check(call(applet, "queueFrame"), "a frame can be queued after a drawing failure");
            applet.presentBackBuffer();
            check(screen.getRGB(0, 0) == 0xffabcdef && !call(applet, "presentQueuedFrame"),
                    "immediate loading progress still presents and invalidates older frames after drawing fails");
        } finally {
            screenGraphics.dispose();
        }
    }

    /**
     * Models title drawing that requests presentation before its scheduled render finishes.
     *
     * @author lare96
     */
    private static final class PresentingApplet extends JagApplet {
        private final BufferedImage source;
        private final BufferedImage screen;
        private boolean fail;

        private PresentingApplet(BufferedImage source, BufferedImage screen) {
            this.source = source;
            this.screen = screen;
        }

        @Override
        public void repaintGame() {
            Arrays.fill(pixels(source), 0x123456);
            presentBackBuffer();
            presentBackBuffer();
            check(screen.getRGB(0, 0) == 0xff000000,
                    "multiple presentation requests during drawing leave the screen untouched");
            if (fail) throw new IllegalStateException("fixture draw failure");
        }
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
