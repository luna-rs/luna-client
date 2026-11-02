package luna.browser.awt;

/**
 * Alpha rules used by the existing panel compositor.
 *
 * @author lare96
 */
public class AlphaComposite {
    public static final int CLEAR = 1, SRC = 2, SRC_OVER = 3, DST_IN = 6;
    public static final AlphaComposite Clear = new AlphaComposite(CLEAR, 1), Src = new AlphaComposite(SRC, 1),
            SrcOver = new AlphaComposite(SRC_OVER, 1), DstIn = new AlphaComposite(DST_IN, 1);
    public final int rule;
    public final float alpha;
    private AlphaComposite(int rule, float alpha) { this.rule = rule; this.alpha = alpha; }
    public static AlphaComposite getInstance(int rule, float alpha) { return new AlphaComposite(rule, alpha); }
}
