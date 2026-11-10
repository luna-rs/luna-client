package luna.browser.awt.geom;

import java.util.ArrayList;
import java.util.List;
import luna.browser.awt.Rectangle;
import luna.browser.awt.Shape;

/**
 * Boolean shape operations used by the shared rounded-panel masks.
 *
 * @author lare96
 */
public class Area implements Shape {
    private final Shape initial;
    private final List<Shape> shapes = new ArrayList<>();
    private final List<Integer> operations = new ArrayList<>();
    public Area() { initial = null; }
    public Area(Shape initial) { this.initial = initial; }
    public void subtract(Area area) { shapes.add(area); operations.add(0); }
    public void intersect(Area area) { shapes.add(area); operations.add(1); }
    public void add(Area area) { shapes.add(area); operations.add(2); }
    public boolean contains(double x, double y) {
        boolean result = initial != null && initial.contains(x, y);
        for (int i = 0; i < shapes.size(); i++) {
            boolean inside = shapes.get(i).contains(x, y);
            int operation = operations.get(i);
            result = operation == 0 ? result && !inside : operation == 1 ? result && inside : result || inside;
        }
        return result;
    }
    public Rectangle getBounds() {
        Rectangle bounds = initial == null ? new Rectangle(0, 0, 0, 0) : initial.getBounds();
        for (int i = 0; i < shapes.size(); i++) {
            Rectangle next = shapes.get(i).getBounds();
            int operation = operations.get(i);
            if (operation == 0) continue;
            if (operation == 2 && (next.width <= 0 || next.height <= 0)) continue;
            if (operation == 2 && (bounds.width <= 0 || bounds.height <= 0)) {
                bounds = new Rectangle(next);
                continue;
            }
            int left = operation == 2 ? Math.min(bounds.x, next.x) : Math.max(bounds.x, next.x);
            int top = operation == 2 ? Math.min(bounds.y, next.y) : Math.max(bounds.y, next.y);
            int right = operation == 2 ? Math.max(bounds.x + bounds.width, next.x + next.width)
                    : Math.min(bounds.x + bounds.width, next.x + next.width);
            int bottom = operation == 2 ? Math.max(bounds.y + bounds.height, next.y + next.height)
                    : Math.min(bounds.y + bounds.height, next.y + next.height);
            bounds = new Rectangle(left, top, Math.max(0, right - left), Math.max(0, bottom - top));
        }
        return bounds;
    }
}
