import java.awt.geom.Area;
import java.awt.geom.Path2D;

/**
 * Trims the stray fixed-frame pixels around the original resizable sidebar artwork.
 *
 * @author lare96
 */
public final class ResizableSidebar {
    // Pixel boundaries follow the native rock shoulders in backhmid1, starting below its fixed-mode strip.
    private static final int[] TOP_LEFT = {
            44, 42, 40, 38, 36, 34, 32, 30, 28, 26, 25, 24, 23,
            22, 21, 21, 20, 20, 19, 19, 18, 18, 17, 17, 16, 16
    };
    private static final int[] TOP_RIGHT = {
            220, 224, 228, 231, 234, 237, 239, 241, 243, 244, 245, 246, 247,
            247, 248, 248, 249, 249, 249, 249, 249, 249, 249, 249, 249, 249
    };
    private static final Path2D OUTLINE = createOutline();

    private static Path2D createOutline() {
        Path2D path = new Path2D.Double();
        path.moveTo(TOP_LEFT[0], 8);
        path.lineTo(TOP_RIGHT[0], 8);
        for (int row = 0; row < TOP_RIGHT.length - 1; row++) {
            path.lineTo(TOP_RIGHT[row], row + 9);
            path.lineTo(TOP_RIGHT[row + 1], row + 9);
        }
        path.lineTo(249, 315);
        path.curveTo(249, 331, 237, 343, 221, 343);
        path.lineTo(45, 343);
        path.curveTo(30, 343, 16, 331, 16, 315);
        path.lineTo(TOP_LEFT[TOP_LEFT.length - 1], TOP_LEFT.length + 7);
        for (int row = TOP_LEFT.length - 2; row >= 0; row--) {
            path.lineTo(TOP_LEFT[row + 1], row + 9);
            path.lineTo(TOP_LEFT[row], row + 9);
        }
        path.closePath();
        return path;
    }

    public static boolean enabled() { return true; }

    public static Area outline() { return new Area(OUTLINE); }

    public static boolean contains(int x, int y) { return OUTLINE.intersects(x, y, 1, 1); }
}
