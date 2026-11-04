package luna.browser.awt;

/**
 * Pixel-backed image shared between the browser decoder and software renderer.
 *
 * @author lare96
 */
public class Image {
    public final int width;
    public final int height;
    public final int[] pixels;
    public final boolean alpha;
    public Image(int width, int height, boolean alpha) { this.width = width; this.height = height; this.alpha = alpha; pixels = new int[width * height]; }
    public int getWidth(Object observer) { return width; }
    public int getHeight(Object observer) { return height; }
}
