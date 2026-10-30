import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Checks scaled panel geometry, native artwork preservation, corner masks and input alignment headlessly.
 *
 * @author lare96
 */
public final class ResizableUiTest {

    public static void main(String[] args) {
        panelGeometry();
        inputMapping();
        inputOwnership();
        renderedInputAlignment();
        nativeArtworkAndCorners();
        componentTranslationAndAlpha();
        frameClearingAndGraphicsIsolation();
        System.out.println("ResizableUiTest passed");
    }

    private static void panelGeometry() {
        int[][] sizes = {{765, 503}, {900, 600}, {1290, 806}, {1366, 768}, {1920, 503}, {765, 1080}};
        for (int[] size : sizes) {
            ClientLayout layout = ClientLayout.create(true, size[0], size[1]);
            ResizableUi ui = new ResizableUi(layout, size[0], size[1]);
            Rectangle window = new Rectangle(0, 0, size[0], size[1]);
            check(window.contains(ui.inventoryBounds) && window.contains(ui.chatboxBounds)
                    && window.contains(ui.minimapBounds), "all panels fit " + size[0] + "x" + size[1]);
            check(ui.inventoryBounds.x + ui.inventoryBounds.width == size[0]
                    && ui.inventoryBounds.y + ui.inventoryBounds.height == size[1], "inventory remains bottom-right");
            check(ui.chatboxBounds.x == 0 && ui.chatboxBounds.y + ui.chatboxBounds.height == size[1],
                    "chat remains bottom-left");
            check(ui.minimapBounds.x + ui.minimapBounds.width == size[0] && ui.minimapBounds.y == 0,
                    "minimap remains top-right");
            check(!ui.inventoryBounds.intersects(ui.chatboxBounds) && !ui.inventoryBounds.intersects(ui.minimapBounds),
                    "expanded panels do not overlap");
            check(ui.scale >= 1.0 && ui.scale <= 1.25, "scale stays bounded");
            check(ui.privateChatY() == ui.chatboxBounds.y - 28, "private chat clears displayed panel");
        }
        ResizableUi full = new ResizableUi(ClientLayout.create(true, 1366, 768), 1366, 768);
        check(full.scale == 1.25 && full.inventoryBounds.width == 311 && full.chatboxBounds.width == 645,
                "normal resized window uses the requested 125% scale");
        ResizableUi minimum = new ResizableUi(ClientLayout.create(true, 765, 503), 765, 503);
        check(minimum.scale == 1.0 && minimum.inventoryBounds.equals(new Rectangle(516, 160, 249, 343))
                && minimum.chatboxBounds.equals(new Rectangle(0, 338, 516, 165)), "minimum size retains native panel geometry");
    }

    private static void inputMapping() {
        for (int[] size : new int[][] {{765, 503}, {800, 600}, {900, 600}, {901, 601}, {1290, 806}, {1366, 768}}) {
            ClientLayout layout = ClientLayout.create(true, size[0], size[1]);
            ResizableUi ui = new ResizableUi(layout, size[0], size[1]);
            int[] codes = {ClientLayout.AREA_INVENTORY, ClientLayout.AREA_CHATBOX, ResizableUi.MINIMAP};
            Rectangle[] sources = {layout.inventoryBlock, layout.chatboxBlock, layout.minimap};
            Rectangle[] destinations = {ui.inventoryBounds, ui.chatboxBounds, ui.minimapBounds};
            for (int i = 0; i < codes.length; i++) {
                Rectangle source = sources[i];
                for (int x = -40; x <= source.width + 40; x += 7) {
                    for (int y = -40; y <= source.height + 40; y += 11) {
                        Point original = new Point(source.x + x, source.y + y);
                        Point displayed = ui.toScreen(original.x, original.y, codes[i]);
                        check(ui.toLogical(displayed.x, displayed.y, codes[i]).equals(original),
                                "native points round-trip even when dragging beyond panel bounds");
                    }
                }
                Rectangle destination = destinations[i];
                int previous = Integer.MIN_VALUE;
                for (int x = destination.x - 60; x < destination.x + destination.width + 60; x++) {
                    int mapped = ui.toLogical(x, destination.y + destination.height / 2, codes[i]).x;
                    check(mapped >= previous, "drag mapping stays monotonic across both panel edges");
                    previous = mapped;
                }
                check(ui.panelAt(destination.x + destination.width / 2, destination.y + destination.height / 2) == codes[i],
                        "displayed panel reserves its hit region");
                check(ui.toLogical(destination.x, destination.y, codes[i]).equals(source.getLocation()),
                        "display origin maps to native origin");
            }
            check(ui.panelAt(100, 100) == ResizableUi.NONE, "world input remains unscaled");
            check(ui.toLogical(100, 100, ResizableUi.NONE).equals(new Point(100, 100)), "world input is identity");
            check(ui.toLogical(100, 100, ClientLayout.AREA_VIEWPORT).equals(new Point(100, 100)),
                    "viewport context-menu coordinates are identity");
        }
    }

    private static void nativeArtworkAndCorners() {
        ClientLayout layout = ClientLayout.create(true, 765, 503);
        ResizableUi ui = new ResizableUi(layout, 765, 503);
        int[] art = new int[249 * 343];
        Arrays.fill(art, 0x735638);
        for (int y = 40; y < 70; y++) {
            for (int x = 30; x < 60; x++) art[y * 249 + x] = ((x * 173) << 8 ^ y * 79) & 0xffffff;
        }
        int[] original = art.clone();
        ui.beginFrame();
        ui.drawPixels(art, 249, 343, layout.inventoryBlock, ClientLayout.AREA_INVENTORY, ClientLayout.OPAQUE);
        BufferedImage rendered = render(ui, 765, 503);
        for (int y = 40; y < 70; y++) {
            for (int x = 30; x < 60; x++) {
                check(rendered.getRGB(ui.inventoryBounds.x + x, ui.inventoryBounds.y + y) == (0xff000000 | art[y * 249 + x]),
                        "original opaque artwork pixels retain their exact colours");
            }
        }
        check(Arrays.equals(art, original), "masking and scaling never mutate the source art");
        check(rendered.getRGB(ui.inventoryBounds.x, ui.inventoryBounds.y) == 0,
                "outer corner is transparent instead of a square backing");
        check(rendered.getRGB(ui.inventoryBounds.x + 21, ui.inventoryBounds.y + 21) == 0xff735638,
                "rounding preserves the interior stone texture");
        Graphics2D map = ui.graphicsFor(ResizableUi.MINIMAP, ClientLayout.OPAQUE);
        map.setColor(new Color(0xabcdef));
        map.fillRect(layout.minimap.x, layout.minimap.y, 172, 156);
        map.dispose();
        BufferedImage withMap = render(ui, 765, 503);
        check(withMap.getRGB(ui.minimapBounds.x, 0) == 0xffabcdef,
                "minimap corner is unmasked so the compass art remains intact");
    }

    private static void renderedInputAlignment() {
        for (int[] size : new int[][] {{765, 503}, {800, 600}, {900, 600}, {901, 601}, {1290, 806}, {1366, 768}}) {
            ClientLayout layout = ClientLayout.create(true, size[0], size[1]);
            ResizableUi ui = new ResizableUi(layout, size[0], size[1]);
            int[] codes = {ClientLayout.AREA_INVENTORY, ClientLayout.AREA_CHATBOX, ResizableUi.MINIMAP};
            Rectangle[] sources = {layout.inventoryBlock, layout.chatboxBlock, layout.minimap};
            Rectangle[] destinations = {ui.inventoryBounds, ui.chatboxBounds, ui.minimapBounds};
            for (int panel = 0; panel < codes.length; panel++) {
                Rectangle source = sources[panel];
                int[] pixels = new int[source.width * source.height];
                for (int i = 0; i < pixels.length; i++) pixels[i] = 0x100000 | i + 1;
                ui.drawPixels(pixels, source.width, source.height, source, codes[panel], ClientLayout.OPAQUE);
                BufferedImage image = render(ui, size[0], size[1]);
                Rectangle shown = destinations[panel];
                for (int y = shown.y + 30; y < shown.y + shown.height - 30; y++) {
                    for (int x = shown.x + 30; x < shown.x + shown.width - 30; x++) {
                        Point nativePoint = ui.toLogical(x, y, codes[panel]);
                        int original = pixels[(nativePoint.y - source.y) * source.width + nativePoint.x - source.x];
                        check(image.getRGB(x, y) == (0xff000000 | original),
                                "input selects the source pixel actually displayed at " + x + "," + y
                                        + " in " + size[0] + "x" + size[1] + ", panel " + codes[panel]
                                        + ", actual " + Integer.toHexString(image.getRGB(x, y))
                                        + ", expected " + Integer.toHexString(0xff000000 | original));
                    }
                }
            }
        }
    }

    private static void inputOwnership() {
        ClientLayout layout = ClientLayout.create(true, 1366, 768);
        ResizableUi ui = new ResizableUi(layout, 1366, 768);
        int inventory = ClientLayout.AREA_INVENTORY;
        int chat = ClientLayout.AREA_CHATBOX;
        int world = ClientLayout.AREA_VIEWPORT;
        Point overChat = new Point(ui.chatboxBounds.x + 100, ui.chatboxBounds.y + 50);
        check(ui.panelAt(overChat.x, overChat.y) == chat, "drag crosses into a different displayed panel");
        check(ui.mapInput(overChat.x, overChat.y, inventory, false, world)
                        .equals(ui.toLogical(overChat.x, overChat.y, inventory)),
                "inventory drag retains its original coordinate space over chat");
        check(!ui.mapInput(overChat.x, overChat.y, inventory, false, world)
                        .equals(ui.toLogical(overChat.x, overChat.y, chat)),
                "crossing a panel does not switch an active drag's coordinate space");
        check(ui.mapInput(overChat.x, overChat.y, inventory, true, chat)
                        .equals(ui.toLogical(overChat.x, overChat.y, inventory)),
                "drag ownership takes priority over an active menu");
        for (int area : new int[] {inventory, chat}) {
            Rectangle source = area == inventory ? layout.inventoryBlock : layout.chatboxBlock;
            Point outsideLogical = new Point(source.x - 60, source.y - 40);
            Point outsideScreen = ui.toScreen(outsideLogical.x, outsideLogical.y, area);
            check(ui.panelAt(outsideScreen.x, outsideScreen.y) != area, "menu pointer moves outside its owning panel");
            check(ui.mapInput(outsideScreen.x, outsideScreen.y, ResizableUi.NONE, true, area).equals(outsideLogical),
                    "panel menus retain their inverse transform outside panel bounds");
        }
        Point overInventory = new Point(ui.inventoryBounds.x + 100, ui.inventoryBounds.y + 100);
        check(ui.mapInput(overInventory.x, overInventory.y, ResizableUi.NONE, true, world).equals(overInventory),
                "world menu remains in viewport coordinates when the pointer crosses a panel");
        check(ui.mapInput(overInventory.x, overInventory.y, world, false, world).equals(overInventory),
                "world drag remains in viewport coordinates over a panel");
        Point logicalMap = new Point(layout.minimap.x + 70, layout.minimap.y + 60);
        Point displayedMap = ui.toScreen(logicalMap.x, logicalMap.y, ResizableUi.MINIMAP);
        check(ui.mapInput(displayedMap.x, displayedMap.y, ResizableUi.NONE, false, world).equals(logicalMap),
                "ordinary minimap input uses the minimap inverse transform");
        check(ui.mapInput(-1, -1, inventory, true, chat).equals(new Point(-1, -1)),
                "mouse-exit sentinel remains intact during a drag or menu");
    }

    private static void componentTranslationAndAlpha() {
        ClientLayout layout = ClientLayout.create(true, 1366, 768);
        ResizableUi ui = new ResizableUi(layout, 1366, 768);
        ui.beginFrame();
        Graphics2D graphics = ui.graphicsFor(ClientLayout.AREA_INVENTORY, ClientLayout.OPAQUE);
        graphics.setColor(new Color(0xe6c13b));
        graphics.fillRect(layout.inventoryBlock.x + 80, layout.inventoryBlock.y + 60, 10, 10);
        graphics.dispose();
        Point glyph = ui.toScreen(layout.inventoryBlock.x + 84, layout.inventoryBlock.y + 64, ClientLayout.AREA_INVENTORY);
        BufferedImage image = render(ui, 1366, 768);
        check(image.getRGB(glyph.x, glyph.y) == 0xffe6c13b, "global drawing coordinates translate into the correct scaled panel");
        int[] checker = new int[16 * 16];
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) checker[y * 16 + x] = (x + y) % 2 == 0 ? 0xff00ff : 0x00ffff;
        }
        Rectangle checkerArea = new Rectangle(layout.inventoryBlock.x + 100, layout.inventoryBlock.y + 80, 16, 16);
        ui.drawPixels(checker, 16, 16, checkerArea, ClientLayout.AREA_INVENTORY, ClientLayout.OPAQUE);
        image = render(ui, 1366, 768);
        Point checkerStart = ui.toScreen(checkerArea.x + 1, checkerArea.y + 1, ClientLayout.AREA_INVENTORY);
        Point checkerEnd = ui.toScreen(checkerArea.x + 15, checkerArea.y + 15, ClientLayout.AREA_INVENTORY);
        for (int y = checkerStart.y; y < checkerEnd.y; y++) {
            for (int x = checkerStart.x; x < checkerEnd.x; x++) {
                int colour = image.getRGB(x, y);
                check(colour == 0xffff00ff || colour == 0xff00ffff,
                        "enlargement preserves the original sprite palette without blending adjacent pixels");
            }
        }
        int[] pixels = new int[8 * 8];
        Arrays.fill(pixels, 0x204060);
        Rectangle area = new Rectangle(layout.chatboxBlock.x + 60, layout.chatboxBlock.y + 40, 8, 8);
        ui.drawPixels(pixels, 8, 8, area, ClientLayout.AREA_CHATBOX, 128);
        Point sample = ui.toScreen(area.x + 3, area.y + 3, ClientLayout.AREA_CHATBOX);
        image = render(ui, 1366, 768);
        check((image.getRGB(sample.x, sample.y) >>> 24) == 128, "panel opacity is applied once during native composition");
    }

    private static void frameClearingAndGraphicsIsolation() {
        ClientLayout layout = ClientLayout.create(true, 900, 600);
        ResizableUi ui = new ResizableUi(layout, 900, 600);
        Graphics2D panel = ui.graphicsFor(ClientLayout.AREA_INVENTORY, ClientLayout.OPAQUE);
        panel.setColor(Color.RED);
        panel.fillRect(layout.inventoryBlock.x, layout.inventoryBlock.y, 249, 343);
        panel.dispose();
        ui.beginFrame();
        BufferedImage image = render(ui, 900, 600);
        check(image.getRGB(ui.inventoryBounds.x + 50, ui.inventoryBounds.y + 50) == 0,
                "starting a frame clears stale panel contents");
        Graphics2D target = image.createGraphics();
        target.translate(3, 4);
        target.setComposite(AlphaComposite.Src);
        target.setClip(0, 0, 40, 50);
        AffineTransform transform = target.getTransform();
        Rectangle clip = target.getClipBounds();
        ui.present(target);
        check(target.getTransform().equals(transform) && target.getClipBounds().equals(clip)
                && target.getComposite().equals(AlphaComposite.Src), "presentation restores the caller's graphics state");
        target.dispose();
    }

    private static BufferedImage render(ResizableUi ui, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        ui.present(graphics);
        graphics.dispose();
        return image;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
