import java.lang.reflect.Field;

/**
 * Checks frame-rate changes and lifecycle deadlines without opening a client window.
 *
 * @author lare96
 */
public final class JagAppletTimingTest {

    public static void main(String[] args) throws Exception {
        JagApplet applet = new JagApplet();
        check(JagApplet.TARGET_FPS == 60, "default render target is 60 FPS");
        for (int target : new int[] {60, 144, 1}) {
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
        System.out.println("JagAppletTimingTest passed");
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
