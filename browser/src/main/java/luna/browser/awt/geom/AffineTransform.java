package luna.browser.awt.geom;

/**
 * Translation transform used by centered login screens.
 *
 * @author lare96
 */
public class AffineTransform {
    public double x, y;
    public static AffineTransform getTranslateInstance(double x, double y) {
        AffineTransform result = new AffineTransform(); result.x = x; result.y = y; return result;
    }
}
