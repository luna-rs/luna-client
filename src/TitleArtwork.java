import luna.SeasonalTitleTheme;
import luna.ClientResources;
import luna.TitleScene;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Composes viewport-resolution default art and optional seasonal replacements for the 377 title.
 *
 * @author lare96
 */
public final class TitleArtwork {

    public static final int WIDTH = 765;
    public static final int HEIGHT = 503;
    public static final int LOGO_WIDTH = 444;
    public static final int LOGO_HEIGHT = 142;
    public static final int LOGO_Y = 18;

    private static final long MAX_FILE_BYTES = 16L * 1024 * 1024;
    private static final int MAX_DIMENSION = 4096;

    private final Path directory;
    private final boolean bundledResources;
    private final BufferedImage cachedBackground;
    private final RgbSprite cachedLogo;
    private final Map<SeasonalTitleTheme, BufferedImage> images = new EnumMap<>(SeasonalTitleTheme.class);
    private boolean defaultSceneLoaded;
    private TitleScene defaultScene;
    private TitleScene.Frame defaultFrame;
    private BufferedImage defaultFrameImage;
    private TitleScene.Rect previousLeftFlame;
    private TitleScene.Rect previousRightFlame;

    public TitleArtwork(Archive archive) {
        this(archive, findTitleDirectory(), true);
    }

    public TitleArtwork(Archive archive, Path directory) {
        this(archive, directory, false);
    }

    TitleArtwork(Archive archive, Path directory, boolean bundledResources) {
        this.directory = directory;
        this.bundledResources = bundledResources;
        RgbSprite half = new RgbSprite(archive.get("title.dat"), new Canvas());
        if (half.width_1490 != 383 || half.height_1491 != HEIGHT) {
            throw new IllegalArgumentException("Expected the cached 383x503 title background");
        }
        cachedBackground = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int sourceX = x < 382 ? x : WIDTH - 1 - x;
                cachedBackground.setRGB(x, y, half.pixels_1489[y * 383 + sourceX]);
            }
        }
        cachedLogo = new RgbSprite(archive, "logo", 0);
    }

    /**
     * Returns a complete title image, caching successful loads and fallback results for this session.
     */
    public synchronized BufferedImage imageFor(SeasonalTitleTheme theme) {
        BufferedImage existing = images.get(theme);
        if (existing != null) return existing;
        if (theme == SeasonalTitleTheme.DEFAULT && hasDefaultScene()) {
            BufferedImage result = defaultFrameFor(WIDTH, HEIGHT, 0, 0);
            images.put(theme, result);
            return result;
        }

        BufferedImage result = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();
        try {
            graphics.drawImage(cachedBackground, 0, 0, null);
            BufferedImage background = load(theme, "background.png");
            if (background != null) {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                double scale = Math.max((double) WIDTH / background.getWidth(),
                        (double) HEIGHT / background.getHeight());
                int width = (int) Math.ceil(background.getWidth() * scale);
                int height = (int) Math.ceil(background.getHeight() * scale);
                graphics.drawImage(background, (WIDTH - width) / 2, (HEIGHT - height) / 2,
                        width, height, null);
            }
            BufferedImage logo = load(theme, "logo.png");
            if (logo != null) logo = cropTransparentPadding(logo);
            if (logo == null) {
                drawCachedLogo(result);
            } else {
                double scale = Math.min((double) LOGO_WIDTH / logo.getWidth(),
                        (double) LOGO_HEIGHT / logo.getHeight());
                int width = Math.max(1, (int) Math.round(logo.getWidth() * scale));
                int height = Math.max(1, (int) Math.round(logo.getHeight() * scale));
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.drawImage(logo, (WIDTH - 1) / 2 - width / 2, LOGO_Y,
                        width, height, null);
            }
        } finally {
            graphics.dispose();
        }
        images.put(theme, result);
        return result;
    }

    /**
     * Enables the full-resolution scene only when both independently validated default assets load.
     */
    public synchronized boolean hasDefaultScene() {
        if (!defaultSceneLoaded) {
            defaultSceneLoaded = true;
            BufferedImage background = load(SeasonalTitleTheme.DEFAULT, "background.png");
            BufferedImage logo = load(SeasonalTitleTheme.DEFAULT, "logo.png");
            if (logo != null) logo = cropTransparentPadding(logo);
            if (background != null && logo != null) {
                defaultScene = new TitleScene(background.getRGB(0, 0, background.getWidth(),
                        background.getHeight(), null, 0, background.getWidth()),
                        background.getWidth(), background.getHeight(),
                        logo.getRGB(0, 0, logo.getWidth(), logo.getHeight(), null, 0, logo.getWidth()),
                        logo.getWidth(), logo.getHeight());
            }
        }
        return defaultScene != null;
    }

    /**
     * Draws an opaque scene using physical framebuffer coordinates, leaving login controls to the caller.
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
     * Restores the torch areas from the HD scene before blending the current transparent flame strips.
     */
    public synchronized void drawDefaultFlames(Graphics2D graphics, BufferedImage left, BufferedImage right,
                                                int width, int height, int uiOffsetX, int uiOffsetY) {
        if (defaultFrameFor(width, height, uiOffsetX, uiOffsetY) == null) return;
        if (previousLeftFlame != defaultFrame.leftFlame) restoreFlame(graphics, previousLeftFlame);
        if (previousRightFlame != defaultFrame.rightFlame) restoreFlame(graphics, previousRightFlame);
        restoreFlame(graphics, defaultFrame.leftFlame);
        restoreFlame(graphics, defaultFrame.rightFlame);
        Graphics2D overlay = (Graphics2D) graphics.create();
        try {
            overlay.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            drawFlame(overlay, left, defaultFrame.leftFlame);
            drawFlame(overlay, right, defaultFrame.rightFlame);
        } finally {
            overlay.dispose();
        }
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
            defaultFrameImage.setRGB(0, 0, width, height, frame.pixels, 0, width);
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

    private static void drawFlame(Graphics2D graphics, BufferedImage flame, TitleScene.Rect bounds) {
        if (flame != null) graphics.drawImage(flame, bounds.x, bounds.y, bounds.width, bounds.height, null);
    }

    /**
     * Uses exactly the runtime composition for headless previews and asset verification.
     */
    public static BufferedImage compose(Archive archive, Path titleDirectory, SeasonalTitleTheme theme) {
        return new TitleArtwork(archive, titleDirectory).imageFor(theme);
    }

    /**
     * Uses the runtime asset search when a caller does not supply an explicit title directory.
     */
    public static BufferedImage compose(Archive archive, SeasonalTitleTheme theme) {
        return new TitleArtwork(archive).imageFor(theme);
    }

    private void drawCachedLogo(BufferedImage target) {
        int originX = (WIDTH - 1) / 2 - cachedLogo.width_1490 / 2 + cachedLogo.attrib_492;
        int originY = LOGO_Y + cachedLogo.attrib_1493;
        for (int y = 0; y < cachedLogo.height_1491; y++) {
            for (int x = 0; x < cachedLogo.width_1490; x++) {
                int pixel = cachedLogo.pixels_1489[y * cachedLogo.width_1490 + x];
                int targetX = originX + x;
                int targetY = originY + y;
                if (pixel != 0 && targetX >= 0 && targetX < WIDTH && targetY >= 0 && targetY < 171) {
                    target.setRGB(targetX, targetY, pixel);
                }
            }
        }
    }

    private BufferedImage load(SeasonalTitleTheme theme, String file) {
        Path path = directory.resolve(theme.directory()).resolve(file);
        try {
            InputStream source;
            if (Files.isRegularFile(path)) {
                if (Files.size(path) > MAX_FILE_BYTES) return null;
                source = Files.newInputStream(path);
            } else if (bundledResources) {
                try (InputStream resource = ClientResources.openBundled("data/title/" + theme.directory() + "/" + file)) {
                    byte[] bytes = resource.readNBytes((int) MAX_FILE_BYTES + 1);
                    if (bytes.length > MAX_FILE_BYTES) return null;
                    source = new ByteArrayInputStream(bytes);
                }
            } else {
                return null;
            }
            try (InputStream stream = source;
                 ImageInputStream imageStream = new MemoryCacheImageInputStream(stream)) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
                if (!readers.hasNext()) return null;
                ImageReader reader = readers.next();
                try {
                    if (!"png".equalsIgnoreCase(reader.getFormatName())) return null;
                    reader.setInput(imageStream, true, true);
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    if (width < 1 || height < 1 || width > MAX_DIMENSION || height > MAX_DIMENSION) return null;
                    return reader.read(0);
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private static BufferedImage cropTransparentPadding(BufferedImage image) {
        int left = image.getWidth();
        int top = image.getHeight();
        int right = -1;
        int bottom = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    left = Math.min(left, x);
                    top = Math.min(top, y);
                    right = Math.max(right, x);
                    bottom = Math.max(bottom, y);
                }
            }
        }
        return right < left ? null : image.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }

    private static Path findTitleDirectory() {
        Path workingDirectory = Paths.get("data", "title").toAbsolutePath().normalize();
        if (Files.isDirectory(workingDirectory)) return workingDirectory;
        try {
            Path location = Paths.get(TitleArtwork.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!Files.isDirectory(location)) location = location.getParent();
            for (int level = 0; location != null && level < 4; level++, location = location.getParent()) {
                Path candidate = location.resolve("data").resolve("title");
                if (Files.isDirectory(candidate)) return candidate;
            }
        } catch (URISyntaxException | RuntimeException ignored) {
        }
        return workingDirectory;
    }
}
