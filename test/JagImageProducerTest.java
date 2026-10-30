import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageConsumer;
import java.util.Arrays;
import java.util.Hashtable;

/**
 * Verifies direct software image presentation against opaque RGB reference images.
 *
 * @author lare96
 */
public final class JagImageProducerTest {

    public static void main(String[] args) {
        nativeDrawing();
        changingPixels();
        replacementPixels();
        consumerCompatibility();
        System.out.println("JagImageProducerTest passed");
    }

    private static void nativeDrawing() {
        for (int[] size : new int[][] {{37, 23}, {320, 180}}) {
            JagImageProducer producer = producer(size[0], size[1]);
            for (float alpha : new float[] {0F, 0.25F, 0.5F, 1F}) {
                for (boolean cropped : new boolean[] {false, true}) {
                    assertDrawing(producer, alpha, cropped);
                }
            }
        }
    }

    private static void changingPixels() {
        JagImageProducer producer = producer(37, 23);
        check(producer.image instanceof BufferedImage, "native presentation uses a buffered RGB image");
        check(producer.pixels == ((DataBufferInt) ((BufferedImage) producer.image)
                        .getRaster().getDataBuffer()).getData(),
                "the software rasterizer and presentation share the same pixel storage");
        for (int frame = 0; frame < 5; frame++) {
            fill(producer.pixels, frame);
            assertDrawing(producer, 1F, false);
            assertDrawing(producer, 0.5F, true);
        }
    }

    private static void replacementPixels() {
        JagImageProducer producer = producer(37, 23);
        producer.pixels = new int[producer.width * producer.height];
        for (int frame = 0; frame < 3; frame++) {
            fill(producer.pixels, frame + 10);
            producer.pushPixels();
            check(Drawable.pixels == producer.pixels, "replacement pixels remain the active raster target");
            assertDrawing(producer, 1F, false);
            assertDrawing(producer, 0.5F, true);
        }
    }

    private static void consumerCompatibility() {
        JagImageProducer producer = producer(37, 23);
        RecordingConsumer consumer = new RecordingConsumer();
        producer.startProduction(consumer);
        check(producer.isConsumer(consumer) && consumer.width == producer.width
                        && consumer.height == producer.height && consumer.model == producer.colorModel,
                "legacy image consumers receive unchanged dimensions and color metadata");
        producer.flipBuffer();
        check(consumer.completions == 1 && Arrays.equals(consumer.pixels, rgb(producer.pixels)),
                "legacy consumers receive the complete current RGB frame");
        producer.pixels = producer.pixels.clone();
        fill(producer.pixels, 12);
        producer.flipBuffer();
        check(consumer.completions == 2 && Arrays.equals(consumer.pixels, rgb(producer.pixels)),
                "legacy consumers still receive replacement pixel arrays");
        producer.removeConsumer(consumer);
        producer.flipBuffer();
        check(!producer.isConsumer(consumer) && consumer.completions == 2,
                "removing a legacy consumer stops further updates");
    }

    private static JagImageProducer producer(int width, int height) {
        JagImageProducer producer = new JagImageProducer(width, height, new Canvas());
        fill(producer.pixels, 0);
        return producer;
    }

    private static void fill(int[] pixels, int frame) {
        int[] special = {0, 0xffffff, 0x7f123456, 0xff2468ac, 0x00123456, 0x80000000};
        for (int index = 0; index < pixels.length; index++) {
            pixels[index] = index < special.length ? special[(index + frame) % special.length]
                    : Integer.rotateLeft(index * 0x17293b + frame * 0x314159, frame & 31);
        }
    }

    private static void assertDrawing(JagImageProducer producer, float alpha, boolean cropped) {
        BufferedImage reference = new BufferedImage(producer.width, producer.height, BufferedImage.TYPE_INT_RGB);
        reference.setRGB(0, 0, producer.width, producer.height, producer.pixels, 0, producer.width);
        BufferedImage expected = target(producer.width + 20, producer.height + 20);
        BufferedImage actual = target(expected.getWidth(), expected.getHeight());
        Graphics2D expectedGraphics = expected.createGraphics();
        Graphics2D actualGraphics = actual.createGraphics();
        int[] original = producer.pixels.clone();
        try {
            expectedGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            actualGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            if (cropped) {
                int width = producer.width - 7;
                int height = producer.height - 5;
                expectedGraphics.drawImage(reference, -2, 7, -2 + width, 7 + height,
                        4, 3, 4 + width, 3 + height, null);
                producer.drawImageRegion(-2, 7, 4, 3, width, height, actualGraphics);
            } else {
                expectedGraphics.drawImage(reference, 7, 9, null);
                producer.drawImage(7, 9, actualGraphics);
            }
        } finally {
            expectedGraphics.dispose();
            actualGraphics.dispose();
        }
        check(Arrays.equals(expected.getRGB(0, 0, expected.getWidth(), expected.getHeight(), null, 0,
                        expected.getWidth()), actual.getRGB(0, 0, actual.getWidth(), actual.getHeight(), null, 0,
                        actual.getWidth())),
                "native, cropped and alpha drawing preserves every reference pixel");
        check(Arrays.equals(original, producer.pixels), "drawing leaves the source raster unchanged");
    }

    private static BufferedImage target(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(0x193a52));
            graphics.fillRect(0, 0, width, height);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static int[] rgb(int[] pixels) {
        int[] result = pixels.clone();
        for (int index = 0; index < result.length; index++) result[index] |= 0xff000000;
        return result;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /**
     * Captures legacy ImageProducer deliveries independently of Java2D presentation.
     *
     * @author lare96
     */
    private static final class RecordingConsumer implements ImageConsumer {
        private int width;
        private int height;
        private ColorModel model;
        private int[] pixels;
        private int completions;

        @Override
        public void setDimensions(int width, int height) {
            this.width = width;
            this.height = height;
            pixels = new int[width * height];
        }

        @Override
        public void setProperties(Hashtable<?, ?> properties) {
        }

        @Override
        public void setColorModel(ColorModel model) {
            this.model = model;
        }

        @Override
        public void setHints(int hints) {
            check(hints == 14, "consumer drawing hints remain unchanged");
        }

        @Override
        public void setPixels(int x, int y, int width, int height, ColorModel model,
                              byte[] pixels, int offset, int stride) {
            throw new AssertionError("Expected integer RGB pixels");
        }

        @Override
        public void setPixels(int x, int y, int width, int height, ColorModel model,
                              int[] pixels, int offset, int stride) {
            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {
                    this.pixels[(y + row) * this.width + x + column]
                            = model.getRGB(pixels[offset + row * stride + column]);
                }
            }
        }

        @Override
        public void imageComplete(int status) {
            check(status == ImageConsumer.SINGLEFRAMEDONE, "consumer frames remain complete single frames");
            completions++;
        }
    }
}
