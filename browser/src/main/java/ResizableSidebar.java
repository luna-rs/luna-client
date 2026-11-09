import luna.browser.awt.geom.Area;

/**
 * Keeps the existing browser sidebar presentation.
 *
 * @author lare96
 */
public final class ResizableSidebar {
    public static boolean enabled() { return false; }
    public static Area outline() { throw new UnsupportedOperationException(); }
    public static boolean contains(int x, int y) { return true; }
}
