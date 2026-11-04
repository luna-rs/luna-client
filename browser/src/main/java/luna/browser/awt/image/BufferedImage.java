package luna.browser.awt.image;

import luna.browser.awt.Graphics2D;
import luna.browser.awt.Image;

/**
 * Integer framebuffer used by the browser build's shared drawing code.
 *
 * @author lare96
 */
public class BufferedImage extends Image {
    public static final int TYPE_INT_RGB = 1, TYPE_INT_ARGB = 2;
    public BufferedImage(int width, int height, int type) { super(width, height, type == TYPE_INT_ARGB); }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Graphics2D createGraphics() { return new Graphics2D(this); }
    public int getRGB(int x, int y) { int value = pixels[y * width + x]; return alpha ? value : value | 0xff000000; }
    public void setRGB(int x, int y, int value) { pixels[y * width + x] = value; }
    public int[] getRGB(int x, int y, int width, int height, int[] output, int offset, int stride) {
        if (output == null) output = new int[offset + height * stride];
        for (int row = 0; row < height; row++) for (int column = 0; column < width; column++)
            output[offset + row * stride + column] = getRGB(x + column, y + row);
        return output;
    }
    public BufferedImage getSubimage(int x, int y, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, alpha ? TYPE_INT_ARGB : TYPE_INT_RGB);
        getRGB(x, y, width, height, image.pixels, 0, width);
        return image;
    }
    public Raster getRaster() { return new Raster(pixels); }
}
