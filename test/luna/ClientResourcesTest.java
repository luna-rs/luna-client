package luna;

import javax.imageio.ImageIO;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Verifies packaged startup resources without a working-directory resource tree or audio device.
 *
 * @author lare96
 */
public final class ClientResourcesTest {

    public static void main(String[] args) throws Exception {
        RsaParser.parse();
        check(RsaParser.getModulus().signum() > 0 && RsaParser.getExponent().signum() > 0,
                "the public login key loads from packaged resources");
        try (InputStream stream = ClientResources.openBundled("data/soundbank.sf2")) {
            Soundbank soundbank = MidiSystem.getSoundbank(stream);
            check(soundbank != null && soundbank.getInstruments().length > 0,
                    "the bundled soundbank parses without external files or audio devices");
        }
        for (SeasonalTitleTheme theme : new SeasonalTitleTheme[] {
                SeasonalTitleTheme.EASTER, SeasonalTitleTheme.HALLOWEEN, SeasonalTitleTheme.CHRISTMAS}) {
            for (String image : new String[] {"background.png", "logo.png"}) {
                try (InputStream stream = ClientResources.openBundled("data/title/" + theme.directory() + "/" + image)) {
                    BufferedImage decoded = ImageIO.read(stream);
                    check(decoded != null && decoded.getWidth() > 0 && decoded.getHeight() > 0,
                            "every seasonal image decodes from the classpath");
                }
            }
        }
        try {
            ClientResources.open("missing-luna-resource.bin");
            throw new AssertionError("missing required resources must fail explicitly");
        } catch (IOException exception) {
            check(exception.getMessage().contains("missing-luna-resource.bin"),
                    "resource failure identifies the missing packaged asset");
        }
        System.out.println("ClientResourcesTest passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
