package luna.browser.awt;

/**
 * Browser canvas has no native window insets.
 *
 * @author lare96
 */
public class Insets {
    public int left, top, right, bottom;
    public Insets(int top, int left, int bottom, int right) { this.top = top; this.left = left; this.bottom = bottom; this.right = right; }
}
