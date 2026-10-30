import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Exercises the client's native input consumers without opening a window or connecting to a server.
 *
 * @author lare96
 */
public final class ResizableClientInputTest {

    public static void main(String[] args) throws Exception {
        check(!GraphicsEnvironment.isHeadless(), "Run this Applet fixture with -Djava.awt.headless=false");
        int previousWidth = client.clientWidth;
        int previousHeight = client.clientHeight;
        Player previousPlayer = client.thisPlayer;
        try {
            fixedModeInput();
            resizedTabs();
            resizedChatButtons();
            minimapDestination();
            loginAndFullscreenInput();
            dragOwnership();
            scrollbarOwnership();
            menuOwnership();
            awtInputCapture();
            panelPresentation();
        } finally {
            client.clientWidth = previousWidth;
            client.clientHeight = previousHeight;
            client.thisPlayer = previousPlayer;
        }
        System.out.println("ResizableClientInputTest passed");
    }

    private static void fixedModeInput() throws Exception {
        Fixture fixture = new Fixture(false);
        Point original = new Point(647, 185);
        fixture.sample(original, original);
        fixture.assertMapped(original, original, "fixed mode keeps its native input coordinates");
        fixture.game.anInt28 = 1;
        fixture.game.tabId = 0;
        fixture.game.processMouseClickOnTabs21();
        check(fixture.game.tabId == 3, "fixed inventory tab retains its original click bounds");
        Point outside = new Point(-1, -1);
        fixture.sample(outside, new Point(55, 484));
        fixture.assertMapped(outside, new Point(55, 484), "fixed mouse-exit and chat coordinates stay unchanged");
    }

    private static void resizedTabs() throws Exception {
        Fixture fixture = new Fixture(true);
        int[] classicX = {555, 584, 612, 647, 681, 709, 740, 557, 587, 614, 649, 683, 711, 741};
        for (int tab = 0; tab < classicX.length; tab++) {
            Point logical = new Point(classicX[tab] + fixture.game.layout.inventoryDx,
                    (tab < 7 ? 185 : 484) + fixture.game.layout.inventoryDy);
            Point physical = fixture.ui.toScreen(logical.x, logical.y, ClientLayout.AREA_INVENTORY);
            fixture.sample(physical, physical);
            fixture.assertMapped(logical, logical, "resized tab " + tab + " maps into the original native bounds");
            fixture.game.anInt28 = 1;
            fixture.game.tabId = -1;
            fixture.game.processMouseClickOnTabs21();
            check(fixture.game.tabId == tab, "actual tab consumer selects resized tab " + tab);
            fixture.game.updateMouseInput();
            fixture.assertMapped(logical, logical, "sampling before drawing does not inverse-scale the click twice");
        }
        Point world = new Point(400, 300);
        fixture.sample(world, world);
        fixture.assertMapped(world, world, "world input remains in unscaled viewport coordinates");
    }

    private static void resizedChatButtons() throws Exception {
        Fixture fixture = new Fixture(true);
        int[] classicX = {55, 184, 324};
        for (int button = 0; button < classicX.length; button++) {
            fixture.game.publicChatMode = 0;
            fixture.game.privateChatMode = 0;
            fixture.game.tradeMode = 0;
            Point logical = new Point(classicX[button], 484 + fixture.game.layout.chatboxDy);
            Point physical = fixture.ui.toScreen(logical.x, logical.y, ClientLayout.AREA_CHATBOX);
            fixture.sample(physical, physical);
            fixture.game.anInt28 = 1;
            fixture.game.method39(true);
            check(fixture.game.publicChatMode == (button == 0 ? 1 : 0)
                            && fixture.game.privateChatMode == (button == 1 ? 1 : 0)
                            && fixture.game.tradeMode == (button == 2 ? 1 : 0),
                    "actual chat consumer changes only the clicked resized mode button");
            fixture.assertRaw(physical, physical);
        }
    }

    private static void minimapDestination() throws Exception {
        Fixture fixture = new Fixture(true);
        client.thisPlayer = new Player();
        client.thisPlayer.unitX = 50 * 128 + 64;
        client.thisPlayer.unitY = 50 * 128 + 64;
        client.thisPlayer.walkingQueueX[0] = 50;
        client.thisPlayer.walkingQueueY[0] = 50;
        fixture.game.anInt1252 = 0;
        fixture.game.anInt916 = 0;
        fixture.game.anInt1233 = 0;
        fixture.game.minimapState = 0;
        Point logical = new Point(fixture.game.layout.minimap.x + 25 + 73 + 24,
                fixture.game.layout.minimap.y + 5 + 75 + 12);
        Point physical = fixture.ui.toScreen(logical.x, logical.y, ResizableUi.MINIMAP);
        fixture.sample(physical, physical);
        fixture.assertMapped(logical, logical, "minimap uses its own display scale");
        fixture.game.anInt28 = 1;
        fixture.game.updateMinimapClick();
        check(fixture.game.walkCalls == 1 && fixture.game.destinationX == 56 && fixture.game.destinationY == 47
                        && fixture.game.sourceX == 50 && fixture.game.sourceY == 50 && fixture.game.packetType == 1,
                "actual minimap consumer calculates the original destination from the scaled click");
        fixture.assertRaw(physical, physical);
    }

    private static void loginAndFullscreenInput() throws Exception {
        Fixture fixture = new Fixture(true);
        Point physical = new Point(fixture.ui.inventoryBounds.x + 60, fixture.ui.inventoryBounds.y + 70);
        fixture.game.isLoggedIn = false;
        fixture.sample(physical, physical);
        fixture.assertMapped(physical, physical, "login input bypasses gameplay panel transforms");
        check(fixture.game.inputOffsetX() == (client.clientWidth - client.REGULAR_WIDTH) / 2
                        && fixture.game.inputOffsetY() == (client.clientHeight - client.REGULAR_HEIGHT) / 2,
                "resizable login retains its original centered input offset");
        fixture.game.isLoggedIn = true;
        fixture.game.anInt1053 = 123;
        fixture.sample(physical, physical);
        fixture.assertMapped(physical, physical, "fullscreen interfaces bypass panel input transforms");
        fixture.game.anInt1053 = -1;
        fixture.sample(physical, physical);
        Point logical = fixture.ui.toLogical(physical.x, physical.y, ClientLayout.AREA_INVENTORY);
        fixture.assertMapped(logical, logical, "closing a fullscreen interface restores scaled panel input");
    }

    private static void dragOwnership() throws Exception {
        Fixture fixture = new Fixture(true);
        Point overChat = new Point(fixture.ui.chatboxBounds.x + 100, fixture.ui.chatboxBounds.y + 40);
        Point overInventory = new Point(fixture.ui.inventoryBounds.x + 80, fixture.ui.inventoryBounds.y + 90);
        int[] dragTypes = {1, 2, 3};
        int[] panelCodes = {ClientLayout.AREA_VIEWPORT, ClientLayout.AREA_INVENTORY, ClientLayout.AREA_CHATBOX};
        for (int index = 0; index < dragTypes.length; index++) {
            fixture.game.anInt1113 = dragTypes[index];
            fixture.game.isContextMenuActive = true;
            fixture.game.anInt1304 = ClientLayout.AREA_CHATBOX;
            Point logicalMouse = fixture.ui.toLogical(overChat.x, overChat.y, panelCodes[index]);
            Point logicalClick = fixture.ui.toLogical(overInventory.x, overInventory.y, panelCodes[index]);
            fixture.sample(overChat, overInventory);
            fixture.assertMapped(logicalMouse, logicalClick, "drag type " + dragTypes[index] + " owns its input space");
            fixture.game.updateMouseInput();
            fixture.assertMapped(logicalMouse, logicalClick, "repeated drag sampling remains stable across panels");
        }
        fixture.game.anInt1113 = 0;
        fixture.game.isContextMenuActive = false;
        fixture.sample(overChat, overInventory);
        fixture.assertMapped(fixture.ui.toLogical(overChat.x, overChat.y, ClientLayout.AREA_CHATBOX),
                fixture.ui.toLogical(overInventory.x, overInventory.y, ClientLayout.AREA_INVENTORY),
                "ending a drag restores independent hover and click panel mapping");
    }

    private static void menuOwnership() throws Exception {
        Fixture fixture = new Fixture(true);
        Point overInventory = new Point(fixture.ui.inventoryBounds.x + 50, fixture.ui.inventoryBounds.y + 60);
        fixture.game.isContextMenuActive = true;
        fixture.game.anInt1304 = ClientLayout.AREA_VIEWPORT;
        fixture.sample(overInventory, overInventory);
        fixture.assertMapped(overInventory, overInventory, "world context menu stays unscaled over displayed panels");
        Point outside = new Point(600, 250);
        for (int area : new int[] {ClientLayout.AREA_INVENTORY, ClientLayout.AREA_CHATBOX}) {
            fixture.game.anInt1304 = area;
            fixture.sample(outside, outside);
            Point logical = fixture.ui.toLogical(outside.x, outside.y, area);
            fixture.assertMapped(logical, logical, "panel context menu keeps its inverse transform outside the panel");
        }
        fixture.game.anInt1113 = 2;
        fixture.sample(new Point(-1, -1), outside);
        check(fixture.game.mouseX == -1 && fixture.game.mouseY == -1,
                "mouse-exit sentinel survives active menu and drag mapping");
    }

    private static void scrollbarOwnership() throws Exception {
        Fixture fixture = new Fixture(true);
        Point chatLogical = new Point(fixture.game.layout.chatbox.x + 468, fixture.game.layout.chatbox.y + 38);
        Point chatPhysical = fixture.ui.toScreen(chatLogical.x, chatLogical.y, ClientLayout.AREA_CHATBOX);
        fixture.game.anInt21 = 1;
        fixture.game.anInt1094 = 1;
        fixture.sample(chatPhysical, chatPhysical);
        fixture.game.method42(1000, 0, new JagInterface(), (byte) 102, 38, 2, 468, 77, 463);
        check(fixture.game.aBoolean1127, "actual scrollbar consumer captures a held thumb drag");
        Point overInventory = new Point(fixture.ui.inventoryBounds.x + 80, fixture.ui.inventoryBounds.y + 100);
        fixture.sample(overInventory, chatPhysical);
        fixture.assertMapped(fixture.ui.toLogical(overInventory.x, overInventory.y, ClientLayout.AREA_CHATBOX),
                chatLogical, "held scrollbar retains chat coordinates when pointer crosses into inventory");
        fixture.game.anInt21 = 0;
        fixture.sample(overInventory, chatPhysical);
        fixture.assertMapped(fixture.ui.toLogical(overInventory.x, overInventory.y, ClientLayout.AREA_INVENTORY),
                chatLogical, "releasing the scrollbar restores normal pointer mapping");
    }

    private static void panelPresentation() throws Exception {
        Method drawPanel = client.class.getDeclaredMethod("drawPanel", JagImageProducer.class, Rectangle.class, int.class);
        drawPanel.setAccessible(true);
        final int background = 0x193a52;
        for (boolean resizable : new boolean[] {false, true}) {
            Fixture fixture = new Fixture(resizable);
            Rectangle[] areas = {fixture.game.layout.inventory, fixture.game.layout.chatbox, fixture.game.layout.minimap};
            int[] owners = {ClientLayout.AREA_INVENTORY, ClientLayout.AREA_CHATBOX, ResizableUi.MINIMAP};
            for (int component = 0; component < areas.length; component++) {
                Rectangle area = areas[component];
                JagImageProducer panel = new JagImageProducer(area.width, area.height, new Canvas());
                for (int y = 0; y < area.height; y++) {
                    for (int x = 0; x < area.width; x++) {
                        panel.pixels[y * area.width + x] = ((x / 8 + y / 8) & 1) == 0
                                ? 0 : 0x683319 + component * 0x102030;
                    }
                }
                int[] original = panel.pixels.clone();
                BufferedImage screen = new BufferedImage(client.clientWidth, client.clientHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = screen.createGraphics();
                graphics.setColor(new Color(background));
                graphics.fillRect(0, 0, screen.getWidth(), screen.getHeight());
                fixture.game.graphics = graphics;
                if (resizable) fixture.ui.beginFrame();
                drawPanel.invoke(fixture.game, panel, area, resizable ? ClientLayout.OPAQUE : 128);
                if (resizable) {
                    check((screen.getRGB(area.x + 10, area.y + 10) & 0xffffff) == background,
                            "resizable drawPanel stages components instead of drawing at unscaled screen positions");
                    fixture.ui.present(graphics);
                    for (int y = 3; y < area.height; y += 8) {
                        for (int x = 3; x < area.width; x += 8) {
                            Point shown = fixture.ui.toScreen(area.x + x, area.y + y, owners[component]);
                            check((screen.getRGB(shown.x, shown.y) & 0xffffff) == original[y * area.width + x],
                                    "actual resizable drawPanel routes component to its scaled destination");
                        }
                    }
                    check((screen.getRGB(400, 250) & 0xffffff) == background,
                            "resizable panel presentation leaves the world untouched");
                } else {
                    for (int y = 0; y < screen.getHeight(); y++) {
                        for (int x = 0; x < screen.getWidth(); x++) {
                            int expected = area.contains(x, y)
                                    ? original[(y - area.y) * area.width + x - area.x] : background;
                            check((screen.getRGB(x, y) & 0xffffff) == expected,
                                    "fixed drawPanel stays exactly unscaled and opaque, including black source pixels");
                        }
                    }
                }
                graphics.dispose();
                check(Arrays.equals(original, panel.pixels), "actual presentation never alters original panel pixels");
            }
        }
    }

    private static void awtInputCapture() throws Exception {
        Fixture fixture = new Fixture(true);
        Point physical = new Point(fixture.ui.inventoryBounds.x + 100, fixture.ui.inventoryBounds.y + 80);
        fixture.game.mouseMoved(mouse(fixture.game, MouseEvent.MOUSE_MOVED, physical, MouseEvent.NOBUTTON));
        check(fixture.game.screenMouseX == physical.x && fixture.game.screenMouseY == physical.y,
                "AWT movement stores physical coordinates before game-thread mapping");
        fixture.game.screenClickX = physical.x;
        fixture.game.screenClickY = physical.y;
        fixture.game.updateMouseInput();
        Point logical = fixture.ui.toLogical(physical.x, physical.y, ClientLayout.AREA_INVENTORY);
        fixture.assertMapped(logical, logical, "game-thread sampling maps captured AWT movement");
        fixture.game.mousePressed(mouse(fixture.game, MouseEvent.MOUSE_PRESSED, physical, MouseEvent.BUTTON1));
        check(fixture.game.anInt25 == physical.x && fixture.game.anInt26 == physical.y
                        && fixture.game.anInt24 == 1 && fixture.game.anInt21 == 1,
                "AWT press retains the physical press for the game-thread click snapshot");
        fixture.game.mouseReleased(mouse(fixture.game, MouseEvent.MOUSE_RELEASED, physical, MouseEvent.BUTTON1));
        check(fixture.game.anInt21 == 0, "AWT release clears held-button state");
        fixture.game.mouseExited(mouse(fixture.game, MouseEvent.MOUSE_EXITED, physical, MouseEvent.NOBUTTON));
        fixture.game.updateMouseInput();
        check(fixture.game.screenMouseX == -1 && fixture.game.screenMouseY == -1
                        && fixture.game.mouseX == -1 && fixture.game.mouseY == -1,
                "AWT exit reaches both physical and logical mouse fields");

        fixture.game.isLoggedIn = false;
        Point login = new Point(220, 180);
        Point centered = new Point(login.x + fixture.game.inputOffsetX(), login.y + fixture.game.inputOffsetY());
        fixture.game.mouseMoved(mouse(fixture.game, MouseEvent.MOUSE_MOVED, centered, MouseEvent.NOBUTTON));
        fixture.game.screenClickX = login.x;
        fixture.game.screenClickY = login.y;
        fixture.game.updateMouseInput();
        fixture.assertMapped(login, login, "AWT login offset is applied exactly once before sampling");
    }

    private static MouseEvent mouse(client game, int id, Point point, int button) {
        return new MouseEvent(game, id, 1L, 0, point.x, point.y, 1, false, button);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /**
     * Configures the real client without invoking its window, asset, audio or networking startup.
     *
     * @author lare96
     */
    private static final class Fixture {
        private final InputClient game;
        private final ResizableUi ui;

        private Fixture(boolean resizable) throws Exception {
            client.clientWidth = resizable ? 1366 : client.REGULAR_WIDTH;
            client.clientHeight = resizable ? 768 : client.REGULAR_HEIGHT;
            game = new InputClient();
            game.clientSize = resizable ? 1 : 0;
            game.layout = ClientLayout.create(resizable, client.clientWidth, client.clientHeight);
            ui = resizable ? new ResizableUi(game.layout, client.clientWidth, client.clientHeight) : null;
            Field presentation = client.class.getDeclaredField("resizableUi");
            presentation.setAccessible(true);
            presentation.set(game, ui);
            game.isLoggedIn = true;
            game.anInt1053 = -1;
            Arrays.fill(game.anIntArray1081, 1);
            game.outBuffer.random = new IsaacRandom(new int[4]);
        }

        private void sample(Point mouse, Point click) {
            game.screenMouseX = mouse.x;
            game.screenMouseY = mouse.y;
            game.screenClickX = click.x;
            game.screenClickY = click.y;
            game.updateMouseInput();
            assertRaw(mouse, click);
        }

        private void assertMapped(Point mouse, Point click, String message) {
            check(game.mouseX == mouse.x && game.mouseY == mouse.y
                    && game.anInt29 == click.x && game.anInt30 == click.y, message);
        }

        private void assertRaw(Point mouse, Point click) {
            check(game.screenMouseX == mouse.x && game.screenMouseY == mouse.y
                            && game.screenClickX == click.x && game.screenClickY == click.y,
                    "mapping preserves physical mouse and click fields");
        }
    }

    /**
     * Observes the native minimap's chosen destination without pathfinding or sending packets.
     *
     * @author lare96
     */
    private static final class InputClient extends client {
        private int walkCalls;
        private int destinationX;
        private int destinationY;
        private int sourceX;
        private int sourceY;
        private int packetType;

        @Override
        public boolean walk(boolean flag, boolean flag1, int dstY, int srcY, int k, int l,
                            int type, int j1, int dstX, int l1, int i2, int srcX) {
            walkCalls++;
            destinationX = dstX;
            destinationY = dstY;
            sourceX = srcX;
            sourceY = srcY;
            packetType = type;
            return false;
        }
    }
}
