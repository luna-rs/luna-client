package luna.browser.awt;

/**
 * Pixel containment required by the optional panel masks.
 *
 * @author lare96
 */
public interface Shape {
    boolean contains(double x, double y);
    Rectangle getBounds();
}
