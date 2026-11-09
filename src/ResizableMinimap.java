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
    public static boolean enabled() { return true; }

    public static boolean contains(int x, int y) {
        long compassX = (long) x - 17;
        long compassY = (long) y - 17;
        long mapX = (long) x - 97;
        long mapY = (long) y - 77;
        return mapX * mapX + mapY * mapY <= 75 * 75
                || compassX * compassX + compassY * compassY <= 18 * 18;
    }

    public static boolean containsMap(int x, int y) {
        long dx = (long) x - 97;
        long dy = (long) y - 77;
        return dx * dx + dy * dy < 74 * 74;
    }

    public static Area outline() {
        Area result = new Area(new Ellipse2D.Double(23, 3, 148, 148));
        result.add(new Area(new Ellipse2D.Double(0, 0, 34, 34)));
        return result;
    }

    public static void drawBorder(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(28, 25, 20));
        graphics.drawOval(23, 3, 147, 147);
        graphics.setColor(new Color(111, 101, 77));
        graphics.drawOval(24, 4, 145, 145);
        graphics.setColor(new Color(28, 25, 20));
        graphics.drawOval(0, 0, 33, 33);
    }
}
