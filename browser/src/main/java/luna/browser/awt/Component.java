package luna.browser.awt;

/**
 * Canvas-sized component surface used by shared sprite constructors.
 *
 * @author lare96
 */
public class Component {
    protected int componentWidth = 765, componentHeight = 503;
    public int getWidth() { return componentWidth; }
    public int getHeight() { return componentHeight; }
    public FontMetrics getFontMetrics(Font font) { return new FontMetrics(font); }
    public Insets getInsets() { return new Insets(0, 0, 0, 0); }
    public void setSize(int width, int height) { componentWidth = width; componentHeight = height; }
    public void requestFocus() { BrowserDisplay.focus(); }
}
