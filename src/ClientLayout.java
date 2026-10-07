import java.awt.Point;
import java.awt.Rectangle;

/**
 * Where each part of the client is drawn, in client (window content) coordinates.
 * <p>
 * Drawing code asks the layout instead of using hardcoded positions, so the fixed (765x503) and the
 * resizable layouts share one code path.
 * <p>
 * Fixed layout: the classic positions, with the game view inside a stone frame.
 * <p>
 * Resizable layout: the game view fills the whole window and the panels are drawn on top of it, each
 * panel keeping its classic size: the minimap in the top right corner, the inventory block (tab icon rows
 * and inventory) in the bottom right corner and the chatbox block (chatbox and chat buttons) in the
 * bottom left corner.
 */
public final class ClientLayout {

    /** Size of the client in the fixed layout. */
    public static final int FIXED_WIDTH = 765, FIXED_HEIGHT = 503;

    /** Sizes of the panels, which are the same in both layouts. */
    public static final int MINIMAP_WIDTH = 172, MINIMAP_HEIGHT = 156;
    public static final int INVENTORY_WIDTH = 190, INVENTORY_HEIGHT = 261;
    public static final int CHATBOX_WIDTH = 479, CHATBOX_HEIGHT = 96;

    /**
     * How opaque the panels are drawn in the resizable layout, from 0 (invisible) to 256 (opaque). The fixed layout
     * is always opaque. The inventory value also covers the tab icons and the stone frame around the inventory, the
     * chatbox value the chat buttons and the frame around the chatbox.
     */
    public static final int OPAQUE = 256;
    public static final int MINIMAP_ALPHA = 256;
    public static final int INVENTORY_ALPHA = 256;
    public static final int CHATBOX_ALPHA = 256;

    /** True for the resizable layout. */
    public final boolean resizable;

    /** The 3D game view. */
    public final Rectangle viewport;
    public final Rectangle minimap;
    /** The inventory / selected tab contents. */
    public final Rectangle inventory;
    public final Rectangle chatbox;
    /** The public / private / trade chat mode buttons below the chatbox. */
    public final Rectangle chatButtons;
    /** The row of tab icons above the inventory. */
    public final Rectangle tabIconsTop;
    /** The row of tab icons below the inventory. */
    public final Rectangle tabIconsBottom;
    /** Number of columns on the left of the bottom tab icon image that are not drawn with the inventory block. */
    public final int tabIconsBottomCrop;
    /** Width of the part of the chatbox frame that sticks out to the right of the chatbox (496 to 516). */
    public static final int CHAT_CORNER_WIDTH = 20;

    /* Stone frame pieces around the panels. The top edge, left edge and minimap pieces are only drawn in the fixed
     * layout. The chatbox and inventory pieces are drawn in both layouts and move with their panels. */
    public final Point frameTopEdge = new Point(0, 0);
    public final Point frameLeftEdge = new Point(0, 4);
    public final Point frameChatboxTop;
    public final Point frameChatboxLeft;
    public final Point frameChatboxRight;
    public final Point frameMinimapLeft = new Point(516, 4);
    public final Point frameMinimapRight = new Point(722, 4);
    public final Point frameRockLeft;
    public final Point frameRockRight;

    /**
     * How far the inventory block is moved from its classic position. Mouse positions inside the block can be
     * compared with the classic coordinates after subtracting these.
     */
    public final int inventoryDx, inventoryDy;

    /** How far the chatbox block is moved down from its classic position. */
    public final int chatboxDy;

    /** Where the first private chat message line is drawn and picked, relative to the viewport. */
    public final int privateChatY;

    /** Where a full screen interface (765x503) is drawn: the centre of the window in the resizable layout. */
    public final int fullscreenInterfaceX, fullscreenInterfaceY;

    /** The area codes used by the context menu: which panel it was opened in. */
    public static final int AREA_VIEWPORT = 0, AREA_INVENTORY = 1, AREA_CHATBOX = 2;

    private ClientLayout(boolean resizable, int clientWidth, int clientHeight) {
        this.resizable = resizable;
        // how far the right (inventory) and bottom (chatbox, inventory) panels move from their classic positions
        int dx = resizable ? Math.max(0, clientWidth - FIXED_WIDTH) : 0;
        int dy = resizable ? Math.max(0, clientHeight - FIXED_HEIGHT) : 0;
        inventoryDx = dx;
        inventoryDy = dy;
        chatboxDy = dy;

        if (resizable) {
            viewport = new Rectangle(0, 0, clientWidth, clientHeight);
            minimap = new Rectangle(clientWidth - MINIMAP_WIDTH, 0, MINIMAP_WIDTH, MINIMAP_HEIGHT);
        } else {
            viewport = new Rectangle(4, 4, 512, 334);
            minimap = new Rectangle(550, 4, MINIMAP_WIDTH, MINIMAP_HEIGHT);
        }
        // inventory block, bottom right
        tabIconsTop = new Rectangle(516 + dx, 160 + dy, 249, 45);
        inventory = new Rectangle(553 + dx, 205 + dy, INVENTORY_WIDTH, INVENTORY_HEIGHT);
        // the bottom icon row is 20px wider than the inventory on the left, where it belongs to the chatbox corner;
        // the resizable layout draws that part with the chatbox block and only the rest with the inventory block
        tabIconsBottomCrop = resizable ? CHAT_CORNER_WIDTH : 0;
        tabIconsBottom = new Rectangle(496 + dx + tabIconsBottomCrop, 466 + dy, 269 - tabIconsBottomCrop, 37);
        // chatbox block, bottom left
        chatbox = new Rectangle(17, 357 + dy, CHATBOX_WIDTH, CHATBOX_HEIGHT);
        chatButtons = new Rectangle(0, 453 + dy, 496, 50);

        // stone frame pieces that belong to the two blocks
        frameChatboxTop = new Point(0, 338 + dy);
        frameChatboxLeft = new Point(0, 357 + dy);
        frameChatboxRight = new Point(496, 357 + dy);
        frameRockLeft = new Point(516 + dx, 205 + dy);
        frameRockRight = new Point(743 + dx, 205 + dy);

        // everything the two blocks cover, including their frame pieces
        inventoryBlock = new Rectangle(516 + dx, 160 + dy, 249, 343);
        chatboxBlock = new Rectangle(0, 338 + dy, 516, 165);

        privateChatY = resizable ? chatbox.y - 28 : 329;
        fullscreenInterfaceX = resizable ? Math.max(0, (clientWidth - FIXED_WIDTH) / 2) : 0;
        fullscreenInterfaceY = resizable ? Math.max(0, (clientHeight - FIXED_HEIGHT) / 2) : 0;
    }

    /** The inventory block: both tab icon rows, the inventory and the frame pieces around it. */
    public final Rectangle inventoryBlock;

    /** The chatbox block: the chatbox, the chat buttons and the frame pieces around them. */
    public final Rectangle chatboxBlock;

    /** True if the point is over one of the panels drawn on top of the viewport (resizable layout only). */
    public boolean isOverPanel(int x, int y) {
        return resizable && (minimap.contains(x, y) || inventoryBlock.contains(x, y) || chatboxBlock.contains(x, y));
    }

    /** True if the point is over the game view and not hidden by a panel. */
    public boolean isInViewport(int x, int y) {
        return isInside(viewport, x, y) && !isOverPanel(x, y);
    }

    public boolean isInInventory(int x, int y) {
        return isInside(inventory, x, y);
    }

    public boolean isInChatbox(int x, int y) {
        return isInside(chatbox, x, y);
    }

    /** Same exclusive bounds the classic hit tests used. */
    private static boolean isInside(Rectangle r, int x, int y) {
        return x > r.x && y > r.y && x < r.x + r.width && y < r.y + r.height;
    }

    /** Left edge of the area a context menu was opened in, one of the AREA_ constants. */
    public int areaX(int area) {
        return area == AREA_INVENTORY ? inventory.x : area == AREA_CHATBOX ? chatbox.x : viewport.x;
    }

    /** Top edge of the area a context menu was opened in, one of the AREA_ constants. */
    public int areaY(int area) {
        return area == AREA_INVENTORY ? inventory.y : area == AREA_CHATBOX ? chatbox.y : viewport.y;
    }

    /**
     * @param resizable    true for the resizable mode
     * @param clientWidth  width of the window content
     * @param clientHeight height of the window content
     */
    public static ClientLayout create(boolean resizable, int clientWidth, int clientHeight) {
        return new ClientLayout(resizable, clientWidth, clientHeight);
    }
}
