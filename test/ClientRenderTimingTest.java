import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Verifies that extra rendered frames do not advance game effects or packet timers.
 *
 * @author lare96
 */
public final class ClientRenderTimingTest {

    public static void main(String[] args) throws Exception {
        int previousCycle = client.pulseCycle;
        int previousHeartbeat = client.heartbeatCounter;
        Player previousPlayer = client.thisPlayer;
        SpotAnimation[] previousAnimations = SpotAnimation.spotAnimations;
        try {
            heartbeatCadence();
            roofHeartbeatCadence();
            projectilePresentation();
            cameraShakeCadence();
            scrollbarDragCadence();
            unchangedTextures();
            performanceCommands();
        } finally {
            client.pulseCycle = previousCycle;
            client.heartbeatCounter = previousHeartbeat;
            client.thisPlayer = previousPlayer;
            SpotAnimation.spotAnimations = previousAnimations;
        }
        System.out.println("ClientRenderTimingTest passed");
    }

    private static void roofHeartbeatCadence() {
        client game = fixture();
        game.aByteArrayArrayArray1125 = new byte[4][104][104];
        client.thisPlayer = new Player();
        client.thisPlayer.unitX = 256;
        client.thisPlayer.unitY = 128;
        game.anInt1216 = 128;
        game.anInt1218 = 128;
        game.anInt1219 = 128;
        game.anInt978 = 1457;
        game.anInt951 = 0;
        for (int frame = 0; frame < 10; frame++) game.method117((byte) 1);
        check(game.anInt978 == 1457 && game.outBuffer.position == 0,
                "extra frames do not accelerate the roof heartbeat");
        game.anInt951 = 1;
        game.method117((byte) 1);
        check(game.anInt978 == 0 && game.outBuffer.position > 0,
                "roof heartbeat still advances with game ticks");
    }

    private static client fixture() {
        client game = new client();
        game.outBuffer.random = new IsaacRandom(new int[4]);
        return game;
    }

    private static void performanceCommands() throws Exception {
        client game = fixture();
        Field target = JagApplet.class.getDeclaredField("targetFps");
        target.setAccessible(true);
        int previousDistance = SceneGraph.getResizableSceneryDistanceIncrease();
        try {
            for (int rate : new int[] {60, 120, 240}) {
                check(game.handleClientCommand("::fpslimit " + rate), "render limits are handled locally");
                check(target.getInt(game) == rate && game.delayTime == 20,
                        "live render limits preserve the fixed game update rate");
            }
            for (String invalid : new String[] {"", "0", "1001", "word"}) {
                game.handleClientCommand("::fpslimit " + invalid);
                check(target.getInt(game) == 240, "invalid render limits retain the previous target");
            }
            for (int distance : new int[] {15, 0, 27}) {
                check(game.handleClientCommand("::scenerydistance " + distance),
                        "scenery limits are handled locally");
                check(SceneGraph.getResizableSceneryDistanceIncrease() == distance,
                        "live scenery limits can be reduced and restored");
            }
            for (String invalid : new String[] {"", "-1", "28", "word"}) {
                game.handleClientCommand("::scenerydistance " + invalid);
                check(SceneGraph.getResizableSceneryDistanceIncrease() == 27,
                        "invalid scenery limits retain the previous distance");
            }
            check(game.outBuffer.position == 0, "performance commands send no server packets");
            check(!game.handleClientCommand("::unrelated"), "unrelated commands retain normal routing");
        } finally {
            SceneGraph.setResizableSceneryDistanceIncrease(previousDistance);
        }
    }

    private static void heartbeatCadence() {
        client game = fixture();
        client.heartbeatCounter = 0;
        for (int tick = 0; tick < 51; tick++) {
            game.anInt951 = 1;
            game.updateProjectiles();
            for (int frame = 0; frame < 3; frame++) {
                game.anInt951 = 0;
                game.updateProjectiles();
            }
        }
        check(client.heartbeatCounter == 51 && game.outBuffer.position == 0,
                "extra frames do not accelerate the projectile heartbeat");
        game.anInt951 = 1;
        game.updateProjectiles();
        check(client.heartbeatCounter == 0 && game.outBuffer.position == 1,
                "heartbeat is emitted after the original 52 game ticks");
    }

    private static void projectilePresentation() {
        client game = fixture();
        RecordingScene scene = new RecordingScene();
        game.sceneGraph = scene;
        game.intGroundArray = new int[4][105][105];
        game.aByteArrayArrayArray1125 = new byte[4][104][104];
        game.npcs[0] = new Npc();
        game.npcs[0].unitX = 1024;
        game.npcs[0].unitY = 1024;
        SpotAnimation.spotAnimations = new SpotAnimation[] {new SpotAnimation()};
        RecordingProjectile projectile = new RecordingProjectile();
        game.projectileQueue.addLast(projectile);
        client.pulseCycle = 10;
        game.anInt951 = 1;
        game.updateProjectiles();
        double[] state = projectileState(projectile);
        game.anInt951 = 0;
        for (int frame = 0; frame < 10; frame++) game.updateProjectiles();
        check(Arrays.equals(state, projectileState(projectile)),
                "extra frames preserve projectile position, velocity and acceleration");
        check(projectile.targetUpdates == 1 && projectile.elapsedTicks == 1 && scene.presentations == 11,
                "extra frames present the projectile without retargeting or advancing it");
        client.pulseCycle += 2;
        game.anInt951 = 2;
        game.updateProjectiles();
        check(projectile.targetUpdates == 2 && projectile.elapsedTicks == 3,
                "projectiles consume all elapsed game ticks after a slower frame");
    }

    private static double[] projectileState(Projectile projectile) {
        return new double[] {projectile.aDouble1555, projectile.aDouble1556, projectile.aDouble1557,
                projectile.aDouble1569, projectile.aDouble1570, projectile.aDouble1571,
                projectile.aDouble1572, projectile.aDouble1574, projectile.anInt1562, projectile.anInt1563};
    }

    private static void cameraShakeCadence() throws Exception {
        client game = fixture();
        Arrays.fill(game.customCameraActive, true);
        Arrays.fill(game.cameraJitter, 20);
        client.pulseCycle = 100;
        Field offsets = client.class.getDeclaredField("cameraShakeOffsets");
        offsets.setAccessible(true);
        game.updateCameraShake();
        int[] original = ((int[]) offsets.get(game)).clone();
        for (int frame = 0; frame < 40; frame++) {
            game.updateCameraShake();
            check(Arrays.equals(original, (int[]) offsets.get(game)),
                    "camera shake remains identical between game ticks");
        }
        Arrays.fill(game.customCameraActive, false);
        client.pulseCycle++;
        game.updateCameraShake();
        check(Arrays.equals(new int[5], (int[]) offsets.get(game)),
                "the next game tick refreshes camera effects");
    }

    private static void unchangedTextures() {
        client game = fixture();
        game.anInt951 = 0;
        byte[] scratch = game.aByteArray1245;
        game.animateTexture_65(0);
        check(game.aByteArray1245 == scratch,
                "zero-tick frames do not copy or invalidate animated textures");
    }

    private static void scrollbarDragCadence() {
        client game = fixture();
        JagInterface scrollbar = new JagInterface();
        scrollbar.anInt231 = 100;
        game.anInt21 = 1;
        game.anInt1094 = 1;
        game.method42(1000, 0, scrollbar, (byte) 102, 38, 2, 105, 100, 100);
        check(game.aBoolean1127, "holding a scrollbar thumb starts a drag");
        int position = scrollbar.anInt231;
        game.anInt1094 = 0;
        game.method42(1000, 0, scrollbar, (byte) 102, 42, 2, 124, 100, 100);
        check(game.aBoolean1127 && scrollbar.anInt231 == position,
                "extra frames retain the thumb drag without moving its position");
        game.anInt1094 = 1;
        game.method42(1000, 0, scrollbar, (byte) 102, 42, 2, 124, 100, 100);
        check(game.aBoolean1127 && scrollbar.anInt231 != position,
                "the next game tick still accepts dragging beside the narrow rail");
        game.anInt21 = 0;
        game.anInt1094 = 0;
        game.method42(1000, 0, scrollbar, (byte) 102, 42, 2, 124, 100, 100);
        check(!game.aBoolean1127, "releasing the mouse clears the drag on an extra frame");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /**
     * Counts projectile presentation independently of loaded map data.
     *
     * @author lare96
     */
    private static final class RecordingScene extends SceneGraph {
        private int presentations;

        private RecordingScene() {
            super(new int[4][105][105], 104, 4, 104, (byte) 5);
        }

        @Override
        public boolean method252(int i, Entity entity, int j, int k, boolean flag, int l,
                                 int plane, int radius, int y, int rotation) {
            presentations++;
            return true;
        }
    }

    /**
     * Observes real projectile physics without constructing a render model.
     *
     * @author lare96
     */
    private static final class RecordingProjectile extends Projectile {
        private int targetUpdates;
        private int elapsedTicks;

        private RecordingProjectile() {
            super(0, 0, 0, 512, 0, 100, 0, 1, 0, 512, 0);
        }

        @Override
        public void trackTarget(int x, int y, int height, int cycle) {
            targetUpdates++;
            super.trackTarget(x, y, height, cycle);
        }

        @Override
        public void method563(int ticks, boolean flag) {
            elapsedTicks += ticks;
            super.method563(ticks, flag);
        }
    }
}
