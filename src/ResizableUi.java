import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Presents the original panel art at a larger size without changing its native drawing coordinates.
 *
 * @author lare96
 */
public final class ResizableUi {

    public static final int NONE = -1;
    public static final int MINIMAP = 3;

    private static final double MAX_SCALE = 1.25;
    private static final double MAX_MINIMAP_SCALE = 1.15;
    private static final int CORNER_RADIUS = 20;

    public final double scale;
    public final Rectangle inventoryBounds;
    public final Rectangle chatboxBounds;
    public final Rectangle minimapBounds;

    private final Panel inventory;
    private final Panel chatbox;
    private final Panel minimap;
    private final Map<Long, BufferedImage> pixelImages = new HashMap<>();

    public ResizableUi(ClientLayout layout, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Expected positive window dimensions");
        }
        scale = Math.max(1.0, Math.min(MAX_SCALE,
                Math.min((double) width / ClientLayout.FIXED_WIDTH,
                        (double) height / ClientLayout.FIXED_HEIGHT)));
        inventoryBounds = bottomRight(layout.inventoryBlock, width, height, scale);
        chatboxBounds = bottomLeft(layout.chatboxBlock, height, scale);
        double minimapScale = Math.min(MAX_MINIMAP_SCALE, scale);
        minimapBounds = new Rectangle(width - scaled(layout.minimap.width, minimapScale), 0,
                scaled(layout.minimap.width, minimapScale), scaled(layout.minimap.height, minimapScale));
        inventory = new Panel(layout.inventoryBlock, inventoryBounds, true);
        chatbox = new Panel(layout.chatboxBlock, chatboxBounds, true);
        minimap = new Panel(layout.minimap, minimapBounds, false);
    }

    /**
     * Places viewport chat immediately above the displayed chat panel.
     */
    public int privateChatY() {
        return chatboxBounds.y - 28;
    }

    /**
     * Removes the previous frame while retaining the native panel buffers.
     */
    public void beginFrame() {
        inventory.clear();
        chatbox.clear();
        minimap.clear();
    }

    /**
     * Creates a panel graphics context accepting the existing layout's global drawing coordinates.
     * The caller disposes this context after drawing.
     */
    public Graphics2D graphicsFor(int panelCode, int alpha) {
        Panel panel = panel(panelCode);
        Graphics2D graphics = panel.image.createGraphics();
        graphics.translate(-panel.source.x, -panel.source.y);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                Math.max(0, Math.min(ClientLayout.OPAQUE, alpha)) / (float) ClientLayout.OPAQUE));
        return graphics;
    }

    /**
     * Copies a native software-rendered component into its panel, reusing staging images between frames.
     */
    public void drawPixels(int[] pixels, int width, int height, Rectangle logicalArea, int panelCode, int alpha) {
        if (width <= 0 || height <= 0 || (long) width * height > pixels.length) {
            throw new IllegalArgumentException("Pixel data does not match the component dimensions");
        }
        long key = (long) width << 32 | height & 0xffffffffL;
        BufferedImage image = pixelImages.computeIfAbsent(key,
                ignored -> new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB));
        image.setRGB(0, 0, width, height, pixels, 0, width);
        Graphics2D graphics = graphicsFor(panelCode, alpha);
        try {
            graphics.drawImage(image, logicalArea.x, logicalArea.y, logicalArea.width, logicalArea.height, null);
        } finally {
            graphics.dispose();
        }
    }

    /**
     * Composites each completed panel once, preserving original pixel art with nearest-neighbour scaling.
     */
    public void present(Graphics2D target) {
        Graphics2D graphics = (Graphics2D) target.create();
        try {
            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            chatbox.present(graphics);
            inventory.present(graphics);
            minimap.present(graphics);
        } finally {
            graphics.dispose();
        }
    }

    /**
     * Identifies the displayed panel beneath a screen coordinate, keeping its full control rectangle reserved.
     */
    public int panelAt(int screenX, int screenY) {
        if (inventoryBounds.contains(screenX, screenY)) return ClientLayout.AREA_INVENTORY;
        if (chatboxBounds.contains(screenX, screenY)) return ClientLayout.AREA_CHATBOX;
        if (minimapBounds.contains(screenX, screenY)) return MINIMAP;
        return NONE;
    }

    /**
     * Maps native input according to its owning drag or menu before considering the panel under the pointer.
     */
    public Point mapInput(int screenX, int screenY, int dragArea, boolean menuActive, int menuArea) {
        if (screenX == -1 && screenY == -1) return new Point(-1, -1);
        int area = dragArea != NONE ? dragArea : menuActive ? menuArea : panelAt(screenX, screenY);
        return toLogical(screenX, screenY, area);
    }

    /**
     * Converts screen input to native layout coordinates, including positions outside a dragged panel.
     */
    public Point toLogical(int screenX, int screenY, int panelCode) {
        if (panelCode == NONE || panelCode == ClientLayout.AREA_VIEWPORT) return new Point(screenX, screenY);
        Panel panel = panel(panelCode);
        return new Point(inverse(screenX, panel.destination.x, panel.destination.width,
                        panel.source.x, panel.source.width),
                inverse(screenY, panel.destination.y, panel.destination.height,
                        panel.source.y, panel.source.height));
    }

    /**
     * Converts a native coordinate to a screen pixel at the centre of its displayed source pixel.
     */
    public Point toScreen(int logicalX, int logicalY, int panelCode) {
        if (panelCode == NONE || panelCode == ClientLayout.AREA_VIEWPORT) return new Point(logicalX, logicalY);
        Panel panel = panel(panelCode);
        return new Point(forward(logicalX, panel.source.x, panel.source.width,
                        panel.destination.x, panel.destination.width),
                forward(logicalY, panel.source.y, panel.source.height,
                        panel.destination.y, panel.destination.height));
    }

    private Panel panel(int code) {
        if (code == ClientLayout.AREA_INVENTORY) return inventory;
        if (code == ClientLayout.AREA_CHATBOX) return chatbox;
        if (code == MINIMAP) return minimap;
        throw new IllegalArgumentException("Unknown panel: " + code);
    }

    private static Rectangle bottomRight(Rectangle source, int width, int height, double scale) {
        int panelWidth = scaled(source.width, scale);
        int panelHeight = scaled(source.height, scale);
        return new Rectangle(width - panelWidth, height - panelHeight, panelWidth, panelHeight);
    }

    private static Rectangle bottomLeft(Rectangle source, int height, double scale) {
        int panelWidth = scaled(source.width, scale);
        int panelHeight = scaled(source.height, scale);
        return new Rectangle(0, height - panelHeight, panelWidth, panelHeight);
    }

    private static int scaled(int value, double scale) {
        return Math.max(1, (int) Math.round(value * scale));
    }

    /**
     * Matches Java2D's nearest-neighbour pixel centres, choosing the lower source pixel at an exact boundary.
     */
    private static int inverse(int coordinate, int destinationOrigin, int destinationSize,
                               int sourceOrigin, int sourceSize) {
        return sourceOrigin + (int) Math.ceil((coordinate - destinationOrigin + 0.5) * sourceSize / destinationSize) - 1;
    }

    private static int forward(int coordinate, int sourceOrigin, int sourceSize,
                               int destinationOrigin, int destinationSize) {
        return destinationOrigin + (int) Math.floor((coordinate - sourceOrigin + 0.5) * destinationSize / sourceSize);
    }

    /**
     * Retains one native panel and an optional reusable rounded presentation buffer.
     *
     * @author lare96
     */
    private static final class Panel {
        private final Rectangle source;
        private final Rectangle destination;
        private final BufferedImage image;
        private final BufferedImage mask;
        private final BufferedImage rounded;

        private Panel(Rectangle source, Rectangle destination, boolean roundCorners) {
            this.source = new Rectangle(source);
            this.destination = destination;
            image = new BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB);
            if (roundCorners) {
                rounded = new BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB);
                mask = new BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = mask.createGraphics();
                try {
                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    graphics.setColor(java.awt.Color.WHITE);
                    graphics.fill(new RoundRectangle2D.Double(0, 0, source.width, source.height,
                            CORNER_RADIUS * 2, CORNER_RADIUS * 2));
                } finally {
                    graphics.dispose();
                }
            } else {
                rounded = null;
                mask = null;
            }
        }

        private void clear() {
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.setComposite(AlphaComposite.Clear);
                graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            } finally {
                graphics.dispose();
            }
        }

        private void present(Graphics2D target) {
            BufferedImage displayed = image;
            if (rounded != null) {
                Graphics2D graphics = rounded.createGraphics();
                try {
                    graphics.setComposite(AlphaComposite.Src);
                    graphics.drawImage(image, 0, 0, null);
                    graphics.setComposite(AlphaComposite.DstIn);
                    graphics.drawImage(mask, 0, 0, null);
                } finally {
                    graphics.dispose();
                }
                displayed = rounded;
            }
            target.drawImage(displayed, destination.x, destination.y, destination.width, destination.height, null);
        }
    }
}
