package luna;

/**
 * Composes the default title at viewport resolution without depending on a desktop graphics API.
 *
 * @author lare96
 */
public final class TitleScene {

    private final int[] background;
    private final int backgroundWidth;
    private final int backgroundHeight;
    private final int[] logo;
    private final int logoWidth;
    private final int logoHeight;
    private Frame cached;

    public TitleScene(int[] background, int backgroundWidth, int backgroundHeight,
                      int[] logo, int logoWidth, int logoHeight) {
        validate(background, backgroundWidth, backgroundHeight);
        validate(logo, logoWidth, logoHeight);
        this.background = background;
        this.backgroundWidth = backgroundWidth;
        this.backgroundHeight = backgroundHeight;
        this.logo = logo;
        this.logoWidth = logoWidth;
        this.logoHeight = logoHeight;
    }

    /**
     * Reuses the opaque composition until the viewport or centered login controls move.
     */
    public Frame frameFor(int width, int height, int uiOffsetX, int uiOffsetY) {
        if (cached != null && cached.width == width && cached.height == height
                && cached.uiOffsetX == uiOffsetX && cached.uiOffsetY == uiOffsetY) return cached;
        if (width < 1 || height < 1 || width > Integer.MAX_VALUE / height)
            throw new IllegalArgumentException("Invalid title viewport");

        double scale = Math.max((double) width / backgroundWidth, (double) height / backgroundHeight);
        int renderedWidth = (int) Math.ceil(backgroundWidth * scale);
        int renderedHeight = (int) Math.ceil(backgroundHeight * scale);
        int backgroundX = (width - renderedWidth) / 2;
        int backgroundY = (height - renderedHeight) / 2;
        int[] pixels = new int[width * height];
        paint(pixels, width, height, background, backgroundWidth, backgroundHeight,
                backgroundX, backgroundY, renderedWidth, renderedHeight, false);

        double logoScale = Math.min(444.0 / logoWidth, 142.0 / logoHeight);
        int drawnLogoWidth = Math.max(1, (int) Math.round(logoWidth * logoScale));
        int drawnLogoHeight = Math.max(1, (int) Math.round(logoHeight * logoScale));
        paint(pixels, width, height, logo, logoWidth, logoHeight,
                uiOffsetX + 382 - drawnLogoWidth / 2, uiOffsetY + 18,
                drawnLogoWidth, drawnLogoHeight, true);

        double flameScale = renderedHeight / 503.0;
        int flameWidth = Math.max(1, (int) Math.round(128 * flameScale));
        int flameHeight = Math.max(1, (int) Math.round(265 * flameScale));
        int flameY = (int) Math.round(backgroundY + renderedHeight * 0.545 - flameHeight);
        Rect leftFlame = new Rect((int) Math.round(backgroundX + renderedWidth * 0.114 - flameWidth / 2.0),
                flameY, flameWidth, flameHeight);
        Rect rightFlame = new Rect((int) Math.round(backgroundX + renderedWidth * 0.886 - flameWidth / 2.0),
                flameY, flameWidth, flameHeight);
        cached = new Frame(width, height, uiOffsetX, uiOffsetY, pixels, leftFlame, rightFlame);
        return cached;
    }

    private static void validate(int[] pixels, int width, int height) {
        if (width < 1 || height < 1 || width > 4096 || height > 4096
                || pixels == null || pixels.length != width * height)
            throw new IllegalArgumentException("Invalid title image");
    }

    /**
     * Uses premultiplied-alpha bilinear samples to keep transparent logo edges clean in both clients.
     */
    private static void paint(int[] target, int targetWidth, int targetHeight,
                              int[] source, int sourceWidth, int sourceHeight,
                              int x, int y, int width, int height, boolean overlay) {
        int left = Math.max(0, x), top = Math.max(0, y);
        int right = Math.min(targetWidth, x + width), bottom = Math.min(targetHeight, y + height);
        for (int row = top; row < bottom; row++) {
            double sourceY = Math.max(0, Math.min(sourceHeight - 1,
                    (row - y + 0.5) * sourceHeight / height - 0.5));
            int y0 = (int) sourceY, y1 = Math.min(sourceHeight - 1, y0 + 1);
            double vertical = sourceY - y0;
            for (int column = left; column < right; column++) {
                double sourceX = Math.max(0, Math.min(sourceWidth - 1,
                        (column - x + 0.5) * sourceWidth / width - 0.5));
                int x0 = (int) sourceX, x1 = Math.min(sourceWidth - 1, x0 + 1);
                int color = sample(source[y0 * sourceWidth + x0], source[y0 * sourceWidth + x1],
                        source[y1 * sourceWidth + x0], source[y1 * sourceWidth + x1], sourceX - x0, vertical);
                int index = row * targetWidth + column;
                target[index] = overOpaque(color, overlay ? target[index] : 0);
            }
        }
    }

    private static int sample(int topLeft, int topRight, int bottomLeft, int bottomRight,
                              double horizontal, double vertical) {
        double a = (1 - horizontal) * (1 - vertical), b = horizontal * (1 - vertical);
        double c = (1 - horizontal) * vertical, d = horizontal * vertical;
        double aa = (topLeft >>> 24) * a, ab = (topRight >>> 24) * b;
        double ac = (bottomLeft >>> 24) * c, ad = (bottomRight >>> 24) * d;
        double alpha = aa + ab + ac + ad;
        if (alpha < 0.5) return 0;
        int red = (int) (((topLeft >> 16 & 255) * aa + (topRight >> 16 & 255) * ab
                + (bottomLeft >> 16 & 255) * ac + (bottomRight >> 16 & 255) * ad) / alpha + 0.5);
        int green = (int) (((topLeft >> 8 & 255) * aa + (topRight >> 8 & 255) * ab
                + (bottomLeft >> 8 & 255) * ac + (bottomRight >> 8 & 255) * ad) / alpha + 0.5);
        int blue = (int) (((topLeft & 255) * aa + (topRight & 255) * ab
                + (bottomLeft & 255) * ac + (bottomRight & 255) * ad) / alpha + 0.5);
        return (int) (alpha + 0.5) << 24 | red << 16 | green << 8 | blue;
    }

    private static int overOpaque(int source, int destination) {
        int alpha = source >>> 24;
        if (alpha == 255) return source;
        int inverse = 255 - alpha;
        int red = ((source >> 16 & 255) * alpha + (destination >> 16 & 255) * inverse + 127) / 255;
        int green = ((source >> 8 & 255) * alpha + (destination >> 8 & 255) * inverse + 127) / 255;
        int blue = ((source & 255) * alpha + (destination & 255) * inverse + 127) / 255;
        return 0xff000000 | red << 16 | green << 8 | blue;
    }

    /**
     * Holds an opaque viewport and its torch positions in physical framebuffer pixels.
     *
     * @author lare96
     */
    public static final class Frame {
        public final int width;
        public final int height;
        public final int uiOffsetX;
        public final int uiOffsetY;
        public final int[] pixels;
        public final Rect leftFlame;
        public final Rect rightFlame;

        private Frame(int width, int height, int uiOffsetX, int uiOffsetY,
                      int[] pixels, Rect leftFlame, Rect rightFlame) {
            this.width = width;
            this.height = height;
            this.uiOffsetX = uiOffsetX;
            this.uiOffsetY = uiOffsetY;
            this.pixels = pixels;
            this.leftFlame = leftFlame;
            this.rightFlame = rightFlame;
        }
    }

    /**
     * Describes a flame overlay before clipping it to the current viewport.
     *
     * @author lare96
     */
    public static final class Rect {
        public final int x;
        public final int y;
        public final int width;
        public final int height;

        private Rect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
