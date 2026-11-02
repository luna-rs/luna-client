package luna.browser.awt;

/**
 * Integer rectangle used by the shared client layout.
 *
 * @author lare96
 */
public class Rectangle implements Shape {
    public int x;
    public int y;
    public int width;
    public int height;
    public Rectangle(int x, int y, int width, int height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
    }
    public Rectangle(Rectangle rectangle) { this(rectangle.x, rectangle.y, rectangle.width, rectangle.height); }
    public boolean contains(int x, int y) { return x >= this.x && y >= this.y && x < this.x + width && y < this.y + height; }
    public boolean contains(double x, double y) { return x >= this.x && y >= this.y && x < this.x + width && y < this.y + height; }
    public Rectangle getBounds() { return new Rectangle(this); }
}
