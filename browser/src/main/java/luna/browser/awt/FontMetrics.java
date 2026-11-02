package luna.browser.awt;

/**
 * Measures loading text with the browser's actual font metrics.
 *
 * @author lare96
 */
public class FontMetrics {
    private final Font font;
    public FontMetrics(Font font) { this.font = font; }
    public int stringWidth(String text) { return BrowserDisplay.measureText(text, font.css()); }
}
