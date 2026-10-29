import luna.SeasonalTitleTheme;

import javax.imageio.ImageIO;
import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Verifies exact legacy fallback pixels, optional asset validation, composition and session caching.
 *
 * @author lare96
 */
public final class TitleArtworkTest {

    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("luna-title-art-test-");
        try {
            FixtureArchive archive = archive();
            legacyIdentity(archive, directory);
            seasonalComposition(archive, directory);
            independentFallbacks(archive, directory);
            boundedLoading(archive, directory);
            sessionCaching(archive, directory);
            sharedCalendar();
        } finally {
            try (Stream<Path> paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toArray(Path[]::new)) Files.delete(path);
            }
        }
        System.out.println("TitleArtworkTest passed");
    }

    private static void legacyIdentity(Archive archive, Path directory) {
        BufferedImage composed = TitleArtwork.compose(archive, directory, SeasonalTitleTheme.DEFAULT);
        int[] expected = new int[TitleArtwork.WIDTH * TitleArtwork.HEIGHT];
        Drawable.putPixels(TitleArtwork.WIDTH, TitleArtwork.HEIGHT, expected);
        RgbSprite half = new RgbSprite(archive.get("title.dat"), new Canvas());
        half.method459(0, 0);
        for (int y = 0; y < half.height_1491; y++) {
            for (int x = 0; x < half.width_1490 / 2; x++) {
                int first = y * half.width_1490 + x;
                int second = y * half.width_1490 + half.width_1490 - 1 - x;
                int saved = half.pixels_1489[first];
                half.pixels_1489[first] = half.pixels_1489[second];
                half.pixels_1489[second] = saved;
            }
        }
        half.method459(0, 382);
        RgbSprite logo = new RgbSprite(archive, "logo", 0);
        logo.method461(18, 382 - logo.width_1490 / 2, -488);
        int[] actual = pixels(composed);
        for (int index = 0; index < expected.length; index++) {
            check((expected[index] & 0xffffff) == (actual[index] & 0xffffff),
                    "ordinary title must retain the legacy pixel at " + index);
        }
        check(Arrays.equals(actual, pixels(TitleArtwork.compose(archive, directory, SeasonalTitleTheme.EASTER))),
                "missing seasonal assets retain the complete cached title");
    }

    private static void seasonalComposition(Archive archive, Path directory) throws Exception {
        Path halloween = Files.createDirectories(directory.resolve("halloween"));
        ImageIO.write(solid(1530, 1006, 0xff0000ff), "png", halloween.resolve("background.png").toFile());
        BufferedImage paddedLogo = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        for (int y = 260; y < 340; y++) {
            for (int x = 100; x < 500; x++) paddedLogo.setRGB(x, y, 0x80ff0000);
        }
        ImageIO.write(paddedLogo, "png", halloween.resolve("logo.png").toFile());
        BufferedImage image = TitleArtwork.compose(archive, directory, SeasonalTitleTheme.HALLOWEEN);
        check(image.getWidth() == 765 && image.getHeight() == 503, "background matches the 377 canvas");
        check(rgb(image, 400, 450) == 0x0000ff, "larger backgrounds fill the full canvas");
        check(rgb(image, 159, 18) == 0x0000ff && rgb(image, 604, 18) == 0x0000ff,
                "transparent padding is removed before fitting the original logo width");
        check(rgb(image, 160, 18) == 0x80007f, "partial alpha is composited against the background");
        check(rgb(image, 382, 106) == 0x80007f && rgb(image, 382, 107) == 0x0000ff,
                "logo aspect ratio is preserved instead of stretching to 444x142");
        check(rgb(image, 382, 171) == 0x0000ff, "seasonal logo remains clear of the login panel");
    }

    private static void independentFallbacks(Archive archive, Path directory) throws Exception {
        Path christmas = Files.createDirectories(directory.resolve("christmas"));
        ImageIO.write(solid(765, 503, 0xff123456), "png", christmas.resolve("background.png").toFile());
        Files.write(christmas.resolve("logo.png"), new byte[]{1, 2, 3});
        BufferedImage image = TitleArtwork.compose(archive, directory, SeasonalTitleTheme.CHRISTMAS);
        check(rgb(image, 400, 450) == 0x123456, "bad logo retains a valid seasonal background");
        check(rgb(image, 382, 70) == 0xffcc00, "bad logo falls back to cached palette pixels");
        ImageIO.write(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB), "png",
                christmas.resolve("logo.png").toFile());
        image = TitleArtwork.compose(archive, directory, SeasonalTitleTheme.CHRISTMAS);
        check(rgb(image, 382, 70) == 0xffcc00, "fully transparent logos fall back to the cached logo");
        Files.write(christmas.resolve("background.png"), new byte[]{1, 2, 3});
        ImageIO.write(solid(444, 142, 0xff00ff00), "png", christmas.resolve("logo.png").toFile());
        image = TitleArtwork.compose(archive, directory, SeasonalTitleTheme.CHRISTMAS);
        BufferedImage ordinary = TitleArtwork.compose(archive, directory, SeasonalTitleTheme.DEFAULT);
        check(rgb(image, 400, 450) == rgb(ordinary, 400, 450), "bad background retains the cached background");
        check(rgb(image, 382, 70) == 0x00ff00, "valid seasonal logo can use the cached background");
    }

    private static void boundedLoading(Archive archive, Path directory) throws Exception {
        Path easter = Files.createDirectories(directory.resolve("easter"));
        ImageIO.write(solid(5000, 1, 0xffff0000), "png", easter.resolve("background.png").toFile());
        BufferedImage ordinary = TitleArtwork.compose(archive, directory, SeasonalTitleTheme.DEFAULT);
        BufferedImage image = TitleArtwork.compose(archive, directory, SeasonalTitleTheme.EASTER);
        check(rgb(image, 400, 450) == rgb(ordinary, 400, 450),
                "oversized PNG dimensions are rejected before pixel decoding");
        ImageIO.write(solid(20, 20, 0xffff0000), "jpeg", easter.resolve("background.png").toFile());
        image = TitleArtwork.compose(archive, directory, SeasonalTitleTheme.EASTER);
        check(rgb(image, 400, 450) == rgb(ordinary, 400, 450), "non-PNG data is rejected despite its filename");
    }

    private static void sessionCaching(Archive archive, Path directory) throws Exception {
        TitleArtwork artwork = new TitleArtwork(archive, directory);
        BufferedImage before = artwork.imageFor(SeasonalTitleTheme.HALLOWEEN);
        Path halloween = directory.resolve("halloween");
        Files.delete(halloween.resolve("background.png"));
        Files.delete(halloween.resolve("logo.png"));
        check(artwork.imageFor(SeasonalTitleTheme.HALLOWEEN) == before,
                "same-theme frames and recreated title panels reuse the composed image");
        BufferedImage missing = artwork.imageFor(SeasonalTitleTheme.EASTER);
        ImageIO.write(solid(765, 503, 0xffabcdef), "png", directory.resolve("easter/background.png").toFile());
        check(artwork.imageFor(SeasonalTitleTheme.EASTER) == missing,
                "missing or invalid assets are cached too, avoiding repeated file reads");
        check(artwork.imageFor(SeasonalTitleTheme.DEFAULT) != before,
                "leaving a seasonal month restores a separately cached ordinary title");
    }

    private static void sharedCalendar() {
        check(SeasonalTitleTheme.forDate(LocalDate.of(2027, 3, 1)) == SeasonalTitleTheme.EASTER,
                "artwork follows March Easter years");
        check(SeasonalTitleTheme.forDate(LocalDate.of(2026, 4, 30)) == SeasonalTitleTheme.EASTER,
                "artwork follows April Easter years for the complete month");
        check(SeasonalTitleTheme.forDate(LocalDate.of(2026, 10, 31)) == SeasonalTitleTheme.HALLOWEEN,
                "Halloween includes October 31");
        check(SeasonalTitleTheme.forDate(LocalDate.of(2026, 11, 1)) == SeasonalTitleTheme.DEFAULT,
                "November restores the ordinary title");
        check(SeasonalTitleTheme.forDate(LocalDate.of(2026, 12, 31)) == SeasonalTitleTheme.CHRISTMAS,
                "Christmas includes December 31");
        check(SeasonalTitleTheme.forDate(LocalDate.of(2027, 1, 1)) == SeasonalTitleTheme.DEFAULT,
                "January restores the ordinary title");
    }

    private static BufferedImage solid(int width, int height, int color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new java.awt.Color(color));
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        return image;
    }

    private static int[] pixels(BufferedImage image) {
        return image.getRGB(0, 0, TitleArtwork.WIDTH, TitleArtwork.HEIGHT, null, 0, TitleArtwork.WIDTH);
    }

    private static int rgb(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) & 0xffffff;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static FixtureArchive archive() throws Exception {
        BufferedImage half = new BufferedImage(383, 503, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 503; y++) {
            for (int x = 0; x < 383; x++) half.setRGB(x, y, (x * 7919 + y * 997) & 0xffffff);
        }
        ByteArrayOutputStream titleBytes = new ByteArrayOutputStream();
        ImageIO.write(half, "jpeg", titleBytes);
        ByteArrayOutputStream indexBytes = new ByteArrayOutputStream();
        DataOutputStream index = new DataOutputStream(indexBytes);
        index.writeShort(444);
        index.writeShort(142);
        index.writeByte(2);
        index.write(new byte[]{(byte) 0xff, (byte) 0xcc, 0});
        index.writeByte(0);
        index.writeByte(0);
        index.writeShort(444);
        index.writeShort(142);
        index.writeByte(0);
        byte[] logo = new byte[2 + 444 * 142];
        for (int y = 20; y < 120; y++) {
            for (int x = 30; x < 414; x++) logo[2 + y * 444 + x] = 1;
        }
        Map<String, byte[]> files = new HashMap<>();
        files.put("title.dat", titleBytes.toByteArray());
        files.put("logo.dat", logo);
        files.put("index.dat", indexBytes.toByteArray());
        return new FixtureArchive(files);
    }

    /**
     * Supplies real title and indexed-logo bytes without depending on a local cache installation.
     *
     * @author lare96
     */
    private static final class FixtureArchive extends Archive {
        private final Map<String, byte[]> files;

        FixtureArchive(Map<String, byte[]> files) {
            super(new byte[8]);
            this.files = files;
        }

        @Override
        public byte[] get(String name) {
            return files.get(name);
        }
    }
}
