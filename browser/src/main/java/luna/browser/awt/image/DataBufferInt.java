package luna.browser.awt.image;

/**
 * Exposes the integer pixel storage without copying it.
 *
 * @author lare96
 */
public class DataBufferInt {
    private final int[] pixels;
    public DataBufferInt(int[] pixels) { this.pixels = pixels; }
    public int[] getData() { return pixels; }
}
