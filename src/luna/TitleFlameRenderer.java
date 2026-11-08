package luna;

/**
 * Smooths the native title fire and reconstructs transparent flames at their displayed resolution.
 *
 * @author lare96
 */
public final class TitleFlameRenderer {

    private static final int SOURCE_WIDTH = 128;
    private static final int SOURCE_HEIGHT = 265;
    private final int[] premultiplied = new int[SOURCE_WIDTH * SOURCE_HEIGHT];
    private final int[] horizontal = new int[SOURCE_WIDTH * SOURCE_HEIGHT];
    private final int[] filtered = new int[SOURCE_WIDTH * SOURCE_HEIGHT];
    private int width;
    private int height;
    private int[] pixels;
    private int[] columns;
    private int[] columnWeights;
    private int[] rows;
    private int[] rowWeights;

    /**
     * Returns a reused straight-alpha framebuffer; callers draw it without another scaling pass.
     */
    public int[] render(int[] source, int targetWidth, int targetHeight) {
        if (source == null || source.length != premultiplied.length)
            throw new IllegalArgumentException("Expected the native 128-by-265 flame strip");
        if (targetWidth < 1 || targetHeight < 1 || targetWidth > Integer.MAX_VALUE / targetHeight)
            throw new IllegalArgumentException("Invalid flame viewport");
        prepare(targetWidth, targetHeight);
        for (int i = 0; i < source.length; i++) {
            int color = source[i], alpha = color >>> 24;
            premultiplied[i] = alpha << 24
                    | ((color >> 16 & 255) * alpha + 127) / 255 << 16
                    | ((color >> 8 & 255) * alpha + 127) / 255 << 8
                    | ((color & 255) * alpha + 127) / 255;
        }
        for (int y = 0; y < SOURCE_HEIGHT; y++) {
            int row = y * SOURCE_WIDTH;
            for (int x = 0; x < SOURCE_WIDTH; x++) {
                int index = row + x;
                horizontal[index] = blur(premultiplied[row + Math.max(0, x - 1)],
                        premultiplied[index], premultiplied[row + Math.min(SOURCE_WIDTH - 1, x + 1)]);
            }
        }
        for (int y = 0; y < SOURCE_HEIGHT; y++) {
            int above = Math.max(0, y - 1) * SOURCE_WIDTH;
            int row = y * SOURCE_WIDTH;
            int below = Math.min(SOURCE_HEIGHT - 1, y + 1) * SOURCE_WIDTH;
            for (int x = 0; x < SOURCE_WIDTH; x++) {
                // Retain the native transparent border and visible base so smoothing cannot move the fire.
                filtered[row + x] = x == 0 || x == SOURCE_WIDTH - 1 || y < 8 || y >= 263 ? 0
                        : blur(horizontal[above + x], horizontal[row + x], horizontal[below + x]);
            }
        }
        for (int y = 0; y < height; y++) {
            int top = rows[y] * SOURCE_WIDTH;
            int bottom = Math.min(SOURCE_HEIGHT - 1, rows[y] + 1) * SOURCE_WIDTH;
            int vertical = rowWeights[y];
            int target = y * width;
            for (int x = 0; x < width; x++) {
                int left = columns[x], right = Math.min(SOURCE_WIDTH - 1, left + 1);
                int upper = interpolate(filtered[top + left], filtered[top + right], columnWeights[x]);
                int lower = interpolate(filtered[bottom + left], filtered[bottom + right], columnWeights[x]);
                int color = interpolate(upper, lower, vertical), alpha = color >>> 24;
                pixels[target + x] = alpha == 0 ? 0 : alpha << 24
                        | (color >> 16 & 255) * 255 / alpha << 16
                        | (color >> 8 & 255) * 255 / alpha << 8
                        | (color & 255) * 255 / alpha;
            }
        }
        return pixels;
    }

    private void prepare(int targetWidth, int targetHeight) {
        if (width == targetWidth && height == targetHeight) return;
        width = targetWidth;
        height = targetHeight;
        pixels = new int[width * height];
        columns = new int[width];
        columnWeights = new int[width];
        rows = new int[height];
        rowWeights = new int[height];
        samples(SOURCE_WIDTH, columns, columnWeights);
        samples(SOURCE_HEIGHT, rows, rowWeights);
    }

    private static void samples(int sourceSize, int[] indices, int[] weights) {
        for (int i = 0; i < indices.length; i++) {
            double position = Math.max(0, Math.min(sourceSize - 1,
                    (i + 0.5) * sourceSize / indices.length - 0.5));
            indices[i] = (int) position;
            weights[i] = (int) Math.round((position - indices[i]) * 256);
        }
    }

    private static int blur(int first, int center, int last) {
        int redBlue = ((first & 0xff00ff) + ((center & 0xff00ff) << 1)
                + (last & 0xff00ff) + 0x20002) >>> 2 & 0xff00ff;
        int alphaGreen = ((first >>> 8 & 0xff00ff) + ((center >>> 8 & 0xff00ff) << 1)
                + (last >>> 8 & 0xff00ff) + 0x20002) >>> 2 & 0xff00ff;
        return alphaGreen << 8 | redBlue;
    }

    private static int interpolate(int first, int last, int weight) {
        int inverse = 256 - weight;
        int redBlue = ((first & 0xff00ff) * inverse + (last & 0xff00ff) * weight
                + 0x800080) >>> 8 & 0xff00ff;
        int alphaGreen = ((first >>> 8 & 0xff00ff) * inverse + (last >>> 8 & 0xff00ff) * weight
                + 0x800080) >>> 8 & 0xff00ff;
        return alphaGreen << 8 | redBlue;
    }
}
