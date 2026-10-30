import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashMap;
import java.util.Map;

/**
 * Rounds the resizable panels while retaining their original dimensions, pixels and drawing coordinates.
 *
 * @author lare96
 */
public final class ResizableUi {

    public static final int NONE = -1;
    public static final int MINIMAP = 3;

    private static final int CORNER_RADIUS = 20;

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
        inventoryBounds = new Rectangle(layout.inventoryBlock);
        chatboxBounds = new Rectangle(layout.chatboxBlock);
        minimapBounds = new Rectangle(layout.minimap);
        inventory = new Panel(layout.inventoryBlock, inventoryBounds, CORNER_RADIUS);
        chatbox = new Panel(layout.chatboxBlock, chatboxBounds, CORNER_RADIUS);
        minimap = new Panel(layout.minimap, minimapBounds, 12);
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
        if (width <= 0 || height <= 0 || (long) width * height > pixels.length
                || logicalArea.width != width || logicalArea.height != height) {
            throw new IllegalArgumentException("Pixel data does not match the component dimensions");
        }
        long key = (long) width << 32 | height & 0xffffffffL;
        BufferedImage image = pixelImages.computeIfAbsent(key,
                ignored -> new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB));
        int[] stagingPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(pixels, 0, stagingPixels, 0, width * height);
        Graphics2D graphics = graphicsFor(panelCode, alpha);
        try {
            graphics.drawImage(image, logicalArea.x, logicalArea.y, null);
        } finally {
            graphics.dispose();
        }
    }

    /**
     * Copies each completed panel at its native size, without resampling any artwork or text.
     */
    public void present(Graphics2D target) {
        Graphics2D graphics = (Graphics2D) target.create();
        try {
            graphics.setComposite(AlphaComposite.SrcOver);
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
        if (panelCode != NONE && panelCode != ClientLayout.AREA_VIEWPORT) panel(panelCode);
        return new Point(screenX, screenY);
    }

    /**
     * Converts a native coordinate to its screen position.
     */
    public Point toScreen(int logicalX, int logicalY, int panelCode) {
        if (panelCode != NONE && panelCode != ClientLayout.AREA_VIEWPORT) panel(panelCode);
        return new Point(logicalX, logicalY);
    }

    private Panel panel(int code) {
        if (code == ClientLayout.AREA_INVENTORY) return inventory;
        if (code == ClientLayout.AREA_CHATBOX) return chatbox;
        if (code == MINIMAP) return minimap;
        throw new IllegalArgumentException("Unknown panel: " + code);
    }

    /**
     * Retains one native panel and a reusable rounded presentation buffer.
     *
     * @author lare96
     */
    private static final class Panel {
        private final Rectangle source;
        private final Rectangle destination;
        private final BufferedImage image;
        private final BufferedImage mask;
        private final BufferedImage rounded;

        private Panel(Rectangle source, Rectangle destination, int topLeftRadius) {
            this.source = new Rectangle(source);
            this.destination = destination;
            image = new BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB);
            rounded = new BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB);
            mask = new BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = mask.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(java.awt.Color.WHITE);
                Area outline = new Area(new RoundRectangle2D.Double(0, 0, source.width, source.height,
                        CORNER_RADIUS * 2, CORNER_RADIUS * 2));
                if (topLeftRadius != CORNER_RADIUS) {
                    // The minimap's 33x33 compass needs a tighter top-left curve.
                    Area cornerBounds = new Area(new Rectangle(0, 0, CORNER_RADIUS, CORNER_RADIUS));
                    outline.subtract(cornerBounds);
                    Area corner = new Area(new RoundRectangle2D.Double(0, 0,
                            CORNER_RADIUS * 2, CORNER_RADIUS * 2, topLeftRadius * 2, topLeftRadius * 2));
                    corner.intersect(cornerBounds);
                    outline.add(corner);
                }
                graphics.fill(outline);
            } finally {
                graphics.dispose();
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
            Graphics2D graphics = rounded.createGraphics();
            try {
                graphics.setComposite(AlphaComposite.Src);
                graphics.drawImage(image, 0, 0, null);
                graphics.setComposite(AlphaComposite.DstIn);
                graphics.drawImage(mask, 0, 0, null);
            } finally {
                graphics.dispose();
            }
            target.drawImage(rounded, destination.x, destination.y, null);
        }
    }
}
