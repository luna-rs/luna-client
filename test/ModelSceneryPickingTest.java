import java.util.Arrays;

/**
 * Exercises scenery depth and picking limits through the real model rasterizer.
 *
 * @author lare96
 */
public final class ModelSceneryPickingTest {

    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final int EXTENDED_FAR_CLIP = 5420;
    private static final int MODEL_ID = 0x40004001;
    private static final int FACE_COLOR = 0x29b74f;
    private static final int UNPROJECTED = Integer.MIN_VALUE;

    public static void main(String[] args) {
        Drawable.putPixels(WIDTH, HEIGHT, new int[WIDTH * HEIGHT]);
        ThreeDimensionalCanvas.init3D(WIDTH, HEIGHT);
        Model.colorLookupTable[1] = FACE_COLOR;
        Model.mouseX = WIDTH / 2;
        Model.mouseY = HEIGHT / 2 - 8;
        boolean previousPicking = Model.isPickingEnabled;
        Model.isPickingEnabled = true;
        try {
            checkPickingPath(false);
            checkPickingPath(true);
        } finally {
            Model.isPickingEnabled = previousPicking;
        }
        System.out.println("ModelSceneryPickingTest passed");
    }

    private static void checkPickingPath(boolean priorityPicking) {
        Model model = triangle(priorityPicking);
        String path = priorityPicking ? "priority picking" : "face picking";

        renderAndCheck(model, 3000, false, true, true, true, path + ": legacy near model");
        renderAndCheck(model, 3499, false, true, true, true, path + ": legacy depth 3499");
        renderAndCheck(model, 3500, false, true, false, false, path + ": legacy depth 3500");
        renderAndCheck(model, 4000, false, true, false, false, path + ": legacy far model");

        renderAndCheck(model, 3000, true, true, true, true, path + ": extended near model");
        renderAndCheck(model, 3000, true, false, true, false, path + ": explicitly unpickable near model");
        renderAndCheck(model, 3499, true, true, true, true, path + ": extended depth 3499");
        renderAndCheck(model, 3500, true, true, true, false, path + ": extended depth 3500");
        renderAndCheck(model, 4000, true, true, true, false, path + ": extended depth 4000");
        renderAndCheck(model, 5000, true, true, true, false, path + ": extended depth 5000");
        renderAndCheck(model, EXTENDED_FAR_CLIP - 1, true, true, true, false,
                path + ": extended far boundary minus one");
        renderAndCheck(model, EXTENDED_FAR_CLIP, true, true, false, false,
                path + ": extended far boundary");
        renderAndCheck(model, 4000, true, false, true, false,
                path + ": explicitly unpickable far model");
        renderAndCheck(model, 3000, false, true, true, true,
                path + ": legacy picking still works after far and unpickable calls");
        checkEntityForwarding(model, path);
    }

    private static void checkEntityForwarding(Model model, String path) {
        ModelEntity entity = new ModelEntity(model);
        entity.height = -1;
        renderAndCheck(entity, model, 4000, true, 4001, true, true, false,
                path + ": entity forwards a custom extended far clip");
        check(entity.height == model.height, path + ": entity height follows its model");
        check(entity.modelRequests == 1, path + ": entity requests its current model once");

        entity.height = -1;
        renderAndCheck(entity, model, 4000, true, 4000, true, false, false,
                path + ": entity forwards a custom far clip boundary");
        check(entity.height == model.height, path + ": entity updates height even when culled");
        renderAndCheck(entity, model, 3000, true, EXTENDED_FAR_CLIP, false, true, false,
                path + ": entity forwards disabled near picking");
        renderAndCheck(entity, model, 3000, true, EXTENDED_FAR_CLIP, true, true, true,
                path + ": entity forwards enabled near picking");
        renderAndCheck(entity, model, 4000, false, EXTENDED_FAR_CLIP, true, false, false,
                path + ": entity legacy method retains classic depth");
        renderAndCheck(entity, model, 3000, false, EXTENDED_FAR_CLIP, true, true, true,
                path + ": entity legacy method retains near picking after extended calls");
        check(entity.modelRequests == 6, path + ": each entity render resolves its current model");
    }

    private static Model triangle(boolean priorityPicking) {
        Model model = new Model(-1);
        model.verticesCount = 3;
        model.verticesX = new int[]{-220, 0, 220};
        model.verticesY = new int[]{-220, 220, -220};
        model.verticesZ = new int[]{0, 0, 0};
        model.faceCount = 1;
        model.faceIndicesX = new int[]{0};
        model.faceIndicesY = new int[]{1};
        model.faceIndicesZ = new int[]{2};
        model.faceRenderTypes = new int[]{1};
        model.faceColorsA = new int[]{1};
        model.isPriorityPicking = priorityPicking;
        model.calculateRadius();
        return model;
    }

    private static void renderAndCheck(Model model, int depth, boolean extended, boolean allowPicking,
                                       boolean shouldDraw, boolean shouldPick, String message) {
        renderAndCheck(model, model, depth, extended, EXTENDED_FAR_CLIP, allowPicking,
                shouldDraw, shouldPick, message);
    }

    private static void renderAndCheck(Entity entity, Model model, int depth, boolean extended, int farClip,
                                       boolean allowPicking, boolean shouldDraw, boolean shouldPick, String message) {
        Arrays.fill(Drawable.pixels, 0);
        Arrays.fill(Model.projectedX, 0, model.verticesCount, UNPROJECTED);
        Arrays.fill(Model.hoveredModels, -1);
        Model.hoveredCount = 0;
        if (extended) {
            entity.render(0, 0, 65536, 0, 65536, 0, 0, depth, MODEL_ID, farClip, allowPicking);
        } else {
            entity.render(0, 0, 65536, 0, 65536, 0, 0, depth, MODEL_ID);
        }

        int coloredPixels = 0;
        for (int pixel : Drawable.pixels) {
            if (pixel == FACE_COLOR) coloredPixels++;
        }
        check((coloredPixels > 0) == shouldDraw, message + " rasterized pixels");
        for (int vertex = 0; vertex < model.verticesCount; vertex++) {
            int expectedX = shouldDraw ? WIDTH / 2 + (model.verticesX[vertex] << 9) / depth : UNPROJECTED;
            check(Model.projectedX[vertex] == expectedX, message + " vertex " + vertex + " projection");
        }
        check(Model.hoveredCount == (shouldPick ? 1 : 0), message + " hovered count");
        check(Model.hoveredModels[0] == (shouldPick ? MODEL_ID : -1), message + " hovered ID");
        check(Model.isPickingEnabled, message + " leaves the shared picking flag enabled");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /**
     * Supplies a current model through the same inherited render path as animated scenery.
     *
     * @author lare96
     */
    private static final class ModelEntity extends Entity {

        private final Model model;
        private int modelRequests;

        private ModelEntity(Model model) {
            this.model = model;
        }

        @Override
        public Model getModel() {
            modelRequests++;
            return model;
        }
    }
}
