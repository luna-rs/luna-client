import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * Shapes the native resizable minimap without changing its fixed-mode sprite.
 *
 * @author lare96
 */
public final class ResizableMinimap {
    private static final int[] ROW_OFFSETS = new int[151];
    private static final int[] ROW_LENGTHS = new int[151];

    static {
        for (int row = 0; row < ROW_OFFSETS.length; row++) {
            int first = 172, last = -1;
            for (int x = 25; x < 172; x++) {
                if (!containsMap(x, row + 5)) continue;
                first = Math.min(first, x);
                last = x;
            }
            ROW_OFFSETS[row] = last < first ? 0 : first - 25;
            ROW_LENGTHS[row] = last < first ? 0 : last - first + 1;
        }
    }

    public static boolean enabled() { return true; }
    public static int[] rowOffsets(int[] fallback) { return ROW_OFFSETS; }
    public static int[] rowLengths(int[] fallback) { return ROW_LENGTHS; }

    public static boolean contains(int x, int y) {
        long compassX = (long) x - 17;
        long compassY = (long) y - 17;
        long mapX = (long) x - 98;
        long mapY = (long) y - 80;
        return mapX * mapX + mapY * mapY <= 74 * 74
                || compassX * compassX + compassY * compassY <= 18 * 18;
    }

    public static boolean containsMap(int x, int y) {
        long dx = (long) x - 98;
        long dy = (long) y - 80;
        return dx * dx + dy * dy <= 73 * 73;
    }

    public static Area outline() {
        Area result = new Area(new Ellipse2D.Double(25, 7, 146, 146));
        result.add(new Area(new Ellipse2D.Double(0, 0, 34, 34)));
        return result;
    }

    public static void drawBorder(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(28, 25, 20));
        graphics.drawOval(25, 7, 145, 145);
        graphics.setColor(new Color(111, 101, 77));
        graphics.drawOval(26, 8, 143, 143);
        graphics.setColor(new Color(28, 25, 20));
        graphics.drawOval(0, 0, 33, 33);
    }
}
