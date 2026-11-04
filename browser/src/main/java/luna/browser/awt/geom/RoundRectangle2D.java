package luna.browser.awt.geom;

import luna.browser.awt.Rectangle;
import luna.browser.awt.Shape;

/**
 * Rounded-rectangle containment for resizable panel masks.
 *
 * @author lare96
 */
public abstract class RoundRectangle2D implements Shape {
    /**
     * Retains the double-precision shape coordinates used by the shared compositor.
     *
     * @author lare96
     */
    public static class Double extends RoundRectangle2D {
        private final double x, y, width, height, radiusX, radiusY;
        public Double(double x, double y, double width, double height, double arcWidth, double arcHeight) {
            this.x = x; this.y = y; this.width = width; this.height = height;
            radiusX = Math.min(width / 2, arcWidth / 2); radiusY = Math.min(height / 2, arcHeight / 2);
        }
        public boolean contains(double px, double py) {
            if (px < x || py < y || px >= x + width || py >= y + height) return false;
            double cx = Math.max(x + radiusX, Math.min(x + width - radiusX, px));
            double cy = Math.max(y + radiusY, Math.min(y + height - radiusY, py));
            if (radiusX <= 0 || radiusY <= 0) return true;
            double dx = (px - cx) / radiusX, dy = (py - cy) / radiusY;
            return dx * dx + dy * dy <= 1;
        }
        public Rectangle getBounds() { return new Rectangle((int) x, (int) y, (int) Math.ceil(width), (int) Math.ceil(height)); }
    }
}
