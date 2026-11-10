package luna.browser.awt;

import java.util.Arrays;
import luna.browser.awt.geom.AffineTransform;
import luna.browser.awt.image.BufferedImage;

/**
 * Composes software-rendered client panels into an integer back buffer.
 *
 * @author lare96
 */
public class Graphics {
    protected final BufferedImage target;
    protected int translateX, translateY;
    protected int clipX, clipY, clipRight, clipBottom;
    protected Color color = Color.white;
    protected Font font = new Font("Helvetica", 0, 13);
    protected AlphaComposite composite = AlphaComposite.SrcOver;

    public Graphics(BufferedImage target) {
        this.target = target; clipRight = target.width; clipBottom = target.height;
    }

    public void setColor(Color color) { this.color = color; }
    public void setFont(Font font) { this.font = font; }
    public void translate(int x, int y) { translateX += x; translateY += y; }
    public void setTransform(AffineTransform transform) { translateX = (int) transform.x; translateY = (int) transform.y; }
    public void setComposite(AlphaComposite composite) { this.composite = composite; }
    public void setRenderingHint(Object key, Object value) { }
    public void dispose() { }
    public void setClip(int x, int y, int width, int height) {
        clipX = Math.max(0, x + translateX); clipY = Math.max(0, y + translateY);
        clipRight = Math.min(target.width, x + translateX + width); clipBottom = Math.min(target.height, y + translateY + height);
    }

    public Graphics create() {
        Graphics copy = new Graphics2D(target);
        copy.translateX = translateX; copy.translateY = translateY;
        copy.clipX = clipX; copy.clipY = clipY; copy.clipRight = clipRight; copy.clipBottom = clipBottom;
        copy.color = color; copy.font = font; copy.composite = composite;
        return copy;
    }

    public void fillRect(int x, int y, int width, int height) {
        int left = Math.max(clipX, x + translateX), top = Math.max(clipY, y + translateY);
        int right = Math.min(clipRight, x + translateX + width), bottom = Math.min(clipBottom, y + translateY + height);
        int argb = color.getRGB();
        if (composite.rule == AlphaComposite.CLEAR || composite.rule == AlphaComposite.SRC
                || composite.rule == AlphaComposite.SRC_OVER && composite.alpha == 1 && argb >>> 24 == 255) {
            int value = composite.rule == AlphaComposite.CLEAR ? 0 : argb;
            if (composite.rule == AlphaComposite.SRC && composite.alpha != 1)
                value = argb & 0xffffff | Math.max(0, Math.min(255, Math.round((argb >>> 24) * composite.alpha))) << 24;
            if (right > left) for (int row = top; row < bottom; row++)
                Arrays.fill(target.pixels, row * target.width + left, row * target.width + right, value);
            return;
        }
        for (int row = top; row < bottom; row++) for (int column = left; column < right; column++)
            pixel(column, row, argb);
    }

    public void drawRect(int x, int y, int width, int height) {
        fillRect(x, y, width + 1, 1); fillRect(x, y + height, width + 1, 1);
        fillRect(x, y, 1, height); fillRect(x + width, y, 1, height);
    }

    /**
     * Draws a one-pixel antialiased ellipse while retaining translation, clipping and compositing.
     */
    public void drawOval(int x, int y, int width, int height) {
        if (width < 0 || height < 0) return;
        if (width == 0 || height == 0) {
            fillRect(x, y, width + 1, height + 1);
            return;
        }
        double centerX = x + translateX + width / 2.0, centerY = y + translateY + height / 2.0;
        double outerX = width / 2.0 + .5, outerY = height / 2.0 + .5;
        double innerX = Math.max(0, width / 2.0 - .5), innerY = Math.max(0, height / 2.0 - .5);
        int left = Math.max(clipX, x + translateX - 1), top = Math.max(clipY, y + translateY - 1);
        int right = Math.min(clipRight, x + translateX + width + 1);
        int bottom = Math.min(clipBottom, y + translateY + height + 1);
        int argb = color.getRGB();
        for (int row = top; row < bottom; row++) {
            double nearY = Math.max(0, Math.abs(row + .5 - centerY) - .5) / outerY;
            if (nearY > 1) continue;
            double extent = outerX * Math.sqrt(1 - nearY * nearY);
            int rowLeft = Math.max(left, (int) Math.floor(centerX - extent));
            int rowRight = Math.min(right, (int) Math.ceil(centerX + extent));
            for (int column = rowLeft; column < rowRight; column++) {
                double farX = Math.abs(column + .5 - centerX) + .5;
                double farY = Math.abs(row + .5 - centerY) + .5;
                if (innerX > 0 && innerY > 0
                        && farX * farX / (innerX * innerX) + farY * farY / (innerY * innerY) < 1) continue;
                int coverage = 0;
                for (int sy = 0; sy < 4; sy++) for (int sx = 0; sx < 4; sx++) {
                    double dx = column + (sx + .5) / 4 - centerX;
                    double dy = row + (sy + .5) / 4 - centerY;
                    if (dx * dx / (outerX * outerX) + dy * dy / (outerY * outerY) <= 1
                            && (innerX == 0 || innerY == 0
                            || dx * dx / (innerX * innerX) + dy * dy / (innerY * innerY) >= 1)) coverage++;
                }
                if (coverage > 0) pixel(column, row, argb & 0xffffff | ((argb >>> 24) * coverage + 8) / 16 << 24);
            }
        }
    }

    public void drawString(String text, int x, int y) {
        int originX = x + translateX, originY = y + translateY;
        BrowserDisplay.rasterText(text, font.css(), font.size, color.getRGB(),
                (dx, dy, argb) -> pixel(originX + dx, originY + dy, argb));
    }

    public boolean drawImage(Image image, int x, int y, Object observer) {
        if (image == null) return false;
        return drawImage(image, x, y, image.width, image.height, observer);
    }

    public boolean drawImage(Image image, int x, int y, int width, int height, Object observer) {
        return drawImage(image, x, y, x + width, y + height, 0, 0, image.width, image.height, observer);
    }

    public boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2, Object observer) {
        if (image == null || dx2 <= dx1 || dy2 <= dy1) return false;
        int left = Math.max(clipX, dx1 + translateX), top = Math.max(clipY, dy1 + translateY);
        int right = Math.min(clipRight, dx2 + translateX), bottom = Math.min(clipBottom, dy2 + translateY);
        if (dx2 - dx1 == sx2 - sx1 && dy2 - dy1 == sy2 - sy1) {
            drawUnscaled(image, dx1 + translateX, dy1 + translateY, sx1, sy1, left, top, right, bottom);
            return true;
        }
        for (int y = top; y < bottom; y++) {
            int sourceY = sy1 + (int) ((long) (y - dy1 - translateY) * (sy2 - sy1) / (dy2 - dy1));
            if (sourceY < 0 || sourceY >= image.height) continue;
            for (int x = left; x < right; x++) {
                int sourceX = sx1 + (int) ((long) (x - dx1 - translateX) * (sx2 - sx1) / (dx2 - dx1));
                if (sourceX < 0 || sourceX >= image.width) continue;
                int value = image.pixels[sourceY * image.width + sourceX];
                pixel(x, y, image.alpha ? value : value | 0xff000000);
            }
        }
        return true;
    }

    /**
     * Copies the viewport and native-size panels without emulated long division for every pixel.
     */
    private void drawUnscaled(Image image, int destinationX, int destinationY, int sourceX, int sourceY,
                              int left, int top, int right, int bottom) {
        left = Math.max(left, destinationX - sourceX);
        top = Math.max(top, destinationY - sourceY);
        right = Math.min(right, destinationX - sourceX + image.width);
        bottom = Math.min(bottom, destinationY - sourceY + image.height);
        if (left >= right || top >= bottom) return;
        int rowWidth = right - left;
        boolean opaqueCopy = !image.alpha && !target.alpha && composite.alpha == 1
                && (composite.rule == AlphaComposite.SRC || composite.rule == AlphaComposite.SRC_OVER);
        boolean sourceCopy = image.alpha && composite.rule == AlphaComposite.SRC && composite.alpha == 1;
        for (int row = top; row < bottom; row++) {
            int sourceOffset = (sourceY + row - destinationY) * image.width + sourceX + left - destinationX;
            if (opaqueCopy || sourceCopy) {
                System.arraycopy(image.pixels, sourceOffset, target.pixels, row * target.width + left, rowWidth);
            } else {
                for (int column = left; column < right; column++) {
                    int value = image.pixels[sourceOffset++];
                    pixel(column, row, image.alpha ? value : value | 0xff000000);
                }
            }
        }
    }

    public void fill(Shape shape) {
        Rectangle bounds = shape.getBounds();
        for (int y = bounds.y; y < bounds.y + bounds.height; y++)
            for (int x = bounds.x; x < bounds.x + bounds.width; x++)
                if (shape.contains(x + .5, y + .5)) pixel(x + translateX, y + translateY, color.getRGB());
    }

    protected void pixel(int x, int y, int argb) {
        if (x < clipX || y < clipY || x >= clipRight || y >= clipBottom) return;
        int offset = y * target.width + x;
        if (composite.rule == AlphaComposite.CLEAR) { target.pixels[offset] = 0; return; }
        int alpha = Math.max(0, Math.min(255, Math.round((argb >>> 24) * composite.alpha)));
        if (composite.rule == AlphaComposite.SRC) {
            target.pixels[offset] = argb & 0xffffff | alpha << 24; return;
        }
        int previous = target.pixels[offset];
        int previousAlpha = target.alpha ? previous >>> 24 : 255;
        if (composite.rule == AlphaComposite.DST_IN) {
            target.pixels[offset] = previous & 0xffffff | previousAlpha * alpha / 255 << 24; return;
        }
        if (alpha == 0) return;
        if (alpha == 255) { target.pixels[offset] = argb; return; }
        int remainingAlpha = previousAlpha * (255 - alpha) / 255;
        int outAlpha = alpha + remainingAlpha;
        int red = (((argb >> 16) & 255) * alpha + ((previous >> 16) & 255) * remainingAlpha + outAlpha / 2) / outAlpha;
        int green = (((argb >> 8) & 255) * alpha + ((previous >> 8) & 255) * remainingAlpha + outAlpha / 2) / outAlpha;
        int blue = ((argb & 255) * alpha + (previous & 255) * remainingAlpha + outAlpha / 2) / outAlpha;
        target.pixels[offset] = outAlpha << 24 | red << 16 | green << 8 | blue;
    }
}
