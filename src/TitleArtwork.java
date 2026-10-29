import luna.SeasonalTitleTheme;

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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Composes optional seasonal art over the original 377 title without changing its login controls.
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
    private final BufferedImage cachedBackground;
    private final RgbSprite cachedLogo;
    private final Map<SeasonalTitleTheme, BufferedImage> images = new EnumMap<>(SeasonalTitleTheme.class);

    public TitleArtwork(Archive archive) {
        this(archive, findTitleDirectory());
    }

    public TitleArtwork(Archive archive, Path directory) {
        this.directory = directory;
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
    public BufferedImage imageFor(SeasonalTitleTheme theme) {
        BufferedImage existing = images.get(theme);
        if (existing != null) return existing;

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
        if (theme == SeasonalTitleTheme.DEFAULT) return null;
        Path path = directory.resolve(theme.directory()).resolve(file);
        try {
            if (!Files.isRegularFile(path) || Files.size(path) > MAX_FILE_BYTES) return null;
            try (InputStream stream = Files.newInputStream(path);
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
