import sign.signlink;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Checks saved game-message preferences and actual tagged glyph rendering without a cache or visible window.
 *
 * @author lare96
 */
public final class GameMessageFormattingTest {

    private static final int WIDTH = 160;
    private static final int HEIGHT = 12;
    private static final int BACKGROUND = 0xffffff;
    private static final int FIRST_GLYPH = 3 + WIDTH * 4;

    public static void main(String[] args) throws Exception {
        savedPreferences();
        renderedMessages();
        System.out.println("GameMessageFormattingTest passed");
    }

    private static void savedPreferences() throws Exception {
        String previousHome = System.getProperty("user.home");
        Path fixture = Files.createTempDirectory("luna-chat-colours-").toAbsolutePath().normalize();
        try {
            System.setProperty("user.home", fixture.toString());
            check(new ClientSettings().coloredGameMessages, "new clients enable coloured game messages");
            check(ClientSettings.load().coloredGameMessages, "missing preferences retain the default");
            Path file = Path.of(signlink.findcachedir()).resolve("client.properties");

            Properties legacy = new Properties();
            legacy.setProperty("clientSize", "1");
            legacy.setProperty("width", "1111");
            legacy.setProperty("height", "777");
            store(file, legacy);
            ClientSettings loaded = ClientSettings.load();
            check(loaded.coloredGameMessages && loaded.clientSize == 1
                            && loaded.width == 1111 && loaded.height == 777,
                    "older preferences gain colours without discarding the saved window");

            loaded.coloredGameMessages = false;
            loaded.save();
            ClientSettings plain = ClientSettings.load();
            check(!plain.coloredGameMessages && plain.clientSize == 1
                            && plain.width == 1111 && plain.height == 777,
                    "plain game messages and window preferences survive a save and reload");
            plain.coloredGameMessages = true;
            plain.save();
            check(ClientSettings.load().coloredGameMessages, "colours can be saved again after disabling them");

            legacy.setProperty("coloredGameMessages", "  FALSE  ");
            store(file, legacy);
            check(!ClientSettings.load().coloredGameMessages, "a saved explicit false is case and space tolerant");
            legacy.setProperty("coloredGameMessages", "unrecognized");
            store(file, legacy);
            check(ClientSettings.load().coloredGameMessages, "an invalid preference retains the default");
        } finally {
            if (previousHome == null) System.clearProperty("user.home");
            else System.setProperty("user.home", previousHome);
            try (Stream<Path> paths = Files.walk(fixture)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toArray(Path[]::new)) Files.delete(path);
            }
        }
    }

    private static void renderedMessages() throws Exception {
        JagFont font = fixtureFont();
        int[] coloured = render(font, "@blu@a@bla@b", true);
        check(coloured[FIRST_GLYPH] == 0x0000ff && coloured[FIRST_GLYPH + 3] == 0,
                "tagged game messages apply blue and restore black in the actual pixel buffer");

        int[] plain = render(font, "@blu@a@bla@b", false);
        check(plain[FIRST_GLYPH] == 0 && plain[FIRST_GLYPH + 3] == 0,
                "disabling colours draws both visible glyphs in black");
        check(Arrays.equals(plain, render(font, "ab", false)),
                "plain mode consumes formatting codes without displaying them or adding gaps");
        check(Arrays.equals(render(font, "@red@a@str@b@end@", false), render(font, "ab", false)),
                "disabled game formatting cannot apply residual strikethrough effects");
        check(Arrays.equals(render(font, "@blu@30 GP; 1,000@bla@", false), render(font, "30 GP; 1,000", false)),
                "hiding message colours preserves the complete visible price and quantity text");
        check(Arrays.equals(render(font, "a@blu", false), render(font, "a@blu", true)),
                "an incomplete formatting token is rendered literally in either mode");

        int[] legacy = pixels();
        font.drawString("@blu@a", 0, 2, 5, false);
        check(legacy[FIRST_GLYPH] == 0x0000ff,
                "existing formatted UI rendering stays enabled independently of game-message preferences");
    }

    static JagFont fixtureFont() throws Exception {
        return new JagFont(new FontArchive(), "fixture", false);
    }

    private static int[] render(JagFont font, String text, boolean formatting) {
        int[] pixels = pixels();
        font.drawString(text, 0, 2, 5, false, formatting);
        return pixels;
    }

    private static int[] pixels() {
        int[] pixels = new int[WIDTH * HEIGHT];
        Arrays.fill(pixels, BACKGROUND);
        Drawable.putPixels(WIDTH, HEIGHT, pixels);
        return pixels;
    }

    private static void store(Path file, Properties properties) throws Exception {
        try (OutputStream out = Files.newOutputStream(file)) {
            properties.store(out, "fixture");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /**
     * Supplies a complete one-pixel font through the normal archive and font-decoding path.
     *
     * @author lare96
     */
    private static final class FontArchive extends Archive {
        private final byte[] font;
        private final byte[] index;

        private FontArchive() throws Exception {
            super(new byte[8]);
            ByteArrayOutputStream dataBytes = new ByteArrayOutputStream();
            ByteArrayOutputStream indexBytes = new ByteArrayOutputStream();
            try (DataOutputStream data = new DataOutputStream(dataBytes);
                 DataOutputStream indices = new DataOutputStream(indexBytes)) {
                data.writeShort(0);
                indices.writeInt(0);
                indices.writeByte(0);
                for (int character = 0; character < 256; character++) {
                    indices.writeByte(0);
                    indices.writeByte(0);
                    indices.writeShort(1);
                    indices.writeShort(1);
                    indices.writeByte(0);
                    data.writeByte(1);
                }
            }
            font = dataBytes.toByteArray();
            index = indexBytes.toByteArray();
        }

        @Override
        public byte[] get(String name) {
            return name.equals("index.dat") ? index : font;
        }
    }
}
