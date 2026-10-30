import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Verifies extended scenery traversal with classic picking and actor admission.
 *
 * @author lare96
 */
public final class CachedSceneryDistanceTest {

    private static final int CACHE_UID = 0x40004000;
    private static final int ACTOR_UID = 0x20004000;
    private static final int CAMERA = 52;

    public static void main(String[] args) {
        sceneryPathsAndBoundaries();
        visibilityAndCameraChanges();
        sharedFootprintsAndDeferredWalls();
        liveSceneryDistance();
        outerFootprintAdmission();
        System.out.println("CachedSceneryDistanceTest passed");
    }

    private static void sceneryPathsAndBoundaries() {
        RecordingScene scene = new RecordingScene();
        List<Fixture> fixtures = new ArrayList<>();
        for (int offset : new int[]{-40, -26, -25, 0, 24, 25, 39, 40}) {
            fixtures.add(addFixture(scene, CAMERA + offset, CAMERA));
            if (offset != 0) fixtures.add(addFixture(scene, CAMERA, CAMERA + offset));
        }
        configure(0, true);
        render(scene, CAMERA, CAMERA);
        for (Fixture fixture : fixtures) {
            boolean classic = classicOffset(fixture.x - CAMERA) && classicOffset(fixture.y - CAMERA);
            fixture.checkCalls(classic, classic, false);
            fixture.reset();
        }

        configure(15, true);
        render(scene, CAMERA, CAMERA);
        for (Fixture fixture : fixtures) {
            boolean visible = fixture.x - CAMERA >= -40 && fixture.x - CAMERA < 40
                    && fixture.y - CAMERA >= -40 && fixture.y - CAMERA < 40;
            boolean classic = classicOffset(fixture.x - CAMERA) && classicOffset(fixture.y - CAMERA);
            fixture.checkCalls(visible, classic, true);
            fixture.reset();
        }
        check(SceneGraph.anInt461 == 0, "all scenery and terrain tiles finish traversal");

        configure(0, true);
        render(scene, CAMERA, CAMERA);
        for (Fixture fixture : fixtures) {
            boolean classic = classicOffset(fixture.x - CAMERA) && classicOffset(fixture.y - CAMERA);
            fixture.checkCalls(classic, classic, false);
        }
    }

    private static void visibilityAndCameraChanges() {
        RecordingScene scene = new RecordingScene();
        Fixture fixture = addFixture(scene, 76, 52);
        configure(15, false);
        render(scene, CAMERA, CAMERA);
        fixture.checkCalls(true, false, true);
        fixture.reset();

        scene.anIntArrayArrayArray455[0][76][52] = 2000;
        render(scene, CAMERA, CAMERA);
        fixture.checkCalls(true, true, true);
        fixture.reset();

        scene.anIntArrayArrayArray455[0][76][52] = 0;
        configure(15, true);
        render(scene, 51, CAMERA);
        fixture.checkCalls(true, false, true);
        fixture.reset();
        render(scene, CAMERA, CAMERA);
        fixture.checkCalls(true, true, true);
        fixture.reset();

        scene.tiles[0][76][52].anInt1411 = 1;
        render(scene, CAMERA, CAMERA);
        fixture.checkCalls(false, false, true);
    }

    private static void sharedFootprintsAndDeferredWalls() {
        RecordingScene scene = new RecordingScene();
        RecordingEntity leftCache = new RecordingEntity();
        RecordingEntity rightCache = new RecordingEntity();
        RecordingEntity crossingActor = new RecordingEntity();
        RecordingEntity farActor = new RecordingEntity();
        addObject(scene, leftCache, CACHE_UID, 26, 28, 52);
        addObject(scene, rightCache, CACHE_UID, 76, 78, 52);
        addObject(scene, crossingActor, ACTOR_UID, 76, 78, 52);
        addObject(scene, farActor, ACTOR_UID, 78, 78, 52);
        Fixture farWall = addFixture(scene, 78, 52);
        scene.tiles[0][78][52].aClass44_1403.anInt722 = 16;
        configure(15, true);
        render(scene, CAMERA, CAMERA);
        check(leftCache.extendedCalls == 1 && leftCache.lastPicking,
                "a cache anchor outside the classic window stays pickable through its visible footprint");
        check(rightCache.extendedCalls == 1 && rightCache.lastPicking,
                "far-first traversal preserves picking through the near end of a shared footprint");
        check(crossingActor.classicCalls == 1 && crossingActor.extendedCalls == 0,
                "a far traversal tile does not consume the shared actor's classic render");
        check(farActor.classicCalls == 0 && farActor.extendedCalls == 0,
                "a wholly distant actor is excluded even when it shares a cache scenery tile");
        check(farWall.wall.extendedCalls > 0 && !farWall.wall.lastPicking,
                "deferred far walls render without waiting for excluded actors");
        check(SceneGraph.anInt461 == 0, "shared objects and deferred walls do not stall traversal");

        leftCache.reset();
        rightCache.reset();
        configure(15, false);
        render(scene, CAMERA, CAMERA);
        check(leftCache.extendedCalls == 1 && !leftCache.lastPicking
                        && rightCache.extendedCalls == 1 && !rightCache.lastPicking,
                "shared cache objects lose picking when every classic footprint tile is outside the old frustum");
    }

    private static Fixture addFixture(RecordingScene scene, int x, int y) {
        Tile tile = scene.tiles[0][x][y];
        if (tile == null) scene.tiles[0][x][y] = tile = new Tile(0, x, y);
        tile.class3 = new ScenegraphMember3(10, 10, 10, 10, -1, 0, false);
        Fixture fixture = new Fixture(x, y);
        tile.aClass44_1403 = wall(fixture.wall, x, y);
        ScenegraphMember35 decoration = new ScenegraphMember35();
        decoration.aClass50_Sub1_Sub4_608 = fixture.decoration;
        decoration.anInt604 = x * 128;
        decoration.anInt605 = y * 128;
        decoration.anInt606 = 255;
        decoration.anInt609 = CACHE_UID;
        tile.aClass35_1404 = decoration;
        ScenegraphMember28 ground = new ScenegraphMember28();
        ground.aClass50_Sub1_Sub4_570 = fixture.ground;
        ground.anInt568 = x * 128;
        ground.anInt569 = y * 128;
        ground.anInt571 = CACHE_UID | Integer.MIN_VALUE;
        tile.aClass28_1405 = ground;
        ScenegraphMember10 pile = new ScenegraphMember10();
        pile.aClass50_Sub1_Sub4_176 = fixture.pile;
        pile.anInt180 = x & 1;
        tile.aClass10_1406 = pile;
        addObject(scene, fixture.object, CACHE_UID, x, x, y);
        addObject(scene, fixture.actor, ACTOR_UID, x, x, y);
        Tile bridge = new Tile(0, x, y);
        bridge.aClass44_1403 = wall(fixture.bridge, x, y);
        ScenegraphRelated5 bridgeObject = new ScenegraphRelated5();
        bridgeObject.entity = fixture.bridge;
        bridgeObject.anInt125 = CACHE_UID;
        bridgeObject.anInt115 = x * 128;
        bridgeObject.anInt116 = y * 128;
        bridgeObject.anInt119 = bridgeObject.anInt120 = x;
        bridgeObject.anInt121 = bridgeObject.anInt122 = y;
        bridge.class5[0] = bridgeObject;
        bridge.anInt1407 = 1;
        tile.aClass50_Sub3_1419 = bridge;
        return fixture;
    }

    private static ScenegraphMember44 wall(Entity entity, int x, int y) {
        ScenegraphMember44 wall = new ScenegraphMember44();
        wall.aClass50_Sub1_Sub4_724 = wall.aClass50_Sub1_Sub4_725 = entity;
        wall.anInt720 = x * 128;
        wall.anInt721 = y * 128;
        wall.anInt722 = wall.anInt723 = 255;
        wall.uid = CACHE_UID;
        return wall;
    }

    private static void addObject(RecordingScene scene, Entity entity, int uid, int minX, int maxX, int y) {
        check(scene.method254(0, minX, y, maxX - minX + 1, 1,
                        minX * 128, y * 128, 0, entity, 0, false, uid, (byte) 0),
                "fixture object fits into the scene");
    }

    private static void configure(int extra, boolean classicVisible) {
        int[] heights = new int[9];
        for (int index = 0; index < heights.length; index++) {
            int angle = 128 + index * 32 + 15;
            heights[index] = (600 + angle * 3) * ThreeDimensionalCanvas.sineTable[angle] >> 16;
        }
        SceneGraph.preCalcFrustrumTable(heights, 1290, 806, 500, 800, extra);
        fill(SceneGraph.aBooleanArrayArrayArrayArray506, true);
        try {
            Field field = SceneGraph.class.getDeclaredField("objectVisibilityTables");
            field.setAccessible(true);
            fill((boolean[][][][]) field.get(null), classicVisible);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static void fill(boolean[][][][] tables, boolean visible) {
        for (boolean[][][] pitch : tables)
            for (boolean[][] yaw : pitch)
                for (boolean[] row : yaw) Arrays.fill(row, visible);
    }

    private static boolean classicOffset(int offset) {
        return offset >= -25 && offset < 25;
    }

    private static void liveSceneryDistance() {
        int previousDistance = SceneGraph.getResizableSceneryDistanceIncrease();
        try {
            SceneGraph.setResizableSceneryDistanceIncrease(27);
            RecordingScene scene = new RecordingScene();
            List<Fixture> fixtures = new ArrayList<>();
            for (int offset : new int[] {-50, -41, -39, -26, -25, 0, 24, 25, 39, 41, 50}) {
                fixtures.add(addFixture(scene, CAMERA + offset, CAMERA));
                if (offset != 0) fixtures.add(addFixture(scene, CAMERA, CAMERA + offset));
            }
            scene.tiles[0][CAMERA + 41][CAMERA].aClass44_1403.anInt722 = 16;
            RecordingEntity crossingCache = new RecordingEntity();
            addObject(scene, crossingCache, CACHE_UID, 26, 28, CAMERA);
            RecordingEntity negativeBoundary = new RecordingEntity();
            RecordingEntity positiveBoundary = new RecordingEntity();
            addObject(scene, negativeBoundary, CACHE_UID | Integer.MIN_VALUE, 12, 12, CAMERA);
            addObject(scene, positiveBoundary, CACHE_UID, 92, 92, CAMERA);
            configure(27, true);
            boolean[][][][] terrainTables = SceneGraph.aBooleanArrayArrayArrayArray506;
            for (int distance : new int[] {27, 15, 0, 27}) {
                SceneGraph.setResizableSceneryDistanceIncrease(distance);
                check(SceneGraph.getResizableSceneryDistanceIncrease() == distance,
                        "scenery distance can change during an active scene");
                for (Fixture fixture : fixtures) fixture.reset();
                crossingCache.reset();
                negativeBoundary.reset();
                positiveBoundary.reset();
                scene.floorCalls = 0;
                render(scene, CAMERA, CAMERA);
                for (Fixture fixture : fixtures) {
                    boolean classic = classicOffset(fixture.x - CAMERA) && classicOffset(fixture.y - CAMERA);
                    int radius = 25 + distance;
                    boolean visible = fixture.x - CAMERA >= -radius && fixture.x - CAMERA < radius
                            && fixture.y - CAMERA >= -radius && fixture.y - CAMERA < radius;
                    fixture.checkCalls(visible, classic, distance > 0, 3500 + distance * 128);
                }
                check(scene.floorCalls == fixtures.size(), "scenery distance does not shorten terrain traversal");
                check(SceneGraph.aBooleanArrayArrayArrayArray506 == terrainTables
                                && terrainTables[0][0].length == 105 && terrainFarClip() == 6956,
                        "live scenery changes preserve terrain visibility tables and depth");
                check(SceneGraph.anInt461 == 0, "reduced scenery distance still resolves deferred walls");
                if (distance > 0) {
                    check(crossingCache.extendedCalls == 1 && crossingCache.lastPicking,
                            "shorter scenery distance preserves shared classic footprint picking");
                    check(negativeBoundary.extendedCalls == 1 && !negativeBoundary.lastPicking,
                            "the signed cache UID renders at the inclusive negative scenery boundary");
                } else {
                    check(crossingCache.classicCalls == 1 && crossingCache.extendedCalls == 0,
                            "zero extension retains classic objects whose anchor lies outside the classic window");
                    check(negativeBoundary.classicCalls + negativeBoundary.extendedCalls == 0,
                            "zero extension excludes wholly distant scenery");
                }
                check((positiveBoundary.extendedCalls > 0) == (distance == 27),
                        "the positive scenery boundary remains half-open");
            }

            for (Fixture fixture : fixtures) fixture.reset();
            configure(0, true);
            render(scene, CAMERA, CAMERA);
            for (Fixture fixture : fixtures) {
                boolean classic = classicOffset(fixture.x - CAMERA) && classicOffset(fixture.y - CAMERA);
                fixture.checkCalls(classic, classic, false);
            }
            for (Fixture fixture : fixtures) fixture.reset();
            configure(15, true);
            render(scene, CAMERA, CAMERA);
            for (Fixture fixture : fixtures) {
                boolean classic = classicOffset(fixture.x - CAMERA) && classicOffset(fixture.y - CAMERA);
                boolean visible = fixture.x - CAMERA >= -40 && fixture.x - CAMERA < 40
                        && fixture.y - CAMERA >= -40 && fixture.y - CAMERA < 40;
                fixture.checkCalls(visible, classic, true);
            }
            SceneGraph.setResizableSceneryDistanceIncrease(Integer.MAX_VALUE);
            check(SceneGraph.getResizableSceneryDistanceIncrease() == 27, "excessive scenery increases are capped");
            SceneGraph.setResizableSceneryDistanceIncrease(Integer.MIN_VALUE);
            check(SceneGraph.getResizableSceneryDistanceIncrease() == 0, "negative scenery increases are disabled");
        } finally {
            SceneGraph.setResizableSceneryDistanceIncrease(previousDistance);
        }
    }

    private static int terrainFarClip() {
        try {
            Field field = SceneGraph.class.getDeclaredField("terrainFarClip");
            field.setAccessible(true);
            return field.getInt(null);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static void outerFootprintAdmission() {
        int previousDistance = SceneGraph.getResizableSceneryDistanceIncrease();
        try {
            RecordingScene scene = new RecordingScene();
            RecordingEntity crossingCache = new RecordingEntity();
            check(scene.method254(0, 81, CAMERA, 4, 1, 82 * 128, CAMERA * 128, 0,
                            crossingCache, 0, false, CACHE_UID, (byte) 0),
                    "outer footprint fixture fits the loaded scene");
            configure(27, true);
            for (int distance : new int[] {27, 15, 27}) {
                SceneGraph.setResizableSceneryDistanceIncrease(distance);
                crossingCache.reset();
                render(scene, 30, CAMERA);
                check(crossingCache.extendedCalls == (distance == 27 ? 1 : 0),
                        "full scenery distance admits an outer footprint even when its centre lies beyond the window");
                check(!crossingCache.lastPicking && crossingCache.classicCalls == 0,
                        "outer footprint admission never expands classic interaction");
                check(SceneGraph.anInt461 == 0, "outer footprint objects do not stall reduced scenery traversal");
            }
        } finally {
            SceneGraph.setResizableSceneryDistanceIncrease(previousDistance);
        }
    }

    private static void render(RecordingScene scene, int x, int y) {
        scene.method280(x * 128, 0, 0, -1000, y * 128, 0, 256);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /**
     * Holds every scenery path alongside actors and ground items on one tile.
     *
     * @author lare96
     */
    private static final class Fixture {
        private final int x;
        private final int y;
        private final RecordingEntity wall = new RecordingEntity();
        private final RecordingEntity decoration = new RecordingEntity();
        private final RecordingEntity ground = new RecordingEntity();
        private final RecordingEntity object = new RecordingEntity();
        private final RecordingEntity bridge = new RecordingEntity();
        private final RecordingEntity pile = new RecordingEntity();
        private final RecordingEntity actor = new RecordingEntity();

        private Fixture(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private void checkCalls(boolean visible, boolean classic, boolean extended) {
            checkCalls(visible, classic, extended, 5420);
        }

        private void checkCalls(boolean visible, boolean classic, boolean extended, int farClip) {
            for (RecordingEntity scenery : new RecordingEntity[]{wall, decoration, ground, object, bridge}) {
                check((scenery.classicCalls + scenery.extendedCalls > 0) == visible,
                        "scenery visibility at " + x + ":" + y);
                if (visible) {
                    check((scenery.extendedCalls > 0) == extended,
                            "fixed and disabled modes preserve the original virtual renderer");
                    if (extended) {
                        check(scenery.lastPicking == classic, "scenery keeps classic picking at " + x + ":" + y);
                        check(scenery.lastFarClip == farClip, "scenery receives its configured visual depth");
                    }
                }
            }
            check((pile.classicCalls > 0) == classic && pile.extendedCalls == 0,
                    "ground items keep classic visibility at " + x + ":" + y);
            check((actor.classicCalls > 0) == classic && actor.extendedCalls == 0,
                    "actors keep classic visibility at " + x + ":" + y);
        }

        private void reset() {
            for (RecordingEntity entity : new RecordingEntity[]{wall, decoration, ground, object, bridge, pile, actor})
                entity.reset();
        }
    }

    /**
     * Isolates scene ordering and admission from rasterization and occlusion caches.
     *
     * @author lare96
     */
    private static final class RecordingScene extends SceneGraph {
        private int floorCalls;
        private RecordingScene() {
            super(new int[4][105][105], 104, 4, 104, (byte) 5);
        }

        @Override
        public void method282(ScenegraphMember3 floor, int plane, int sinPitch, int cosPitch,
                              int sinYaw, int cosYaw, int x, int y) {
            floorCalls++;
        }

        @Override
        public boolean method287(int plane, int x, int y) { return false; }

        @Override
        public boolean method288(int plane, int x, int y, int orientation) { return false; }

        @Override
        public boolean method289(int plane, int x, int y, int height) { return false; }

        @Override
        public boolean method290(int plane, int minX, int maxX, int minY, int maxY, int height) { return false; }
    }

    /**
     * Records which virtual renderer and picking eligibility each path receives.
     *
     * @author lare96
     */
    private static final class RecordingEntity extends Entity {
        private int classicCalls;
        private int extendedCalls;
        private boolean lastPicking;
        private int lastFarClip;

        @Override
        public void render(int yaw, int sinPitch, int cosPitch, int sinYaw, int cosYaw,
                           int relativeX, int relativeY, int relativeZ, int uid) {
            classicCalls++;
        }

        @Override
        public void render(int yaw, int sinPitch, int cosPitch, int sinYaw, int cosYaw,
                           int relativeX, int relativeY, int relativeZ, int uid, int farClip, boolean picking) {
            extendedCalls++;
            lastPicking = picking;
            lastFarClip = farClip;
        }

        private void reset() {
            classicCalls = extendedCalls = 0;
            lastPicking = false;
            lastFarClip = 0;
        }
    }
}
