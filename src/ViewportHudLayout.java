import java.awt.Point;

/**
 * Positions native corner HUD groups without moving unrelated children in their shared interface roots.
 * Cache coordinates remain unchanged; drawing and picking apply the same parent-relative translation.
 *
 * @author lare96
 */
public final class ViewportHudLayout {

    private ViewportHudLayout() {
    }

    /**
     * Identifies walkable roots with explicitly audited native corner widgets.
     */
    public static boolean supports(int rootId) {
        return switch (rootId) {
            case 193, 197, 201, 203, 4535, 4958, 8287, 11146, 11344, 15529, 15588 -> true;
            default -> false;
        };
    }

    /**
     * Returns the translation of an immediate root child, preserving local offsets within an authored HUD group.
     */
    public static Point childOffset(ClientLayout layout, int rootId, int childId) {
        if (!layout.resizable) {
            return new Point();
        }
        return switch (rootId) {
            // Wilderness variants keep the skull and level together within their native container.
            case 193 -> childId == 196 ? offset(layout, 457, 256, 53) : new Point();
            case 197 -> childId == 200 ? offset(layout, 457, 256, 53) : new Point();
            case 201 -> childId == 202 ? offset(layout, 472, 296, 25) : new Point();
            case 203 -> childId == 204 ? offset(layout, 472, 296, 25) : new Point();
            // Other models, scores and popups in these mixed roots retain their native positions.
            case 4535 -> childId == 4536 ? offset(layout, 418, 303, 75) : new Point();
            case 11146 -> childId == 11155 ? offset(layout, 470, 317, 35) : new Point();
            case 11344 -> childId == 11353 ? offset(layout, 470, 317, 35) : new Point();
            case 15529 -> childId == 15536 ? offset(layout, 480, 302, 25) : new Point();
            case 15588 -> childId == 15589 ? offset(layout, 480, 304, 25) : new Point();
            // Gnomeball scores and Brimhaven lights each form one native top-right group.
            case 4958 -> offset(layout, 428, 4, 80);
            case 8287 -> offset(layout, 485, 5, 22);
            default -> new Point();
        };
    }

    private static Point offset(ClientLayout layout, int x, int y, int width) {
        return new Point(layout.statusHud.x + (layout.statusHud.width - width) / 2 - x,
                layout.statusHud.y - y);
    }
}
