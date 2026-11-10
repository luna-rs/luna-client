package luna.browser.awt.geom;

import luna.browser.awt.Rectangle;
import luna.browser.awt.Shape;

/**
 * Elliptical containment for the shared circular minimap and compass masks.
 *
 * @author lare96
 */
public abstract class Ellipse2D implements Shape {
    /**
     * Retains the double-precision ellipse bounds used by the shared compositor.
     *
     * @author lare96
     */
    public static class Double extends Ellipse2D {
        private final double x, y, width, height;

        public Double(double x, double y, double width, double height) {
            this.x = x; this.y = y; this.width = width; this.height = height;
        }

        public boolean contains(double px, double py) {
            if (width <= 0 || height <= 0) return false;
            double dx = (px - x) / width - .5, dy = (py - y) / height - .5;
            return dx * dx + dy * dy < .25;
        }

        public Rectangle getBounds() {
            int left = (int) Math.floor(x), top = (int) Math.floor(y);
            return new Rectangle(left, top, width <= 0 ? 0 : (int) Math.ceil(x + width) - left,
                    height <= 0 ? 0 : (int) Math.ceil(y + height) - top);
        }
    }
}
