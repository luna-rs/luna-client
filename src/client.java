// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.applet.AppletContext;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.zip.CRC32;

import luna.Constants;
import luna.RsaParser;
import sign.signlink;

@SuppressWarnings("serial")
public class client extends JagApplet {

    private static int PROCESS_PACKET_COUNT = 100;//5

    public int archiveHashes[];
    public byte aByteArrayArray838[][];
    public String aString839;
    public static BigInteger JAGEX_MODULUS = new BigInteger(
            "7162900525229798032761816791230527296329313291232324290237849263501208207972894053929065636522363163621000728841182238772712427862772219676577293600221789");
    public static int anInt841;
    public int anIntArray842[] = {0xffff00, 0xff0000, 65280, 65535, 0xff00ff, 0xffffff};
    public int anIntArray843[];
    public int anInt844;
    public int anInt845;
    public int anInt846;
    public int anInt847;
    public int anInt848;
    public String aStringArray849[];
    public int anInt850;
    public int anInt851;
    public int cameraAmplitude[];
    public int anInt853;
    public int anInt854;
    public int ignoresCount;
    public int coordinates[];
    public int anIntArray857[];
    public int anIntArray858[];
    public int friendsCount;
    public int anInt860;
    public String aString861;
    public int anInt862;
    public String aStringArray863[];
    public int anIntArray864[];
    public int anInt865;
    public boolean aBoolean866;
    public int playerRights;
    public static boolean fps;
    public int size;
    public int opcode;
    public int anInt871;
    public int anInt872;
    public int anInt873;
    public int anInt874;
    public int anInt875;
    public int anInt876;
    public int anInt877;
    public int anInt878;
    public int constructedMapPalette[][][];
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_880;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_881;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_882;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_883;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_884;
    public int anIntArrayArray885[][];
    public int anIntArrayArray886[][];
    public int privateChatMode;
    public Archive titleArchive;
    public int chunkX;
    public int chunkY;
    public int intGroundArray[][][];
    public boolean aBoolean892;
    public int anInt893;
    public int anInt894;
    public static int anInt895;
    public RgbSprite aClass50_Sub1_Sub1_Sub1Array896[];
    public int anInt897;
    public byte aByte898;
    public IsaacRandom incomingRandom;
    public boolean aBoolean900;
    public byte aByte901;
    public long aLong902;
    public int anInt903;
    public int lastOpcode;
    public int anInt905;
    public int anInt915;
    public int anInt916;
    public int anInt917;
    public boolean aBoolean918;
    public boolean aBoolean919;
    public int anIntArray920[];
    public int anInt921;
    public static int world = 10;
    public static int portOffset;
    public static boolean memberServer = true;
    public static boolean lowMemory;
    public boolean customCameraActive[];
    public int anInt928;
    public JagBuffer tempBuffer;
    public long serverSeed;
    public int anInt931;
    public int anInt932;
    public int anInt933;
    public int anInt935;
    public byte aByte936;
    public String aString937;
    public int anInt938;
    public int anInt939;
    public int anInt940;
    public int anIntArray941[];
    public int anIntArray942[];
    public int anIntArray943[];
    public int anIntArray944[];
    public int anIntArray945[];
    public int anIntArray946[];
    public int anIntArray947[];
    public String aStringArray948[];
    public String chatboxInput;
    public boolean aBoolean950;
    public int anInt951;
    public static int anIntArray952[];
    public RgbSprite aClass50_Sub1_Sub1_Sub1Array954[];
    public int anInt955;
    public byte aByte956;
    public String statusLineOne;
    public String statusLineTwo;
    public boolean aBoolean959;
    public int openInterfaceID;
    public int thisPlayerServerId;
    public static boolean accountFlagged;
    public static boolean aBoolean963 = true;
    public JagBuffer outBuffer;
    public int anInt968;
    public int thisPlayerId;
    public Player players[];
    public int localPlayerCount;
    public int localPlayers[];
    public int updatedPlayerCount;
    public int updatedPlayers[];
    public JagBuffer cachedAppearances[];
    public int anInt977;
    public static int anInt978;
    public int anIntArray979[];
    public int anIntArray980[];
    public int anIntArray981[];
    public int anIntArray982[];
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_983;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_984;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_985;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_986;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_987;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_965;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_966;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_967;
    public IndexedSprite[] aClass50_Sub1_Sub1_Sub3Array976;
    public int anInt988;
    public int placementX;
    public int placementY;
    public int cameraFrequency[];
    public int membershipDaysRemaining;
    public int anInt993;
    public int anInt994;
    public int anInt995;
    public int anInt996;
    public int anInt997;
    public int anInt998;
    public static boolean started;
    public int chatBoxOffsets[];
    public int tabsOffsets[];
    public int gameViewportOffsets[];
    public int clientEntireOffsets[];
    public int anInt1004;
    public int defaultLocalVarps[];
    public int publicChatMode;
    public static String aString1007 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";
    public static final int anIntArrayArray1008[][] = {
            {6798, 107, 10283, 16, 4797, 7744, 5799, 4634, 33697, 22433, 2983, 54193},
            {8741, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 56621, 4783, 1341, 16578, 35003, 25239},
            {25238, 8742, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 56621, 4783, 1341, 16578, 35003},
            {4626, 11146, 6439, 12, 4758, 10270}, {4550, 4537, 5681, 5673, 5790, 6806, 8076, 4574}};
    public int anInt1009;
    public int anInt1010;
    public int anInt1011;
    public int anInt1012;
    public static int anInt1013;
    public boolean aBoolean1014;
    public int anInt1015;
    public boolean aBoolean1016;
    public RgbSprite sprite_1017;
    public RgbSprite sprite_1018;
    public int anIntArray1019[];
    public int anInt1020;
    public int anInt1021;
    public int anInt1022;
    public int anInt1023;
    public JagSocket connection;
    public String userInputString;
    public String aString1027;
    public boolean aBoolean1028;
    public int anIntArray1029[];
    public int anInt1030;
    public RgbSprite aClass50_Sub1_Sub1_Sub1Array1031[];
    public final int anIntArray1032[] = {0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3};
    public boolean aBoolean1033;
    public int recoveryQuestionDays;
    public int anInt1035;
    public RgbSprite aClass50_Sub1_Sub1_Sub1_1036;
    public RgbSprite aClass50_Sub1_Sub1_Sub1_1037;
    public boolean aBoolean1038;
    public int localVarps[];
    public int nextTopLeftTileX;
    public int nextTopLeftTileY;
    public int topLeftTileX;
    public int topLeftTileY;
    public int anInt1044;
    public int anInt1045;
    public boolean shouldRenderUI;
    public int anInt1047;
    public int anInt1048;
    public static int anInt1049;
    public int minimapState;
    public int anInt1051;
    public static int anInt1052;
    public int anInt1053;
    public int anIntArray1054[];
    public int anInt1055;
    public int anInt1056;
    public int anInt1057;
    public String aString1058;
    public JagFont font_p11_full;
    public JagFont fontChatboxButtons;
    public JagFont loginScreenFont;
    public JagFont font_q9_full;
    public int anInt1063;
    public int anInt1064;
    public boolean isContextMenuActive;
    public byte aByte1066;
    public boolean aBoolean1067;
    public int playerMembers;
    public String aStringArray1069[];
    public boolean aBooleanArray1070[];
    public int loadingStage;
    public int anInt1072;
    public long ignores[];
    public boolean aBoolean1074;
    public int anInt1076;
    public int anIntArray1077[];
    public int anIntArray1078[];
    public RgbSprite aClass50_Sub1_Sub1_Sub1Array1079[];
    public int anInt1080;
    public int anIntArray1081[] = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    public static int anInt1082;
    public int lastPasswordChange;
    public int anIntArray1084[];
    public int anIntArray1085[];
    public RgbSprite aClass50_Sub1_Sub1_Sub1_1086;
    public CRC32 aCRC32_1088;
    public int anInt1089;
    public int anIntArray1090[];
    public int plane;
    public String thisPlayerName;
    public String thisPlayerPassword;
    public int anInt1094;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_1095;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_1096;
    public boolean aBoolean1097;
    public boolean aBoolean1098;
    public int anIntArray1099[];
    public static int anInt1100;
    public int anInt1101;
    public RgbSprite aClass50_Sub1_Sub1_Sub1_1102;
    public RgbSprite aClass50_Sub1_Sub1_Sub1_1103;
    public String chatInput;
    public int cameraJitter[];
    public int anInt1106;
    public int anInt1107;
    public int anInt1111;
    public int anInt1112;
    public int anInt1113;
    public int anInt1114;
    public int anInt1115;
    public RgbSprite rbgSprite_compass_1116;
    public IndexedSprite runes_array1117[];
    public int anInt1118;
    public int anInt1119;
    public int anInt1120;
    public int anInt1121;
    public RgbSprite rbgSprite_1122;
    public int walkingPathX[];
    public int walkingPathY[];
    public byte aByteArrayArrayArray1125[][][];
    public int anInt1126;
    public boolean aBoolean1127;
    public int anInt1128;
    public int currentlyHovered1129;
    public long friends[];
    public JagBuffer aClass50_Sub1_Sub2_1131;
    public Npc npcs[];
    public int localNpcCount;
    public int anIntArray1134[];
    public int colorBrown1135;
    public boolean aBoolean1136;
    public boolean isLoggedIn;
    public int tickCounter1138;
    public static int anInt1139;
    public int anInt1140;
    public long aLong1141;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3Array1142[];
    public byte aByte1143;
    public boolean aBoolean1144;
    public int unknownCameraVariable[];
    public int anInt1146;
    public int itemIndexId;
    public int itemInterfaceId;
    public int itemId;
    public String aString1150;
    public int anInt1151;
    public int anInt1152;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3Array1153[];
    public int anInt1154;
    public boolean aBoolean1155;

    public JagImageProducer gameViewportImage;
    public JagImageProducer inventoryImage;
    public JagImageProducer aClass18_1157;
    public JagImageProducer chatboxImage_1159;
    public JagImageProducer chatboxButtons;
    public JagImageProducer aClass18_1109;
    public JagImageProducer aClass18_1110;

    public JagImageProducer loginboxElement;
    public JagImageProducer loginFlameLeft;
    public JagImageProducer loginFlameRight;
    public JagImageProducer loginBackground_1;
    public JagImageProducer loginBackground_2;
    public JagImageProducer loginBackground_3;
    public JagImageProducer loginBackground_4;
    public JagImageProducer loginBackground_5;
    public JagImageProducer loginBackground_6;

    public JagImageProducer aClass18_906;
    public JagImageProducer uiSideChatboxLeft;
    public JagImageProducer uiSideMinimapRight;
    public JagImageProducer uiSideRockRight1;
    public JagImageProducer aClass18_910;
    public JagImageProducer uiSideMinimapLeft;
    public JagImageProducer uiSideRockLeft1;
    public JagImageProducer uiSideChatboxRight;
    public JagImageProducer uiSideChatboxTop;

    public static int anInt1160;
    public byte aByte1161;
    public static int anInt1162;
    public boolean aBoolean1163;
    public SceneGraph sceneGraph;
    public static int anInt1165;
    public int anIntArray1166[];
    public static Player thisPlayer;
    public static int heartbeatCounter;
    public int anInt1169;
    public int lastLoginDays;
    public int anInt1171;
    public int spellId;
    public int anInt1173;
    public String aString1174;
    public int anInt1175;
    public int anIntArray1176[];
    public int anIntArray1177[];
    public int anInt1178;
    public int anInt1179;
    public int anIntArray1180[];
    public boolean aBoolean1181;
    public RgbSprite[] spriteArray1182;
    public int anInt1183;
    public String rightClickOptions[];
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_1185;
    public IndexedSprite mapback_1186;
    public IndexedSprite aClass50_Sub1_Sub1_Sub3_1187;
    public JagBuffer buffer;
    public int cost[][];
    public int anInt1191;
    public RgbSprite rgbSprite_1192;
    public RgbSprite aClass50_Sub1_Sub1_Sub1_1193;
    public RgbSprite aClass50_Sub1_Sub1_Sub1_1194;
    public RgbSprite aClass50_Sub1_Sub1_Sub1_1195;
    public RgbSprite aClass50_Sub1_Sub1_Sub1_1196;
    public int anInt1197;
    public static boolean aBoolean1207;
    public boolean mapLoading;
    public LinkedList aClass6_1210;
    public boolean aBoolean1211;
    public boolean aBoolean1212;
    public int anInt1213;
    public static int BITFIELD_MAX_VALUES[];
    public int somethngLoginDays;
    public int anInt1216;
    public int anInt1217;
    public int anInt1218;
    public int anInt1219;
    public int anInt1220;
    public int anInt1221;
    public int anInt1222;
    public int splitPrivateChat;
    public Socket aSocket1224;
    public int loginScreenState;
    public int anInt1226;
    public int tradeMode;
    public FileStore stores[];
    public long aLong1229;
    public static int anInt1230;
    public int anInt1231;
    public byte aByteArrayArray1232[][];
    public int anInt1233;
    public int anInt1234;
    public static int anInt1235;
    public int anInt1236;
    public static int anInt1237;
    public int anInt1238;
    public boolean aBoolean1239;
    public boolean aBoolean1240;
    public int lastAddress;
    public static boolean aBoolean1242 = true;
    public volatile boolean isThreadStarted;
    public int chatboxInterfaceType;
    public byte aByteArray1245[];
    public int anInt1246;
    public RgbSprite aClass50_Sub1_Sub1_Sub1_1247;
    public MouseRecorder mouseRecorder;
    public JagInterface aClass13_1249;
    public long aLong1250;
    public int anInt1251;
    public int anInt1252;
    public int anInt1253;
    public int anInt1254;
    public int anInt1255;
    public int anInt1256;
    public final int anInt1257 = 100;
    public int anIntArray1258[];
    public int anIntArray1259[];
    public ClippingPlane clippingPlanes[];
    public LinkedList gameObjectSpawnsRequestList;
    public int anInt1262;
    public int anInt1263;
    public int anInt1264;
    public boolean aBoolean1265;
    public boolean musicEnabled;
    public int anIntArray1267[];
    public static final int anIntArray1268[] = {9104, 10275, 7595, 3610, 7975, 8526, 918, 38802, 24466, 10145, 58654,
            5027, 1457, 16565, 34991, 25486};
    public int anInt1269;
    public int anInt1270;
    public boolean aBoolean1271;
    public int anInt1272;
    public int unreadMessages;
    public boolean aBoolean1274;
    public boolean aBoolean1275;
    public int anInt1276;
    public boolean aBoolean1277;
    public RgbSprite aClass50_Sub1_Sub1_Sub1Array1278[];
    public int walkableInterfaceId;
    public int anInt1280;
    public int anInt1281;
    public LinkedList projectileQueue;
    public boolean aBoolean1283;
    public int anInt1284;
    public int tabId;
    public int anIntArray1286[];
    public int anInt1287;
    public RgbSprite aClass50_Sub1_Sub1_Sub1Array1288[];
    public int anInt1289;
    public int anIntArray1290[] = {17, 24, 34, 40};
    public OnDemandFetcher fileFetcher;
    public IndexedSprite titlebox_1292;
    public IndexedSprite titlebutton_1293;
    public int removePlayerCount;
    public int removePlayers[];
    public int anIntArray1296[];
    public String aStringArray1297[];
    public String aStringArray1298[];
    public int anInt1299;
    public int anInt1300;
    public boolean aBoolean1301;
    public int currentlyHovered1302;
    public int anInt1303;
    public int anInt1304;
    public int anInt1305;
    public int anInt1306;
    public int anInt1307;
    public int anInt1308;
    public static int paintCounter1309;
    public int anIntArray1310[];
    public int anIntArray1311[];
    public int anIntArray1312[];
    public int anIntArray1313[];
    public volatile boolean isGameThreadStarted;
    public int anInt1315;
    public static BigInteger JAGEX_PUBLIC_KEY = new BigInteger(
            "58778699976184461502525193738213253649000149147835990136706041084440742975821");
    public byte aByte1317;
    public int anInt1318;
    public int anInt1319;
    public volatile boolean delayedResetter1320;
    public int anIntArray1321[];
    public int anInt1322;
    public LinkedList groundItems[][][];
    public int anInt1324;
    public static int pulseCycle;
    public int anIntArray1326[];
    public int anInt1327;
    public int anInt1328;
    public int anInt1329;
    public int anInt1330;
    public int anInt1331;
    public int anInt1332;

    // the below variables were added for resizable

    /**
     * Represents different modes for the client, 0 is fixed mode and 1 is resizable
     */
    public int clientSize = 0;

    /**
     * Current size of the entire client
     */
    public static int clientWidth = 765, clientHeight = 503;

    /**
     * Standard size for the entire client
     */
    public static final int REGULAR_WIDTH = 765, REGULAR_HEIGHT = 503;

    /**
     * Where each part of the client is drawn, rebuilt whenever the mode or window size changes
     */
    public ClientLayout layout = ClientLayout.create(false, REGULAR_WIDTH, REGULAR_HEIGHT);

    /**
     * The saved mode and window size, written when the mode changes and when the client closes
     */
    public static ClientSettings settings = new ClientSettings();

    /**
     * The mode that was saved last time, applied once the client has finished loading
     */
    private int startupClientSize = 0;

    /**
     * Whether the window around the login screen has been cleared since the game was last on screen
     */
    private boolean loginScreenCleared = false;

    public static void main(String args[]) {
        try {
            System.out.println("RS2 user client - release #" + 377);
            RsaParser.parse();
            world = 10;
            portOffset = 0;
            switchToHighMem();
            memberServer = true;
            signlink.storeid = 32;
            signlink.startpriv(InetAddress.getLocalHost());
            settings = ClientSettings.load();
            client cl = new client();
            cl.startupClientSize = settings.clientSize;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> settings.save()));
            cl.start(765, 503);
        } catch (Exception exception) {
            return;
        }
    }

    /**
     * Changes the window to the given size for the given mode and rebuilds everything that depends on it.
     */
    public void rebuildFrame(int size, int width, int height) {
        try {
            clientWidth = width;
            clientHeight = height;
            rebuildFrame(width, height, clientSize == 1);
            updateGameArea();
            super.mouseX = super.mouseY = -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Rebuilds the layout, the 3D view and the game screen for the current mode and client size.
     */
    private void updateGameArea() {
        layout = ClientLayout.create(clientSize == 1, clientWidth, clientHeight);
        // the line offsets must match the width of the image that is drawn into
        ThreeDimensionalCanvas.init3D(clientWidth, clientHeight);
        clientEntireOffsets = ThreeDimensionalCanvas.lineOffsets;
        ThreeDimensionalCanvas.init3D(ClientLayout.CHATBOX_WIDTH, ClientLayout.CHATBOX_HEIGHT);
        chatBoxOffsets = ThreeDimensionalCanvas.lineOffsets;
        ThreeDimensionalCanvas.init3D(ClientLayout.INVENTORY_WIDTH, ClientLayout.INVENTORY_HEIGHT);
        tabsOffsets = ThreeDimensionalCanvas.lineOffsets;
        // last, because init3D also sets the centre of the 3D view
        ThreeDimensionalCanvas.init3D(layout.viewport.width, layout.viewport.height);
        gameViewportOffsets = ThreeDimensionalCanvas.lineOffsets;

        int ai[] = new int[9];
        for (int l8 = 0; l8 < 9; l8++) {
            int j9 = 128 + l8 * 32 + 15;
            int k9 = 600 + j9 * 3;
            int l9 = ThreeDimensionalCanvas.sineTable[j9];
            ai[l8] = k9 * l9 >> 16;
        }

        SceneGraph.preCalcFrustrumTable(ai, layout.viewport.width, layout.viewport.height, 500, 800);

        // the context menu belongs to the old layout
        isContextMenuActive = false;
        if (isLoggedIn) {
            // rebuild the game screen for the new layout, a full screen interface is redrawn at the new size
            super.imageProducer = null;
            gameViewportImage = new JagImageProducer(layout.viewport.width, layout.viewport.height, getParentComponent());
            Drawable.clearScreen();
            initUI();
        }
        // when not logged in the game screen is built on login, from the layout
        // the layout changed, so everything has to be drawn again (also removes leftovers of the old layout)
        shouldRenderUI = true;
    }

    /**
     * Switches between the fixed (0) and the resizable (1) mode.
     */
    public void toggleSize(int size) {
        if (clientSize != size) {
            clientSize = size;
            int width = REGULAR_WIDTH;
            int height = REGULAR_HEIGHT;
            if (size == 1) {
                width = settings.width;
                height = settings.height;
            }
            // rebuildFrame also calls updateGameArea
            rebuildFrame(size, width, height);
            settings.clientSize = clientSize;
            settings.save();
        }
    }

    /**
     * Handles the commands that only concern the client and are not sent to the server.
     *
     * @return true if the input was such a command
     */
    public boolean handleClientCommand(String input) {
        if (input.equals("::regular")) {
            toggleSize(0);
            return true;
        }
        if (input.equals("::resize")) {
            toggleSize(1);
            return true;
        }
        return false;
    }

    /**
     * Applies a window resize reported by the AWT thread, on the game thread.
     * The size is the drawable area inside the borders and title bar, not the outer window size.
     */
    public void checkSize() {
        int width = super.resizedWidth;
        int height = super.resizedHeight;
        if (clientSize == 1 && width > 0 && height > 0 && (width != clientWidth || height != clientHeight)) {
            clientWidth = settings.width = width;
            clientHeight = settings.height = height;
            updateGameArea();
        }
    }

    /**
     * The game is laid out for 765x503 and is drawn centred in the window while on the login screen in the
     * resizable mode, so mouse positions are moved by the same amount.
     */
    @Override
    public int inputOffsetX() {
        return loginScreenOffsetX();
    }

    @Override
    public int inputOffsetY() {
        return loginScreenOffsetY();
    }

    /**
     * How far the 765x503 login screen is moved to the centre of the window. Zero in the fixed mode and in the game.
     */
    public int loginScreenOffsetX() {
        return clientSize == 1 && !isLoggedIn ? Math.max(0, (clientWidth - REGULAR_WIDTH) / 2) : 0;
    }

    public int loginScreenOffsetY() {
        return clientSize == 1 && !isLoggedIn ? Math.max(0, (clientHeight - REGULAR_HEIGHT) / 2) : 0;
    }

    public void run() {
        if (isGameThreadStarted) {
            startLoginScreenLoop();
            return;
        } else {
            super.run();
            return;
        }
    }

    public void method14(String s, int i) {
        if (s == null || s.length() == 0) {
            anInt862 = 0;
            return;
        }
        String s1 = s;
        String as[] = new String[100];
        int j = 0;
        do {
            int k = s1.indexOf(" ");
            if (k == -1)
                break;
            String s2 = s1.substring(0, k).trim();
            if (s2.length() > 0)
                as[j++] = s2.toLowerCase();
            s1 = s1.substring(k + 1);
        } while (true);
        s1 = s1.trim();
        if (s1.length() > 0)
            as[j++] = s1.toLowerCase();
        anInt862 = 0;
        if (i != 2)
            aBoolean959 = !aBoolean959;
        label0:
        for (int l = 0; l < ItemDefinition.count; l++) {
            ItemDefinition class16 = ItemDefinition.forId(l);
            if (class16.notedGraphicsId != -1 || class16.name == null)
                continue;
            String s3 = class16.name.toLowerCase();
            for (int i1 = 0; i1 < j; i1++)
                if (s3.indexOf(as[i1]) == -1)
                    continue label0;

            aStringArray863[anInt862] = s3;
            anIntArray864[anInt862] = l;
            anInt862++;
            if (anInt862 >= aStringArray863.length)
                return;
        }

    }

    public void method15(boolean flag) { // closeWindow?
        outBuffer.putOpcode(110);
        if (flag)
            groundItems = null;
        if (anInt1089 != -1) {
            method44(anInt1089);
            anInt1089 = -1;
            aBoolean1181 = true;
            aBoolean1239 = false;
            aBoolean950 = true;
        }
        if (anInt988 != -1) {
            method44(anInt988);
            anInt988 = -1;
            aBoolean1240 = true;
            aBoolean1239 = false;
        }
        if (anInt1053 != -1) {
            method44(anInt1053);
            anInt1053 = -1;
            shouldRenderUI = true;
        }
        if (openInterfaceID != -1) {
            method44(openInterfaceID);
            openInterfaceID = -1;
        }
        if (anInt1169 != -1) {
            method44(anInt1169);
            anInt1169 = -1;
        }
    }

    public void addNewPlayers(int newPlayers, JagBuffer buf) {
        while (buf.bitPosition + 10 < newPlayers * 8) {
            int index = buf.getBits(11);
            if (index == 2047)
                break;
            if (players[index] == null) {
                players[index] = new Player();
                if (cachedAppearances[index] != null)
                    players[index].updateAppearance(cachedAppearances[index], 0);
            }
            localPlayers[localPlayerCount++] = index;
            Player plr = players[index];
            plr.pulseCycle = pulseCycle;
            int x = buf.getBits(5);
            if (x > 15)
                x -= 32;
            int updated = buf.getBits(1);
            if (updated == 1)
                updatedPlayers[updatedPlayerCount++] = index;
            int discardQueue = buf.getBits(1);
            int y = buf.getBits(5);
            if (y > 15)
                y -= 32;
            plr.teleport(((Actor) (thisPlayer)).walkingQueueX[0] + x, ((Actor) (thisPlayer)).walkingQueueY[0] + y,
                    discardQueue == 1);
        }
        buf.finishBitAccess();
    }

    private void stopMidi() {
        try {
            if (signlink.musicSr.isOpen()) {
                signlink.midifade = 0;
                signlink.midi = "stop";
                signlink.musicSr.stop();
                signlink.musicSr.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startLoginScreenLoop() {
        delayedResetter1320 = true;
        try {
            long l = System.currentTimeMillis();
            int i = 0;
            int j = 20;
            while (isThreadStarted) {
                anInt1101++;
                updateLoginFlames((byte) 1);
                updateLoginFlames((byte) 1);
                renderLoginFlames();
                if (++i > 10) {
                    long l1 = System.currentTimeMillis();
                    int k = (int) (l1 - l) / 10 - j;
                    j = 40 - k;
                    if (j < 5) {
                        j = 5;
                    }
                    i = 0;
                    l = l1;
                }
                try {
                    Thread.sleep(j);
                } catch (Exception _ex) {
                }
            }
        } catch (Exception _ex) {
        }
        delayedResetter1320 = false;
    }

    public void processGameObjectSpawnRequests() {
        for (GameObjectSpawnRequest objectSpawnRequest = (GameObjectSpawnRequest) gameObjectSpawnsRequestList.first();
             objectSpawnRequest != null;
             objectSpawnRequest = (GameObjectSpawnRequest) gameObjectSpawnsRequestList.next())
            if (objectSpawnRequest.delayUntilRespawn == -1) {
                objectSpawnRequest.anInt1395 = 0;
                method140((byte) -61, objectSpawnRequest);
            } else {
                objectSpawnRequest.unlink();
            }

    }

    public void printError_19(String s) {
        System.out.println(s);
        try {
            getAppletContext().showDocument(new URL(getCodeBase(), "loaderror_" + s + ".html"));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        while (true) try {
            Thread.sleep(1000L);
        } catch (Exception _ex) {
        }
    }

    public static String addMoneySuffix(int coins, int j) {
        if (j >= 0)
            throw new NullPointerException();
        if (coins < 0x186a0)
            return String.valueOf(coins);
        if (coins < 0x989680)
            return coins / 1000 + "K";
        else
            return coins / 0xf4240 + "M";
    }

    public void cleanupShutdown() {
        players = null;
        localPlayers = null;
        updatedPlayers = null;
        cachedAppearances = null;
        removePlayers = null;
        aClass18_906 = null;
        uiSideChatboxLeft = null;
        uiSideMinimapRight = null;
        uiSideRockRight1 = null;
        aClass50_Sub1_Sub1_Sub3_880 = null;
        aClass50_Sub1_Sub1_Sub3_881 = null;
        aClass50_Sub1_Sub1_Sub3_882 = null;
        aClass50_Sub1_Sub1_Sub3_883 = null;
        aClass50_Sub1_Sub1_Sub3_884 = null;
        aClass50_Sub1_Sub1_Sub3_983 = null;
        aClass50_Sub1_Sub1_Sub3_984 = null;
        aClass50_Sub1_Sub1_Sub3_985 = null;
        aClass50_Sub1_Sub1_Sub3_986 = null;
        aClass50_Sub1_Sub1_Sub3_987 = null;
        aStringArray849 = null;
        friends = null;
        anIntArray1267 = null;
        chatboxButtons = null;
        aClass18_1109 = null;
        aClass18_1110 = null;
        localVarps = null;
        coordinates = null;
        aByteArrayArray838 = null;
        aByteArrayArray1232 = null;
        anIntArray857 = null;
        anIntArray858 = null;
        loginBackground_3 = null;
        loginBackground_4 = null;
        loginBackground_5 = null;
        loginBackground_6 = null;
        anIntArrayArray885 = null;
        cost = null;
        walkingPathX = null;
        walkingPathY = null;
        rgbSprite_1192 = null;
        aClass50_Sub1_Sub1_Sub1_1193 = null;
        aClass50_Sub1_Sub1_Sub1_1194 = null;
        aClass50_Sub1_Sub1_Sub1_1195 = null;
        aClass50_Sub1_Sub1_Sub1_1196 = null;
        if (mouseRecorder != null)
            mouseRecorder.running = false;
        mouseRecorder = null;
        aClass50_Sub1_Sub1_Sub3_965 = null;
        aClass50_Sub1_Sub1_Sub3_966 = null;
        aClass50_Sub1_Sub1_Sub3_967 = null;
        aClass18_910 = null;
        uiSideMinimapLeft = null;
        uiSideRockLeft1 = null;
        uiSideChatboxRight = null;
        uiSideChatboxTop = null;
        intGroundArray = null;
        aByteArrayArrayArray1125 = null;
        sceneGraph = null;
        clippingPlanes = null;
        rbgSprite_1122 = null;
        loginFlameLeft = null;
        loginFlameRight = null;
        loginBackground_1 = null;
        loginBackground_2 = null;
        loginboxElement = null;
        rbgSprite_compass_1116 = null;
        spriteArray1182 = null;
        aClass50_Sub1_Sub1_Sub1Array1288 = null;
        aClass50_Sub1_Sub1_Sub1Array1079 = null;
        aClass50_Sub1_Sub1_Sub1Array954 = null;
        aClass50_Sub1_Sub1_Sub1Array896 = null;
        method50(false);
        outBuffer = null;
        tempBuffer = null;
        buffer = null;
        inventoryImage = null;
        aClass18_1157 = null;
        gameViewportImage = null;
        chatboxImage_1159 = null;
        aClass50_Sub1_Sub1_Sub3_1185 = null;
        mapback_1186 = null;
        aClass50_Sub1_Sub1_Sub3_1187 = null;
        try {
            if (connection != null)
                connection.closeConnection();
        } catch (Exception _ex) {
        }
        connection = null;
        anIntArray1077 = null;
        anIntArray1078 = null;
        aClass50_Sub1_Sub1_Sub1Array1278 = null;
        npcs = null;
        anIntArray1134 = null;
        aByteArray1245 = null;
        aClass50_Sub1_Sub2_1131 = null;
        aClass50_Sub1_Sub1_Sub3Array1153 = null;
        aClass50_Sub1_Sub1_Sub1Array1031 = null;
        anIntArrayArray886 = null;
        aClass50_Sub1_Sub1_Sub3Array976 = null;
        projectileQueue = null;
        aClass6_1210 = null;
        aClass50_Sub1_Sub1_Sub1_1086 = null;
        if (fileFetcher != null)
            fileFetcher.method339();
        fileFetcher = null;
        anIntArray979 = null;
        anIntArray980 = null;
        anIntArray981 = null;
        anIntArray982 = null;
        rightClickOptions = null;
        groundItems = null;
        gameObjectSpawnsRequestList = null;
        resetWhenBoolTrue();
        ObjectDefinition.method433(false);
        NpcDefinition.clearBuffers();
        ItemDefinition.method222(false);
        JagInterface.method202(false);
        TileDefinition.tiles = null;
        IdentityKit.identityKits = null;
        UnusedClass4.aUnusedClass4Array103 = null;
        Animation.animations = null;
        SpotAnimation.spotAnimations = null;
        SpotAnimation.models = null;
        Varp.varpTable = null;
        super.imageProducer = null;
        Player.aClass33_1761 = null;
        ThreeDimensionalCanvas.unload();
        SceneGraph.method240(false);
        Model.dispose(false);
        AnimationFrame.clearFrames(false);
        System.gc();
    }

    public void processMouseClickOnTabs21() {
        int tabClickX = super.anInt29 - layout.inventoryDx; // compared with the classic tab positions
        int tabClickY = super.anInt30 - layout.inventoryDy;
        if (super.anInt28 == 1) {
            if (tabClickX >= 539 && tabClickX <= 573 && tabClickY >= 169 && tabClickY < 205
                    && anIntArray1081[0] != -1) {
                aBoolean1181 = true;
                tabId = 0;
                aBoolean950 = true;
            }
            if (tabClickX >= 569 && tabClickX <= 599 && tabClickY >= 168 && tabClickY < 205
                    && anIntArray1081[1] != -1) {
                aBoolean1181 = true;
                tabId = 1;
                aBoolean950 = true;
            }
            if (tabClickX >= 597 && tabClickX <= 627 && tabClickY >= 168 && tabClickY < 205
                    && anIntArray1081[2] != -1) {
                aBoolean1181 = true;
                tabId = 2;
                aBoolean950 = true;
            }
            if (tabClickX >= 625 && tabClickX <= 669 && tabClickY >= 168 && tabClickY < 203
                    && anIntArray1081[3] != -1) {
                aBoolean1181 = true;
                tabId = 3;
                aBoolean950 = true;
            }
            if (tabClickX >= 666 && tabClickX <= 696 && tabClickY >= 168 && tabClickY < 205
                    && anIntArray1081[4] != -1) {
                aBoolean1181 = true;
                tabId = 4;
                aBoolean950 = true;
            }
            if (tabClickX >= 694 && tabClickX <= 724 && tabClickY >= 168 && tabClickY < 205
                    && anIntArray1081[5] != -1) {
                aBoolean1181 = true;
                tabId = 5;
                aBoolean950 = true;
            }
            if (tabClickX >= 722 && tabClickX <= 756 && tabClickY >= 169 && tabClickY < 205
                    && anIntArray1081[6] != -1) {
                aBoolean1181 = true;
                tabId = 6;
                aBoolean950 = true;
            }
            if (tabClickX >= 540 && tabClickX <= 574 && tabClickY >= 466 && tabClickY < 502
                    && anIntArray1081[7] != -1) {
                aBoolean1181 = true;
                tabId = 7;
                aBoolean950 = true;
            }
            if (tabClickX >= 572 && tabClickX <= 602 && tabClickY >= 466 && tabClickY < 503
                    && anIntArray1081[8] != -1) {
                aBoolean1181 = true;
                tabId = 8;
                aBoolean950 = true;
            }
            if (tabClickX >= 599 && tabClickX <= 629 && tabClickY >= 466 && tabClickY < 503
                    && anIntArray1081[9] != -1) {
                aBoolean1181 = true;
                tabId = 9;
                aBoolean950 = true;
            }
            if (tabClickX >= 627 && tabClickX <= 671 && tabClickY >= 467 && tabClickY < 502
                    && anIntArray1081[10] != -1) {
                aBoolean1181 = true;
                tabId = 10;
                aBoolean950 = true;
            }
            if (tabClickX >= 669 && tabClickX <= 699 && tabClickY >= 466 && tabClickY < 503
                    && anIntArray1081[11] != -1) {
                aBoolean1181 = true;
                tabId = 11;
                aBoolean950 = true;
            }
            if (tabClickX >= 696 && tabClickX <= 726 && tabClickY >= 466 && tabClickY < 503
                    && anIntArray1081[12] != -1) {
                aBoolean1181 = true;
                tabId = 12;
                aBoolean950 = true;
            }
            if (tabClickX >= 724 && tabClickX <= 758 && tabClickY >= 466 && tabClickY < 502
                    && anIntArray1081[13] != -1) {
                aBoolean1181 = true;
                tabId = 13;
                aBoolean950 = true;
            }
        }
    }

    public void loadPlayer22() {
        try {
            int j = ((Actor) (thisPlayer)).unitX + anInt853;
            int k = ((Actor) (thisPlayer)).unitY + anInt1009;
            if (anInt1262 - j < -500 || anInt1262 - j > 500 || anInt1263 - k < -500 || anInt1263 - k > 500) {
                anInt1262 = j;
                anInt1263 = k;
            }
            if (anInt1262 != j)
                anInt1262 += (j - anInt1262) / 16;
            if (anInt1263 != k)
                anInt1263 += (k - anInt1263) / 16;
            if (super.keyStatus[1] == 1)
                anInt1253 += (-24 - anInt1253) / 2;
            else if (super.keyStatus[2] == 1)
                anInt1253 += (24 - anInt1253) / 2;
            else
                anInt1253 /= 2;
            if (super.keyStatus[3] == 1)
                anInt1254 += (12 - anInt1254) / 2;
            else if (super.keyStatus[4] == 1)
                anInt1254 += (-12 - anInt1254) / 2;
            else
                anInt1254 /= 2;
            anInt1252 = anInt1252 + anInt1253 / 2 & 0x7ff;
            anInt1251 += anInt1254 / 2;
            if (anInt1251 < 128)
                anInt1251 = 128;
            if (anInt1251 > 383)
                anInt1251 = 383;
            int l = anInt1262 >> 7;
            int i1 = anInt1263 >> 7;
            int j1 = getFloorDrawHeight(anInt1263, anInt1262, plane);
            int k1 = 0;
            if (l > 3 && i1 > 3 && l < 100 && i1 < 100) {
                for (int l1 = l - 4; l1 <= l + 4; l1++) {
                    for (int j2 = i1 - 4; j2 <= i1 + 4; j2++) {
                        int k2 = plane;
                        if (k2 < 3 && (aByteArrayArrayArray1125[1][l1][j2] & 2) == 2)
                            k2++;
                        int l2 = j1 - intGroundArray[k2][l1][j2];
                        if (l2 > k1)
                            k1 = l2;
                    }

                }

            }
            int i2 = k1 * 192;
            if (i2 > 0x17f00)
                i2 = 0x17f00;
            if (i2 < 32768)
                i2 = 32768;
            if (i2 > anInt1289) {
                anInt1289 += (i2 - anInt1289) / 24;
                return;
            }
            if (i2 < anInt1289) {
                anInt1289 += (i2 - anInt1289) / 80;
                return;
            }
        } catch (Exception _ex) {
            signlink.reporterror("glfc_ex " + ((Actor) (thisPlayer)).unitX + ","
                    + ((Actor) (thisPlayer)).unitY + "," + anInt1262 + "," + anInt1263 + "," + chunkX + ","
                    + chunkY + "," + nextTopLeftTileX + "," + nextTopLeftTileY);
            throw new RuntimeException("eek");
        }
    }

    public boolean method23(JagInterface class13, int i) {
        i = 98 / i;
        int j = class13.anInt242;
        if (j >= 1 && j <= 200 || j >= 701 && j <= 900) {
            if (j >= 801)
                j -= 701;
            else if (j >= 701)
                j -= 601;
            else if (j >= 101)
                j -= 101;
            else
                j--;
            rightClickOptions[anInt1183] = "Remove @whi@" + aStringArray849[j];
            anIntArray981[anInt1183] = 775;
            anInt1183++;
            rightClickOptions[anInt1183] = "Message @whi@" + aStringArray849[j];
            anIntArray981[anInt1183] = 984;
            anInt1183++;
            return true;
        }
        if (j >= 401 && j <= 500) {
            rightClickOptions[anInt1183] = "Remove @whi@" + class13.aString230;
            anIntArray981[anInt1183] = 859;
            anInt1183++;
            return true;
        } else {
            return false;
        }
    }

    public void method24(boolean flag, byte abyte0[], int i) {
        if (!musicEnabled) {
            return;
        } else {
            signlink.midifade = flag ? 1 : 0;
            signlink.midisave(abyte0, abyte0.length);
            i = 71 / i;
            return;
        }
    }

    public void method25(int i) {
        if (i != 0)
            outBuffer.putByte(186);
        aBoolean1277 = true;
        for (int j = 0; j < 7; j++) {
            anIntArray1326[j] = -1;
            for (int k = 0; k < IdentityKit.count; k++) {
                if (IdentityKit.identityKits[k].notSelectable
                        || IdentityKit.identityKits[k].part != j + (aBoolean1144 ? 0 : 7))
                    continue;
                anIntArray1326[j] = k;
                break;
            }

        }

    }

    public void method26(int x, int y) {
        LinkedList class6 = groundItems[plane][x][y];
        if (class6 == null) {
            sceneGraph.method262(plane, x, y);
            return;
        }
        int k = 0xfa0a1f01;
        Object obj = null;
        for (GroundItem class50_sub1_sub4_sub1 = (GroundItem) class6.first(); class50_sub1_sub4_sub1 != null; class50_sub1_sub4_sub1 = (GroundItem) class6
                .next()) {
            ItemDefinition class16 = ItemDefinition.forId(class50_sub1_sub4_sub1.id);
            int l = class16.value;
            if (class16.stackable)
                l *= class50_sub1_sub4_sub1.amount + 1;
            if (l > k) {
                k = l;
                obj = class50_sub1_sub4_sub1;
            }
        }

        class6.addFirst(((Node) (obj)));
        Object obj1 = null;
        Object obj2 = null;
        for (GroundItem class50_sub1_sub4_sub1_1 = (GroundItem) class6.first(); class50_sub1_sub4_sub1_1 != null; class50_sub1_sub4_sub1_1 = (GroundItem) class6
                .next()) {
            if (class50_sub1_sub4_sub1_1.id != ((GroundItem) (obj)).id && obj1 == null)
                obj1 = class50_sub1_sub4_sub1_1;
            if (class50_sub1_sub4_sub1_1.id != ((GroundItem) (obj)).id
                    && class50_sub1_sub4_sub1_1.id != ((GroundItem) (obj1)).id
                    && obj2 == null)
                obj2 = class50_sub1_sub4_sub1_1;
        }

        int i1 = x + (y << 7) + 0x60000000;
        sceneGraph.addSomethingToScenegraph2(plane, x, y, getFloorDrawHeight(y * 128 + 64, x * 128 + 64, plane),
                ((Entity) (obj)), ((Entity) (obj1)), i1, ((Entity) (obj2)), 2);
    }

    public static void switchToHighMem() {
        SceneGraph.lowMemory = false;
        ThreeDimensionalCanvas.lowMemory = false;
        lowMemory = false;
        Region.lowMemory = false;
        ObjectDefinition.lowMemory = false;
    }

    public void updateGame28(byte byte0) {
        if (anInt1057 > 1)
            anInt1057--;
        if (anInt873 > 0)
            anInt873--;
        for (int i = 0; i < PROCESS_PACKET_COUNT; i++)
            if (!parseIncomingPacket())
                break;

        if (!isLoggedIn)
            return;
        synchronized (mouseRecorder.lock) {
            if (accountFlagged) {
                if (super.anInt28 != 0 || mouseRecorder.pos >= 40) {
                    outBuffer.putOpcode(171);
                    outBuffer.putByte(0);
                    int i2 = outBuffer.position;
                    int i3 = 0;
                    for (int i4 = 0; i4 < mouseRecorder.pos; i4++) {
                        if (i2 - outBuffer.position >= 240)
                            break;
                        i3++;
                        int k4 = mouseRecorder.mouseY[i4];
                        if (k4 < 0)
                            k4 = 0;
                        else if (k4 > 502)
                            k4 = 502;
                        int j5 = mouseRecorder.mouseX[i4];
                        if (j5 < 0)
                            j5 = 0;
                        else if (j5 > 764)
                            j5 = 764;
                        int l5 = k4 * 765 + j5;
                        if (mouseRecorder.mouseY[i4] == -1 && mouseRecorder.mouseX[i4] == -1) {
                            j5 = -1;
                            k4 = -1;
                            l5 = 0x7ffff;
                        }
                        if (j5 == anInt1011 && k4 == anInt1012) {
                            if (anInt1299 < 2047)
                                anInt1299++;
                        } else {
                            int i6 = j5 - anInt1011;
                            anInt1011 = j5;
                            int j6 = k4 - anInt1012;
                            anInt1012 = k4;
                            if (anInt1299 < 8 && i6 >= -32 && i6 <= 31 && j6 >= -32 && j6 <= 31) {
                                i6 += 32;
                                j6 += 32;
                                outBuffer.putShort((anInt1299 << 12) + (i6 << 6) + j6);
                                anInt1299 = 0;
                            } else if (anInt1299 < 8) {
                                outBuffer.putTriByte(0x800000 + (anInt1299 << 19) + l5);
                                anInt1299 = 0;
                            } else {
                                outBuffer.putInt(0xc0000000 + (anInt1299 << 19) + l5);
                                anInt1299 = 0;
                            }
                        }
                    }

                    outBuffer.putLength(outBuffer.position - i2);
                    if (i3 >= mouseRecorder.pos) {
                        mouseRecorder.pos = 0;
                    } else {
                        mouseRecorder.pos -= i3;
                        for (int l4 = 0; l4 < mouseRecorder.pos; l4++) {
                            mouseRecorder.mouseX[l4] = mouseRecorder.mouseX[l4 + i3];
                            mouseRecorder.mouseY[l4] = mouseRecorder.mouseY[l4 + i3];
                        }

                    }
                }
            } else {
                mouseRecorder.pos = 0;
            }
        }
        if (super.anInt28 != 0) {
            long l = (super.aLong31 - aLong902) / 50L;
            if (l > 4095L)
                l = 4095L;
            aLong902 = super.aLong31;
            int j2 = super.anInt30;
            if (j2 < 0)
                j2 = 0;
            else if (j2 > 502)
                j2 = 502;
            int j3 = super.anInt29;
            if (j3 < 0)
                j3 = 0;
            else if (j3 > 764)
                j3 = 764;
            int j4 = j2 * 765 + j3;
            int i5 = 0;
            if (super.anInt28 == 2)
                i5 = 1;
            int k5 = (int) l;
            outBuffer.putOpcode(19);
            outBuffer.putInt((k5 << 20) + (i5 << 19) + j4);
        }
        if (anInt1264 > 0)
            anInt1264--;
        if (super.keyStatus[1] == 1 || super.keyStatus[2] == 1 || super.keyStatus[3] == 1
                || super.keyStatus[4] == 1)
            aBoolean1265 = true;
        if (aBoolean1265 && anInt1264 <= 0) {
            anInt1264 = 20;
            aBoolean1265 = false;
            outBuffer.putOpcode(140);
            outBuffer.putLEShortDup(anInt1251);
            outBuffer.putLEShortDup(anInt1252);
        }
        if (super.awtFocus && !aBoolean1275) {
            aBoolean1275 = true;
            outBuffer.putOpcode(187);
            outBuffer.putByte(1);
        }
        if (!super.awtFocus && aBoolean1275) {
            aBoolean1275 = false;
            outBuffer.putOpcode(187);
            outBuffer.putByte(0);
        }
        loadingStages();
        method36(16220);
        method152(-23763);
        anInt871++;
        if (anInt871 > 750)
            method59(1);
        method100(0);
        updateNpcs();
        method85(0);
        anInt951++;
        if (anInt1023 != 0) {
            anInt1022 += 20;
            if (anInt1022 >= 400)
                anInt1023 = 0;
        }
        if (anInt1332 != 0) {
            anInt1329++;
            if (anInt1329 >= 15) {
                if (anInt1332 == 2)
                    aBoolean1181 = true;
                if (anInt1332 == 3)
                    aBoolean1240 = true;
                anInt1332 = 0;
            }
        }
        if (anInt1113 != 0) {
            anInt1269++;
            if (super.mouseX > anInt1114 + 5 || super.mouseX < anInt1114 - 5 || super.mouseY > anInt1115 + 5
                    || super.mouseY < anInt1115 - 5)
                aBoolean1155 = true;
            if (super.anInt21 == 0) {
                if (anInt1113 == 2)
                    aBoolean1181 = true;
                if (anInt1113 == 3)
                    aBoolean1240 = true;
                anInt1113 = 0;
                if (aBoolean1155 && anInt1269 >= 5) {
                    anInt1064 = -1;
                    generateContextOptions(-521);
                    if (anInt1064 == anInt1111 && anInt1063 != anInt1112) {
                        JagInterface class13 = JagInterface.forId(anInt1111);
                        int i1 = 0;
                        if (anInt955 == 1 && class13.anInt242 == 206)
                            i1 = 1;
                        if (class13.itemIds[anInt1063] <= 0)
                            i1 = 0;
                        if (class13.aBoolean217) {
                            int k2 = anInt1112;
                            int k3 = anInt1063;
                            class13.itemIds[k3] = class13.itemIds[k2];
                            class13.itemAmounts[k3] = class13.itemAmounts[k2];
                            class13.itemIds[k2] = -1;
                            class13.itemAmounts[k2] = 0;
                        } else if (i1 == 1) {
                            int l2 = anInt1112;
                            for (int l3 = anInt1063; l2 != l3; )
                                if (l2 > l3) {
                                    class13.swapItems(l2 - 1, l2);
                                    l2--;
                                } else if (l2 < l3) {
                                    class13.swapItems(l2 + 1, l2);
                                    l2++;
                                }

                        } else {
                            class13.swapItems(anInt1063, anInt1112);
                        }
                        System.out.println("123, " + anInt1063 + ", " + i1 + ", " + anInt1111 + ", " + anInt1112);
                        outBuffer.putOpcode(123);
                        outBuffer.putLEShortAdded(anInt1063);
                        outBuffer.putByteAdded(i1);
                        outBuffer.putShortAdded(anInt1111);
                        outBuffer.putLEShortDup(anInt1112);
                    }
                } else if ((anInt1300 == 1 || method126(anInt1183 - 1, aByte1161)) && anInt1183 > 2)
                    method108(811);
                else if (anInt1183 > 0)
                    sendOutgoingPackets(anInt1183 - 1, 8);
                anInt1329 = 10;
                super.anInt28 = 0;
            }
        }
        if (SceneGraph.anInt485 != -1) {
            int dstX = SceneGraph.anInt485;
            int dstY = SceneGraph.anInt486;
            boolean flag = walk(true, false, dstY, ((Actor) (thisPlayer)).walkingQueueY[0], 0, 0, 0, 0, dstX, 0, 0,
                    ((Actor) (thisPlayer)).walkingQueueX[0]);
            SceneGraph.anInt485 = -1;
            if (flag) {
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 1;
                anInt1022 = 0;
            }
        }
        if (super.anInt28 == 1 && aString1058 != null) {
            aString1058 = null;
            aBoolean1240 = true;
            super.anInt28 = 0;
        }
        updateMouseClicks();
        if (anInt1053 == -1) {
            updateMinimapClick();
            processMouseClickOnTabs21();
            method39(true);
        }
        if (super.anInt21 == 1 || super.anInt28 == 1)
            anInt1094++;
        if (anInt1284 != 0 || anInt1044 != 0 || currentlyHovered1129 != 0) {
            if (anInt893 < 100) {
                anInt893++;
                if (anInt893 == 100) {
                    if (anInt1284 != 0)
                        aBoolean1240 = true;
                    if (anInt1044 != 0)
                        aBoolean1181 = true;
                }
            }
        } else if (anInt893 > 0)
            anInt893--;
        if (loadingStage == 2)
            loadPlayer22();
        if (loadingStage == 2 && aBoolean1211)
            method29(aBoolean959);
        for (int k = 0; k < 5; k++)
            unknownCameraVariable[k]++;

        updateChatbox();
        super.anInt20++;
        if (super.anInt20 > 4500) {
            anInt873 = 250;
            super.anInt20 -= 500;
            outBuffer.putOpcode(202);
        }
        anInt1118++;
        if (anInt1118 > 500) {
            anInt1118 = 0;
            int k1 = (int) (Math.random() * 8D);
            if ((k1 & 1) == 1)
                anInt853 += anInt854;
            if ((k1 & 2) == 2)
                anInt1009 += anInt1010;
            if ((k1 & 4) == 4)
                anInt1255 += anInt1256;
        }
        if (anInt853 < -50)
            anInt854 = 2;
        if (anInt853 > 50)
            anInt854 = -2;
        if (anInt1009 < -55)
            anInt1010 = 2;
        if (anInt1009 > 55)
            anInt1010 = -2;
        if (anInt1255 < -40)
            anInt1256 = 1;
        if (anInt1255 > 40)
            anInt1256 = -1;
        anInt1045++;
        if (anInt1045 > 500) {
            anInt1045 = 0;
            int l1 = (int) (Math.random() * 8D);
            if ((l1 & 1) == 1)
                anInt916 += anInt917;
            if ((l1 & 2) == 2)
                anInt1233 += anInt1234;
        }
        if (anInt916 < -60)
            anInt917 = 2;
        if (anInt916 > 60)
            anInt917 = -2;
        if (anInt1233 < -20)
            anInt1234 = 1;
        if (anInt1233 > 10)
            anInt1234 = -1;
        anInt872++;
        if (byte0 != 4)
            opcode = buffer.getByte();
        if (anInt872 > 50)
            outBuffer.putOpcode(40);
        try {
            if (connection != null && outBuffer.position > 0) {
                connection.putBytes(0, outBuffer.position, 0, outBuffer.buffer);
                outBuffer.position = 0;
                anInt872 = 0;
                return;
            }
        } catch (IOException _ex) {
            method59(1);
            return;
        } catch (Exception exception) {
            method124(true);
        }
    }

    public void method29(boolean flag) {
        int i = anInt874 * 128 + 64;
        int j = anInt875 * 128 + 64;
        int k = getFloorDrawHeight(j, i, plane) - anInt876;
        if (anInt1216 < i) {
            anInt1216 += anInt877 + ((i - anInt1216) * anInt878) / 1000;
            if (anInt1216 > i)
                anInt1216 = i;
        }
        if (anInt1216 > i) {
            anInt1216 -= anInt877 + ((anInt1216 - i) * anInt878) / 1000;
            if (anInt1216 < i)
                anInt1216 = i;
        }
        if (anInt1217 < k) {
            anInt1217 += anInt877 + ((k - anInt1217) * anInt878) / 1000;
            if (anInt1217 > k)
                anInt1217 = k;
        }
        if (anInt1217 > k) {
            anInt1217 -= anInt877 + ((anInt1217 - k) * anInt878) / 1000;
            if (anInt1217 < k)
                anInt1217 = k;
        }
        if (anInt1218 < j) {
            anInt1218 += anInt877 + ((j - anInt1218) * anInt878) / 1000;
            if (anInt1218 > j)
                anInt1218 = j;
        }
        if (anInt1218 > j) {
            anInt1218 -= anInt877 + ((anInt1218 - j) * anInt878) / 1000;
            if (anInt1218 < j)
                anInt1218 = j;
        }
        i = anInt993 * 128 + 64;
        j = anInt994 * 128 + 64;
        k = getFloorDrawHeight(j, i, plane) - anInt995;
        int l = i - anInt1216;
        int i1 = k - anInt1217;
        int j1 = j - anInt1218;
        int k1 = (int) Math.sqrt(l * l + j1 * j1);
        int l1 = (int) (Math.atan2(i1, k1) * 325.94900000000001D) & 0x7ff;
        if (!flag) {
            for (int i2 = 1; i2 > 0; i2++) ;
        }
        int j2 = (int) (Math.atan2(l, j1) * -325.94900000000001D) & 0x7ff;
        if (l1 < 128)
            l1 = 128;
        if (l1 > 383)
            l1 = 383;
        if (anInt1219 < l1) {
            anInt1219 += anInt996 + ((l1 - anInt1219) * anInt997) / 1000;
            if (anInt1219 > l1)
                anInt1219 = l1;
        }
        if (anInt1219 > l1) {
            anInt1219 -= anInt996 + ((anInt1219 - l1) * anInt997) / 1000;
            if (anInt1219 < l1)
                anInt1219 = l1;
        }
        int k2 = j2 - anInt1220;
        if (k2 > 1024)
            k2 -= 2048;
        if (k2 < -1024)
            k2 += 2048;
        if (k2 > 0) {
            anInt1220 += anInt996 + (k2 * anInt997) / 1000;
            anInt1220 &= 0x7ff;
        }
        if (k2 < 0) {
            anInt1220 -= anInt996 + (-k2 * anInt997) / 1000;
            anInt1220 &= 0x7ff;
        }
        int l2 = j2 - anInt1220;
        if (l2 > 1024)
            l2 -= 2048;
        if (l2 < -1024)
            l2 += 2048;
        if (l2 < 0 && k2 > 0 || l2 > 0 && k2 < 0)
            anInt1220 = j2;
    }

    public void updateChatbox() {
        do {
            int key = readCharFromChatbox();
            if (key == -1)
                break;
            if (anInt1169 != -1 && anInt1169 == anInt1231) {
                if (key == 8 && aString839.length() > 0)
                    aString839 = aString839.substring(0, aString839.length() - 1);
                if ((key >= 97 && key <= 122 || key >= 65 && key <= 90 || key >= 48 && key <= 57 || key == 32)
                        && aString839.length() < 12)
                    aString839 += (char) key;
            } else if (aBoolean866) {
                if (key >= 32 && key <= 122 && userInputString.length() < 80) {
                    userInputString += (char) key;
                    aBoolean1240 = true;
                }
                if (key == 8 && userInputString.length() > 0) {
                    userInputString = userInputString.substring(0, userInputString.length() - 1);
                    aBoolean1240 = true;
                }
                if (key == 13 || key == 10) {
                    aBoolean866 = false;
                    aBoolean1240 = true;
                    if (anInt1221 == 1) {
                        long l = StringUtils.encodeBase37(userInputString);
                        addFriend(l, -45229);
                    }
                    if (anInt1221 == 2 && friendsCount > 0) {
                        long l1 = StringUtils.encodeBase37(userInputString);
                        method53(l1, 0);
                    }
                    if (anInt1221 == 3 && userInputString.length() > 0) {
                        outBuffer.putOpcode(227);
                        outBuffer.putByte(0);
                        int j = outBuffer.position;
                        outBuffer.putLong(aLong1141);
                        ChatCompressor.compress(userInputString, outBuffer);
                        outBuffer.putLength(outBuffer.position - j);
                        userInputString = ChatCompressor.format(userInputString);
                        //aString1026 = ChatFilter.applyCensor((byte) 0, aString1026);
                        pushMessage(StringUtils.formatPlayerName(StringUtils.decodeBase37(aLong1141)), (byte) -123,
                                userInputString, 6);
                        if (privateChatMode == 2) {
                            privateChatMode = 1;
                            aBoolean1212 = true;
                            outBuffer.putOpcode(176);
                            outBuffer.putByte(publicChatMode);
                            outBuffer.putByte(privateChatMode);
                            outBuffer.putByte(tradeMode);
                        }
                    }
                    if (anInt1221 == 4 && ignoresCount < 100) {
                        long l2 = StringUtils.encodeBase37(userInputString);
                        method90(anInt1154, l2);
                    }
                    if (anInt1221 == 5 && ignoresCount > 0) {
                        long l3 = StringUtils.encodeBase37(userInputString);
                        removeIgnore(325, l3);
                    }
                }
            } else if (chatboxInterfaceType == 1) {
                if (key >= 48 && key <= 57 && chatboxInput.length() < 10) {
                    chatboxInput += (char) key;
                    aBoolean1240 = true;
                }
                if (key == 8 && chatboxInput.length() > 0) {
                    chatboxInput = chatboxInput.substring(0, chatboxInput.length() - 1);
                    aBoolean1240 = true;
                }
                if (key == 13 || key == 10) {
                    if (chatboxInput.length() > 0) {
                        int k = 0;
                        try {
                            k = Integer.parseInt(chatboxInput);
                        } catch (Exception _ex) {
                        }
                        outBuffer.putOpcode(75);
                        outBuffer.putInt(k);
                    }
                    chatboxInterfaceType = 0;
                    aBoolean1240 = true;
                }
            } else if (chatboxInterfaceType == 2) {
                if (key >= 32 && key <= 122 && chatboxInput.length() < 12) {
                    chatboxInput += (char) key;
                    aBoolean1240 = true;
                }
                if (key == 8 && chatboxInput.length() > 0) {
                    chatboxInput = chatboxInput.substring(0, chatboxInput.length() - 1);
                    aBoolean1240 = true;
                }
                if (key == 13 || key == 10) {
                    if (chatboxInput.length() > 0) {
                        outBuffer.putOpcode(206);
                        outBuffer.putLong(StringUtils.encodeBase37(chatboxInput));
                    }
                    chatboxInterfaceType = 0;
                    aBoolean1240 = true;
                }
            } else if (chatboxInterfaceType == 3) {
                if (key >= 32 && key <= 122 && chatboxInput.length() < 40) {
                    chatboxInput += (char) key;
                    aBoolean1240 = true;
                }
                if (key == 8 && chatboxInput.length() > 0) {
                    chatboxInput = chatboxInput.substring(0, chatboxInput.length() - 1);
                    aBoolean1240 = true;
                }
            } else if (anInt988 == -1 && anInt1053 == -1) {
                if (key >= 32 && key <= 122 && chatInput.length() < 80) {
                    chatInput += (char) key;
                    aBoolean1240 = true;
                }
                if (key == 8 && chatInput.length() > 0) {
                    chatInput = chatInput.substring(0, chatInput.length() - 1);
                    aBoolean1240 = true;
                }
                if ((key == 13 || key == 10) && chatInput.length() > 0) {
                    // client side commands for everyone, these are not sent to the server
                    boolean clientCommand = handleClientCommand(chatInput);
                    if (playerRights == 2) {
                        if (chatInput.equals("::clientdrop")) {
                            method59(1);
                        }
                        if (chatInput.equals("::lag")) {
                            printLagInfo(false);
                        }
                        if (chatInput.equals("::dumpobjdefs")) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < ObjectDefinition.count; i++) {
                                try {
                                    ObjectDefinition def = ObjectDefinition.forId(i);
                                    if (def == null)
                                        continue;
                                    sb.append(def).append('\n');
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                Files.writeString(Paths.get("objects.txt"), sb);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if (chatInput.equals("::prefetchmusic")) {
                            for (int i1 = 0; i1 < fileFetcher.method340(2); i1++)
                                fileFetcher.method327(-44, 2, (byte) 1, i1);

                        }
                        if (chatInput.equals("::dumpitemdefs")) {
                            String comma = ", ";
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < ItemDefinition.count; i++) {
                                try {
                                    ItemDefinition def = ItemDefinition.forId(i);
                                    if (def == null)
                                        continue;
                                    sb.append(i).
                                            append(comma).
                                            append(def.name).append('\n');
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                Files.writeString(Paths.get("items.txt"), sb);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if (chatInput.equals("::fpson"))
                            fps = true;
                        if (chatInput.equals("::fpsoff"))
                            fps = false;
                        if (chatInput.equals("::noclip")) {
                            for (int j1 = 0; j1 < 4; j1++) {
                                for (int k1 = 1; k1 < 103; k1++) {
                                    for (int j2 = 1; j2 < 103; j2++)
                                        clippingPlanes[j1].masks[k1][j2] = 0;

                                }

                            }

                        }
                    }
                    if (clientCommand) {
                        // already handled by the client
                    } else if (chatInput.startsWith("::")) {
                        outBuffer.putOpcode(56);
                        outBuffer.putByte(chatInput.length() - 1);
                        outBuffer.putString(chatInput.substring(2));
                    } else {
                        String s = chatInput.toLowerCase();
                        int colour = 0;
                        if (s.startsWith("yellow:")) {
                            colour = 0;
                            chatInput = chatInput.substring(7);
                        } else if (s.startsWith("red:")) {
                            colour = 1;
                            chatInput = chatInput.substring(4);
                        } else if (s.startsWith("green:")) {
                            colour = 2;
                            chatInput = chatInput.substring(6);
                        } else if (s.startsWith("cyan:")) {
                            colour = 3;
                            chatInput = chatInput.substring(5);
                        } else if (s.startsWith("purple:")) {
                            colour = 4;
                            chatInput = chatInput.substring(7);
                        } else if (s.startsWith("white:")) {
                            colour = 5;
                            chatInput = chatInput.substring(6);
                        } else if (s.startsWith("flash1:")) {
                            colour = 6;
                            chatInput = chatInput.substring(7);
                        } else if (s.startsWith("flash2:")) {
                            colour = 7;
                            chatInput = chatInput.substring(7);
                        } else if (s.startsWith("flash3:")) {
                            colour = 8;
                            chatInput = chatInput.substring(7);
                        } else if (s.startsWith("glow1:")) {
                            colour = 9;
                            chatInput = chatInput.substring(6);
                        } else if (s.startsWith("glow2:")) {
                            colour = 10;
                            chatInput = chatInput.substring(6);
                        } else if (s.startsWith("glow3:")) {
                            colour = 11;
                            chatInput = chatInput.substring(6);
                        }
                        s = chatInput.toLowerCase();
                        int movement = 0;
                        if (s.startsWith("wave:")) {
                            movement = 1;
                            chatInput = chatInput.substring(5);
                        } else if (s.startsWith("wave2:")) {
                            movement = 2;
                            chatInput = chatInput.substring(6);
                        } else if (s.startsWith("shake:")) {
                            movement = 3;
                            chatInput = chatInput.substring(6);
                        } else if (s.startsWith("scroll:")) {
                            movement = 4;
                            chatInput = chatInput.substring(7);
                        } else if (s.startsWith("slide:")) {
                            movement = 5;
                            chatInput = chatInput.substring(6);
                        }
                        outBuffer.putOpcode(49);
                        outBuffer.putByte(0);
                        int i3 = outBuffer.position;
                        outBuffer.putByteNegated(colour);
                        outBuffer.putByteAdded(movement);
                        aClass50_Sub1_Sub2_1131.position = 0;
                        ChatCompressor.compress(chatInput, aClass50_Sub1_Sub2_1131);
                        outBuffer.putBytes(aClass50_Sub1_Sub2_1131.buffer, 0,
                                aClass50_Sub1_Sub2_1131.position);
                        outBuffer.putLength(outBuffer.position - i3);
                        chatInput = ChatCompressor.format(chatInput);
                        //  chatInput = ChatFilter.applyCensor((byte) 0, chatInput);
                        thisPlayer.forcedChatMessage = chatInput;
                        thisPlayer.anInt1583 = colour;
                        thisPlayer.anInt1593 = movement;
                        thisPlayer.forcedChatTicks = 150;
                        if (playerRights == 2)
                            pushMessage("@cr2@" + thisPlayer.username, (byte) -123,
                                    ((Actor) (thisPlayer)).forcedChatMessage, 2);
                        else if (playerRights == 1)
                            pushMessage("@cr1@" + thisPlayer.username, (byte) -123,
                                    ((Actor) (thisPlayer)).forcedChatMessage, 2);
                        else
                            pushMessage(thisPlayer.username, (byte) -123, ((Actor) (thisPlayer)).forcedChatMessage, 2);
                        if (publicChatMode == 2) {
                            publicChatMode = 3;
                            aBoolean1212 = true;
                            outBuffer.putOpcode(176);
                            outBuffer.putByte(publicChatMode);
                            outBuffer.putByte(privateChatMode);
                            outBuffer.putByte(tradeMode);
                        }
                    }
                    chatInput = "";
                    aBoolean1240 = true;
                }
            }
        } while (true);
    }

    public DataInputStream method31(String s) throws IOException {
        if (!aBoolean900)
            if (signlink.mainapp != null)
                return signlink.openurl(s);
            else
                return new DataInputStream((new URL(getCodeBase(), s)).openStream());
        if (aSocket1224 != null) {
            try {
                aSocket1224.close();
            } catch (Exception _ex) {
            }
            aSocket1224 = null;
        }
        aSocket1224 = openSocket(43595);
        aSocket1224.setSoTimeout(10000);
        java.io.InputStream inputstream = aSocket1224.getInputStream();
        OutputStream outputstream = aSocket1224.getOutputStream();
        outputstream.write(("JAGGRAB /" + s + "\n\n").getBytes());
        return new DataInputStream(inputstream);
    }

    public Socket openSocket(int i) throws IOException {
        if (signlink.mainapp != null)
            return signlink.opensocket(i);
        else
            return new Socket(InetAddress.getByName(getCodeBase().getHost()), i);
    }

    public boolean parseIncomingPacket() {
        if (connection == null)
            return false;
        try {
            int avail = connection.available();
            if (avail == 0)
                return false;
            if (opcode == -1) {
                connection.getBytes(buffer.buffer, 0, 1);
                opcode = buffer.buffer[0] & 0xff;
                if (incomingRandom != null)
                    opcode = opcode - incomingRandom.nextInt() & 0xff;
                size = SizeConstants.INCOMING_PACKET_LENGTHS[opcode];
                avail--;
            }
            if (size == -1)
                if (avail > 0) {
                    connection.getBytes(buffer.buffer, 0, 1);
                    size = buffer.buffer[0] & 0xff;
                    avail--;
                } else {
                    return false;
                }
            if (size == -2)
                if (avail > 1) {
                    connection.getBytes(buffer.buffer, 0, 2);
                    buffer.position = 0;
                    size = buffer.getShort();
                    avail -= 2;
                } else {
                    return false;
                }

            if (avail < size)
                return false;
            buffer.position = 0;
            connection.getBytes(buffer.buffer, 0, size);
            anInt871 = 0;
            anInt905 = lastOpcode;
            lastOpcode = anInt903;
            anInt903 = opcode;
            if (opcode == 166) {
                int l = buffer.method552();
                int l10 = buffer.method552();
                int interfaceId = buffer.getShort();
                JagInterface class13_5 = JagInterface.forId(interfaceId);
                class13_5.anInt228 = l10;
                class13_5.anInt259 = l;
                opcode = -1;
                return true;
            }
            if (opcode == 186) {
                int i1 = buffer.getShortAdded();
                int interfaceId = buffer.getLEShortA();
                int l16 = buffer.getShortAdded();
                int i22 = buffer.getLEShort();
                JagInterface.forId(interfaceId).anInt252 = i1;
                JagInterface.forId(interfaceId).anInt253 = i22;
                JagInterface.forId(interfaceId).anInt251 = l16;
                opcode = -1;
                return true;
            }
            if (opcode == 216) {
                int j1 = buffer.getLEShortA();
                int interfaceId = buffer.getLEShortA();
                JagInterface.forId(interfaceId).anInt283 = 1;
                JagInterface.forId(interfaceId).anInt284 = j1;
                opcode = -1;
                return true;
            }
            if (opcode == 26) {
                int k1 = buffer.getShort();
                int k11 = buffer.getByte();
                int i17 = buffer.getShort();
                if (i17 == 65535) {
                    if (anInt1035 < 50) {
                        anIntArray1090[anInt1035] = (short) k1;
                        anIntArray1321[anInt1035] = k11;
                        anIntArray1259[anInt1035] = 0;
                        anInt1035++;
                    }
                } else if (aBoolean1301 && !lowMemory && anInt1035 < 50) {
                    anIntArray1090[anInt1035] = k1;
                    anIntArray1321[anInt1035] = k11;
                    anIntArray1259[anInt1035] = i17 + Sound.anIntArray669[k1];
                    anInt1035++;
                }
                opcode = -1;
                return true;
            }
            if (opcode == 182) {
                int varpId = buffer.getShortAdded();
                byte value = buffer.getSignedByteSubtracted();
                defaultLocalVarps[varpId] = value;
                if (localVarps[varpId] != value) {
                    localVarps[varpId] = value;
                    handleVarp(varpId);
                    aBoolean1181 = true;
                    if (anInt1191 != -1)
                        aBoolean1240 = true;
                }
                opcode = -1;
                return true;
            }
            if (opcode == 13) {
                for (int i2 = 0; i2 < players.length; i2++)
                    if (players[i2] != null)
                        players[i2].currentAnimation = -1;

                for (int l11 = 0; l11 < npcs.length; l11++)
                    if (npcs[l11] != null)
                        npcs[l11].currentAnimation = -1;

                opcode = -1;
                return true;
            }
            if (opcode == 156) {
                minimapState = buffer.getByte(); // 0 normal, 1 unclickable, 2 blacked out
                opcode = -1;
                return true;
            }
            if (opcode == 162) {
                int j2 = buffer.getShortAdded();
                int interfaceId = buffer.getLEShort();
                JagInterface.forId(interfaceId).anInt283 = 2;
                JagInterface.forId(interfaceId).anInt284 = j2;
                opcode = -1;
                return true;
            }
            if (opcode == 109) {
                int k2 = buffer.getShort();
                method112((byte) 36, k2);
                if (anInt1089 != -1) {
                    method44(anInt1089);
                    anInt1089 = -1;
                    aBoolean1181 = true;
                    aBoolean950 = true;
                }
                if (anInt1053 != -1) {
                    method44(anInt1053);
                    anInt1053 = -1;
                    shouldRenderUI = true;
                }
                if (openInterfaceID != -1) {
                    method44(openInterfaceID);
                    openInterfaceID = -1;
                }
                if (anInt1169 != -1) {
                    method44(anInt1169);
                    anInt1169 = -1;
                }
                if (anInt988 != k2) {
                    method44(anInt988);
                    anInt988 = k2;
                }
                aBoolean1239 = false;
                aBoolean1240 = true;
                opcode = -1;
                return true;
            }
            if (opcode == 220) {
                int l2 = buffer.getLEShortA();
                if (l2 == 65535)
                    l2 = -1;
                if (l2 != anInt1327 && musicEnabled && !lowMemory && anInt1128 == 0) {
                    anInt1270 = l2;
                    aBoolean1271 = true;
                    fileFetcher.request(2, anInt1270);
                }
                anInt1327 = l2;
                opcode = -1;
                return true;
            }
            if (opcode == 249) {
                int fileId = buffer.getLEShort();
                int j12 = buffer.method554();
                if (musicEnabled && !lowMemory) {
                    anInt1270 = fileId;
                    aBoolean1271 = false;
                    fileFetcher.request(2, anInt1270); // request something from cache!?!
                    anInt1128 = j12;
                }
                opcode = -1;
                return true;
            }
            if (opcode == 158) {
                int j3 = buffer.method552();
                if (j3 != anInt1191) {
                    method44(anInt1191);
                    anInt1191 = j3;
                }
                aBoolean1240 = true;
                opcode = -1;
                return true;
            }
            if (opcode == 218) { // set interface colour(?)
                int interfaceId = buffer.getShort();
                int rgb = buffer.getShortAdded();
                int j17 = rgb >> 10 & 0x1f;
                int j22 = rgb >> 5 & 0x1f;
                int l24 = rgb & 0x1f;
                JagInterface.forId(interfaceId).anInt240 = (j17 << 19) + (j22 << 11) + (l24 << 3);
                opcode = -1;
                return true;
            }
            if (opcode == 157) { // update player option
                int slot = buffer.getByteNegated();
                String option = buffer.getString();
                int alwaysOnTop = buffer.getByte();
                if (slot >= 1 && slot <= 5) {
                    if (option.equalsIgnoreCase("null"))
                        option = null;
                    aStringArray1069[slot - 1] = option;
                    aBooleanArray1070[slot - 1] = alwaysOnTop == 0;
                }
                opcode = -1;
                return true;
            }
            if (opcode == 6) {
                aBoolean866 = false;
                chatboxInterfaceType = 2;
                chatboxInput = "";
                aBoolean1240 = true;
                opcode = -1;
                return true;
            }
            if (opcode == 201) {
                publicChatMode = buffer.getByte();
                privateChatMode = buffer.getByte();
                tradeMode = buffer.getByte();
                aBoolean1212 = true;
                aBoolean1240 = true;
                opcode = -1;
                return true;
            }
            if (opcode == 199) {
                anInt1197 = buffer.getByte();
                if (anInt1197 == 1)
                    anInt1226 = buffer.getShort();
                if (anInt1197 >= 2 && anInt1197 <= 6) {
                    if (anInt1197 == 2) {
                        anInt847 = 64;
                        anInt848 = 64;
                    }
                    if (anInt1197 == 3) {
                        anInt847 = 0;
                        anInt848 = 64;
                    }
                    if (anInt1197 == 4) {
                        anInt847 = 128;
                        anInt848 = 64;
                    }
                    if (anInt1197 == 5) {
                        anInt847 = 64;
                        anInt848 = 0;
                    }
                    if (anInt1197 == 6) {
                        anInt847 = 64;
                        anInt848 = 128;
                    }
                    anInt1197 = 2;
                    anInt844 = buffer.getShort();
                    anInt845 = buffer.getShort();
                    anInt846 = buffer.getByte();
                }
                if (anInt1197 == 10)
                    anInt1151 = buffer.getShort();
                opcode = -1;
                return true;
            }
            if (opcode == 167) {
                aBoolean1211 = true;
                anInt993 = buffer.getByte();
                anInt994 = buffer.getByte();
                anInt995 = buffer.getShort();
                anInt996 = buffer.getByte();
                anInt997 = buffer.getByte();
                if (anInt997 >= 100) {
                    int i4 = anInt993 * 128 + 64;
                    int l12 = anInt994 * 128 + 64;
                    int l17 = getFloorDrawHeight(l12, i4, plane) - anInt995;
                    int k22 = i4 - anInt1216;
                    int i25 = l17 - anInt1217;
                    int k27 = l12 - anInt1218;
                    int i30 = (int) Math.sqrt(k22 * k22 + k27 * k27);
                    anInt1219 = (int) (Math.atan2(i25, i30) * 325.94900000000001D) & 0x7ff;
                    anInt1220 = (int) (Math.atan2(k22, k27) * -325.94900000000001D) & 0x7ff;
                    if (anInt1219 < 128)
                        anInt1219 = 128;
                    if (anInt1219 > 383)
                        anInt1219 = 383;
                }
                opcode = -1;
                return true;
            }
            if (opcode == 5) {
                method124(true); // simulate a crash??
                opcode = -1;
                return false;
            }
            if (opcode == 115) {
                int value = buffer.getIMInt();
                int varpId = buffer.getLEShort();
                defaultLocalVarps[varpId] = value;
                if (localVarps[varpId] != value) {
                    localVarps[varpId] = value;
                    handleVarp(varpId);
                    aBoolean1181 = true;
                    if (anInt1191 != -1)
                        aBoolean1240 = true;
                }
                opcode = -1;
                return true;
            }
            if (opcode == 29) { // close open interfaces??
                if (anInt1089 != -1) {
                    method44(anInt1089);
                    anInt1089 = -1;
                    aBoolean1181 = true;
                    aBoolean950 = true;
                }
                if (anInt988 != -1) {
                    method44(anInt988);
                    anInt988 = -1;
                    aBoolean1240 = true;
                }
                if (anInt1053 != -1) {
                    method44(anInt1053);
                    anInt1053 = -1;
                    shouldRenderUI = true;
                }
                if (openInterfaceID != -1) {
                    method44(openInterfaceID);
                    openInterfaceID = -1;
                }
                if (anInt1169 != -1) {
                    method44(anInt1169);
                    anInt1169 = -1;
                }
                if (chatboxInterfaceType != 0) {
                    chatboxInterfaceType = 0;
                    aBoolean1240 = true;
                }
                aBoolean1239 = false;
                opcode = -1;
                return true;
            }
            if (opcode == 76) { // open welcome screen
                lastPasswordChange = buffer.getLEShort();
                buffer.getLEShortA();
                buffer.getShort();
                buffer.getShort();
                lastLoginDays = buffer.getLEShort();// last login date
                unreadMessages = buffer.getShortAdded(); // unread messages
                somethngLoginDays = buffer.getShortAdded(); // ??
                membershipDaysRemaining = buffer.getShort();
                lastAddress = buffer.getLittleInt();
                recoveryQuestionDays = buffer.getLEShortA();
                buffer.getByteAdded();
                signlink.dnslookup(StringUtils.ipBitsToString(lastAddress));
                opcode = -1;
                return true;
            }
            if (opcode == 63) { // server message
                String message = buffer.getString();
                if (message.endsWith(":tradereq:")) {
                    String s3 = message.substring(0, message.indexOf(":"));
                    long l18 = StringUtils.encodeBase37(s3);
                    boolean flag1 = false;
                    for (int l27 = 0; l27 < ignoresCount; l27++) {
                        if (ignores[l27] != l18)
                            continue;
                        flag1 = true;
                        break;
                    }

                    if (!flag1 && anInt1246 == 0)
                        pushMessage(s3, (byte) -123, "wishes to trade with you.", 4);
                } else if (message.endsWith(":duelreq:")) {
                    String s4 = message.substring(0, message.indexOf(":"));
                    long l19 = StringUtils.encodeBase37(s4);
                    boolean flag2 = false;
                    for (int i28 = 0; i28 < ignoresCount; i28++) {
                        if (ignores[i28] != l19)
                            continue;
                        flag2 = true;
                        break;
                    }

                    if (!flag2 && anInt1246 == 0)
                        pushMessage(s4, (byte) -123, "wishes to duel with you.", 8);
                } else if (message.endsWith(":chalreq:")) {
                    String s5 = message.substring(0, message.indexOf(":"));
                    long l20 = StringUtils.encodeBase37(s5);
                    boolean flag3 = false;
                    for (int j28 = 0; j28 < ignoresCount; j28++) {
                        if (ignores[j28] != l20)
                            continue;
                        flag3 = true;
                        break;
                    }

                    if (!flag3 && anInt1246 == 0) {
                        String s8 = message.substring(message.indexOf(":") + 1, message.length() - 9);
                        pushMessage(s5, (byte) -123, s8, 8);
                    }
                } else {
                    pushMessage("", (byte) -123, message, 0);
                }
                opcode = -1;
                return true;
            }
            if (opcode == 50) {
                int id = buffer.getSignedShort();
                if (id >= 0)
                    method112((byte) 36, id);
                if (id != walkableInterfaceId) {
                    method44(walkableInterfaceId);
                    walkableInterfaceId = id;
                }
                opcode = -1;
                return true;
            }
            if (opcode == 82) { // make interface (in)visible maybe?
                boolean flag = buffer.getByte() == 1;
                int interfaceId = buffer.getShort();
                JagInterface.forId(interfaceId).aBoolean219 = flag;
                opcode = -1;
                return true;
            }
            if (opcode == 174) {
                if (tabId == 12)
                    aBoolean1181 = true;
                anInt1030 = buffer.getSignedShort();
                opcode = -1;
                return true;
            }
            if (opcode == 233) {
                anInt1319 = buffer.getByte();
                opcode = -1;
                return true;
            }
            if (opcode == 61) {
                anInt1120 = 0;
                opcode = -1;
                return true;
            }
            if (opcode == 128) {
                int l4 = buffer.getShortAdded();
                int k13 = buffer.getLEShortA();
                if (anInt988 != -1) {
                    method44(anInt988);
                    anInt988 = -1;
                    aBoolean1240 = true;
                }
                if (anInt1053 != -1) {
                    method44(anInt1053);
                    anInt1053 = -1;
                    shouldRenderUI = true;
                }
                if (openInterfaceID != -1) {
                    method44(openInterfaceID);
                    openInterfaceID = -1;
                }
                if (anInt1169 != l4) {
                    method44(anInt1169);
                    anInt1169 = l4;
                }
                if (anInt1089 != k13) {
                    method44(anInt1089);
                    anInt1089 = k13;
                }
                if (chatboxInterfaceType != 0) {
                    chatboxInterfaceType = 0;
                    aBoolean1240 = true;
                }
                aBoolean1181 = true;
                aBoolean950 = true;
                aBoolean1239 = false;
                opcode = -1;
                return true;
            }
            if (opcode == 67) {
                int cameraId = buffer.getByte();
                int jitter = buffer.getByte();
                int amplitude = buffer.getByte();
                int frequency = buffer.getByte();
                customCameraActive[cameraId] = true;
                cameraJitter[cameraId] = jitter;
                cameraAmplitude[cameraId] = amplitude;
                cameraFrequency[cameraId] = frequency;
                unknownCameraVariable[cameraId] = 0;
                opcode = -1;
                return true;
            }
            if (opcode == 134) { // set items in interface
                aBoolean1181 = true;
                int interfaceId = buffer.getShort();
                JagInterface inter = JagInterface.forId(interfaceId);
                while (buffer.position < size) {
                    int slot = buffer.getSmart();
                    int id = buffer.getShort();
                    int amount = buffer.getByte();
                    if (amount == 255)
                        amount = buffer.getInt();
                    if (slot >= 0 && slot < inter.itemIds.length) {
                        inter.itemIds[slot] = id;
                        inter.itemAmounts[slot] = amount;
                    }
                }
                opcode = -1;
                return true;
            }
            if (opcode == 78) { // update friend status
                long friend = buffer.getLong();
                int nodeId = buffer.getByte();
                String s7 = StringUtils.formatPlayerName(StringUtils.decodeBase37(friend));
                for (int k25 = 0; k25 < friendsCount; k25++) {
                    if (friend != friends[k25])
                        continue;
                    if (anIntArray1267[k25] != nodeId) {
                        anIntArray1267[k25] = nodeId;
                        aBoolean1181 = true;
                        if (nodeId > 0)
                            pushMessage("", (byte) -123, s7 + " has logged in.", 5);
                        if (nodeId == 0)
                            pushMessage("", (byte) -123, s7 + " has logged out.", 5);
                    }
                    s7 = null;
                    break;
                }

                if (s7 != null && friendsCount < 200) {
                    friends[friendsCount] = friend;
                    aStringArray849[friendsCount] = s7;
                    anIntArray1267[friendsCount] = nodeId;
                    friendsCount++;
                    aBoolean1181 = true;
                }
                for (boolean flag5 = false; !flag5; ) {
                    flag5 = true;
                    for (int j30 = 0; j30 < friendsCount - 1; j30++)
                        if (anIntArray1267[j30] != world && anIntArray1267[j30 + 1] == world
                                || anIntArray1267[j30] == 0 && anIntArray1267[j30 + 1] != 0) {
                            int l31 = anIntArray1267[j30];
                            anIntArray1267[j30] = anIntArray1267[j30 + 1];
                            anIntArray1267[j30 + 1] = l31;
                            String s10 = aStringArray849[j30];
                            aStringArray849[j30] = aStringArray849[j30 + 1];
                            aStringArray849[j30 + 1] = s10;
                            long l33 = friends[j30];
                            friends[j30] = friends[j30 + 1];
                            friends[j30 + 1] = l33;
                            aBoolean1181 = true;
                            flag5 = false;
                        }

                }

                opcode = -1;
                return true;
            }
            if (opcode == 58) { // enter amount interface
                aBoolean866 = false;
                chatboxInterfaceType = 1;
                chatboxInput = "";
                aBoolean1240 = true;
                opcode = -1;
                return true;
            }
            if (opcode == 252) {
                tabId = buffer.getByteNegated();
                aBoolean1181 = true;
                aBoolean950 = true;
                opcode = -1;
                return true;
            }
            if (opcode == 40) {
                placementY = buffer.getByteSubtracted();
                placementX = buffer.getByteNegated();
                for (int k5 = placementX; k5 < placementX + 8; k5++) {
                    for (int i14 = placementY; i14 < placementY + 8; i14++)
                        if (groundItems[plane][k5][i14] != null) {
                            groundItems[plane][k5][i14] = null;
                            method26(k5, i14);
                        }

                }

                for (GameObjectSpawnRequest gameObjectSpawnRequest = (GameObjectSpawnRequest) gameObjectSpawnsRequestList.first();
                     gameObjectSpawnRequest != null;
                     gameObjectSpawnRequest = (GameObjectSpawnRequest) gameObjectSpawnsRequestList.next())
                    if (gameObjectSpawnRequest.anInt1393 >= placementX && gameObjectSpawnRequest.anInt1393 < placementX + 8
                            && gameObjectSpawnRequest.anInt1394 >= placementY && gameObjectSpawnRequest.anInt1394 < placementY + 8
                            && gameObjectSpawnRequest.anInt1391 == plane)
                        gameObjectSpawnRequest.delayUntilRespawn = 0;

                opcode = -1;
                return true;
            }
            if (opcode == 255) { // show player in an interface *maybe*?
                int interfaceId = buffer.getLEShortA();
                JagInterface.forId(interfaceId).anInt283 = 3;
                if (thisPlayer.npc == null) // maybe that is the appear as npc thing?
                    JagInterface.forId(interfaceId).anInt284 = (thisPlayer.colors[0] << 25) + (thisPlayer.colors[4] << 20)
                            + (thisPlayer.equipment[0] << 15) + (thisPlayer.equipment[8] << 10)
                            + (thisPlayer.equipment[11] << 5) + thisPlayer.equipment[1];
                else
                    JagInterface.forId(interfaceId).anInt284 = (int) (0x12345678L + thisPlayer.npc.id);
                opcode = -1;
                return true;
            }
            if (opcode == 135) { // private message (?)
                long recipientName = buffer.getLong();
                int messageId = buffer.getInt();
                int rights = buffer.getByte();
                boolean flag4 = false;
                for (int k28 = 0; k28 < 100; k28++) {
                    if (anIntArray1258[k28] != messageId)
                        continue;
                    flag4 = true;
                    break;
                }

                if (rights <= 1) {
                    for (int k30 = 0; k30 < ignoresCount; k30++) {
                        if (ignores[k30] != recipientName)
                            continue;
                        flag4 = true;
                        break;
                    }

                }
                if (!flag4 && anInt1246 == 0)
                    try {
                        anIntArray1258[anInt1152] = messageId;
                        anInt1152 = (anInt1152 + 1) % 100;
                        String s9 = ChatCompressor.decompress(buffer, size - 13);
                        if (rights != 3)
                            //s9 = ChatFilter.applyCensor((byte) 0, s9);
                            if (rights == 2 || rights == 3)
                                pushMessage("@cr2@" + StringUtils.formatPlayerName(StringUtils.decodeBase37(recipientName)), (byte) -123,
                                        s9, 7);
                            else if (rights == 1)
                                pushMessage("@cr1@" + StringUtils.formatPlayerName(StringUtils.decodeBase37(recipientName)), (byte) -123,
                                        s9, 7);
                            else
                                pushMessage(StringUtils.formatPlayerName(StringUtils.decodeBase37(recipientName)), (byte) -123, s9, 3);
                    } catch (Exception exception1) {
                        signlink.reporterror("cde1");
                    }
                opcode = -1;
                return true;
            }
            if (opcode == 183) {
                placementX = buffer.getByte();
                placementY = buffer.getByteAdded();
                while (buffer.position < size) {
                    int j6 = buffer.getByte();
                    parsePlacementPacket(buffer, j6);
                }
                opcode = -1;
                return true;
            }
            if (opcode == 159) { // open interface
                int interfaceId = buffer.getLEShortA();
                method112((byte) 36, interfaceId);
                if (anInt1089 != -1) {
                    method44(anInt1089);
                    anInt1089 = -1;
                    aBoolean1181 = true;
                    aBoolean950 = true;
                }
                if (anInt988 != -1) {
                    method44(anInt988);
                    anInt988 = -1;
                    aBoolean1240 = true;
                }
                if (anInt1053 != -1) {
                    method44(anInt1053);
                    anInt1053 = -1;
                    shouldRenderUI = true;
                }
                if (openInterfaceID != -1) {
                    method44(openInterfaceID);
                    openInterfaceID = -1;
                }
                if (anInt1169 != interfaceId) {
                    method44(anInt1169);
                    anInt1169 = interfaceId;
                }
                if (chatboxInterfaceType != 0) {
                    chatboxInterfaceType = 0;
                    aBoolean1240 = true;
                }
                aBoolean1239 = false;
                opcode = -1;
                return true;
            }
            if (opcode == 246) {
                int i7 = buffer.getLEShortA();
                method112((byte) 36, i7);
                if (anInt988 != -1) {
                    method44(anInt988);
                    anInt988 = -1;
                    aBoolean1240 = true;
                }
                if (anInt1053 != -1) {
                    method44(anInt1053);
                    anInt1053 = -1;
                    shouldRenderUI = true;
                }
                if (openInterfaceID != -1) {
                    method44(openInterfaceID);
                    openInterfaceID = -1;
                }
                if (anInt1169 != -1) {
                    method44(anInt1169);
                    anInt1169 = -1;
                }
                if (anInt1089 != i7) {
                    method44(anInt1089);
                    anInt1089 = i7;
                }
                if (chatboxInterfaceType != 0) {
                    chatboxInterfaceType = 0;
                    aBoolean1240 = true;
                }
                aBoolean1181 = true;
                aBoolean950 = true;
                aBoolean1239 = false;
                opcode = -1;
                return true;
            }
            if (opcode == 49) {
                aBoolean1181 = true;
                int j7 = buffer.getByteNegated();
                int j14 = buffer.getByte();
                int j19 = buffer.getInt();
                anIntArray843[j7] = j19;
                anIntArray1029[j7] = j14;
                anIntArray1054[j7] = 1;
                for (int k23 = 0; k23 < 98; k23++)
                    if (j19 >= anIntArray952[k23])
                        anIntArray1054[j7] = k23 + 2;

                opcode = -1;
                return true;
            }
            if (opcode == 206) { // update all items in interface
                aBoolean1181 = true;
                int interfaceId = buffer.getShort();
                JagInterface inter = JagInterface.forId(interfaceId);
                int items = buffer.getShort();
                for (int item = 0; item < items; item++) {
                    inter.itemIds[item] = buffer.getLEShortA();
                    int amount = buffer.getByteNegated();
                    if (amount == 255)
                        amount = buffer.getLittleInt();
                    inter.itemAmounts[item] = amount;
                }

                for (int i26 = items; i26 < inter.itemIds.length; i26++) {
                    inter.itemIds[i26] = 0;
                    inter.itemAmounts[i26] = 0;
                }

                opcode = -1;
                return true;
            }
            if (opcode == 222 || opcode == 53) { // new map region
                int tmpChunkX = chunkX;
                int tmpChunkY = chunkY;
                if (opcode == 222) {
                    tmpChunkY = buffer.getShort();
                    tmpChunkX = buffer.getLEShortA();
                    aBoolean1163 = false;
                }
                if (opcode == 53) {
                    tmpChunkX = buffer.getShortAdded();
                    buffer.initBitAccess();
                    for (int z = 0; z < 4; z++) {
                        for (int x = 0; x < 13; x++) {
                            for (int y = 0; y < 13; y++) {
                                int flag = buffer.getBits(1);
                                if (flag == 1)
                                    constructedMapPalette[z][x][y] = buffer.getBits(26);
                                else
                                    constructedMapPalette[z][x][y] = -1;
                            }

                        }

                    }

                    buffer.finishBitAccess();
                    tmpChunkY = buffer.getShortAdded();
                    aBoolean1163 = true;
                }
                if (opcode != 53 && chunkX == tmpChunkX && chunkY == tmpChunkY && loadingStage == 2) {
                    opcode = -1;
                    return true;
                }
                chunkX = tmpChunkX;
                chunkY = tmpChunkY;
                nextTopLeftTileX = (chunkX - 6) * 8;
                nextTopLeftTileY = (chunkY - 6) * 8;
                aBoolean1067 = false;
                if ((chunkX / 8 == 48 || chunkX / 8 == 49) && chunkY / 8 == 48)
                    aBoolean1067 = true;
                if (chunkX / 8 == 48 && chunkY / 8 == 148)
                    aBoolean1067 = true;
                loadingStage = 1;
                aLong1229 = System.currentTimeMillis();
                method125(null, "Loading - please wait.");
                if (opcode == 222) {
                    int count = 0;
                    for (int fileX = (chunkX - 6) / 8; fileX <= (chunkX + 6) / 8; fileX++) {
                        for (int fileY = (chunkY - 6) / 8; fileY <= (chunkY + 6) / 8; fileY++)
                            count++;

                    }

                    aByteArrayArray838 = new byte[count][];
                    aByteArrayArray1232 = new byte[count][];
                    coordinates = new int[count];
                    anIntArray857 = new int[count];
                    anIntArray858 = new int[count];
                    count = 0;
                    for (int fileX = (chunkX - 6) / 8; fileX <= (chunkX + 6) / 8; fileX++) {
                        for (int fileY = (chunkY - 6) / 8; fileY <= (chunkY + 6) / 8; fileY++) {
                            coordinates[count] = (fileX << 8) + fileY;
                            if (aBoolean1067
                                    && (fileY == 49 || fileY == 149 || fileY == 147 || fileX == 50 || fileX == 49 && fileY == 47)) {
                                anIntArray857[count] = -1;
                                anIntArray858[count] = -1;
                                count++;
                            } else {
                                int l30 = anIntArray857[count] = fileFetcher.method344(0, fileX, fileY, 0);
                                if (l30 != -1)
                                    fileFetcher.request(3, l30);
                                int i32 = anIntArray858[count] = fileFetcher.method344(0, fileX, fileY, 1);
                                if (i32 != -1)
                                    fileFetcher.request(3, i32);
                                count++;
                            }
                        }

                    }

                }
                if (opcode == 53) {
                    int uniqueCount = 0;
                    int fileIndices[] = new int[676];
                    for (int tileZ = 0; tileZ < 4; tileZ++) {
                        for (int tileX = 0; tileX < 13; tileX++) {
                            for (int tileY = 0; tileY < 13; tileY++) {
                                int data = constructedMapPalette[tileZ][tileX][tileY];
                                if (data != -1) {
                                    int chunkX = data >> 14 & 0x3ff;
                                    int chunkY = data >> 3 & 0x7ff;
                                    int fileIndex = (chunkX / 8 << 8) + chunkY / 8;
                                    for (int pos = 0; pos < uniqueCount; pos++) {
                                        if (fileIndices[pos] != fileIndex)
                                            continue;
                                        fileIndex = -1;
                                        break;
                                    }

                                    if (fileIndex != -1)
                                        fileIndices[uniqueCount++] = fileIndex;
                                }
                            }

                        }

                    }

                    aByteArrayArray838 = new byte[uniqueCount][];
                    aByteArrayArray1232 = new byte[uniqueCount][];
                    coordinates = new int[uniqueCount];
                    anIntArray857 = new int[uniqueCount];
                    anIntArray858 = new int[uniqueCount];
                    for (int pos = 0; pos < uniqueCount; pos++) {
                        int j31 = coordinates[pos] = fileIndices[pos];
                        int fileX = j31 >> 8 & 0xff;
                        int fileY = j31 & 0xff;
                        int i34 = anIntArray857[pos] = fileFetcher.method344(0, fileX, fileY, 0);
                        if (i34 != -1)
                            fileFetcher.request(3, i34);
                        int k34 = anIntArray858[pos] = fileFetcher.method344(0, fileX, fileY, 1);
                        if (k34 != -1)
                            fileFetcher.request(3, k34);
                    }

                }
                int deltaX = nextTopLeftTileX - topLeftTileX;
                int deltaY = nextTopLeftTileY - topLeftTileY;
                topLeftTileX = nextTopLeftTileX;
                topLeftTileY = nextTopLeftTileY;
                for (int id = 0; id < 16384; id++) {
                    Npc npc = npcs[id];
                    if (npc != null) {
                        for (int pos = 0; pos < 10; pos++) {
                            ((Actor) (npc)).walkingQueueX[pos] -= deltaX;
                            ((Actor) (npc)).walkingQueueY[pos] -= deltaY;
                        }

                        npc.unitX -= deltaX * 128;
                        npc.unitY -= deltaY * 128;
                    }
                }

                for (int id = 0; id < anInt968; id++) {
                    Player player = players[id];
                    if (player != null) {
                        for (int pos = 0; pos < 10; pos++) {
                            ((Actor) (player)).walkingQueueX[pos] -= deltaX;
                            ((Actor) (player)).walkingQueueY[pos] -= deltaY;
                        }

                        player.unitX -= deltaX * 128;
                        player.unitY -= deltaY * 128;
                    }
                }

                mapLoading = true;
                byte byte1 = 0;
                byte byte2 = 104;
                byte byte3 = 1;
                if (deltaX < 0) {
                    byte1 = 103;
                    byte2 = -1;
                    byte3 = -1;
                }
                byte byte4 = 0;
                byte byte5 = 104;
                byte byte6 = 1;
                if (deltaY < 0) {
                    byte4 = 103;
                    byte5 = -1;
                    byte6 = -1;
                }
                for (int i35 = byte1; i35 != byte2; i35 += byte3) {
                    for (int j35 = byte4; j35 != byte5; j35 += byte6) {
                        int k35 = i35 + deltaX;
                        int l35 = j35 + deltaY;
                        for (int i36 = 0; i36 < 4; i36++)
                            if (k35 >= 0 && l35 >= 0 && k35 < 104 && l35 < 104)
                                groundItems[i36][i35][j35] = groundItems[i36][k35][l35];
                            else
                                groundItems[i36][i35][j35] = null;

                    }

                }

                for (GameObjectSpawnRequest class50_sub2_1 = (GameObjectSpawnRequest) gameObjectSpawnsRequestList.first(); class50_sub2_1 != null; class50_sub2_1 = (GameObjectSpawnRequest) gameObjectSpawnsRequestList
                        .next()) {
                    class50_sub2_1.anInt1393 -= deltaX;
                    class50_sub2_1.anInt1394 -= deltaY;
                    if (class50_sub2_1.anInt1393 < 0 || class50_sub2_1.anInt1394 < 0 || class50_sub2_1.anInt1393 >= 104
                            || class50_sub2_1.anInt1394 >= 104)
                        class50_sub2_1.unlink();
                }

                if (anInt1120 != 0) {
                    anInt1120 -= deltaX;
                    anInt1121 -= deltaY;
                }
                aBoolean1211 = false;
                opcode = -1;
                return true;
            }
            if (opcode == 190) {
                anInt1057 = buffer.getLEShort() * 30;
                opcode = -1;
                return true;
            }
            if (opcode == 41 || opcode == 121 || opcode == 203 || opcode == 106 || opcode == 59 || opcode == 181
                    || opcode == 208 || opcode == 107 || opcode == 142 || opcode == 88 || opcode == 152) {
                parsePlacementPacket(buffer, opcode); // these are to do with objects iirc
                opcode = -1;
                return true;
            }
            if (opcode == 125) {
                if (tabId == 12)
                    aBoolean1181 = true;
                anInt1324 = buffer.getByte();
                opcode = -1;
                return true;
            }
            if (opcode == 21) { // show a model on an interface??
                int scale = buffer.getShort();
                int itemId = buffer.getLEShort();
                int interfaceId = buffer.getLEShortA();
                if (itemId == 65535) {
                    JagInterface.forId(interfaceId).anInt283 = 0;
                    opcode = -1;
                    return true;
                } else {
                    ItemDefinition class16 = ItemDefinition.forId(itemId);
                    JagInterface.forId(interfaceId).anInt283 = 4;
                    JagInterface.forId(interfaceId).anInt284 = itemId;
                    JagInterface.forId(interfaceId).anInt252 = class16.modelRotationX;
                    JagInterface.forId(interfaceId).anInt253 = class16.modelRotationY;
                    JagInterface.forId(interfaceId).anInt251 = (class16.modelScale * 100) / scale;
                    opcode = -1;
                    return true;
                }
            }
            if (opcode == 3) {
                aBoolean1211 = true;
                anInt874 = buffer.getByte();
                anInt875 = buffer.getByte();
                anInt876 = buffer.getShort();
                anInt877 = buffer.getByte();
                anInt878 = buffer.getByte();
                if (anInt878 >= 100) {
                    anInt1216 = anInt874 * 128 + 64;
                    anInt1218 = anInt875 * 128 + 64;
                    anInt1217 = getFloorDrawHeight(anInt1218, anInt1216, plane) - anInt876;
                }
                opcode = -1;
                return true;
            }
            if (opcode == 2) {
                int interfaceId = buffer.getLEShortA();
                int i15 = buffer.method553();
                JagInterface class13_3 = JagInterface.forId(interfaceId);
                if (class13_3.anInt286 != i15 || i15 == -1) {
                    class13_3.anInt286 = i15;
                    class13_3.anInt235 = 0;
                    class13_3.anInt227 = 0;
                }
                opcode = -1;
                return true;
            }
            if (opcode == 71) {
                updateNpcs(buffer, aBoolean1038, size);
                opcode = -1;
                return true;
            }
            if (opcode == 226) { // ignore list
                ignoresCount = size / 8;
                for (int k8 = 0; k8 < ignoresCount; k8++)
                    ignores[k8] = buffer.getLong();

                opcode = -1;
                return true;
            }
            if (opcode == 10) {
                int l8 = buffer.getByteSubtracted();
                int j15 = buffer.getShortAdded();
                if (j15 == 65535)
                    j15 = -1;
                if (anIntArray1081[l8] != j15) {
                    method44(anIntArray1081[l8]);
                    anIntArray1081[l8] = j15;
                }
                aBoolean1181 = true;
                aBoolean950 = true;
                opcode = -1;
                return true;
            }
            if (opcode == 219) { // reset all items on interface?
                int interfaceId = buffer.getLEShort();
                JagInterface class13_2 = JagInterface.forId(interfaceId);
                for (int k21 = 0; k21 < class13_2.itemIds.length; k21++) {
                    class13_2.itemIds[k21] = -1;
                    class13_2.itemIds[k21] = 0;
                }

                opcode = -1;
                return true;
            }
            if (opcode == 238) {
                anInt1213 = buffer.getByte();
                if (anInt1213 == tabId) {
                    if (anInt1213 == 3)
                        tabId = 1;
                    else
                        tabId = 3;
                    aBoolean1181 = true;
                }
                opcode = -1;
                return true;
            }
            if (opcode == 148) {
                aBoolean1211 = false;
                for (int j9 = 0; j9 < 5; j9++)
                    customCameraActive[j9] = false;

                opcode = -1;
                return true;
            }
            if (opcode == 126) {
                playerMembers = buffer.getByte();
                thisPlayerServerId = buffer.getLEShort();
                opcode = -1;
                return true;
            }
            if (opcode == 75) {
                placementX = buffer.getByteNegated();
                placementY = buffer.getByteAdded();
                opcode = -1;
                return true;
            }
            if (opcode == 253) { // open fullscreen interface
                int k9 = buffer.getLEShort();
                int k15 = buffer.getShortAdded();
                method112((byte) 36, k15);
                if (k9 != -1)
                    method112((byte) 36, k9);
                if (anInt1169 != -1) {
                    method44(anInt1169);
                    anInt1169 = -1;
                }
                if (anInt1089 != -1) {
                    method44(anInt1089);
                    anInt1089 = -1;
                }
                if (anInt988 != -1) {
                    method44(anInt988);
                    anInt988 = -1;
                }
                if (anInt1053 != k15) {
                    method44(anInt1053);
                    anInt1053 = k15;
                }
                if (openInterfaceID != k15) {
                    method44(openInterfaceID);
                    openInterfaceID = k9;
                }
                chatboxInterfaceType = 0;
                aBoolean1239 = false;
                opcode = -1;
                return true;
            }
            if (opcode == 251) {
                anInt860 = buffer.getByte();
                aBoolean1181 = true;
                opcode = -1;
                return true;
            }
            if (opcode == 18) {
                int l9 = buffer.getShort();
                int interfaceId = buffer.getShortAdded();
                int l21 = buffer.getLEShort();
                JagInterface.forId(interfaceId).anInt218 = (l9 << 16) + l21;
                opcode = -1;
                return true;
            }
            if (opcode == 90) { // player update
                updatePlayers(size, 69, buffer);
                mapLoading = false;
                opcode = -1;
                return true;
            }
            if (opcode == 113) {
                for (int varpId = 0; varpId < localVarps.length; varpId++)
                    if (localVarps[varpId] != defaultLocalVarps[varpId]) {
                        localVarps[varpId] = defaultLocalVarps[varpId];
                        handleVarp(varpId);
                        aBoolean1181 = true;
                    }

                opcode = -1;
                return true;
            }
            if (opcode == 232) { // update interface string?
                int j10 = buffer.getLEShortA();
                String s6 = buffer.getString();
                JagInterface.forId(j10).aString230 = s6;
                if (JagInterface.forId(j10).anInt248 == anIntArray1081[tabId])
                    aBoolean1181 = true;
                opcode = -1;
                return true;
            }
            if (opcode == 200) {
                int interfaceId = buffer.getShort();
                int i16 = buffer.getLEShortA();
                JagInterface class13_4 = JagInterface.forId(interfaceId);
                if (class13_4 != null && class13_4.type == 0) {
                    if (i16 < 0)
                        i16 = 0;
                    if (i16 > class13_4.anInt285 - class13_4.height)
                        i16 = class13_4.anInt285 - class13_4.height;
                    class13_4.anInt231 = i16;
                }
                opcode = -1;
                return true;
            }
            signlink.reporterror("INSTREAM ERROR - OPCODE " + opcode + ", SIZE " + size + " - last opcode & size " + lastOpcode + "," + anInt905);
            method124(true);
        } catch (IOException _ex) {
            method59(1);
        } catch (Exception exception) {
            String s1 = "T2 - " + opcode + "," + lastOpcode + "," + anInt905 + " - " + size + ","
                    + (nextTopLeftTileX + ((Actor) (thisPlayer)).walkingQueueX[0]) + ","
                    + (nextTopLeftTileY + ((Actor) (thisPlayer)).walkingQueueY[0]) + " - ";
            for (int j16 = 0; j16 < size && j16 < 50; j16++)
                s1 = s1 + buffer.buffer[j16] + ",";

            signlink.reporterror(s1);
            method124(true);

            exception.printStackTrace();
        }
        return true;
    }

    public void method34(byte byte0) {
        if (anInt1183 < 2 && anInt1146 == 0 && anInt1171 == 0)
            return;
        if (byte0 != -79)
            return;
        String s;
        if (anInt1146 == 1 && anInt1183 < 2)
            s = "Use " + aString1150 + " with...";
        else if (anInt1171 == 1 && anInt1183 < 2)
            s = aString1174 + "...";
        else
            s = rightClickOptions[anInt1183 - 1];
        if (anInt1183 > 2)
            s = s + "@whi@ / " + (anInt1183 - 2) + " more options";
        loginScreenFont.method479(true, pulseCycle / 1000, 4, 0xffffff, 15, s, 0);
    }

    public boolean walk(boolean flag, boolean flag1, int dstY, int srcY, int k, int l, int packetType, int j1, int dstX, int l1,
                        int i2, int srcX) {
        byte byte0 = 104;
        byte byte1 = 104;
        for (int x = 0; x < byte0; x++) {
            for (int y = 0; y < byte1; y++) {
                anIntArrayArray885[x][y] = 0;
                cost[x][y] = 0x5f5e0ff;
            }

        }

        int curX = srcX;
        int curY = srcY;
        anIntArrayArray885[srcX][srcY] = 99;
        cost[srcX][srcY] = 0;
        int k3 = 0;
        int l3 = 0;
        walkingPathX[k3] = srcX;
        walkingPathY[k3++] = srcY;
        boolean flag2 = false;
        int i4 = walkingPathX.length;
        int masks[][] = clippingPlanes[plane].masks;
        while (l3 != k3) {
            curX = walkingPathX[l3];
            curY = walkingPathY[l3];
            l3 = (l3 + 1) % i4;
            if (curX == dstX && curY == dstY) {
                flag2 = true;
                break;
            }
            if (j1 != 0) {
                if ((j1 < 5 || j1 == 10) && clippingPlanes[plane].method420(dstX, 0, dstY, j1 - 1, curX, curY, i2)) {
                    flag2 = true;
                    break;
                }
                if (j1 < 10 && clippingPlanes[plane].method421(-37, curY, dstX, curX, i2, j1 - 1, dstY)) {
                    flag2 = true;
                    break;
                }
            }
            if (k != 0 && l != 0 && clippingPlanes[plane].method422(k, curX, true, dstX, l1, l, dstY, curY)) {
                flag2 = true;
                break;
            }
            int nextCost = cost[curX][curY] + 1;
            if (curX > 0 && anIntArrayArray885[curX - 1][curY] == 0 && (masks[curX - 1][curY] & 0x1280108) == 0) {
                walkingPathX[k3] = curX - 1;
                walkingPathY[k3] = curY;
                k3 = (k3 + 1) % i4;
                anIntArrayArray885[curX - 1][curY] = 2;
                cost[curX - 1][curY] = nextCost;
            }
            if (curX < byte0 - 1 && anIntArrayArray885[curX + 1][curY] == 0 && (masks[curX + 1][curY] & 0x1280180) == 0) {
                walkingPathX[k3] = curX + 1;
                walkingPathY[k3] = curY;
                k3 = (k3 + 1) % i4;
                anIntArrayArray885[curX + 1][curY] = 8;
                cost[curX + 1][curY] = nextCost;
            }
            if (curY > 0 && anIntArrayArray885[curX][curY - 1] == 0 && (masks[curX][curY - 1] & 0x1280102) == 0) {
                walkingPathX[k3] = curX;
                walkingPathY[k3] = curY - 1;
                k3 = (k3 + 1) % i4;
                anIntArrayArray885[curX][curY - 1] = 1;
                cost[curX][curY - 1] = nextCost;
            }
            if (curY < byte1 - 1 && anIntArrayArray885[curX][curY + 1] == 0 && (masks[curX][curY + 1] & 0x1280120) == 0) {
                walkingPathX[k3] = curX;
                walkingPathY[k3] = curY + 1;
                k3 = (k3 + 1) % i4;
                anIntArrayArray885[curX][curY + 1] = 4;
                cost[curX][curY + 1] = nextCost;
            }
            if (curX > 0 && curY > 0 && anIntArrayArray885[curX - 1][curY - 1] == 0 && (masks[curX - 1][curY - 1] & 0x128010e) == 0
                    && (masks[curX - 1][curY] & 0x1280108) == 0 && (masks[curX][curY - 1] & 0x1280102) == 0) {
                walkingPathX[k3] = curX - 1;
                walkingPathY[k3] = curY - 1;
                k3 = (k3 + 1) % i4;
                anIntArrayArray885[curX - 1][curY - 1] = 3;
                cost[curX - 1][curY - 1] = nextCost;
            }
            if (curX < byte0 - 1 && curY > 0 && anIntArrayArray885[curX + 1][curY - 1] == 0
                    && (masks[curX + 1][curY - 1] & 0x1280183) == 0 && (masks[curX + 1][curY] & 0x1280180) == 0
                    && (masks[curX][curY - 1] & 0x1280102) == 0) {
                walkingPathX[k3] = curX + 1;
                walkingPathY[k3] = curY - 1;
                k3 = (k3 + 1) % i4;
                anIntArrayArray885[curX + 1][curY - 1] = 9;
                cost[curX + 1][curY - 1] = nextCost;
            }
            if (curX > 0 && curY < byte1 - 1 && anIntArrayArray885[curX - 1][curY + 1] == 0
                    && (masks[curX - 1][curY + 1] & 0x1280138) == 0 && (masks[curX - 1][curY] & 0x1280108) == 0
                    && (masks[curX][curY + 1] & 0x1280120) == 0) {
                walkingPathX[k3] = curX - 1;
                walkingPathY[k3] = curY + 1;
                k3 = (k3 + 1) % i4;
                anIntArrayArray885[curX - 1][curY + 1] = 6;
                cost[curX - 1][curY + 1] = nextCost;
            }
            if (curX < byte0 - 1 && curY < byte1 - 1 && anIntArrayArray885[curX + 1][curY + 1] == 0
                    && (masks[curX + 1][curY + 1] & 0x12801e0) == 0 && (masks[curX + 1][curY] & 0x1280180) == 0
                    && (masks[curX][curY + 1] & 0x1280120) == 0) {
                walkingPathX[k3] = curX + 1;
                walkingPathY[k3] = curY + 1;
                k3 = (k3 + 1) % i4;
                anIntArrayArray885[curX + 1][curY + 1] = 12;
                cost[curX + 1][curY + 1] = nextCost;
            }
        }
        anInt1126 = 0;
        if (!flag2)
            if (flag) {
                int l4 = 1000;
                int j5 = 100;
                byte byte2 = 10;
                for (int i6 = dstX - byte2; i6 <= dstX + byte2; i6++) {
                    for (int k6 = dstY - byte2; k6 <= dstY + byte2; k6++)
                        if (i6 >= 0 && k6 >= 0 && i6 < 104 && k6 < 104 && cost[i6][k6] < 100) {
                            int i7 = 0;
                            if (i6 < dstX)
                                i7 = dstX - i6;
                            else if (i6 > (dstX + k) - 1)
                                i7 = i6 - ((dstX + k) - 1);
                            int j7 = 0;
                            if (k6 < dstY)
                                j7 = dstY - k6;
                            else if (k6 > (dstY + l) - 1)
                                j7 = k6 - ((dstY + l) - 1);
                            int k7 = i7 * i7 + j7 * j7;
                            if (k7 < l4 || k7 == l4 && cost[i6][k6] < j5) {
                                l4 = k7;
                                j5 = cost[i6][k6];
                                curX = i6;
                                curY = k6;
                            }
                        }

                }

                if (l4 == 1000)
                    return false;
                if (curX == srcX && curY == srcY)
                    return false;
                anInt1126 = 1;
            } else {
                return false;
            }
        l3 = 0;
        if (flag1)
            load();
        walkingPathX[l3] = curX;
        walkingPathY[l3++] = curY;
        int k5;
        for (int i5 = k5 = anIntArrayArray885[curX][curY]; curX != srcX || curY != srcY; i5 = anIntArrayArray885[curX][curY]) {
            if (i5 != k5) {
                k5 = i5;
                walkingPathX[l3] = curX;
                walkingPathY[l3++] = curY;
            }
            if ((i5 & 2) != 0)
                curX++;
            else if ((i5 & 8) != 0)
                curX--;
            if ((i5 & 1) != 0)
                curY++;
            else if ((i5 & 4) != 0)
                curY--;
        }

        if (l3 > 0) {
            int walkingPathSize = l3;
            if (walkingPathSize > 25)
                walkingPathSize = 25;
            l3--;
            int clickedX = walkingPathX[l3];
            int clickedY = walkingPathY[l3];
            if (packetType == 0) {
                outBuffer.putOpcode(28);
                outBuffer.putByte(walkingPathSize + walkingPathSize + 3);
            }
            if (packetType == 1) {
                outBuffer.putOpcode(213);
                outBuffer.putByte(walkingPathSize + walkingPathSize + 3 + 14);
            }
            if (packetType == 2) {
                outBuffer.putOpcode(247);
                outBuffer.putByte(walkingPathSize + walkingPathSize + 3);
            }
            outBuffer.putLEShortAdded(clickedX + nextTopLeftTileX);
            outBuffer.putByte(super.keyStatus[5] != 1 ? 0 : 1);
            outBuffer.putLEShortAdded(clickedY + nextTopLeftTileY);
            anInt1120 = walkingPathX[0];
            anInt1121 = walkingPathY[0];
            for (int index = 1; index < walkingPathSize; index++) {
                l3--;
                outBuffer.putByte(walkingPathX[l3] - clickedX);
                outBuffer.putByte(walkingPathY[l3] - clickedY);
            }
            return true;
        }
        return packetType != 1;
    }

    public void method36(int i) {
        if (i != 16220)
            anInt1328 = 458;
        if (loadingStage == 2) {
            for (GameObjectSpawnRequest class50_sub2 = (GameObjectSpawnRequest) gameObjectSpawnsRequestList.first(); class50_sub2 != null; class50_sub2 = (GameObjectSpawnRequest) gameObjectSpawnsRequestList
                    .next()) {
                if (class50_sub2.delayUntilRespawn > 0)
                    class50_sub2.delayUntilRespawn--;
                if (class50_sub2.delayUntilRespawn == 0) {
                    if (class50_sub2.anInt1387 < 0
                            || Region.method170(class50_sub2.anInt1389, aByte1143, class50_sub2.anInt1387)) {
                        method45(class50_sub2.anInt1388, class50_sub2.anInt1393, class50_sub2.anInt1387,
                                class50_sub2.anInt1394, class50_sub2.anInt1391, class50_sub2.anInt1389, (byte) 1,
                                class50_sub2.anInt1392);
                        class50_sub2.unlink();
                    }
                } else {
                    if (class50_sub2.anInt1395 > 0)
                        class50_sub2.anInt1395--;
                    if (class50_sub2.anInt1395 == 0
                            && class50_sub2.anInt1393 >= 1
                            && class50_sub2.anInt1394 >= 1
                            && class50_sub2.anInt1393 <= 102
                            && class50_sub2.anInt1394 <= 102
                            && (class50_sub2.anInt1384 < 0 || Region.method170(class50_sub2.anInt1386, aByte1143,
                            class50_sub2.anInt1384))) {
                        method45(class50_sub2.anInt1385, class50_sub2.anInt1393, class50_sub2.anInt1384,
                                class50_sub2.anInt1394, class50_sub2.anInt1391, class50_sub2.anInt1386, (byte) 1,
                                class50_sub2.anInt1392);
                        class50_sub2.anInt1395 = -1;
                        if (class50_sub2.anInt1384 == class50_sub2.anInt1387 && class50_sub2.anInt1387 == -1)
                            class50_sub2.unlink();
                        else if (class50_sub2.anInt1384 == class50_sub2.anInt1387
                                && class50_sub2.anInt1385 == class50_sub2.anInt1388
                                && class50_sub2.anInt1386 == class50_sub2.anInt1389)
                            class50_sub2.unlink();
                    }
                }
            }

        }
    }

    public String method37(int i) {
        if (i != -42588) {
            opcode = buffer.getByte();
        }
        if (signlink.mainapp != null) {
            return signlink.mainapp.getDocumentBase().getHost().toLowerCase();
        }
        if (super.frame != null) {
            return "runescape.com";
        } else {
            return super.getDocumentBase().getHost().toLowerCase();
        }
    }

    public void method38(int i, int j, int k, Player player, int l) {
        if (player == thisPlayer)
            return;
        if (anInt1183 >= 400)
            return;
        if (l != 0)
            aBoolean963 = !aBoolean963;
        String s;
        if (player.anInt1759 == 0) {
            s = player.username
                    + method92(player.anInt1753, thisPlayer.anInt1753, 736) + " (level-"
                    + player.anInt1753 + ")";
        } else {
            s = player.username + " (skill-" + player.anInt1759 + ")";
        }
        if (anInt1146 == 1) {
            rightClickOptions[anInt1183] = "Use " + aString1150 + " with @whi@" + s;
            anIntArray981[anInt1183] = 596;
            anIntArray982[anInt1183] = i;
            anIntArray979[anInt1183] = k;
            anIntArray980[anInt1183] = j;
            anInt1183++;
        } else if (anInt1171 == 1) {
            if ((anInt1173 & 8) == 8) {
                rightClickOptions[anInt1183] = aString1174 + " @whi@" + s;
                anIntArray981[anInt1183] = 918;
                anIntArray982[anInt1183] = i;
                anIntArray979[anInt1183] = k;
                anIntArray980[anInt1183] = j;
                anInt1183++;
            }
        } else {
            for (int i1 = 4; i1 >= 0; i1--)
                if (aStringArray1069[i1] != null) {
                    rightClickOptions[anInt1183] = aStringArray1069[i1] + " @whi@" + s;
                    char c = '\0';
                    if (aStringArray1069[i1].equalsIgnoreCase("attack")) {
                        if (player.anInt1753 > thisPlayer.anInt1753)
                            c = '\u07D0';
                        if (thisPlayer.team != 0 && player.team != 0)
                            if (thisPlayer.team == player.team)
                                c = '\u07D0';
                            else
                                c = '\0';
                    } else if (aBooleanArray1070[i1])
                        c = '\u07D0';
                    if (i1 == 0) {
                        anIntArray981[anInt1183] = 200 + c;
                    }
                    if (i1 == 1)
                        anIntArray981[anInt1183] = 493 + c;
                    if (i1 == 2)
                        anIntArray981[anInt1183] = 408 + c;
                    if (i1 == 3)
                        anIntArray981[anInt1183] = 677 + c;
                    if (i1 == 4)
                        anIntArray981[anInt1183] = 876 + c;
                    anIntArray982[anInt1183] = i;
                    anIntArray979[anInt1183] = k;
                    anIntArray980[anInt1183] = j;
                    anInt1183++;
                }

        }
        for (int j1 = 0; j1 < anInt1183; j1++)
            if (anIntArray981[j1] == 14) {
                rightClickOptions[j1] = "Walk here @whi@" + s;
                return;
            }

    }

    public void method39(boolean flag) {
        if (!flag)
            groundItems = null;
        int chatClickY = super.anInt30 - layout.chatboxDy; // compared with the classic button positions
        if (super.anInt28 == 1) {
            if (super.anInt29 >= 6 && super.anInt29 <= 106 && chatClickY >= 467 && chatClickY <= 499) {
                publicChatMode = (publicChatMode + 1) % 4;
                aBoolean1212 = true;
                aBoolean1240 = true;
                outBuffer.putOpcode(176);
                outBuffer.putByte(publicChatMode);
                outBuffer.putByte(privateChatMode);
                outBuffer.putByte(tradeMode);
            }
            if (super.anInt29 >= 135 && super.anInt29 <= 235 && chatClickY >= 467 && chatClickY <= 499) {
                privateChatMode = (privateChatMode + 1) % 3;
                aBoolean1212 = true;
                aBoolean1240 = true;
                outBuffer.putOpcode(176);
                outBuffer.putByte(publicChatMode);
                outBuffer.putByte(privateChatMode);
                outBuffer.putByte(tradeMode);
            }
            if (super.anInt29 >= 273 && super.anInt29 <= 373 && chatClickY >= 467 && chatClickY <= 499) {
                tradeMode = (tradeMode + 1) % 3;
                aBoolean1212 = true;
                aBoolean1240 = true;
                outBuffer.putOpcode(176);
                outBuffer.putByte(publicChatMode);
                outBuffer.putByte(privateChatMode);
                outBuffer.putByte(tradeMode);
            }
            if (super.anInt29 >= 412 && super.anInt29 <= 512 && chatClickY >= 467 && chatClickY <= 499)
                if (anInt1169 == -1) {
                    method15(false);
                    aString839 = "";
                    aBoolean1098 = false;
                    anInt1231 = anInt1169 = JagInterface.anInt246;
                } else {
                    pushMessage("", (byte) -123, "Please close the interface you have open before using 'report abuse'", 0);
                }
            anInt1160++;
            if (anInt1160 > 161) {
                anInt1160 = 0;
                outBuffer.putOpcode(22);
                outBuffer.putShort(38304);
            }
        }
    }

    public void parsePlayerBlocks(JagBuffer vec, int packetSize) {
        for (int k = 0; k < updatedPlayerCount; k++) {
            int id = updatedPlayers[k];
            Player plr = players[id];
            int mask = vec.getByte();
            if ((mask & 0x20) != 0)
                mask += vec.getByte() << 8;
            parsePlayerBlock(id, plr, mask, vec);
        }
    }

    public void updateThisPlayerMovement(int i, boolean flag, JagBuffer buffer) {
        buffer.initBitAccess();
        int moved = buffer.getBits(1);
        if (moved == 0)
            return;
        int moveType = buffer.getBits(2);
        isLoggedIn &= flag;

        if (moveType == 0) {
            updatedPlayers[updatedPlayerCount++] = thisPlayerId;
            return;
        }
        if (moveType == 1) {
            int direction = buffer.getBits(3);
            thisPlayer.addStep(direction, false);
            int blockUpdateRequired = buffer.getBits(1);
            if (blockUpdateRequired == 1)
                updatedPlayers[updatedPlayerCount++] = thisPlayerId;
            return;
        }
        if (moveType == 2) {
            int direction1 = buffer.getBits(3);
            thisPlayer.addStep(direction1, true);
            int direction2 = buffer.getBits(3);
            thisPlayer.addStep(direction2, true);
            int blockUpdateRequired = buffer.getBits(1);
            if (blockUpdateRequired == 1)
                updatedPlayers[updatedPlayerCount++] = thisPlayerId;
            return;
        }
        if (moveType == 3) {
            int discardWalkingQueue = buffer.getBits(1);
            plane = buffer.getBits(2);
            int localY = buffer.getBits(7);
            int localX = buffer.getBits(7);
            int blockUpdateRequired = buffer.getBits(1);
            if (blockUpdateRequired == 1)
                updatedPlayers[updatedPlayerCount++] = thisPlayerId;
            thisPlayer.teleport(localX, localY, discardWalkingQueue == 1);
        }
    }

    public void method42(int i, int j, JagInterface class13, byte byte0, int k, int l, int i1, int j1, int k1) {
        if (aBoolean1127)
            anInt1303 = 32;
        else
            anInt1303 = 0;
        aBoolean1127 = false;
        if (byte0 != 102) {
            for (int l1 = 1; l1 > 0; l1++) ;
        }
        if (i1 >= k1 && i1 < k1 + 16 && k >= j && k < j + 16) {
            class13.anInt231 -= anInt1094 * 4;
            if (l == 1)
                aBoolean1181 = true;
            if (l == 2 || l == 3)
                aBoolean1240 = true;
            return;
        }
        if (i1 >= k1 && i1 < k1 + 16 && k >= (j + j1) - 16 && k < j + j1) {
            class13.anInt231 += anInt1094 * 4;
            if (l == 1)
                aBoolean1181 = true;
            if (l == 2 || l == 3)
                aBoolean1240 = true;
            return;
        }
        if (i1 >= k1 - anInt1303 && i1 < k1 + 16 + anInt1303 && k >= j + 16 && k < (j + j1) - 16 && anInt1094 > 0) {
            int i2 = ((j1 - 32) * j1) / i;
            if (i2 < 8)
                i2 = 8;
            int j2 = k - j - 16 - i2 / 2;
            int k2 = j1 - 32 - i2;
            class13.anInt231 = ((i - j1) * j2) / k2;
            if (l == 1)
                aBoolean1181 = true;
            if (l == 2 || l == 3)
                aBoolean1240 = true;
            aBoolean1127 = true;
        }
    }

    public void generateContextOptions43(byte byte0) {
        if (anInt1146 == 0 && anInt1171 == 0) {
            rightClickOptions[anInt1183] = "Walk here";
            anIntArray981[anInt1183] = 14;
            anIntArray979[anInt1183] = super.mouseX;
            anIntArray980[anInt1183] = super.mouseY;
            anInt1183++;
        }
        int i = -1;
        if (byte0 != 7)
            opcode = -1;
        for (int j = 0; j < Model.hoveredCount; j++) {
            int k = Model.hoveredModels[j];
            int x = k & 0x7f;
            int y = k >> 7 & 0x7f;
            int j1 = k >> 29 & 3;
            int k1 = k >> 14 & 0x7fff;
            if (k == i)
                continue;
            i = k;
            if (j1 == 2 && sceneGraph.method271(plane, x, y, k) >= 0) {
                ObjectDefinition def = ObjectDefinition.forId(k1);
                if (def.anIntArray805 != null)
                    def = def.method424(0);
                if (def == null)
                    continue;
                if (anInt1146 == 1) {
                    rightClickOptions[anInt1183] = "Use " + aString1150 + " with @cya@" + def.name;
                    anIntArray981[anInt1183] = 467;
                    anIntArray982[anInt1183] = k;
                    anIntArray979[anInt1183] = x;
                    anIntArray980[anInt1183] = y;
                    anInt1183++;
                } else if (anInt1171 == 1) {
                    if ((anInt1173 & 4) == 4) {
                        rightClickOptions[anInt1183] = aString1174 + " @cya@" + def.name;
                        anIntArray981[anInt1183] = 376;
                        anIntArray982[anInt1183] = k;
                        anIntArray979[anInt1183] = x;
                        anIntArray980[anInt1183] = y;
                        anInt1183++;
                    }
                } else {
                    if (def.options != null) {
                        for (int l1 = 4; l1 >= 0; l1--)
                            if (def.options[l1] != null) {
                                rightClickOptions[anInt1183] = def.options[l1] + " @cya@"
                                        + def.name;
                                if (l1 == 0)
                                    anIntArray981[anInt1183] = 35;
                                if (l1 == 1)
                                    anIntArray981[anInt1183] = 389;
                                if (l1 == 2)
                                    anIntArray981[anInt1183] = 888;
                                if (l1 == 3)
                                    anIntArray981[anInt1183] = 892;
                                if (l1 == 4)
                                    anIntArray981[anInt1183] = 1280;
                                anIntArray982[anInt1183] = k;
                                anIntArray979[anInt1183] = x;
                                anIntArray980[anInt1183] = y;
                                anInt1183++;
                            }

                    }
                    rightClickOptions[anInt1183] = Constants.DEBUG ? "Examine @cya@" + def.name + " id(" + def.id + "), pos(" + (x + nextTopLeftTileX) + ", " + (y + nextTopLeftTileY) + ")" : "Examine @cya@" + def.name;
                    anIntArray981[anInt1183] = 1412;
                    anIntArray982[anInt1183] = def.id << 14;
                    anIntArray979[anInt1183] = x;
                    anIntArray980[anInt1183] = y;
                    anInt1183++;
                }
            }
            if (j1 == 1) {
                Npc class50_sub1_sub4_sub3_sub1 = npcs[k1];
                if (class50_sub1_sub4_sub3_sub1.def.aByte642 == 1
                        && (((Actor) (class50_sub1_sub4_sub3_sub1)).unitX & 0x7f) == 64
                        && (((Actor) (class50_sub1_sub4_sub3_sub1)).unitY & 0x7f) == 64) {
                    for (int i2 = 0; i2 < localNpcCount; i2++) {
                        Npc class50_sub1_sub4_sub3_sub1_1 = npcs[anIntArray1134[i2]];
                        if (class50_sub1_sub4_sub3_sub1_1 != null
                                && class50_sub1_sub4_sub3_sub1_1 != class50_sub1_sub4_sub3_sub1
                                && class50_sub1_sub4_sub3_sub1_1.def.aByte642 == 1
                                && ((Actor) (class50_sub1_sub4_sub3_sub1_1)).unitX == ((Actor) (class50_sub1_sub4_sub3_sub1)).unitX
                                && ((Actor) (class50_sub1_sub4_sub3_sub1_1)).unitY == ((Actor) (class50_sub1_sub4_sub3_sub1)).unitY)
                            method82(class50_sub1_sub4_sub3_sub1_1.def, y, x, anIntArray1134[i2], (byte) -76);
                    }

                    for (int k2 = 0; k2 < localPlayerCount; k2++) {
                        Player class50_sub1_sub4_sub3_sub2_1 = players[localPlayers[k2]];
                        if (class50_sub1_sub4_sub3_sub2_1 != null
                                && ((Actor) (class50_sub1_sub4_sub3_sub2_1)).unitX == ((Actor) (class50_sub1_sub4_sub3_sub1)).unitX
                                && ((Actor) (class50_sub1_sub4_sub3_sub2_1)).unitY == ((Actor) (class50_sub1_sub4_sub3_sub1)).unitY)
                            method38(localPlayers[k2], y, x, class50_sub1_sub4_sub3_sub2_1, 0);
                    }

                }
                method82(class50_sub1_sub4_sub3_sub1.def, y, x, k1, (byte) -76);
            }
            if (j1 == 0) {
                Player class50_sub1_sub4_sub3_sub2 = players[k1];
                if ((((Actor) (class50_sub1_sub4_sub3_sub2)).unitX & 0x7f) == 64
                        && (((Actor) (class50_sub1_sub4_sub3_sub2)).unitY & 0x7f) == 64) {
                    for (int j2 = 0; j2 < localNpcCount; j2++) {
                        Npc class50_sub1_sub4_sub3_sub1_2 = npcs[anIntArray1134[j2]];
                        if (class50_sub1_sub4_sub3_sub1_2 != null
                                && class50_sub1_sub4_sub3_sub1_2.def.aByte642 == 1
                                && ((Actor) (class50_sub1_sub4_sub3_sub1_2)).unitX == ((Actor) (class50_sub1_sub4_sub3_sub2)).unitX
                                && ((Actor) (class50_sub1_sub4_sub3_sub1_2)).unitY == ((Actor) (class50_sub1_sub4_sub3_sub2)).unitY)
                            method82(class50_sub1_sub4_sub3_sub1_2.def, y, x, anIntArray1134[j2], (byte) -76);
                    }

                    for (int l2 = 0; l2 < localPlayerCount; l2++) {
                        Player class50_sub1_sub4_sub3_sub2_2 = players[localPlayers[l2]];
                        if (class50_sub1_sub4_sub3_sub2_2 != null
                                && class50_sub1_sub4_sub3_sub2_2 != class50_sub1_sub4_sub3_sub2
                                && ((Actor) (class50_sub1_sub4_sub3_sub2_2)).unitX == ((Actor) (class50_sub1_sub4_sub3_sub2)).unitX
                                && ((Actor) (class50_sub1_sub4_sub3_sub2_2)).unitY == ((Actor) (class50_sub1_sub4_sub3_sub2)).unitY)
                            method38(localPlayers[l2], y, x, class50_sub1_sub4_sub3_sub2_2, 0);
                    }

                }
                method38(k1, y, x, class50_sub1_sub4_sub3_sub2, 0);
            }
            if (j1 == 3) {
                LinkedList class6 = groundItems[plane][x][y];
                if (class6 != null) {
                    for (GroundItem class50_sub1_sub4_sub1 = (GroundItem) class6.last(); class50_sub1_sub4_sub1 != null; class50_sub1_sub4_sub1 = (GroundItem) class6
                            .previous()) {
                        ItemDefinition def = ItemDefinition.forId(class50_sub1_sub4_sub1.id);
                        if (anInt1146 == 1) {
                            rightClickOptions[anInt1183] = "Use " + aString1150 + " with @lre@" + def.name;
                            anIntArray981[anInt1183] = 100;
                            anIntArray982[anInt1183] = class50_sub1_sub4_sub1.id;
                            anIntArray979[anInt1183] = x;
                            anIntArray980[anInt1183] = y;
                            anInt1183++;
                        } else if (anInt1171 == 1) {
                            if ((anInt1173 & 1) == 1) {
                                rightClickOptions[anInt1183] = aString1174 + " @lre@" + def.name;
                                anIntArray981[anInt1183] = 199;
                                anIntArray982[anInt1183] = class50_sub1_sub4_sub1.id;
                                anIntArray979[anInt1183] = x;
                                anIntArray980[anInt1183] = y;
                                anInt1183++;
                            }
                        } else {
                            for (int i3 = 4; i3 >= 0; i3--)
                                if (def.groundActions != null && def.groundActions[i3] != null) {
                                    rightClickOptions[anInt1183] = def.groundActions[i3] + " @lre@" + def.name;
                                    if (i3 == 0)
                                        anIntArray981[anInt1183] = 68;
                                    if (i3 == 1)
                                        anIntArray981[anInt1183] = 26;
                                    if (i3 == 2)
                                        anIntArray981[anInt1183] = 684;
                                    if (i3 == 3)
                                        anIntArray981[anInt1183] = 930;
                                    if (i3 == 4)
                                        anIntArray981[anInt1183] = 270;
                                    anIntArray982[anInt1183] = class50_sub1_sub4_sub1.id;
                                    anIntArray979[anInt1183] = x;
                                    anIntArray980[anInt1183] = y;
                                    anInt1183++;
                                } else if (i3 == 2) {
                                    rightClickOptions[anInt1183] = "Take @lre@" + def.name;
                                    anIntArray981[anInt1183] = 684;
                                    anIntArray982[anInt1183] = class50_sub1_sub4_sub1.id;
                                    anIntArray979[anInt1183] = x;
                                    anIntArray980[anInt1183] = y;
                                    anInt1183++;
                                }

                            rightClickOptions[anInt1183] = Constants.DEBUG ? "Examine @lre@" + def.name + " id(" + def.id + ")" : "Examine @lre@" + def.name;
                            anIntArray981[anInt1183] = 1564;
                            anIntArray982[anInt1183] = class50_sub1_sub4_sub1.id;
                            anIntArray979[anInt1183] = x;
                            anIntArray980[anInt1183] = y;
                            anInt1183++;
                        }
                    }

                }
            }
        }

    }

    public void method44(int i) {
        JagInterface.method200(i);
        return;
    }

    public void method45(int i, int j, int k, int l, int i1, int j1, byte byte0, int k1) {
        if (byte0 != aByte1066)
            anInt1175 = -380;
        if (j >= 1 && l >= 1 && j <= 102 && l <= 102) {
            if (lowMemory && i1 != plane)
                return;
            int l1 = 0;
            if (k1 == 0)
                l1 = sceneGraph.method267(i1, j, l);
            if (k1 == 1)
                l1 = sceneGraph.method268(j, (byte) 4, i1, l);
            if (k1 == 2)
                l1 = sceneGraph.method269(i1, j, l);
            if (k1 == 3)
                l1 = sceneGraph.method270(i1, j, l);
            if (l1 != 0) {
                int l2 = sceneGraph.method271(i1, j, l, l1);
                int i2 = l1 >> 14 & 0x7fff;
                int j2 = l2 & 0x1f;
                int k2 = l2 >> 6;
                if (k1 == 0) {
                    sceneGraph.method258(l, i1, j, true);
                    ObjectDefinition class47 = ObjectDefinition.forId(i2);
                    if (class47.aBoolean810)
                        clippingPlanes[i1].method416(k2, j, 0, l, j2, class47.aBoolean809);
                }
                if (k1 == 1)
                    sceneGraph.method259(false, j, l, i1);
                if (k1 == 2) {
                    sceneGraph.method260(l, i1, -779, j);
                    ObjectDefinition class47_1 = ObjectDefinition.forId(i2);
                    if (j + class47_1.anInt801 > 103 || l + class47_1.anInt801 > 103 || j + class47_1.anInt775 > 103
                            || l + class47_1.anInt775 > 103)
                        return;
                    if (class47_1.aBoolean810)
                        clippingPlanes[i1].method417(anInt1055, l, j, k2, class47_1.anInt775, class47_1.aBoolean809,
                                class47_1.anInt801);
                }
                if (k1 == 3) {
                    sceneGraph.method261(j, l, true, i1);
                    ObjectDefinition class47_2 = ObjectDefinition.forId(i2);
                    if (class47_2.aBoolean810 && class47_2.aBoolean759)
                        clippingPlanes[i1].method419(j, (byte) -122, l);
                }
            }
            if (k >= 0) {
                int i3 = i1;
                if (i3 < 3 && (aByteArrayArrayArray1125[1][j][l] & 2) == 2)
                    i3++;
                Region.method165(k, i3, j1, l, clippingPlanes[i1], i, j, 0, i1, sceneGraph,
                        intGroundArray);
            }
        }
    }

    public void handleNpcMovement(int i, byte byte0, JagBuffer buffer) {
        buffer.initBitAccess();
        int npcCount = buffer.getBits(8);
        if (byte0 != aByte1317)
            anInt1281 = -460;
        if (npcCount < localNpcCount) {
            for (int k = npcCount; k < localNpcCount; k++)
                removePlayers[removePlayerCount++] = anIntArray1134[k];

        }
        if (npcCount > localNpcCount) {
            signlink.reporterror(thisPlayerName + " Too many npcs");
            throw new RuntimeException("eek");
        }
        localNpcCount = 0;
        for (int l = 0; l < npcCount; l++) {
            int i1 = anIntArray1134[l];
            Npc npc = npcs[i1];
            int updateRequired = buffer.getBits(1);
            if (updateRequired == 0) {
                anIntArray1134[localNpcCount++] = i1;
                npc.pulseCycle = pulseCycle;
            } else {
                int moveType = buffer.getBits(2);
                if (moveType == 0) {
                    anIntArray1134[localNpcCount++] = i1;
                    npc.pulseCycle = pulseCycle;
                    updatedPlayers[updatedPlayerCount++] = i1;
                } else if (moveType == 1) {
                    anIntArray1134[localNpcCount++] = i1;
                    npc.pulseCycle = pulseCycle;
                    int direction = buffer.getBits(3);
                    npc.addStep(direction, false);
                    int blockUpdateRequired = buffer.getBits(1);
                    if (blockUpdateRequired == 1)
                        updatedPlayers[updatedPlayerCount++] = i1;
                } else if (moveType == 2) {
                    anIntArray1134[localNpcCount++] = i1;
                    npc.pulseCycle = pulseCycle;
                    int direction1 = buffer.getBits(3);
                    npc.addStep(direction1, true);
                    int direction2 = buffer.getBits(3);
                    npc.addStep(direction2, true);
                    int blockUpdateRequired = buffer.getBits(1);
                    if (blockUpdateRequired == 1)
                        updatedPlayers[updatedPlayerCount++] = i1;
                } else if (moveType == 3)
                    removePlayers[removePlayerCount++] = i1;
            }
        }

    }

    public void pushMessage(String s, byte byte0, String s1, int i) {
        if (i == 0 && anInt1191 != -1) {
            aString1058 = s1;
            super.anInt28 = 0;
        }
        if (anInt988 == -1)
            aBoolean1240 = true;
        for (int j = 99; j > 0; j--) {
            anIntArray1296[j] = anIntArray1296[j - 1];
            aStringArray1297[j] = aStringArray1297[j - 1];
            aStringArray1298[j] = aStringArray1298[j - 1];
        }

        if (byte0 != aByte901)
            anInt1140 = incomingRandom.nextInt();
        anIntArray1296[0] = i;
        aStringArray1297[0] = s;
        aStringArray1298[0] = s1;
    }

    public void updateNpcs(JagBuffer buf, boolean flag, int packetSize) {
        isLoggedIn &= flag;
        removePlayerCount = 0;
        updatedPlayerCount = 0;
        handleNpcMovement(packetSize, (byte) -58, buf);
        addNewNpcs(buf, packetSize, false);
        parseNpcBlocks(buf, packetSize, 838);
        for (int j = 0; j < removePlayerCount; j++) {
            int k = removePlayers[j];
            if (npcs[k].pulseCycle != pulseCycle) {
                npcs[k].def = null;
                npcs[k] = null;
            }
        }

        if (buf.position != packetSize) {
            signlink.reporterror(thisPlayerName + " size mismatch in getnpcpos - pos:" + buf.position
                    + " psize:" + packetSize);
            throw new RuntimeException("eek");
        }
        for (int l = 0; l < localNpcCount; l++)
            if (npcs[anIntArray1134[l]] == null) {
                signlink.reporterror(thisPlayerName + " null entry in npc list - pos:" + l + " size:" + localNpcCount);
                throw new RuntimeException("eek");
            }

    }

    public void method49(int i) {
        ObjectDefinition.lruHashTable.clear();
        ObjectDefinition.aClass33_762.clear();
        if (i <= 0) {
            for (int j = 1; j > 0; j++) ;
        }
        NpcDefinition.aClass33_635.clear();
        ItemDefinition.aClass33_337.clear();
        ItemDefinition.spriteCache.clear();
        Player.aClass33_1761.clear();
        SpotAnimation.models.clear();
    }

    public void method50(boolean flag) {
        /*    signlink.midiplay = false;
        if (flag)
            anInt1119 = 466;
        signlink.midifade = 0;
        signlink.midi = "stop";*/
        stopMidi();
    }

    public void updateProjectiles() {
        Projectile projectile = (Projectile) projectileQueue.first();
        for (; projectile != null; projectile = (Projectile) projectileQueue
                .next())
            if (projectile.plane != plane || pulseCycle > projectile.speed)
                projectile.unlink();
            else if (pulseCycle >= projectile.createdTime) {
                if (projectile.target > 0) {
                    Npc npc = npcs[projectile.target - 1];
                    if (npc != null
                            && npc.unitX >= 0
                            && npc.unitX < 13312
                            && npc.unitY >= 0
                            && npc.unitY < 13312)
                        projectile.trackTarget(npc.unitX,
                                npc.unitY, getFloorDrawHeight(
                                        npc.unitY,
                                        npc.unitX,
                                        projectile.plane)
                                        - projectile.heightEnd, pulseCycle);
                }
                if (projectile.target < 0) {
                    int i = -projectile.target - 1;
                    Player player;
                    if (i == thisPlayerServerId)
                        player = thisPlayer;
                    else
                        player = players[i];
                    if (player != null
                            && ((Actor) (player)).unitX >= 0
                            && ((Actor) (player)).unitX < 13312
                            && ((Actor) (player)).unitY >= 0
                            && ((Actor) (player)).unitY < 13312)
                        projectile.trackTarget(((Actor) (player)).unitX,
                                ((Actor) (player)).unitY, getFloorDrawHeight(
                                        ((Actor) (player)).unitY,
                                        ((Actor) (player)).unitX,
                                        projectile.plane)
                                        - projectile.heightEnd, pulseCycle);
                }
                projectile.method563(anInt951, false);
                sceneGraph.method252(-1, projectile, (int) projectile.aDouble1555,
                        (int) projectile.aDouble1557, false, 0, plane, 60,
                        (int) projectile.aDouble1556, projectile.anInt1562);
            }

        heartbeatCounter++;
        if (heartbeatCounter > 51) {
            heartbeatCounter = 0;
            outBuffer.putOpcode(248);
        }
    }

    public void prepareLoginScreen_52(boolean flag) {
        titlebox_1292 = new IndexedSprite(titleArchive, "titlebox", 0);
        titlebutton_1293 = new IndexedSprite(titleArchive, "titlebutton", 0);
        runes_array1117 = new IndexedSprite[12];
        if (flag) {
            load();
        }
        for (int i = 0; i < 12; i++) {
            runes_array1117[i] = new IndexedSprite(titleArchive, "runes", i);
        }
        sprite_1017 = new RgbSprite(128, 265);
        sprite_1018 = new RgbSprite(128, 265);
        for (int j = 0; j < 33920; j++) {
            sprite_1017.pixels_1489[j] = loginFlameLeft.pixels[j];
        }

        for (int k = 0; k < 33920; k++) {
            sprite_1018.pixels_1489[k] = loginFlameRight.pixels[k];
        }

        anIntArray1311 = new int[256];
        for (int l = 0; l < 64; l++) {
            anIntArray1311[l] = l * 0x40000;
        }

        for (int i1 = 0; i1 < 64; i1++) {
            anIntArray1311[i1 + 64] = 0xff0000 + 1024 * i1;
        }

        for (int j1 = 0; j1 < 64; j1++) {
            anIntArray1311[j1 + 128] = 0xffff00 + 4 * j1;
        }

        for (int k1 = 0; k1 < 64; k1++) {
            anIntArray1311[k1 + 192] = 0xffffff;
        }

        anIntArray1312 = new int[256];
        for (int l1 = 0; l1 < 64; l1++) {
            anIntArray1312[l1] = l1 * 1024;
        }

        for (int i2 = 0; i2 < 64; i2++)
            anIntArray1312[i2 + 64] = 65280 + 4 * i2;

        for (int j2 = 0; j2 < 64; j2++)
            anIntArray1312[j2 + 128] = 65535 + 0x40000 * j2;

        for (int k2 = 0; k2 < 64; k2++)
            anIntArray1312[k2 + 192] = 0xffffff;

        anIntArray1313 = new int[256];
        for (int l2 = 0; l2 < 64; l2++) {
            anIntArray1313[l2] = l2 * 4;
        }

        for (int i3 = 0; i3 < 64; i3++) {
            anIntArray1313[i3 + 64] = 255 + 0x40000 * i3;
        }

        for (int j3 = 0; j3 < 64; j3++) {
            anIntArray1313[j3 + 128] = 0xff00ff + 1024 * j3;
        }

        for (int k3 = 0; k3 < 64; k3++) {
            anIntArray1313[k3 + 192] = 0xffffff;
        }

        anIntArray1310 = new int[256];
        anIntArray1176 = new int[32768];
        anIntArray1177 = new int[32768];
        method83(null, 0);
        anIntArray1084 = new int[32768];
        anIntArray1085 = new int[32768];
        drawLoadingText(10, "Connecting to fileserver");
        if (!isThreadStarted) {
            isGameThreadStarted = true;
            isThreadStarted = true;
            startThread(this, 2);
        }
    }

    public void method53(long l, int i) {
        try {
            if (l == 0L)
                return;
            for (int j = 0; j < friendsCount; j++) {
                if (friends[j] != l)
                    continue;
                friendsCount--;
                aBoolean1181 = true;
                for (int k = j; k < friendsCount; k++) {
                    aStringArray849[k] = aStringArray849[k + 1];
                    anIntArray1267[k] = anIntArray1267[k + 1];
                    friends[k] = friends[k + 1];
                }

                outBuffer.putOpcode(141);
                outBuffer.putLong(l);
                break;
            }

            size += i;
            return;
        } catch (RuntimeException runtimeexception) {
            signlink.reporterror("38799, " + l + ", " + i + ", " + runtimeexception.toString());
        }
        throw new RuntimeException();
    }

    public void updateMouseClicks() {
        if (anInt1113 != 0) {
            return;
        }
        int j = super.anInt28;
        if (anInt1171 == 1 && super.anInt29 >= layout.tabIconsTop.x && super.anInt30 >= layout.tabIconsTop.y
                && super.anInt29 <= layout.tabIconsTop.x + layout.tabIconsTop.width
                && super.anInt30 <= layout.tabIconsTop.y + layout.tabIconsTop.height) {
            j = 0;
        }
        if (isContextMenuActive) {
            if (j != 1) {
                int k = super.mouseX - layout.areaX(anInt1304);
                int j1 = super.mouseY - layout.areaY(anInt1304);
                if (k < anInt1305 - 10 || k > anInt1305 + anInt1307 + 10 || j1 < anInt1306 - 10
                        || j1 > anInt1306 + anInt1308 + 10) {
                    isContextMenuActive = false;
                    if (anInt1304 == 1) {
                        aBoolean1181 = true;
                    }
                    if (anInt1304 == 2) {
                        aBoolean1240 = true;
                    }
                }
            }
            if (j == 1) {
                int l = anInt1305;
                int k1 = anInt1306;
                int i2 = anInt1307;
                int k2 = super.anInt29 - layout.areaX(anInt1304);
                int l2 = super.anInt30 - layout.areaY(anInt1304);
                int i3 = -1;
                for (int j3 = 0; j3 < anInt1183; j3++) {
                    int k3 = k1 + 31 + (anInt1183 - 1 - j3) * 15;
                    if (k2 > l && k2 < l + i2 && l2 > k3 - 13 && l2 < k3 + 3)
                        i3 = j3;
                }

                if (i3 != -1)
                    sendOutgoingPackets(i3, 8);
                isContextMenuActive = false;
                if (anInt1304 == 1)
                    aBoolean1181 = true;
                if (anInt1304 == 2) {
                    aBoolean1240 = true;
                    return;
                }
            }
        } else {
            if (j == 1 && anInt1183 > 0) {
                int i1 = anIntArray981[anInt1183 - 1];
                if (i1 == 9 || i1 == 225 || i1 == 444 || i1 == 564 || i1 == 894 || i1 == 961 || i1 == 399 || i1 == 324
                        || i1 == 227 || i1 == 891 || i1 == 52 || i1 == 1094) {
                    int l1 = anIntArray979[anInt1183 - 1];
                    int j2 = anIntArray980[anInt1183 - 1];
                    JagInterface class13 = JagInterface.forId(j2);
                    if (class13.aBoolean274 || class13.aBoolean217) {
                        aBoolean1155 = false;
                        anInt1269 = 0;
                        anInt1111 = j2;
                        anInt1112 = l1;
                        anInt1113 = 2;
                        anInt1114 = super.anInt29;
                        anInt1115 = super.anInt30;
                        if (JagInterface.forId(j2).anInt248 == anInt1169)
                            anInt1113 = 1;
                        if (JagInterface.forId(j2).anInt248 == anInt988)
                            anInt1113 = 3;
                        return;
                    }
                }
            }
            if (j == 1 && (anInt1300 == 1 || method126(anInt1183 - 1, aByte1161)) && anInt1183 > 2) {
                j = 2;
            }
            if (j == 1 && anInt1183 > 0) {
                sendOutgoingPackets(anInt1183 - 1, 8);
            }
            if (j == 2 && anInt1183 > 0) {
                method108(811);
            }
        }
    }

    public void method55(int i, RgbSprite class50_sub1_sub1_sub1, int j, int k) {
        int l = k * k + i * i;
        while (j >= 0)
            opcode = -1;
        if (l > 4225 && l < 0x15f90) {
            int i1 = anInt1252 + anInt916 & 0x7ff;
            int j1 = Model.sineTable[i1];
            int k1 = Model.cosineTable[i1];
            j1 = (j1 * 256) / (anInt1233 + 256);
            k1 = (k1 * 256) / (anInt1233 + 256);
            int l1 = i * j1 + k * k1 >> 16;
            int i2 = i * k1 - k * j1 >> 16;
            double d = Math.atan2(l1, i2);
            int j2 = (int) (Math.sin(d) * 63D);
            int k2 = (int) (Math.cos(d) * 57D);
            aClass50_Sub1_Sub1_Sub1_1247.method466(256, 15, (94 + j2 + 4) - 10, 15, 20, anInt1119, 20, d, 83 - k2 - 20);
            return;
        } else {
            method130(i, true, class50_sub1_sub1_sub1, k);
            return;
        }
    }

    public void method56(boolean flag, int i, int j, int k, int l, int i1) {
        aClass50_Sub1_Sub1_Sub3_1095.drawSprite(j, i1);
        aClass50_Sub1_Sub1_Sub3_1096.drawSprite(j, (i1 + k) - 16);
        Drawable.drawFullRect(j, i1 + 16, 16, k - 32, anInt931);
        int j1 = ((k - 32) * k) / l;
        if (j1 < 8)
            j1 = 8;
        int k1 = ((k - 32 - j1) * i) / (l - k);
        Drawable.drawFullRect(j, i1 + 16 + k1, 16, j1, anInt1080);
        Drawable.drawVerticalLine(colorBrown1135, j, i1 + 16 + k1, j1);
        Drawable.drawVerticalLine(colorBrown1135, j + 1, i1 + 16 + k1, j1);
        if (!flag)
            anInt921 = -136;
        Drawable.drawHorizontalLine(colorBrown1135, j, i1 + 16 + k1, 16);
        Drawable.drawHorizontalLine(colorBrown1135, j, i1 + 17 + k1, 16);
        Drawable.drawVerticalLine(anInt1287, j + 15, i1 + 16 + k1, j1);
        Drawable.drawVerticalLine(anInt1287, j + 14, i1 + 17 + k1, j1 - 1);
        Drawable.drawHorizontalLine(anInt1287, j, i1 + 15 + k1 + j1, 16);
        Drawable.drawHorizontalLine(anInt1287, j + 1, i1 + 14 + k1 + j1, 15);
    }

    public void addNpcsToScenegraph(boolean flag) {
        for (int j = 0; j < localNpcCount; j++) {
            Npc class50_sub1_sub4_sub3_sub1 = npcs[anIntArray1134[j]];
            int k = 0x20000000 + (anIntArray1134[j] << 14);
            if (class50_sub1_sub4_sub3_sub1 == null || !class50_sub1_sub4_sub3_sub1.isVisible()
                    || class50_sub1_sub4_sub3_sub1.def.aBoolean644 != flag
                    || !class50_sub1_sub4_sub3_sub1.def.method360(-993))
                continue;
            int l = ((Actor) (class50_sub1_sub4_sub3_sub1)).unitX >> 7;
            int i1 = ((Actor) (class50_sub1_sub4_sub3_sub1)).unitY >> 7;
            if (l < 0 || l >= 104 || i1 < 0 || i1 >= 104)
                continue;
            if (((Actor) (class50_sub1_sub4_sub3_sub1)).anInt1601 == 1
                    && (((Actor) (class50_sub1_sub4_sub3_sub1)).unitX & 0x7f) == 64
                    && (((Actor) (class50_sub1_sub4_sub3_sub1)).unitY & 0x7f) == 64) {
                if (anIntArrayArray886[l][i1] == tickCounter1138)
                    continue;
                anIntArrayArray886[l][i1] = tickCounter1138;
            }
            if (!class50_sub1_sub4_sub3_sub1.def.aBoolean631)
                k += 0x80000000;
            sceneGraph.method252(k, class50_sub1_sub4_sub3_sub1,
                    ((Actor) (class50_sub1_sub4_sub3_sub1)).unitX, getFloorDrawHeight(
                            ((Actor) (class50_sub1_sub4_sub3_sub1)).unitY,
                            ((Actor) (class50_sub1_sub4_sub3_sub1)).unitX, plane),
                    ((Actor) (class50_sub1_sub4_sub3_sub1)).aBoolean1592, 0, plane,
                    (((Actor) (class50_sub1_sub4_sub3_sub1)).anInt1601 - 1) * 64 + 60,
                    ((Actor) (class50_sub1_sub4_sub3_sub1)).unitY,
                    ((Actor) (class50_sub1_sub4_sub3_sub1)).anInt1612);
        }

    }

    public void method58(int i, int j) {
        signlink.wavevol = j;
        if (i <= 0) {
            anInt1051 = 57;
        }
    }

    public void method59(int i) {
        if (anInt873 > 0) {
            method124(true);
            return;
        }
        method125("Please wait - attempting to reestablish", "Connection lost");
        minimapState = 0;
        if (i != 1)
            aBoolean1242 = !aBoolean1242;
        anInt1120 = 0;
        JagSocket class17 = connection;
        isLoggedIn = false;
        anInt850 = 0;
        login(thisPlayerName, thisPlayerPassword, true);
        if (!isLoggedIn)
            method124(true);
        try {
            class17.closeConnection();
            return;
        } catch (Exception _ex) {
            return;
        }
    }

    public boolean method60(int i, JagInterface class13) {
        int j = class13.anInt242;
        if (i <= 0)
            opcode = -1;
        if (anInt860 == 2) {
            if (j == 201) {
                aBoolean1240 = true;
                chatboxInterfaceType = 0;
                aBoolean866 = true;
                userInputString = "";
                anInt1221 = 1;
                aString937 = "Enter name of friend to add to list";
            }
            if (j == 202) {
                aBoolean1240 = true;
                chatboxInterfaceType = 0;
                aBoolean866 = true;
                userInputString = "";
                anInt1221 = 2;
                aString937 = "Enter name of friend to delete from list";
            }
        }
        if (j == 205) {
            anInt873 = 250;
            return true;
        }
        if (j == 501) {
            aBoolean1240 = true;
            chatboxInterfaceType = 0;
            aBoolean866 = true;
            userInputString = "";
            anInt1221 = 4;
            aString937 = "Enter name of player to add to list";
        }
        if (j == 502) {
            aBoolean1240 = true;
            chatboxInterfaceType = 0;
            aBoolean866 = true;
            userInputString = "";
            anInt1221 = 5;
            aString937 = "Enter name of player to delete from list";
        }
        if (j >= 300 && j <= 313) {
            int k = (j - 300) / 2;
            int j1 = j & 1;
            int i2 = anIntArray1326[k];
            if (i2 != -1) {
                do {
                    if (j1 == 0 && --i2 < 0)
                        i2 = IdentityKit.count - 1;
                    if (j1 == 1 && ++i2 >= IdentityKit.count)
                        i2 = 0;
                } while (IdentityKit.identityKits[i2].notSelectable
                        || IdentityKit.identityKits[i2].part != k + (aBoolean1144 ? 0 : 7));
                anIntArray1326[k] = i2;
                aBoolean1277 = true;
            }
        }
        if (j >= 314 && j <= 323) {
            int l = (j - 314) / 2;
            int k1 = j & 1;
            int j2 = anIntArray1099[l];
            if (k1 == 0 && --j2 < 0)
                j2 = anIntArrayArray1008[l].length - 1;
            if (k1 == 1 && ++j2 >= anIntArrayArray1008[l].length)
                j2 = 0;
            anIntArray1099[l] = j2;
            aBoolean1277 = true;
        }
        if (j == 324 && !aBoolean1144) {
            aBoolean1144 = true;
            method25(anInt1015);
        }
        if (j == 325 && aBoolean1144) {
            aBoolean1144 = false;
            method25(anInt1015);
        }
        if (j == 326) {
            outBuffer.putOpcode(163);
            outBuffer.putByte(aBoolean1144 ? 0 : 1);
            for (int i1 = 0; i1 < 7; i1++)
                outBuffer.putByte(anIntArray1326[i1]);

            for (int l1 = 0; l1 < 5; l1++)
                outBuffer.putByte(anIntArray1099[l1]);

            return true;
        }
        if (j == 620)
            aBoolean1098 = !aBoolean1098;
        if (j >= 601 && j <= 613) {
            method15(false);
            if (aString839.length() > 0) {
                outBuffer.putOpcode(184);
                outBuffer.putLong(StringUtils.encodeBase37(aString839));
                outBuffer.putByte(j - 601);
                outBuffer.putByte(aBoolean1098 ? 1 : 0);
            }
        }
        return false;
    }

    public Archive loadArchive_61(int checksum, String s, int k, int archiveIndex, String loadingText) {
        byte abyte0[] = null;
        int i1 = 5;
        try {
            if (stores[0] != null)
                abyte0 = stores[0].get(archiveIndex);
        } catch (Exception _ex) {
        }
        if (abyte0 != null) {
           /* aCRC32_1088.reset();
            aCRC32_1088.update(abyte0);
            int j1 = (int) aCRC32_1088.getValue();
            if (j1 != j)
                abyte0 = null;*/
        }
        if (abyte0 != null) {
            Archive class2 = new Archive(abyte0);
            return class2;
        }
        int k1 = 0;
        anInt1281 = -343;
        while (abyte0 == null) {
            String s2 = "Unknown error";
            drawLoadingText(k, "Requesting " + loadingText);
            try {
                int l1 = 0;
                DataInputStream datainputstream = method31(s + checksum);
                byte abyte1[] = new byte[6];
                datainputstream.readFully(abyte1, 0, 6);
                JagBuffer class50_sub1_sub2 = new JagBuffer(abyte1);
                class50_sub1_sub2.position = 3;
                int j2 = class50_sub1_sub2.getTriByte() + 6;
                int k2 = 6;
                abyte0 = new byte[j2];
                for (int l2 = 0; l2 < 6; l2++)
                    abyte0[l2] = abyte1[l2];

                while (k2 < j2) {
                    int i3 = j2 - k2;
                    if (i3 > 1000)
                        i3 = 1000;
                    int k3 = datainputstream.read(abyte0, k2, i3);
                    if (k3 < 0) {
                        s2 = "Length error: " + k2 + "/" + j2;
                        throw new IOException("EOF");
                    }
                    k2 += k3;
                    int l3 = (k2 * 100) / j2;
                    if (l3 != l1)
                        drawLoadingText(k, "Loading " + loadingText + " - " + l3 + "%");
                    l1 = l3;
                }
                datainputstream.close();
                try {
                    if (stores[0] != null)
                        stores[0].put(abyte0.length, abyte0, archiveIndex);
                } catch (Exception _ex) {
                    stores[0] = null;
                }
                if (abyte0 != null) {
                    aCRC32_1088.reset();
                    aCRC32_1088.update(abyte0);
                    int newChecksum = (int) aCRC32_1088.getValue();
                    if (newChecksum != checksum) {
                        abyte0 = null;
                        k1++;
                        s2 = "Checksum error: " + newChecksum;
                    }
                }
            } catch (IOException ioexception) {
                if (s2.equals("Unknown error"))
                    s2 = "Connection error";
                abyte0 = null;
            } catch (NullPointerException _ex) {
                s2 = "Null error";
                abyte0 = null;
                if (!signlink.reporterror)
                    return null;
            } catch (ArrayIndexOutOfBoundsException _ex) {
                s2 = "Bounds error";
                abyte0 = null;
                if (!signlink.reporterror)
                    return null;
            } catch (Exception _ex) {
                s2 = "Unexpected error";
                abyte0 = null;
                if (!signlink.reporterror)
                    return null;
            }
            if (abyte0 == null) {
                for (int i2 = i1; i2 > 0; i2--) {
                    if (k1 >= 3) {
                        drawLoadingText(k, "Game updated - please reload page");
                        i2 = 10;
                    } else {
                        drawLoadingText(k, s2 + " - Retrying in " + i2);
                    }
                    try {
                        Thread.sleep(1000L);
                    } catch (Exception _ex) {
                    }
                }

                i1 *= 2;
                if (i1 > 60)
                    i1 = 60;
                aBoolean900 = !aBoolean900;
            }
        }
        Archive class2_1 = new Archive(abyte0);
        return class2_1;
    }

    public void needsUIRedraw() {
        shouldRenderUI = true;
    }

    public void parseNpcBlocks(JagBuffer buf, int i, int j) {
        j = 24 / j;
        for (int k = 0; k < updatedPlayerCount; k++) {
            int l = updatedPlayers[k];
            Npc npc = npcs[l];
            int updateMask = buf.getByte();

            // Update NPC definition
            if ((updateMask & 1) != 0) {
                npc.def = NpcDefinition.forId(buf.getShortAdded());
                npc.anInt1601 = npc.def.aByte642;
                npc.anInt1600 = npc.def.anInt651;
                npc.anInt1619 = npc.def.anInt645;
                npc.anInt1620 = npc.def.anInt643;
                npc.anInt1621 = npc.def.anInt641;
                npc.anInt1622 = npc.def.anInt633;
                npc.anInt1634 = npc.def.anInt621;
            }
            // Update NPC transformation
            if ((updateMask & 0x40) != 0) {
                npc.transformationId = buf.getLEShort();
                if (npc.transformationId == 65535)
                    npc.transformationId = -1;
            }

            // Update NPC animation
            if ((updateMask & 0x80) != 0) {
                int j1 = buf.getByteAdded();
                int j2 = buf.getByteAdded();
                npc.applyHit(pulseCycle, false, j1, j2);
                npc.lastHitCycle = pulseCycle + 300;
                npc.hitType = buf.getByte();
                npc.hitAmount = buf.getByteSubtracted();
            }
            if ((updateMask & 4) != 0) {
                npc.anInt1614 = buf.getShort();
                int k1 = buf.method556();
                npc.anInt1618 = k1 >> 16;
                npc.anInt1617 = pulseCycle + (k1 & 0xffff);
                npc.anInt1615 = 0;
                npc.anInt1616 = 0;
                if (npc.anInt1617 > pulseCycle)
                    npc.anInt1615 = -1;
                if (npc.anInt1614 == 65535)
                    npc.anInt1614 = -1;
            }
            // NPC forced chat
            if ((updateMask & 0x20) != 0) {
                npc.forcedChatMessage = buf.getString();
                npc.forcedChatTicks = 100;
            }
            // Update NPC movement
            if ((updateMask & 8) != 0) {
                npc.nextStepX = buf.getLEShortA();
                npc.nextStepY = buf.getLEShort();
            }

            // Update NPC animation
            if ((updateMask & 2) != 0) {
                int animationId = buf.getShort();
                if (animationId == 65535)
                    animationId = -1;
                int animationDelay = buf.getByteSubtracted();
                if (animationId == npc.currentAnimation && animationId != -1) {
                    int animationType = Animation.animations[animationId].type;
                    if (animationType == 1) {
                        npc.animationFrame = 0;
                        npc.animationFrameCycle = 0;
                        npc.animationDelay = animationDelay;
                        npc.animationResetCycle = 0;
                    }
                    if (animationType == 2)
                        npc.animationResetCycle = 0;
                } else if (animationId == -1
                        || npc.currentAnimation == -1
                        || Animation.animations[animationId].anInt301 >= Animation.animations[npc.currentAnimation].anInt301) {
                    npc.currentAnimation = animationId;
                    npc.animationFrame = 0;
                    npc.animationFrameCycle = 0;
                    npc.animationDelay = animationDelay;
                    npc.animationResetCycle = 0;
                    npc.anInt1613 = npc.walkingQueueSize;
                }
            }

            // Update NPC facing direction
            if ((updateMask & 0x10) != 0) {
                int facingX = buf.getByteSubtracted();
                int facingY = buf.getByteSubtracted();
                npc.applyHit(pulseCycle, false, facingX, facingY);
                npc.lastHitCycle = pulseCycle + 300;
                npc.hitType = buf.getByte();
                npc.hitAmount = buf.getByteNegated();
            }
        }

    }

    public void parsePlayerBlock(int id, Player plr, int mask, JagBuffer vec) {
        if ((mask & 8) != 0) {
            int i1 = vec.getShort();
            if (i1 == 65535)
                i1 = -1;
            int k2 = vec.getByteSubtracted();
            if (i1 == ((Actor) (plr)).currentAnimation && i1 != -1) {
                int k3 = Animation.animations[i1].type;
                if (k3 == 1) {
                    plr.animationFrame = 0;
                    plr.animationFrameCycle = 0;
                    plr.animationDelay = k2;
                    plr.animationResetCycle = 0;
                }
                if (k3 == 2)
                    plr.animationResetCycle = 0;
            } else if (i1 == -1
                    || ((Actor) (plr)).currentAnimation == -1
                    || Animation.animations[i1].anInt301 >= Animation.animations[((Actor) (plr)).currentAnimation].anInt301) {
                plr.currentAnimation = i1;
                plr.animationFrame = 0;
                plr.animationFrameCycle = 0;
                plr.animationDelay = k2;
                plr.animationResetCycle = 0;
                plr.anInt1613 = ((Actor) (plr)).walkingQueueSize;
            }
        }
        if ((mask & 0x10) != 0) {
            plr.forcedChatMessage = vec.getString();
            if (((Actor) (plr)).forcedChatMessage.charAt(0) == '~') {
                plr.forcedChatMessage = ((Actor) (plr)).forcedChatMessage.substring(1);
                pushMessage(plr.username, (byte) -123, ((Actor) (plr)).forcedChatMessage, 2);
            } else if (plr == thisPlayer)
                pushMessage(plr.username, (byte) -123, ((Actor) (plr)).forcedChatMessage, 2);
            plr.anInt1583 = 0;
            plr.anInt1593 = 0;
            plr.forcedChatTicks = 150;
        }
        if ((mask & 0x100) != 0) {
            plr.anInt1602 = vec.getByteAdded();
            plr.anInt1604 = vec.getByteNegated();
            plr.anInt1603 = vec.getByteSubtracted();
            plr.anInt1605 = vec.getByte();
            plr.anInt1606 = vec.getShort() + pulseCycle;
            plr.anInt1607 = vec.getShortAdded() + pulseCycle;
            plr.anInt1608 = vec.getByte();
            plr.resetWalkingQueue();
        }
        if ((mask & 1) != 0) {
            plr.transformationId = vec.getShortAdded();
            if (((Actor) (plr)).transformationId == 65535)
                plr.transformationId = -1;
        }
        if ((mask & 2) != 0) {
            plr.nextStepX = vec.getShort();
            plr.nextStepY = vec.getShort();
        }
        if ((mask & 0x200) != 0) {
            plr.anInt1614 = vec.getShortAdded();
            int j1 = vec.method556();
            plr.anInt1618 = j1 >> 16;
            plr.anInt1617 = pulseCycle + (j1 & 0xffff);
            plr.anInt1615 = 0;
            plr.anInt1616 = 0;
            if (((Actor) (plr)).anInt1617 > pulseCycle)
                plr.anInt1615 = -1;
            if (((Actor) (plr)).anInt1614 == 65535)
                plr.anInt1614 = -1;
        }
        if ((mask & 4) != 0) {
            int size = vec.getByte();
            byte bytes[] = new byte[size];
            JagBuffer appearance = new JagBuffer(bytes);
            vec.getBytesReverse(bytes, 0, size);
            cachedAppearances[id] = appearance;
            plr.updateAppearance(appearance, 0);
        }
        if ((mask & 0x400) != 0) {
            int l1 = vec.getByteAdded();
            int l2 = vec.getByteSubtracted();
            plr.applyHit(pulseCycle, false, l1, l2);
            plr.lastHitCycle = pulseCycle + 300;
            plr.hitType = vec.getByteNegated();
            plr.hitAmount = vec.getByte();
        }
        if ((mask & 0x40) != 0) {
            int i2 = vec.getShort();
            int rights = vec.getByteNegated();
            int length = vec.getByteAdded();
            int i4 = vec.position;
            if (plr.username != null && plr.visible) {
                long l4 = StringUtils.encodeBase37(plr.username);
                boolean flag = false;
                if (rights <= 1) {
                    for (int j4 = 0; j4 < ignoresCount; j4++) {
                        if (ignores[j4] != l4)
                            continue;
                        flag = true;
                        break;
                    }

                }
                if (!flag && anInt1246 == 0)
                    try {
                        aClass50_Sub1_Sub2_1131.position = 0;
                        vec.getBytesAdded(aClass50_Sub1_Sub2_1131.buffer, 0, length);
                        aClass50_Sub1_Sub2_1131.position = 0;
                        String s = ChatCompressor.decompress(aClass50_Sub1_Sub2_1131, length);
                        s = ChatFilter.applyCensor((byte) 0, s);
                        plr.forcedChatMessage = s;
                        plr.anInt1583 = i2 >> 8;
                        plr.anInt1593 = i2 & 0xff;
                        plr.forcedChatTicks = 150;
                        if (rights == 2 || rights == 3)
                            pushMessage("@cr2@" + plr.username, (byte) -123, s, 1);
                        else if (rights == 1)
                            pushMessage("@cr1@" + plr.username, (byte) -123, s, 1);
                        else
                            pushMessage(plr.username, (byte) -123, s, 2);
                    } catch (Exception exception) {
                        signlink.reporterror("cde2");
                    }
            }
            vec.position = i4 + length;
        }
        if ((mask & 0x80) != 0) {
            int j2 = vec.getByteSubtracted();
            int j3 = vec.getByteNegated();
            plr.applyHit(pulseCycle, false, j2, j3);
            plr.lastHitCycle = pulseCycle + 300;
            plr.hitType = vec.getByteSubtracted();
            plr.hitAmount = vec.getByte();
        }
    }

    public void prepareLoginUI() {
        if (loginBackground_1 != null) {
            return;
        }
        super.imageProducer = null;
        chatboxImage_1159 = null;
        aClass18_1157 = null;
        inventoryImage = null;
        gameViewportImage = null;
        chatboxButtons = null;
        aClass18_1109 = null;
        aClass18_1110 = null;

        loginFlameLeft = new JagImageProducer(128, 265, getParentComponent());
        Drawable.clearScreen();
        loginFlameRight = new JagImageProducer(128, 265, getParentComponent());
        Drawable.clearScreen();
        loginBackground_1 = new JagImageProducer(509, 171, getParentComponent());
        Drawable.clearScreen();
        loginBackground_2 = new JagImageProducer(360, 132, getParentComponent());
        Drawable.clearScreen();
        loginboxElement = new JagImageProducer(360, 200, getParentComponent());
        Drawable.clearScreen();
        loginBackground_3 = new JagImageProducer(202, 238, getParentComponent());
        Drawable.clearScreen();
        loginBackground_4 = new JagImageProducer(203, 238, getParentComponent());
        Drawable.clearScreen();
        loginBackground_5 = new JagImageProducer(74, 94, getParentComponent());
        Drawable.clearScreen();
        loginBackground_6 = new JagImageProducer(75, 94, getParentComponent());
        Drawable.clearScreen();
        if (titleArchive != null) {
            loadPixelsLoginScreen_139();
            prepareLoginScreen_52(false);
        }
        shouldRenderUI = true;
    }

    public void load() {
        drawLoadingText(20, "Starting up");
        if (signlink.sunjava) {
            super.minDelay = 5;
        }
        if (started) {
            aBoolean1016 = true;
            return;
        }
        started = true;
        boolean flag = false;
        String s = method37(-42588);
        if (s.endsWith("jagex.com"))
            flag = true;
        if (s.endsWith("runescape.com"))
            flag = true;
        if (s.endsWith("192.168.1.2"))
            flag = true;
        if (s.endsWith("192.168.1.231"))
            flag = true;
        if (s.endsWith("192.168.1.229"))
            flag = true;
        if (s.endsWith("192.168.1.228"))
            flag = true;
        if (s.endsWith("192.168.1.227"))
            flag = true;
        if (s.endsWith("192.168.1.226"))
            flag = true;
        if (s.endsWith("192.168.1.224"))
            flag = true;
        if (s.endsWith("192.168.1.223"))
            flag = true;
        if (s.endsWith("192.168.1.221"))
            flag = true;
        if (s.endsWith("127.0.0.1"))
            flag = true;
        if (!flag) {
            aBoolean1097 = true;
            return;
        }
        if (signlink.cache_dat != null) {
            for (int type = 0; type < 5; type++)
                stores[type] = new FileStore(type + 1, 0x927c0, signlink.cache_dat, signlink.cache_idx[type]);
        }
        try {
            //connectUpdateServer(false);
            titleArchive = loadArchive_61(archiveHashes[1], "title", 25, 1, "title screen");
            font_p11_full = new JagFont(titleArchive, "p11_full", false);
            fontChatboxButtons = new JagFont(titleArchive, "p12_full", false);
            loginScreenFont = new JagFont(titleArchive, "b12_full", false);
            font_q9_full = new JagFont(titleArchive, "q8_full", true);
            loadPixelsLoginScreen_139();
            prepareLoginScreen_52(false);
            Archive configArchive = loadArchive_61(archiveHashes[2], "config", 30, 2, "config");
            Archive interfaceArchive = loadArchive_61(archiveHashes[3], "interface", 35, 3, "interface");
            Archive mediaArchive = loadArchive_61(archiveHashes[4], "media", 40, 4, "2d graphics");
            Archive textureArchive = loadArchive_61(archiveHashes[6], "textures", 45, 6, "textures");
            Archive chatArchive = loadArchive_61(archiveHashes[7], "wordenc", 50, 7, "chat system");
            Archive soundArchive = loadArchive_61(archiveHashes[8], "sounds", 55, 8, "sound effects");
            aByteArrayArrayArray1125 = new byte[4][104][104];
            intGroundArray = new int[4][105][105];
            sceneGraph = new SceneGraph(intGroundArray, 104, 4, 104, (byte) 5);
            for (int j = 0; j < 4; j++) {
                clippingPlanes[j] = new ClippingPlane(104, 0, 104);
            }

            rbgSprite_1122 = new RgbSprite(512, 512);
            Archive versionListArchive = loadArchive_61(archiveHashes[5], "versionlist", 60, 5, "update list");
            drawLoadingText(60, "Connecting to update server");
            fileFetcher = new OnDemandFetcher();
            fileFetcher.init(versionListArchive, this);
            AnimationFrame.initFrames(fileFetcher.method343(553));
            Model.init(fileFetcher.method340(0), fileFetcher);
            if (!lowMemory) {
                anInt1270 = 0;
                aBoolean1271 = true;
                fileFetcher.request(2, anInt1270);
                while (fileFetcher.method333() > 0) {
                    method77(false);
                    try {
                        Thread.sleep(100L);
                    } catch (Exception _ex) {
                    }
                    if (fileFetcher.anInt1379 > 3) {
                        printError_19("ondemand");
                        return;
                    }
                }
            }
            drawLoadingText(65, "Requesting animations");
            int k = fileFetcher.method340(1);
            for (int l = 0; l < k; l++)
                fileFetcher.request(1, l);

            while (fileFetcher.method333() > 0) {
                int i1 = k - fileFetcher.method333();
                if (i1 > 0)
                    drawLoadingText(65, "Loading animations - " + (i1 * 100) / k + "%");
                method77(false);
                try {
                    Thread.sleep(100L);
                } catch (Exception _ex) {
                }
                if (fileFetcher.anInt1379 > 3) {
                    printError_19("ondemand");
                    return;
                }
            }
            drawLoadingText(70, "Requesting models");
            k = fileFetcher.method340(0);
            for (int j1 = 0; j1 < k; j1++) {
                int k1 = fileFetcher.method325(j1, -493);
                if ((k1 & 1) != 0)
                    fileFetcher.request(0, j1);
            }

            k = fileFetcher.method333();
            while (fileFetcher.method333() > 0) {
                int l1 = k - fileFetcher.method333();
                if (l1 > 0)
                    drawLoadingText(70, "Loading models - " + (l1 * 100) / k + "%");
                method77(false);
                try {
                    Thread.sleep(100L);
                } catch (Exception _ex) {
                }
            }
            if (stores[0] != null) {
                drawLoadingText(75, "Requesting maps");
                fileFetcher.request(3, fileFetcher.method344(0, 47, 48, 0)); // these are the maps around tutorial island
                fileFetcher.request(3, fileFetcher.method344(0, 47, 48, 1));
                fileFetcher.request(3, fileFetcher.method344(0, 48, 48, 0));
                fileFetcher.request(3, fileFetcher.method344(0, 48, 48, 1));
                fileFetcher.request(3, fileFetcher.method344(0, 49, 48, 0));
                fileFetcher.request(3, fileFetcher.method344(0, 49, 48, 1));
                fileFetcher.request(3, fileFetcher.method344(0, 47, 47, 0));
                fileFetcher.request(3, fileFetcher.method344(0, 47, 47, 1));
                fileFetcher.request(3, fileFetcher.method344(0, 48, 47, 0));
                fileFetcher.request(3, fileFetcher.method344(0, 48, 47, 1));
                fileFetcher.request(3, fileFetcher.method344(0, 48, 148, 0));
                fileFetcher.request(3, fileFetcher.method344(0, 48, 148, 1));
                k = fileFetcher.method333();
                while (fileFetcher.method333() > 0) {
                    int i2 = k - fileFetcher.method333();
                    if (i2 > 0)
                        drawLoadingText(75, "Loading maps - " + (i2 * 100) / k + "%");
                    method77(false);
                    try {
                        Thread.sleep(100L);
                    } catch (Exception _ex) {
                    }
                }
            }
            k = fileFetcher.method340(0);
            for (int j2 = 0; j2 < k; j2++) {
                int k2 = fileFetcher.method325(j2, -493);
                byte byte0 = 0;
                if ((k2 & 8) != 0)
                    byte0 = 10;
                else if ((k2 & 0x20) != 0)
                    byte0 = 9;
                else if ((k2 & 0x10) != 0)
                    byte0 = 8;
                else if ((k2 & 0x40) != 0)
                    byte0 = 7;
                else if ((k2 & 0x80) != 0)
                    byte0 = 6;
                else if ((k2 & 2) != 0)
                    byte0 = 5;
                else if ((k2 & 4) != 0)
                    byte0 = 4;
                if ((k2 & 1) != 0)
                    byte0 = 3;
                if (byte0 != 0)
                    fileFetcher.method327(-44, 0, byte0, j2);
            }

            fileFetcher.method332(memberServer, (byte) 109);
            if (!lowMemory) {
                k = fileFetcher.method340(2);
                for (int l2 = 1; l2 < k; l2++)
                    if (fileFetcher.method328(l2, aBoolean963))
                        fileFetcher.method327(-44, 2, (byte) 1, l2);

            }
            k = fileFetcher.method340(0);
            for (int i3 = 0; i3 < k; i3++) {
                int j3 = fileFetcher.method325(i3, -493);
                if (j3 == 0 && fileFetcher.anInt1350 < 200)
                    fileFetcher.method327(-44, 0, (byte) 1, i3);
            }

            drawLoadingText(80, "Unpacking media");
            aClass50_Sub1_Sub1_Sub3_1185 = new IndexedSprite(mediaArchive, "invback", 0);
            aClass50_Sub1_Sub1_Sub3_1187 = new IndexedSprite(mediaArchive, "chatback", 0);
            mapback_1186 = new IndexedSprite(mediaArchive, "mapback", 0);
            aClass50_Sub1_Sub1_Sub3_965 = new IndexedSprite(mediaArchive, "backbase1", 0);
            aClass50_Sub1_Sub1_Sub3_966 = new IndexedSprite(mediaArchive, "backbase2", 0);
            aClass50_Sub1_Sub1_Sub3_967 = new IndexedSprite(mediaArchive, "backhmid1", 0);
            for (int k3 = 0; k3 < 13; k3++)
                aClass50_Sub1_Sub1_Sub3Array976[k3] = new IndexedSprite(mediaArchive, "sideicons", k3);

            rbgSprite_compass_1116 = new RgbSprite(mediaArchive, "compass", 0);
            aClass50_Sub1_Sub1_Sub1_1247 = new RgbSprite(mediaArchive, "mapedge", 0);
            aClass50_Sub1_Sub1_Sub1_1247.method458();
            for (int l3 = 0; l3 < 72; l3++)
                aClass50_Sub1_Sub1_Sub3Array1153[l3] = new IndexedSprite(mediaArchive, "mapscene", l3);

            for (int i4 = 0; i4 < 70; i4++)
                aClass50_Sub1_Sub1_Sub1Array1031[i4] = new RgbSprite(mediaArchive, "mapfunction", i4);

            for (int j4 = 0; j4 < 5; j4++)
                spriteArray1182[j4] = new RgbSprite(mediaArchive, "hitmarks", j4);

            for (int k4 = 0; k4 < 6; k4++)
                aClass50_Sub1_Sub1_Sub1Array1288[k4] = new RgbSprite(mediaArchive, "headicons_pk", k4);

            for (int l4 = 0; l4 < 9; l4++)
                aClass50_Sub1_Sub1_Sub1Array1079[l4] = new RgbSprite(mediaArchive, "headicons_prayer", l4);

            for (int i5 = 0; i5 < 6; i5++)
                aClass50_Sub1_Sub1_Sub1Array954[i5] = new RgbSprite(mediaArchive, "headicons_hint", i5);

            aClass50_Sub1_Sub1_Sub1_1086 = new RgbSprite(mediaArchive, "overlay_multiway", 0);
            aClass50_Sub1_Sub1_Sub1_1036 = new RgbSprite(mediaArchive, "mapmarker", 0);
            aClass50_Sub1_Sub1_Sub1_1037 = new RgbSprite(mediaArchive, "mapmarker", 1);
            for (int j5 = 0; j5 < 8; j5++)
                aClass50_Sub1_Sub1_Sub1Array896[j5] = new RgbSprite(mediaArchive, "cross", j5);

            rgbSprite_1192 = new RgbSprite(mediaArchive, "mapdots", 0);
            aClass50_Sub1_Sub1_Sub1_1193 = new RgbSprite(mediaArchive, "mapdots", 1);
            aClass50_Sub1_Sub1_Sub1_1194 = new RgbSprite(mediaArchive, "mapdots", 2);
            aClass50_Sub1_Sub1_Sub1_1195 = new RgbSprite(mediaArchive, "mapdots", 3);
            aClass50_Sub1_Sub1_Sub1_1196 = new RgbSprite(mediaArchive, "mapdots", 4);
            aClass50_Sub1_Sub1_Sub3_1095 = new IndexedSprite(mediaArchive, "scrollbar", 0);
            aClass50_Sub1_Sub1_Sub3_1096 = new IndexedSprite(mediaArchive, "scrollbar", 1);
            aClass50_Sub1_Sub1_Sub3_880 = new IndexedSprite(mediaArchive, "redstone1", 0);
            aClass50_Sub1_Sub1_Sub3_881 = new IndexedSprite(mediaArchive, "redstone2", 0);
            aClass50_Sub1_Sub1_Sub3_882 = new IndexedSprite(mediaArchive, "redstone3", 0);
            aClass50_Sub1_Sub1_Sub3_883 = new IndexedSprite(mediaArchive, "redstone1", 0);
            aClass50_Sub1_Sub1_Sub3_883.method487(0);
            aClass50_Sub1_Sub1_Sub3_884 = new IndexedSprite(mediaArchive, "redstone2", 0);
            aClass50_Sub1_Sub1_Sub3_884.method487(0);
            aClass50_Sub1_Sub1_Sub3_983 = new IndexedSprite(mediaArchive, "redstone1", 0);
            aClass50_Sub1_Sub1_Sub3_983.method488((byte) 7);
            aClass50_Sub1_Sub1_Sub3_984 = new IndexedSprite(mediaArchive, "redstone2", 0);
            aClass50_Sub1_Sub1_Sub3_984.method488((byte) 7);
            aClass50_Sub1_Sub1_Sub3_985 = new IndexedSprite(mediaArchive, "redstone3", 0);
            aClass50_Sub1_Sub1_Sub3_985.method488((byte) 7);
            aClass50_Sub1_Sub1_Sub3_986 = new IndexedSprite(mediaArchive, "redstone1", 0);
            aClass50_Sub1_Sub1_Sub3_986.method487(0);
            aClass50_Sub1_Sub1_Sub3_986.method488((byte) 7);
            aClass50_Sub1_Sub1_Sub3_987 = new IndexedSprite(mediaArchive, "redstone2", 0);
            aClass50_Sub1_Sub1_Sub3_987.method487(0);
            aClass50_Sub1_Sub1_Sub3_987.method488((byte) 7);
            for (int k5 = 0; k5 < 2; k5++)
                aClass50_Sub1_Sub1_Sub3Array1142[k5] = new IndexedSprite(mediaArchive, "mod_icons", k5);

            RgbSprite rbgSprite = new RgbSprite(mediaArchive, "backleft1", 0);
            aClass18_906 = new JagImageProducer(rbgSprite.width_1490, rbgSprite.height_1491, getParentComponent());
            rbgSprite.method459(0, 0);
            rbgSprite = new RgbSprite(mediaArchive, "backleft2", 0);
            uiSideChatboxLeft = new JagImageProducer(rbgSprite.width_1490, rbgSprite.height_1491, getParentComponent());
            rbgSprite.method459(0, 0);
            rbgSprite = new RgbSprite(mediaArchive, "backright1", 0);
            uiSideMinimapRight = new JagImageProducer(rbgSprite.width_1490, rbgSprite.height_1491, getParentComponent());
            rbgSprite.method459(0, 0);
            rbgSprite = new RgbSprite(mediaArchive, "backright2", 0);
            uiSideRockRight1 = new JagImageProducer(rbgSprite.width_1490, rbgSprite.height_1491, getParentComponent());
            rbgSprite.method459(0, 0);
            rbgSprite = new RgbSprite(mediaArchive, "backtop1", 0);
            aClass18_910 = new JagImageProducer(rbgSprite.width_1490, rbgSprite.height_1491, getParentComponent());
            rbgSprite.method459(0, 0);
            rbgSprite = new RgbSprite(mediaArchive, "backvmid1", 0);
            uiSideMinimapLeft = new JagImageProducer(rbgSprite.width_1490, rbgSprite.height_1491, getParentComponent());
            rbgSprite.method459(0, 0);
            rbgSprite = new RgbSprite(mediaArchive, "backvmid2", 0);
            uiSideRockLeft1 = new JagImageProducer(rbgSprite.width_1490, rbgSprite.height_1491, getParentComponent());
            rbgSprite.method459(0, 0);
            rbgSprite = new RgbSprite(mediaArchive, "backvmid3", 0);
            uiSideChatboxRight = new JagImageProducer(rbgSprite.width_1490, rbgSprite.height_1491, getParentComponent());
            rbgSprite.method459(0, 0);
            rbgSprite = new RgbSprite(mediaArchive, "backhmid2", 0);
            uiSideChatboxTop = new JagImageProducer(rbgSprite.width_1490, rbgSprite.height_1491, getParentComponent());
            rbgSprite.method459(0, 0);
            int l5 = (int) (Math.random() * 21D) - 10;
            int i6 = (int) (Math.random() * 21D) - 10;
            int j6 = (int) (Math.random() * 21D) - 10;
            int k6 = (int) (Math.random() * 41D) - 20;
            for (int l6 = 0; l6 < 100; l6++) {
                if (aClass50_Sub1_Sub1_Sub1Array1031[l6] != null)
                    aClass50_Sub1_Sub1_Sub1Array1031[l6].method457(j6 + k6, i6 + k6, l5 + k6);
                if (aClass50_Sub1_Sub1_Sub3Array1153[l6] != null)
                    aClass50_Sub1_Sub1_Sub3Array1153[l6].rgbAdjust_489(j6 + k6, i6 + k6, l5 + k6, -235);
            }

            drawLoadingText(83, "Unpacking textures");
            ThreeDimensionalCanvas.unpackTextures(textureArchive);
            ThreeDimensionalCanvas.initColorTable(0.80000000000000004D, (byte) 6);
            ThreeDimensionalCanvas.initTextureBufferPool(20);
            drawLoadingText(86, "Unpacking config");
            Animation.unpack(configArchive);
            ObjectDefinition.unpack(configArchive);
            TileDefinition.unpack(configArchive);
            ItemDefinition.unpack(configArchive);
            NpcDefinition.unpack(configArchive);
            IdentityKit.unpack(configArchive);
            SpotAnimation.unpack(configArchive);
            Varp.unpack(configArchive);
            Varbit.unpack(configArchive);
            ItemDefinition.memberServer = memberServer;
            if (!lowMemory) {
                drawLoadingText(90, "Unpacking sounds");
                byte abyte0[] = soundArchive.get("sounds.dat");
                JagBuffer buf = new JagBuffer(abyte0);
                Sound.unpack(buf);
            }
            drawLoadingText(95, "Unpacking interfaces");
            JagFont aclass50_sub1_sub1_sub2[] = {font_p11_full,
                    fontChatboxButtons, loginScreenFont, font_q9_full};
            JagInterface.unpack(-845, aclass50_sub1_sub1_sub2, interfaceArchive, mediaArchive);
            drawLoadingText(100, "Preparing game engine");
            for (int i7 = 0; i7 < 33; i7++) {
                int j7 = 999;
                int l7 = 0;
                for (int j8 = 0; j8 < 34; j8++) {
                    if (mapback_1186.pixels_1516[j8 + i7 * mapback_1186.width_1518] == 0) {
                        if (j7 == 999)
                            j7 = j8;
                        continue;
                    }
                    if (j7 == 999)
                        continue;
                    l7 = j8;
                    break;
                }

                anIntArray1180[i7] = j7;
                anIntArray1286[i7] = l7 - j7;
            }

            for (int k7 = 5; k7 < 156; k7++) {
                int i8 = 999;
                int k8 = 0;
                for (int i9 = 25; i9 < 172; i9++) {
                    if (mapback_1186.pixels_1516[i9 + k7 * mapback_1186.width_1518] == 0
                            && (i9 > 34 || k7 > 34)) {
                        if (i8 == 999)
                            i8 = i9;
                        continue;
                    }
                    if (i8 == 999)
                        continue;
                    k8 = i9;
                    break;
                }

                anIntArray1019[k7 - 5] = i8 - 25;
                anIntArray920[k7 - 5] = k8 - i8;
            }

            ThreeDimensionalCanvas.init3D(765, 503);
            clientEntireOffsets = ThreeDimensionalCanvas.lineOffsets;
            ThreeDimensionalCanvas.init3D(479, 96);
            chatBoxOffsets = ThreeDimensionalCanvas.lineOffsets;
            ThreeDimensionalCanvas.init3D(190, 261);
            tabsOffsets = ThreeDimensionalCanvas.lineOffsets;
            ThreeDimensionalCanvas.init3D(512, 334);
            gameViewportOffsets = ThreeDimensionalCanvas.lineOffsets;
            int ai[] = new int[9];
            for (int l8 = 0; l8 < 9; l8++) {
                int j9 = 128 + l8 * 32 + 15;
                int k9 = 600 + j9 * 3;
                int l9 = ThreeDimensionalCanvas.sineTable[j9];
                ai[l8] = k9 * l9 >> 16;
            }

            SceneGraph.preCalcFrustrumTable(ai, 512, 334, 500, 800);
            ChatFilter.unpack(chatArchive);
            mouseRecorder = new MouseRecorder(this);
            startThread(mouseRecorder, 10);
            GameObject.aClient1723 = this;
            ObjectDefinition.aClient770 = this;
            NpcDefinition.aClient629 = this;
            return;
        } catch (Exception exception) {
            signlink.reporterror("loaderror " + aString1027 + " " + anInt1322);
        }
        aBoolean1283 = true;
    }

    public void animateTexture_65(int i) {
        if (!lowMemory) {
            for (int k = 0; k < anIntArray1290.length; k++) {
                int l = anIntArray1290[k];
                if (ThreeDimensionalCanvas.anIntArray1546[l] >= i) {
                    IndexedSprite class50_sub1_sub1_sub3 = ThreeDimensionalCanvas.textures[l];
                    int i1 = class50_sub1_sub1_sub3.width_1518 * class50_sub1_sub1_sub3.height_1519 - 1;
                    int j1 = class50_sub1_sub1_sub3.width_1518 * anInt951 * 2;
                    byte abyte0[] = class50_sub1_sub1_sub3.pixels_1516;
                    byte abyte1[] = aByteArray1245;
                    for (int k1 = 0; k1 <= i1; k1++) {
                        abyte1[k1] = abyte0[k1 - j1 & i1];
                    }

                    class50_sub1_sub1_sub3.pixels_1516 = abyte1;
                    aByteArray1245 = abyte0;
                    ThreeDimensionalCanvas.pushTexture(l);
                }
            }
        }
    }

    public void method66(int i, JagInterface class13, int j, int k, int l, int i1, int j1, int k1) {
        if (j1 != 23658)
            return;
        if (class13.type != 0 || class13.anIntArray258 == null || class13.aBoolean219)
            return;
        if (i1 < l || k1 < i || i1 > l + class13.width || k1 > i + class13.height)
            return;
        int l1 = class13.anIntArray258.length;
        for (int i2 = 0; i2 < l1; i2++) {
            int j2 = class13.anIntArray232[i2] + l;
            int k2 = (class13.anIntArray276[i2] + i) - k;
            JagInterface class13_1 = JagInterface.forId(class13.anIntArray258[i2]);
            j2 += class13_1.anInt228;
            k2 += class13_1.anInt259;
            if ((class13_1.anInt254 >= 0 || class13_1.anInt261 != 0) && i1 >= j2 && k1 >= k2
                    && i1 < j2 + class13_1.width && k1 < k2 + class13_1.height)
                if (class13_1.anInt254 >= 0)
                    anInt915 = class13_1.anInt254;
                else
                    anInt915 = class13_1.id;
            if (class13_1.type == 8 && i1 >= j2 && k1 >= k2 && i1 < j2 + class13_1.width
                    && k1 < k2 + class13_1.height)
                anInt1315 = class13_1.id;
            if (class13_1.type == 0) {
                method66(k2, class13_1, j, class13_1.anInt231, j2, i1, 23658, k1);
                if (class13_1.anInt285 > class13_1.height)
                    method42(class13_1.anInt285, k2, class13_1, (byte) 102, k1, j, i1, class13_1.height, j2
                            + class13_1.width);
            } else {
                if (class13_1.anInt289 == 1 && i1 >= j2 && k1 >= k2 && i1 < j2 + class13_1.width
                        && k1 < k2 + class13_1.height) {
                    boolean flag = false;
                    if (class13_1.anInt242 != 0)
                        flag = method23(class13_1, 8);
                    if (!flag) {
                        rightClickOptions[anInt1183] = class13_1.tooltip;
                        anIntArray981[anInt1183] = 352;
                        anIntArray980[anInt1183] = class13_1.id;
                        anInt1183++;
                    }
                }
                if (class13_1.anInt289 == 2 && anInt1171 == 0 && i1 >= j2 && k1 >= k2 && i1 < j2 + class13_1.width
                        && k1 < k2 + class13_1.height) {
                    String s = class13_1.aString281;
                    if (s.indexOf(" ") != -1)
                        s = s.substring(0, s.indexOf(" "));
                    rightClickOptions[anInt1183] = s + " @gre@" + class13_1.aString211;
                    anIntArray981[anInt1183] = 70;
                    anIntArray980[anInt1183] = class13_1.id;
                    anInt1183++;
                }
                if (class13_1.anInt289 == 3 && i1 >= j2 && k1 >= k2 && i1 < j2 + class13_1.width
                        && k1 < k2 + class13_1.height) {
                    rightClickOptions[anInt1183] = "Close";
                    if (j == 3)
                        anIntArray981[anInt1183] = 55;
                    else
                        anIntArray981[anInt1183] = 639;
                    anIntArray980[anInt1183] = class13_1.id;
                    anInt1183++;
                }
                if (class13_1.anInt289 == 4 && i1 >= j2 && k1 >= k2 && i1 < j2 + class13_1.width
                        && k1 < k2 + class13_1.height) {
                    rightClickOptions[anInt1183] = class13_1.tooltip;
                    anIntArray981[anInt1183] = 890;
                    anIntArray980[anInt1183] = class13_1.id;
                    anInt1183++;
                }
                if (class13_1.anInt289 == 5 && i1 >= j2 && k1 >= k2 && i1 < j2 + class13_1.width
                        && k1 < k2 + class13_1.height) {
                    rightClickOptions[anInt1183] = class13_1.tooltip;
                    anIntArray981[anInt1183] = 518;
                    anIntArray980[anInt1183] = class13_1.id;
                    anInt1183++;
                }
                if (class13_1.anInt289 == 6 && !aBoolean1239 && i1 >= j2 && k1 >= k2 && i1 < j2 + class13_1.width
                        && k1 < k2 + class13_1.height) {
                    rightClickOptions[anInt1183] = class13_1.tooltip;
                    anIntArray981[anInt1183] = 575;
                    anIntArray980[anInt1183] = class13_1.id;
                    anInt1183++;
                }
                if (class13_1.type == 2) {
                    int l2 = 0;
                    for (int i3 = 0; i3 < class13_1.height; i3++) {
                        for (int j3 = 0; j3 < class13_1.width; j3++) {
                            int k3 = j2 + j3 * (32 + class13_1.anInt263);
                            int l3 = k2 + i3 * (32 + class13_1.anInt244);
                            if (l2 < 20) {
                                k3 += class13_1.anIntArray221[l2];
                                l3 += class13_1.anIntArray213[l2];
                            }
                            if (i1 >= k3 && k1 >= l3 && i1 < k3 + 32 && k1 < l3 + 32) {
                                anInt1063 = l2;
                                anInt1064 = class13_1.id;
                                if (class13_1.itemIds[l2] > 0) {
                                    ItemDefinition def = ItemDefinition.forId(class13_1.itemIds[l2] - 1);
                                    if (anInt1146 == 1 && class13_1.aBoolean229) {
                                        if (class13_1.id != itemInterfaceId || l2 != itemIndexId) {
                                            rightClickOptions[anInt1183] = "Use " + aString1150 + " with @lre@"
                                                    + def.name;
                                            anIntArray981[anInt1183] = 903;
                                            anIntArray982[anInt1183] = def.id;
                                            anIntArray979[anInt1183] = l2;
                                            anIntArray980[anInt1183] = class13_1.id;
                                            anInt1183++;
                                        }
                                    } else if (anInt1171 == 1 && class13_1.aBoolean229) {
                                        if ((anInt1173 & 0x10) == 16) {
                                            rightClickOptions[anInt1183] = aString1174 + " @lre@" + def.name;
                                            anIntArray981[anInt1183] = 361;
                                            anIntArray982[anInt1183] = def.id;
                                            anIntArray979[anInt1183] = l2;
                                            anIntArray980[anInt1183] = class13_1.id;
                                            anInt1183++;
                                        }
                                    } else {
                                        if (class13_1.aBoolean229) {
                                            for (int i4 = 4; i4 >= 3; i4--)
                                                if (def.inventoryActions != null
                                                        && def.inventoryActions[i4] != null) {
                                                    rightClickOptions[anInt1183] = def.inventoryActions[i4]
                                                            + " @lre@" + def.name;
                                                    if (i4 == 3)
                                                        anIntArray981[anInt1183] = 227;
                                                    if (i4 == 4)
                                                        anIntArray981[anInt1183] = 891;
                                                    anIntArray982[anInt1183] = def.id;
                                                    anIntArray979[anInt1183] = l2;
                                                    anIntArray980[anInt1183] = class13_1.id;
                                                    anInt1183++;
                                                } else if (i4 == 4) {
                                                    rightClickOptions[anInt1183] = "Drop @lre@" + def.name;
                                                    anIntArray981[anInt1183] = 891;
                                                    anIntArray982[anInt1183] = def.id;
                                                    anIntArray979[anInt1183] = l2;
                                                    anIntArray980[anInt1183] = class13_1.id;
                                                    anInt1183++;
                                                }

                                        }
                                        if (class13_1.aBoolean288) {
                                            rightClickOptions[anInt1183] = "Use @lre@" + def.name;
                                            anIntArray981[anInt1183] = 52;
                                            anIntArray982[anInt1183] = def.id;
                                            anIntArray979[anInt1183] = l2;
                                            anIntArray980[anInt1183] = class13_1.id;
                                            anInt1183++;
                                        }
                                        if (class13_1.aBoolean229 && def.inventoryActions != null) {
                                            for (int j4 = 2; j4 >= 0; j4--)
                                                if (def.inventoryActions[j4] != null) {
                                                    rightClickOptions[anInt1183] = def.inventoryActions[j4]
                                                            + " @lre@" + def.name;
                                                    if (j4 == 0)
                                                        anIntArray981[anInt1183] = 961;
                                                    if (j4 == 1)
                                                        anIntArray981[anInt1183] = 399;
                                                    if (j4 == 2)
                                                        anIntArray981[anInt1183] = 324;
                                                    anIntArray982[anInt1183] = def.id;
                                                    anIntArray979[anInt1183] = l2;
                                                    anIntArray980[anInt1183] = class13_1.id;
                                                    anInt1183++;
                                                }

                                        }
                                        if (class13_1.options != null) {
                                            for (int k4 = 4; k4 >= 0; k4--)
                                                if (class13_1.options[k4] != null) {
                                                    rightClickOptions[anInt1183] = class13_1.options[k4]
                                                            + " @lre@" + def.name;
                                                    if (k4 == 0)
                                                        anIntArray981[anInt1183] = 9;
                                                    if (k4 == 1)
                                                        anIntArray981[anInt1183] = 225;
                                                    if (k4 == 2)
                                                        anIntArray981[anInt1183] = 444;
                                                    if (k4 == 3)
                                                        anIntArray981[anInt1183] = 564;
                                                    if (k4 == 4)
                                                        anIntArray981[anInt1183] = 894;
                                                    anIntArray982[anInt1183] = def.id;
                                                    anIntArray979[anInt1183] = l2;
                                                    anIntArray980[anInt1183] = class13_1.id;
                                                    anInt1183++;
                                                }

                                        }
                                        rightClickOptions[anInt1183] = Constants.DEBUG ? "Examine @lre@" + def.name + " id(" + def.id + ")" : "Examine @lre@" + def.name;
                                        anIntArray981[anInt1183] = 1094;
                                        anIntArray982[anInt1183] = def.id;
                                        anIntArray979[anInt1183] = l2;
                                        anIntArray980[anInt1183] = class13_1.id;
                                        anInt1183++;
                                    }
                                }
                            }
                            l2++;
                        }

                    }

                }
            }
        }

    }

    public void updateNpcs() {
        for (int j = 0; j < localNpcCount; j++) {
            int k = anIntArray1134[j];
            Npc npc = npcs[k];
            if (npc != null) {
                method68(npc.def.aByte642, (byte) -97, npc);
            }
        }
    }

    public void method68(int i, byte byte0, Actor class50_sub1_sub4_sub3) {
        if (class50_sub1_sub4_sub3.unitX < 128 || class50_sub1_sub4_sub3.unitY < 128
                || class50_sub1_sub4_sub3.unitX >= 13184 || class50_sub1_sub4_sub3.unitY >= 13184) {
            class50_sub1_sub4_sub3.currentAnimation = -1;
            class50_sub1_sub4_sub3.anInt1614 = -1;
            class50_sub1_sub4_sub3.anInt1606 = 0;
            class50_sub1_sub4_sub3.anInt1607 = 0;
            class50_sub1_sub4_sub3.unitX = class50_sub1_sub4_sub3.walkingQueueX[0] * 128
                    + class50_sub1_sub4_sub3.anInt1601 * 64;
            class50_sub1_sub4_sub3.unitY = class50_sub1_sub4_sub3.walkingQueueY[0] * 128
                    + class50_sub1_sub4_sub3.anInt1601 * 64;
            class50_sub1_sub4_sub3.resetWalkingQueue();
        }
        if (class50_sub1_sub4_sub3 == thisPlayer
                && (class50_sub1_sub4_sub3.unitX < 1536 || class50_sub1_sub4_sub3.unitY < 1536
                || class50_sub1_sub4_sub3.unitX >= 11776 || class50_sub1_sub4_sub3.unitY >= 11776)) {
            class50_sub1_sub4_sub3.currentAnimation = -1;
            class50_sub1_sub4_sub3.anInt1614 = -1;
            class50_sub1_sub4_sub3.anInt1606 = 0;
            class50_sub1_sub4_sub3.anInt1607 = 0;
            class50_sub1_sub4_sub3.unitX = class50_sub1_sub4_sub3.walkingQueueX[0] * 128
                    + class50_sub1_sub4_sub3.anInt1601 * 64;
            class50_sub1_sub4_sub3.unitY = class50_sub1_sub4_sub3.walkingQueueY[0] * 128
                    + class50_sub1_sub4_sub3.anInt1601 * 64;
            class50_sub1_sub4_sub3.resetWalkingQueue();
        }
        if (class50_sub1_sub4_sub3.anInt1606 > pulseCycle)
            method69(class50_sub1_sub4_sub3, true);
        else if (class50_sub1_sub4_sub3.anInt1607 >= pulseCycle)
            method70(class50_sub1_sub4_sub3, -31135);
        else
            method71(class50_sub1_sub4_sub3, 0);
        method72((byte) 8, class50_sub1_sub4_sub3);
        method73(class50_sub1_sub4_sub3, -136);
        if (byte0 == -97)
            ;
    }

    public void method69(Actor class50_sub1_sub4_sub3, boolean flag) {
        if (!flag)
            aBoolean963 = !aBoolean963;
        int i = class50_sub1_sub4_sub3.anInt1606 - pulseCycle;
        int j = class50_sub1_sub4_sub3.anInt1602 * 128 + class50_sub1_sub4_sub3.anInt1601 * 64;
        int k = class50_sub1_sub4_sub3.anInt1604 * 128 + class50_sub1_sub4_sub3.anInt1601 * 64;
        class50_sub1_sub4_sub3.unitX += (j - class50_sub1_sub4_sub3.unitX) / i;
        class50_sub1_sub4_sub3.unitY += (k - class50_sub1_sub4_sub3.unitY) / i;
        class50_sub1_sub4_sub3.anInt1623 = 0;
        if (class50_sub1_sub4_sub3.anInt1608 == 0)
            class50_sub1_sub4_sub3.anInt1584 = 1024;
        if (class50_sub1_sub4_sub3.anInt1608 == 1)
            class50_sub1_sub4_sub3.anInt1584 = 1536;
        if (class50_sub1_sub4_sub3.anInt1608 == 2)
            class50_sub1_sub4_sub3.anInt1584 = 0;
        if (class50_sub1_sub4_sub3.anInt1608 == 3)
            class50_sub1_sub4_sub3.anInt1584 = 512;
    }

    public void method70(Actor class50_sub1_sub4_sub3, int i) {
        if (class50_sub1_sub4_sub3.anInt1607 == pulseCycle
                || class50_sub1_sub4_sub3.currentAnimation == -1
                || class50_sub1_sub4_sub3.animationDelay != 0
                || class50_sub1_sub4_sub3.animationFrameCycle + 1 > Animation.animations[class50_sub1_sub4_sub3.currentAnimation]
                .method205(0, class50_sub1_sub4_sub3.animationFrame)) {
            int j = class50_sub1_sub4_sub3.anInt1607 - class50_sub1_sub4_sub3.anInt1606;
            int k = pulseCycle - class50_sub1_sub4_sub3.anInt1606;
            int l = class50_sub1_sub4_sub3.anInt1602 * 128 + class50_sub1_sub4_sub3.anInt1601 * 64;
            int i1 = class50_sub1_sub4_sub3.anInt1604 * 128 + class50_sub1_sub4_sub3.anInt1601 * 64;
            int j1 = class50_sub1_sub4_sub3.anInt1603 * 128 + class50_sub1_sub4_sub3.anInt1601 * 64;
            int k1 = class50_sub1_sub4_sub3.anInt1605 * 128 + class50_sub1_sub4_sub3.anInt1601 * 64;
            class50_sub1_sub4_sub3.unitX = (l * (j - k) + j1 * k) / j;
            class50_sub1_sub4_sub3.unitY = (i1 * (j - k) + k1 * k) / j;
        }
        class50_sub1_sub4_sub3.anInt1623 = 0;
        if (class50_sub1_sub4_sub3.anInt1608 == 0)
            class50_sub1_sub4_sub3.anInt1584 = 1024;
        if (class50_sub1_sub4_sub3.anInt1608 == 1)
            class50_sub1_sub4_sub3.anInt1584 = 1536;
        if (class50_sub1_sub4_sub3.anInt1608 == 2)
            class50_sub1_sub4_sub3.anInt1584 = 0;
        if (class50_sub1_sub4_sub3.anInt1608 == 3)
            class50_sub1_sub4_sub3.anInt1584 = 512;
        class50_sub1_sub4_sub3.anInt1612 = class50_sub1_sub4_sub3.anInt1584;
        if (i == -31135)
            ;
    }

    public void method71(Actor class50_sub1_sub4_sub3, int i) {
        class50_sub1_sub4_sub3.anInt1588 = class50_sub1_sub4_sub3.anInt1634;
        if (class50_sub1_sub4_sub3.walkingQueueSize == 0) {
            class50_sub1_sub4_sub3.anInt1623 = 0;
            return;
        }
        if (class50_sub1_sub4_sub3.currentAnimation != -1 && class50_sub1_sub4_sub3.animationDelay == 0) {
            Animation class14 = Animation.animations[class50_sub1_sub4_sub3.currentAnimation];
            if (class50_sub1_sub4_sub3.anInt1613 > 0 && class14.anInt305 == 0) {
                class50_sub1_sub4_sub3.anInt1623++;
                return;
            }
            if (class50_sub1_sub4_sub3.anInt1613 <= 0 && class14.anInt306 == 0) {
                class50_sub1_sub4_sub3.anInt1623++;
                return;
            }
        }
        int j = class50_sub1_sub4_sub3.unitX;
        int k = class50_sub1_sub4_sub3.unitY;
        int l = class50_sub1_sub4_sub3.walkingQueueX[class50_sub1_sub4_sub3.walkingQueueSize - 1] * 128
                + class50_sub1_sub4_sub3.anInt1601 * 64;
        int i1 = class50_sub1_sub4_sub3.walkingQueueY[class50_sub1_sub4_sub3.walkingQueueSize - 1] * 128
                + class50_sub1_sub4_sub3.anInt1601 * 64;
        if (l - j > 256 || l - j < -256 || i1 - k > 256 || i1 - k < -256) {
            class50_sub1_sub4_sub3.unitX = l;
            class50_sub1_sub4_sub3.unitY = i1;
            return;
        }
        if (j < l) {
            if (k < i1)
                class50_sub1_sub4_sub3.anInt1584 = 1280;
            else if (k > i1)
                class50_sub1_sub4_sub3.anInt1584 = 1792;
            else
                class50_sub1_sub4_sub3.anInt1584 = 1536;
        } else if (j > l) {
            if (k < i1)
                class50_sub1_sub4_sub3.anInt1584 = 768;
            else if (k > i1)
                class50_sub1_sub4_sub3.anInt1584 = 256;
            else
                class50_sub1_sub4_sub3.anInt1584 = 512;
        } else if (k < i1)
            class50_sub1_sub4_sub3.anInt1584 = 1024;
        else
            class50_sub1_sub4_sub3.anInt1584 = 0;
        int j1 = class50_sub1_sub4_sub3.anInt1584 - class50_sub1_sub4_sub3.anInt1612 & 0x7ff;
        if (j1 > 1024)
            j1 -= 2048;
        int k1 = class50_sub1_sub4_sub3.anInt1620;
        if (i != 0)
            outBuffer.putByte(34);
        if (j1 >= -256 && j1 <= 256)
            k1 = class50_sub1_sub4_sub3.anInt1619;
        else if (j1 >= 256 && j1 < 768)
            k1 = class50_sub1_sub4_sub3.anInt1622;
        else if (j1 >= -768 && j1 <= -256)
            k1 = class50_sub1_sub4_sub3.anInt1621;
        if (k1 == -1)
            k1 = class50_sub1_sub4_sub3.anInt1619;
        class50_sub1_sub4_sub3.anInt1588 = k1;
        int l1 = 4;
        if (class50_sub1_sub4_sub3.anInt1612 != class50_sub1_sub4_sub3.anInt1584
                && class50_sub1_sub4_sub3.transformationId == -1 && class50_sub1_sub4_sub3.anInt1600 != 0)
            l1 = 2;
        if (class50_sub1_sub4_sub3.walkingQueueSize > 2)
            l1 = 6;
        if (class50_sub1_sub4_sub3.walkingQueueSize > 3)
            l1 = 8;
        if (class50_sub1_sub4_sub3.anInt1623 > 0 && class50_sub1_sub4_sub3.walkingQueueSize > 1) {
            l1 = 8;
            class50_sub1_sub4_sub3.anInt1623--;
        }
        if (class50_sub1_sub4_sub3.runningQueue[class50_sub1_sub4_sub3.walkingQueueSize - 1])
            l1 <<= 1;
        if (l1 >= 8 && class50_sub1_sub4_sub3.anInt1588 == class50_sub1_sub4_sub3.anInt1619
                && class50_sub1_sub4_sub3.anInt1629 != -1)
            class50_sub1_sub4_sub3.anInt1588 = class50_sub1_sub4_sub3.anInt1629;
        if (j < l) {
            class50_sub1_sub4_sub3.unitX += l1;
            if (class50_sub1_sub4_sub3.unitX > l)
                class50_sub1_sub4_sub3.unitX = l;
        } else if (j > l) {
            class50_sub1_sub4_sub3.unitX -= l1;
            if (class50_sub1_sub4_sub3.unitX < l)
                class50_sub1_sub4_sub3.unitX = l;
        }
        if (k < i1) {
            class50_sub1_sub4_sub3.unitY += l1;
            if (class50_sub1_sub4_sub3.unitY > i1)
                class50_sub1_sub4_sub3.unitY = i1;
        } else if (k > i1) {
            class50_sub1_sub4_sub3.unitY -= l1;
            if (class50_sub1_sub4_sub3.unitY < i1)
                class50_sub1_sub4_sub3.unitY = i1;
        }
        if (class50_sub1_sub4_sub3.unitX == l && class50_sub1_sub4_sub3.unitY == i1) {
            class50_sub1_sub4_sub3.walkingQueueSize--;
            if (class50_sub1_sub4_sub3.anInt1613 > 0)
                class50_sub1_sub4_sub3.anInt1613--;
        }
    }

    public void method72(byte byte0, Actor class50_sub1_sub4_sub3) {
        if (byte0 != 8)
            anInt928 = incomingRandom.nextInt();
        if (class50_sub1_sub4_sub3.anInt1600 == 0)
            return;
        if (class50_sub1_sub4_sub3.transformationId != -1 && class50_sub1_sub4_sub3.transformationId < 32768) {
            Npc class50_sub1_sub4_sub3_sub1 = npcs[class50_sub1_sub4_sub3.transformationId];
            if (class50_sub1_sub4_sub3_sub1 != null) {
                int l = class50_sub1_sub4_sub3.unitX - ((Actor) (class50_sub1_sub4_sub3_sub1)).unitX;
                int j1 = class50_sub1_sub4_sub3.unitY - ((Actor) (class50_sub1_sub4_sub3_sub1)).unitY;
                if (l != 0 || j1 != 0)
                    class50_sub1_sub4_sub3.anInt1584 = (int) (Math.atan2(l, j1) * 325.94900000000001D) & 0x7ff;
            }
        }
        if (class50_sub1_sub4_sub3.transformationId >= 32768) {
            int i = class50_sub1_sub4_sub3.transformationId - 32768;
            if (i == thisPlayerServerId)
                i = thisPlayerId;
            Player class50_sub1_sub4_sub3_sub2 = players[i];
            if (class50_sub1_sub4_sub3_sub2 != null) {
                int k1 = class50_sub1_sub4_sub3.unitX - ((Actor) (class50_sub1_sub4_sub3_sub2)).unitX;
                int l1 = class50_sub1_sub4_sub3.unitY - ((Actor) (class50_sub1_sub4_sub3_sub2)).unitY;
                if (k1 != 0 || l1 != 0)
                    class50_sub1_sub4_sub3.anInt1584 = (int) (Math.atan2(k1, l1) * 325.94900000000001D) & 0x7ff;
            }
        }
        if ((class50_sub1_sub4_sub3.nextStepX != 0 || class50_sub1_sub4_sub3.nextStepY != 0)
                && (class50_sub1_sub4_sub3.walkingQueueSize == 0 || class50_sub1_sub4_sub3.anInt1623 > 0)) {
            int j = class50_sub1_sub4_sub3.unitX - (class50_sub1_sub4_sub3.nextStepX - nextTopLeftTileX - nextTopLeftTileX) * 64;
            int i1 = class50_sub1_sub4_sub3.unitY - (class50_sub1_sub4_sub3.nextStepY - nextTopLeftTileY - nextTopLeftTileY) * 64;
            if (j != 0 || i1 != 0)
                class50_sub1_sub4_sub3.anInt1584 = (int) (Math.atan2(j, i1) * 325.94900000000001D) & 0x7ff;
            class50_sub1_sub4_sub3.nextStepX = 0;
            class50_sub1_sub4_sub3.nextStepY = 0;
        }
        int k = class50_sub1_sub4_sub3.anInt1584 - class50_sub1_sub4_sub3.anInt1612 & 0x7ff;
        if (k != 0) {
            if (k < class50_sub1_sub4_sub3.anInt1600 || k > 2048 - class50_sub1_sub4_sub3.anInt1600)
                class50_sub1_sub4_sub3.anInt1612 = class50_sub1_sub4_sub3.anInt1584;
            else if (k > 1024)
                class50_sub1_sub4_sub3.anInt1612 -= class50_sub1_sub4_sub3.anInt1600;
            else
                class50_sub1_sub4_sub3.anInt1612 += class50_sub1_sub4_sub3.anInt1600;
            class50_sub1_sub4_sub3.anInt1612 &= 0x7ff;
            if (class50_sub1_sub4_sub3.anInt1588 == class50_sub1_sub4_sub3.anInt1634
                    && class50_sub1_sub4_sub3.anInt1612 != class50_sub1_sub4_sub3.anInt1584) {
                if (class50_sub1_sub4_sub3.anInt1635 != -1) {
                    class50_sub1_sub4_sub3.anInt1588 = class50_sub1_sub4_sub3.anInt1635;
                    return;
                }
                class50_sub1_sub4_sub3.anInt1588 = class50_sub1_sub4_sub3.anInt1619;
            }
        }
    }

    public void method73(Actor class50_sub1_sub4_sub3, int i) {
        while (i >= 0)
            anInt1328 = incomingRandom.nextInt();
        class50_sub1_sub4_sub3.aBoolean1592 = false;
        if (class50_sub1_sub4_sub3.anInt1588 != -1) {
            Animation class14 = Animation.animations[class50_sub1_sub4_sub3.anInt1588];
            class50_sub1_sub4_sub3.anInt1590++;
            if (class50_sub1_sub4_sub3.anInt1589 < class14.anInt294
                    && class50_sub1_sub4_sub3.anInt1590 > class14.method205(0, class50_sub1_sub4_sub3.anInt1589)) {
                class50_sub1_sub4_sub3.anInt1590 = 1;
                class50_sub1_sub4_sub3.anInt1589++;
            }
            if (class50_sub1_sub4_sub3.anInt1589 >= class14.anInt294) {
                class50_sub1_sub4_sub3.anInt1590 = 1;
                class50_sub1_sub4_sub3.anInt1589 = 0;
            }
        }
        if (class50_sub1_sub4_sub3.anInt1614 != -1 && pulseCycle >= class50_sub1_sub4_sub3.anInt1617) {
            if (class50_sub1_sub4_sub3.anInt1615 < 0)
                class50_sub1_sub4_sub3.anInt1615 = 0;
            Animation class14_1 = SpotAnimation.spotAnimations[class50_sub1_sub4_sub3.anInt1614].animation;
            class50_sub1_sub4_sub3.anInt1616++;
            if (class50_sub1_sub4_sub3.anInt1615 < class14_1.anInt294
                    && class50_sub1_sub4_sub3.anInt1616 > class14_1.method205(0, class50_sub1_sub4_sub3.anInt1615)) {
                class50_sub1_sub4_sub3.anInt1616 = 1;
                class50_sub1_sub4_sub3.anInt1615++;
            }
            if (class50_sub1_sub4_sub3.anInt1615 >= class14_1.anInt294
                    && (class50_sub1_sub4_sub3.anInt1615 < 0 || class50_sub1_sub4_sub3.anInt1615 >= class14_1.anInt294))
                class50_sub1_sub4_sub3.anInt1614 = -1;
        }
        if (class50_sub1_sub4_sub3.currentAnimation != -1 && class50_sub1_sub4_sub3.animationDelay <= 1) {
            Animation class14_2 = Animation.animations[class50_sub1_sub4_sub3.currentAnimation];
            if (class14_2.anInt305 == 1 && class50_sub1_sub4_sub3.anInt1613 > 0
                    && class50_sub1_sub4_sub3.anInt1606 <= pulseCycle && class50_sub1_sub4_sub3.anInt1607 < pulseCycle) {
                class50_sub1_sub4_sub3.animationDelay = 1;
                return;
            }
        }
        if (class50_sub1_sub4_sub3.currentAnimation != -1 && class50_sub1_sub4_sub3.animationDelay == 0) {
            Animation class14_3 = Animation.animations[class50_sub1_sub4_sub3.currentAnimation];
            class50_sub1_sub4_sub3.animationFrameCycle++;
            if (class50_sub1_sub4_sub3.animationFrame < class14_3.anInt294
                    && class50_sub1_sub4_sub3.animationFrameCycle > class14_3.method205(0, class50_sub1_sub4_sub3.animationFrame)) {
                class50_sub1_sub4_sub3.animationFrameCycle = 1;
                class50_sub1_sub4_sub3.animationFrame++;
            }
            if (class50_sub1_sub4_sub3.animationFrame >= class14_3.anInt294) {
                class50_sub1_sub4_sub3.animationFrame -= class14_3.anInt298;
                class50_sub1_sub4_sub3.animationResetCycle++;
                if (class50_sub1_sub4_sub3.animationResetCycle >= class14_3.anInt304)
                    class50_sub1_sub4_sub3.currentAnimation = -1;
                if (class50_sub1_sub4_sub3.animationFrame < 0 || class50_sub1_sub4_sub3.animationFrame >= class14_3.anInt294)
                    class50_sub1_sub4_sub3.currentAnimation = -1;
            }
            class50_sub1_sub4_sub3.aBoolean1592 = class14_3.aBoolean300;
        }
        if (class50_sub1_sub4_sub3.animationDelay > 0)
            class50_sub1_sub4_sub3.animationDelay--;
    }

    public void drawGame() {
        // the login screen may have moved the origin to the centre of the window
        ((Graphics2D) super.graphics).setTransform(new AffineTransform());
        loginScreenCleared = false;
        if (anInt1053 != -1 && (loadingStage == 2 || super.imageProducer != null)) {
            if (loadingStage == 2) {
                updateInterfaceAnimations(anInt951, anInt1053);
                if (openInterfaceID != -1) {
                    updateInterfaceAnimations(anInt951, openInterfaceID);
                }
                anInt951 = 0;
                resetAllImageProducers();
                super.imageProducer.pushPixels();
                ThreeDimensionalCanvas.lineOffsets = clientEntireOffsets;
                Drawable.clearScreen();
                shouldRenderUI = true;
                JagInterface interface_1 = JagInterface.forId(anInt1053);
                if (interface_1.width == 512 && interface_1.height == 334 && interface_1.type == 0) {
                    interface_1.width = 765;
                    interface_1.height = 503;
                }
                drawInterface(layout.fullscreenInterfaceX, layout.fullscreenInterfaceY, interface_1, 0, 8);
                if (openInterfaceID != -1) {
                    JagInterface interface_2 = JagInterface.forId(openInterfaceID);
                    if (interface_2.width == 512 && interface_2.height == 334 && interface_2.type == 0) {
                        interface_2.width = 765;
                        interface_2.height = 503;
                    }
                    drawInterface(layout.fullscreenInterfaceX, layout.fullscreenInterfaceY, interface_2, 0, 8);
                }
                if (!isContextMenuActive) {
                    generateContextOptions(-521);
                    method34((byte) -79);
                } else {
                    drawContextMenu();
                }
            }
            super.imageProducer.drawImage(0, 0, super.graphics);
            return;
        }
        if (shouldRenderUI) {
            initUI();
            shouldRenderUI = false;
            if (!layout.resizable) {
                // the stone frame only exists in the fixed layout
                aClass18_906.drawImage(layout.frameLeftEdge, super.graphics);
                uiSideChatboxLeft.drawImage(layout.frameChatboxLeft, super.graphics);
                uiSideMinimapRight.drawImage(layout.frameMinimapRight, super.graphics);
                uiSideRockRight1.drawImage(layout.frameRockRight, super.graphics);
                aClass18_910.drawImage(layout.frameTopEdge, super.graphics);
                uiSideMinimapLeft.drawImage(layout.frameMinimapLeft, super.graphics);
                uiSideRockLeft1.drawImage(layout.frameRockLeft, super.graphics);
                uiSideChatboxRight.drawImage(layout.frameChatboxRight, super.graphics);
                uiSideChatboxTop.drawImage(layout.frameChatboxTop, super.graphics);
            }
            aBoolean1181 = true;
            aBoolean1240 = true;
            aBoolean950 = true;
            aBoolean1212 = true;
            if (loadingStage != 2) {
                gameViewportImage.drawImage(layout.viewport, super.graphics);
                drawPanel(aClass18_1157, layout.minimap, ClientLayout.MINIMAP_ALPHA);
            }
            anInt1237++;
            if (anInt1237 > 85) {
                anInt1237 = 0;
                outBuffer.putOpcode(168);
            }
        }
        if (loadingStage == 2) {
            drawGameViewport();
            if (clientSize == 1) {
                // in resizable mode the viewport covers the whole window and is redrawn every frame,
                // so the panels drawn on top of it have to be redrawn every frame too
                aBoolean1181 = true; // inventory
                aBoolean1240 = true; // chatbox
                aBoolean950 = true; // tab icons
                aBoolean1212 = true; // chat mode buttons
                drawResizableFrame();
            }
        }
        if (isContextMenuActive && anInt1304 == 1)
            aBoolean1181 = true;
        if (anInt1089 != -1) {
            boolean flag = updateInterfaceAnimations(anInt951, anInt1089);
            if (flag) {
                aBoolean1181 = true;
            }
        }
        if (anInt1332 == 2)
            aBoolean1181 = true;
        if (anInt1113 == 2)
            aBoolean1181 = true;
        if (aBoolean1181) {
            method134((byte) 7);
            aBoolean1181 = false;
        }
        if (anInt988 == -1 && chatboxInterfaceType == 0) {
            aClass13_1249.anInt231 = anInt1107 - anInt851 - 77;
            if (super.mouseX > 448 && super.mouseX < 560 && super.mouseY > 332 + layout.chatboxDy)
                method42(anInt1107, 0, aClass13_1249, (byte) 102, super.mouseY - layout.chatbox.y, -1, super.mouseX - layout.chatbox.x, 77, 463);
            int j = anInt1107 - 77 - aClass13_1249.anInt231;
            if (j < 0)
                j = 0;
            if (j > anInt1107 - 77)
                j = anInt1107 - 77;
            if (anInt851 != j) {
                anInt851 = j;
                aBoolean1240 = true;
            }
        }
        if (anInt988 == -1 && chatboxInterfaceType == 3) {
            int k = anInt862 * 14 + 7;
            aClass13_1249.anInt231 = anInt865;
            if (super.mouseX > 448 && super.mouseX < 560 && super.mouseY > 332 + layout.chatboxDy)
                method42(k, 0, aClass13_1249, (byte) 102, super.mouseY - layout.chatbox.y, -1, super.mouseX - layout.chatbox.x, 77, 463);
            int i1 = aClass13_1249.anInt231;
            if (i1 < 0)
                i1 = 0;
            if (i1 > k - 77)
                i1 = k - 77;
            if (anInt865 != i1) {
                anInt865 = i1;
                aBoolean1240 = true;
            }
        }
        if (anInt988 != -1) {
            boolean flag1 = updateInterfaceAnimations(anInt951, anInt988);
            if (flag1)
                aBoolean1240 = true;
        }
        if (anInt1332 == 3)
            aBoolean1240 = true;
        if (anInt1113 == 3)
            aBoolean1240 = true;
        if (aString1058 != null)
            aBoolean1240 = true;
        if (isContextMenuActive && anInt1304 == 2)
            aBoolean1240 = true;
        if (aBoolean1240) {
            drawChatbox();
            aBoolean1240 = false;
        }
        if (loadingStage == 2) {
            method87(503);
            drawPanel(aClass18_1157, layout.minimap, ClientLayout.MINIMAP_ALPHA);
        }
        if (anInt1213 != -1)
            aBoolean950 = true;
        if (aBoolean950) {
            if (anInt1213 != -1 && anInt1213 == tabId) {
                anInt1213 = -1;
                outBuffer.putOpcode(119);
                outBuffer.putByte(tabId);
            }
            aBoolean950 = false;
            aClass18_1110.pushPixels();
            aClass50_Sub1_Sub1_Sub3_967.drawSprite(0, 0);
            if (anInt1089 == -1) {
                if (anIntArray1081[tabId] != -1) {
                    if (tabId == 0)
                        aClass50_Sub1_Sub1_Sub3_880.drawSprite(22, 10);
                    if (tabId == 1)
                        aClass50_Sub1_Sub1_Sub3_881.drawSprite(54, 8);
                    if (tabId == 2)
                        aClass50_Sub1_Sub1_Sub3_881.drawSprite(82, 8);
                    if (tabId == 3)
                        aClass50_Sub1_Sub1_Sub3_882.drawSprite(110, 8);
                    if (tabId == 4)
                        aClass50_Sub1_Sub1_Sub3_884.drawSprite(153, 8);
                    if (tabId == 5)
                        aClass50_Sub1_Sub1_Sub3_884.drawSprite(181, 8);
                    if (tabId == 6)
                        aClass50_Sub1_Sub1_Sub3_883.drawSprite(209, 9);
                }
                if (anIntArray1081[0] != -1 && (anInt1213 != 0 || pulseCycle % 20 < 10))
                    aClass50_Sub1_Sub1_Sub3Array976[0].drawSprite(29, 13);
                if (anIntArray1081[1] != -1 && (anInt1213 != 1 || pulseCycle % 20 < 10))
                    aClass50_Sub1_Sub1_Sub3Array976[1].drawSprite(53, 11);
                if (anIntArray1081[2] != -1 && (anInt1213 != 2 || pulseCycle % 20 < 10))
                    aClass50_Sub1_Sub1_Sub3Array976[2].drawSprite(82, 11);
                if (anIntArray1081[3] != -1 && (anInt1213 != 3 || pulseCycle % 20 < 10))
                    aClass50_Sub1_Sub1_Sub3Array976[3].drawSprite(115, 12);
                if (anIntArray1081[4] != -1 && (anInt1213 != 4 || pulseCycle % 20 < 10))
                    aClass50_Sub1_Sub1_Sub3Array976[4].drawSprite(153, 13);
                if (anIntArray1081[5] != -1 && (anInt1213 != 5 || pulseCycle % 20 < 10))
                    aClass50_Sub1_Sub1_Sub3Array976[5].drawSprite(180, 11);
                if (anIntArray1081[6] != -1 && (anInt1213 != 6 || pulseCycle % 20 < 10))
                    aClass50_Sub1_Sub1_Sub3Array976[6].drawSprite(208, 13);
            }
            drawPanel(aClass18_1110, layout.tabIconsTop, ClientLayout.INVENTORY_ALPHA);
            aClass18_1109.pushPixels();
            aClass50_Sub1_Sub1_Sub3_966.drawSprite(0, 0);
            if (anInt1089 == -1) {
                if (anIntArray1081[tabId] != -1) {
                    if (tabId == 7)
                        aClass50_Sub1_Sub1_Sub3_983.drawSprite(42, 0);
                    if (tabId == 8)
                        aClass50_Sub1_Sub1_Sub3_984.drawSprite(74, 0);
                    if (tabId == 9)
                        aClass50_Sub1_Sub1_Sub3_984.drawSprite(102, 0);
                    if (tabId == 10)
                        aClass50_Sub1_Sub1_Sub3_985.drawSprite(130, 1);
                    if (tabId == 11)
                        aClass50_Sub1_Sub1_Sub3_987.drawSprite(173, 0);
                    if (tabId == 12)
                        aClass50_Sub1_Sub1_Sub3_987.drawSprite(201, 0);
                    if (tabId == 13)
                        aClass50_Sub1_Sub1_Sub3_986.drawSprite(229, 0);
                }
                if (anIntArray1081[8] != -1 && (anInt1213 != 8 || pulseCycle % 20 < 10))
                    aClass50_Sub1_Sub1_Sub3Array976[7].drawSprite(74, 2);
                if (anIntArray1081[9] != -1 && (anInt1213 != 9 || pulseCycle % 20 < 10))
                    aClass50_Sub1_Sub1_Sub3Array976[8].drawSprite(102, 3);
                if (anIntArray1081[10] != -1 && (anInt1213 != 10 || pulseCycle % 20 < 10))
                    aClass50_Sub1_Sub1_Sub3Array976[9].drawSprite(137, 4);
                if (anIntArray1081[11] != -1 && (anInt1213 != 11 || pulseCycle % 20 < 10))
                    aClass50_Sub1_Sub1_Sub3Array976[10].drawSprite(174, 2);
                if (anIntArray1081[12] != -1 && (anInt1213 != 12 || pulseCycle % 20 < 10))
                    aClass50_Sub1_Sub1_Sub3Array976[11].drawSprite(201, 2);
                if (anIntArray1081[13] != -1 && (anInt1213 != 13 || pulseCycle % 20 < 10))
                    aClass50_Sub1_Sub1_Sub3Array976[12].drawSprite(226, 2);
            }
            if (layout.resizable) {
                // the left part of the image is the chatbox corner, the rest is the inventory block
                setPanelAlpha(ClientLayout.INVENTORY_ALPHA);
                aClass18_1109.drawImageRegion(layout.tabIconsBottom.x, layout.tabIconsBottom.y,
                        layout.tabIconsBottomCrop, 0, layout.tabIconsBottom.width, layout.tabIconsBottom.height, super.graphics);
                setPanelAlpha(ClientLayout.CHATBOX_ALPHA);
                aClass18_1109.drawImageRegion(layout.frameChatboxRight.x, layout.tabIconsBottom.y, 0, 0,
                        ClientLayout.CHAT_CORNER_WIDTH, layout.tabIconsBottom.height, super.graphics);
                setPanelAlpha(ClientLayout.OPAQUE);
            } else {
                aClass18_1109.drawImage(layout.tabIconsBottom, super.graphics);
            }
            gameViewportImage.pushPixels();
            ThreeDimensionalCanvas.lineOffsets = gameViewportOffsets;
        }
        if (aBoolean1212) {
            aBoolean1212 = false;
            chatboxButtons.pushPixels();
            aClass50_Sub1_Sub1_Sub3_965.drawSprite(0, 0);
            fontChatboxButtons.drawString("Public chat", 55, 28, true, 0xffffff);
            if (publicChatMode == 0)
                fontChatboxButtons.drawString("On", 55, 41, true, 65280);
            if (publicChatMode == 1)
                fontChatboxButtons.drawString("Friends", 55, 41, true, 0xffff00);
            if (publicChatMode == 2)
                fontChatboxButtons.drawString("Off", 55, 41, true, 0xff0000);
            if (publicChatMode == 3)
                fontChatboxButtons.drawString("Hide", 55, 41, true, 65535);
            fontChatboxButtons.drawString("Private chat", 184, 28, true, 0xffffff);
            if (privateChatMode == 0)
                fontChatboxButtons.drawString("On", 184, 41, true, 65280);
            if (privateChatMode == 1)
                fontChatboxButtons.drawString("Friends", 184, 41, true, 0xffff00);
            if (privateChatMode == 2)
                fontChatboxButtons.drawString("Off", 184, 41, true, 0xff0000);
            fontChatboxButtons.drawString("Trade/compete", 324, 28, true, 0xffffff);
            if (tradeMode == 0)
                fontChatboxButtons.drawString("On", 324, 41, true, 65280);
            if (tradeMode == 1)
                fontChatboxButtons.drawString("Friends", 324, 41, true, 0xffff00);
            if (tradeMode == 2)
                fontChatboxButtons.drawString("Off", 324, 41, true, 0xff0000);
            fontChatboxButtons.drawString("Report abuse", 458, 33, true, 0xffffff);
            drawPanel(chatboxButtons, layout.chatButtons, ClientLayout.CHATBOX_ALPHA);
            gameViewportImage.pushPixels();
            ThreeDimensionalCanvas.lineOffsets = gameViewportOffsets;
        }
        anInt951 = 0;
    }

    /**
     * Draws the stone frame around the chatbox block and the inventory block in the resizable layout, below the
     * panels. In the fixed layout the frame is made of pieces that cross from one block into the other (for example
     * the strip above the chatbox is also the top of the inventory's left side), so the pieces are cut in two here
     * to give each block a rectangle of its own.
     */
    private void drawResizableFrame() {
        Graphics g = super.graphics;
        int cornerWidth = ClientLayout.CHAT_CORNER_WIDTH;
        int inventoryLeft = layout.frameRockLeft.x; // the left side of the inventory block, 516 + dx
        int chatboxFrameWidth = inventoryLeft - layout.inventoryDx; // 516, the width of the chatbox block

        // chatbox block: top strip, left side and the right side
        setPanelAlpha(ClientLayout.CHATBOX_ALPHA);
        uiSideChatboxTop.drawImageRegion(layout.frameChatboxTop.x, layout.frameChatboxTop.y, 0, 0, chatboxFrameWidth, uiSideChatboxTop.height, g);
        uiSideChatboxLeft.drawImage(layout.frameChatboxLeft, g);
        uiSideChatboxRight.drawImageRegion(layout.frameChatboxRight.x, layout.frameChatboxRight.y, 0, 0, cornerWidth, uiSideChatboxRight.height, g);

        // inventory block: left side (three pieces, the lower two are cut from the chatbox pieces) and right side
        setPanelAlpha(ClientLayout.INVENTORY_ALPHA);
        uiSideRockLeft1.drawImage(layout.frameRockLeft, g);
        uiSideChatboxTop.drawImageRegion(inventoryLeft, layout.frameChatboxTop.y, chatboxFrameWidth, 0,
                uiSideChatboxTop.width - chatboxFrameWidth, uiSideChatboxTop.height, g);
        uiSideChatboxRight.drawImageRegion(inventoryLeft, layout.frameChatboxRight.y, cornerWidth, 0,
                uiSideChatboxRight.width - cornerWidth, uiSideChatboxRight.height, g);
        uiSideRockRight1.drawImage(layout.frameRockRight, g);
        setPanelAlpha(ClientLayout.OPAQUE);
    }

    /**
     * Draws a panel at its place in the layout. In the resizable layout the panel is drawn see-through by the
     * given alpha, so the game view shows through. The fixed layout always draws opaque.
     */
    private void drawPanel(JagImageProducer panel, Rectangle area, int alpha) {
        setPanelAlpha(alpha);
        panel.drawImage(area, super.graphics);
        setPanelAlpha(ClientLayout.OPAQUE);
    }

    /**
     * Sets how opaque the next drawing to the screen is, from 0 (invisible) to 256 (opaque). Ignored in the fixed layout.
     */
    private void setPanelAlpha(int alpha) {
        if (!layout.resizable) {
            return;
        }
        Graphics2D g = (Graphics2D) super.graphics;
        if (alpha >= ClientLayout.OPAQUE) {
            g.setComposite(AlphaComposite.SrcOver);
        } else {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha) / (float) ClientLayout.OPAQUE));
        }
    }

    public void drawPrivateChat() {
        if (splitPrivateChat == 0) {
            return;
        }
        JagFont class50_sub1_sub1_sub2 = fontChatboxButtons;
        int j = 0;
        if (anInt1057 != 0)
            j = 1;
        for (int k = 0; k < 100; k++)
            if (aStringArray1298[k] != null) {
                int l = anIntArray1296[k];
                String s = aStringArray1297[k];
                byte byte0 = 0;
                if (s != null && s.startsWith("@cr1@")) {
                    s = s.substring(5);
                    byte0 = 1;
                }
                if (s != null && s.startsWith("@cr2@")) {
                    s = s.substring(5);
                    byte0 = 2;
                }
                if ((l == 3 || l == 7) && (l == 7 || privateChatMode == 0 || privateChatMode == 1 && method148(13292, s))) {
                    int i1 = layout.privateChatY - j * 13;
                    int l1 = 4;
                    class50_sub1_sub1_sub2.drawString_474("From", 2245, l1, 0, i1);
                    class50_sub1_sub1_sub2.drawString_474("From", 2245, l1, 65535, i1 - 1);
                    l1 += class50_sub1_sub1_sub2.method472((byte) 35, "From ");
                    if (byte0 == 1) {
                        aClass50_Sub1_Sub1_Sub3Array1142[0].drawSprite(l1, i1 - 12);
                        l1 += 14;
                    }
                    if (byte0 == 2) {
                        aClass50_Sub1_Sub1_Sub3Array1142[1].drawSprite(l1, i1 - 12);
                        l1 += 14;
                    }
                    class50_sub1_sub1_sub2.drawString_474(s + ": " + aStringArray1298[k], 2245, l1, 0, i1);
                    class50_sub1_sub1_sub2.drawString_474(s + ": " + aStringArray1298[k], 2245, l1, 65535, i1 - 1);
                    if (++j >= 5)
                        return;
                }
                if (l == 5 && privateChatMode < 2) {
                    int j1 = layout.privateChatY - j * 13;
                    class50_sub1_sub1_sub2.drawString_474(aStringArray1298[k], 2245, 4, 0, j1);
                    class50_sub1_sub1_sub2.drawString_474(aStringArray1298[k], 2245, 4, 65535, j1 - 1);
                    if (++j >= 5)
                        return;
                }
                if (l == 6 && privateChatMode < 2) {
                    int k1 = layout.privateChatY - j * 13;
                    class50_sub1_sub1_sub2.drawString_474("To " + s + ": " + aStringArray1298[k], 2245, 4, 0, k1);
                    class50_sub1_sub1_sub2.drawString_474("To " + s + ": " + aStringArray1298[k], 2245, 4, 65535, k1 - 1);
                    if (++j >= 5)
                        return;
                }
            }

    }

    public void init() {
        world = Integer.parseInt(getParameter("nodeid"));
        portOffset = Integer.parseInt(getParameter("portoff"));
        switchToHighMem();
        memberServer = true;
        start(765, 503);
    }

    public void updateSpotAnimations() {
        for (Graphic graphic = (Graphic) aClass6_1210.first(); graphic != null; graphic = (Graphic) aClass6_1210
                .next())
            if (graphic.anInt1731 != plane || graphic.aBoolean1736)
                graphic.unlink();
            else if (pulseCycle >= graphic.anInt1740) {
                graphic.method604((byte) 1, anInt951);
                if (graphic.aBoolean1736)
                    graphic.unlink();
                else
                    sceneGraph.method252(-1, graphic, graphic.anInt1732,
                            graphic.anInt1734, false, 0, graphic.anInt1731, 60,
                            graphic.anInt1733, 0);
            }

    }

    public void method77(boolean flag) {
        if (flag)
            opcode = -1;
        do {
            FileNode class50_sub1_sub3;
            do {
                class50_sub1_sub3 = fileFetcher.method330();
                if (class50_sub1_sub3 == null)
                    return;
                if (class50_sub1_sub3.type == 0) {
                    Model.unpackModelHeader(class50_sub1_sub3.buf, class50_sub1_sub3.id, (byte) 7);
                    if ((fileFetcher.method325(class50_sub1_sub3.id, -493) & 0x62) != 0) {
                        aBoolean1181 = true;
                        if (anInt988 != -1 || anInt1191 != -1)
                            aBoolean1240 = true;
                    }
                }
                if (class50_sub1_sub3.type == 1 && class50_sub1_sub3.buf != null)
                    AnimationFrame.unpackFrames(class50_sub1_sub3.buf, true);
                if (class50_sub1_sub3.type == 2 && class50_sub1_sub3.id == anInt1270 && class50_sub1_sub3.buf != null)
                    method24(aBoolean1271, class50_sub1_sub3.buf, 659);
                if (class50_sub1_sub3.type == 3 && loadingStage == 1) {
                    for (int i = 0; i < aByteArrayArray838.length; i++) {
                        if (anIntArray857[i] == class50_sub1_sub3.id) {
                            aByteArrayArray838[i] = class50_sub1_sub3.buf;
                            if (class50_sub1_sub3.buf == null)
                                anIntArray857[i] = -1;
                            break;
                        }
                        if (anIntArray858[i] != class50_sub1_sub3.id)
                            continue;
                        aByteArrayArray1232[i] = class50_sub1_sub3.buf;
                        if (class50_sub1_sub3.buf == null)
                            anIntArray858[i] = -1;
                        break;
                    }

                }
            } while (class50_sub1_sub3.type != 93 || !fileFetcher.method334(class50_sub1_sub3.id, false));
            Region.method169(fileFetcher, new JagBuffer(class50_sub1_sub3.buf), (byte) -3);
        } while (true);
    }

    public boolean method78(int i) {
        if (i <= 0) {
            for (int j = 1; j > 0; j++) ;
        }
        return signlink.wavereplay();
    }

    public void login(String username, String password, boolean reconnecting) {
        signlink.errorname = username;
        try {
            if (!reconnecting) {
                statusLineOne = "";
                statusLineTwo = "Connecting to server...";
                drawLoginScreen(true);
            }
            connection = new JagSocket((byte) 2, openSocket(43594 + portOffset), this);
            long base37name = StringUtils.encodeBase37(username);
            int hash = (int) (base37name >> 16 & 31L);
            outBuffer.position = 0;
            outBuffer.putByte(14);
            outBuffer.putByte(hash);
            connection.putBytes(0, 2, 0, outBuffer.buffer);
            for (int j = 0; j < 8; j++)
                connection.getByte();

            int returnCode = connection.getByte();
            int i1 = returnCode;
            if (returnCode == 0) {
                connection.getBytes(buffer.buffer, 0, 8);
                buffer.position = 0;
                serverSeed = buffer.getLong();
                int seed[] = new int[4];
                seed[0] = (int) (Math.random() * 99999999D);
                seed[1] = (int) (Math.random() * 99999999D);
                seed[2] = (int) (serverSeed >> 32);
                seed[3] = (int) serverSeed;
                outBuffer.position = 0;
                outBuffer.putByte(10);
                outBuffer.putInt(seed[0]);
                outBuffer.putInt(seed[1]);
                outBuffer.putInt(seed[2]);
                outBuffer.putInt(seed[3]);
                outBuffer.putInt(signlink.uid);
                outBuffer.putString(username);
                outBuffer.putString(password);
                outBuffer.rsa(JAGEX_MODULUS, JAGEX_PUBLIC_KEY);
                tempBuffer.position = 0;
                if (reconnecting)
                    tempBuffer.putByte(18);
                else
                    tempBuffer.putByte(16);
                tempBuffer.putByte(outBuffer.position + 36 + 1 + 1 + 2);
                tempBuffer.putByte(255);
                tempBuffer.putShort(377);
                tempBuffer.putByte(lowMemory ? 1 : 0);
                for (int i = 0; i < 9; i++)
                    tempBuffer.putInt(archiveHashes[i]);

                tempBuffer.putBytes(outBuffer.buffer, 0, outBuffer.position);
                outBuffer.random = new IsaacRandom(seed);
                for (int i = 0; i < 4; i++)
                    seed[i] += 50;

                incomingRandom = new IsaacRandom(seed);
                connection.putBytes(0, tempBuffer.position, 0, tempBuffer.buffer);
                returnCode = connection.getByte();
            }
            if (returnCode == 1) {
                try {
                    Thread.sleep(2000L);
                } catch (Exception _ex) {
                }
                login(username, password, reconnecting);
                return;
            }
            if (returnCode == 2) {
                stopMidi();
                playerRights = connection.getByte();
                accountFlagged = connection.getByte() == 1;
                aLong902 = 0L;
                anInt1299 = 0;
                mouseRecorder.pos = 0;
                super.awtFocus = true;
                aBoolean1275 = true;
                isLoggedIn = true;
                outBuffer.position = 0;
                buffer.position = 0;
                opcode = -1;
                anInt903 = -1;
                lastOpcode = -1;
                anInt905 = -1;
                size = 0;
                anInt871 = 0;
                anInt1057 = 0;
                anInt873 = 0;
                anInt1197 = 0;
                anInt1183 = 0;
                isContextMenuActive = false;
                super.anInt20 = 0;
                for (int j1 = 0; j1 < 100; j1++)
                    aStringArray1298[j1] = null;

                anInt1146 = 0;
                anInt1171 = 0;
                loadingStage = 0;
                anInt1035 = 0;
                anInt853 = (int) (Math.random() * 100D) - 50;
                anInt1009 = (int) (Math.random() * 110D) - 55;
                anInt1255 = (int) (Math.random() * 80D) - 40;
                anInt916 = (int) (Math.random() * 120D) - 60;
                anInt1233 = (int) (Math.random() * 30D) - 20;
                anInt1252 = (int) (Math.random() * 20D) - 10 & 0x7ff;
                minimapState = 0;
                anInt1276 = -1;
                anInt1120 = 0;
                anInt1121 = 0;
                localPlayerCount = 0;
                localNpcCount = 0;
                for (int i2 = 0; i2 < anInt968; i2++) {
                    players[i2] = null;
                    cachedAppearances[i2] = null;
                }

                for (int k2 = 0; k2 < 16384; k2++)
                    npcs[k2] = null;

                thisPlayer = players[thisPlayerId] = new Player();
                projectileQueue.clear();
                aClass6_1210.clear();
                for (int l2 = 0; l2 < 4; l2++) {
                    for (int i3 = 0; i3 < 104; i3++) {
                        for (int k3 = 0; k3 < 104; k3++)
                            groundItems[l2][i3][k3] = null;

                    }

                }

                gameObjectSpawnsRequestList = new LinkedList();
                anInt860 = 0;
                friendsCount = 0;
                method44(anInt1191);
                anInt1191 = -1;
                method44(anInt988);
                anInt988 = -1;
                method44(anInt1169);
                anInt1169 = -1;
                method44(anInt1053);
                anInt1053 = -1;
                method44(openInterfaceID);
                openInterfaceID = -1;
                method44(anInt1089);
                anInt1089 = -1;
                method44(walkableInterfaceId);
                walkableInterfaceId = -1;
                aBoolean1239 = false;
                tabId = 3;
                chatboxInterfaceType = 0;
                isContextMenuActive = false;
                aBoolean866 = false;
                aString1058 = null;
                anInt1319 = 0;
                anInt1213 = -1;
                aBoolean1144 = true;
                method25(anInt1015);
                for (int j3 = 0; j3 < 5; j3++)
                    anIntArray1099[j3] = 0;

                for (int l3 = 0; l3 < 5; l3++) {
                    aStringArray1069[l3] = null;
                    aBooleanArray1070[l3] = false;
                }

                anInt1100 = 0;
                anInt1165 = 0;
                anInt1235 = 0;
                anInt1052 = 0;
                anInt1139 = 0;
                anInt841 = 0;
                anInt1230 = 0;
                anInt1013 = 0;
                anInt1049 = 0;
                anInt1162 = 0;
                initUI();
                return;
            }
            if (returnCode == 3) {
                statusLineOne = "";
                statusLineTwo = "Invalid username or password.";
                return;
            }
            if (returnCode == 4) {
                statusLineOne = "Your account has been disabled.";
                statusLineTwo = "Please check your message-centre for details.";
                return;
            }
            if (returnCode == 5) {
                statusLineOne = "Your account is already logged in.";
                statusLineTwo = "Try again in 60 secs...";
                return;
            }
            if (returnCode == 6) {
                statusLineOne = "RuneScape has been updated!";
                statusLineTwo = "Please reload this page.";
                return;
            }
            if (returnCode == 7) {
                statusLineOne = "This world is full.";
                statusLineTwo = "Please use a different world.";
                return;
            }
            if (returnCode == 8) {
                statusLineOne = "Unable to connect.";
                statusLineTwo = "Login server offline.";
                return;
            }
            if (returnCode == 9) {
                statusLineOne = "Login limit exceeded.";
                statusLineTwo = "Too many connections from your address.";
                return;
            }
            if (returnCode == 10) {
                statusLineOne = "Unable to connect.";
                statusLineTwo = "Bad session id.";
                return;
            }
            if (returnCode == 12) {
                statusLineOne = "You need a members account to login to this world.";
                statusLineTwo = "Please subscribe, or use a different world.";
                return;
            }
            if (returnCode == 13) {
                statusLineOne = "Could not complete login.";
                statusLineTwo = "Please try using a different world.";
                return;
            }
            if (returnCode == 14) {
                statusLineOne = "The server is being updated.";
                statusLineTwo = "Please wait 1 minute and try again.";
                return;
            }
            if (returnCode == 15) {
                isLoggedIn = true;
                outBuffer.position = 0;
                buffer.position = 0;
                opcode = -1;
                anInt903 = -1;
                lastOpcode = -1;
                anInt905 = -1;
                size = 0;
                anInt871 = 0;
                anInt1057 = 0;
                anInt1183 = 0;
                isContextMenuActive = false;
                aLong1229 = System.currentTimeMillis();
                return;
            }
            if (returnCode == 16) {
                statusLineOne = "Login attempts exceeded.";
                statusLineTwo = "Please wait 1 minute and try again.";
                return;
            }
            if (returnCode == 17) {
                statusLineOne = "You are standing in a members-only area.";
                statusLineTwo = "To play on this world move to a free area first";
                return;
            }
            if (returnCode == 18) {
                statusLineOne = "Account locked as we suspect it has been stolen.";
                statusLineTwo = "Press 'recover a locked account' on front page.";
                return;
            }
            if (returnCode == 20) {
                statusLineOne = "Invalid loginserver requested";
                statusLineTwo = "Please try using a different world.";
                return;
            }
            if (returnCode == 21) {
                int k1 = connection.getByte();
                for (k1 += 3; k1 >= 0; k1--) {
                    statusLineOne = "You have only just left another world";
                    statusLineTwo = "Your profile will be transferred in: " + k1;
                    drawLoginScreen(true);
                    try {
                        Thread.sleep(1200L);
                    } catch (Exception _ex) {
                    }
                }

                login(username, password, reconnecting);
                return;
            }
            if (returnCode == 22) {
                statusLineOne = "Malformed login packet.";
                statusLineTwo = "Please try again.";
                return;
            }
            if (returnCode == 23) {
                statusLineOne = "No reply from loginserver.";
                statusLineTwo = "Please try again.";
                return;
            }
            if (returnCode == 24) {
                statusLineOne = "Error loading your profile.";
                statusLineTwo = "Please contact customer support.";
                return;
            }
            if (returnCode == 25) {
                statusLineOne = "Unexpected loginserver response.";
                statusLineTwo = "Please try using a different world.";
                return;
            }
            if (returnCode == 26) {
                statusLineOne = "This computers address has been blocked";
                statusLineTwo = "as it was used to break our rules";
                return;
            }
            if (returnCode == -1) {
                if (i1 == 0) {
                    if (anInt850 < 2) {
                        try {
                            Thread.sleep(2000L);
                        } catch (Exception _ex) {
                        }
                        anInt850++;
                        login(username, password, reconnecting);
                        return;
                    } else {
                        statusLineOne = "No response from loginserver";
                        statusLineTwo = "Please wait 1 minute and try again.";
                        return;
                    }
                } else {
                    statusLineOne = "No response from server";
                    statusLineTwo = "Please try using a different world.";
                    return;
                }
            } else {
                System.out.println("response:" + returnCode);
                statusLineOne = "Unexpected server response";
                statusLineTwo = "Please try using a different world.";
                return;
            }
        } catch (IOException _ex) {
            statusLineOne = "";
        }
        statusLineTwo = "Error connecting to server.";
    }

    public boolean method80(int dstY, int j, int dstX, int l) {
        int i1 = l >> 14 & 0x7fff;
        int j1 = sceneGraph.method271(plane, dstX, dstY, l);
        if (j1 == -1)
            return false;
        int objectType = j1 & 0x1f;
        int l1 = j1 >> 6 & 3;
        if (objectType == 10 || objectType == 11 || objectType == 22) {
            ObjectDefinition class47 = ObjectDefinition.forId(i1);
            int i2;
            int j2;
            if (l1 == 0 || l1 == 2) {
                i2 = class47.anInt801;
                j2 = class47.anInt775;
            } else {
                i2 = class47.anInt775;
                j2 = class47.anInt801;
            }
            int k2 = class47.anInt764;
            if (l1 != 0)
                k2 = (k2 << l1 & 0xf) + (k2 >> 4 - l1);
            walk(true, false, dstY, ((Actor) (thisPlayer)).walkingQueueY[0], i2, j2, 2, 0, dstX, k2, 0,
                    ((Actor) (thisPlayer)).walkingQueueX[0]);
        } else {
            walk(true, false, dstY, ((Actor) (thisPlayer)).walkingQueueY[0], 0, 0, 2, objectType + 1, dstX, 0, l1,
                    ((Actor) (thisPlayer)).walkingQueueX[0]);
        }
        anInt1020 = super.anInt29;
        anInt1021 = super.anInt30;
        anInt1023 = 2;
        anInt1022 = 0;
        size += j;
        return true;
    }

    public void updateLoginFlames(byte byte0) {
        char c = '\u0100';
        for (int i = 10; i < 117; i++) {
            int j = (int) (Math.random() * 100D);
            if (j < 50)
                anIntArray1084[i + (c - 2 << 7)] = 255;
        }

        for (int k = 0; k < 100; k++) {
            int l = (int) (Math.random() * 124D) + 2;
            int j1 = (int) (Math.random() * 128D) + 128;
            int j2 = l + (j1 << 7);
            anIntArray1084[j2] = 192;
        }

        for (int i1 = 1; i1 < c - 1; i1++) {
            for (int k1 = 1; k1 < 127; k1++) {
                int k2 = k1 + (i1 << 7);
                anIntArray1085[k2] = (anIntArray1084[k2 - 1] + anIntArray1084[k2 + 1] + anIntArray1084[k2 - 128] + anIntArray1084[k2 + 128]) / 4;
            }

        }

        anInt1238 += 128;
        if (anInt1238 > anIntArray1176.length) {
            anInt1238 -= anIntArray1176.length;
            int l1 = (int) (Math.random() * 12D);
            method83(runes_array1117[l1], 0);
        }
        for (int i2 = 1; i2 < c - 1; i2++) {
            for (int l2 = 1; l2 < 127; l2++) {
                int k3 = l2 + (i2 << 7);
                int i4 = anIntArray1085[k3 + 128] - anIntArray1176[k3 + anInt1238 & anIntArray1176.length - 1] / 5;
                if (i4 < 0)
                    i4 = 0;
                anIntArray1084[k3] = i4;
            }

        }

        if (byte0 == 1) {
            byte0 = 0;
        } else {
            for (int i3 = 1; i3 > 0; i3++) ;
        }
        for (int j3 = 0; j3 < c - 1; j3++)
            anIntArray1166[j3] = anIntArray1166[j3 + 1];

        anIntArray1166[c - 1] = (int) (Math.sin((double) pulseCycle / 14D) * 16D + Math.sin((double) pulseCycle / 15D)
                * 14D + Math.sin((double) pulseCycle / 16D) * 12D);
        if (anInt1047 > 0)
            anInt1047 -= 4;
        if (anInt1048 > 0)
            anInt1048 -= 4;
        if (anInt1047 == 0 && anInt1048 == 0) {
            int l3 = (int) (Math.random() * 2000D);
            if (l3 == 0) {
                anInt1047 = 1024;
            }
            if (l3 == 1)
                anInt1048 = 1024;
        }
    }

    public void method82(NpcDefinition class37, int i, int j, int k, byte byte0) {
        if (byte0 != -76)
            groundItems = null;
        if (anInt1183 >= 400)
            return;
        if (class37.anIntArray622 != null)
            class37 = class37.method363(false);
        if (class37 == null)
            return;
        if (!class37.aBoolean631)
            return;
        String s = class37.name;
        if (class37.anInt639 != 0)
            s = s + method92(class37.anInt639, thisPlayer.anInt1753, 736) + " (level-" + class37.anInt639 + ")";
        if (anInt1146 == 1) {
            rightClickOptions[anInt1183] = "Use " + aString1150 + " with @yel@" + s;
            anIntArray981[anInt1183] = 347;
            anIntArray982[anInt1183] = k;
            anIntArray979[anInt1183] = j;
            anIntArray980[anInt1183] = i;
            anInt1183++;
            return;
        }
        if (anInt1171 == 1) {
            if ((anInt1173 & 2) == 2) {
                rightClickOptions[anInt1183] = aString1174 + " @yel@" + s;
                anIntArray981[anInt1183] = 67;
                anIntArray982[anInt1183] = k;
                anIntArray979[anInt1183] = j;
                anIntArray980[anInt1183] = i;
                anInt1183++;
                return;
            }
        } else {
            if (class37.aStringArray646 != null) {
                for (int l = 4; l >= 0; l--)
                    if (class37.aStringArray646[l] != null && !class37.aStringArray646[l].equalsIgnoreCase("attack")) {
                        rightClickOptions[anInt1183] = class37.aStringArray646[l] + " @yel@" + s;
                        if (l == 0)
                            anIntArray981[anInt1183] = 318;
                        if (l == 1)
                            anIntArray981[anInt1183] = 921;
                        if (l == 2)
                            anIntArray981[anInt1183] = 118;
                        if (l == 3)
                            anIntArray981[anInt1183] = 553;
                        if (l == 4)
                            anIntArray981[anInt1183] = 432;
                        anIntArray982[anInt1183] = k;
                        anIntArray979[anInt1183] = j;
                        anIntArray980[anInt1183] = i;
                        anInt1183++;
                    }

            }
            if (class37.aStringArray646 != null) {
                for (int i1 = 4; i1 >= 0; i1--)
                    if (class37.aStringArray646[i1] != null && class37.aStringArray646[i1].equalsIgnoreCase("attack")) {
                        char c = '\0';
                        if (class37.anInt639 > thisPlayer.anInt1753)
                            c = '\u07D0';
                        rightClickOptions[anInt1183] = class37.aStringArray646[i1] + " @yel@" + s;
                        if (i1 == 0)
                            anIntArray981[anInt1183] = 318 + c;
                        if (i1 == 1)
                            anIntArray981[anInt1183] = 921 + c;
                        if (i1 == 2)
                            anIntArray981[anInt1183] = 118 + c;
                        if (i1 == 3)
                            anIntArray981[anInt1183] = 553 + c;
                        if (i1 == 4)
                            anIntArray981[anInt1183] = 432 + c;
                        anIntArray982[anInt1183] = k;
                        anIntArray979[anInt1183] = j;
                        anIntArray980[anInt1183] = i;
                        anInt1183++;
                    }

            }
            rightClickOptions[anInt1183] = "Examine @yel@" + s;
            anIntArray981[anInt1183] = 1668;
            anIntArray982[anInt1183] = k;
            anIntArray979[anInt1183] = j;
            anIntArray980[anInt1183] = i;
            anInt1183++;
        }
    }

    public void method83(IndexedSprite class50_sub1_sub1_sub3, int i) {
        size += i;
        int j = 256;
        for (int k = 0; k < anIntArray1176.length; k++)
            anIntArray1176[k] = 0;

        for (int l = 0; l < 5000; l++) {
            int i1 = (int) (Math.random() * 128D * (double) j);
            anIntArray1176[i1] = (int) (Math.random() * 256D);
        }

        for (int j1 = 0; j1 < 20; j1++) {
            for (int k1 = 1; k1 < j - 1; k1++) {
                for (int i2 = 1; i2 < 127; i2++) {
                    int k2 = i2 + (k1 << 7);
                    anIntArray1177[k2] = (anIntArray1176[k2 - 1] + anIntArray1176[k2 + 1] + anIntArray1176[k2 - 128] + anIntArray1176[k2 + 128]) / 4;
                }

            }

            int ai[] = anIntArray1176;
            anIntArray1176 = anIntArray1177;
            anIntArray1177 = ai;
        }

        if (class50_sub1_sub1_sub3 != null) {
            int l1 = 0;
            for (int j2 = 0; j2 < class50_sub1_sub1_sub3.height_1519; j2++) {
                for (int l2 = 0; l2 < class50_sub1_sub1_sub3.width_1518; l2++)
                    if (class50_sub1_sub1_sub3.pixels_1516[l1++] != 0) {
                        int i3 = l2 + 16 + class50_sub1_sub1_sub3.offsetX_1520;
                        int j3 = j2 + 16 + class50_sub1_sub1_sub3.offsetY_1521;
                        int k3 = i3 + (j3 << 7);
                        anIntArray1176[k3] = 0;
                    }

            }

        }
    }

    public void drawChatbox() {
        chatboxImage_1159.pushPixels();
        ThreeDimensionalCanvas.lineOffsets = chatBoxOffsets;
        aClass50_Sub1_Sub1_Sub3_1187.drawSprite(0, 0);
        if (aBoolean866) {
            loginScreenFont.drawHorizontallyCenteredString(239, 40, 0, aString937);
            loginScreenFont.drawHorizontallyCenteredString(239, 60, 128, userInputString + "*");
        } else if (chatboxInterfaceType == 1) {
            loginScreenFont.drawHorizontallyCenteredString(239, 40, 0, "Enter amount:");
            loginScreenFont.drawHorizontallyCenteredString(239, 60, 128, chatboxInput + "*");
        } else if (chatboxInterfaceType == 2) {
            loginScreenFont.drawHorizontallyCenteredString(239, 40, 0, "Enter name:");
            loginScreenFont.drawHorizontallyCenteredString(239, 60, 128, chatboxInput + "*");
        } else if (chatboxInterfaceType == 3) {
            if (chatboxInput != aString861) {
                method14(chatboxInput, 2);
                aString861 = chatboxInput;
            }
            JagFont class50_sub1_sub1_sub2 = fontChatboxButtons;
            Drawable.recalcEdges(0, 0, 77, 463, true);
            for (int j = 0; j < anInt862; j++) {
                int l = (18 + j * 14) - anInt865;
                if (l > 0 && l < 110)
                    class50_sub1_sub1_sub2.drawHorizontallyCenteredString(239, l, 0, aStringArray863[j]);
            }

            Drawable.recalcSize();
            if (anInt862 > 5) {
                method56(true, anInt865, 463, 77, anInt862 * 14 + 7, 0);
            }
            if (chatboxInput.length() == 0) {
                loginScreenFont.drawHorizontallyCenteredString(239, 40, 255, "Enter object name");
            } else if (anInt862 == 0) {
                loginScreenFont.drawHorizontallyCenteredString(239, 40, 0,
                        "No matching objects found, please shorten search");
            }
            class50_sub1_sub1_sub2.drawHorizontallyCenteredString(239, 90, 0, chatboxInput + "*");
            Drawable.drawHorizontalLine(0, 0, 77, 479);
        } else if (aString1058 != null) {
            loginScreenFont.drawHorizontallyCenteredString(239, 40, 0, aString1058);
            loginScreenFont.drawHorizontallyCenteredString(239, 60, 128, "Click to continue");
        } else if (anInt988 != -1)
            drawInterface(0, 0, JagInterface.forId(anInt988), 0, 8);
        else if (anInt1191 != -1) {
            drawInterface(0, 0, JagInterface.forId(anInt1191), 0, 8);
        } else {
            JagFont class50_sub1_sub1_sub2_1 = fontChatboxButtons;
            int k = 0;
            Drawable.recalcEdges(0, 0, 77, 463, true);
            for (int i1 = 0; i1 < 100; i1++)
                if (aStringArray1298[i1] != null) {
                    int j1 = anIntArray1296[i1];
                    int k1 = (70 - k * 14) + anInt851;
                    String s1 = aStringArray1297[i1];
                    byte byte0 = 0;
                    if (s1 != null && s1.startsWith("@cr1@")) {
                        s1 = s1.substring(5);
                        byte0 = 1;
                    }
                    if (s1 != null && s1.startsWith("@cr2@")) {
                        s1 = s1.substring(5);
                        byte0 = 2;
                    }
                    if (j1 == 0) {
                        if (k1 > 0 && k1 < 110)
                            class50_sub1_sub1_sub2_1.drawString_474(aStringArray1298[i1], 2245, 4, 0, k1);
                        k++;
                    }
                    if ((j1 == 1 || j1 == 2) && (j1 == 1 || publicChatMode == 0 || publicChatMode == 1 && method148(13292, s1))) {
                        if (k1 > 0 && k1 < 110) {
                            int l1 = 4;
                            if (byte0 == 1) {
                                aClass50_Sub1_Sub1_Sub3Array1142[0].drawSprite(l1, k1 - 12);
                                l1 += 14;
                            }
                            if (byte0 == 2) {
                                aClass50_Sub1_Sub1_Sub3Array1142[1].drawSprite(l1, k1 - 12);
                                l1 += 14;
                            }
                            class50_sub1_sub1_sub2_1.drawString_474(s1 + ":", 2245, l1, 0, k1);
                            l1 += class50_sub1_sub1_sub2_1.method472((byte) 35, s1) + 8;
                            class50_sub1_sub1_sub2_1.drawString_474(aStringArray1298[i1], 2245, l1, 255, k1);
                        }
                        k++;
                    }
                    if ((j1 == 3 || j1 == 7) && splitPrivateChat == 0
                            && (j1 == 7 || privateChatMode == 0 || privateChatMode == 1 && method148(13292, s1))) {
                        if (k1 > 0 && k1 < 110) {
                            int i2 = 4;
                            class50_sub1_sub1_sub2_1.drawString_474("From", 2245, i2, 0, k1);
                            i2 += class50_sub1_sub1_sub2_1.method472((byte) 35, "From ");
                            if (byte0 == 1) {
                                aClass50_Sub1_Sub1_Sub3Array1142[0].drawSprite(i2, k1 - 12);
                                i2 += 14;
                            }
                            if (byte0 == 2) {
                                aClass50_Sub1_Sub1_Sub3Array1142[1].drawSprite(i2, k1 - 12);
                                i2 += 14;
                            }
                            class50_sub1_sub1_sub2_1.drawString_474(s1 + ":", 2245, i2, 0, k1);
                            i2 += class50_sub1_sub1_sub2_1.method472((byte) 35, s1) + 8;
                            class50_sub1_sub1_sub2_1.drawString_474(aStringArray1298[i1], 2245, i2, 0x800000, k1);
                        }
                        k++;
                    }
                    if (j1 == 4 && (tradeMode == 0 || tradeMode == 1 && method148(13292, s1))) {
                        if (k1 > 0 && k1 < 110)
                            class50_sub1_sub1_sub2_1.drawString_474(s1 + " " + aStringArray1298[i1], 2245, 4, 0x800080, k1);
                        k++;
                    }
                    if (j1 == 5 && splitPrivateChat == 0 && privateChatMode < 2) {
                        if (k1 > 0 && k1 < 110)
                            class50_sub1_sub1_sub2_1.drawString_474(aStringArray1298[i1], 2245, 4, 0x800000, k1);
                        k++;
                    }
                    if (j1 == 6 && splitPrivateChat == 0 && privateChatMode < 2) {
                        if (k1 > 0 && k1 < 110) {
                            class50_sub1_sub1_sub2_1.drawString_474("To " + s1 + ":", 2245, 4, 0, k1);
                            class50_sub1_sub1_sub2_1.drawString_474(aStringArray1298[i1], 2245, 12 + class50_sub1_sub1_sub2_1.method472((byte) 35,
                                    "To " + s1), 0x800000, k1);
                        }
                        k++;
                    }
                    if (j1 == 8 && (tradeMode == 0 || tradeMode == 1 && method148(13292, s1))) {
                        if (k1 > 0 && k1 < 110)
                            class50_sub1_sub1_sub2_1.drawString_474(s1 + " " + aStringArray1298[i1], 2245, 4, 0x7e3200, k1);
                        k++;
                    }
                }

            Drawable.recalcSize();
            anInt1107 = k * 14 + 7;
            if (anInt1107 < 78)
                anInt1107 = 78;
            method56(true, anInt1107 - anInt851 - 77, 463, 77, anInt1107, 0);
            String s;
            if (thisPlayer != null && thisPlayer.username != null)
                s = thisPlayer.username;
            else
                s = StringUtils.formatPlayerName(thisPlayerName);
            class50_sub1_sub1_sub2_1.drawString_474(s + ":", 2245, 4, 0, 90);
            class50_sub1_sub1_sub2_1.drawString_474(chatInput + "*", 2245, 6 + class50_sub1_sub1_sub2_1.method472((byte) 35, s + ": "), 255,
                    90);
            Drawable.drawHorizontalLine(0, 0, 77, 479);
        }
        if (isContextMenuActive && anInt1304 == 2) {
            drawContextMenu();
        }
        drawPanel(chatboxImage_1159, layout.chatbox, ClientLayout.CHATBOX_ALPHA);
        gameViewportImage.pushPixels();
        ThreeDimensionalCanvas.lineOffsets = gameViewportOffsets;
    }

    public void method85(int i) {
        for (int j = -1; j < localPlayerCount; j++) {
            int k;
            if (j == -1)
                k = thisPlayerId;
            else
                k = localPlayers[j];
            Player class50_sub1_sub4_sub3_sub2 = players[k];
            if (class50_sub1_sub4_sub3_sub2 != null && ((Actor) (class50_sub1_sub4_sub3_sub2)).forcedChatTicks > 0) {
                class50_sub1_sub4_sub3_sub2.forcedChatTicks--;
                if (((Actor) (class50_sub1_sub4_sub3_sub2)).forcedChatTicks == 0)
                    class50_sub1_sub4_sub3_sub2.forcedChatMessage = null;
            }
        }

        size += i;
        for (int l = 0; l < localNpcCount; l++) {
            int i1 = anIntArray1134[l];
            Npc class50_sub1_sub4_sub3_sub1 = npcs[i1];
            if (class50_sub1_sub4_sub3_sub1 != null && ((Actor) (class50_sub1_sub4_sub3_sub1)).forcedChatTicks > 0) {
                class50_sub1_sub4_sub3_sub1.forcedChatTicks--;
                if (((Actor) (class50_sub1_sub4_sub3_sub1)).forcedChatTicks == 0)
                    class50_sub1_sub4_sub3_sub1.forcedChatMessage = null;
            }
        }

    }

    public void connectUpdateServer(boolean flag) {
        int i = 5;
        archiveHashes[8] = 0;
        if (flag) {
            for (int j = 1; j > 0; j++) ;
        }
        int k = 0;
        while (archiveHashes[8] == 0) {
            String s = "Unknown problem";
            drawLoadingText(20, "Connecting to web server");
            try {
                DataInputStream datainputstream = method31("crc" + (int) (Math.random() * 99999999D) + "-" + 377);
                JagBuffer class50_sub1_sub2 = new JagBuffer(new byte[40]);
                datainputstream.readFully(class50_sub1_sub2.buffer, 0, 40);
                datainputstream.close();
                for (int i1 = 0; i1 < 9; i1++)
                    archiveHashes[i1] = class50_sub1_sub2.getInt();

                int j1 = class50_sub1_sub2.getInt();
                int k1 = 1234;
                for (int l1 = 0; l1 < 9; l1++)
                    k1 = (k1 << 1) + archiveHashes[l1];

                if (j1 != k1) {
                    s = "checksum problem";
                    archiveHashes[8] = 0;
                }
            } catch (EOFException _ex) {
                s = "EOF problem";
                archiveHashes[8] = 0;
            } catch (IOException _ex) {
                s = "connection problem";
                archiveHashes[8] = 0;
            } catch (Exception _ex) {
                s = "logic problem";
                archiveHashes[8] = 0;
                if (!signlink.reporterror)
                    return;
            }
            if (archiveHashes[8] == 0) {
                k++;
                for (int l = i; l > 0; l--) {
                    if (k >= 10) {
                        drawLoadingText(10, "Game updated - please reload page");
                        l = 10;
                    } else {
                        drawLoadingText(10, s + " - Will retry in " + l + " secs.");
                    }
                    try {
                        Thread.sleep(1000L);
                    } catch (Exception _ex) {
                    }
                }

                i *= 2;
                if (i > 60)
                    i = 60;
                aBoolean900 = !aBoolean900;
            }
        }
    }

    public void method87(int i) {
        aClass18_1157.pushPixels();
        if (minimapState == 2) {
            byte abyte0[] = mapback_1186.pixels_1516;
            int ai[] = Drawable.pixels;
            int l2 = abyte0.length;
            for (int j5 = 0; j5 < l2; j5++)
                if (abyte0[j5] == 0)
                    ai[j5] = 0;

            rbgSprite_compass_1116.method465(0, 567, 33, 25, 33, anIntArray1286, 0, anInt1252, 256,
                    anIntArray1180, 25);
            gameViewportImage.pushPixels();
            ThreeDimensionalCanvas.lineOffsets = gameViewportOffsets;
            return;
        }
        int j = anInt1252 + anInt916 & 0x7ff;
        int k = 48 + ((Actor) (thisPlayer)).unitX / 32;
        i = 58 / i;
        int i3 = 464 - ((Actor) (thisPlayer)).unitY / 32;
        rbgSprite_1122.method465(5, 567, 151, k, 146, anIntArray920, 25, j, 256 + anInt1233,
                anIntArray1019, i3);
        rbgSprite_compass_1116.method465(0, 567, 33, 25, 33, anIntArray1286, 0, anInt1252, 256, anIntArray1180,
                25);
        for (int k5 = 0; k5 < anInt1076; k5++) {
            int l = (anIntArray1077[k5] * 4 + 2) - ((Actor) (thisPlayer)).unitX / 32;
            int j3 = (anIntArray1078[k5] * 4 + 2) - ((Actor) (thisPlayer)).unitY / 32;
            method130(j3, true, aClass50_Sub1_Sub1_Sub1Array1278[k5], l);
        }

        for (int l5 = 0; l5 < 104; l5++) {
            for (int i6 = 0; i6 < 104; i6++) {
                LinkedList class6 = groundItems[plane][l5][i6];
                if (class6 != null) {
                    int i1 = (l5 * 4 + 2) - ((Actor) (thisPlayer)).unitX / 32;
                    int k3 = (i6 * 4 + 2) - ((Actor) (thisPlayer)).unitY / 32;
                    method130(k3, true, rgbSprite_1192, i1);
                }
            }

        }

        for (int j6 = 0; j6 < localNpcCount; j6++) {
            Npc class50_sub1_sub4_sub3_sub1 = npcs[anIntArray1134[j6]];
            if (class50_sub1_sub4_sub3_sub1 != null && class50_sub1_sub4_sub3_sub1.isVisible()) {
                NpcDefinition class37 = class50_sub1_sub4_sub3_sub1.def;
                if (class37.anIntArray622 != null)
                    class37 = class37.method363(false);
                if (class37 != null && class37.aBoolean636 && class37.aBoolean631) {
                    int j1 = ((Actor) (class50_sub1_sub4_sub3_sub1)).unitX / 32
                            - ((Actor) (thisPlayer)).unitX / 32;
                    int l3 = ((Actor) (class50_sub1_sub4_sub3_sub1)).unitY / 32
                            - ((Actor) (thisPlayer)).unitY / 32;
                    method130(l3, true, aClass50_Sub1_Sub1_Sub1_1193, j1);
                }
            }
        }

        for (int k6 = 0; k6 < localPlayerCount; k6++) {
            Player class50_sub1_sub4_sub3_sub2 = players[localPlayers[k6]];
            if (class50_sub1_sub4_sub3_sub2 != null && class50_sub1_sub4_sub3_sub2.isVisible()) {
                int k1 = ((Actor) (class50_sub1_sub4_sub3_sub2)).unitX / 32
                        - ((Actor) (thisPlayer)).unitX / 32;
                int i4 = ((Actor) (class50_sub1_sub4_sub3_sub2)).unitY / 32
                        - ((Actor) (thisPlayer)).unitY / 32;
                boolean flag = false;
                long l6 = StringUtils.encodeBase37(class50_sub1_sub4_sub3_sub2.username);
                for (int i7 = 0; i7 < friendsCount; i7++) {
                    if (l6 != friends[i7] || anIntArray1267[i7] == 0)
                        continue;
                    flag = true;
                    break;
                }

                boolean flag1 = false;
                if (thisPlayer.team != 0 && class50_sub1_sub4_sub3_sub2.team != 0
                        && thisPlayer.team == class50_sub1_sub4_sub3_sub2.team)
                    flag1 = true;
                if (flag)
                    method130(i4, true, aClass50_Sub1_Sub1_Sub1_1195, k1);
                else if (flag1)
                    method130(i4, true, aClass50_Sub1_Sub1_Sub1_1196, k1);
                else
                    method130(i4, true, aClass50_Sub1_Sub1_Sub1_1194, k1);
            }
        }

        if (anInt1197 != 0 && pulseCycle % 20 < 10) {
            if (anInt1197 == 1 && anInt1226 >= 0 && anInt1226 < npcs.length) {
                Npc class50_sub1_sub4_sub3_sub1_1 = npcs[anInt1226];
                if (class50_sub1_sub4_sub3_sub1_1 != null) {
                    int l1 = ((Actor) (class50_sub1_sub4_sub3_sub1_1)).unitX / 32
                            - ((Actor) (thisPlayer)).unitX / 32;
                    int j4 = ((Actor) (class50_sub1_sub4_sub3_sub1_1)).unitY / 32
                            - ((Actor) (thisPlayer)).unitY / 32;
                    method55(j4, aClass50_Sub1_Sub1_Sub1_1037, -687, l1);
                }
            }
            if (anInt1197 == 2) {
                int i2 = ((anInt844 - nextTopLeftTileX) * 4 + 2) - ((Actor) (thisPlayer)).unitX / 32;
                int k4 = ((anInt845 - nextTopLeftTileY) * 4 + 2) - ((Actor) (thisPlayer)).unitY / 32;
                method55(k4, aClass50_Sub1_Sub1_Sub1_1037, -687, i2);
            }
            if (anInt1197 == 10 && anInt1151 >= 0 && anInt1151 < players.length) {
                Player class50_sub1_sub4_sub3_sub2_1 = players[anInt1151];
                if (class50_sub1_sub4_sub3_sub2_1 != null) {
                    int j2 = ((Actor) (class50_sub1_sub4_sub3_sub2_1)).unitX / 32
                            - ((Actor) (thisPlayer)).unitX / 32;
                    int l4 = ((Actor) (class50_sub1_sub4_sub3_sub2_1)).unitY / 32
                            - ((Actor) (thisPlayer)).unitY / 32;
                    method55(l4, aClass50_Sub1_Sub1_Sub1_1037, -687, j2);
                }
            }
        }
        if (anInt1120 != 0) {
            int k2 = (anInt1120 * 4 + 2) - ((Actor) (thisPlayer)).unitX / 32;
            int i5 = (anInt1121 * 4 + 2) - ((Actor) (thisPlayer)).unitY / 32;
            method130(i5, true, aClass50_Sub1_Sub1_Sub1_1036, k2);
        }
        Drawable.drawFullRect(97, 78, 3, 3, 0xffffff);
        gameViewportImage.pushPixels();
        ThreeDimensionalCanvas.lineOffsets = gameViewportOffsets;
    }

    public URL getCodeBase() {
        if (signlink.mainapp != null)
            return signlink.mainapp.getCodeBase();
        try {
            if (super.frame != null)
                return new URL("http://127.0.0.1:" + (80 + portOffset));
        } catch (Exception _ex) {
        }
        return super.getCodeBase();
    }

    public boolean updateInterfaceAnimations(int i, int id) {
        boolean flag = false;
        JagInterface class13 = JagInterface.forId(id);
        for (int k = 0; k < class13.anIntArray258.length; k++) {
            if (class13.anIntArray258[k] == -1)
                break;
            JagInterface class13_1 = JagInterface.forId(class13.anIntArray258[k]);
            if (class13_1.type == 0)
                flag |= updateInterfaceAnimations(i, class13_1.id);
            if (class13_1.type == 6 && (class13_1.anInt286 != -1 || class13_1.anInt287 != -1)) {
                boolean flag1 = method95(class13_1, -693);
                int i1;
                if (flag1)
                    i1 = class13_1.anInt287;
                else
                    i1 = class13_1.anInt286;
                if (i1 != -1) {
                    Animation class14 = Animation.animations[i1];
                    for (class13_1.anInt227 += i; class13_1.anInt227 > class14.method205(0, class13_1.anInt235); ) {
                        class13_1.anInt227 -= class14.method205(0, class13_1.anInt235);
                        class13_1.anInt235++;
                        if (class13_1.anInt235 >= class14.anInt294) {
                            class13_1.anInt235 -= class14.anInt298;
                            if (class13_1.anInt235 < 0 || class13_1.anInt235 >= class14.anInt294)
                                class13_1.anInt235 = 0;
                        }
                        flag = true;
                    }

                }
            }
            if (class13_1.type == 6 && class13_1.anInt218 != 0) {
                int l = class13_1.anInt218 >> 16;
                int j1 = (class13_1.anInt218 << 16) >> 16;
                l *= i;
                j1 *= i;
                class13_1.anInt252 = class13_1.anInt252 + l & 0x7ff;
                class13_1.anInt253 = class13_1.anInt253 + j1 & 0x7ff;
                flag = true;
            }
        }
        return flag;
    }

    public String method89(int i, int j) {
        if (j < 8 || j > 8)
            throw new NullPointerException();
        if (i < 0x3b9ac9ff)
            return String.valueOf(i);
        else
            return "*";
    }

    public void method90(int i, long l) {
        try {
            if (i != -916)
                opcode = buffer.getByte();
            if (l == 0L)
                return;
            if (ignoresCount >= 100) {
                pushMessage("", (byte) -123, "Your ignore list is full. Max of 100 hit", 0);
                return;
            }
            String s = StringUtils.formatPlayerName(StringUtils.decodeBase37(l));
            for (int j = 0; j < ignoresCount; j++)
                if (ignores[j] == l) {
                    pushMessage("", (byte) -123, s + " is already on your ignore list", 0);
                    return;
                }

            for (int k = 0; k < friendsCount; k++)
                if (friends[k] == l) {
                    pushMessage("", (byte) -123, "Please remove " + s + " from your friend list first", 0);
                    return;
                }

            ignores[ignoresCount++] = l;
            aBoolean1181 = true;
            outBuffer.putOpcode(217);
            outBuffer.putLong(l);
            return;
        } catch (RuntimeException runtimeexception) {
            signlink.reporterror("27939, " + i + ", " + l + ", " + runtimeexception.toString());
        }
        throw new RuntimeException();
    }

    public void method7(byte byte0) {
        // TEST WHEN THIS IS BEING CALLED EVERY PULSE...
        // IF ITS BEING CALLED IF NOT WHY NOT, LEADS TO PACKET DECODING
        if (aBoolean1016 || aBoolean1283 || aBoolean1097)
            return;
        pulseCycle++;
        if (byte0 != -111)
            return;
        if (startupClientSize != 0) {
            // the client has loaded, so the saved mode can be applied
            int size = startupClientSize;
            startupClientSize = 0;
            toggleSize(size);
        }
        checkSize();
        if (!isLoggedIn)
            method149(-724);
        else
            updateGame28((byte) 4);
        method77(false);
    }

    public void generateContextOptions(int i) {
        if (anInt1113 != 0) {
            return;
        }
        rightClickOptions[0] = "Cancel";
        anIntArray981[0] = 1016;
        anInt1183 = 1;
        if (i >= 0) {
            anInt1004 = incomingRandom.nextInt();
        }
        if (anInt1053 != -1) {
            anInt915 = 0;
            anInt1315 = 0;
            method66(layout.fullscreenInterfaceY, JagInterface.forId(anInt1053), 0, 0, layout.fullscreenInterfaceX, super.mouseX, 23658, super.mouseY);
            if (anInt915 != currentlyHovered1302)
                currentlyHovered1302 = anInt915;
            if (anInt1315 != currentlyHovered1129)
                currentlyHovered1129 = anInt1315;
            return;
        }
        method111(anInt1178);
        anInt915 = 0;
        anInt1315 = 0;
        if (layout.isInViewport(super.mouseX, super.mouseY)) {
            if (anInt1169 != -1) {
                method66(layout.viewport.y, JagInterface.forId(anInt1169), 0, 0, layout.viewport.x, super.mouseX, 23658, super.mouseY);
            } else {
                generateContextOptions43((byte) 7);
            }
        }
        if (anInt915 != currentlyHovered1302) {
            currentlyHovered1302 = anInt915;
        }
        if (anInt1315 != currentlyHovered1129) {
            currentlyHovered1129 = anInt1315;
        }
        anInt915 = 0;
        anInt1315 = 0;
        if (layout.isInInventory(super.mouseX, super.mouseY))
            if (anInt1089 != -1) {
                method66(layout.inventory.y, JagInterface.forId(anInt1089), 1, 0, layout.inventory.x, super.mouseX, 23658, super.mouseY);
            } else if (anIntArray1081[tabId] != -1) {
                method66(layout.inventory.y, JagInterface.forId(anIntArray1081[tabId]), 1, 0, layout.inventory.x, super.mouseX, 23658,
                        super.mouseY);
            }
        if (anInt915 != anInt1280) {
            aBoolean1181 = true;
            anInt1280 = anInt915;
        }
        if (anInt1315 != anInt1044) {
            aBoolean1181 = true;
            anInt1044 = anInt1315;
        }
        anInt915 = 0;
        anInt1315 = 0;
        if (layout.isInChatbox(super.mouseX, super.mouseY))
            if (anInt988 != -1) {
                method66(layout.chatbox.y, JagInterface.forId(anInt988), 2, 0, layout.chatbox.x, super.mouseX, 23658, super.mouseY);
            } else if (anInt1191 != -1) {
                method66(layout.chatbox.y, JagInterface.forId(anInt1191), 3, 0, layout.chatbox.x, super.mouseX, 23658, super.mouseY);
            } else if (super.mouseY < 434 + layout.chatboxDy && super.mouseX < 426 && chatboxInterfaceType == 0) {
                method113(466, super.mouseX - layout.chatbox.x, super.mouseY - layout.chatbox.y);
            }
        if ((anInt988 != -1 || anInt1191 != -1) && anInt915 != anInt1106) {
            aBoolean1240 = true;
            anInt1106 = anInt915;
        }
        if ((anInt988 != -1 || anInt1191 != -1) && anInt1315 != anInt1284) {
            aBoolean1240 = true;
            anInt1284 = anInt1315;
        }
        for (boolean flag = false; !flag; ) {
            flag = true;
            for (int j = 0; j < anInt1183 - 1; j++) {
                if (anIntArray981[j] < 1000 && anIntArray981[j + 1] > 1000) {
                    String s = rightClickOptions[j];
                    rightClickOptions[j] = rightClickOptions[j + 1];
                    rightClickOptions[j + 1] = s;
                    int k = anIntArray981[j];
                    anIntArray981[j] = anIntArray981[j + 1];
                    anIntArray981[j + 1] = k;
                    k = anIntArray979[j];
                    anIntArray979[j] = anIntArray979[j + 1];
                    anIntArray979[j + 1] = k;
                    k = anIntArray980[j];
                    anIntArray980[j] = anIntArray980[j + 1];
                    anIntArray980[j + 1] = k;
                    k = anIntArray982[j];
                    anIntArray982[j] = anIntArray982[j + 1];
                    anIntArray982[j + 1] = k;
                    flag = false;
                }
            }
        }
    }

    public static String method92(int i, int j, int k) {
        if (k <= 0)
            throw new NullPointerException();
        int l = j - i;
        if (l < -9)
            return "@red@";
        if (l < -6)
            return "@or3@";
        if (l < -3)
            return "@or2@";
        if (l < 0)
            return "@or1@";
        if (l > 9)
            return "@gre@";
        if (l > 6)
            return "@gr3@";
        if (l > 3)
            return "@gr2@";
        if (l > 0)
            return "@gr1@";
        else
            return "@yel@";
    }

    public void loadRegion() {
        try {
            anInt1276 = -1;
            aClass6_1210.clear();
            projectileQueue.clear();
            ThreeDimensionalCanvas.clearTexels();
            method49(383);
            sceneGraph.method241((byte) 7);
            System.gc();
            for (int plane = 0; plane < 4; plane++)
                clippingPlanes[plane].clear();

            for (int i1 = 0; i1 < 4; i1++) {
                for (int l1 = 0; l1 < 104; l1++) {
                    for (int k2 = 0; k2 < 104; k2++)
                        aByteArrayArrayArray1125[i1][l1][k2] = 0;

                }

            }

            Region region = new Region(intGroundArray, 14290, aByteArrayArrayArray1125, 104, 104);
            int l2 = aByteArrayArray838.length;
            outBuffer.putOpcode(40);
            if (!aBoolean1163) {
                for (int j3 = 0; j3 < l2; j3++) {
                    int j4 = (coordinates[j3] >> 8) * 64 - nextTopLeftTileX;
                    int l5 = (coordinates[j3] & 0xff) * 64 - nextTopLeftTileY;
                    byte abyte0[] = aByteArrayArray838[j3];
                    if (abyte0 != null)
                        region.method174(l5, false, (chunkY - 6) * 8, j4, abyte0, (chunkX - 6) * 8,
                                clippingPlanes);
                }

                for (int k4 = 0; k4 < l2; k4++) {
                    int i6 = (coordinates[k4] >> 8) * 64 - nextTopLeftTileX;
                    int l7 = (coordinates[k4] & 0xff) * 64 - nextTopLeftTileY;
                    byte abyte2[] = aByteArrayArray838[k4];
                    if (abyte2 == null && chunkY < 800)
                        region.method180(i6, l7, 64, -810, 64);
                }

                outBuffer.putOpcode(40);
                for (int j6 = 0; j6 < l2; j6++) {
                    byte abyte1[] = aByteArrayArray1232[j6];
                    if (abyte1 != null) {
                        int l8 = (coordinates[j6] >> 8) * 64 - nextTopLeftTileX;
                        int k9 = (coordinates[j6] & 0xff) * 64 - nextTopLeftTileY;
                        region.method179(k9, clippingPlanes, l8, -571, sceneGraph, abyte1);
                    }
                }

            }
            if (aBoolean1163) {
                for (int k3 = 0; k3 < 4; k3++) {
                    for (int l4 = 0; l4 < 13; l4++) {
                        for (int k6 = 0; k6 < 13; k6++) {
                            boolean flag = false;
                            int i9 = constructedMapPalette[k3][l4][k6];
                            if (i9 != -1) {
                                int l9 = i9 >> 24 & 3;
                                int j10 = i9 >> 1 & 3;
                                int l10 = i9 >> 14 & 0x3ff;
                                int j11 = i9 >> 3 & 0x7ff;
                                int l11 = (l10 / 8 << 8) + j11 / 8;
                                for (int j12 = 0; j12 < coordinates.length; j12++) {
                                    if (coordinates[j12] != l11 || aByteArrayArray838[j12] == null)
                                        continue;
                                    region.method168(j10, (j11 & 7) * 8, false, aByteArrayArray838[j12], k3, l9,
                                            l4 * 8, clippingPlanes, k6 * 8, (l10 & 7) * 8);
                                    flag = true;
                                    break;
                                }

                            }
                            if (!flag)
                                region.method166(anInt1072, k3, k6 * 8, l4 * 8);
                        }

                    }

                }

                for (int i5 = 0; i5 < 13; i5++) {
                    for (int l6 = 0; l6 < 13; l6++) {
                        int i8 = constructedMapPalette[0][i5][l6];
                        if (i8 == -1)
                            region.method180(i5 * 8, l6 * 8, 8, -810, 8);
                    }

                }

                outBuffer.putOpcode(40); // Spam
                for (int plane = 0; plane < 4; plane++) {
                    for (int x = 0; x < 13; x++) {
                        for (int y = 0; y < 13; y++) {
                            int i10 = constructedMapPalette[plane][x][y];
                            if (i10 != -1) {
                                int k10 = i10 >> 24 & 3;
                                int i11 = i10 >> 1 & 3;
                                int k11 = i10 >> 14 & 0x3ff;
                                int i12 = i10 >> 3 & 0x7ff;
                                int k12 = (k11 / 8 << 8) + i12 / 8;
                                for (int l12 = 0; l12 < coordinates.length; l12++) {
                                    if (coordinates[l12] != k12 || aByteArrayArray1232[l12] == null)
                                        continue;
                                    region.method172(plane, clippingPlanes, sceneGraph,
                                            aByteArrayArray1232[l12], y * 8, i11, (k11 & 7) * 8, x * 8,
                                            (i12 & 7) * 8, k10);
                                    break;
                                }

                            }
                        }

                    }

                }

            }
            outBuffer.putOpcode(40);
            region.method167(clippingPlanes, anInt1318, sceneGraph);
            if (gameViewportImage != null) {
                gameViewportImage.pushPixels();
                ThreeDimensionalCanvas.lineOffsets = gameViewportOffsets;
            }
            outBuffer.putOpcode(40);
            int l3 = Region.anInt150;
            if (l3 > plane)
                l3 = plane;
            if (l3 < plane - 1)
                l3 = plane - 1;
            if (lowMemory)
                sceneGraph.method242(Region.anInt150, true);
            else
                sceneGraph.method242(0, true);
            for (int j5 = 0; j5 < 104; j5++) {
                for (int j7 = 0; j7 < 104; j7++)
                    method26(j5, j7);

            }

            processGameObjectSpawnRequests();
        } catch (Exception exception) {
        }
        ObjectDefinition.lruHashTable.clear();
        if (super.frame != null) {
            outBuffer.putOpcode(78);
            outBuffer.putInt(0x3f008edd);
        }
        if (lowMemory && signlink.cache_dat != null) {
            int k = fileFetcher.method340(0);
            for (int j1 = 0; j1 < k; j1++) {
                int i2 = fileFetcher.method325(j1, -493);
                if ((i2 & 0x79) == 0)
                    Model.unloadModelHeader(j1);
            }

        }
        System.gc();
        ThreeDimensionalCanvas.initTextureBufferPool(20);
        fileFetcher.method336((byte) -125);
        int l = (chunkX - 6) / 8 - 1;
        int k1 = (chunkX + 6) / 8 + 1;
        int j2 = (chunkY - 6) / 8 - 1;
        int i3 = (chunkY + 6) / 8 + 1;
        if (aBoolean1067) {
            l = 49;
            k1 = 50;
            j2 = 49;
            i3 = 50;
        }
        for (int i4 = l; i4 <= k1; i4++) {
            for (int k5 = j2; k5 <= i3; k5++)
                if (i4 == l || i4 == k1 || k5 == j2 || k5 == i3) {
                    int k7 = fileFetcher.method344(0, i4, k5, 0);
                    if (k7 != -1)
                        fileFetcher.method337(k7, 3, aByte936);
                    int k8 = fileFetcher.method344(0, i4, k5, 1);
                    if (k8 != -1)
                        fileFetcher.method337(k8, 3, aByte936);
                }

        }

    }

    public void updateCamera94(int i, int j, int k, int l, int i1, int j1, byte byte0) {
        int k1 = 2048 - k & 0x7ff;
        int l1 = 2048 - i1 & 0x7ff;
        if (byte0 != -103)
            opcode = -1;
        int i2 = 0;
        int j2 = 0;
        int k2 = l;
        if (k1 != 0) {
            int l2 = Model.sineTable[k1];
            int j3 = Model.cosineTable[k1];
            int l3 = j2 * j3 - k2 * l2 >> 16;
            k2 = j2 * l2 + k2 * j3 >> 16;
            j2 = l3;
        }
        if (l1 != 0) {
            int i3 = Model.sineTable[l1];
            int k3 = Model.cosineTable[l1];
            int i4 = k2 * i3 + i2 * k3 >> 16;
            k2 = k2 * k3 - i2 * i3 >> 16;
            i2 = i4;
        }
        anInt1216 = j - i2;
        anInt1217 = i - j2;
        anInt1218 = j1 - k2;
        anInt1219 = k;
        anInt1220 = i1;
    }

    public boolean method95(JagInterface class13, int i) {
        if (i >= 0)
            anInt1175 = 276;
        if (class13.anIntArray273 == null)
            return false;
        for (int j = 0; j < class13.anIntArray273.length; j++) {
            int k = method129(3, j, class13);
            int l = class13.anIntArray256[j];
            if (class13.anIntArray273[j] == 2) {
                if (k >= l)
                    return false;
            } else if (class13.anIntArray273[j] == 3) {
                if (k <= l)
                    return false;
            } else if (class13.anIntArray273[j] == 4) {
                if (k == l)
                    return false;
            } else if (k != l)
                return false;
        }

        return true;
    }

    public void updatePlayers(int packetSize, int j, JagBuffer vec) {
        removePlayerCount = 0;
        updatedPlayerCount = 0;
        updateThisPlayerMovement(packetSize, aBoolean1274, vec);
        updateOtherPlayerMovement(packetSize, vec);

        j = 40 / j;
        addNewPlayers(packetSize, vec);
        parsePlayerBlocks(vec, packetSize);

        for (int k = 0; k < removePlayerCount; k++) {
            int l = removePlayers[k];
            if (((Actor) (players[l])).pulseCycle != pulseCycle)
                players[l] = null;
        }

        if (vec.position != packetSize) {
            signlink
                    .reporterror("Error packet size mismatch in getplayer pos:" + vec.position + " psize:" + packetSize);
            throw new RuntimeException("eek");
        }
        for (int i1 = 0; i1 < localPlayerCount; i1++)
            if (players[localPlayers[i1]] == null) {
                signlink.reporterror(thisPlayerName + " null entry in pl list - pos:" + i1 + " size:"
                        + localPlayerCount);
                throw new RuntimeException("eek");
            }

    }

    public void removeIgnore(int i, long l) {
        try {
            if (l == 0L) {
                return;
            }
            for (int j = 0; j < ignoresCount; j++) {
                if (ignores[j] != l) {
                    continue;
                }
                ignoresCount--;
                aBoolean1181 = true;
                for (int k = j; k < ignoresCount; k++) {
                    ignores[k] = ignores[k + 1];
                }

                outBuffer.putOpcode(160);
                outBuffer.putLong(l);
                break;
            }

            i = 42 / i;
            return;
        } catch (RuntimeException runtimeexception) {
            signlink.reporterror("45745, " + i + ", " + l + ", " + runtimeexception.toString());
        }
        throw new RuntimeException();
    }

    public String getParameter(String s) {
        if (signlink.mainapp != null)
            return signlink.mainapp.getParameter(s);
        else
            return super.getParameter(s);
    }

    public void renderLoginFlames() {
        char c = '\u0100';
        if (anInt1047 > 0) {
            for (int j = 0; j < 256; j++) {
                if (anInt1047 > 768) {
                    anIntArray1310[j] = method106(anIntArray1311[j], anIntArray1312[j], 1024 - anInt1047, 8);
                } else if (anInt1047 > 256) {
                    anIntArray1310[j] = anIntArray1312[j];
                } else {
                    anIntArray1310[j] = method106(anIntArray1312[j], anIntArray1311[j], 256 - anInt1047, 8);
                }
            }

        } else if (anInt1048 > 0) {
            for (int k = 0; k < 256; k++)
                if (anInt1048 > 768)
                    anIntArray1310[k] = method106(anIntArray1311[k], anIntArray1313[k], 1024 - anInt1048, 8);
                else if (anInt1048 > 256)
                    anIntArray1310[k] = anIntArray1313[k];
                else {
                    anIntArray1310[k] = method106(anIntArray1313[k], anIntArray1311[k], 256 - anInt1048, 8);
                }

        } else {
            for (int l = 0; l < 256; l++) {
                anIntArray1310[l] = anIntArray1311[l];
            }

        }
        for (int i1 = 0; i1 < 33920; i1++) {
            loginFlameLeft.pixels[i1] = sprite_1017.pixels_1489[i1];
        }

        int j1 = 0;
        int k1 = 1152;
        for (int l1 = 1; l1 < c - 1; l1++) {
            int i2 = (anIntArray1166[l1] * (c - l1)) / c;
            int k2 = 22 + i2;
            if (k2 < 0)
                k2 = 0;
            j1 += k2;
            for (int i3 = k2; i3 < 128; i3++) {
                int k3 = anIntArray1084[j1++];
                if (k3 != 0) {
                    int i4 = k3;
                    int k4 = 256 - k3;
                    k3 = anIntArray1310[k3];
                    int i5 = loginFlameLeft.pixels[k1];
                    loginFlameLeft.pixels[k1++] = ((k3 & 0xff00ff) * i4 + (i5 & 0xff00ff) * k4 & 0xff00ff00)
                            + ((k3 & 0xff00) * i4 + (i5 & 0xff00) * k4 & 0xff0000) >> 8;
                } else {
                    k1++;
                }
            }

            k1 += k2;
        }

        loginFlameLeft.drawImage(0, 0, super.graphics);
        for (int j2 = 0; j2 < 33920; j2++)
            loginFlameRight.pixels[j2] = sprite_1018.pixels_1489[j2];

        j1 = 0;
        k1 = 1176;
        for (int l2 = 1; l2 < c - 1; l2++) {
            int j3 = (anIntArray1166[l2] * (c - l2)) / c;
            int l3 = 103 - j3;
            k1 += j3;
            for (int j4 = 0; j4 < l3; j4++) {
                int l4 = anIntArray1084[j1++];
                if (l4 != 0) {
                    int j5 = l4;
                    int k5 = 256 - l4;
                    l4 = anIntArray1310[l4];
                    int l5 = loginFlameRight.pixels[k1];
                    loginFlameRight.pixels[k1++] = ((l4 & 0xff00ff) * j5 + (l5 & 0xff00ff) * k5 & 0xff00ff00)
                            + ((l4 & 0xff00) * j5 + (l5 & 0xff00) * k5 & 0xff0000) >> 8;
                } else {
                    k1++;
                }
            }

            j1 += 128 - l3;
            k1 += 128 - l3 - j3;
        }

        loginFlameRight.drawImage(637, 0, super.graphics);
        presentBackBuffer();
    }

    public void adjustVolume(boolean flag, byte byte0, int i) {
        if (byte0 != 8)
            outBuffer.putByte(49);
        signlink.midivol = i;
        if (flag)
            signlink.midi = "voladjust";
    }

    public void method100(int i) {
        for (int j = -1; j < localPlayerCount; j++) {
            int k;
            if (j == -1)
                k = thisPlayerId;
            else
                k = localPlayers[j];
            Player class50_sub1_sub4_sub3_sub2 = players[k];
            if (class50_sub1_sub4_sub3_sub2 != null)
                method68(1, (byte) -97, class50_sub1_sub4_sub3_sub2);
        }

        if (i < anInt1222 || i > anInt1222) {
            for (int l = 1; l > 0; l++) ;
        }
    }

    public static void switchToLowMem() {
        SceneGraph.lowMemory = true;
        ThreeDimensionalCanvas.lowMemory = true;
        lowMemory = true;
        Region.lowMemory = true;
        ObjectDefinition.lowMemory = true;
    }

    public void addFriend(long l, int i) {
        try {
            if (l == 0L)
                return;
            if (friendsCount >= 100 && playerMembers != 1) {
                pushMessage("", (byte) -123, "Your friendlist is full. Max of 100 for free users, and 200 for members", 0);
                return;
            }
            if (friendsCount >= 200) {
                pushMessage("", (byte) -123, "Your friendlist is full. Max of 100 for free users, and 200 for members", 0);
                return;
            }
            String s = StringUtils.formatPlayerName(StringUtils.decodeBase37(l));
            for (int j = 0; j < friendsCount; j++)
                if (friends[j] == l) {
                    pushMessage("", (byte) -123, s + " is already on your friend list", 0);
                    return;
                }

            for (int k = 0; k < ignoresCount; k++)
                if (ignores[k] == l) {
                    pushMessage("", (byte) -123, "Please remove " + s + " from your ignore list first", 0);
                    return;
                }

            if (s.equals(thisPlayer.username))
                return;
            aStringArray849[friendsCount] = s;
            if (i != -45229)
                anInt1178 = -30;
            friends[friendsCount] = l;
            anIntArray1267[friendsCount] = 0;
            friendsCount++;
            aBoolean1181 = true;
            outBuffer.putOpcode(120);
            outBuffer.putLong(l);
            return;
        } catch (RuntimeException runtimeexception) {
            signlink.reporterror("94629, " + l + ", " + i + ", " + runtimeexception.toString());
        }
        throw new RuntimeException();
    }

    public void method103(byte byte0, JagInterface class13) {
        if (byte0 == 2)
            byte0 = 0;
        else
            anInt1004 = -82;
        int i = class13.anInt242;
        if (i >= 1 && i <= 100 || i >= 701 && i <= 800) {
            if (i == 1 && anInt860 == 0) {
                class13.aString230 = "Loading friend list";
                class13.anInt289 = 0;
                return;
            }
            if (i == 1 && anInt860 == 1) {
                class13.aString230 = "Connecting to friendserver";
                class13.anInt289 = 0;
                return;
            }
            if (i == 2 && anInt860 != 2) {
                class13.aString230 = "Please wait...";
                class13.anInt289 = 0;
                return;
            }
            int j = friendsCount;
            if (anInt860 != 2)
                j = 0;
            if (i > 700)
                i -= 601;
            else
                i--;
            if (i >= j) {
                class13.aString230 = "";
                class13.anInt289 = 0;
                return;
            } else {
                class13.aString230 = aStringArray849[i];
                class13.anInt289 = 1;
                return;
            }
        }
        if (i >= 101 && i <= 200 || i >= 801 && i <= 900) {
            int k = friendsCount;
            if (anInt860 != 2)
                k = 0;
            if (i > 800)
                i -= 701;
            else
                i -= 101;
            if (i >= k) {
                class13.aString230 = "";
                class13.anInt289 = 0;
                return;
            }
            if (anIntArray1267[i] == 0)
                class13.aString230 = "@red@Offline";
            else if (anIntArray1267[i] < 200) {
                if (anIntArray1267[i] == world)
                    class13.aString230 = "@gre@World" + (anIntArray1267[i] - 9);
                else
                    class13.aString230 = "@yel@World" + (anIntArray1267[i] - 9);
            } else if (anIntArray1267[i] == world)
                class13.aString230 = "@gre@Classic" + (anIntArray1267[i] - 219);
            else
                class13.aString230 = "@yel@Classic" + (anIntArray1267[i] - 219);
            class13.anInt289 = 1;
            return;
        }
        if (i == 203) {
            int l = friendsCount;
            if (anInt860 != 2)
                l = 0;
            class13.anInt285 = l * 15 + 20;
            if (class13.anInt285 <= class13.height)
                class13.anInt285 = class13.height + 1;
            return;
        }
        if (i >= 401 && i <= 500) {
            if ((i -= 401) == 0 && anInt860 == 0) {
                class13.aString230 = "Loading ignore list";
                class13.anInt289 = 0;
                return;
            }
            if (i == 1 && anInt860 == 0) {
                class13.aString230 = "Please wait...";
                class13.anInt289 = 0;
                return;
            }
            int i1 = ignoresCount;
            if (anInt860 == 0)
                i1 = 0;
            if (i >= i1) {
                class13.aString230 = "";
                class13.anInt289 = 0;
                return;
            } else {
                class13.aString230 = StringUtils.formatPlayerName(StringUtils.decodeBase37(ignores[i]));
                class13.anInt289 = 1;
                return;
            }
        }
        if (i == 503) {
            class13.anInt285 = ignoresCount * 15 + 20;
            if (class13.anInt285 <= class13.height)
                class13.anInt285 = class13.height + 1;
            return;
        }
        if (i == 327) {
            class13.anInt252 = 150;
            class13.anInt253 = (int) (Math.sin((double) pulseCycle / 40D) * 256D) & 0x7ff;
            if (aBoolean1277) {
                for (int j1 = 0; j1 < 7; j1++) {
                    int i2 = anIntArray1326[j1];
                    if (i2 >= 0 && !IdentityKit.identityKits[i2].isBodyDownloaded())
                        return;
                }

                aBoolean1277 = false;
                Model aclass50_sub1_sub4_sub4[] = new Model[7];
                int j2 = 0;
                for (int k2 = 0; k2 < 7; k2++) {
                    int l2 = anIntArray1326[k2];
                    if (l2 >= 0)
                        aclass50_sub1_sub4_sub4[j2++] = IdentityKit.identityKits[l2].getBodyModel();
                }

                Model class50_sub1_sub4_sub4 = new Model(j2, aclass50_sub1_sub4_sub4);
                for (int i3 = 0; i3 < 5; i3++)
                    if (anIntArray1099[i3] != 0) {
                        class50_sub1_sub4_sub4.replaceColor(anIntArrayArray1008[i3][0],
                                anIntArrayArray1008[i3][anIntArray1099[i3]]);
                        if (i3 == 1)
                            class50_sub1_sub4_sub4.replaceColor(anIntArray1268[0], anIntArray1268[anIntArray1099[i3]]);
                    }

                class50_sub1_sub4_sub4.groupIndicesByTransform();
                class50_sub1_sub4_sub4.applyAnimation(
                        Animation.animations[((Actor) (thisPlayer)).anInt1634].anIntArray295[0], (byte) 6);
                class50_sub1_sub4_sub4.initLighting(64, 850, -30, -50, -30, true);
                class13.anInt283 = 5;
                class13.anInt284 = 0;
                JagInterface.method201(5, class50_sub1_sub4_sub4, 0, 6);
            }
            return;
        }
        if (i == 324) {
            if (aClass50_Sub1_Sub1_Sub1_1102 == null) {
                aClass50_Sub1_Sub1_Sub1_1102 = class13.aClass50_Sub1_Sub1_Sub1_212;
                aClass50_Sub1_Sub1_Sub1_1103 = class13.aClass50_Sub1_Sub1_Sub1_245;
            }
            if (aBoolean1144) {
                class13.aClass50_Sub1_Sub1_Sub1_212 = aClass50_Sub1_Sub1_Sub1_1103;
                return;
            } else {
                class13.aClass50_Sub1_Sub1_Sub1_212 = aClass50_Sub1_Sub1_Sub1_1102;
                return;
            }
        }
        if (i == 325) {
            if (aClass50_Sub1_Sub1_Sub1_1102 == null) {
                aClass50_Sub1_Sub1_Sub1_1102 = class13.aClass50_Sub1_Sub1_Sub1_212;
                aClass50_Sub1_Sub1_Sub1_1103 = class13.aClass50_Sub1_Sub1_Sub1_245;
            }
            if (aBoolean1144) {
                class13.aClass50_Sub1_Sub1_Sub1_212 = aClass50_Sub1_Sub1_Sub1_1102;
                return;
            } else {
                class13.aClass50_Sub1_Sub1_Sub1_212 = aClass50_Sub1_Sub1_Sub1_1103;
                return;
            }
        }
        if (i == 600) {
            class13.aString230 = aString839;
            if (pulseCycle % 20 < 10) {
                class13.aString230 += "|";
                return;
            } else {
                class13.aString230 += " ";
                return;
            }
        }
        if (i == 620)
            if (playerRights >= 1) {
                if (aBoolean1098) {
                    class13.anInt240 = 0xff0000;
                    class13.aString230 = "Moderator option: Mute player for 48 hours: <ON>";
                } else {
                    class13.anInt240 = 0xffffff;
                    class13.aString230 = "Moderator option: Mute player for 48 hours: <OFF>";
                }
            } else {
                class13.aString230 = "";
            }
        if (i == 660) {
            int k1 = lastLoginDays - somethngLoginDays;
            String s1;
            if (k1 <= 0)
                s1 = "earlier today";
            else if (k1 == 1)
                s1 = "yesterday";
            else
                s1 = k1 + " days ago";
            class13.aString230 = "You last logged in @red@" + s1 + "@bla@ from: @red@" + signlink.dns;
        }
        if (i == 661)
            if (recoveryQuestionDays == 0)
                class13.aString230 = "\\nYou have not yet set any recovery questions.\\nIt is @lre@strongly@yel@ recommended that you do so.\\n\\nIf you don't you will be @lre@unable to recover your\\n@lre@password@yel@ if you forget it, or it is stolen.";
            else if (recoveryQuestionDays <= lastLoginDays) {
                class13.aString230 = "\\n\\nRecovery Questions Last Set:\\n@gre@" + formatDate(recoveryQuestionDays);
            } else {
                int l1 = (lastLoginDays + 14) - recoveryQuestionDays;
                String s2;
                if (l1 <= 0)
                    s2 = "Earlier today";
                else if (l1 == 1)
                    s2 = "Yesterday";
                else
                    s2 = l1 + " days ago";
                class13.aString230 = s2
                        + " you requested@lre@ new recovery\\n@lre@questions.@yel@ The requested change will occur\\non: @lre@"
                        + formatDate(recoveryQuestionDays)
                        + "\\n\\nIf you do not remember making this request\\ncancel it immediately, and change your password.";
            }
        if (i == 662) {
            String s;
            if (unreadMessages == 0)
                s = "@yel@0 unread messages";
            else if (unreadMessages == 1)
                s = "@gre@1 unread message";
            else
                s = "@gre@" + unreadMessages + " unread messages";
            class13.aString230 = "You have " + s + "\\nin your message centre.";
        }
        if (i == 663)
            if (lastPasswordChange <= 0 || lastPasswordChange > lastLoginDays + 10)
                class13.aString230 = "Last password change:\\n@gre@Never changed";
            else
                class13.aString230 = "Last password change:\\n@gre@" + formatDate(lastPasswordChange);
        if (i == 665)
            if (membershipDaysRemaining > 2 && !memberServer)
                class13.aString230 = "This is a non-members\\nworld. To enjoy your\\nmembers benefits we\\nrecommend you play on a\\nmembers world instead.";
            else if (membershipDaysRemaining > 2)
                class13.aString230 = "\\n\\nYou have @gre@" + membershipDaysRemaining + "@yel@ days of\\nmember credit remaining.";
            else if (membershipDaysRemaining > 0)
                class13.aString230 = "You have @gre@"
                        + membershipDaysRemaining
                        + "@yel@ days of\\nmember credit remaining.\\n\\n@lre@Credit low! Renew now\\n@lre@to avoid losing members.";
            else
                class13.aString230 = "You are not a member.\\n\\nChoose to subscribe and\\nyou'll get loads of extra\\nbenefits and features.";
        if (i == 667)
            if (membershipDaysRemaining > 2 && !memberServer)
                class13.aString230 = "To switch to a members-only world:\\n1) Logout and return to the world selection page.\\n2) Choose one of the members world with a gold star next to it's name.\\n\\nIf you prefer you can continue to use this world,\\nbut members only features will be unavailable here.";
            else if (membershipDaysRemaining > 0)
                class13.aString230 = "To extend or cancel a subscription:\\n1) Logout and return to the frontpage of this website.\\n2)Choose the relevant option from the 'membership' section.\\n\\nNote: If you are a credit card subscriber a top-up payment will\\nautomatically be taken when 3 days credit remain.\\n(unless you cancel your subscription, which can be done at any time.)";
            else
                class13.aString230 = "To start a subscripton:\\n1) Logout and return to the frontpage of this website.\\n2) Choose 'Start a new subscription'";
        if (i == 668) {
            if (recoveryQuestionDays > lastLoginDays) {
                class13.aString230 = "To cancel this request:\\n1) Logout and return to the frontpage of this website.\\n2) Choose 'Cancel recovery questions'.";
                return;
            }
            class13.aString230 = "To change your recovery questions:\\n1) Logout and return to the frontpage of this website.\\n2) Choose 'Set new recovery questions'.";
        }
    }

    public String formatDate(int i) {
        if (i > lastLoginDays + 10) {
            return "Unknown";
        } else {
            long dayMs = 0x5265c00L;
            long totalMs = ((long) i + 11745L) * dayMs;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(totalMs));
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            String[] as = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            return day + "-" + as[month] + "-" + year;
        }
    }

    public void handleVarp(int id) {
        int varpType = Varp.varpTable[id].type;
        if (varpType == 0)
            return;
        int value = localVarps[id];
        if (varpType == 1) {
            if (value == 1)
                ThreeDimensionalCanvas.initColorTable(0.90000000000000002D, (byte) 6);
            if (value == 2)
                ThreeDimensionalCanvas.initColorTable(0.80000000000000004D, (byte) 6);
            if (value == 3)
                ThreeDimensionalCanvas.initColorTable(0.69999999999999996D, (byte) 6);
            if (value == 4)
                ThreeDimensionalCanvas.initColorTable(0.59999999999999998D, (byte) 6);
            ItemDefinition.spriteCache.clear();
            shouldRenderUI = true;
        }
        if (varpType == 3) {
            boolean wasMusicEnabled = musicEnabled;
            if (value == 0) {
                adjustVolume(musicEnabled, (byte) 8, 0);
                musicEnabled = true;
            }
            if (value == 1) {
                adjustVolume(musicEnabled, (byte) 8, -400);
                musicEnabled = true;
            }
            if (value == 2) {
                adjustVolume(musicEnabled, (byte) 8, -800);
                musicEnabled = true;
            }
            if (value == 3) {
                adjustVolume(musicEnabled, (byte) 8, -1200);
                musicEnabled = true;
            }
            if (value == 4)
                musicEnabled = false;
            if (musicEnabled != wasMusicEnabled && !lowMemory) {
                if (musicEnabled) {
                    anInt1270 = anInt1327;
                    aBoolean1271 = true;
                    fileFetcher.request(2, anInt1270);
                } else {
                    method50(false);
                }
                anInt1128 = 0;
            }
        }
        if (varpType == 4) {
            if (value == 0) {
                aBoolean1301 = true;
                method58(822, 0);
            }
            if (value == 1) {
                aBoolean1301 = true;
                method58(822, -400);
            }
            if (value == 2) {
                aBoolean1301 = true;
                method58(822, -800);
            }
            if (value == 3) {
                aBoolean1301 = true;
                method58(822, -1200);
            }
            if (value == 4)
                aBoolean1301 = false;
        }
        if (varpType == 5)
            anInt1300 = value;
        if (varpType == 6)
            anInt998 = value;
        if (varpType == 8) {
            splitPrivateChat = value;
            aBoolean1240 = true;
        }
        if (varpType == 9)
            anInt955 = value;
    }

    public int method106(int i, int j, int k, int l) {
        if (l < 8 || l > 8)
            outBuffer.putByte(235);
        int i1 = 256 - k;
        return ((i & 0xff00ff) * i1 + (j & 0xff00ff) * k & 0xff00ff00)
                + ((i & 0xff00) * i1 + (j & 0xff00) * k & 0xff0000) >> 8;
    }

    public void method107(int i) {
        anInt1246 = 0;
        int j = (((Actor) (thisPlayer)).unitX >> 7) + nextTopLeftTileX;
        int k;
        for (k = (((Actor) (thisPlayer)).unitY >> 7) + nextTopLeftTileY; i >= 0; )
            return;

        if (j >= 3053 && j <= 3156 && k >= 3056 && k <= 3136)
            anInt1246 = 1;
        if (j >= 3072 && j <= 3118 && k >= 9492 && k <= 9535)
            anInt1246 = 1;
        if (anInt1246 == 1 && j >= 3139 && j <= 3199 && k >= 3008 && k <= 3062)
            anInt1246 = 0;
    }

    public void method108(int i) {
        int j = loginScreenFont.method472((byte) 35, "Choose Option");
        for (int k = 0; k < anInt1183; k++) {
            int l = loginScreenFont.method472((byte) 35, rightClickOptions[k]);
            if (l > j)
                j = l;
        }

        j += 8;
        int i1 = 15 * anInt1183 + 21;
        if (layout.isInViewport(super.anInt29, super.anInt30)) {
            int j1 = super.anInt29 - layout.viewport.x - j / 2;
            if (j1 + j > layout.viewport.width)
                j1 = layout.viewport.width - j;
            if (j1 < 0)
                j1 = 0;
            int i2 = super.anInt30 - layout.viewport.y;
            if (i2 + i1 > layout.viewport.height)
                i2 = layout.viewport.height - i1;
            if (i2 < 0)
                i2 = 0;
            isContextMenuActive = true;
            anInt1304 = 0;
            anInt1305 = j1;
            anInt1306 = i2;
            anInt1307 = j;
            anInt1308 = 15 * anInt1183 + 22;
        }
        if (layout.isInInventory(super.anInt29, super.anInt30)) {
            int k1 = super.anInt29 - layout.inventory.x - j / 2;
            if (k1 < 0)
                k1 = 0;
            else if (k1 + j > 190)
                k1 = 190 - j;
            int j2 = super.anInt30 - layout.inventory.y;
            if (j2 < 0)
                j2 = 0;
            else if (j2 + i1 > 261)
                j2 = 261 - i1;
            isContextMenuActive = true;
            anInt1304 = 1;
            anInt1305 = k1;
            anInt1306 = j2;
            anInt1307 = j;
            anInt1308 = 15 * anInt1183 + 22;
        }
        if (layout.isInChatbox(super.anInt29, super.anInt30)) {
            int l1 = super.anInt29 - layout.chatbox.x - j / 2;
            if (l1 < 0)
                l1 = 0;
            else if (l1 + j > 479)
                l1 = 479 - j;
            int k2 = super.anInt30 - layout.chatbox.y;
            if (k2 < 0)
                k2 = 0;
            else if (k2 + i1 > 96)
                k2 = 96 - i1;
            isContextMenuActive = true;
            anInt1304 = 2;
            anInt1305 = l1;
            anInt1306 = k2;
            anInt1307 = j;
            anInt1308 = 15 * anInt1183 + 22;
        }
    }

    public void draw3dScreen() {
        drawPrivateChat();
        if (anInt1023 == 1)
            aClass50_Sub1_Sub1_Sub1Array896[anInt1022 / 100].method461(anInt1021 - 8 - layout.viewport.y, anInt1020 - 8 - layout.viewport.x, -488);
        if (anInt1023 == 2)
            aClass50_Sub1_Sub1_Sub1Array896[4 + anInt1022 / 100].method461(anInt1021 - 8 - layout.viewport.y, anInt1020 - 8 - layout.viewport.x, -488);
        if (walkableInterfaceId != -1) {
            updateInterfaceAnimations(anInt951, walkableInterfaceId);
            drawInterface(0, 0, JagInterface.forId(walkableInterfaceId), 0, 8);
        }
        if (anInt1169 != -1) {
            updateInterfaceAnimations(anInt951, anInt1169);
            drawInterface(0, 0, JagInterface.forId(anInt1169), 0, 8);
        }
        method107(-7);
        if (!isContextMenuActive) {
            generateContextOptions(-521);
            method34((byte) -79);
        } else if (anInt1304 == 0)
            drawContextMenu();
        if (anInt1319 == 1)
            aClass50_Sub1_Sub1_Sub1_1086.method461(296, 472, -488);
        if (fps) {
            char c = '\u01FB';
            int k = 20;
            int i1 = 0xffff00;
            if (super.fps < 30 && lowMemory)
                i1 = 0xff0000;
            if (super.fps < 20 && !lowMemory)
                i1 = 0xff0000;
            fontChatboxButtons.method469(true, "Fps:" + super.fps, i1, c, k);
            k += 15;
            Runtime runtime = Runtime.getRuntime();
            int j1 = (int) ((runtime.totalMemory() - runtime.freeMemory()) / 1024L);
            i1 = 0xffff00;
            if (j1 > 0x2000000 && lowMemory)
                i1 = 0xff0000;
            if (j1 > 0x4000000 && !lowMemory)
                i1 = 0xff0000;
            fontChatboxButtons.method469(true, "Mem:" + j1 + "k", 0xffff00, c, k);
            k += 15;
        }
        if (anInt1057 != 0) {
            int j = anInt1057 / 50;
            int l = j / 60;
            j %= 60;
            if (j < 10)
                fontChatboxButtons.drawString_474("System update in: " + l + ":0" + j, 2245, 4, 0xffff00, layout.privateChatY);
            else
                fontChatboxButtons.drawString_474("System update in: " + l + ":" + j, 2245, 4, 0xffff00, layout.privateChatY);
            anInt895++;
            if (anInt895 > 112) {
                anInt895 = 0;
                outBuffer.putOpcode(197);
                outBuffer.putInt(0);
            }
        }
    }

    public int getFloorDrawHeight(int i, int j, int k) {
        int l = j >> 7;
        int i1 = i >> 7;
        if (l < 0 || i1 < 0 || l > 103 || i1 > 103)
            return 0;
        int j1 = k;
        if (j1 < 3 && (aByteArrayArrayArray1125[1][l][i1] & 2) == 2)
            j1++;
        int k1 = j & 0x7f;
        int l1 = i & 0x7f;
        int i2 = intGroundArray[j1][l][i1] * (128 - k1) + intGroundArray[j1][l + 1][i1] * k1 >> 7;
        int j2 = intGroundArray[j1][l][i1 + 1] * (128 - k1) + intGroundArray[j1][l + 1][i1 + 1] * k1 >> 7;
        return i2 * (128 - l1) + j2 * l1 >> 7;
    }

    public AppletContext getAppletContext() {
        if (signlink.mainapp != null)
            return signlink.mainapp.getAppletContext();
        else
            return super.getAppletContext();
    }

    public void method111(int i) {
        i = 21 / i;
        if (splitPrivateChat == 0)
            return;
        int j = 0;
        if (anInt1057 != 0)
            j = 1;
        for (int k = 0; k < 100; k++)
            if (aStringArray1298[k] != null) {
                int l = anIntArray1296[k];
                String s = aStringArray1297[k];
                if (s != null && s.startsWith("@cr1@")) {
                    s = s.substring(5);
                }
                if (s != null && s.startsWith("@cr2@")) {
                    s = s.substring(5);
                }
                if ((l == 3 || l == 7) && (l == 7 || privateChatMode == 0 || privateChatMode == 1 && method148(13292, s))) {
                    int i1 = layout.privateChatY - j * 13;
                    if (super.mouseX > layout.viewport.x && super.mouseY - layout.viewport.y > i1 - 10 && super.mouseY - layout.viewport.y <= i1 + 3) {
                        int j1 = fontChatboxButtons.method472((byte) 35, "From:  " + s + aStringArray1298[k]) + 25;
                        if (j1 > 450)
                            j1 = 450;
                        if (super.mouseX < layout.viewport.x + j1) {
                            if (playerRights >= 1) {
                                rightClickOptions[anInt1183] = "Report abuse @whi@" + s;
                                anIntArray981[anInt1183] = 2507;
                                anInt1183++;
                            }
                            rightClickOptions[anInt1183] = "Add ignore @whi@" + s;
                            anIntArray981[anInt1183] = 2574;
                            anInt1183++;
                            rightClickOptions[anInt1183] = "Add friend @whi@" + s;
                            anIntArray981[anInt1183] = 2762;
                            anInt1183++;
                        }
                    }
                    if (++j >= 5)
                        return;
                }
                if ((l == 5 || l == 6) && privateChatMode < 2 && ++j >= 5)
                    return;
            }

    }

    public void method112(byte byte0, int i) {
        if (byte0 != 36)
            outBuffer.putByte(6);
        JagInterface class13 = JagInterface.forId(i);
        for (int j = 0; j < class13.anIntArray258.length; j++) {
            if (class13.anIntArray258[j] == -1)
                break;
            JagInterface class13_1 = JagInterface.forId(class13.anIntArray258[j]);
            if (class13_1.type == 1)
                method112((byte) 36, class13_1.id);
            class13_1.anInt235 = 0;
            class13_1.anInt227 = 0;
        }

    }

    public void method113(int i, int j, int k) {
        int l = 0;
        i = 44 / i;
        for (int i1 = 0; i1 < 100; i1++) {
            if (aStringArray1298[i1] == null)
                continue;
            int j1 = anIntArray1296[i1];
            int k1 = (70 - l * 14) + anInt851 + 4;
            if (k1 < -20)
                break;
            String s = aStringArray1297[i1];
            if (s != null && s.startsWith("@cr1@")) {
                s = s.substring(5);
            }
            if (s != null && s.startsWith("@cr2@")) {
                s = s.substring(5);
            }
            if (j1 == 0)
                l++;
            if ((j1 == 1 || j1 == 2) && (j1 == 1 || publicChatMode == 0 || publicChatMode == 1 && method148(13292, s))) {
                if (k > k1 - 14 && k <= k1 && !s.equals(thisPlayer.username)) {
                    if (playerRights >= 1) {
                        rightClickOptions[anInt1183] = "Report abuse @whi@" + s;
                        anIntArray981[anInt1183] = 507;
                        anInt1183++;
                    }
                    rightClickOptions[anInt1183] = "Add ignore @whi@" + s;
                    anIntArray981[anInt1183] = 574;
                    anInt1183++;
                    rightClickOptions[anInt1183] = "Add friend @whi@" + s;
                    anIntArray981[anInt1183] = 762;
                    anInt1183++;
                }
                l++;
            }
            if ((j1 == 3 || j1 == 7) && splitPrivateChat == 0
                    && (j1 == 7 || privateChatMode == 0 || privateChatMode == 1 && method148(13292, s))) {
                if (k > k1 - 14 && k <= k1) {
                    if (playerRights >= 1) {
                        rightClickOptions[anInt1183] = "Report abuse @whi@" + s;
                        anIntArray981[anInt1183] = 507;
                        anInt1183++;
                    }
                    rightClickOptions[anInt1183] = "Add ignore @whi@" + s;
                    anIntArray981[anInt1183] = 574;
                    anInt1183++;
                    rightClickOptions[anInt1183] = "Add friend @whi@" + s;
                    anIntArray981[anInt1183] = 762;
                    anInt1183++;
                }
                l++;
            }
            if (j1 == 4 && (tradeMode == 0 || tradeMode == 1 && method148(13292, s))) {
                if (k > k1 - 14 && k <= k1) {
                    rightClickOptions[anInt1183] = "Accept trade @whi@" + s;
                    anIntArray981[anInt1183] = 544;
                    anInt1183++;
                }
                l++;
            }
            if ((j1 == 5 || j1 == 6) && splitPrivateChat == 0 && privateChatMode < 2)
                l++;
            if (j1 == 8 && (tradeMode == 0 || tradeMode == 1 && method148(13292, s))) {
                if (k > k1 - 14 && k <= k1) {
                    rightClickOptions[anInt1183] = "Accept challenge @whi@" + s;
                    anIntArray981[anInt1183] = 695;
                    anInt1183++;
                }
                l++;
            }
        }

    }

    public void updateOtherPlayerMovement(int packetSize, JagBuffer buffer) {
        int playerCount = buffer.getBits(8);
        if (playerCount < localPlayerCount) {
            for (int l = playerCount; l < localPlayerCount; l++)
                removePlayers[removePlayerCount++] = localPlayers[l];

        }
        if (playerCount > localPlayerCount) {
            signlink.reporterror(thisPlayerName + " Too many players");
            throw new RuntimeException("eek");
        }
        localPlayerCount = 0;
        for (int i = 0; i < playerCount; i++) {
            int id = localPlayers[i];
            Player plr = players[id];
            int updated = buffer.getBits(1);
            if (updated == 0) {
                localPlayers[localPlayerCount++] = id;
                plr.pulseCycle = pulseCycle;
            } else {
                int moveType = buffer.getBits(2);
                if (moveType == 0) {
                    localPlayers[localPlayerCount++] = id;
                    plr.pulseCycle = pulseCycle;
                    updatedPlayers[updatedPlayerCount++] = id;
                } else if (moveType == 1) {
                    localPlayers[localPlayerCount++] = id;
                    plr.pulseCycle = pulseCycle;
                    int direction = buffer.getBits(3);
                    plr.addStep(direction, false);
                    int blockUpdateRequired = buffer.getBits(1);
                    if (blockUpdateRequired == 1)
                        updatedPlayers[updatedPlayerCount++] = id;
                } else if (moveType == 2) {
                    localPlayers[localPlayerCount++] = id;
                    plr.pulseCycle = pulseCycle;
                    int direction1 = buffer.getBits(3);
                    plr.addStep(direction1, true);
                    int direction2 = buffer.getBits(3);
                    plr.addStep(direction2, true);
                    int updateRequired = buffer.getBits(1);
                    if (updateRequired == 1)
                        updatedPlayers[updatedPlayerCount++] = id;
                } else if (moveType == 3)
                    removePlayers[removePlayerCount++] = id;
            }
        }

    }

    public void method115(int i, int j) {
        int ai[] = rbgSprite_1122.pixels_1489;
        int k = ai.length;
        for (int l = 0; l < k; l++)
            ai[l] = 0;

        for (int i1 = 1; i1 < 103; i1++) {
            int j1 = 24628 + (103 - i1) * 512 * 4;
            for (int l1 = 1; l1 < 103; l1++) {
                if ((aByteArrayArrayArray1125[i][l1][i1] & 0x18) == 0)
                    sceneGraph.method276(ai, j1, 512, i, l1, i1);
                if (i < 3 && (aByteArrayArrayArray1125[i + 1][l1][i1] & 8) != 0)
                    sceneGraph.method276(ai, j1, 512, i + 1, l1, i1);
                j1 += 4;
            }

        }

        int k1 = ((238 + (int) (Math.random() * 20D)) - 10 << 16) + ((238 + (int) (Math.random() * 20D)) - 10 << 8)
                + ((238 + (int) (Math.random() * 20D)) - 10);
        if (j != 0)
            opcode = buffer.getByte();
        int i2 = (238 + (int) (Math.random() * 20D)) - 10 << 16;
        rbgSprite_1122.method456(false);
        for (int j2 = 1; j2 < 103; j2++) {
            for (int k2 = 1; k2 < 103; k2++) {
                if ((aByteArrayArrayArray1125[i][k2][j2] & 0x18) == 0)
                    method150(j2, i, k2, i2, 563, k1);
                if (i < 3 && (aByteArrayArrayArray1125[i + 1][k2][j2] & 8) != 0)
                    method150(j2, i + 1, k2, i2, 563, k1);
            }

        }

        if (gameViewportImage != null) {
            gameViewportImage.pushPixels();
            ThreeDimensionalCanvas.lineOffsets = gameViewportOffsets;
        }
        anInt1082++;
        if (anInt1082 > 177) {
            anInt1082 = 0;
            outBuffer.putOpcode(173);
            outBuffer.putTriByte(0x288b80);
        }
        anInt1076 = 0;
        for (int l2 = 0; l2 < 104; l2++) {
            for (int i3 = 0; i3 < 104; i3++) {
                int j3 = sceneGraph.method270(plane, l2, i3);
                if (j3 != 0) {
                    j3 = j3 >> 14 & 0x7fff;
                    int k3 = ObjectDefinition.forId(j3).anInt806;
                    if (k3 >= 0) {
                        int l3 = l2;
                        int i4 = i3;
                        if (k3 != 22 && k3 != 29 && k3 != 34 && k3 != 36 && k3 != 46 && k3 != 47 && k3 != 48) {
                            byte byte0 = 104;
                            byte byte1 = 104;
                            int ai1[][] = clippingPlanes[plane].masks;
                            for (int j4 = 0; j4 < 10; j4++) {
                                int k4 = (int) (Math.random() * 4D);
                                if (k4 == 0 && l3 > 0 && l3 > l2 - 3 && (ai1[l3 - 1][i4] & 0x1280108) == 0)
                                    l3--;
                                if (k4 == 1 && l3 < byte0 - 1 && l3 < l2 + 3 && (ai1[l3 + 1][i4] & 0x1280180) == 0)
                                    l3++;
                                if (k4 == 2 && i4 > 0 && i4 > i3 - 3 && (ai1[l3][i4 - 1] & 0x1280102) == 0)
                                    i4--;
                                if (k4 == 3 && i4 < byte1 - 1 && i4 < i3 + 3 && (ai1[l3][i4 + 1] & 0x1280120) == 0)
                                    i4++;
                            }

                        }
                        aClass50_Sub1_Sub1_Sub1Array1278[anInt1076] = aClass50_Sub1_Sub1_Sub1Array1031[k3];
                        anIntArray1077[anInt1076] = l3;
                        anIntArray1078[anInt1076] = i4;
                        anInt1076++;
                    }
                }
            }

        }

    }

    public boolean method116(int i, int j, byte abyte0[]) {
        if (i < 3 || i > 3)
            throw new NullPointerException();
        if (abyte0 == null)
            return true;
        else
            return signlink.wavesave(abyte0, j);
    }

    public int method117(byte byte0) {
        int i = 3;
        if (byte0 == aByte956)
            byte0 = 0;
        else
            load();
        if (anInt1219 < 310) {
            anInt978++;
            if (anInt978 > 1457) {
                anInt978 = 0;
                outBuffer.putOpcode(244);
                outBuffer.putByte(0);
                int j = outBuffer.position;
                outBuffer.putByte(219);
                outBuffer.putShort(37745);
                outBuffer.putByte(61);
                outBuffer.putShort(43756);
                outBuffer.putShort((int) (Math.random() * 65536D));
                outBuffer.putByte((int) (Math.random() * 256D));
                outBuffer.putShort(51171);
                if ((int) (Math.random() * 2D) == 0)
                    outBuffer.putShort(15808);
                outBuffer.putByte(97);
                outBuffer.putByte((int) (Math.random() * 256D));
                outBuffer.putLength(outBuffer.position - j);
            }
            int k = anInt1216 >> 7;
            int l = anInt1218 >> 7;
            int i1 = ((Actor) (thisPlayer)).unitX >> 7;
            int j1 = ((Actor) (thisPlayer)).unitY >> 7;
            if ((aByteArrayArrayArray1125[plane][k][l] & 4) != 0)
                i = plane;
            int k1;
            if (i1 > k)
                k1 = i1 - k;
            else
                k1 = k - i1;
            int l1;
            if (j1 > l)
                l1 = j1 - l;
            else
                l1 = l - j1;
            if (k1 > l1) {
                int i2 = (l1 * 0x10000) / k1;
                int k2 = 32768;
                while (k != i1) {
                    if (k < i1)
                        k++;
                    else if (k > i1)
                        k--;
                    if ((aByteArrayArrayArray1125[plane][k][l] & 4) != 0)
                        i = plane;
                    k2 += i2;
                    if (k2 >= 0x10000) {
                        k2 -= 0x10000;
                        if (l < j1)
                            l++;
                        else if (l > j1)
                            l--;
                        if ((aByteArrayArrayArray1125[plane][k][l] & 4) != 0)
                            i = plane;
                    }
                }
            } else {
                int j2 = (k1 * 0x10000) / l1;
                int l2 = 32768;
                while (l != j1) {
                    if (l < j1)
                        l++;
                    else if (l > j1)
                        l--;
                    if ((aByteArrayArrayArray1125[plane][k][l] & 4) != 0)
                        i = plane;
                    l2 += j2;
                    if (l2 >= 0x10000) {
                        l2 -= 0x10000;
                        if (k < i1)
                            k++;
                        else if (k > i1)
                            k--;
                        if ((aByteArrayArrayArray1125[plane][k][l] & 4) != 0)
                            i = plane;
                    }
                }
            }
        }
        if ((aByteArrayArrayArray1125[plane][((Actor) (thisPlayer)).unitX >> 7][((Actor) (thisPlayer)).unitY >> 7] & 4) != 0)
            i = plane;
        return i;
    }

    public int method118(int i) {
        int j = getFloorDrawHeight(anInt1218, anInt1216, plane);
        while (i >= 0)
            opcode = buffer.getByte();
        if (j - anInt1217 < 800 && (aByteArrayArrayArray1125[plane][anInt1216 >> 7][anInt1218 >> 7] & 4) != 0)
            return plane;
        else
            return 3;
    }

    public void startThread(Runnable runnable, int priority) {
        if (priority > 10)
            priority = 10;
        if (signlink.mainapp != null) {
            signlink.startthread(runnable, priority);
            return;
        } else {
            super.startThread(runnable, priority);
            return;
        }
    }

    public void addPlayersToSceneGraph(boolean flag) {
        if (((Actor) (thisPlayer)).unitX >> 7 == anInt1120
                && ((Actor) (thisPlayer)).unitY >> 7 == anInt1121)
            anInt1120 = 0;
        int j = localPlayerCount;
        if (flag)
            j = 1;
        for (int k = 0; k < j; k++) {
            Player class50_sub1_sub4_sub3_sub2;
            int l;
            if (flag) {
                class50_sub1_sub4_sub3_sub2 = thisPlayer;
                l = thisPlayerId << 14;
            } else {
                class50_sub1_sub4_sub3_sub2 = players[localPlayers[k]];
                l = localPlayers[k] << 14;
            }
            if (class50_sub1_sub4_sub3_sub2 == null || !class50_sub1_sub4_sub3_sub2.isVisible())
                continue;
            class50_sub1_sub4_sub3_sub2.aBoolean1763 = false;
            if ((lowMemory && localPlayerCount > 50 || localPlayerCount > 200)
                    && !flag
                    && ((Actor) (class50_sub1_sub4_sub3_sub2)).anInt1588 == ((Actor) (class50_sub1_sub4_sub3_sub2)).anInt1634)
                class50_sub1_sub4_sub3_sub2.aBoolean1763 = true;
            int i1 = ((Actor) (class50_sub1_sub4_sub3_sub2)).unitX >> 7;
            int j1 = ((Actor) (class50_sub1_sub4_sub3_sub2)).unitY >> 7;
            if (i1 < 0 || i1 >= 104 || j1 < 0 || j1 >= 104)
                continue;
            if (class50_sub1_sub4_sub3_sub2.aClass50_Sub1_Sub4_Sub4_1746 != null
                    && pulseCycle >= class50_sub1_sub4_sub3_sub2.anInt1764
                    && pulseCycle < class50_sub1_sub4_sub3_sub2.anInt1765) {
                class50_sub1_sub4_sub3_sub2.aBoolean1763 = false;
                class50_sub1_sub4_sub3_sub2.anInt1750 = getFloorDrawHeight(
                        ((Actor) (class50_sub1_sub4_sub3_sub2)).unitY,
                        ((Actor) (class50_sub1_sub4_sub3_sub2)).unitX, plane);
                sceneGraph.method253(class50_sub1_sub4_sub3_sub2.anInt1750, class50_sub1_sub4_sub3_sub2.anInt1769,
                        60, 7, class50_sub1_sub4_sub3_sub2, class50_sub1_sub4_sub3_sub2.anInt1768,
                        ((Actor) (class50_sub1_sub4_sub3_sub2)).unitY, class50_sub1_sub4_sub3_sub2.anInt1771,
                        ((Actor) (class50_sub1_sub4_sub3_sub2)).unitX,
                        ((Actor) (class50_sub1_sub4_sub3_sub2)).anInt1612, class50_sub1_sub4_sub3_sub2.anInt1770,
                        plane, l);
                continue;
            }
            if ((((Actor) (class50_sub1_sub4_sub3_sub2)).unitX & 0x7f) == 64
                    && (((Actor) (class50_sub1_sub4_sub3_sub2)).unitY & 0x7f) == 64) {
                if (anIntArrayArray886[i1][j1] == tickCounter1138)
                    continue;
                anIntArrayArray886[i1][j1] = tickCounter1138;
            }
            class50_sub1_sub4_sub3_sub2.anInt1750 = getFloorDrawHeight(((Actor) (class50_sub1_sub4_sub3_sub2)).unitY,
                    ((Actor) (class50_sub1_sub4_sub3_sub2)).unitX, plane);
            sceneGraph.method252(l, class50_sub1_sub4_sub3_sub2,
                    ((Actor) (class50_sub1_sub4_sub3_sub2)).unitX, class50_sub1_sub4_sub3_sub2.anInt1750,
                    ((Actor) (class50_sub1_sub4_sub3_sub2)).aBoolean1592, 0, plane, 60,
                    ((Actor) (class50_sub1_sub4_sub3_sub2)).unitY,
                    ((Actor) (class50_sub1_sub4_sub3_sub2)).anInt1612);
        }
    }

    public void sendOutgoingPackets(int i, int j) {
        if (i < 0)
            return;
        int slot = anIntArray979[i];
        int interfaceId = anIntArray980[i];
        int i1 = anIntArray981[i];
        int id = anIntArray982[i];
        if (j < anInt921 || j > anInt921)
            opcode = buffer.getByte();
        if (i1 >= 2000)
            i1 -= 2000;
        if (chatboxInterfaceType != 0 && i1 != 1016) {
            chatboxInterfaceType = 0;
            aBoolean1240 = true;
        }
        if (i1 == 200) {
            Player class50_sub1_sub4_sub3_sub2 = players[id];
            if (class50_sub1_sub4_sub3_sub2 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub2)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub2)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                outBuffer.putOpcode(245);
                outBuffer.putLEShortAdded(id);
            }
        }
        if (i1 == 227) {
            anInt1165++;
            if (anInt1165 >= 62) {
                outBuffer.putOpcode(165);
                outBuffer.putByte(206);
                anInt1165 = 0;
            }
            outBuffer.putOpcode(228);
            outBuffer.putLEShortDup(slot);
            outBuffer.putShortAdded(id);
            outBuffer.putShort(interfaceId);
            anInt1329 = 0;
            anInt1330 = interfaceId;
            anInt1331 = slot;
            anInt1332 = 2;
            if (JagInterface.forId(interfaceId).anInt248 == anInt1169)
                anInt1332 = 1;
            if (JagInterface.forId(interfaceId).anInt248 == anInt988)
                anInt1332 = 3;
        }
        if (i1 == 876) {
            Player class50_sub1_sub4_sub3_sub2_1 = players[id];
            if (class50_sub1_sub4_sub3_sub2_1 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub2_1)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub2_1)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                outBuffer.putOpcode(45);
                outBuffer.putShortAdded(id);
            }
        }
        if (i1 == 921) {
            Npc class50_sub1_sub4_sub3_sub1 = npcs[id];
            if (class50_sub1_sub4_sub3_sub1 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub1)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub1)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                outBuffer.putOpcode(67);
                outBuffer.putShortAdded(id);
            }
        }
        if (i1 == 961) {
            anInt1139 += id;
            if (anInt1139 >= 115) {
                outBuffer.putOpcode(126);
                outBuffer.putByte(125);
                anInt1139 = 0;
            }
            outBuffer.putOpcode(203);
            outBuffer.putShortAdded(interfaceId);
            outBuffer.putLEShortDup(slot);
            outBuffer.putLEShortDup(id);
            anInt1329 = 0;
            anInt1330 = interfaceId;
            anInt1331 = slot;
            anInt1332 = 2;
            if (JagInterface.forId(interfaceId).anInt248 == anInt1169)
                anInt1332 = 1;
            if (JagInterface.forId(interfaceId).anInt248 == anInt988)
                anInt1332 = 3;
        }
        if (i1 == 467 && method80(interfaceId, 0, slot, id)) {
            outBuffer.putOpcode(152);
            outBuffer.putLEShortDup(id >> 14 & 0x7fff);
            outBuffer.putLEShortDup(itemInterfaceId);
            outBuffer.putLEShortDup(itemId);
            outBuffer.putLEShortDup(interfaceId + nextTopLeftTileY);
            outBuffer.putShort(itemIndexId);
            outBuffer.putLEShortAdded(slot + nextTopLeftTileX);
        }
        if (i1 == 9) {
            outBuffer.putOpcode(3);
            outBuffer.putShortAdded(id);
            outBuffer.putShort(interfaceId);
            outBuffer.putShort(slot);
            anInt1329 = 0;
            anInt1330 = interfaceId;
            anInt1331 = slot;
            anInt1332 = 2;
            if (JagInterface.forId(interfaceId).anInt248 == anInt1169)
                anInt1332 = 1;
            if (JagInterface.forId(interfaceId).anInt248 == anInt988)
                anInt1332 = 3;
        }
        if (i1 == 553) {
            Npc class50_sub1_sub4_sub3_sub1_1 = npcs[id];
            if (class50_sub1_sub4_sub3_sub1_1 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub1_1)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub1_1)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                outBuffer.putOpcode(42);
                outBuffer.putLEShortDup(id);
            }
        }
        if (i1 == 677) {
            Player class50_sub1_sub4_sub3_sub2_2 = players[id];
            if (class50_sub1_sub4_sub3_sub2_2 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub2_2)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub2_2)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                outBuffer.putOpcode(116);
                outBuffer.putLEShortDup(id);
            }
        }
        if (i1 == 762 || i1 == 574 || i1 == 775 || i1 == 859) {
            String s = rightClickOptions[i];
            int l1 = s.indexOf("@whi@");
            if (l1 != -1) {
                long l3 = StringUtils.encodeBase37(s.substring(l1 + 5).trim());
                if (i1 == 762)
                    addFriend(l3, -45229);
                if (i1 == 574)
                    method90(anInt1154, l3);
                if (i1 == 775)
                    method53(l3, 0);
                if (i1 == 859)
                    removeIgnore(325, l3);
            }
        }
        if (i1 == 930) {
            boolean flag = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 0, 0, 2, 0, slot, 0, 0,
                    ((Actor) (thisPlayer)).walkingQueueX[0]);
            if (!flag)
                flag = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0, slot, 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
            anInt1020 = super.anInt29;
            anInt1021 = super.anInt30;
            anInt1023 = 2;
            anInt1022 = 0;
            outBuffer.putOpcode(54);
            outBuffer.putShortAdded(id);
            outBuffer.putLEShortDup(interfaceId + nextTopLeftTileY);
            outBuffer.putShort(slot + nextTopLeftTileX);
        }
        if (i1 == 399) {
            outBuffer.putOpcode(24);
            outBuffer.putLEShortDup(interfaceId);
            outBuffer.putLEShortDup(id);
            outBuffer.putShortAdded(slot);
            anInt1329 = 0;
            anInt1330 = interfaceId;
            anInt1331 = slot;
            anInt1332 = 2;
            if (JagInterface.forId(interfaceId).anInt248 == anInt1169)
                anInt1332 = 1;
            if (JagInterface.forId(interfaceId).anInt248 == anInt988)
                anInt1332 = 3;
        }
        if (i1 == 347) {
            Npc class50_sub1_sub4_sub3_sub1_2 = npcs[id];
            if (class50_sub1_sub4_sub3_sub1_2 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub1_2)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub1_2)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                outBuffer.putOpcode(57);
                outBuffer.putShort(id);
                outBuffer.putLEShortDup(itemId);
                outBuffer.putLEShortAdded(itemInterfaceId);
                outBuffer.putShort(itemIndexId);
            }
        }
        if (i1 == 890) {
            outBuffer.putOpcode(79);
            outBuffer.putShort(interfaceId);
            JagInterface inter = JagInterface.forId(interfaceId);
            if (inter.anIntArrayArray234 != null && inter.anIntArrayArray234[0][0] == 5) {
                int i2 = inter.anIntArrayArray234[0][1];
                localVarps[i2] = 1 - localVarps[i2];
                handleVarp(i2);
                aBoolean1181 = true;
            }
        }
        if (i1 == 493) {
            Player class50_sub1_sub4_sub3_sub2_3 = players[id];
            if (class50_sub1_sub4_sub3_sub2_3 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub2_3)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub2_3)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                outBuffer.putOpcode(233);
                outBuffer.putShortAdded(id);
            }
        }
        if (i1 == 14)
            if (!isContextMenuActive)
                sceneGraph.method279(0, super.anInt29 - layout.viewport.x, super.anInt30 - layout.viewport.y);
            else
                sceneGraph.method279(0, slot - layout.viewport.x, interfaceId - layout.viewport.y);
        if (i1 == 903) {
            outBuffer.putOpcode(1);
            outBuffer.putShort(id);
            outBuffer.putLEShortDup(itemIndexId);
            outBuffer.putLEShortDup(itemId);
            outBuffer.putLEShortAdded(itemInterfaceId);
            outBuffer.putShortAdded(slot);
            outBuffer.putShortAdded(interfaceId);
            anInt1329 = 0;
            anInt1330 = interfaceId;
            anInt1331 = slot;
            anInt1332 = 2;
            if (JagInterface.forId(interfaceId).anInt248 == anInt1169)
                anInt1332 = 1;
            if (JagInterface.forId(interfaceId).anInt248 == anInt988)
                anInt1332 = 3;
        }
        if (i1 == 361) {
            outBuffer.putOpcode(36);
            outBuffer.putShort(spellId);
            outBuffer.putShortAdded(interfaceId);
            outBuffer.putShortAdded(slot);
            outBuffer.putShortAdded(id);
            anInt1329 = 0;
            anInt1330 = interfaceId;
            anInt1331 = slot;
            anInt1332 = 2;
            if (JagInterface.forId(interfaceId).anInt248 == anInt1169)
                anInt1332 = 1;
            if (JagInterface.forId(interfaceId).anInt248 == anInt988)
                anInt1332 = 3;
        }
        if (i1 == 118) {
            Npc class50_sub1_sub4_sub3_sub1_3 = npcs[id];
            if (class50_sub1_sub4_sub3_sub1_3 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub1_3)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub1_3)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                anInt1235 += id;
                if (anInt1235 >= 143) {
                    outBuffer.putOpcode(157);
                    outBuffer.putInt(0);
                    anInt1235 = 0;
                }
                outBuffer.putOpcode(13);
                outBuffer.putLEShortAdded(id);
            }
        }
        if (i1 == 376 && method80(interfaceId, 0, slot, id)) {
            outBuffer.putOpcode(210);
            outBuffer.putShort(spellId);
            outBuffer.putLEShortDup(id >> 14 & 0x7fff);
            outBuffer.putShortAdded(slot + nextTopLeftTileX);
            outBuffer.putLEShortDup(interfaceId + nextTopLeftTileY);
        }
        if (i1 == 432) {
            Npc class50_sub1_sub4_sub3_sub1_4 = npcs[id];
            if (class50_sub1_sub4_sub3_sub1_4 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub1_4)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub1_4)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                outBuffer.putOpcode(8);
                outBuffer.putLEShortDup(id);
            }
        }
        if (i1 == 639)
            method15(false);
        if (i1 == 918) {
            Player class50_sub1_sub4_sub3_sub2_4 = players[id];
            if (class50_sub1_sub4_sub3_sub2_4 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub2_4)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub2_4)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                outBuffer.putOpcode(31);
                outBuffer.putShort(id);
                outBuffer.putLEShortDup(spellId);
            }
        }
        if (i1 == 67) {
            Npc class50_sub1_sub4_sub3_sub1_5 = npcs[id];
            if (class50_sub1_sub4_sub3_sub1_5 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub1_5)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub1_5)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                outBuffer.putOpcode(104);
                outBuffer.putShortAdded(spellId);
                outBuffer.putLEShortDup(id);
            }
        }
        if (i1 == 68) {
            boolean flag1 = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 0, 0, 2, 0, slot, 0, 0,
                    ((Actor) (thisPlayer)).walkingQueueX[0]);
            if (!flag1)
                flag1 = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0, slot, 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
            anInt1020 = super.anInt29;
            anInt1021 = super.anInt30;
            anInt1023 = 2;
            anInt1022 = 0;
            outBuffer.putOpcode(77);
            outBuffer.putShortAdded(slot + nextTopLeftTileX);
            outBuffer.putShort(interfaceId + nextTopLeftTileY);
            outBuffer.putLEShortAdded(id);
        }
        if (i1 == 684) {
            boolean flag2 = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 0, 0, 2, 0, slot, 0, 0,
                    ((Actor) (thisPlayer)).walkingQueueX[0]);
            if (!flag2)
                flag2 = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0, slot, 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
            anInt1020 = super.anInt29;
            anInt1021 = super.anInt30;
            anInt1023 = 2;
            anInt1022 = 0;
            if ((id & 3) == 0)
                anInt1052++;
            if (anInt1052 >= 84) {
                outBuffer.putOpcode(222);
                outBuffer.putTriByte(0xabc842);
                anInt1052 = 0;
            }
            outBuffer.putOpcode(71);
            outBuffer.putLEShortAdded(id);
            outBuffer.putLEShortAdded(slot + nextTopLeftTileX);
            outBuffer.putShortAdded(interfaceId + nextTopLeftTileY);
        }
        if (i1 == 544 || i1 == 695) {
            String s1 = rightClickOptions[i];
            int j2 = s1.indexOf("@whi@");
            if (j2 != -1) {
                s1 = s1.substring(j2 + 5).trim();
                String s7 = StringUtils.formatPlayerName(StringUtils.decodeBase37(StringUtils.encodeBase37(s1)));
                boolean flag8 = false;
                for (int j3 = 0; j3 < localPlayerCount; j3++) {
                    Player player = players[localPlayers[j3]];
                    if (player == null || player.username == null
                            || !player.username.equalsIgnoreCase(s7))
                        continue;
                    walk(false, false, ((Actor) (player)).walkingQueueY[0],
                            ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                            ((Actor) (player)).walkingQueueX[0], 0, 0,
                            ((Actor) (thisPlayer)).walkingQueueX[0]);
                    if (i1 == 544) {
                        outBuffer.putOpcode(116);
                        outBuffer.putLEShortDup(localPlayers[j3]);
                    }
                    if (i1 == 695) {
                        outBuffer.putOpcode(245);
                        outBuffer.putLEShortAdded(localPlayers[j3]);
                    }
                    flag8 = true;
                    break;
                }

                if (!flag8)
                    pushMessage("", (byte) -123, "Unable to find " + s7, 0);
            }
        }
        if (i1 == 225) {
            outBuffer.putOpcode(177); // second item action
            outBuffer.putShortAdded(slot);
            outBuffer.putLEShortDup(id);
            outBuffer.putLEShortDup(interfaceId);
            anInt1329 = 0;
            anInt1330 = interfaceId;
            anInt1331 = slot;
            anInt1332 = 2;
            if (JagInterface.forId(interfaceId).anInt248 == anInt1169)
                anInt1332 = 1;
            if (JagInterface.forId(interfaceId).anInt248 == anInt988)
                anInt1332 = 3;
        }
        if (i1 == 70) {
            JagInterface class13_1 = JagInterface.forId(interfaceId);
            anInt1171 = 1;
            spellId = interfaceId;
            anInt1173 = class13_1.anInt222;
            anInt1146 = 0;
            aBoolean1181 = true;
            String s4 = class13_1.aString281;
            if (s4.indexOf(" ") != -1)
                s4 = s4.substring(0, s4.indexOf(" "));
            String s8 = class13_1.aString281;
            if (s8.indexOf(" ") != -1)
                s8 = s8.substring(s8.indexOf(" ") + 1);
            aString1174 = s4 + " " + class13_1.aString211 + " " + s8;
            if (anInt1173 == 16) {
                aBoolean1181 = true;
                tabId = 3;
                aBoolean950 = true;
            }
            return;
        }
        if (i1 == 891) {
            outBuffer.putOpcode(4);
            outBuffer.putLEShortDup(slot);
            outBuffer.putLEShortAdded(id);
            outBuffer.putLEShortAdded(interfaceId);
            anInt1329 = 0;
            anInt1330 = interfaceId;
            anInt1331 = slot;
            anInt1332 = 2;
            if (JagInterface.forId(interfaceId).anInt248 == anInt1169)
                anInt1332 = 1;
            if (JagInterface.forId(interfaceId).anInt248 == anInt988)
                anInt1332 = 3;
        }
        if (i1 == 894) {
            outBuffer.putOpcode(158); // fifth item action event
            outBuffer.putLEShortAdded(slot);
            outBuffer.putLEShortAdded(id);
            outBuffer.putLEShortDup(interfaceId);
            anInt1329 = 0;
            anInt1330 = interfaceId;
            anInt1331 = slot;
            anInt1332 = 2;
            if (JagInterface.forId(interfaceId).anInt248 == anInt1169)
                anInt1332 = 1;
            if (JagInterface.forId(interfaceId).anInt248 == anInt988)
                anInt1332 = 3;
        }
        if (i1 == 1280) {
            method80(interfaceId, 0, slot, id);
            outBuffer.putOpcode(55);
            outBuffer.putLEShortDup(id >> 14 & 0x7fff);
            outBuffer.putLEShortDup(interfaceId + nextTopLeftTileY);
            outBuffer.putShort(slot + nextTopLeftTileX);
        }
        if (i1 == 35) {
            method80(interfaceId, 0, slot, id);
            outBuffer.putOpcode(181);
            outBuffer.putShortAdded(slot + nextTopLeftTileX);
            outBuffer.putLEShortDup(interfaceId + nextTopLeftTileY);
            outBuffer.putLEShortDup(id >> 14 & 0x7fff);
        }
        if (i1 == 888) {
            method80(interfaceId, 0, slot, id);
            outBuffer.putOpcode(50);
            outBuffer.putShortAdded(interfaceId + nextTopLeftTileY);
            outBuffer.putLEShortDup(id >> 14 & 0x7fff);
            outBuffer.putLEShortAdded(slot + nextTopLeftTileX);
        }
        if (i1 == 324) {
            outBuffer.putOpcode(161);
            outBuffer.putLEShortAdded(slot);
            outBuffer.putLEShortAdded(id);
            outBuffer.putLEShortDup(interfaceId);
            anInt1329 = 0;
            anInt1330 = interfaceId;
            anInt1331 = slot;
            anInt1332 = 2;
            if (JagInterface.forId(interfaceId).anInt248 == anInt1169)
                anInt1332 = 1;
            if (JagInterface.forId(interfaceId).anInt248 == anInt988)
                anInt1332 = 3;
        }
        if (i1 == 1094) {
            ItemDefinition class16 = ItemDefinition.forId(id);
            JagInterface class13_4 = JagInterface.forId(interfaceId);
            String s5;
            if (class13_4 != null && class13_4.itemAmounts[slot] >= 0x186a0)
                s5 = class13_4.itemAmounts[slot] + " x " + class16.name;
            else if (class16.description != null)
                s5 = new String(class16.description);
            else
                s5 = "It's a " + class16.name + ".";
            pushMessage("", (byte) -123, s5, 0);
        }
        if (i1 == 352) {
            JagInterface class13_2 = JagInterface.forId(interfaceId);
            boolean flag7 = true;
            if (class13_2.anInt242 > 0)
                flag7 = method60(631, class13_2);
            if (flag7) {
                outBuffer.putOpcode(79);
                outBuffer.putShort(interfaceId);
            }
        }
        if (i1 == 1412) {
            int k1 = id >> 14 & 0x7fff;
            ObjectDefinition class47 = ObjectDefinition.forId(k1);
            String s9;
            if (class47.description != null)
                s9 = new String(class47.description);
            else
                s9 = "It's a " + class47.name + ".";
            pushMessage("", (byte) -123, s9, 0);
        }
        if (i1 == 575 && !aBoolean1239) {
            outBuffer.putOpcode(226);
            outBuffer.putShort(interfaceId);
            aBoolean1239 = true;
        }
        if (i1 == 892) {
            method80(interfaceId, 0, slot, id);
            outBuffer.putOpcode(136);
            outBuffer.putShort(slot + nextTopLeftTileX);
            outBuffer.putLEShortDup(interfaceId + nextTopLeftTileY);
            outBuffer.putShort(id >> 14 & 0x7fff);
        }
        if (i1 == 270) {
            boolean flag3 = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 0, 0, 2, 0, slot, 0, 0,
                    ((Actor) (thisPlayer)).walkingQueueX[0]);
            if (!flag3)
                flag3 = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0, slot, 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
            anInt1020 = super.anInt29;
            anInt1021 = super.anInt30;
            anInt1023 = 2;
            anInt1022 = 0;
            outBuffer.putOpcode(230);
            outBuffer.putLEShortDup(id);
            outBuffer.putShortAdded(slot + nextTopLeftTileX);
            outBuffer.putShort(interfaceId + nextTopLeftTileY);
        }
        if (i1 == 596) {
            Player class50_sub1_sub4_sub3_sub2_5 = players[id];
            if (class50_sub1_sub4_sub3_sub2_5 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub2_5)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub2_5)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                outBuffer.putOpcode(143);
                outBuffer.putLEShortDup(itemId);
                outBuffer.putLEShortAdded(itemIndexId);
                outBuffer.putShort(itemInterfaceId);
                outBuffer.putShortAdded(id);
            }
        }
        if (i1 == 100) {
            boolean flag4 = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 0, 0, 2, 0, slot, 0, 0,
                    ((Actor) (thisPlayer)).walkingQueueX[0]);
            if (!flag4)
                flag4 = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0, slot, 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
            anInt1020 = super.anInt29;
            anInt1021 = super.anInt30;
            anInt1023 = 2;
            anInt1022 = 0;
            outBuffer.putOpcode(211);
            outBuffer.putLEShortAdded(itemIndexId);
            outBuffer.putShortAdded(itemId);
            outBuffer.putLEShortAdded(interfaceId + nextTopLeftTileY);
            outBuffer.putLEShortAdded(slot + nextTopLeftTileX);
            outBuffer.putLEShortDup(itemInterfaceId);
            outBuffer.putLEShortDup(id);
        }
        if (i1 == 1668) {
            Npc class50_sub1_sub4_sub3_sub1_6 = npcs[id];
            if (class50_sub1_sub4_sub3_sub1_6 != null) {
                NpcDefinition class37 = class50_sub1_sub4_sub3_sub1_6.def;
                if (class37.anIntArray622 != null)
                    class37 = class37.method363(false);
                if (class37 != null) {
                    String s10;
                    if (class37.aByteArray660 != null)
                        s10 = new String(class37.aByteArray660);
                    else
                        s10 = "It's a " + class37.name + ".";
                    pushMessage("", (byte) -123, s10, 0);
                }
            }
        }
        if (i1 == 26) {
            boolean flag5 = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 0, 0, 2, 0, slot, 0, 0,
                    ((Actor) (thisPlayer)).walkingQueueX[0]);
            if (!flag5)
                flag5 = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0, slot, 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
            anInt1020 = super.anInt29;
            anInt1021 = super.anInt30;
            anInt1023 = 2;
            anInt1022 = 0;
            anInt1100++;
            if (anInt1100 >= 120) {
                outBuffer.putOpcode(95);
                outBuffer.putInt(0);
                anInt1100 = 0;
            }
            outBuffer.putOpcode(100);
            outBuffer.putShort(slot + nextTopLeftTileX);
            outBuffer.putShortAdded(interfaceId + nextTopLeftTileY);
            outBuffer.putLEShortAdded(id);
        }
        if (i1 == 444) {
            outBuffer.putOpcode(91); // third item action
            outBuffer.putLEShortDup(id);
            outBuffer.putLEShortAdded(slot);
            outBuffer.putShort(interfaceId);
            anInt1329 = 0;
            anInt1330 = interfaceId;
            anInt1331 = slot;
            anInt1332 = 2;
            if (JagInterface.forId(interfaceId).anInt248 == anInt1169)
                anInt1332 = 1;
            if (JagInterface.forId(interfaceId).anInt248 == anInt988)
                anInt1332 = 3;
        }
        if (i1 == 507) {
            String s2 = rightClickOptions[i];
            int k2 = s2.indexOf("@whi@");
            if (k2 != -1)
                if (anInt1169 == -1) {
                    method15(false);
                    aString839 = s2.substring(k2 + 5).trim();
                    aBoolean1098 = false;
                    anInt1231 = anInt1169 = JagInterface.anInt246;
                } else {
                    pushMessage("", (byte) -123, "Please close the interface you have open before using 'report abuse'", 0);
                }
        }
        if (i1 == 389) {
            method80(interfaceId, 0, slot, id);
            outBuffer.putOpcode(241);
            outBuffer.putShort(id >> 14 & 0x7fff);
            outBuffer.putShort(slot + nextTopLeftTileX);
            outBuffer.putShortAdded(interfaceId + nextTopLeftTileY);
        }
        if (i1 == 564) {
            outBuffer.putOpcode(231); // fourth item action event
            outBuffer.putLEShortAdded(interfaceId);
            outBuffer.putLEShortDup(slot);
            outBuffer.putShort(id);
            anInt1329 = 0;
            anInt1330 = interfaceId;
            anInt1331 = slot;
            anInt1332 = 2;
            if (JagInterface.forId(interfaceId).anInt248 == anInt1169)
                anInt1332 = 1;
            if (JagInterface.forId(interfaceId).anInt248 == anInt988)
                anInt1332 = 3;
        }
        if (i1 == 984) {
            String s3 = rightClickOptions[i];
            int l2 = s3.indexOf("@whi@");
            if (l2 != -1) {
                long l4 = StringUtils.encodeBase37(s3.substring(l2 + 5).trim());
                int k3 = -1;
                for (int i4 = 0; i4 < friendsCount; i4++) {
                    if (friends[i4] != l4)
                        continue;
                    k3 = i4;
                    break;
                }

                if (k3 != -1 && anIntArray1267[k3] > 0) {
                    aBoolean1240 = true;
                    chatboxInterfaceType = 0;
                    aBoolean866 = true;
                    userInputString = "";
                    anInt1221 = 3;
                    aLong1141 = friends[k3];
                    aString937 = "Enter message to send to " + aStringArray849[k3];
                }
            }
        }
        if (i1 == 518) {
            outBuffer.putOpcode(79);
            outBuffer.putShort(interfaceId);
            JagInterface class13_3 = JagInterface.forId(interfaceId);
            if (class13_3.anIntArrayArray234 != null && class13_3.anIntArrayArray234[0][0] == 5) {
                int i3 = class13_3.anIntArrayArray234[0][1];
                if (localVarps[i3] != class13_3.anIntArray256[0]) {
                    localVarps[i3] = class13_3.anIntArray256[0];
                    handleVarp(i3);
                    aBoolean1181 = true;
                }
            }
        }
        if (i1 == 318) {
            Npc class50_sub1_sub4_sub3_sub1_7 = npcs[id];
            if (class50_sub1_sub4_sub3_sub1_7 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub1_7)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub1_7)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                outBuffer.putOpcode(112);
                outBuffer.putLEShortDup(id);
            }
        }
        if (i1 == 199) {
            boolean flag6 = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 0, 0, 2, 0, slot, 0, 0,
                    ((Actor) (thisPlayer)).walkingQueueX[0]);
            if (!flag6)
                flag6 = walk(false, false, interfaceId, ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0, slot, 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
            anInt1020 = super.anInt29;
            anInt1021 = super.anInt30;
            anInt1023 = 2;
            anInt1022 = 0;
            outBuffer.putOpcode(83);
            outBuffer.putLEShortDup(id);
            outBuffer.putShort(interfaceId + nextTopLeftTileY);
            outBuffer.putLEShortDup(spellId);
            outBuffer.putLEShortAdded(slot + nextTopLeftTileX);
        }
        if (i1 == 55) {
            method44(anInt1191);
            anInt1191 = -1;
            aBoolean1240 = true;
        }
        if (i1 == 52) {
            anInt1146 = 1;
            itemIndexId = slot;
            itemInterfaceId = interfaceId;
            itemId = id;
            aString1150 = String.valueOf(ItemDefinition.forId(id).name);
            anInt1171 = 0;
            aBoolean1181 = true;
            return;
        }
        if (i1 == 1564) {
            ItemDefinition class16_1 = ItemDefinition.forId(id);
            String s6;
            if (class16_1.description != null)
                s6 = new String(class16_1.description);
            else
                s6 = "It's a " + class16_1.name + ".";
            pushMessage("", (byte) -123, s6, 0);
        }
        if (i1 == 408) {
            Player class50_sub1_sub4_sub3_sub2_6 = players[id];
            if (class50_sub1_sub4_sub3_sub2_6 != null) {
                walk(false, false, ((Actor) (class50_sub1_sub4_sub3_sub2_6)).walkingQueueY[0],
                        ((Actor) (thisPlayer)).walkingQueueY[0], 1, 1, 2, 0,
                        ((Actor) (class50_sub1_sub4_sub3_sub2_6)).walkingQueueX[0], 0, 0,
                        ((Actor) (thisPlayer)).walkingQueueX[0]);
                anInt1020 = super.anInt29;
                anInt1021 = super.anInt30;
                anInt1023 = 2;
                anInt1022 = 0;
                outBuffer.putOpcode(194);
                outBuffer.putLEShortDup(id);
            }
        }
        anInt1146 = 0;
        anInt1171 = 0;
        aBoolean1181 = true;
    }

    public void method121(boolean flag) {
        anInt939 = 0;
        for (int i = -1; i < localPlayerCount + localNpcCount; i++) {
            Object obj;
            if (i == -1)
                obj = thisPlayer;
            else if (i < localPlayerCount)
                obj = players[localPlayers[i]];
            else
                obj = npcs[anIntArray1134[i - localPlayerCount]];
            if (obj == null || !((Actor) (obj)).isVisible())
                continue;
            if (obj instanceof Npc) {
                NpcDefinition class37 = ((Npc) obj).def;
                if (class37.anIntArray622 != null)
                    class37 = class37.method363(false);
                if (class37 == null)
                    continue;
            }
            if (i < localPlayerCount) {
                int k = 30;
                Player class50_sub1_sub4_sub3_sub2 = (Player) obj;
                if (class50_sub1_sub4_sub3_sub2.skullIcon != -1 || class50_sub1_sub4_sub3_sub2.prayerIcon != -1) {
                    method136(((Actor) (obj)), false, ((Actor) (obj)).anInt1594 + 15);
                    if (anInt932 > -1) {
                        if (class50_sub1_sub4_sub3_sub2.skullIcon != -1) {
                            aClass50_Sub1_Sub1_Sub1Array1288[class50_sub1_sub4_sub3_sub2.skullIcon].method461(anInt933
                                    - k, anInt932 - 12, -488);
                            k += 25;
                        }
                        if (class50_sub1_sub4_sub3_sub2.prayerIcon != -1) {
                            aClass50_Sub1_Sub1_Sub1Array1079[class50_sub1_sub4_sub3_sub2.prayerIcon].method461(anInt933
                                    - k, anInt932 - 12, -488);
                            k += 25;
                        }
                    }
                }
                if (i >= 0 && anInt1197 == 10 && anInt1151 == localPlayers[i]) {
                    method136(((Actor) (obj)), false, ((Actor) (obj)).anInt1594 + 15);
                    if (anInt932 > -1)
                        aClass50_Sub1_Sub1_Sub1Array954[1].method461(anInt933 - k, anInt932 - 12, -488);
                }
            } else {
                NpcDefinition class37_1 = ((Npc) obj).def;
                if (class37_1.anInt638 >= 0 && class37_1.anInt638 < aClass50_Sub1_Sub1_Sub1Array1079.length) {
                    method136(((Actor) (obj)), false, ((Actor) (obj)).anInt1594 + 15);
                    if (anInt932 > -1)
                        aClass50_Sub1_Sub1_Sub1Array1079[class37_1.anInt638].method461(anInt933 - 30, anInt932 - 12,
                                -488);
                }
                if (anInt1197 == 1 && anInt1226 == anIntArray1134[i - localPlayerCount] && pulseCycle % 20 < 10) {
                    method136(((Actor) (obj)), false, ((Actor) (obj)).anInt1594 + 15);
                    if (anInt932 > -1)
                        aClass50_Sub1_Sub1_Sub1Array954[0].method461(anInt933 - 28, anInt932 - 12, -488);
                }
            }
            if (((Actor) (obj)).forcedChatMessage != null
                    && (i >= localPlayerCount || publicChatMode == 0 || publicChatMode == 3 || publicChatMode == 1
                    && method148(13292, ((Player) obj).username))) {
                method136(((Actor) (obj)), false, ((Actor) (obj)).anInt1594);
                if (anInt932 > -1 && anInt939 < anInt940) {
                    anIntArray944[anInt939] = loginScreenFont.method473(((Actor) (obj)).forcedChatMessage,
                            (byte) -53) / 2;
                    anIntArray943[anInt939] = loginScreenFont.anInt1506;
                    anIntArray941[anInt939] = anInt932;
                    anIntArray942[anInt939] = anInt933;
                    anIntArray945[anInt939] = ((Actor) (obj)).anInt1583;
                    anIntArray946[anInt939] = ((Actor) (obj)).anInt1593;
                    anIntArray947[anInt939] = ((Actor) (obj)).forcedChatTicks;
                    aStringArray948[anInt939++] = ((Actor) (obj)).forcedChatMessage;
                    if (anInt998 == 0 && ((Actor) (obj)).anInt1593 >= 1 && ((Actor) (obj)).anInt1593 <= 3) {
                        anIntArray943[anInt939] += 10;
                        anIntArray942[anInt939] += 5;
                    }
                    if (anInt998 == 0 && ((Actor) (obj)).anInt1593 == 4)
                        anIntArray944[anInt939] = 60;
                    if (anInt998 == 0 && ((Actor) (obj)).anInt1593 == 5)
                        anIntArray943[anInt939] += 5;
                }
            }
            if (((Actor) (obj)).lastHitCycle > pulseCycle) {
                method136(((Actor) (obj)), false, ((Actor) (obj)).anInt1594 + 15);
                if (anInt932 > -1) {
                    int l = (((Actor) (obj)).hitType * 30) / ((Actor) (obj)).hitAmount;
                    if (l > 30)
                        l = 30;
                    Drawable.drawFullRect(anInt932 - 15, anInt933 - 3, l, 5, 65280);
                    Drawable.drawFullRect((anInt932 - 15) + l, anInt933 - 3, 30 - l, 5, 0xff0000);
                }
            }
            for (int i1 = 0; i1 < 4; i1++)
                if (((Actor) (obj)).anIntArray1632[i1] > pulseCycle) {
                    method136(((Actor) (obj)), false, ((Actor) (obj)).anInt1594 / 2);
                    if (anInt932 > -1) {
                        if (i1 == 1)
                            anInt933 -= 20;
                        if (i1 == 2) {
                            anInt932 -= 15;
                            anInt933 -= 10;
                        }
                        if (i1 == 3) {
                            anInt932 += 15;
                            anInt933 -= 10;
                        }
                        spriteArray1182[((Actor) (obj)).anIntArray1631[i1]].method461(anInt933 - 12, anInt932 - 12, -488);
                        font_p11_full.drawHorizontallyCenteredString(anInt932, anInt933 + 4, 0, String
                                .valueOf(((Actor) (obj)).anIntArray1630[i1]));
                        font_p11_full.drawHorizontallyCenteredString(anInt932 - 1, anInt933 + 3, 0xffffff, String
                                .valueOf(((Actor) (obj)).anIntArray1630[i1]));
                    }
                }

        }

        for (int j = 0; j < anInt939; j++) {
            int j1 = anIntArray941[j];
            int k1 = anIntArray942[j];
            int l1 = anIntArray944[j];
            int i2 = anIntArray943[j];
            boolean flag1 = true;
            while (flag1) {
                flag1 = false;
                for (int j2 = 0; j2 < j; j2++)
                    if (k1 + 2 > anIntArray942[j2] - anIntArray943[j2] && k1 - i2 < anIntArray942[j2] + 2
                            && j1 - l1 < anIntArray941[j2] + anIntArray944[j2]
                            && j1 + l1 > anIntArray941[j2] - anIntArray944[j2]
                            && anIntArray942[j2] - anIntArray943[j2] < k1) {
                        k1 = anIntArray942[j2] - anIntArray943[j2];
                        flag1 = true;
                    }

            }
            anInt932 = anIntArray941[j];
            anInt933 = anIntArray942[j] = k1;
            String s = aStringArray948[j];
            if (anInt998 == 0) {
                int k2 = 0xffff00;
                if (anIntArray945[j] < 6)
                    k2 = anIntArray842[anIntArray945[j]];
                if (anIntArray945[j] == 6)
                    k2 = tickCounter1138 % 20 >= 10 ? 0xffff00 : 0xff0000;
                if (anIntArray945[j] == 7)
                    k2 = tickCounter1138 % 20 >= 10 ? 65535 : 255;
                if (anIntArray945[j] == 8)
                    k2 = tickCounter1138 % 20 >= 10 ? 0x80ff80 : 45056;
                if (anIntArray945[j] == 9) {
                    int l2 = 150 - anIntArray947[j];
                    if (l2 < 50)
                        k2 = 0xff0000 + 1280 * l2;
                    else if (l2 < 100)
                        k2 = 0xffff00 - 0x50000 * (l2 - 50);
                    else if (l2 < 150)
                        k2 = 65280 + 5 * (l2 - 100);
                }
                if (anIntArray945[j] == 10) {
                    int i3 = 150 - anIntArray947[j];
                    if (i3 < 50)
                        k2 = 0xff0000 + 5 * i3;
                    else if (i3 < 100)
                        k2 = 0xff00ff - 0x50000 * (i3 - 50);
                    else if (i3 < 150)
                        k2 = (255 + 0x50000 * (i3 - 100)) - 5 * (i3 - 100);
                }
                if (anIntArray945[j] == 11) {
                    int j3 = 150 - anIntArray947[j];
                    if (j3 < 50)
                        k2 = 0xffffff - 0x50005 * j3;
                    else if (j3 < 100)
                        k2 = 65280 + 0x50005 * (j3 - 50);
                    else if (j3 < 150)
                        k2 = 0xffffff - 0x50000 * (j3 - 100);
                }
                if (anIntArray946[j] == 0) {
                    loginScreenFont.drawHorizontallyCenteredString(anInt932, anInt933 + 1, 0, s);
                    loginScreenFont.drawHorizontallyCenteredString(anInt932, anInt933, k2, s);
                }
                if (anIntArray946[j] == 1) {
                    loginScreenFont.method475(anInt933 + 1, tickCounter1138, s, anInt932, 0);
                    loginScreenFont.method475(anInt933, tickCounter1138, s, anInt932, k2);
                }
                if (anIntArray946[j] == 2) {
                    loginScreenFont.drawString_476(s, anInt933 + 1, 0, (byte) 1, anInt932, tickCounter1138);
                    loginScreenFont.drawString_476(s, anInt933, k2, (byte) 1, anInt932, tickCounter1138);
                }
                if (anIntArray946[j] == 3) {
                    loginScreenFont.method477(-601, s, 0, anInt932, anInt933 + 1, 150 - anIntArray947[j],
                            tickCounter1138);
                    loginScreenFont.method477(-601, s, k2, anInt932, anInt933, 150 - anIntArray947[j],
                            tickCounter1138);
                }
                if (anIntArray946[j] == 4) {
                    int k3 = loginScreenFont.method473(s, (byte) -53);
                    int i4 = ((150 - anIntArray947[j]) * (k3 + 100)) / 150;
                    Drawable.recalcEdges(0, anInt932 - 50, 334, anInt932 + 50, true);
                    loginScreenFont.drawString_474(s, 2245, (anInt932 + 50) - i4, 0, anInt933 + 1);
                    loginScreenFont.drawString_474(s, 2245, (anInt932 + 50) - i4, k2, anInt933);
                    Drawable.recalcSize();
                }
                if (anIntArray946[j] == 5) {
                    int l3 = 150 - anIntArray947[j];
                    int j4 = 0;
                    if (l3 < 25)
                        j4 = l3 - 25;
                    else if (l3 > 125)
                        j4 = l3 - 125;
                    Drawable.recalcEdges(anInt933 - loginScreenFont.anInt1506 - 1, 0, anInt933 + 5,
                            512, true);
                    loginScreenFont.drawHorizontallyCenteredString(anInt932, anInt933 + 1 + j4, 0, s);
                    loginScreenFont.drawHorizontallyCenteredString(anInt932, anInt933 + j4, k2, s);
                    Drawable.recalcSize();
                }
            } else {
                loginScreenFont.drawHorizontallyCenteredString(anInt932, anInt933 + 1, 0, s);
                loginScreenFont.drawHorizontallyCenteredString(anInt932, anInt933, 0xffff00, s);
            }
        }
        if (flag) {
            opcode = -1;
        }
    }

    public void initUI() {
        if (chatboxImage_1159 != null) {
            return;
        }
        resetWhenBoolTrue();
        super.imageProducer = null;
        loginBackground_1 = null;
        loginBackground_2 = null;
        loginboxElement = null;
        loginFlameLeft = null;
        loginFlameRight = null;
        loginBackground_3 = null;
        loginBackground_4 = null;
        loginBackground_5 = null;
        loginBackground_6 = null;
        chatboxImage_1159 = new JagImageProducer(479, 96, getParentComponent());
        aClass18_1157 = new JagImageProducer(172, 156, getParentComponent());
        Drawable.clearScreen();
        mapback_1186.drawSprite(0, 0);
        inventoryImage = new JagImageProducer(190, 261, getParentComponent());
        gameViewportImage = new JagImageProducer(layout.viewport.width, layout.viewport.height, getParentComponent());
        Drawable.clearScreen();
        chatboxButtons = new JagImageProducer(496, 50, getParentComponent());
        aClass18_1109 = new JagImageProducer(269, 37, getParentComponent());
        aClass18_1110 = new JagImageProducer(249, 45, getParentComponent());
        shouldRenderUI = true;

        gameViewportImage.pushPixels();
        ThreeDimensionalCanvas.lineOffsets = gameViewportOffsets;
    }

    public void drawErrorScreen() {
        Graphics g = super.graphics;
        g.setColor(Color.black);
        g.fillRect(0, 0, 765, 503);
        setFramerate(1);
        if (aBoolean1283) {
            isThreadStarted = false;
            g.setFont(new Font("Helvetica", 1, 16));
            g.setColor(Color.yellow);
            int j = 35;
            g.drawString("Sorry, an error has occured whilst loading RuneScape", 30, j);
            j += 50;
            g.setColor(Color.white);
            g.drawString("To fix this try the following (in order):", 30, j);
            j += 50;
            g.setColor(Color.white);
            g.setFont(new Font("Helvetica", 1, 12));
            g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, j);
            j += 30;
            g.drawString("2: Try clearing your web-browsers cache from tools->internet options", 30, j);
            j += 30;
            g.drawString("3: Try using a different game-world", 30, j);
            j += 30;
            g.drawString("4: Try rebooting your computer", 30, j);
            j += 30;
            g.drawString("5: Try selecting a different version of Java from the play-game menu", 30, j);
        }
        if (aBoolean1097) {
            isThreadStarted = false;
            g.setFont(new Font("Helvetica", 1, 20));
            g.setColor(Color.white);
            g.drawString("Error - unable to load game!", 50, 50);
            g.drawString("To play RuneScape make sure you play from", 50, 100);
            g.drawString("http://www.runescape.com", 50, 150);
        }
        if (aBoolean1016) {
            isThreadStarted = false;
            g.setColor(Color.yellow);
            int k = 35;
            g.drawString("Error a copy of RuneScape already appears to be loaded", 30, k);
            k += 50;
            g.setColor(Color.white);
            g.drawString("To fix this try the following (in order):", 30, k);
            k += 50;
            g.setColor(Color.white);
            g.setFont(new Font("Helvetica", 1, 12));
            g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, k);
            k += 30;
            g.drawString("2: Try rebooting your computer, and reloading", 30, k);
            k += 30;
        }
    }

    public void method124(boolean flag) {
        try {
            if (connection != null)
                connection.closeConnection();
        } catch (Exception _ex) {
        }
        connection = null;
        isLoggedIn = false;
        loginScreenState = 0;
        // thisPlayerName = "";
        //  aString1093 = "";
        method49(383);
        isLoggedIn &= flag;
        sceneGraph.method241((byte) 7);
        for (int plane = 0; plane < 4; plane++) {
            clippingPlanes[plane].clear();
        }
        System.gc();
        method50(false);
        anInt1327 = -1;
        anInt1270 = -1;
        anInt1128 = 0;
    }

    public void method125(String s, String s1) {
        if (gameViewportImage != null) {
            gameViewportImage.pushPixels();
            ThreeDimensionalCanvas.lineOffsets = gameViewportOffsets;
            int j = 151;
            if (s != null)
                j -= 7;
            fontChatboxButtons.drawHorizontallyCenteredString(257, j, 0, s1);
            fontChatboxButtons.drawHorizontallyCenteredString(256, j - 1, 0xffffff, s1);
            j += 15;
            if (s != null) {
                fontChatboxButtons.drawHorizontallyCenteredString(257, j, 0, s);
                fontChatboxButtons.drawHorizontallyCenteredString(256, j - 1, 0xffffff, s);
            }
            gameViewportImage.drawImage(layout.viewport, super.graphics);
            presentBackBuffer();
            return;
        }
        if (super.imageProducer != null) {
            super.imageProducer.pushPixels();
            ThreeDimensionalCanvas.lineOffsets = clientEntireOffsets;
            int k = 251;
            char c = '\u012C';
            byte byte0 = 50;
            Drawable.drawFullRect(383 - c / 2, k - 5 - byte0 / 2, c, byte0, 0);
            Drawable.drawRect(383 - c / 2, k - 5 - byte0 / 2, c, byte0, 0xffffff);
            if (s != null)
                k -= 7;
            fontChatboxButtons.drawHorizontallyCenteredString(383, k, 0, s1);
            fontChatboxButtons.drawHorizontallyCenteredString(382, k - 1, 0xffffff, s1);
            k += 15;
            if (s != null) {
                fontChatboxButtons.drawHorizontallyCenteredString(383, k, 0, s);
                fontChatboxButtons.drawHorizontallyCenteredString(382, k - 1, 0xffffff, s);
            }
            super.imageProducer.drawImage(0, 0, super.graphics);
            presentBackBuffer();
        }
    }

    public boolean method126(int i, byte byte0) {
        if (i < 0)
            return false;
        int j = anIntArray981[i];
        if (byte0 != 97)
            throw new NullPointerException();
        if (j >= 2000)
            j -= 2000;
        return j == 762;
    }

    public void method127(boolean flag) {
        if (!flag)
            anInt1056 = incomingRandom.nextInt();
        if (anInt1197 != 2)
            return;
        calcEntityScreenPos((anInt844 - nextTopLeftTileX << 7) + anInt847, anInt846 * 2, (anInt845 - nextTopLeftTileY << 7) + anInt848, -214);
        if (anInt932 > -1 && pulseCycle % 20 < 10)
            aClass50_Sub1_Sub1_Sub1Array954[0].method461(anInt933 - 28, anInt932 - 12, -488);
    }

    @Override
    public void repaintGame() {
        if (aBoolean1016 || aBoolean1283 || aBoolean1097) {
            drawErrorScreen();
            return;
        }
        paintCounter1309++;
        if (!isLoggedIn) {
            prepareLoginScreenGraphics();
            drawLoginScreen(false);
        } else {
            drawGame();
        }
        anInt1094 = 0;
    }

    /**
     * In the resizable mode the 765x503 login screen is drawn in the centre of the window, with black around it.
     * The back buffer Graphics is moved to the centre for the login screen and back to the corner for the game.
     */
    private void prepareLoginScreenGraphics() {
        Graphics2D g = (Graphics2D) super.graphics;
        // also clear when coming from the game (logging out), which leaves its last frame in the back buffer
        if (clientSize == 1 && (shouldRenderUI || !loginScreenCleared)) {
            g.setTransform(new AffineTransform());
            g.setColor(Color.black);
            g.fillRect(0, 0, clientWidth, clientHeight);
            loginScreenCleared = true;
            // the middle of the screen was cleared too, so the login screen has to be drawn again
            shouldRenderUI = true;
        }
        g.setTransform(AffineTransform.getTranslateInstance(loginScreenOffsetX(), loginScreenOffsetY()));
    }

    public void drawContextMenu() {
        int beginX = anInt1305;
        int beginY = anInt1306;
        int k = anInt1307;
        int l = anInt1308;
        int i1 = 0x5d5447;
        Drawable.drawFullRect(beginX, beginY, k, l, i1);
        Drawable.drawFullRect(beginX + 1, beginY + 1, k - 2, 16, 0);
        Drawable.drawRect(beginX + 1, beginY + 18, k - 2, l - 19, 0);
        loginScreenFont.drawString_474("Choose Option", 2245, beginX + 3, i1, beginY + 14);
        int j1 = super.mouseX - layout.areaX(anInt1304);
        int k1 = super.mouseY - layout.areaY(anInt1304);
        for (int optionCounter = 0; optionCounter < anInt1183; optionCounter++) {
            int i2 = beginY + 31 + (anInt1183 - 1 - optionCounter) * 15;
            int j2 = 0xffffff;
            if (j1 > beginX && j1 < beginX + k && k1 > i2 - 13 && k1 < i2 + 3)
                j2 = 0xffff00;
            loginScreenFont.drawString(rightClickOptions[optionCounter], j2, beginX + 3, i2, true);
        }
    }

    public int method129(int i, int j, JagInterface class13) {
        if (i != 3)
            return anInt1222;
        if (class13.anIntArrayArray234 == null || j >= class13.anIntArrayArray234.length)
            return -2;
        try {
            int ai[] = class13.anIntArrayArray234[j];
            int k = 0;
            int l = 0;
            int i1 = 0;
            do {
                int j1 = ai[l++];
                int k1 = 0;
                byte byte0 = 0;
                if (j1 == 0)
                    return k;
                if (j1 == 1)
                    k1 = anIntArray1029[ai[l++]];
                if (j1 == 2)
                    k1 = anIntArray1054[ai[l++]];
                if (j1 == 3)
                    k1 = anIntArray843[ai[l++]];
                if (j1 == 4) {
                    JagInterface class13_1 = JagInterface.forId(ai[l++]);
                    int k2 = ai[l++];
                    if (k2 >= 0 && k2 < ItemDefinition.count && (!ItemDefinition.forId(k2).members || memberServer)) {
                        for (int j3 = 0; j3 < class13_1.itemIds.length; j3++)
                            if (class13_1.itemIds[j3] == k2 + 1)
                                k1 += class13_1.itemAmounts[j3];

                    }
                }
                if (j1 == 5)
                    k1 = localVarps[ai[l++]];
                if (j1 == 6)
                    k1 = anIntArray952[anIntArray1054[ai[l++]] - 1];
                if (j1 == 7)
                    k1 = (localVarps[ai[l++]] * 100) / 46875;
                if (j1 == 8)
                    k1 = thisPlayer.anInt1753;
                if (j1 == 9) {
                    for (int l1 = 0; l1 < Skills.anInt700; l1++)
                        if (Skills.aBooleanArray702[l1])
                            k1 += anIntArray1054[l1];

                }
                if (j1 == 10) {
                    JagInterface class13_2 = JagInterface.forId(ai[l++]);
                    int l2 = ai[l++] + 1;
                    if (l2 >= 0 && l2 < ItemDefinition.count && (!ItemDefinition.forId(l2).members || memberServer)) {
                        for (int k3 = 0; k3 < class13_2.itemIds.length; k3++) {
                            if (class13_2.itemIds[k3] != l2)
                                continue;
                            k1 = 0x3b9ac9ff;
                            break;
                        }

                    }
                }
                if (j1 == 11)
                    k1 = anInt1324;
                if (j1 == 12)
                    k1 = anInt1030;
                if (j1 == 13) {
                    int i2 = localVarps[ai[l++]];
                    int i3 = ai[l++];
                    k1 = (i2 & 1 << i3) == 0 ? 0 : 1;
                }
                if (j1 == 14) {
                    int j2 = ai[l++];
                    Varbit varbit = Varbit.varbitTable[j2];
                    int varpId = varbit.varpId;
                    int low = varbit.leastSignificantBit;
                    int high = varbit.mostSignificantBit;
                    int k4 = BITFIELD_MAX_VALUES[high - low];
                    k1 = localVarps[varpId] >> low & k4;
                }
                if (j1 == 15)
                    byte0 = 1;
                if (j1 == 16)
                    byte0 = 2;
                if (j1 == 17)
                    byte0 = 3;
                if (j1 == 18)
                    k1 = (((Actor) (thisPlayer)).unitX >> 7) + nextTopLeftTileX;
                if (j1 == 19)
                    k1 = (((Actor) (thisPlayer)).unitY >> 7) + nextTopLeftTileY;
                if (j1 == 20)
                    k1 = ai[l++];
                if (byte0 == 0) {
                    if (i1 == 0)
                        k += k1;
                    if (i1 == 1)
                        k -= k1;
                    if (i1 == 2 && k1 != 0)
                        k /= k1;
                    if (i1 == 3)
                        k *= k1;
                    i1 = 0;
                } else {
                    i1 = byte0;
                }
            } while (true);
        } catch (Exception _ex) {
            return -1;
        }
    }

    public void method130(int i, boolean flag, RgbSprite class50_sub1_sub1_sub1, int j) {
        if (class50_sub1_sub1_sub1 == null)
            return;
        int k = anInt1252 + anInt916 & 0x7ff;
        int l = j * j + i * i;
        if (l > 6400)
            return;
        int i1 = Model.sineTable[k];
        int j1 = Model.cosineTable[k];
        i1 = (i1 * 256) / (anInt1233 + 256);
        j1 = (j1 * 256) / (anInt1233 + 256);
        if (!flag)
            opcode = buffer.getByte();
        int k1 = i * i1 + j * j1 >> 16;
        int l1 = i * j1 - j * i1 >> 16;
        if (l > 2500) {
            class50_sub1_sub1_sub1.method467(mapback_1186, 83 - l1 - class50_sub1_sub1_sub1.height_1495
                    / 2 - 4, -49993, ((94 + k1) - class50_sub1_sub1_sub1.width_1494 / 2) + 4);
            return;
        } else {
            class50_sub1_sub1_sub1.method461(83 - l1 - class50_sub1_sub1_sub1.height_1495 / 2 - 4,
                    ((94 + k1) - class50_sub1_sub1_sub1.width_1494 / 2) + 4, -488);
            return;
        }
    }

    public void drawLoginScreen(boolean flag) {
        prepareLoginUI();
        loginboxElement.pushPixels();
        titlebox_1292.drawSprite(0, 0);
        //char c = '\u0168';
        //char c1 = '\310';
        int c = 360;
        int c1 = 200;
        if (loginScreenState == 0) {
            int j = c1 / 2 + 80;
            font_p11_full.drawString(fileFetcher.aString1347, c / 2, j, true, 0x75a9a9);
            j = c1 / 2 - 20;
            loginScreenFont.drawString("Welcome to RuneScape", c / 2, j, true, 0xffff00);
            j += 30;
            int i1 = c / 2 - 80;
            int l1 = c1 / 2 + 20;
            titlebutton_1293.drawSprite(i1 - 73, l1 - 20);
            loginScreenFont.drawString("New User", i1, l1 + 5, true, 0xffffff);
            i1 = c / 2 + 80;
            titlebutton_1293.drawSprite(i1 - 73, l1 - 20);
            loginScreenFont.drawString("Existing User", i1, l1 + 5, true, 0xffffff);
        }
        if (loginScreenState == 2) {
            int k = c1 / 2 - 40;
            if (statusLineOne.length() > 0) {
                loginScreenFont.drawString(statusLineOne, c / 2, k - 15, true, 0xffff00);
                loginScreenFont.drawString(statusLineTwo, c / 2, k, true, 0xffff00);
                k += 30;
            } else {
                loginScreenFont.drawString(statusLineTwo, c / 2, k - 7, true, 0xffff00);
                k += 30;
            }
            loginScreenFont.drawString("Username: " + thisPlayerName
                    + ((anInt977 == 0) & (pulseCycle % 40 < 20) ? "@yel@|" : ""), 0xffffff, c / 2 - 90, k, true);
            k += 15;
            loginScreenFont.drawString("Password: "
                            + StringUtils.asterisks(thisPlayerPassword) + ((anInt977 == 1) & (pulseCycle % 40 < 20) ? "@yel@|" : ""), 0xffffff, c / 2 - 88, k, true);
            k += 15;
            if (!flag) {
                int j1 = c / 2 - 80;
                int i2 = c1 / 2 + 50;
                titlebutton_1293.drawSprite(j1 - 73, i2 - 20);
                loginScreenFont.drawString("Login", j1, i2 + 5, true, 0xffffff);
                j1 = c / 2 + 80;
                titlebutton_1293.drawSprite(j1 - 73, i2 - 20);
                loginScreenFont.drawString("Cancel", j1, i2 + 5, true, 0xffffff);
            }
        }
        if (loginScreenState == 3) {
            loginScreenFont.drawString("Create a free account", c / 2, c1 / 2 - 60, true, 0xffff00);
            int l = c1 / 2 - 35;
            loginScreenFont.drawString("To create a new account you need to", c / 2, l, true, 0xffffff);
            l += 15;
            loginScreenFont.drawString("go back to the main RuneScape webpage", c / 2, l, true, 0xffffff);
            l += 15;
            loginScreenFont.drawString("and choose the 'create account'", c / 2, l, true, 0xffffff);
            l += 15;
            loginScreenFont.drawString("button near the top of that page.", c / 2, l, true, 0xffffff);
            l += 15;
            int k1 = c / 2;
            int j2 = c1 / 2 + 50;
            titlebutton_1293.drawSprite(k1 - 73, j2 - 20);
            loginScreenFont.drawString("Cancel", k1, j2 + 5, true, 0xffffff);
        }
        loginboxElement.drawImage(202, 171, super.graphics);
        if (shouldRenderUI) {
            shouldRenderUI = false;
            loginBackground_1.drawImage(128, 0, super.graphics);
            loginBackground_2.drawImage(202, 371, super.graphics);
            loginBackground_3.drawImage(0, 265, super.graphics);
            loginBackground_4.drawImage(562, 265, super.graphics);
            loginBackground_5.drawImage(128, 171, super.graphics);
            loginBackground_6.drawImage(562, 171, super.graphics);
        }
        presentBackBuffer();
    }

    public void addNewNpcs(JagBuffer buf, int i, boolean flag) {
        if (flag)
            anInt1140 = 287;
        while (buf.bitPosition + 21 < i * 8) {
            int index = buf.getBits(14);
            if (index == 16383)
                break;
            if (npcs[index] == null)
                npcs[index] = new Npc();
            Npc npc = npcs[index];
            anIntArray1134[localNpcCount++] = index;
            npc.pulseCycle = pulseCycle;
            int updateRequired = buf.getBits(1);
            if (updateRequired == 1)
                updatedPlayers[updatedPlayerCount++] = index;
            int deltaY = buf.getBits(5);
            if (deltaY > 15)
                deltaY -= 32;
            int deltaX = buf.getBits(5);
            if (deltaX > 15)
                deltaX -= 32;
            int discardWalkingQueue = buf.getBits(1);
            npc.def = NpcDefinition.forId(buf.getBits(13));
            npc.anInt1601 = npc.def.aByte642;
            npc.anInt1600 = npc.def.anInt651;
            npc.anInt1619 = npc.def.anInt645;
            npc.anInt1620 = npc.def.anInt643;
            npc.anInt1621 = npc.def.anInt641;
            npc.anInt1622 = npc.def.anInt633;
            npc.anInt1634 = npc.def.anInt621;
            npc.teleport(thisPlayer.walkingQueueX[0] + deltaX, thisPlayer.walkingQueueY[0] + deltaY, discardWalkingQueue == 1);
        }
        buf.finishBitAccess();
    }

    public void parsePlacementPacket(JagBuffer buf, int opcode) {
        if (opcode == 203) {
            int k = buf.getShort();
            int j3 = buf.getByte();
            int i6 = j3 >> 2;
            int rotation = j3 & 3;
            int k11 = anIntArray1032[i6];
            byte byte0 = buf.getSignedByteNegated();
            int offset = buf.getByteAdded();
            int x = placementX + (offset >> 4 & 7);
            int y = placementY + (offset & 7);
            byte byte1 = buf.getSignedByteAdded();
            int l19 = buf.getShortAdded();
            int id = buf.getLEShort();
            byte byte2 = buf.getSignedByte();
            byte byte3 = buf.getSignedByteAdded();
            int l21 = buf.getShort();
            Player player;
            if (id == thisPlayerServerId)
                player = thisPlayer;
            else
                player = players[id];
            if (player != null) {
                ObjectDefinition class47 = ObjectDefinition.forId(k);
                int i22 = intGroundArray[plane][x][y];
                int j22 = intGroundArray[plane][x + 1][y];
                int k22 = intGroundArray[plane][x + 1][y + 1];
                int l22 = intGroundArray[plane][x][y + 1];
                Model class50_sub1_sub4_sub4 = class47.method431(i6, rotation, i22, j22, k22, l22, -1);
                if (class50_sub1_sub4_sub4 != null) {
                    method145(true, plane, x, y, 0, l19 + 1, 0, -1, l21 + 1, k11);
                    player.anInt1764 = l21 + pulseCycle;
                    player.anInt1765 = l19 + pulseCycle;
                    player.aClass50_Sub1_Sub4_Sub4_1746 = class50_sub1_sub4_sub4;
                    int i23 = class47.anInt801;
                    int j23 = class47.anInt775;
                    if (rotation == 1 || rotation == 3) {
                        i23 = class47.anInt775;
                        j23 = class47.anInt801;
                    }
                    player.anInt1743 = x * 128 + i23 * 64;
                    player.anInt1745 = y * 128 + j23 * 64;
                    player.anInt1744 = getFloorDrawHeight(player.anInt1745,
                            player.anInt1743, plane);
                    if (byte1 > byte0) {
                        byte byte4 = byte1;
                        byte1 = byte0;
                        byte0 = byte4;
                    }
                    if (byte3 > byte2) {
                        byte byte5 = byte3;
                        byte3 = byte2;
                        byte2 = byte5;
                    }
                    player.anInt1768 = x + byte1;
                    player.anInt1770 = x + byte0;
                    player.anInt1769 = y + byte3;
                    player.anInt1771 = y + byte2;
                }
            }
        }
        if (opcode == 106) { // add ground item dropped by player
            int offset = buf.getByteAdded();
            int x = placementX + (offset >> 4 & 7);
            int y = placementY + (offset & 7);
            int amount = buf.getLEShortA();
            int id = buf.getShortAdded();
            int playerId = buf.getShortAdded();
            if (x >= 0 && y >= 0 && x < 104 && y < 104 && playerId != thisPlayerServerId) {
                GroundItem item = new GroundItem();
                item.id = id;
                item.amount = amount;
                if (groundItems[plane][x][y] == null)
                    groundItems[plane][x][y] = new LinkedList();
                groundItems[plane][x][y].addLast(item);
                method26(x, y);
            }
            return;
        }
        if (opcode == 142) {
            int i1 = buf.getShort();
            int l3 = buf.getByteAdded();
            int k6 = l3 >> 2;
            int j9 = l3 & 3;
            int i12 = anIntArray1032[k6];
            int j14 = buf.getByte();
            int x = placementX + (j14 >> 4 & 7);
            int y = placementY + (j14 & 7);
            if (x >= 0 && y >= 0 && x < 103 && y < 103) {
                int l18 = intGroundArray[plane][x][y];
                int j19 = intGroundArray[plane][x + 1][y];
                int i20 = intGroundArray[plane][x + 1][y + 1];
                int l20 = intGroundArray[plane][x][y + 1];
                if (i12 == 0) {
                    ScenegraphMember44 class44 = sceneGraph.method263(plane, 17734, x, y);
                    if (class44 != null) {
                        int k21 = class44.uid >> 14 & 0x7fff;
                        if (k6 == 2) {
                            class44.aClass50_Sub1_Sub4_724 = new GameObject(k21, i1, i20, l20, j19, 2, false, l18, 4 + j9);
                            class44.aClass50_Sub1_Sub4_725 = new GameObject(k21, i1, i20, l20, j19, 2, false, l18, j9 + 1 & 3);
                        } else {
                            class44.aClass50_Sub1_Sub4_724 = new GameObject(k21, i1, i20, l20, j19, k6,
                                    false, l18, j9);
                        }
                    }
                }
                if (i12 == 1) {
                    ScenegraphMember35 class35 = sceneGraph.method264(plane, y, x, false);
                    if (class35 != null)
                        class35.aClass50_Sub1_Sub4_608 = new GameObject(class35.anInt609 >> 14 & 0x7fff, i1, i20, l20, j19, 4, false, l18, 0);
                }
                if (i12 == 2) {
                    ScenegraphRelated5 scenegraphRelated5 = sceneGraph.method265(x, (byte) 32, y, plane);
                    if (k6 == 11)
                        k6 = 10;
                    if (scenegraphRelated5 != null)
                        scenegraphRelated5.entity = new GameObject(scenegraphRelated5.anInt125 >> 14 & 0x7fff, i1, i20, l20, j19, k6, false, l18, j9);
                }
                if (i12 == 3) {
                    ScenegraphMember28 class28 = sceneGraph.method266(plane, y, 0, x);
                    if (class28 != null)
                        class28.aClass50_Sub1_Sub4_570 = new GameObject(class28.anInt571 >> 14 & 0x7fff, i1, i20, l20, j19, 22, false, l18, j9);
                }
            }
            return;
        }
        if (opcode == 107) { // add ground item (dropped by npc or "auto spawn")
            int id = buf.getShort();
            int offset = buf.getByteNegated();
            int x = placementX + (offset >> 4 & 7);
            int y = placementY + (offset & 7);
            int amount = buf.getShortAdded();
            if (x >= 0 && y >= 0 && x < 104 && y < 104) {
                GroundItem item = new GroundItem();
                item.id = id;
                item.amount = amount;
                if (groundItems[plane][x][y] == null)
                    groundItems[plane][x][y] = new LinkedList();
                groundItems[plane][x][y].addLast(item);
                method26(x, y);
            }
            return;
        }
        if (opcode == 121) { // update amount of ground item
            int offset = buf.getByte();
            int x = placementX + (offset >> 4 & 7);
            int y = placementY + (offset & 7);
            int id = buf.getShort();
            int amount = buf.getShort();
            int newAmount = buf.getShort();
            if (x >= 0 && y >= 0 && x < 104 && y < 104) {
                LinkedList list = groundItems[plane][x][y];
                if (list != null) {
                    for (GroundItem item = (GroundItem) list.first(); item != null; item = (GroundItem) list.next()) {
                        if (item.id != (id & 0x7fff) || item.amount != amount)
                            continue;
                        item.amount = newAmount;
                        break;
                    }

                    method26(x, y);
                }
            }
            return;
        }
        if (opcode == 181) {
            int offset = buf.getByte();
            int x = placementX + (offset >> 4 & 7);
            int y = placementY + (offset & 7);
            int offsetX = x + buf.getSignedByte();
            int offsetY = y + buf.getSignedByte();
            int target = buf.getSignedShort();
            int id = buf.getShort();
            int heightStart = buf.getByte() * 4;
            int heightEnd = buf.getByte() * 4;
            int createdTime = buf.getShort();
            int speed = buf.getShort();
            int initialSlope = buf.getByte();
            int distanceFromSource = buf.getByte();
            if (x >= 0 && y >= 0 && x < 104 && y < 104 && offsetX >= 0 && offsetY >= 0 && offsetX < 104 && offsetY < 104
                    && id != 65535) {
                x = x * 128 + 64;
                y = y * 128 + 64;
                offsetX = offsetX * 128 + 64;
                offsetY = offsetY * 128 + 64;
                Projectile projectile = new Projectile(plane, heightEnd, distanceFromSource, y,
                        id, speed + pulseCycle, initialSlope, target, getFloorDrawHeight(y, x, plane) - heightStart, x, createdTime + pulseCycle);
                projectile.trackTarget(offsetX, offsetY, getFloorDrawHeight(offsetY, offsetX, plane) - heightEnd, createdTime
                        + pulseCycle);
                projectileQueue.addLast(projectile);
            }
            return;
        }
        if (opcode == 41) { // sound?
            int offset = buf.getByte();
            int x = placementX + (offset >> 4 & 7);
            int y = placementY + (offset & 7);
            int j10 = buf.getShort();
            int i13 = buf.getByte();
            int i15 = i13 >> 4 & 0xf;
            int l16 = i13 & 7;
            if (((Actor) (thisPlayer)).walkingQueueX[0] >= x - i15
                    && ((Actor) (thisPlayer)).walkingQueueX[0] <= x + i15
                    && ((Actor) (thisPlayer)).walkingQueueY[0] >= y - i15
                    && ((Actor) (thisPlayer)).walkingQueueY[0] <= y + i15 && aBoolean1301 && !lowMemory
                    && anInt1035 < 50) {
                anIntArray1090[anInt1035] = j10;
                anIntArray1321[anInt1035] = l16;
                anIntArray1259[anInt1035] = Sound.anIntArray669[j10];
                anInt1035++;
            }
        }
        if (opcode == 59) {
            int offset = buf.getByte();
            int i5 = placementX + (offset >> 4 & 7);
            int l7 = placementY + (offset & 7);
            int id = buf.getShort();
            int height = buf.getByte();
            int delay = buf.getShort();
            if (i5 >= 0 && l7 >= 0 && i5 < 104 && l7 < 104) {
                i5 = i5 * 128 + 64;
                l7 = l7 * 128 + 64;
                Graphic graphic = new Graphic(i5, plane, getFloorDrawHeight(l7,
                        i5, plane)
                        - height, delay, id, pulseCycle, l7, 10709);
                aClass6_1210.addLast(graphic);
            }
            return;
        }
        if (opcode == 152) { // anti cheat?
            int k2 = buf.getByteNegated();
            int j5 = k2 >> 2;
            int i8 = k2 & 3;
            int l10 = anIntArray1032[j5];
            int k13 = buf.getLEShortA();
            int k15 = buf.getByteAdded();
            int i17 = placementX + (k15 >> 4 & 7);
            int j18 = placementY + (k15 & 7);
            if (i17 >= 0 && j18 >= 0 && i17 < 104 && j18 < 104)
                method145(true, plane, i17, j18, i8, -1, j5, k13, 0, l10);
            return;
        }
        if (opcode == 208) { // remove ground item
            int id = buf.getShortAdded();
            int offset = buf.getByteAdded();
            int x = placementX + (offset >> 4 & 7);
            int y = placementY + (offset & 7);
            if (x >= 0 && y >= 0 && x < 104 && y < 104) {
                LinkedList list = groundItems[plane][x][y];
                if (list != null) {
                    for (GroundItem item = (GroundItem) list.first(); item != null; item = (GroundItem) list.next()) {
                        if (item.id != (id & 0x7fff))
                            continue;
                        item.unlink();
                        break;
                    }

                    if (list.first() == null)
                        groundItems[plane][x][y] = null;
                    method26(x, y);
                }
            }
            return;
        }
        if (opcode == 88) {
            int i3 = buf.getByteSubtracted();
            int l5 = placementX + (i3 >> 4 & 7);
            int k8 = placementY + (i3 & 7);
            int j11 = buf.getByteSubtracted();
            int l13 = j11 >> 2;
            int l15 = j11 & 3;
            int j17 = anIntArray1032[l13];
            if (l5 >= 0 && k8 >= 0 && l5 < 104 && k8 < 104)
                method145(true, plane, l5, k8, l15, -1, l13, -1, 0, j17);
        }
    }

    public void method134(byte byte0) {
        inventoryImage.pushPixels();
        ThreeDimensionalCanvas.lineOffsets = tabsOffsets;
        aClass50_Sub1_Sub1_Sub3_1185.drawSprite(0, 0);
        if (anInt1089 != -1)
            drawInterface(0, 0, JagInterface.forId(anInt1089), 0, 8);
        else if (anIntArray1081[tabId] != -1)
            drawInterface(0, 0, JagInterface.forId(anIntArray1081[tabId]), 0, 8);
        if (isContextMenuActive && anInt1304 == 1)
            drawContextMenu();
        drawPanel(inventoryImage, layout.inventory, ClientLayout.INVENTORY_ALPHA);
        gameViewportImage.pushPixels();
        ThreeDimensionalCanvas.lineOffsets = gameViewportOffsets;
        if (byte0 == 7)
            ;
    }

    public static String method135(int i, int j) {
        String s = String.valueOf(j);
        if (i != 0)
            throw new NullPointerException();
        for (int k = s.length() - 3; k > 0; k -= 3)
            s = s.substring(0, k) + "," + s.substring(k);

        if (s.length() > 8)
            s = "@gre@" + s.substring(0, s.length() - 8) + " million @whi@(" + s + ")";
        else if (s.length() > 4)
            s = "@cya@" + s.substring(0, s.length() - 4) + "K @whi@(" + s + ")";
        return " " + s;
    }

    public void method136(Actor class50_sub1_sub4_sub3, boolean flag, int i) {
        calcEntityScreenPos(class50_sub1_sub4_sub3.unitX, i, class50_sub1_sub4_sub3.unitY, -214);
        if (!flag)
            ;
    }

    public void calcEntityScreenPos(int i, int j, int k, int l) {
        if (i < 128 || k < 128 || i > 13056 || k > 13056) {
            anInt932 = -1;
            anInt933 = -1;
            return;
        }
        int i1 = getFloorDrawHeight(k, i, plane) - j;
        i -= anInt1216;
        i1 -= anInt1217;
        k -= anInt1218;
        int j1 = Model.sineTable[anInt1219];
        int k1 = Model.cosineTable[anInt1219];
        int l1 = Model.sineTable[anInt1220];
        int i2 = Model.cosineTable[anInt1220];
        int j2 = k * l1 + i * i2 >> 16;
        k = k * i2 - i * l1 >> 16;
        i = j2;
        j2 = i1 * k1 - k * j1 >> 16;
        k = i1 * j1 + k * k1 >> 16;
        while (l >= 0)
            opcode = -1;
        i1 = j2;
        if (k >= 50) {
            anInt932 = ThreeDimensionalCanvas.halfParentWidth + (i << 9) / k;
            anInt933 = ThreeDimensionalCanvas.halfParentHeight + (i1 << 9) / k;
            return;
        } else {
            anInt932 = -1;
            anInt933 = -1;
            return;
        }
    }

    public void printLagInfo(boolean flag) {
        System.out.println("============");
        System.out.println("flame-cycle:" + anInt1101);
        if (fileFetcher != null)
            System.out.println("Od-cycle:" + fileFetcher.anInt1348);
        System.out.println("loop-cycle:" + pulseCycle);
        System.out.println("draw-cycle:" + paintCounter1309);
        System.out.println("ptype:" + opcode);
        System.out.println("psize:" + size);
        if (flag)
            aBoolean1028 = !aBoolean1028;
        if (connection != null)
            connection.debugPrint(false);
        super.aBoolean11 = true;
    }

    public Component getParentComponent() {
        if (signlink.mainapp != null) {
            return signlink.mainapp;
        }
        return Objects.requireNonNullElse(super.frame, this);
    }

    public void drawLoadingText(int i, String text) {
        anInt1322 = i;
        aString1027 = text;
        prepareLoginUI();
        if (titleArchive == null) {
            super.drawLoadingText(i, text);
            return;
        }
        loginboxElement.pushPixels();
        char c = '\u0168';
        char c1 = '\310';
        byte byte0 = 20;
        loginScreenFont.drawHorizontallyCenteredString(c / 2, c1 / 2 - 26 - byte0, 0xffffff,
                "RuneScape is loading - please wait...");
        int j = c1 / 2 - 18 - byte0;
        Drawable.drawRect(c / 2 - 152, j, 304, 34, 0x8c1111);
        Drawable.drawRect(c / 2 - 151, j + 1, 302, 32, 0);
        Drawable.drawFullRect(c / 2 - 150, j + 2, i * 3, 30, 0x8c1111);
        Drawable.drawFullRect((c / 2 - 150) + i * 3, j + 2, 300 - i * 3, 30, 0);
        loginScreenFont.drawHorizontallyCenteredString(c / 2, (c1 / 2 + 5) - byte0, 0xffffff, text);
        loginboxElement.drawImage(202, 171, super.graphics);
        if (shouldRenderUI) {
            shouldRenderUI = false;
            if (!isThreadStarted) {
                loginFlameLeft.drawImage(0, 0, super.graphics);
                loginFlameRight.drawImage(637, 0, super.graphics);
            }
            loginBackground_1.drawImage(128, 0, super.graphics);
            loginBackground_2.drawImage(202, 371, super.graphics);
            loginBackground_3.drawImage(0, 265, super.graphics);
            loginBackground_4.drawImage(562, 265, super.graphics);
            loginBackground_5.drawImage(128, 171, super.graphics);
            loginBackground_6.drawImage(562, 171, super.graphics);
        }
        presentBackBuffer();
    }

    public void loadPixelsLoginScreen_139() {
        byte[] titleBytes = titleArchive.get("title.dat");
        RgbSprite tmpSprite = new RgbSprite(titleBytes, this);
        loginFlameLeft.pushPixels();
        tmpSprite.method459(0, 0);
        loginFlameRight.pushPixels();
        tmpSprite.method459(0, -637);
        loginBackground_1.pushPixels();
        tmpSprite.method459(0, -128);
        loginBackground_2.pushPixels();
        tmpSprite.method459(-371, -202);
        loginboxElement.pushPixels();
        tmpSprite.method459(-171, -202);
        loginBackground_3.pushPixels();
        tmpSprite.method459(-265, 0);
        loginBackground_4.pushPixels();
        tmpSprite.method459(-265, -562);
        loginBackground_5.pushPixels();
        tmpSprite.method459(-171, -128);
        loginBackground_6.pushPixels();
        tmpSprite.method459(-171, -562);
        int[] ai = new int[tmpSprite.width_1490];
        for (int i = 0; i < tmpSprite.height_1491; i++) {
            for (int j = 0; j < tmpSprite.width_1490; j++) {
                ai[j] = tmpSprite.pixels_1489[(tmpSprite.width_1490 - j - 1)
                        + tmpSprite.width_1490 * i];
            }

            for (int l = 0; l < tmpSprite.width_1490; l++) {
                tmpSprite.pixels_1489[l + tmpSprite.width_1490 * i] = ai[l];
            }
        }

        loginFlameLeft.pushPixels();
        tmpSprite.method459(0, 382);
        loginFlameRight.pushPixels();
        tmpSprite.method459(0, -255);
        loginBackground_1.pushPixels();
        tmpSprite.method459(0, 254);
        loginBackground_2.pushPixels();
        tmpSprite.method459(-371, 180);
        loginboxElement.pushPixels();
        tmpSprite.method459(-171, 180);
        loginBackground_3.pushPixels();
        tmpSprite.method459(-265, 382);
        loginBackground_4.pushPixels();
        tmpSprite.method459(-265, -180);
        loginBackground_5.pushPixels();
        tmpSprite.method459(-171, 254);
        loginBackground_6.pushPixels();
        tmpSprite.method459(-171, -180);
        tmpSprite = new RgbSprite(titleArchive, "logo", 0);
        loginBackground_1.pushPixels();
        // draw the logo on the login screen
        tmpSprite.method461(18, 382 - tmpSprite.width_1490 / 2 - 128, -488);
        tmpSprite = null;
        titleBytes = null;
        ai = null;
        System.gc();
    }

    public void method140(byte byte0, GameObjectSpawnRequest class50_sub2) {
        int i = 0;
        int j = -1;
        int k = 0;
        int l = 0;
        if (byte0 != -61)
            outBuffer.putByte(175);
        if (class50_sub2.anInt1392 == 0)
            i = sceneGraph.method267(class50_sub2.anInt1391, class50_sub2.anInt1393, class50_sub2.anInt1394);
        if (class50_sub2.anInt1392 == 1)
            i = sceneGraph.method268(class50_sub2.anInt1393, (byte) 4, class50_sub2.anInt1391,
                    class50_sub2.anInt1394);
        if (class50_sub2.anInt1392 == 2)
            i = sceneGraph.method269(class50_sub2.anInt1391, class50_sub2.anInt1393, class50_sub2.anInt1394);
        if (class50_sub2.anInt1392 == 3)
            i = sceneGraph.method270(class50_sub2.anInt1391, class50_sub2.anInt1393, class50_sub2.anInt1394);
        if (i != 0) {
            int i1 = sceneGraph.method271(class50_sub2.anInt1391, class50_sub2.anInt1393, class50_sub2.anInt1394, i);
            j = i >> 14 & 0x7fff;
            k = i1 & 0x1f;
            l = i1 >> 6;
        }
        class50_sub2.anInt1387 = j;
        class50_sub2.anInt1389 = k;
        class50_sub2.anInt1388 = l;
    }

    public void resetWhenBoolTrue() {
        isThreadStarted = false;
        while (delayedResetter1320) {
            isThreadStarted = false;
            try {
                Thread.sleep(50L);
            } catch (Exception _ex) {
            }
        }
        titlebox_1292 = null;
        titlebutton_1293 = null;
        runes_array1117 = null;
        anIntArray1310 = null;
        anIntArray1311 = null;
        anIntArray1312 = null;
        anIntArray1313 = null;
        anIntArray1176 = null;
        anIntArray1177 = null;
        anIntArray1084 = null;
        anIntArray1085 = null;
        sprite_1017 = null;
        sprite_1018 = null;
    }

    public void drawInterface(int x, int y, JagInterface jagInterface, int k, int l) {
        if (jagInterface.type != 0 || jagInterface.anIntArray258 == null)
            return;
        if (jagInterface.aBoolean219 && currentlyHovered1302 != jagInterface.id && anInt1280 != jagInterface.id
                && anInt1106 != jagInterface.id)
            return;
        int i1 = Drawable.startX;
        int j1 = Drawable.startY;
        int k1 = Drawable.endX;
        int l1 = Drawable.endY;
        Drawable.recalcEdges(x, y, x + jagInterface.height, y + jagInterface.width, true);
        int i2 = jagInterface.anIntArray258.length;
        if (l != 8)
            opcode = -1;
        for (int j2 = 0; j2 < i2; j2++) {
            int k2 = jagInterface.anIntArray232[j2] + y;
            int l2 = (jagInterface.anIntArray276[j2] + x) - k;
            JagInterface class13_1 = JagInterface.forId(jagInterface.anIntArray258[j2]);
            k2 += class13_1.anInt228;
            l2 += class13_1.anInt259;
            if (class13_1.anInt242 > 0)
                method103((byte) 2, class13_1);
            if (class13_1.type == 0) {
                if (class13_1.anInt231 > class13_1.anInt285 - class13_1.height)
                    class13_1.anInt231 = class13_1.anInt285 - class13_1.height;
                if (class13_1.anInt231 < 0)
                    class13_1.anInt231 = 0;
                drawInterface(l2, k2, class13_1, class13_1.anInt231, 8);
                if (class13_1.anInt285 > class13_1.height)
                    method56(true, class13_1.anInt231, k2 + class13_1.width, class13_1.height, class13_1.anInt285,
                            l2);
            } else if (class13_1.type != 1)
                if (class13_1.type == 2) {
                    int i3 = 0;
                    for (int i4 = 0; i4 < class13_1.height; i4++) {
                        for (int j5 = 0; j5 < class13_1.width; j5++) {
                            int i6 = k2 + j5 * (32 + class13_1.anInt263);
                            int l6 = l2 + i4 * (32 + class13_1.anInt244);
                            if (i3 < 20) {
                                i6 += class13_1.anIntArray221[i3];
                                l6 += class13_1.anIntArray213[i3];
                            }
                            if (class13_1.itemIds[i3] > 0) {
                                int i7 = 0;
                                int j8 = 0;
                                int l10 = class13_1.itemIds[i3] - 1;
                                if (i6 > Drawable.startX - 32 && i6 < Drawable.endX
                                        && l6 > Drawable.startY - 32 && l6 < Drawable.endY
                                        || anInt1113 != 0 && anInt1112 == i3) {
                                    int k11 = 0;
                                    if (anInt1146 == 1 && itemIndexId == i3 && itemInterfaceId == class13_1.id)
                                        k11 = 0xffffff;
                                    RgbSprite class50_sub1_sub1_sub1_2 = ItemDefinition.method221(
                                            (byte) -33, k11, class13_1.itemAmounts[i3], l10);
                                    if (class50_sub1_sub1_sub1_2 != null) {
                                        if (anInt1113 != 0 && anInt1112 == i3 && anInt1111 == class13_1.id) {
                                            i7 = super.mouseX - anInt1114;
                                            j8 = super.mouseY - anInt1115;
                                            if (i7 < 5 && i7 > -5)
                                                i7 = 0;
                                            if (j8 < 5 && j8 > -5)
                                                j8 = 0;
                                            if (anInt1269 < 5) {
                                                i7 = 0;
                                                j8 = 0;
                                            }
                                            class50_sub1_sub1_sub1_2.method463(0, i6 + i7, l6 + j8, 128);
                                            if (l6 + j8 < Drawable.startY && jagInterface.anInt231 > 0) {
                                                int i12 = (anInt951 * (Drawable.startY - l6 - j8)) / 3;
                                                if (i12 > anInt951 * 10)
                                                    i12 = anInt951 * 10;
                                                if (i12 > jagInterface.anInt231)
                                                    i12 = jagInterface.anInt231;
                                                jagInterface.anInt231 -= i12;
                                                anInt1115 += i12;
                                            }
                                            if (l6 + j8 + 32 > Drawable.endY
                                                    && jagInterface.anInt231 < jagInterface.anInt285 - jagInterface.height) {
                                                int j12 = (anInt951 * ((l6 + j8 + 32) - Drawable.endY)) / 3;
                                                if (j12 > anInt951 * 10)
                                                    j12 = anInt951 * 10;
                                                if (j12 > jagInterface.anInt285 - jagInterface.height - jagInterface.anInt231)
                                                    j12 = jagInterface.anInt285 - jagInterface.height - jagInterface.anInt231;
                                                jagInterface.anInt231 += j12;
                                                anInt1115 -= j12;
                                            }
                                        } else if (anInt1332 != 0 && anInt1331 == i3 && anInt1330 == class13_1.id)
                                            class50_sub1_sub1_sub1_2.method463(0, i6, l6, 128);
                                        else
                                            class50_sub1_sub1_sub1_2.method461(l6, i6, -488);
                                        if (class50_sub1_sub1_sub1_2.width_1494 == 33 || class13_1.itemAmounts[i3] != 1) {
                                            int k12 = class13_1.itemAmounts[i3];
                                            font_p11_full.drawString_474(addMoneySuffix(k12, -243), 2245, i6 + 1 + i7, 0, l6 + 10 + j8
                                            );
                                            font_p11_full.drawString_474(addMoneySuffix(k12, -243), 2245, i6 + i7, 0xffff00,
                                                    l6 + 9 + j8);
                                        }
                                    }
                                }
                            } else if (class13_1.aClass50_Sub1_Sub1_Sub1Array265 != null && i3 < 20) {
                                RgbSprite class50_sub1_sub1_sub1_1 = class13_1.aClass50_Sub1_Sub1_Sub1Array265[i3];
                                if (class50_sub1_sub1_sub1_1 != null)
                                    class50_sub1_sub1_sub1_1.method461(l6, i6, -488);
                            }
                            i3++;
                        }

                    }

                } else if (class13_1.type == 3) {
                    boolean flag = false;
                    if (anInt1106 == class13_1.id || anInt1280 == class13_1.id
                            || currentlyHovered1302 == class13_1.id)
                        flag = true;
                    int j3;
                    if (method95(class13_1, -693)) {
                        j3 = class13_1.anInt260;
                        if (flag && class13_1.anInt226 != 0)
                            j3 = class13_1.anInt226;
                    } else {
                        j3 = class13_1.anInt240;
                        if (flag && class13_1.anInt261 != 0)
                            j3 = class13_1.anInt261;
                    }
                    if (class13_1.aByte220 == 0) {
                        if (class13_1.visible)
                            Drawable.drawFullRect(k2, l2, class13_1.width, class13_1.height, j3);
                        else
                            Drawable.drawRect(k2, l2, class13_1.width, class13_1.height, j3);
                    } else if (class13_1.visible) {
                        Drawable.drawTransparentFullRect(k2, l2, class13_1.width, class13_1.height, j3,
                                256 - (class13_1.aByte220 & 0xff));
                    } else {
                        Drawable.drawTransparentRect(k2, l2, class13_1.width, class13_1.height, j3,
                                256 - (class13_1.aByte220 & 0xff));
                    }
                } else if (class13_1.type == 4) {
                    JagFont class50_sub1_sub1_sub2 = class13_1.aClass50_Sub1_Sub1_Sub2_237;
                    String s = class13_1.aString230;
                    boolean flag1 = false;
                    if (anInt1106 == class13_1.id || anInt1280 == class13_1.id
                            || currentlyHovered1302 == class13_1.id)
                        flag1 = true;
                    int j4;
                    if (method95(class13_1, -693)) {
                        j4 = class13_1.anInt260;
                        if (flag1 && class13_1.anInt226 != 0)
                            j4 = class13_1.anInt226;
                        if (class13_1.aString249.length() > 0)
                            s = class13_1.aString249;
                    } else {
                        j4 = class13_1.anInt240;
                        if (flag1 && class13_1.anInt261 != 0)
                            j4 = class13_1.anInt261;
                    }
                    if (class13_1.anInt289 == 6 && aBoolean1239) {
                        s = "Please wait...";
                        j4 = class13_1.anInt240;
                    }
                    if (Drawable.width == 479) {
                        if (j4 == 0xffff00)
                            j4 = 255;
                        if (j4 == 49152)
                            j4 = 0xffffff;
                    }
                    for (int j7 = l2 + class50_sub1_sub1_sub2.anInt1506; s.length() > 0; j7 += class50_sub1_sub1_sub2.anInt1506) {
                        if (s.indexOf("%") != -1) {
                            do {
                                int k8 = s.indexOf("%1");
                                if (k8 == -1)
                                    break;
                                s = s.substring(0, k8) + method89(method129(3, 0, class13_1), 8) + s.substring(k8 + 2);
                            } while (true);
                            do {
                                int l8 = s.indexOf("%2");
                                if (l8 == -1)
                                    break;
                                s = s.substring(0, l8) + method89(method129(3, 1, class13_1), 8) + s.substring(l8 + 2);
                            } while (true);
                            do {
                                int i9 = s.indexOf("%3");
                                if (i9 == -1)
                                    break;
                                s = s.substring(0, i9) + method89(method129(3, 2, class13_1), 8) + s.substring(i9 + 2);
                            } while (true);
                            do {
                                int j9 = s.indexOf("%4");
                                if (j9 == -1)
                                    break;
                                s = s.substring(0, j9) + method89(method129(3, 3, class13_1), 8) + s.substring(j9 + 2);
                            } while (true);
                            do {
                                int k9 = s.indexOf("%5");
                                if (k9 == -1)
                                    break;
                                s = s.substring(0, k9) + method89(method129(3, 4, class13_1), 8) + s.substring(k9 + 2);
                            } while (true);
                        }
                        int l9 = s.indexOf("\\n");
                        String s3;
                        if (l9 != -1) {
                            s3 = s.substring(0, l9);
                            s = s.substring(l9 + 2);
                        } else {
                            s3 = s;
                            s = "";
                        }
                        if (class13_1.aBoolean272)
                            class50_sub1_sub1_sub2.drawString(s3, k2
                                    + class13_1.width / 2, j7, class13_1.aBoolean247, j4);
                        else
                            class50_sub1_sub1_sub2.drawString(s3, j4, k2, j7, class13_1.aBoolean247);
                    }

                } else if (class13_1.type == 5) {
                    RgbSprite class50_sub1_sub1_sub1;
                    if (method95(class13_1, -693))
                        class50_sub1_sub1_sub1 = class13_1.aClass50_Sub1_Sub1_Sub1_245;
                    else
                        class50_sub1_sub1_sub1 = class13_1.aClass50_Sub1_Sub1_Sub1_212;
                    if (class50_sub1_sub1_sub1 != null)
                        class50_sub1_sub1_sub1.method461(l2, k2, -488);
                } else if (class13_1.type == 6) {
                    int k3 = ThreeDimensionalCanvas.halfParentWidth;
                    int k4 = ThreeDimensionalCanvas.halfParentHeight;
                    ThreeDimensionalCanvas.halfParentWidth = k2 + class13_1.width / 2;
                    ThreeDimensionalCanvas.halfParentHeight = l2 + class13_1.height / 2;
                    int k5 = ThreeDimensionalCanvas.sineTable[class13_1.anInt252] * class13_1.anInt251 >> 16;
                    int j6 = ThreeDimensionalCanvas.cosineTable[class13_1.anInt252] * class13_1.anInt251 >> 16;
                    boolean flag2 = method95(class13_1, -693);
                    int k7;
                    if (flag2)
                        k7 = class13_1.anInt287;
                    else
                        k7 = class13_1.anInt286;
                    Model class50_sub1_sub4_sub4;
                    if (k7 == -1) {
                        class50_sub1_sub4_sub4 = class13_1.method203(-1, -1, flag2);
                    } else {
                        Animation class14 = Animation.animations[k7];
                        class50_sub1_sub4_sub4 = class13_1.method203(class14.anIntArray295[class13_1.anInt235],
                                class14.anIntArray296[class13_1.anInt235], flag2);
                    }
                    if (class50_sub1_sub4_sub4 != null)
                        class50_sub1_sub4_sub4.viewportTransform(0, class13_1.anInt253, 0, class13_1.anInt252, 0, k5, j6);
                    ThreeDimensionalCanvas.halfParentWidth = k3;
                    ThreeDimensionalCanvas.halfParentHeight = k4;
                } else {
                    if (class13_1.type == 7) {
                        JagFont class50_sub1_sub1_sub2_1 = class13_1.aClass50_Sub1_Sub1_Sub2_237;
                        int l4 = 0;
                        for (int l5 = 0; l5 < class13_1.height; l5++) {
                            for (int k6 = 0; k6 < class13_1.width; k6++) {
                                if (class13_1.itemIds[l4] > 0) {
                                    ItemDefinition class16 = ItemDefinition.forId(class13_1.itemIds[l4] - 1);
                                    String s6 = String.valueOf(class16.name);
                                    if (class16.stackable || class13_1.itemAmounts[l4] != 1)
                                        s6 = s6 + " x" + method135(0, class13_1.itemAmounts[l4]);
                                    int i10 = k2 + k6 * (115 + class13_1.anInt263);
                                    int i11 = l2 + l5 * (12 + class13_1.anInt244);
                                    if (class13_1.aBoolean272)
                                        class50_sub1_sub1_sub2_1.drawString(s6, i10 + class13_1.width / 2, i11, class13_1.aBoolean247,
                                                class13_1.anInt240);
                                    else
                                        class50_sub1_sub1_sub2_1.drawString(s6, class13_1.anInt240, i10, i11,
                                                class13_1.aBoolean247);
                                }
                                l4++;
                            }

                        }

                    }
                    if (class13_1.type == 8
                            && (anInt1284 == class13_1.id || anInt1044 == class13_1.id || currentlyHovered1129 == class13_1.id)
                            && anInt893 == 100) {
                        int l3 = 0;
                        int i5 = 0;
                        JagFont class50_sub1_sub1_sub2_2 = fontChatboxButtons;
                        for (String s1 = class13_1.aString230; s1.length() > 0; ) {
                            int l7 = s1.indexOf("\\n");
                            String s4;
                            if (l7 != -1) {
                                s4 = s1.substring(0, l7);
                                s1 = s1.substring(l7 + 2);
                            } else {
                                s4 = s1;
                                s1 = "";
                            }
                            int j10 = class50_sub1_sub1_sub2_2.method472((byte) 35, s4);
                            if (j10 > l3)
                                l3 = j10;
                            i5 += class50_sub1_sub1_sub2_2.anInt1506 + 1;
                        }

                        l3 += 6;
                        i5 += 7;
                        int i8 = (k2 + class13_1.width) - 5 - l3;
                        int k10 = l2 + class13_1.height + 5;
                        if (i8 < k2 + 5)
                            i8 = k2 + 5;
                        if (i8 + l3 > y + jagInterface.width)
                            i8 = (y + jagInterface.width) - l3;
                        if (k10 + i5 > x + jagInterface.height)
                            k10 = (x + jagInterface.height) - i5;
                        Drawable.drawFullRect(i8, k10, l3, i5, 0xffffa0);
                        Drawable.drawRect(i8, k10, l3, i5, 0);
                        String s2 = class13_1.aString230;
                        for (int j11 = k10 + class50_sub1_sub1_sub2_2.anInt1506 + 2; s2.length() > 0; j11 += class50_sub1_sub1_sub2_2.anInt1506 + 1) {
                            int l11 = s2.indexOf("\\n");
                            String s5;
                            if (l11 != -1) {
                                s5 = s2.substring(0, l11);
                                s2 = s2.substring(l11 + 2);
                            } else {
                                s5 = s2;
                                s2 = "";
                            }
                            class50_sub1_sub1_sub2_2.drawString(s5, 0, i8 + 3, j11, false);
                        }

                    }
                }
        }

        Drawable.recalcEdges(j1, i1, l1, k1, true);
    }

    public void loadingStages() {
        if (lowMemory && loadingStage == 2 && Region.plane != plane) {
            method125(null, "Loading - please wait.");
            loadingStage = 1;
            aLong1229 = System.currentTimeMillis();
        }
        if (loadingStage == 1) {
            int i = initializeRegions();
            if (i != 0 && System.currentTimeMillis() - aLong1229 > 0x57e40L) {
                signlink.reporterror(thisPlayerName + " glcfb " + serverSeed + "," + i + "," + lowMemory + ","
                        + stores[0] + "," + fileFetcher.method333() + "," + plane + ","
                        + chunkX + "," + chunkY);
                aLong1229 = System.currentTimeMillis();
            }
        }
        if (loadingStage == 2 && plane != anInt1276) {
            anInt1276 = plane;
            method115(plane, 0);
        }
    }

    public int initializeRegions() {
        for (int j = 0; j < aByteArrayArray838.length; j++) {
            if (aByteArrayArray838[j] == null && anIntArray857[j] != -1)
                return -1;
            if (aByteArrayArray1232[j] == null && anIntArray858[j] != -1)
                return -2;
        }

        boolean regionsCached = true;
        for (int k = 0; k < aByteArrayArray838.length; k++) {
            byte objects[] = aByteArrayArray1232[k];
            if (objects != null) {
                int blockX = (coordinates[k] >> 8) * 64 - nextTopLeftTileX;
                int blockY = (coordinates[k] & 0xff) * 64 - nextTopLeftTileY;
                if (aBoolean1163) {
                    blockX = 10;
                    blockY = 10;
                }
                regionsCached &= Region.isCached(blockX, blockY, objects);
            }
        }

        if (!regionsCached)
            return -3;
        if (mapLoading) {
            return -4;
        } else {
            loadingStage = 2;
            Region.plane = plane;
            loadRegion();
            outBuffer.putOpcode(6);
            return 0;
        }
    }

    public void method145(boolean flag, int plane, int x, int y, int k, int l, int i1, int j1, int k1, int l1) {
        GameObjectSpawnRequest class50_sub2 = null;
        for (GameObjectSpawnRequest class50_sub2_1 = (GameObjectSpawnRequest) gameObjectSpawnsRequestList.first(); class50_sub2_1 != null; class50_sub2_1 = (GameObjectSpawnRequest) gameObjectSpawnsRequestList
                .next()) {
            if (class50_sub2_1.anInt1391 != plane || class50_sub2_1.anInt1393 != x || class50_sub2_1.anInt1394 != y
                    || class50_sub2_1.anInt1392 != l1)
                continue;
            class50_sub2 = class50_sub2_1;
            break;
        }

        if (class50_sub2 == null) {
            class50_sub2 = new GameObjectSpawnRequest();
            class50_sub2.anInt1391 = plane;
            class50_sub2.anInt1392 = l1;
            class50_sub2.anInt1393 = x;
            class50_sub2.anInt1394 = y;
            method140((byte) -61, class50_sub2);
            gameObjectSpawnsRequestList.addLast(class50_sub2);
        }
        class50_sub2.anInt1384 = j1;
        class50_sub2.anInt1386 = i1;
        class50_sub2.anInt1385 = k;
        class50_sub2.anInt1395 = k1;
        class50_sub2.delayUntilRespawn = l;
        isLoggedIn &= flag;
    }

    public void updateMinimapClick() {
        if (minimapState != 0) {
            return;
        }
        if (super.anInt28 == 1) {
            int i = super.anInt29 - 25 - layout.minimap.x;
            int j = super.anInt30 - 5 - layout.minimap.y;
            if (i >= 0 && j >= 0 && i < 146 && j < 151) {
                i -= 73;
                j -= 75;
                int k = anInt1252 + anInt916 & 0x7ff;
                int l = ThreeDimensionalCanvas.sineTable[k];
                int i1 = ThreeDimensionalCanvas.cosineTable[k];
                l = l * (anInt1233 + 256) >> 8;
                i1 = i1 * (anInt1233 + 256) >> 8;
                int j1 = j * l + i * i1 >> 11;
                int k1 = j * i1 - i * l >> 11;
                int l1 = ((Actor) (thisPlayer)).unitX + j1 >> 7;
                int i2 = ((Actor) (thisPlayer)).unitY - k1 >> 7;
                boolean flag = walk(true, false, i2, ((Actor) (thisPlayer)).walkingQueueY[0], 0, 0, 1, 0, l1,
                        0, 0, ((Actor) (thisPlayer)).walkingQueueX[0]);
                if (flag) {
                    outBuffer.putByte(i);
                    outBuffer.putByte(j);
                    outBuffer.putShort(anInt1252);
                    outBuffer.putByte(57);
                    outBuffer.putByte(anInt916);
                    outBuffer.putByte(anInt1233);
                    outBuffer.putByte(89);
                    outBuffer.putShort(((Actor) (thisPlayer)).unitX);
                    outBuffer.putShort(((Actor) (thisPlayer)).unitY);
                    outBuffer.putByte(anInt1126);
                    outBuffer.putByte(63);
                }
            }
        }
    }

    public void resetAllImageProducers() {
        if (super.imageProducer != null) {
            return;
        }
        resetWhenBoolTrue();
        loginBackground_1 = null;
        loginBackground_2 = null;
        loginboxElement = null;
        loginFlameLeft = null;
        loginFlameRight = null;
        loginBackground_3 = null;
        loginBackground_4 = null;
        loginBackground_5 = null;
        loginBackground_6 = null;
        chatboxImage_1159 = null;
        aClass18_1157 = null;
        inventoryImage = null;
        gameViewportImage = null;
        chatboxButtons = null;
        aClass18_1109 = null;
        aClass18_1110 = null;
        super.imageProducer = new JagImageProducer(clientWidth, clientHeight, getParentComponent());
        shouldRenderUI = true;
    }

    public boolean method148(int i, String s) {
        if (s == null) {
            return false;
        }
        for (int j = 0; j < friendsCount; j++) {
            if (s.equalsIgnoreCase(aStringArray849[j])) {
                return true;
            }
        }
        return s.equalsIgnoreCase(thisPlayer.username);
    }

    public void method149(int i) {
        while (i >= 0) {
            opcode = buffer.getByte();
        }
        if (loginScreenState == 0) {
            int j = super.width / 2 - 80;
            int i1 = super.height / 2 + 20;
            i1 += 20;
            if (super.anInt28 == 1 && super.anInt29 >= j - 75 && super.anInt29 <= j + 75 && super.anInt30 >= i1 - 20
                    && super.anInt30 <= i1 + 20) {
                loginScreenState = 3;
                anInt977 = 0;
            }
            j = super.width / 2 + 80;
            if (super.anInt28 == 1 && super.anInt29 >= j - 75 && super.anInt29 <= j + 75 && super.anInt30 >= i1 - 20
                    && super.anInt30 <= i1 + 20) {
                statusLineOne = "";
                statusLineTwo = "Enter your username & password.";
                loginScreenState = 2;
                anInt977 = 0;
                return;
            }
        } else {
            if (loginScreenState == 2) {
                int k = super.height / 2 - 40;
                k += 30;
                k += 25;
                if (super.anInt28 == 1 && super.anInt30 >= k - 15 && super.anInt30 < k)
                    anInt977 = 0;
                k += 15;
                if (super.anInt28 == 1 && super.anInt30 >= k - 15 && super.anInt30 < k)
                    anInt977 = 1;
                k += 15;
                int j1 = super.width / 2 - 80;
                int l1 = super.height / 2 + 50;
                l1 += 20;
                if (super.anInt28 == 1 && super.anInt29 >= j1 - 75 && super.anInt29 <= j1 + 75
                        && super.anInt30 >= l1 - 20 && super.anInt30 <= l1 + 20) {
                    anInt850 = 0;
                    login(thisPlayerName, thisPlayerPassword, false);
                    if (isLoggedIn)
                        return;
                }
                j1 = super.width / 2 + 80;
                if (super.anInt28 == 1 && super.anInt29 >= j1 - 75 && super.anInt29 <= j1 + 75
                        && super.anInt30 >= l1 - 20 && super.anInt30 <= l1 + 20) {
                    loginScreenState = 0;
                    //thisPlayerName = "";
                    //aString1093 = "";
                }
                do {
                    int i2 = readCharFromChatbox();
                    if (i2 == -1)
                        break;
                    boolean flag = false;
                    for (int j2 = 0; j2 < aString1007.length(); j2++) {
                        if (i2 != aString1007.charAt(j2))
                            continue;
                        flag = true;
                        break;
                    }

                    if (anInt977 == 0) {
                        if (i2 == 8 && thisPlayerName.length() > 0)
                            thisPlayerName = thisPlayerName.substring(0, thisPlayerName.length() - 1);
                        if (i2 == 9 || i2 == 10 || i2 == 13)
                            anInt977 = 1;
                        if (flag)
                            thisPlayerName += (char) i2;
                        if (thisPlayerName.length() > 12)
                            thisPlayerName = thisPlayerName.substring(0, 12);
                    } else if (anInt977 == 1) {
                        if (i2 == 8 && thisPlayerPassword.length() > 0)
                            thisPlayerPassword = thisPlayerPassword.substring(0, thisPlayerPassword.length() - 1);
                        if (i2 == 9 || i2 == 10 || i2 == 13)
                            anInt977 = 0;
                        if (flag)
                            thisPlayerPassword += (char) i2;
                        if (thisPlayerPassword.length() > 20)
                            thisPlayerPassword = thisPlayerPassword.substring(0, 20);
                    }
                } while (true);
                return;
            }
            if (loginScreenState == 3) {
                int l = super.width / 2;
                int k1 = super.height / 2 + 50;
                k1 += 20;
                if (super.anInt28 == 1 && super.anInt29 >= l - 75 && super.anInt29 <= l + 75
                        && super.anInt30 >= k1 - 20 && super.anInt30 <= k1 + 20)
                    loginScreenState = 0;
            }
        }
    }

    public void method150(int i, int j, int k, int l, int i1, int j1) {
        int k1 = sceneGraph.method267(j, k, i);
        i1 = 62 / i1;
        if (k1 != 0) {
            int l1 = sceneGraph.method271(j, k, i, k1);
            int k2 = l1 >> 6 & 3;
            int i3 = l1 & 0x1f;
            int k3 = j1;
            if (k1 > 0)
                k3 = l;
            int ai[] = rbgSprite_1122.pixels_1489;
            int k4 = 24624 + k * 4 + (103 - i) * 512 * 4;
            int i5 = k1 >> 14 & 0x7fff;
            ObjectDefinition class47_2 = ObjectDefinition.forId(i5);
            if (class47_2.anInt795 != -1) {
                IndexedSprite class50_sub1_sub1_sub3_2 = aClass50_Sub1_Sub1_Sub3Array1153[class47_2.anInt795];
                if (class50_sub1_sub1_sub3_2 != null) {
                    int i6 = (class47_2.anInt801 * 4 - class50_sub1_sub1_sub3_2.width_1518) / 2;
                    int j6 = (class47_2.anInt775 * 4 - class50_sub1_sub1_sub3_2.height_1519) / 2;
                    class50_sub1_sub1_sub3_2.drawSprite(48 + k * 4 + i6, 48 + (104 - i - class47_2.anInt775) * 4 + j6
                    );
                }
            } else {
                if (i3 == 0 || i3 == 2)
                    if (k2 == 0) {
                        ai[k4] = k3;
                        ai[k4 + 512] = k3;
                        ai[k4 + 1024] = k3;
                        ai[k4 + 1536] = k3;
                    } else if (k2 == 1) {
                        ai[k4] = k3;
                        ai[k4 + 1] = k3;
                        ai[k4 + 2] = k3;
                        ai[k4 + 3] = k3;
                    } else if (k2 == 2) {
                        ai[k4 + 3] = k3;
                        ai[k4 + 3 + 512] = k3;
                        ai[k4 + 3 + 1024] = k3;
                        ai[k4 + 3 + 1536] = k3;
                    } else if (k2 == 3) {
                        ai[k4 + 1536] = k3;
                        ai[k4 + 1536 + 1] = k3;
                        ai[k4 + 1536 + 2] = k3;
                        ai[k4 + 1536 + 3] = k3;
                    }
                if (i3 == 3)
                    if (k2 == 0)
                        ai[k4] = k3;
                    else if (k2 == 1)
                        ai[k4 + 3] = k3;
                    else if (k2 == 2)
                        ai[k4 + 3 + 1536] = k3;
                    else if (k2 == 3)
                        ai[k4 + 1536] = k3;
                if (i3 == 2)
                    if (k2 == 3) {
                        ai[k4] = k3;
                        ai[k4 + 512] = k3;
                        ai[k4 + 1024] = k3;
                        ai[k4 + 1536] = k3;
                    } else if (k2 == 0) {
                        ai[k4] = k3;
                        ai[k4 + 1] = k3;
                        ai[k4 + 2] = k3;
                        ai[k4 + 3] = k3;
                    } else if (k2 == 1) {
                        ai[k4 + 3] = k3;
                        ai[k4 + 3 + 512] = k3;
                        ai[k4 + 3 + 1024] = k3;
                        ai[k4 + 3 + 1536] = k3;
                    } else if (k2 == 2) {
                        ai[k4 + 1536] = k3;
                        ai[k4 + 1536 + 1] = k3;
                        ai[k4 + 1536 + 2] = k3;
                        ai[k4 + 1536 + 3] = k3;
                    }
            }
        }
        k1 = sceneGraph.method269(j, k, i);
        if (k1 != 0) {
            int i2 = sceneGraph.method271(j, k, i, k1);
            int l2 = i2 >> 6 & 3;
            int j3 = i2 & 0x1f;
            int l3 = k1 >> 14 & 0x7fff;
            ObjectDefinition class47_1 = ObjectDefinition.forId(l3);
            if (class47_1.anInt795 != -1) {
                IndexedSprite class50_sub1_sub1_sub3_1 = aClass50_Sub1_Sub1_Sub3Array1153[class47_1.anInt795];
                if (class50_sub1_sub1_sub3_1 != null) {
                    int j5 = (class47_1.anInt801 * 4 - class50_sub1_sub1_sub3_1.width_1518) / 2;
                    int k5 = (class47_1.anInt775 * 4 - class50_sub1_sub1_sub3_1.height_1519) / 2;
                    class50_sub1_sub1_sub3_1.drawSprite(48 + k * 4 + j5, 48 + (104 - i - class47_1.anInt775) * 4 + k5
                    );
                }
            } else if (j3 == 9) {
                int l4 = 0xeeeeee;
                if (k1 > 0)
                    l4 = 0xee0000;
                int ai1[] = rbgSprite_1122.pixels_1489;
                int l5 = 24624 + k * 4 + (103 - i) * 512 * 4;
                if (l2 == 0 || l2 == 2) {
                    ai1[l5 + 1536] = l4;
                    ai1[l5 + 1024 + 1] = l4;
                    ai1[l5 + 512 + 2] = l4;
                    ai1[l5 + 3] = l4;
                } else {
                    ai1[l5] = l4;
                    ai1[l5 + 512 + 1] = l4;
                    ai1[l5 + 1024 + 2] = l4;
                    ai1[l5 + 1536 + 3] = l4;
                }
            }
        }
        k1 = sceneGraph.method270(j, k, i);
        if (k1 != 0) {
            int j2 = k1 >> 14 & 0x7fff;
            ObjectDefinition class47 = ObjectDefinition.forId(j2);
            if (class47.anInt795 != -1) {
                IndexedSprite class50_sub1_sub1_sub3 = aClass50_Sub1_Sub1_Sub3Array1153[class47.anInt795];
                if (class50_sub1_sub1_sub3 != null) {
                    int i4 = (class47.anInt801 * 4 - class50_sub1_sub1_sub3.width_1518) / 2;
                    int j4 = (class47.anInt775 * 4 - class50_sub1_sub1_sub3.height_1519) / 2;
                    class50_sub1_sub1_sub3.drawSprite(48 + k * 4 + i4, 48 + (104 - i - class47.anInt775) * 4 + j4);
                }
            }
        }
    }

    public void drawGameViewport() {
        tickCounter1138++;
        addPlayersToSceneGraph(true);
        addNpcsToScenegraph(true);
        addPlayersToSceneGraph(false);
        addNpcsToScenegraph(false);
        updateProjectiles();
        updateSpotAnimations();
        if (!aBoolean1211) {
            int j = anInt1251;
            if (anInt1289 / 256 > j)
                j = anInt1289 / 256;
            if (customCameraActive[4] && cameraAmplitude[4] + 128 > j)
                j = cameraAmplitude[4] + 128;
            int l = anInt1252 + anInt1255 & 0x7ff;
            updateCamera94(getFloorDrawHeight(((Actor) (thisPlayer)).unitY, ((Actor) (thisPlayer)).unitX,
                    plane) - 50, anInt1262, j, 600 + j * 3, l, anInt1263, (byte) -103);
        }
        int k;
        if (!aBoolean1211) {
            k = method117((byte) 1);
        } else {
            k = method118(-276);
        }
        int i1 = anInt1216;
        int j1 = anInt1217;
        int k1 = anInt1218;
        int l1 = anInt1219;
        int i2 = anInt1220;
        for (int j2 = 0; j2 < 5; j2++) {
            if (customCameraActive[j2]) {
                int k2 = (int) ((Math.random() * (double) (cameraJitter[j2] * 2 + 1) - (double) cameraJitter[j2]) + Math
                        .sin((double) unknownCameraVariable[j2] * ((double) cameraFrequency[j2] / 100D))
                        * (double) cameraAmplitude[j2]);
                if (j2 == 0) {
                    anInt1216 += k2;
                }
                if (j2 == 1) {
                    anInt1217 += k2;
                }
                if (j2 == 2) {
                    anInt1218 += k2;
                }
                if (j2 == 3)
                    anInt1220 = anInt1220 + k2 & 0x7ff;
                if (j2 == 4) {
                    anInt1219 += k2;
                    if (anInt1219 < 128)
                        anInt1219 = 128;
                    if (anInt1219 > 383)
                        anInt1219 = 383;
                }
            }
        }

        int l2 = ThreeDimensionalCanvas.anInt1547;
        Model.isPickingEnabled = true;
        Model.hoveredCount = 0;
        Model.mouseX = super.mouseX - layout.viewport.x;
        Model.mouseY = super.mouseY - layout.viewport.y;
        Drawable.clearScreen();
        sceneGraph.method280(anInt1216, k, 0, anInt1217, anInt1218, anInt1220, anInt1219);
        sceneGraph.method255(anInt897);
        method121(false);
        method127(true);
        animateTexture_65(l2);
        draw3dScreen();
        gameViewportImage.drawImage(layout.viewport, super.graphics);
        anInt1216 = i1;
        anInt1217 = j1;
        anInt1218 = k1;
        anInt1219 = l1;
        anInt1220 = i2;
    }

    public void method152(int i) {
        if (i != -23763)
            load();
        for (int j = 0; j < anInt1035; j++)
            if (anIntArray1259[j] <= 0) {
                boolean flag = false;
                try {
                    if (anIntArray1090[j] == anInt1272 && anIntArray1321[j] == anInt935) {
                        if (!method78(295))
                            flag = true;
                    } else {
                        JagBuffer class50_sub1_sub2 = Sound.forId(anIntArray1321[j], (byte) 6, anIntArray1090[j]);
                        if (System.currentTimeMillis() + (long) (class50_sub1_sub2.position / 22) > aLong1250
                                + (long) (anInt1179 / 22)) {
                            anInt1179 = class50_sub1_sub2.position;
                            aLong1250 = System.currentTimeMillis();
                            if (method116(3, class50_sub1_sub2.position, class50_sub1_sub2.buffer)) {
                                anInt1272 = anIntArray1090[j];
                                anInt935 = anIntArray1321[j];
                            } else {
                                flag = true;
                            }
                        }
                    }
                } catch (Exception exception) {
                    if (signlink.reporterror) {
                        outBuffer.putOpcode(80);
                        outBuffer.putShort(anIntArray1090[j] & 0x7fff);
                    } else {
                        outBuffer.putOpcode(80);
                        outBuffer.putShort(-1);
                    }
                }
                if (!flag || anIntArray1259[j] == -5) {
                    anInt1035--;
                    for (int k = j; k < anInt1035; k++) {
                        anIntArray1090[k] = anIntArray1090[k + 1];
                        anIntArray1321[k] = anIntArray1321[k + 1];
                        anIntArray1259[k] = anIntArray1259[k + 1];
                    }

                    j--;
                } else {
                    anIntArray1259[j] = -5;
                }
            } else {
                anIntArray1259[j]--;
            }

        if (anInt1128 > 0) {
            anInt1128 -= 20;
            if (anInt1128 < 0)
                anInt1128 = 0;
            if (anInt1128 == 0 && musicEnabled && !lowMemory) {
                anInt1270 = anInt1327;
                aBoolean1271 = true;
                fileFetcher.request(2, anInt1270);
            }
        }
    }

    public client() {
        archiveHashes = new int[9];
        aString839 = "";
        anIntArray843 = new int[Skills.anInt700];
        aStringArray849 = new String[200];
        cameraAmplitude = new int[5];
        anInt854 = 2;
        aString861 = "";
        aStringArray863 = new String[100];
        anIntArray864 = new int[100];
        aBoolean866 = false;
        constructedMapPalette = new int[4][13][13];
        anIntArrayArray885 = new int[104][104];
        anIntArrayArray886 = new int[104][104];
        aBoolean892 = false;
        anInt894 = -992;
        aClass50_Sub1_Sub1_Sub1Array896 = new RgbSprite[8];
        anInt897 = 559;
        aByte898 = 6;
        aBoolean900 = false;
        aByte901 = -123;
        anInt917 = 2;
        aBoolean918 = true;
        aBoolean919 = true;
        anIntArray920 = new int[151];
        anInt921 = 8;
        customCameraActive = new boolean[5];
        anInt928 = -188;
        tempBuffer = JagBuffer.allocate(1);
        anInt931 = 0x23201b;
        anInt932 = -1;
        anInt933 = -1;
        anInt935 = -1;
        aByte936 = -113;
        aString937 = "";
        anInt938 = -214;
        anInt940 = 50;
        anIntArray941 = new int[anInt940];
        anIntArray942 = new int[anInt940];
        anIntArray943 = new int[anInt940];
        anIntArray944 = new int[anInt940];
        anIntArray945 = new int[anInt940];
        anIntArray946 = new int[anInt940];
        anIntArray947 = new int[anInt940];
        aStringArray948 = new String[anInt940];
        chatboxInput = "";
        aBoolean950 = false;
        aClass50_Sub1_Sub1_Sub1Array954 = new RgbSprite[32];
        aByte956 = 1;
        statusLineOne = "";
        statusLineTwo = "";
        aBoolean959 = true;
        openInterfaceID = -1;
        thisPlayerServerId = -1;
        outBuffer = JagBuffer.allocate(1);
        anInt968 = 2048;
        thisPlayerId = 2047;
        players = new Player[anInt968];
        localPlayers = new int[anInt968];
        updatedPlayers = new int[anInt968];
        cachedAppearances = new JagBuffer[anInt968];
        aClass50_Sub1_Sub1_Sub3Array976 = new IndexedSprite[13];
        anIntArray979 = new int[500];
        anIntArray980 = new int[500];
        anIntArray981 = new int[500];
        anIntArray982 = new int[500];
        anInt988 = -1;
        cameraFrequency = new int[5];
        defaultLocalVarps = new int[2000];
        anInt1010 = 2;
        aBoolean1014 = false;
        aBoolean1016 = false;
        anIntArray1019 = new int[151];
        userInputString = "";
        aBoolean1028 = false;
        anIntArray1029 = new int[Skills.anInt700];
        aClass50_Sub1_Sub1_Sub1Array1031 = new RgbSprite[100];
        aBoolean1033 = false;
        aBoolean1038 = true;
        localVarps = new int[2000];
        shouldRenderUI = false;
        anInt1051 = 69;
        anInt1053 = -1;
        anIntArray1054 = new int[Skills.anInt700];
        anInt1055 = 2;
        anInt1056 = 3;
        isContextMenuActive = false;
        aByte1066 = 1;
        aBoolean1067 = false;
        aStringArray1069 = new String[5];
        aBooleanArray1070 = new boolean[5];
        anInt1072 = 20411;
        ignores = new long[100];
        anIntArray1077 = new int[1000];
        anIntArray1078 = new int[1000];
        aClass50_Sub1_Sub1_Sub1Array1079 = new RgbSprite[32];
        anInt1080 = 0x4d4233;
        aCRC32_1088 = new CRC32();
        anInt1089 = -1;
        anIntArray1090 = new int[50];
        thisPlayerName = "hydro";
        thisPlayerPassword = "hydro";
        aBoolean1097 = false;
        aBoolean1098 = false;
        anIntArray1099 = new int[5];
        chatInput = "";
        cameraJitter = new int[5];
        anInt1107 = 78;
        anInt1119 = -30658;
        walkingPathX = new int[4000];
        walkingPathY = new int[4000];
        aBoolean1127 = false;
        friends = new long[200];
        aClass50_Sub1_Sub2_1131 = new JagBuffer(new byte[5000]);
        npcs = new Npc[16384];
        anIntArray1134 = new int[16384];
        colorBrown1135 = 0x766654;
        aBoolean1136 = false;
        isLoggedIn = false;
        anInt1140 = -110;
        aClass50_Sub1_Sub1_Sub3Array1142 = new IndexedSprite[2];
        aByte1143 = -80;
        aBoolean1144 = true;
        unknownCameraVariable = new int[5];
        aClass50_Sub1_Sub1_Sub3Array1153 = new IndexedSprite[100];
        anInt1154 = -916;
        aBoolean1155 = false;
        aByte1161 = 97;
        aBoolean1163 = false;
        anIntArray1166 = new int[256];
        anInt1169 = -1;
        anInt1175 = -89;
        anInt1178 = 300;
        anIntArray1180 = new int[33];
        aBoolean1181 = false;
        spriteArray1182 = new RgbSprite[20];
        rightClickOptions = new String[500];
        buffer = JagBuffer.allocate(1);
        cost = new int[104][104];
        anInt1191 = -1;
        mapLoading = false;
        aClass6_1210 = new LinkedList();
        aBoolean1211 = false;
        aBoolean1212 = false;
        anInt1213 = -1;
        stores = new FileStore[5];
        anInt1231 = -1;
        anInt1234 = 1;
        anInt1236 = 326;
        aBoolean1239 = false;
        aBoolean1240 = false;
        isThreadStarted = false;
        aByteArray1245 = new byte[16384];
        aClass13_1249 = new JagInterface();
        anInt1251 = 128;
        anInt1256 = 1;
        anIntArray1258 = new int[100];
        anIntArray1259 = new int[50];
        clippingPlanes = new ClippingPlane[4];
        gameObjectSpawnsRequestList = new LinkedList();
        aBoolean1265 = false;
        musicEnabled = true;
        anIntArray1267 = new int[200];
        aBoolean1271 = true;
        anInt1272 = -1;
        aBoolean1274 = true;
        aBoolean1275 = true;
        anInt1276 = -1;
        aBoolean1277 = false;
        aClass50_Sub1_Sub1_Sub1Array1278 = new RgbSprite[1000];
        walkableInterfaceId = -1;
        anInt1281 = -939;
        projectileQueue = new LinkedList();
        aBoolean1283 = false;
        tabId = 3;
        anIntArray1286 = new int[33];
        anInt1287 = 0x332d25;
        aClass50_Sub1_Sub1_Sub1Array1288 = new RgbSprite[32];
        removePlayers = new int[1000];
        anIntArray1296 = new int[100];
        aStringArray1297 = new String[100];
        aStringArray1298 = new String[100];
        aBoolean1301 = true;
        isGameThreadStarted = false;
        aByte1317 = -58;
        anInt1318 = 416;
        delayedResetter1320 = false;
        anIntArray1321 = new int[50];
        groundItems = new LinkedList[4][104][104];
        anIntArray1326 = new int[7];
        anInt1327 = -1;
        anInt1328 = 409;
    }

    public static int anInt1333;

    static {
        anIntArray952 = new int[99];
        int i = 0;
        for (int j = 0; j < 99; j++) {
            int l = j + 1;
            int i1 = (int) ((double) l + 300D * Math.pow(2D, (double) l / 7D));
            i += i1;
            anIntArray952[j] = i / 4;
        }

        BITFIELD_MAX_VALUES = new int[32];
        i = 2;
        for (int k = 0; k < 32; k++) {
            BITFIELD_MAX_VALUES[k] = i - 1;
            i += i;
        }

    }
}
