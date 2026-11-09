import luna.browser.awt.Graphics2D;
import luna.browser.awt.geom.Area;

/**
 * Keeps the existing browser minimap presentation.
 *
 * @author lare96
 */
public final class ResizableMinimap {
    public static boolean enabled() { return false; }
    public static boolean contains(int x, int y) { return true; }
    public static boolean containsMap(int x, int y) { return true; }
    public static Area outline() { throw new UnsupportedOperationException(); }
    public static void drawBorder(Graphics2D graphics) { throw new UnsupportedOperationException(); }
}
