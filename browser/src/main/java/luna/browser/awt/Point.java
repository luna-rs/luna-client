package luna.browser.awt;

/**
 * Integer position used by the shared client layout.
 *
 * @author lare96
 */
public class Point {
    public int x;
    public int y;
    public Point() { this(0, 0); }
    public Point(int x, int y) { this.x = x; this.y = y; }
    public Point(Point point) { this(point.x, point.y); }
}
