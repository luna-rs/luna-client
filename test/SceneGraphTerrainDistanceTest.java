import java.util.Arrays;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Verifies bounded terrain extension, unchanged object range and scene traversal.
 *
 * @author lare96
 */
public final class SceneGraphTerrainDistanceTest {

    public static void main(String[] args) {
        visibilityAndDepthLimits();
        liveDrawDistance();
        terrainOnlyTraversal();
        sceneEdgesAndOccluders();
        System.out.println("SceneGraphTerrainDistanceTest passed");
    }

    private static void liveDrawDistance() {
        int previousDistance = SceneGraph.getResizableDrawDistanceIncrease();
        int[] heights = visibilityHeights();
        try {
            SceneGraph.preCalcFrustrumTable(heights, 1290, 806, 500, 800, false);
            boolean[][][][] classic = SceneGraph.aBooleanArrayArrayArrayArray506;
            SceneGraph.setResizableDrawDistanceIncrease(15);
            check(SceneGraph.aBooleanArrayArrayArrayArray506 == classic,
                    "changing the saved range in fixed mode does not expand fixed visibility");
            checkDepth(3500);

            SceneGraph.preCalcFrustrumTable(heights, 1290, 806, 500, 800, true);
            boolean[][][][] distance15 = SceneGraph.aBooleanArrayArrayArrayArray506;
            check(distance15[0][0].length == 81 && objectVisibilityTables() == classic,
                    "resizable mode restores the chosen range and reuses classic picking visibility");
            checkDepth(5420);

            SceneGraph.setResizableDrawDistanceIncrease(27);
            boolean[][][][] distance27 = SceneGraph.aBooleanArrayArrayArrayArray506;
            check(distance27[0][0].length == 105 && objectVisibilityTables() == classic,
                    "live distance expands terrain without rebuilding classic interaction visibility");
            checkDepth(6956);
            SceneGraph.setResizableDrawDistanceIncrease(15);
            check(SceneGraph.aBooleanArrayArrayArrayArray506 == distance15,
                    "switching back to a recent distance reuses its visibility table");
            checkDepth(5420);
            SceneGraph.setResizableDrawDistanceIncrease(0);
            check(SceneGraph.aBooleanArrayArrayArrayArray506 == classic,
                    "zero uses the exact classic table while retaining resizable mode");
            checkDepth(3500);
            SceneGraph.setResizableDrawDistanceIncrease(27);
            check(SceneGraph.aBooleanArrayArrayArrayArray506 == distance27,
                    "zero can be extended again without a window or buffer rebuild");
            checkDepth(6956);

            SceneGraph.preCalcFrustrumTable(heights, 1290, 806, 500, 800, false);
            SceneGraph.setResizableDrawDistanceIncrease(15);
            check(SceneGraph.aBooleanArrayArrayArrayArray506 == classic,
                    "fixed mode keeps its classic table after a live setting change");
            checkDepth(3500);
            SceneGraph.preCalcFrustrumTable(heights, 900, 600, 500, 800, true);
            check(SceneGraph.aBooleanArrayArrayArrayArray506[0][0].length == 81
                            && SceneGraph.aBooleanArrayArrayArrayArray506 != distance15
                            && objectVisibilityTables() != classic,
                    "a new viewport invalidates both classic and extended visibility tables");
            checkDepth(5420);
        } finally {
            SceneGraph.setResizableDrawDistanceIncrease(previousDistance);
            SceneGraph.preCalcFrustrumTable(heights, 1290, 806, 500, 800, false);
        }
    }

    private static void visibilityAndDepthLimits() {
        int[] heights = visibilityHeights();
        SceneGraph.preCalcFrustrumTable(heights, 1290, 806, 500, 800);
        boolean[][][][] classic = SceneGraph.aBooleanArrayArrayArrayArray506;
        check(classic[0][0].length == 51, "legacy signature keeps the 25 tile radius");
        checkDepth(3500);

        SceneGraph.preCalcFrustrumTable(heights, 1290, 806, 500, 800, true);
        int extra = Math.max(0, Math.min(SceneGraph.RESIZABLE_TERRAIN_DISTANCE_INCREASE, 27));
        check(SceneGraph.aBooleanArrayArrayArrayArray506[0][0].length == 51 + extra * 2,
                "resizable visibility dimensions follow the configured extra tiles");
        check(Arrays.deepEquals(classic, objectVisibilityTables()),
                "objects and occluders retain every original visibility bit in resizable mode");
        checkDepth(3500 + extra * 128);
        if (extra > 0) {
            check(hasVisibleOuterTerrain(25), "expanded visibility includes actual terrain beyond 25 tiles");
        }
        if (extra > 7) {
            check(hasVisibleOuterTerrain(32), "configured visibility extends beyond the previous 32 tile default");
        }

        SceneGraph.preCalcFrustrumTable(heights, 1290, 806, 500, 800, 0);
        check(Arrays.deepEquals(classic, SceneGraph.aBooleanArrayArrayArrayArray506),
                "zero restores the complete classic visibility table");
        checkDepth(3500);
        SceneGraph.preCalcFrustrumTable(heights, 1290, 806, 500, 800, Integer.MIN_VALUE);
        check(Arrays.deepEquals(classic, SceneGraph.aBooleanArrayArrayArrayArray506),
                "negative increases are disabled");
        SceneGraph.preCalcFrustrumTable(heights, 1290, 806, 500, 800, Integer.MAX_VALUE);
        check(SceneGraph.aBooleanArrayArrayArrayArray506[0][0].length == 105,
                "excessive increases are capped at the 52 tile radius");
        checkDepth(3500 + 27 * 128);
        SceneGraph.preCalcFrustrumTable(heights, 1290, 806, 500, 800, false);
        check(Arrays.deepEquals(classic, SceneGraph.aBooleanArrayArrayArrayArray506),
                "switching back to fixed mode restores every classic visibility bit");
    }

    private static void checkDepth(int farClip) {
        SceneGraph.anInt473 = 0;
        SceneGraph.anInt474 = 65536;
        SceneGraph.anInt475 = 0;
        SceneGraph.anInt476 = 65536;
        check(!SceneGraph.method278(49, 0, 0, 0), "near clip is unchanged");
        check(SceneGraph.method278(50, 0, 0, 0), "near clip boundary remains visible");
        check(SceneGraph.method278(farClip, 0, 0, 0), "far clip boundary remains visible");
        check(!SceneGraph.method278(farClip + 1, 0, 0, 0), "terrain is clipped beyond its far limit");
    }

    private static boolean hasVisibleOuterTerrain(int previousRadius) {
        boolean[][][][] visibility = SceneGraph.aBooleanArrayArrayArrayArray506;
        int radius = (visibility[0][0].length - 1) / 2;
        for (boolean[][][] pitch : visibility) {
            for (boolean[][] yaw : pitch) {
                for (int x = 0; x < yaw.length; x++) {
                    for (int y = 0; y < yaw[x].length; y++) {
                        if ((x - radius < -previousRadius || x - radius >= previousRadius
                                || y - radius < -previousRadius || y - radius >= previousRadius) && yaw[x][y]) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void terrainOnlyTraversal() {
        RecordingScene scene = new RecordingScene();
        int[] offsets = {-41, -40, -39, -33, -32, -31, -26, -25, -24, 0,
                24, 25, 26, 31, 32, 33, 39, 40, 41};
        Map<String, CountingEntity> entities = new HashMap<>();
        for (int offset : offsets) {
            addFixture(scene, 52 + offset, 52, entities);
            if (offset != 0) addFixture(scene, 52, 52 + offset, entities);
        }
        Tile hidden = floor(0, 53, 53);
        hidden.anInt1411 = 1;
        scene.tiles[0][53][53] = hidden;

        configureVisibleTiles(0);
        render(scene, 52, 52);
        Set<String> classicFloors = new HashSet<>(scene.floors);
        Map<String, Integer> classicCalls = new HashMap<>();
        for (Map.Entry<String, CountingEntity> entry : entities.entrySet()) {
            classicCalls.put(entry.getKey(), entry.getValue().calls);
            entry.getValue().calls = 0;
        }
        check(classicFloors.contains("0:27:52") && classicFloors.contains("0:76:52"),
                "classic half-open radius includes -25 and +24");
        check(!classicFloors.contains("0:77:52") && !classicFloors.contains("0:26:52"),
                "classic half-open radius excludes +25 and -26");

        int configuredExtra = Math.max(0, Math.min(SceneGraph.RESIZABLE_TERRAIN_DISTANCE_INCREASE, 27));
        for (boolean configuredDefault : new boolean[] {false, true}) {
            scene.floors.clear();
            for (CountingEntity entity : entities.values()) entity.calls = 0;
            if (configuredDefault) {
                configureDefaultVisibleTiles();
            } else {
                configureVisibleTiles(7);
            }
            int radius = 25 + (configuredDefault ? configuredExtra : 7);
            render(scene, 52, 52);
            check(scene.floors.containsAll(classicFloors), "all original terrain remains present");
            for (int offset : offsets) {
                boolean visible = offset >= -radius && offset < radius;
                check(scene.floors.contains("0:" + (52 + offset) + ":52") == visible
                                && scene.floors.contains("0:52:" + (52 + offset)) == visible,
                        "terrain respects the chosen half-open radius on both axes: " + radius + "/" + offset);
            }
            check(!scene.floors.contains("0:53:53"), "plane visibility still hides restricted tiles");
            check(SceneGraph.anInt461 == 0, "expanded traversal finishes every visible tile");
            for (Map.Entry<String, CountingEntity> entry : entities.entrySet()) {
                check(entry.getValue().calls == classicCalls.get(entry.getKey()),
                        "walls, decorations, piles, bridge objects and entities retain their range: " + entry.getKey());
            }
            check(entities.get("52:52").calls > 0, "the fixture exercises actual non-terrain render calls");
            check(entities.get("91:52").calls == 0, "terrain beyond the previous range contains no object render calls");
        }

        scene.floors.clear();
        configureVisibleTiles(0);
        render(scene, 52, 52);
        check(scene.floors.equals(classicFloors), "returning to classic removes only the additional terrain");
    }

    private static void sceneEdgesAndOccluders() {
        RecordingScene scene = new RecordingScene();
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) scene.tiles[0][x][y] = floor(0, x, y);
        }
        configureVisibleTiles(Integer.MAX_VALUE);
        for (int[] camera : new int[][]{{0, 0}, {103, 0}, {0, 103}, {103, 103}, {52, 52}}) {
            scene.floors.clear();
            render(scene, camera[0], camera[1]);
            check(SceneGraph.anInt461 == 0, "scene edge traversal completes without out-of-bounds access");
            check(!scene.floors.isEmpty(), "scene edges retain visible terrain");
        }

        SceneGraph.method244(-8967, 82 * 128, 300, 82 * 128, 103 * 128, 0, 0, -128, 1);
        SceneGraph.method244(-8967, 0, 300, 103 * 128, 82 * 128, 0, 82 * 128, -128, 2);
        SceneGraph.method244(-8967, 62 * 128, 300, 62 * 128, 103 * 128, 0, 0, -128, 1);
        SceneGraph.method244(-8967, 0, 300, 103 * 128, 62 * 128, 0, 62 * 128, -128, 2);
        SceneGraph.method244(-8967, 0, 300, 103 * 128, 103 * 128, 0, 0, 200, 4);
        configureVisibleTiles(7);
        render(scene, 52, 52);
        check(SceneGraph.anInt490 == 3,
                "near occluders stay active while invisible distant walls cannot hide the new terrain");
        configureDefaultVisibleTiles();
        render(scene, 52, 52);
        check(SceneGraph.anInt490 == 3,
                "the configured default does not activate invisible distant wall occluders");
        configureVisibleTiles(0);
        render(scene, 52, 52);
        check(SceneGraph.anInt490 == 3, "classic occluders retain their original radius");
    }

    private static void configureVisibleTiles(int extraTiles) {
        SceneGraph.preCalcFrustrumTable(visibilityHeights(), 1290, 806, 500, 800, extraTiles);
        fillVisibility(SceneGraph.aBooleanArrayArrayArrayArray506);
        fillVisibility(objectVisibilityTables());
    }

    private static void configureDefaultVisibleTiles() {
        SceneGraph.preCalcFrustrumTable(visibilityHeights(), 1290, 806, 500, 800, true);
        fillVisibility(SceneGraph.aBooleanArrayArrayArrayArray506);
        fillVisibility(objectVisibilityTables());
    }

    private static boolean[][][][] objectVisibilityTables() {
        try {
            Field field = SceneGraph.class.getDeclaredField("objectVisibilityTables");
            field.setAccessible(true);
            return (boolean[][][][]) field.get(null);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("unable to isolate scene traversal from frustum culling", exception);
        }
    }

    private static void fillVisibility(boolean[][][][] visibility) {
        for (boolean[][][] pitch : visibility) {
            for (boolean[][] yaw : pitch) {
                for (boolean[] row : yaw) Arrays.fill(row, true);
            }
        }
    }

    private static int[] visibilityHeights() {
        int[] heights = new int[9];
        for (int index = 0; index < heights.length; index++) {
            int angle = 128 + index * 32 + 15;
            heights[index] = (600 + angle * 3) * ThreeDimensionalCanvas.sineTable[angle] >> 16;
        }
        return heights;
    }

    private static void render(SceneGraph scene, int cameraX, int cameraY) {
        scene.method280(cameraX * 128, 0, 0, -1000, cameraY * 128, 0, 256);
    }

    private static Tile floor(int plane, int x, int y) {
        Tile tile = new Tile(plane, x, y);
        tile.class3 = new ScenegraphMember3(10, 10, 10, 10, -1, 0, false);
        return tile;
    }

    private static void addFixture(RecordingScene scene, int x, int y, Map<String, CountingEntity> entities) {
        CountingEntity entity = new CountingEntity();
        entities.put(x + ":" + y, entity);
        Tile tile = floor(0, x, y);
        tile.aClass44_1403 = wall(entity);
        ScenegraphMember35 decoration = new ScenegraphMember35();
        decoration.aClass50_Sub1_Sub4_608 = entity;
        decoration.anInt606 = 255;
        tile.aClass35_1404 = decoration;
        ScenegraphMember28 groundDecoration = new ScenegraphMember28();
        groundDecoration.aClass50_Sub1_Sub4_570 = entity;
        tile.aClass28_1405 = groundDecoration;
        ScenegraphMember10 pile = new ScenegraphMember10();
        pile.aClass50_Sub1_Sub4_176 = entity;
        pile.aClass50_Sub1_Sub4_177 = entity;
        pile.aClass50_Sub1_Sub4_178 = entity;
        pile.anInt180 = (x & 1) == 0 ? 0 : 1;
        tile.aClass10_1406 = pile;
        tile.class5[0] = object(entity, x, y);
        tile.anInt1407 = 1;
        Tile bridgeBase = floor(0, x, y);
        bridgeBase.aClass44_1403 = wall(entity);
        bridgeBase.class5[0] = object(entity, x, y);
        bridgeBase.anInt1407 = 1;
        tile.aClass50_Sub3_1419 = bridgeBase;
        scene.tiles[0][x][y] = tile;
    }

    private static ScenegraphMember44 wall(Entity entity) {
        ScenegraphMember44 wall = new ScenegraphMember44();
        wall.aClass50_Sub1_Sub4_724 = entity;
        wall.aClass50_Sub1_Sub4_725 = entity;
        wall.anInt722 = 255;
        wall.anInt723 = 255;
        return wall;
    }

    private static ScenegraphRelated5 object(Entity entity, int x, int y) {
        ScenegraphRelated5 object = new ScenegraphRelated5();
        object.entity = entity;
        object.anInt119 = object.anInt120 = x;
        object.anInt121 = object.anInt122 = y;
        return object;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /**
     * Records terrain dispatch without requiring a graphics device or cache.
     *
     * @author lare96
     */
    private static final class RecordingScene extends SceneGraph {
        private final Set<String> floors = new HashSet<>();

        private RecordingScene() {
            super(new int[4][105][105], 104, 4, 104, (byte) 5);
        }

        @Override
        public void method282(ScenegraphMember3 floor, int plane, int sinPitch, int cosPitch,
                              int sinYaw, int cosYaw, int x, int y) {
            floors.add(plane + ":" + x + ":" + y);
        }

        @Override
        public boolean method287(int plane, int x, int y) {
            return false;
        }

        @Override
        public boolean method288(int plane, int x, int y, int orientation) {
            return false;
        }

        @Override
        public boolean method289(int plane, int x, int y, int height) {
            return false;
        }

        @Override
        public boolean method290(int plane, int minX, int maxX, int minY, int maxY, int height) {
            return false;
        }
    }

    /**
     * Counts renderer calls before model projection to catch every terrain-only guard.
     *
     * @author lare96
     */
    private static final class CountingEntity extends Entity {
        private int calls;

        @Override
        public void render(int yaw, int sinPitch, int cosPitch, int sinYaw, int cosYaw,
                           int relativeX, int relativeY, int relativeZ, int modelId) {
            calls++;
        }
    }
}
