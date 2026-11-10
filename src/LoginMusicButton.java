import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Draws the rounded title-screen music control in the classic stone style.
 *
 * @author lare96
 */
public final class LoginMusicButton {
    public static final int SIZE = 36;
    private static final String SYMBOLS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_";
    private static final int[] PALETTE = {
        0xc2c1bf, 0xb1b1b2, 0xb0afb1, 0x9b9b9d, 0x9a999c, 0x98989a, 0x909092, 0x89898c,
        0x88888b, 0x87878b, 0x858588, 0x777a7c, 0x77787b, 0x78777c, 0x75777b, 0x77767b,
        0x75767d, 0x777679, 0x74767a, 0x75757b, 0x757478, 0x737478, 0x6e7072, 0x6b6c72,
        0x6a6b70, 0x6a6a6f, 0x676a6e, 0x67676c, 0x64656a, 0x646469, 0x606563, 0x616367,
        0x616168, 0x616265, 0x5e6163, 0x5d5e63, 0x5a5c5f, 0x5b5b60, 0x57575b, 0x545558,
        0x545458, 0x505054, 0x48494d, 0x46484c, 0x444549, 0x414143, 0x404043, 0x3d3f42,
        0x3e3d40, 0x303134, 0x302f33, 0x2f2f32, 0x1f1e20, 0x040606, 0x020204, 0x000202,
        0x010103, 0x000100, 0x020004, 0x000004, 0x000003, 0x000002, 0x000001, 0x000000
    };
    private static final String[] ROWS = {
        "ILUfftvstvvv_-u__-__vvvttvvttvvUUUYU",
        "Ucfvz84535554542211214212243449tvOPM",
        "Ycv9477DFFFLqqq78AA34553979EGJOQdsCB",
        "av38497FKDFLs00v-CJKCKKBEQWTWSNOZetM",
        "Uv14A79797DO_0000_wCLCHOSVSWVQGGNVsY",
        "v13779999FPXs00YX00u-PXVVSZTQQBJBTgv",
        "_158799797JI-12_TZT00--TXVOONOLGJXhv",
        "v4A89997799I_11_--2ec12-uLROOQOONWgt",
        "_59DDA9A799Lz12-mjuu2e122uuVVTVWSZht",
        "_37EEGJGGEEDx12ykDHmw2df112uuabZWbiz",
        "-4KEGGBGJGNPy45zxRFFD-z1de111uVQNVdz",
        "-4PSVSQJJOWbz34kjxxJKRpsz1d22zoOGQd-",
        "z4JZbbTSWWZex78skDDswbedo_535zoNNWg-",
        "-4FVVbbZbcdex79uyx7kez_ed_435zpGNWgt",
        "z3DQWbZedddbxFFuooxw7kkzz_884zmGNVgt",
        "-4FNQWZbbZZbxKFsoRNns_8mjs88AznVZclt",
        "-4FOOxxxxxxx-FDunDLJFp_uemX98_qgghnt",
        "z4DPs_X12222CVXzpFFFDDDkrxJDD_qghgl_",
        "t5DPzf798A877AVzkFJDFJKDPyFDJspTBOd-",
        "-4AzeJDDDDDKHedzoRXZXRPOOwSTSzk79IZz",
        "t5DxkDTWkkecdfdxneeccbbbexTSXzi87Iet",
        "-AP_kCckjjkijjz-pVPRPPXcescedsj77Od-",
        "tAVujXcjkjkm-sokk97FDDVbbxccc_jBJQe-",
        "t9bysqepnq-_hhh78DOzs__z-_ljj_m8GOdt",
        "_9bgnwxxx-ghi89HOxsj55435LlkjzhIGNds",
        "z9TZbVnnnnpCILLN-pXXXXXVSSIjkwh79Idt",
        "tADOGBBOONQOQQRTznYYllcfcenonyi88Odt",
        "z37GJECCCLNMORV-qmdjmnpoopnozwhGGQi_",
        "z3FWSOOICBGGNVbwqkdkqqqqqqqp_kkNQSh_",
        "-3JNOCEECGGNNVbeu-mkqqqqqqq_pnOQQSgt",
        "t49JG888BGBNOVafsukpqqqsuw_ooPPQWggv",
        "Sy9GJI98CGGQRYcdrzsyxxszjkkmKPNXhjsM",
        "T-BdbLIEBBQacfghgnxxxxhghLFFRPWgnntU",
        "MLtihaVTacgggihgghankkkDPPTTZeijntMM",
        "MLBvthllnpnnnpllhedbbbbeggjmkklst56A",
        "QMBBBtttvvtzz__--_tztvvt_r_tz_t76667"
    };
    private static final String[] MUTE_SYMBOL = {
        ".........#######.........",
        ".......###########.......",
        ".....###############.....",
        "....######.....######....",
        "...#####.........#####...",
        "..####...........######..",
        "..###...........#######..",
        ".####..........#########.",
        ".###..........#####..###.",
        "####.........#####...####",
        "###.........#####.....###",
        "###........#####......###",
        "###.......#####.......###",
        "###......#####........###",
        "###.....#####.........###",
        "####...#####.........####",
        ".###..#####..........###.",
        ".#########..........####.",
        "..#######...........###..",
        "..######...........####..",
        "...#####.........#####...",
        "....######.....######....",
        ".....###############.....",
        ".......###########.......",
        ".........#######........."
    };
    private final BufferedImage[][] images = new BufferedImage[2][2];

    public LoginMusicButton() {
        for (int enabled = 0; enabled < 2; enabled++) {
            for (int hover = 0; hover < 2; hover++) {
                BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < SIZE; y++) {
                    int inset = y == 0 || y == 35 ? 5 : y == 1 || y == 34 ? 3
                            : y == 2 || y == 33 ? 2 : y <= 4 || y >= 31 ? 1 : 0;
                    for (int x = inset; x < SIZE - inset; x++) {
                        int color = PALETTE[SYMBOLS.indexOf(ROWS[y].charAt(x))];
                        if (hover == 1) {
                            int red = Math.min(255, (color >> 16 & 255) + 12);
                            int green = Math.min(255, (color >> 8 & 255) + 12);
                            int blue = Math.min(255, (color & 255) + 12);
                            color = red << 16 | green << 8 | blue;
                        }
                        image.setRGB(x, y, 0xff000000 | color);
                    }
                }
                if (enabled == 0) drawMuteSymbol(image);
                images[enabled][hover] = image;
            }
        }
    }

    private static void drawMuteSymbol(BufferedImage image) {
        for (int y = 0; y < MUTE_SYMBOL.length; y++) {
            for (int x = 0; x < MUTE_SYMBOL[y].length(); x++) {
                if (MUTE_SYMBOL[y].charAt(x) != '#') continue;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        image.setRGB(x + 7 + dx, y + 5 + dy, 0xff000000);
                    }
                }
            }
        }
        for (int y = 0; y < MUTE_SYMBOL.length; y++) {
            for (int x = 0; x < MUTE_SYMBOL[y].length(); x++) {
                if (MUTE_SYMBOL[y].charAt(x) == '#') image.setRGB(x + 7, y + 5, 0xff861414);
            }
        }
    }

    public void draw(Graphics graphics, int x, int y, boolean enabled, boolean hover) {
        graphics.drawImage(images[enabled ? 1 : 0][hover ? 1 : 0], x, y, null);
    }
}
