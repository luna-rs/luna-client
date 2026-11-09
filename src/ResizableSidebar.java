import java.awt.geom.Area;
import java.awt.geom.Path2D;

/**
 * Trims the stray fixed-frame pixels around the original resizable sidebar artwork.
 *
 * @author lare96
 */
public final class ResizableSidebar {
    private static final Path2D OUTLINE = createOutline();

    private static Path2D createOutline() {
        Path2D path = new Path2D.Double();
        path.moveTo(45, 0);
        path.lineTo(221, 0);
        path.curveTo(237, 0, 249, 12, 249, 28);
        path.lineTo(249, 315);
        path.curveTo(249, 331, 237, 343, 221, 343);
        path.lineTo(45, 343);
        path.curveTo(30, 343, 16, 331, 16, 315);
        path.lineTo(16, 28);
        path.curveTo(16, 12, 29, 0, 45, 0);
        path.closePath();
        return path;
    }

    public static boolean enabled() { return true; }

    public static Area outline() { return new Area(OUTLINE); }

    public static boolean contains(int x, int y) { return OUTLINE.intersects(x, y, 1, 1); }
}
