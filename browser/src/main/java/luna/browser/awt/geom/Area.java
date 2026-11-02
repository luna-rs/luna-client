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
    public Area(Shape initial) { this.initial = initial; }
    public void subtract(Area area) { shapes.add(area); operations.add(0); }
    public void intersect(Area area) { shapes.add(area); operations.add(1); }
    public void add(Area area) { shapes.add(area); operations.add(2); }
    public boolean contains(double x, double y) {
        boolean result = initial.contains(x, y);
        for (int i = 0; i < shapes.size(); i++) {
            boolean inside = shapes.get(i).contains(x, y);
            int operation = operations.get(i);
            result = operation == 0 ? result && !inside : operation == 1 ? result && inside : result || inside;
        }
        return result;
    }
    public Rectangle getBounds() { return initial.getBounds(); }
}
