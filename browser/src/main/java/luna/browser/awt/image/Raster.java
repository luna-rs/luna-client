package luna.browser.awt.image;

/**
 * Pixel-buffer view matching the shared graphics accessor.
 *
 * @author lare96
 */
public class Raster {
    private final DataBufferInt buffer;
    public Raster(int[] pixels) { buffer = new DataBufferInt(pixels); }
    public DataBufferInt getDataBuffer() { return buffer; }
}
