import luna.browser.awt.Component;
import luna.browser.awt.Graphics;
import luna.browser.awt.Point;
import luna.browser.awt.Rectangle;
import luna.browser.awt.image.BufferedImage;

/**
 * Connects the original software pixel buffer to browser back-buffer compositing.
 *
 * @author lare96
 */
public class JagImageProducer {
    public int[] pixels;
    public int width;
    public int height;
    public final BufferedImage image;

    public JagImageProducer(int width, int height, Component component) {
        this.width = width; this.height = height;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = image.pixels;
        pushPixels();
    }
    public void pushPixels() { Drawable.putPixels(width, height, pixels); }
    public void flipBuffer() { if (pixels != image.pixels) System.arraycopy(pixels, 0, image.pixels, 0, image.pixels.length); }
    public void drawImage(int x, int y, Graphics graphics) { flipBuffer(); graphics.drawImage(image, x, y, null); }
    public void drawImage(Point point, Graphics graphics) { drawImage(point.x, point.y, graphics); }
    public void drawImage(Rectangle rectangle, Graphics graphics) { drawImage(rectangle.x, rectangle.y, graphics); }
    public void drawImageRegion(int x, int y, int sourceX, int sourceY, int width, int height, Graphics graphics) {
        flipBuffer(); graphics.drawImage(image, x, y, x + width, y + height, sourceX, sourceY, sourceX + width, sourceY + height, null);
    }
}
