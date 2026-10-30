import java.util.Arrays;

/**
 * Checks painter ordering, clipped pixels, picking and reusable depth buckets.
 *
 * @author lare96
 */
public final class ModelDepthSortingTest {

    private static final int WIDTH = 128;
    private static final int HEIGHT = 96;
    private static final int MODEL_ID = 0x40004001;
    private static final int[] DEPTHS = {310, 460, 380, 460, 320, 480, 350, 490,
            410, 450, 470, 390, 430, 440, 500, 420};
    private static final int[] PRIORITIES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0, 3, 10, 11};
    private static final int[] DEPTH_ORDER = {14, 5, 10, 1, 3, 9, 13, 12, 15, 8, 11, 6, 4, 0};
    private static final int[] PRIORITY_ORDER = {14, 10, 12, 0, 1, 15, 3, 13, 4, 11, 5, 6, 8, 9};

    public static void main(String[] args) {
        Drawable.putPixels(WIDTH, HEIGHT, new int[WIDTH * HEIGHT]);
        ThreeDimensionalCanvas.init3D(WIDTH, HEIGHT);
        for (int index = 1; index <= DEPTHS.length; index++) {
            Model.colorLookupTable[index] = index * 0x13579 & 0xffffff;
        }
        Model.mouseX = 25;
        Model.mouseY = 25;
        checkFixture(false, false, 383131537);
        checkFixture(true, false, 1413093295);
        checkFixture(false, true, 360076629);
        checkFixture(true, true, 1157897586);
        denseBuckets();
        emptyAndReusedBuckets();
        System.out.println("ModelDepthSortingTest passed");
    }

    private static void checkFixture(boolean priorities, boolean clipped, int expectedPixels) {
        RecordingModel model = fixture(priorities, clipped);
        Arrays.fill(Drawable.pixels, 0);
        Arrays.fill(Model.faceDepthCounts, 7);
        Model.hoveredCount = 0;
        Model.hoveredModels[0] = -1;
        model.processFaces(clipped, true, MODEL_ID);
        check(Arrays.equals(model.order(), priorities ? PRIORITY_ORDER : DEPTH_ORDER),
                "visible faces retain depth ties and priority interleaving");
        check(Model.hoveredCount == 1 && Model.hoveredModels[0] == MODEL_ID,
                "face picking emits the original object exactly once");
        check(Model.faceDepthCounts[2] == 0 && Model.faceDepthCounts[1499] == 0,
                "unused buckets are cleared across the full model range");
        if (clipped) check(Model.faceNeedsClipping[6], "the near-plane face still uses clipping");
        check(Arrays.hashCode(Drawable.pixels) == expectedPixels,
                "rasterized pixels match the original depth traversal");
    }

    private static RecordingModel fixture(boolean priorities, boolean clipped) {
        RecordingModel model = new RecordingModel();
        model.faceCount = DEPTHS.length;
        model.verticesCount = model.faceCount * 3;
        model.modelBoundingSphere = 250;
        model.totalDepthSortingRange = 1500;
        model.faceIndicesX = new int[model.faceCount];
        model.faceIndicesY = new int[model.faceCount];
        model.faceIndicesZ = new int[model.faceCount];
        model.faceRenderTypes = new int[model.faceCount];
        model.faceTransparency = new int[model.faceCount];
        model.faceColorsA = new int[model.faceCount];
        model.faceColorsB = new int[model.faceCount];
        model.faceColorsC = new int[model.faceCount];
        model.facePriorities = priorities ? PRIORITIES.clone() : null;
        for (int face = 0; face < model.faceCount; face++) {
            int vertex = face * 3;
            int x = 12 + face % 4;
            int y = 10 + face % 3;
            model.faceIndicesX[face] = vertex;
            model.faceIndicesY[face] = vertex + 1;
            model.faceIndicesZ[face] = vertex + 2;
            Model.projectedX[vertex] = x;
            Model.projectedX[vertex + 1] = x;
            Model.projectedX[vertex + 2] = x + 35;
            Model.projectedY[vertex] = y;
            Model.projectedY[vertex + 1] = y + 35;
            Model.projectedY[vertex + 2] = y;
            Arrays.fill(Model.projectedZ, vertex, vertex + 3, DEPTHS[face] - model.modelBoundingSphere);
            model.faceRenderTypes[face] = 1;
            model.faceColorsA[face] = face + 1;
            model.faceColorsB[face] = face + 1;
            model.faceColorsC[face] = face + 1;
        }
        model.faceRenderTypes[2] = -1;
        model.faceTransparency[4] = 96;
        model.faceIndicesY[7] = 23;
        model.faceIndicesZ[7] = 22;
        if (clipped) {
            int vertex = 6 * 3;
            Model.projectedX[vertex] = -5000;
            Model.projectedX[vertex + 1] = WIDTH / 2 - 20;
            Model.projectedX[vertex + 2] = WIDTH / 2 + 20;
            Model.projectedY[vertex + 1] = HEIGHT / 2 + 20;
            Model.projectedY[vertex + 2] = HEIGHT / 2 - 20;
            Model.cameraX[vertex] = -4;
            Model.cameraY[vertex] = -4;
            Model.cameraZ[vertex] = 20;
            Model.cameraX[vertex + 1] = -4;
            Model.cameraY[vertex + 1] = 4;
            Model.cameraZ[vertex + 1] = 100;
            Model.cameraX[vertex + 2] = 4;
            Model.cameraY[vertex + 2] = -4;
            Model.cameraZ[vertex + 2] = 100;
        }
        return model;
    }

    private static void emptyAndReusedBuckets() {
        for (boolean priorities : new boolean[] {false, true}) {
            RecordingModel model = fixture(priorities, false);
            model.processFaces(false, false, 0);
            model.reset();
            model.faceCount = 0;
            Arrays.fill(Model.priorityCounts, 5);
            model.processFaces(false, false, 0);
            check(model.order().length == 0, "an empty model never draws faces from the previous model");
            check(Arrays.equals(Model.faceDepthCounts, new int[1500]),
                    "empty models still clear every reusable depth bucket");
            if (priorities) check(Arrays.equals(Model.priorityCounts, new int[12]),
                    "empty priority models still clear every priority bucket");
            model.totalDepthSortingRange = 200;
            Model.faceDepthCounts[1400] = 1;
            model.processFaces(false, false, 0);
            check(Model.faceDepthCounts[1400] == 1, "a smaller model does not clear outside its depth range");
            model.totalDepthSortingRange = 1500;
            model.processFaces(false, false, 0);
            check(Model.faceDepthCounts[1400] == 0 && model.order().length == 0,
                    "a following wide model clears stale buckets before traversing them");
        }
    }

    private static void denseBuckets() {
        for (boolean priorities : new boolean[] {false, true}) {
            RecordingModel model = new RecordingModel();
            model.faceCount = 256;
            model.totalDepthSortingRange = 500;
            model.modelBoundingSphere = 250;
            model.faceIndicesX = new int[model.faceCount];
            model.faceIndicesY = new int[model.faceCount];
            model.faceIndicesZ = new int[model.faceCount];
            model.faceColorsA = new int[model.faceCount];
            model.faceRenderTypes = new int[model.faceCount];
            Arrays.fill(model.faceIndicesY, 1);
            Arrays.fill(model.faceIndicesZ, 2);
            Arrays.fill(model.faceColorsA, 1);
            Arrays.fill(model.faceRenderTypes, 1);
            if (priorities) {
                model.facePriorities = new int[model.faceCount];
                Arrays.fill(model.facePriorities, 5);
            }
            Model.projectedX[0] = 20;
            Model.projectedX[1] = 20;
            Model.projectedX[2] = 60;
            Model.projectedY[0] = 20;
            Model.projectedY[1] = 60;
            Model.projectedY[2] = 20;
            Arrays.fill(Model.projectedZ, 0, 3, 0);
            model.processFaces(false, false, 0);
            int[] expected = new int[model.faceCount];
            for (int face = 0; face < expected.length; face++) expected[face] = face;
            check(Arrays.equals(model.order(), expected), "a dense depth bucket retains every insertion tie");
            check(Drawable.pixels[30 * WIDTH + 30] == Model.colorLookupTable[1],
                    "dense models still rasterize their final ordered faces");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /**
     * Observes actual raster dispatch order while keeping the original face renderer.
     *
     * @author lare96
     */
    private static final class RecordingModel extends Model {
        private final int[] faces = new int[512];
        private int count;

        private RecordingModel() {
            super(-1);
        }

        @Override
        public void drawFace(int face) {
            faces[count++] = face;
            super.drawFace(face);
        }

        private int[] order() {
            return Arrays.copyOf(faces, count);
        }

        private void reset() {
            count = 0;
        }
    }
}
