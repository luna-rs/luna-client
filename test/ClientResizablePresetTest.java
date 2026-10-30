/**
 * Verifies the native resizable preset and restoring it without changing the selected mode.
 *
 * @author lare96
 */
public final class ClientResizablePresetTest {

    public static void main(String[] args) {
        ClientSettings settings = new ClientSettings();
        check(settings.width == 1280 && settings.height == 800,
                "new resizable settings use the performance preset");
        settings.clientSize = 1;
        settings.width = 2061;
        settings.height = 1172;
        settings.resetResizableSize();
        check(settings.width == 1280 && settings.height == 800 && settings.clientSize == 1,
                "restoring the preset keeps resizable mode selected");
        ClientLayout resized = ClientLayout.create(true, settings.width, settings.height);
        check(resized.viewport.width == 1280 && resized.viewport.height == 800,
                "the world renders at the chosen native resolution");
        check(resized.inventoryBlock.width == 249 && resized.chatboxBlock.width == 516
                        && resized.minimap.width == 172,
                "the preset retains native UI sizes and sharpness");
        settings.clientSize = 0;
        settings.resetResizableSize();
        check(settings.clientSize == 0, "restoring stored size does not switch fixed mode");
        ClientLayout fixed = ClientLayout.create(false, 765, 503);
        check(fixed.viewport.width == 512 && fixed.viewport.height == 334,
                "the fixed viewport retains its classic resolution");
        System.out.println("ClientResizablePresetTest passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
