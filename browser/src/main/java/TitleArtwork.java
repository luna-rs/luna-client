import java.util.EnumMap;
import java.util.Map;
import luna.SeasonalTitleTheme;
import luna.browser.awt.BrowserDisplay;
import luna.browser.awt.Canvas;
import luna.browser.awt.Graphics2D;
import luna.browser.awt.image.BufferedImage;

/**
 * Composes the original cache title and seasonal PNG assets using browser image decoding.
 *
 * @author lare96
 */
public final class TitleArtwork {
    public static final int WIDTH = 765, HEIGHT = 503, LOGO_WIDTH = 444, LOGO_HEIGHT = 142, LOGO_Y = 18;
    private final BufferedImage background;
    private final RgbSprite logo;
    private final Map<SeasonalTitleTheme, BufferedImage> images = new EnumMap<>(SeasonalTitleTheme.class);

    public TitleArtwork(Archive archive) {
        RgbSprite half = new RgbSprite(archive.get("title.dat"), new Canvas());
        if (half.width_1490 != 383 || half.height_1491 != HEIGHT)
            throw new IllegalArgumentException("Expected the cached 383x503 title background");
        background = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < HEIGHT; y++) for (int x = 0; x < WIDTH; x++) {
            int sourceX = x < 382 ? x : WIDTH - 1 - x;
            background.setRGB(x, y, half.pixels_1489[y * 383 + sourceX]);
        }
        logo = new RgbSprite(archive, "logo", 0);
    }

    public BufferedImage imageFor(SeasonalTitleTheme theme) {
        BufferedImage cached = images.get(theme);
        if (cached != null) return cached;
        BufferedImage result = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();
        graphics.drawImage(background, 0, 0, null);
        BufferedImage seasonalBackground = load(theme, "background.png");
        if (seasonalBackground != null) {
            double scale = Math.max((double) WIDTH / seasonalBackground.width, (double) HEIGHT / seasonalBackground.height);
            int width = (int) Math.ceil(seasonalBackground.width * scale), height = (int) Math.ceil(seasonalBackground.height * scale);
            graphics.drawImage(seasonalBackground, (WIDTH - width) / 2, (HEIGHT - height) / 2, width, height, null);
        }
        BufferedImage seasonalLogo = load(theme, "logo.png");
        if (seasonalLogo != null) seasonalLogo = crop(seasonalLogo);
        if (seasonalLogo == null) {
            int originX = (WIDTH - 1) / 2 - logo.width_1490 / 2 + logo.attrib_492;
            int originY = LOGO_Y + logo.attrib_1493;
            for (int y = 0; y < logo.height_1491; y++) for (int x = 0; x < logo.width_1490; x++) {
                int color = logo.pixels_1489[y * logo.width_1490 + x], targetX = originX + x, targetY = originY + y;
                if (color != 0 && targetX >= 0 && targetX < WIDTH && targetY >= 0 && targetY < 171)
                    result.setRGB(targetX, targetY, color);
            }
        } else {
            double scale = Math.min((double) LOGO_WIDTH / seasonalLogo.width, (double) LOGO_HEIGHT / seasonalLogo.height);
            int width = Math.max(1, (int) Math.round(seasonalLogo.width * scale));
            int height = Math.max(1, (int) Math.round(seasonalLogo.height * scale));
            graphics.drawImage(seasonalLogo, (WIDTH - 1) / 2 - width / 2, LOGO_Y, width, height, null);
        }
        graphics.dispose();
        images.put(theme, result);
        return result;
    }

    private static BufferedImage load(SeasonalTitleTheme theme, String file) {
        if (theme == SeasonalTitleTheme.DEFAULT) return null;
        try { return BrowserDisplay.decodeUrl("assets/title/" + theme.directory() + "/" + file); }
        catch (RuntimeException failure) { System.err.println("Seasonal title fallback: " + failure); return null; }
    }

    private static BufferedImage crop(BufferedImage image) {
        int left = image.width, top = image.height, right = -1, bottom = -1;
        for (int y = 0; y < image.height; y++) for (int x = 0; x < image.width; x++) {
            if ((image.getRGB(x, y) >>> 24) != 0) {
                left = Math.min(left, x); right = Math.max(right, x);
                top = Math.min(top, y); bottom = Math.max(bottom, y);
            }
        }
        return right < left ? null : image.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }
}
