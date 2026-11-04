package luna.browser.awt;

/**
 * Packed RGB color for the browser graphics adapter.
 *
 * @author lare96
 */
public class Color {
    public static final Color black = new Color(0), white = new Color(0xffffff), yellow = new Color(0xffff00);
    public static final Color BLACK = black, WHITE = white, YELLOW = yellow;
    private final int value;
    public Color(int value) { this.value = value & 0xffffff; }
    public Color(int red, int green, int blue) { this(red << 16 | green << 8 | blue); }
    public int getRGB() { return 0xff000000 | value; }
}
