package luna.browser.awt.image;

import luna.browser.awt.Image;

/**
 * Copies decoded browser image pixels into the original sprite buffer.
 *
 * @author lare96
 */
public class PixelGrabber {
    private final Image image;
    private final int x, y, width, height, offset, stride;
    private final int[] pixels;
    public PixelGrabber(Image image, int x, int y, int width, int height, int[] pixels, int offset, int stride) {
        this.image = image; this.x = x; this.y = y; this.width = width; this.height = height;
        this.pixels = pixels; this.offset = offset; this.stride = stride;
    }
    public boolean grabPixels() {
        for (int row = 0; row < height; row++)
            System.arraycopy(image.pixels, (y + row) * image.width + x, pixels, offset + row * stride, width);
        return true;
    }
}
