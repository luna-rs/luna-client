package luna;

/**
 * Keeps every default-title flame cycle in the violet family while retaining the seasonal fire colors.
 *
 * @author lare96
 */
public final class TitleFlamePalette {
    private TitleFlamePalette() {
    }

    /**
     * Fills the three intensity ramps consumed by the original flame simulation.
     */
    public static void fill(boolean lunar, int[] primary, int[] alternate, int[] third) {
        if (lunar) {
            ramp(primary, new int[] {0, 0x28105b, 0x7c3aed, 0xdccaff, 0xfff5ff});
            ramp(alternate, new int[] {0, 0x141349, 0x6154e8, 0xc8bdff, 0xf5f2ff});
            ramp(third, new int[] {0, 0x330b46, 0xa849f4, 0xe6beff, 0xfff2ff});
            return;
        }
        for (int i = 0; i < 64; i++) {
            primary[i] = i * 0x40000;
            primary[i + 64] = 0xff0000 + 1024 * i;
            primary[i + 128] = 0xffff00 + 4 * i;
            primary[i + 192] = 0xffffff;
            alternate[i] = i * 1024;
            alternate[i + 64] = 65280 + 4 * i;
            alternate[i + 128] = 65535 + 0x40000 * i;
            alternate[i + 192] = 0xffffff;
            third[i] = i * 4;
            third[i + 64] = 255 + 0x40000 * i;
            third[i + 128] = 0xff00ff + 1024 * i;
            third[i + 192] = 0xffffff;
        }
    }

    private static void ramp(int[] destination, int[] stops) {
        for (int segment = 0; segment < 4; segment++) {
            int from = stops[segment], to = stops[segment + 1];
            for (int i = 0; i < 64; i++) {
                int red = ((from >> 16 & 255) * (63 - i) + (to >> 16 & 255) * i) / 63;
                int green = ((from >> 8 & 255) * (63 - i) + (to >> 8 & 255) * i) / 63;
                int blue = ((from & 255) * (63 - i) + (to & 255) * i) / 63;
                destination[segment * 64 + i] = red << 16 | green << 8 | blue;
            }
        }
    }
}
