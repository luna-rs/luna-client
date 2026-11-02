package luna.browser.awt;

/**
 * Browser font description for loading and error messages.
 *
 * @author lare96
 */
public class Font {
    public final String name;
    public final int style;
    public final int size;
    public Font(String name, int style, int size) { this.name = name; this.style = style; this.size = size; }
    public String css() { return (style == 1 ? "bold " : "") + size + "px " + name; }
}
