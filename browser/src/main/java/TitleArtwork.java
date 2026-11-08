import java.util.EnumMap;
import java.util.Map;
import luna.SeasonalTitleTheme;
import luna.TitleScene;
import luna.TitleFlameRenderer;
import luna.browser.awt.BrowserDisplay;
import luna.browser.awt.Canvas;
import luna.browser.awt.Graphics2D;
import luna.browser.awt.image.BufferedImage;

/**
 * Composes viewport-resolution default art and seasonal PNG assets using browser image decoding.
 *
 * @author lare96
 */
public final class TitleArtwork {
    public static final int WIDTH = 765, HEIGHT = 503, LOGO_WIDTH = 444, LOGO_HEIGHT = 142, LOGO_Y = 18;
    private final BufferedImage background;
    private final RgbSprite logo;
    private final Map<SeasonalTitleTheme, BufferedImage> images = new EnumMap<>(SeasonalTitleTheme.class);
    private boolean defaultSceneLoaded;
    private TitleScene defaultScene;
    private TitleScene.Frame defaultFrame;
    private BufferedImage defaultFrameImage;
    private TitleScene.Rect previousLeftFlame;
    private TitleScene.Rect previousRightFlame;
    private final TitleFlameRenderer leftFlameRenderer = new TitleFlameRenderer();
    private final TitleFlameRenderer rightFlameRenderer = new TitleFlameRenderer();
    private BufferedImage smoothLeftFlame;
    private BufferedImage smoothRightFlame;

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

    public synchronized BufferedImage imageFor(SeasonalTitleTheme theme) {
        BufferedImage cached = images.get(theme);
        if (cached != null) return cached;
        if (theme == SeasonalTitleTheme.DEFAULT && hasDefaultScene()) {
            BufferedImage result = defaultFrameFor(WIDTH, HEIGHT, 0, 0);
            images.put(theme, result);
            return result;
        }
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

    /**
     * Enables the full-resolution scene only when both default PNG assets are usable.
     */
    public synchronized boolean hasDefaultScene() {
        if (!defaultSceneLoaded) {
            defaultSceneLoaded = true;
            BufferedImage defaultBackground = load(SeasonalTitleTheme.DEFAULT, "background.png");
            BufferedImage defaultLogo = load(SeasonalTitleTheme.DEFAULT, "logo.png");
            if (defaultLogo != null) defaultLogo = crop(defaultLogo);
            if (defaultBackground != null && defaultLogo != null) {
                defaultScene = new TitleScene(defaultBackground.getRGB(0, 0, defaultBackground.width,
                        defaultBackground.height, null, 0, defaultBackground.width),
                        defaultBackground.width, defaultBackground.height,
                        defaultLogo.getRGB(0, 0, defaultLogo.width, defaultLogo.height,
                                null, 0, defaultLogo.width), defaultLogo.width, defaultLogo.height);
            }
        }
        return defaultScene != null;
    }

    /**
     * Draws the shared bilinear composition at physical framebuffer resolution.
     */
    public synchronized void drawDefaultScene(Graphics2D graphics, int width, int height,
                                               int uiOffsetX, int uiOffsetY) {
        BufferedImage frame = defaultFrameFor(width, height, uiOffsetX, uiOffsetY);
        if (frame == null) return;
        graphics.drawImage(frame, 0, 0, null);
        previousLeftFlame = null;
        previousRightFlame = null;
    }

    /**
     * Restores only the torch areas before drawing this frame's transparent flame strips.
     */
    public synchronized void drawDefaultFlames(Graphics2D graphics, BufferedImage left, BufferedImage right,
                                                int width, int height, int uiOffsetX, int uiOffsetY) {
        if (defaultFrameFor(width, height, uiOffsetX, uiOffsetY) == null) return;
        if (previousLeftFlame != defaultFrame.leftFlame) restoreFlame(graphics, previousLeftFlame);
        if (previousRightFlame != defaultFrame.rightFlame) restoreFlame(graphics, previousRightFlame);
        restoreFlame(graphics, defaultFrame.leftFlame);
        restoreFlame(graphics, defaultFrame.rightFlame);
        if (visible(defaultFrame.leftFlame))
            smoothLeftFlame = drawFlame(graphics, left, defaultFrame.leftFlame, leftFlameRenderer, smoothLeftFlame);
        if (visible(defaultFrame.rightFlame))
            smoothRightFlame = drawFlame(graphics, right, defaultFrame.rightFlame, rightFlameRenderer, smoothRightFlame);
        previousLeftFlame = defaultFrame.leftFlame;
        previousRightFlame = defaultFrame.rightFlame;
    }

    /**
     * Copies the matching viewport pixels behind a login panel, preserving the HD scene at its edges.
     */
    public synchronized boolean copyDefaultRegion(int[] target, int targetWidth, int targetHeight,
                                                   int x, int y, int viewportWidth, int viewportHeight,
                                                   int uiOffsetX, int uiOffsetY) {
        if (target == null || targetWidth < 1 || targetHeight < 1
                || targetWidth > Integer.MAX_VALUE / targetHeight
                || target.length < targetWidth * targetHeight || x < 0 || y < 0
                || targetWidth > viewportWidth || targetHeight > viewportHeight
                || x > viewportWidth - targetWidth || y > viewportHeight - targetHeight
                || defaultFrameFor(viewportWidth, viewportHeight, uiOffsetX, uiOffsetY) == null) return false;
        for (int row = 0; row < targetHeight; row++) {
            System.arraycopy(defaultFrame.pixels, (y + row) * viewportWidth + x,
                    target, row * targetWidth, targetWidth);
        }
        return true;
    }

    private BufferedImage defaultFrameFor(int width, int height, int uiOffsetX, int uiOffsetY) {
        if (width < 1 || height < 1 || !hasDefaultScene()) return null;
        TitleScene.Frame frame = defaultScene.frameFor(width, height, uiOffsetX, uiOffsetY);
        if (frame != defaultFrame) {
            defaultFrame = frame;
            defaultFrameImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            System.arraycopy(frame.pixels, 0, defaultFrameImage.pixels, 0, frame.pixels.length);
        }
        return defaultFrameImage;
    }

    private void restoreFlame(Graphics2D graphics, TitleScene.Rect bounds) {
        if (bounds == null) return;
        int left = Math.max(0, bounds.x), top = Math.max(0, bounds.y);
        int right = Math.min(defaultFrame.width, bounds.x + bounds.width);
        int bottom = Math.min(defaultFrame.height, bounds.y + bounds.height);
        if (left < right && top < bottom)
            graphics.drawImage(defaultFrameImage, left, top, right, bottom, left, top, right, bottom, null);
    }

    private boolean visible(TitleScene.Rect bounds) {
        return bounds.x < defaultFrame.width && bounds.y < defaultFrame.height
                && bounds.x + bounds.width > 0 && bounds.y + bounds.height > 0;
    }

    private static BufferedImage drawFlame(Graphics2D graphics, BufferedImage flame, TitleScene.Rect bounds,
                                            TitleFlameRenderer renderer, BufferedImage output) {
        if (flame == null) return output;
        int[] pixels = renderer.render(flame.pixels, bounds.width, bounds.height);
        if (output == null || output.width != bounds.width || output.height != bounds.height)
            output = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
        System.arraycopy(pixels, 0, output.pixels, 0, pixels.length);
        graphics.drawImage(output, bounds.x, bounds.y, null);
        return output;
    }

    private static BufferedImage load(SeasonalTitleTheme theme, String file) {
        try {
            BufferedImage image = BrowserDisplay.decodeUrl("assets/title/" + theme.directory() + "/" + file);
            return image != null && image.width > 0 && image.height > 0
                    && image.width <= 4096 && image.height <= 4096 ? image : null;
        }
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
