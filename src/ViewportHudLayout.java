import java.awt.Point;
import java.awt.Rectangle;

/**
 * Places native status groups in a row above the resizable chatbox without moving unrelated interface children.
 * Cache coordinates remain unchanged; drawing and picking apply the same parent-relative translation.
 *
 * @author lare96
 */
public final class ViewportHudLayout {

    private ViewportHudLayout() {
    }

    /**
     * Identifies walkable roots with explicitly audited native status widgets.
     */
    public static boolean supports(int rootId) {
        return switch (rootId) {
            case 193, 197, 201, 203, 4535, 4958, 8287, 11146, 11344, 15529, 15588 -> true;
            default -> false;
        };
    }

    /**
     * Returns the translation of an immediate root child, preserving local offsets and dimensions within its native
     * HUD group. Groups align to the bottom of the status row.
     */
    public static Point childOffset(ClientLayout layout, int rootId, int childId) {
        if (!layout.resizable) {
            return new Point();
        }
        boolean move = switch (rootId) {
            // Wilderness variants keep the skull and level together within their native container.
            case 193 -> childId == 196;
            case 197 -> childId == 200;
            case 201 -> childId == 202;
            case 203 -> childId == 204;
            // Other models, scores and popups in these mixed roots retain their native positions.
            case 4535 -> childId == 4536;
            case 11146 -> childId == 11155;
            case 11344 -> childId == 11353;
            case 15529 -> childId == 15536;
            case 15588 -> childId == 15589;
            // Gnomeball scores and Brimhaven lights each form one native top-right group.
            case 4958, 8287 -> true;
            default -> false;
        };
        if (!move) {
            return new Point();
        }
        Rectangle group = groupBounds(rootId);
        return new Point(layout.statusHud.x - group.x,
                layout.statusHud.y + layout.statusHud.height - group.height - group.y);
    }

    /**
     * Positions the multicombat marker after the active walkable HUD group, or first when no group is active.
     */
    public static Point multicombatPosition(ClientLayout layout, int rootId) {
        Point position = new Point(layout.multicombatPosition);
        if (layout.resizable) {
            Rectangle group = groupBounds(rootId);
            if (group != null) {
                position.x += group.width + 8;
            }
        }
        return position;
    }

    /**
     * Returns the authored bounds of each movable group, excluding unrelated children in mixed roots.
     */
    private static Rectangle groupBounds(int rootId) {
        return switch (rootId) {
            case 193, 197 -> new Rectangle(457, 256, 53, 41);
            case 201, 203 -> new Rectangle(472, 296, 25, 25);
            case 4535 -> new Rectangle(418, 303, 75, 14);
            case 11146, 11344 -> new Rectangle(470, 317, 35, 11);
            case 15529 -> new Rectangle(480, 302, 25, 25);
            case 15588 -> new Rectangle(480, 304, 25, 25);
            case 4958 -> new Rectangle(428, 4, 80, 60);
            case 8287 -> new Rectangle(485, 5, 22, 43);
            default -> null;
        };
    }
}
