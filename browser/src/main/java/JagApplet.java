import luna.browser.applet.Applet;
import luna.browser.awt.BrowserDisplay;
import luna.browser.awt.Color;
import luna.browser.awt.Component;
import luna.browser.awt.Font;
import luna.browser.awt.Graphics;
import luna.browser.awt.image.BufferedImage;

/**
 * Runs the unchanged client lifecycle and software rendering against a browser canvas.
 *
 * @author lare96
 */
public class JagApplet extends Applet implements Runnable {
    public static final int MIN_CONTENT_WIDTH = 765, MIN_CONTENT_HEIGHT = 503, TARGET_FPS = 60;
    public JagFrame frame;
    public volatile int resizedWidth = -1, resizedHeight = -1;
    public int width, height;
    public volatile Graphics graphics;
    public int mouseX, mouseY;
    public volatile int screenMouseX, screenMouseY;
    public int screenClickX, screenClickY;
    public int[] keyStatus = new int[128], inputBuffer = new int[128];
    public boolean[] keyCodeHeld = new boolean[256];
    public boolean aBoolean2, aBoolean3;
    public int anInt5;
    public volatile int gameState;
    public int delayTime = 20, minDelay = 1;
    public long[] otims = new long[10];
    public int fps;
    public boolean aBoolean11;
    public JagImageProducer imageProducer;
    public boolean clearBackground = true, awtFocus = true;
    public int anInt20, anInt21, anInt24, anInt25, anInt26;
    public long aLong27;
    public int anInt28, anInt29, anInt30;
    public long aLong31;
    public int anInt34, anInt35;
    private BufferedImage buffer;
    private int targetFps = TARGET_FPS;
    private long stopDeadline;
    private int wheelX, wheelY, wheelDelta;
    private boolean rendering;
    private int contentWidth = MIN_CONTENT_WIDTH, contentHeight = MIN_CONTENT_HEIGHT;
    private int pendingWidth = -1, pendingHeight = -1, pendingMode = -1;

    public void start(int width, int height) {
        this.width = MIN_CONTENT_WIDTH;
        this.height = MIN_CONTENT_HEIGHT;
        rebuildFrame(this.width, this.height, false);
        imageProducer = new JagImageProducer(this.width, this.height, getParentComponent());
        startThread(this, Thread.NORM_PRIORITY);
    }

    public void rebuildFrame(int width, int height, boolean resizable) {
        width = resizable ? ClientSettings.clampWidth(width) : MIN_CONTENT_WIDTH;
        height = resizable ? ClientSettings.clampHeight(height) : MIN_CONTENT_HEIGHT;
        contentWidth = width;
        contentHeight = height;
        if (this instanceof client game) {
            game.clientSize = resizable ? 1 : 0;
            game.clientWidth = width;
            game.clientHeight = height;
        }
        if (frame == null) frame = new JagFrame(width, height, this, resizable);
        else {
            frame.setContentSize(width, height);
            frame.setResizable(resizable);
        }
        resizedWidth = resizedHeight = -1;
        BrowserDisplay.bind(width, height, this::input);
        BrowserDisplay.layout(resizable, width, height);
        ensureBackBuffer();
    }

    public void ensureBackBuffer() {
        int width = getContentWidth(), height = getContentHeight();
        if (buffer != null && buffer.width == width && buffer.height == height) return;
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        graphics = buffer.createGraphics();
        clearBackground = true;
        needsUIRedraw();
    }

    public void presentBackBuffer() {
        if (!rendering && buffer != null) {
            long started = ClientPerformance.begin(ClientPerformance.PRESENT);
            BrowserDisplay.present(buffer.pixels, buffer.width, buffer.height);
            ClientPerformance.end(ClientPerformance.PRESENT, started);
        }
    }

    protected final void invalidateFramePresentation() { }
    public int getContentWidth() { return contentWidth; }
    public int getContentHeight() { return contentHeight; }
    public int inputOffsetX() { return 0; }
    public int inputOffsetY() { return 0; }

    public void run() {
        try {
            drawLoadingText(0, "Loading...");
            load();
            if (this instanceof client game && (game.aBoolean1283 || game.aBoolean1097 || game.aBoolean1016)) {
                repaintGame();
                presentBackBuffer();
                BrowserClientMain.clientFailed();
                return;
            }
            applyBrowserChanges();
            BrowserClientMain.clientReady();
            BrowserPerformance.install(this);
            BrowserFrameClock.start();
            int scheduledFps = getEffectiveFramerate();
            FrameScheduler scheduler = new FrameScheduler(System.nanoTime(), scheduledFps);
            long statisticsStart = System.nanoTime();
            int totalUpdates = 0;
            int statisticsUpdates = 0, statisticsFrames = 0;
            while (gameState >= 0) {
                long performanceLoopStarted = ClientPerformance.loopStarted();
                applyBrowserChanges();
                long now = System.nanoTime();
                if (gameState > 0 && stopDeadline != 0 && now >= stopDeadline) break;
                int effectiveFps = getEffectiveFramerate();
                ClientPerformance.observeTargetFps(effectiveFps);
                if (scheduledFps != effectiveFps) {
                    scheduledFps = effectiveFps;
                    scheduler.setTargetFps(scheduledFps, now);
                }
                int updates = scheduler.pendingUpdates(now);
                long updateStarted = ClientPerformance.begin(ClientPerformance.UPDATE);
                for (int update = 0; update < updates; update++) {
                    anInt28 = anInt24; anInt29 = anInt25; anInt30 = anInt26;
                    screenClickX = anInt29; screenClickY = anInt30; aLong31 = aLong27; anInt24 = 0;
                    updateMouseInput(); method7((byte) -111); anInt34 = anInt35;
                    statisticsUpdates++;
                    totalUpdates++;
                }
                ClientPerformance.end(ClientPerformance.UPDATE, updateStarted);
                ClientPerformance.updated(updates);
                long frameStart = System.nanoTime();
                boolean renderDue = BrowserFrameClock.enabled()
                        ? BrowserFrameClock.frameDue(effectiveFps) : scheduler.isRenderDue(frameStart);
                if (renderDue) {
                    long performanceFrameStarted = ClientPerformance.frameStarted();
                    scheduler.frameStarted(frameStart);
                    ensureBackBuffer(); updateMouseInput();
                    rendering = true;
                    try { repaintGame(); }
                    finally { rendering = false; }
                    ClientPerformance.frameRendered(performanceFrameStarted);
                    presentBackBuffer();
                    scheduler.frameRendered(System.nanoTime());
                    scheduler.frameCompleted(System.nanoTime(), true);
                    fps = scheduler.getFramesPerSecond();
                    statisticsFrames++;
                }
                long statisticsNow = System.nanoTime();
                long elapsed = statisticsNow - statisticsStart;
                if (elapsed >= 1_000_000_000L && this instanceof client game) {
                    int updateRate = (int) (statisticsUpdates * 1_000_000_000L / elapsed);
                    int renderRate = (int) (statisticsFrames * 1_000_000_000L / elapsed);
                    BrowserDisplay.stats(updateRate, renderRate, totalUpdates, game.isLoggedIn,
                            game.loadingStage, game.outBuffer == null ? 0 : game.outBuffer.position,
                            game.opcode, game.lastOpcode, game.size, game.anInt871);
                    statisticsStart = statisticsNow;
                    statisticsUpdates = statisticsFrames = 0;
                }
                ClientPerformance.loopFinished(performanceLoopStarted);
                BrowserPerformance.poll();
                if (BrowserFrameClock.enabled()) {
                    BrowserFrameClock.awaitWork(scheduler.nanosUntilUpdate(System.nanoTime()) / 1_000_000.0);
                } else {
                    long waitNanos = scheduler.nanosUntilWork(System.nanoTime());
                    // Browser events and TeaVM continuations must run even when rendering misses its deadline.
                    Thread.sleep(Math.max(1, waitNanos / 1_000_000L));
                }
            }
            exit(true);
        } catch (Throwable failure) {
            BrowserFrameClock.stop();
            failure.printStackTrace();
            luna.browser.BrowserDiagnostics.report(failure);
            BrowserClientMain.clientFailed();
        }
    }

    public void setFramerate(int targetFps) {
        if (targetFps < 1 || targetFps > 1000) throw new IllegalArgumentException("Invalid frame rate");
        this.targetFps = targetFps;
    }

    protected int getEffectiveFramerate() {
        if (BrowserDisplay.hidden()) return 1;
        return awtFocus ? targetFps : Math.min(targetFps, 10);
    }

    private void applyBrowserChanges() {
        if (!(this instanceof client game)) return;
        if (pendingMode != -1) {
            int mode = pendingMode;
            pendingMode = -1;
            if (mode == 1) {
                client.settings.width = ClientSettings.clampWidth(BrowserDisplay.availableWidth());
                client.settings.height = ClientSettings.clampHeight(BrowserDisplay.availableHeight());
            }
            game.toggleSize(mode);
        }
        int nextWidth = pendingWidth, nextHeight = pendingHeight;
        pendingWidth = pendingHeight = -1;
        if (game.clientSize == 1 && nextWidth > 0 && nextHeight > 0) {
            nextWidth = ClientSettings.clampWidth(nextWidth);
            nextHeight = ClientSettings.clampHeight(nextHeight);
            if (nextWidth != contentWidth || nextHeight != contentHeight) {
                contentWidth = nextWidth;
                contentHeight = nextHeight;
                if (frame != null) frame.setContentSize(nextWidth, nextHeight);
                resizedWidth = nextWidth;
                resizedHeight = nextHeight;
                game.checkSize();
                ensureBackBuffer();
            }
        }
        if (wheelDelta != 0) {
            int delta = wheelDelta;
            wheelDelta = 0;
            scroll(game, wheelX, wheelY, delta);
        }
    }

    private void scroll(client game, int x, int y, int delta) {
        if (!game.isLoggedIn || game.layout == null) return;
        ClientLayout layout = game.layout;
        if (layout.chatbox.contains(x, y)) {
            if (game.anInt988 == -1 && game.chatboxInterfaceType == 0) {
                game.anInt851 = clamp(game.anInt851 - delta, Math.max(0, game.anInt1107 - 77));
                game.aBoolean1240 = true;
                return;
            }
            if (scrollInterface(game.anInt988, layout.chatbox.x, layout.chatbox.y, x, y, delta, 0)) {
                game.aBoolean1240 = true;
                return;
            }
        }
        if (layout.inventoryBlock.contains(x, y)) {
            int root = game.anInt1089 != -1 ? game.anInt1089 : game.anIntArray1081[game.tabId];
            if (scrollInterface(root, layout.inventory.x, layout.inventory.y, x, y, delta, 0)) {
                game.aBoolean1181 = true;
                return;
            }
        }
        int root = game.anInt1053 != -1 ? game.anInt1053 : game.anInt1169;
        int originX = game.anInt1053 != -1 ? layout.fullscreenInterfaceX : layout.viewport.x;
        int originY = game.anInt1053 != -1 ? layout.fullscreenInterfaceY : layout.viewport.y;
        scrollInterface(root, originX, originY, x, y, delta, 0);
    }

    private boolean scrollInterface(int id, int x, int y, int mouseX, int mouseY, int delta, int depth) {
        if (id < 0 || depth > 32 || JagInterface.interfaces == null || id >= JagInterface.interfaces.length) {
            return false;
        }
        JagInterface widget = JagInterface.forId(id);
        if (widget == null || widget.type != 0 || widget.aBoolean219 || mouseX < x || mouseY < y
                || mouseX >= x + widget.width + 16 || mouseY >= y + widget.height) return false;
        if (widget.anIntArray258 != null) {
            for (int index = widget.anIntArray258.length - 1; index >= 0; index--) {
                int childId = widget.anIntArray258[index];
                if (childId < 0 || childId >= JagInterface.interfaces.length) continue;
                JagInterface child = JagInterface.forId(childId);
                int childX = x + widget.anIntArray232[index] + child.anInt228;
                int childY = y + widget.anIntArray276[index] + child.anInt259 - widget.anInt231;
                if (scrollInterface(childId, childX, childY, mouseX, mouseY, delta, depth + 1)) return true;
            }
        }
        if (widget.anInt285 <= widget.height) return false;
        widget.anInt231 = clamp(widget.anInt231 + delta, widget.anInt285 - widget.height);
        return true;
    }

    private static int clamp(int value, int maximum) {
        return Math.max(0, Math.min(value, maximum));
    }

    public void exit(boolean flag) {
        BrowserFrameClock.stop();
        gameState = -2;
        cleanupShutdown();
    }
    public void start() { gameState = 0; stopDeadline = 0; }
    public void stop() { gameState = 1; stopDeadline = System.nanoTime() + 4_000_000_000L; }
    public void destroy() { gameState = -1; }
    public void load() { }
    public void method7(byte value) { }
    public void cleanupShutdown() { }
    public void repaintGame() { }
    public void needsUIRedraw() { }
    public Component getParentComponent() { return frame == null ? this : frame; }
    public void startThread(Runnable runnable, int priority) { new Thread(runnable).start(); }

    protected void updateMouseInput() {
        mouseX = screenMouseX; mouseY = screenMouseY; anInt29 = screenClickX; anInt30 = screenClickY;
    }

    public int readCharFromChatbox() {
        if (anInt34 == anInt35) return -1;
        int value = inputBuffer[anInt34]; anInt34 = anInt34 + 1 & 127; return value;
    }

    private void input(int type, int x, int y, int keyCode, int character) {
        if (type == 7) {
            pendingWidth = x;
            pendingHeight = y;
        } else if (type == 8) {
            pendingMode = x == 1 ? 1 : 0;
        } else if (type == 9) {
            wheelX = x; wheelY = y; wheelDelta += character;
        } else if (type == 10) {
            awtFocus = true;
            clearBackground = true;
            needsUIRedraw();
        } else if (type == 0 || type == 6) {
            screenMouseX = x - inputOffsetX(); screenMouseY = y - inputOffsetY(); anInt20 = 0;
        } else if (type == 1) {
            anInt25 = x - inputOffsetX(); anInt26 = y - inputOffsetY();
            aLong27 = System.currentTimeMillis(); anInt24 = anInt21 = keyCode; anInt20 = 0;
        } else if (type == 2) { anInt21 = 0; anInt20 = 0; }
        else if (type == 3) {
            awtFocus = false;
            anInt21 = anInt24 = 0;
            java.util.Arrays.fill(keyStatus, 0); java.util.Arrays.fill(keyCodeHeld, false);
        } else if (type == 4 || type == 5) {
            awtFocus = true; anInt20 = 0;
            boolean pressed = type == 4;
            if (keyCode >= 0 && keyCode < keyCodeHeld.length) keyCodeHeld[keyCode] = pressed;
            int value = character < 30 ? 0 : character;
            if (keyCode == 37) value = 1;
            if (keyCode == 39) value = 2;
            if (keyCode == 38) value = 3;
            if (keyCode == 40) value = 4;
            if (keyCode == 17) value = 5;
            if (keyCode == 8 || keyCode == 127) value = 8;
            if (keyCode == 9 || keyCode == 10 || keyCode == 27) value = keyCode;
            if (keyCode >= 112 && keyCode <= 123) value = 1008 + keyCode - 112;
            if (keyCode == 36) value = 1000;
            if (keyCode == 35) value = 1001;
            if (keyCode == 33) value = 1002;
            if (keyCode == 34) value = 1003;
            if (value > 0 && value < keyStatus.length) keyStatus[value] = pressed ? 1 : 0;
            if (pressed && value > 4) { inputBuffer[anInt35] = value; anInt35 = anInt35 + 1 & 127; }
        }
    }

    public void drawLoadingText(int percent, String text) {
        ensureBackBuffer();
        Font font = new Font("Helvetica", 1, 13);
        if (clearBackground) {
            graphics.setColor(Color.black); graphics.fillRect(0, 0, width, height); clearBackground = false;
        }
        int y = height / 2 - 18;
        graphics.setColor(new Color(140, 17, 17));
        graphics.drawRect(width / 2 - 152, y, 304, 34);
        graphics.fillRect(width / 2 - 150, y + 2, percent * 3, 30);
        graphics.setColor(Color.black); graphics.fillRect(width / 2 - 150 + percent * 3, y + 2, 300 - percent * 3, 30);
        graphics.setFont(font); graphics.setColor(Color.white);
        graphics.drawString(text, (width - getFontMetrics(font).stringWidth(text)) / 2, y + 22);
        presentBackBuffer();
        System.out.println("Loading " + percent + "%: " + text);
    }
}
