import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
            parallelFailureRecovery();
            titleAnimationCadence();
        } finally {
            client.pulseCycle = previousCycle;
            client.heartbeatCounter = previousHeartbeat;
            client.thisPlayer = previousPlayer;
            SpotAnimation.spotAnimations = previousAnimations;
        }
        System.out.println("ClientRenderTimingTest passed");
    }

    private static void titleAnimationCadence() throws Exception {
        RecordingTitle game = new RecordingTitle();
        BufferedImage image = new BufferedImage(8, 6, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        game.graphics = graphics;
        Field ready = client.class.getDeclaredField("loginFlamesReady");
        ready.setAccessible(true);
        int previousPaints = client.paintCounter1309;
        try {
            game.repaintGame();
            check(game.titleDraws == 1 && game.flameUpdates == 0 && game.flameDraws == 0,
                    "title rendering is safe before flame resources are ready");
            ready.setBoolean(game, true);
            game.repaintGame();
            check(game.titleDraws == 2 && game.flameUpdates == 2 && game.flameDraws == 1,
                    "one title render advances and draws its flames once on the render caller");
            check(game.animationThread == Thread.currentThread(),
                    "title animation runs on the rendering thread");
            game.drawLoginScreen(true);
            check(game.flameUpdates == 2 && game.flameDraws == 1,
                    "immediate connecting messages do not independently advance title animation");
            game.isLoggedIn = true;
            game.repaintGame();
            check(game.gameDraws == 1 && game.flameUpdates == 2 && game.flameDraws == 1,
                    "in-game rendering performs no title animation work");
            game.resetWhenBoolTrue();
            check(!ready.getBoolean(game), "clearing login resources disables further flame drawing");
            game.isLoggedIn = false;
            game.repaintGame();
            check(game.flameUpdates == 2 && game.flameDraws == 1,
                    "cleared flame resources remain untouched until the title is initialized again");
        } finally {
            client.paintCounter1309 = previousPaints;
            graphics.dispose();
        }
    }

    /**
     * Observes scheduled title work without loading title artwork or world resources.
     *
     * @author lare96
     */
    private static final class RecordingTitle extends client {
        private int titleDraws;
        private int gameDraws;
        private int flameUpdates;
        private int flameDraws;
        private Thread animationThread;

        @Override
        public void drawLoginScreen(boolean immediate) {
            titleDraws++;
        }

        @Override
        public void updateLoginFlames(byte unused) {
            flameUpdates++;
            animationThread = Thread.currentThread();
        }

        @Override
        public void renderLoginFlames() {
            flameDraws++;
            animationThread = Thread.currentThread();
        }

        @Override
        public void drawGame() {
            gameDraws++;
        }
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

    private static void parallelFailureRecovery() throws Exception {
        client game = fixture();
        game.layout = ClientLayout.create(true, 1280, 800);
        FailingRasterScene scene = new FailingRasterScene();
        game.sceneGraph = scene;
        int previousThreads = ParallelRasterizer.getThreadCount();
        int previousHovered = Model.hoveredCount;
        try {
            ParallelRasterizer.setThreadCount(4);
            Drawable.putPixels(1280, 800, new int[1280 * 800]);
            ThreeDimensionalCanvas.init3D(1280, 800);
            ThreeDimensionalCanvas.hClip = false;
            ThreeDimensionalCanvas.jagged = true;
            ThreeDimensionalCanvas.anInt1531 = 0;
            Model.hoveredCount = 0;
            Method render = client.class.getDeclaredMethod("renderWorldScene", int.class);
            render.setAccessible(true);
            render.invoke(game, 0);
            boolean parallelAvailable = Runtime.getRuntime().availableProcessors() > 1;
            check(scene.frames == (parallelAvailable ? 2 : 1),
                    "a deferred cache shader failure retries the same world through the serial renderer");
            check(Drawable.pixels[2] == 0x123456 && Drawable.pixels[9] == 0x123456
                            && Drawable.pixels[10] == 0 && Model.hoveredCount == 1,
                    "recovery clears partial pixels and picking before completing the original serial frame");
            check(!ParallelRasterizer.isActive(), "recovery leaves no pending worker writes");
            if (parallelAvailable) check(ParallelRasterizer.getThreadCount() == 1,
                    "a bad cache shader disables parallel drawing for following frames");
        } finally {
            ParallelRasterizer.shutdown();
            ParallelRasterizer.setThreadCount(previousThreads);
            Model.hoveredCount = previousHovered;
        }
    }

    /**
     * Models a bad cached shader that the legacy model renderer catches locally.
     *
     * @author lare96
     */
    private static final class FailingRasterScene extends SceneGraph {
        private int frames;

        private FailingRasterScene() {
            super(new int[4][105][105], 104, 4, 104, (byte) 5);
        }

        @Override
        public void method280(int x, int renderPlane, int unused, int height, int y, int yaw, int pitch) {
            frames++;
            Model.hoveredCount++;
            try {
                ThreeDimensionalCanvas.gouraudRaster(Drawable.pixels, 0, 0, 0, 2, 10,
                        70000 << 8, 70000 << 8);
            } catch (RuntimeException ignored) {
                // The original model renderer skips malformed shader data and continues the scene.
            }
            ThreeDimensionalCanvas.flatRaster(Drawable.pixels, 0, 0x123456, 0, 2, 10);
        }
    }

    private static void performanceCommands() throws Exception {
        client game = fixture();
        Field target = JagApplet.class.getDeclaredField("targetFps");
        target.setAccessible(true);
        int previousDistance = SceneGraph.getResizableDrawDistanceIncrease();
        int previousThreads = ParallelRasterizer.getThreadCount();
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
            for (String command : new String[] {"::drawdistance", "::scenerydistance"}) {
                for (int distance : new int[] {15, 0, 27}) {
                    check(game.handleClientCommand(command + " " + distance),
                            "both draw-distance command names are handled locally");
                    check(SceneGraph.getResizableDrawDistanceIncrease() == distance,
                            "both commands reduce and restore the shared draw distance");
                    check(game.aStringArray1298[0].contains("terrain and scenery"),
                            "the distance feedback identifies both synchronized layers");
                }
                for (String invalid : new String[] {"", "-1", "28", "word"}) {
                    game.handleClientCommand(command + " " + invalid);
                    check(SceneGraph.getResizableDrawDistanceIncrease() == 27,
                            "invalid draw distances retain the previous value");
                }
                check(game.handleClientCommand(command), "missing distance is handled locally");
            }
            check(game.outBuffer.position == 0, "performance commands send no server packets");
            for (int threads : new int[] {0, 1, 2, 4}) {
                check(game.handleClientCommand("::renderthreads " + threads),
                        "render-worker selection is handled locally");
                check(ParallelRasterizer.getThreadCount() == threads,
                        "render-worker selection can enable and disable parallel drawing");
            }
            for (String invalid : new String[] {"", "-1", "5", "word"}) {
                game.handleClientCommand("::renderthreads " + invalid);
                check(ParallelRasterizer.getThreadCount() == 4,
                        "invalid worker counts preserve the existing choice");
            }
            check(game.outBuffer.position == 0, "render-worker commands send no server packets");
            check(!game.handleClientCommand("::unrelated"), "unrelated commands retain normal routing");
        } finally {
            SceneGraph.setResizableDrawDistanceIncrease(previousDistance);
            ParallelRasterizer.setThreadCount(previousThreads);
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
