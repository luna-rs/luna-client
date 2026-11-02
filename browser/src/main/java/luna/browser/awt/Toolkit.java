package luna.browser.awt;

/**
 * Routes cache JPEG decoding to the browser's image decoder.
 *
 * @author lare96
 */
public class Toolkit {
    private static final Toolkit INSTANCE = new Toolkit();
    public static Toolkit getDefaultToolkit() { return INSTANCE; }
    public Image createImage(byte[] bytes) { return BrowserDisplay.decodeBytes(bytes); }
}
