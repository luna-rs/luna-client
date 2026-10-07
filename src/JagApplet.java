// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import javax.swing.*;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.image.BufferedImage;

@SuppressWarnings("serial")
public class JagApplet extends Applet implements Runnable, MouseListener, MouseMotionListener, KeyListener,
		FocusListener, WindowListener {

	public JagFrame frame;

	/** Smallest drawable area allowed for the resizable window (the fixed-mode size). */
	public static final int MIN_CONTENT_WIDTH = 765, MIN_CONTENT_HEIGHT = 503;

	/**
	 * Latest drawable size reported by the AWT thread when the frame was resized, or -1 if no resize is pending.
	 * The game thread reads these (see client.checkSize) so buffers are only rebuilt on the game thread.
	 */
	public volatile int resizedWidth = -1, resizedHeight = -1;

	public int width;
	public int height;
	/**
	 * Everything the game draws goes to this Graphics, which belongs to the back buffer, not to the screen.
	 * The back buffer is copied to the window once per frame by presentBackBuffer().
	 */
	public volatile Graphics graphics;

	/** Graphics of the window itself, only used by presentBackBuffer(). */
	private volatile Graphics screenGraphics;
	private volatile BufferedImage backBuffer;
	private final Object presentLock = new Object();

	public int mouseX;
	public int mouseY;
	public int keyStatus[];
	public int inputBuffer[];

	public boolean aBoolean2;
	public boolean aBoolean3;
	public int anInt5;
	public int gameState;
	public int delayTime;
	public int minDelay;
	public long otims[];
	public int fps;
	public boolean aBoolean11;
	public JagImageProducer imageProducer;
	public boolean clearBackground;
	public boolean awtFocus;
	public int anInt20;
	public int anInt21;
	public int anInt24;
	public int anInt25;
	public int anInt26;
	public long aLong27;
	public int anInt28;
	public int anInt29;
	public int anInt30;
	public long aLong31;
	public int anInt34;
	public int anInt35;

	public JagApplet() {
		aBoolean2 = false;
		aBoolean3 = false;
		aBoolean11 = false;
		delayTime = 20;
		minDelay = 1;
		otims = new long[10];
		clearBackground = true;
		awtFocus = true;
		keyStatus = new int[128];
		inputBuffer = new int[128];
	}

	/**
	 * Resizes / reconfigures the existing frame instead of disposing it and creating a new one.
	 * The width and height fields keep the classic game size (765x503), which the login screen is laid out for.
	 */
	public void rebuildFrame(int width, int height, boolean resizable) {
		if (frame == null) {
			frame = new JagFrame(width, height, this, resizable);
			frame.addWindowListener(this);
			installFrameResizeListener();
			addInputListeners();
		} else {
			frame.setResizable(resizable);
			frame.setContentSize(width, height);
		}
		frame.setMinimumContentSize(MIN_CONTENT_WIDTH, MIN_CONTENT_HEIGHT);
		// discard resize events from before this rebuild; the size we just set is the current one
		resizedWidth = resizedHeight = -1;
		screenGraphics = frame.getGraphics();
		ensureBackBuffer();
	}

	/**
	 * Makes sure there is a back buffer matching the drawable area of the window. Called at the start of every
	 * frame on the game thread. A new buffer is blank, so everything is flagged to be drawn again.
	 */
	public void ensureBackBuffer() {
		synchronized (presentLock) {
			if (screenGraphics == null) {
				screenGraphics = getParentComponent().getGraphics();
			}
			int w = getContentWidth();
			int h = getContentHeight();
			if (w <= 0 || h <= 0) {
				w = width;
				h = height;
			}
			BufferedImage old = backBuffer;
			if (old != null && old.getWidth() == w && old.getHeight() == h) {
				return;
			}
			// the old Graphics is not disposed: other threads may still be drawing with it
			BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics g = buffer.createGraphics();
			backBuffer = buffer;
			graphics = g;
			if (old != null) {
				needsUIRedraw();
			}
		}
	}

	/**
	 * Copies the back buffer to the window. This is the only place the game output reaches the screen.
	 */
	public void presentBackBuffer() {
		synchronized (presentLock) {
			BufferedImage buffer = backBuffer;
			Graphics screen = screenGraphics;
			if (buffer != null && screen != null) {
				screen.drawImage(buffer, 0, 0, null);
			}
		}
	}

	private void addInputListeners() {
		getParentComponent().addMouseListener(this);
		getParentComponent().addMouseMotionListener(this);
		getParentComponent().addKeyListener(this);
		getParentComponent().addFocusListener(this);
	}

	/**
	 * The drawable area of the frame changes whenever it is resized, maximised or restored. The cached Graphics
	 * has a fixed clip, so it must be fetched again, and everything has to be repainted.
	 */
	private void installFrameResizeListener() {
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				screenGraphics = frame.getGraphics();
				resizedWidth = frame.getContentWidth();
				resizedHeight = frame.getContentHeight();
				clearBackground = true;
				needsUIRedraw();
			}
		});
	}

	/**
	 * Width of the drawable area of the window, excluding borders and title bar.
	 */
	public int getContentWidth() {
		return frame != null ? frame.getContentWidth() : getParentComponent().getWidth();
	}

	/**
	 * Height of the drawable area of the window, excluding borders and title bar.
	 */
	public int getContentHeight() {
		return frame != null ? frame.getContentHeight() : getParentComponent().getHeight();
	}

	/**
	 * Where the origin of the game is inside the drawable area. Mouse positions are reported relative to it.
	 * Overridden when the game is drawn centred in the window (the login screen in the resizable mode).
	 */
	public int inputOffsetX() {
		return 0;
	}

	public int inputOffsetY() {
		return 0;
	}

	private int insetLeft() {
		return frame != null ? frame.getInsets().left : 0;
	}

	private int insetTop() {
		return frame != null ? frame.getInsets().top : 0;
	}

	public void start(int _width, int _height) {
		width = _width;
		height = _height;
		frame = new JagFrame(width, height, this, false);
		screenGraphics = getParentComponent().getGraphics();
		ensureBackBuffer();
		imageProducer = new JagImageProducer(width, height, getParentComponent());
		startThread(this, 1);
	}

	public void run() {
		addInputListeners();
		if (frame != null) {
			frame.addWindowListener(this);
			installFrameResizeListener();
		}
		drawLoadingText(0, "Loading...");
		load();
		int i = 0;
		int j = 256;
		int k = 1;
		int i1 = 0;
		int j1 = 0;
		for (int k1 = 0; k1 < 10; k1++)
			otims[k1] = System.currentTimeMillis();

		while (gameState >= 0) {
			if (gameState > 0) {
				gameState--;
				if (gameState == 0) {
					exit(aBoolean2);
					return;
				}
			}
			int i2 = j;
			int j2 = k;
			j = 300;
			k = 1;
			long l1 = System.currentTimeMillis();
			if (otims[i] == 0L) {
				j = i2;
				k = j2;
			} else if (l1 > otims[i])
				j = (int) ((2560 * delayTime) / (l1 - otims[i]));
			if (j < 25)
				j = 25;
			if (j > 256) {
				j = 256;
				k = (int) (delayTime - (l1 - otims[i]) / 10L);
			}
			if (k > delayTime)
				k = delayTime;
			otims[i] = l1;
			i = (i + 1) % 10;
			if (k > 1) {
				for (int k2 = 0; k2 < 10; k2++)
					if (otims[k2] != 0L)
						otims[k2] += k;

			}
			if (k < minDelay)
				k = minDelay;
			try {
				Thread.sleep(k);
			} catch (InterruptedException _ex) {
				j1++;
			}
			for (; i1 < 256; i1 += j) {
				anInt28 = anInt24;
				anInt29 = anInt25;
				anInt30 = anInt26;
				aLong31 = aLong27;
				anInt24 = 0;
				method7((byte) -111);
				anInt34 = anInt35;
			}

			i1 &= 0xff;
			if (delayTime > 0)
				fps = (1000 * j) / (delayTime * 256);
			ensureBackBuffer();
			repaintGame();
			presentBackBuffer();
			if (aBoolean11) {
				System.out.println("ntime:" + l1);
				for (int l2 = 0; l2 < 10; l2++) {
					int i3 = ((i - l2 - 1) + 20) % 10;
					System.out.println("otim" + i3 + ":" + otims[i3]);
				}

				System.out.println("fps:" + fps + " ratio:" + j + " count:" + i1);
				System.out.println("del:" + k + " deltime:" + delayTime + " mindel:" + minDelay);
				System.out.println("intex:" + j1 + " opos:" + i);
				aBoolean11 = false;
				j1 = 0;
			}
		}
		if (gameState == -1)
			exit(aBoolean2);
	}

	public void exit(boolean flag) {
		gameState = -2;
		cleanupShutdown();
		if (flag)
			return;
		if (frame != null) {
			try {
				Thread.sleep(1000L);
			} catch (Exception _ex) {
			}
			try {
				System.exit(0);
				return;
			} catch (Throwable _ex) {
			}
		}
	}

	public void setFramerate(int newFramerate) {
		delayTime = 1000 / newFramerate;
	}

	@Override
	public void start() {
		if (gameState >= 0)
			gameState = 0;
	}

	@Override
	public void stop() {
		if (gameState >= 0)
			gameState = 4000 / delayTime;
	}

	@Override
	public void destroy() {
		gameState = -1;
		try {
			Thread.sleep(10000L);
		} catch (Exception _ex) {
		}
		if (gameState == -1)
			exit(aBoolean2);
	}

	@Override
	public void update(Graphics g) {
		if (screenGraphics == null)
			screenGraphics = g;
		clearBackground = true;
		needsUIRedraw();
	}

	@Override
	public void paint(Graphics g) {
		if (screenGraphics == null)
			screenGraphics = g;
		clearBackground = true;
		needsUIRedraw();
	}

	public void mousePressed(MouseEvent mouseevent) {
		int i = mouseevent.getX();
		int j = mouseevent.getY();
		i -= insetLeft() + inputOffsetX();
		j -= insetTop() + inputOffsetY();
		anInt20 = 0;
		anInt25 = i;
		anInt26 = j;
		aLong27 = System.currentTimeMillis();
		if (SwingUtilities.isRightMouseButton(mouseevent)) {
			anInt24 = 2;
			anInt21 = 2;
			return;
		} else if (SwingUtilities.isLeftMouseButton(mouseevent)){
			anInt24 = 1;
			anInt21 = 1;
			return;
		}
	}

	public void mouseReleased(MouseEvent mouseevent) {
		anInt20 = 0;
		anInt21 = 0;
	}

	public void mouseClicked(MouseEvent mouseevent) {
	}

	public void mouseEntered(MouseEvent mouseevent) {
	}

	public void mouseExited(MouseEvent mouseevent) {
		anInt20 = 0;
		mouseX = -1;
		mouseY = -1;
	}

	public void mouseDragged(MouseEvent mouseevent) {
		int i = mouseevent.getX();
		int j = mouseevent.getY();
		i -= insetLeft() + inputOffsetX();
		j -= insetTop() + inputOffsetY();
		anInt20 = 0;
		mouseX = i;
		mouseY = j;
	}

	public void mouseMoved(MouseEvent mouseevent) {
		int i = mouseevent.getX();
		int j = mouseevent.getY();
		i -= insetLeft() + inputOffsetX();
		j -= insetTop() + inputOffsetY();
		anInt20 = 0;
		mouseX = i;
		mouseY = j;
	}

	public void keyPressed(KeyEvent keyevent) {
		anInt20 = 0;
		int keycode = keyevent.getKeyCode();
		int keychar = keyevent.getKeyChar();
		if (keychar < 30)
			keychar = 0;
		if (keycode == 37)
			keychar = 1;
		if (keycode == 39)
			keychar = 2;
		if (keycode == 38)
			keychar = 3;
		if (keycode == 40)
			keychar = 4;
		if (keycode == 17)
			keychar = 5;
		if (keycode == 8)
			keychar = 8;
		if (keycode == 127)
			keychar = 8;
		if (keycode == 9)
			keychar = 9;
		if (keycode == 10)
			keychar = 10;
		if (keycode >= 112 && keycode <= 123)
			keychar = (1008 + keycode) - 112;
		if (keycode == 36)
			keychar = 1000;
		if (keycode == 35)
			keychar = 1001;
		if (keycode == 33)
			keychar = 1002;
		if (keycode == 34)
			keychar = 1003;
		if (keychar > 0 && keychar < 128)
			keyStatus[keychar] = 1;
		if (keychar > 4) {
			inputBuffer[anInt35] = keychar;
			anInt35 = anInt35 + 1 & 0x7f;
		}
	}

	public void keyReleased(KeyEvent keyevent) {
		anInt20 = 0;
		int i = keyevent.getKeyCode();
		char c = keyevent.getKeyChar();
		if (c < '\036')
			c = '\0';
		if (i == 37)
			c = '\001';
		if (i == 39)
			c = '\002';
		if (i == 38)
			c = '\003';
		if (i == 40)
			c = '\004';
		if (i == 17)
			c = '\005';
		if (i == 8)
			c = '\b';
		if (i == 127)
			c = '\b';
		if (i == 9)
			c = '\t';
		if (i == 10)
			c = '\n';
		if (c > 0 && c < '\200')
			keyStatus[c] = 0;
	}

	public void keyTyped(KeyEvent keyevent) {
	}

	public int readCharFromChatbox() {
		int j = -1;
		if (anInt35 != anInt34) {
			j = inputBuffer[anInt34];
			anInt34 = anInt34 + 1 & 0x7f;
		}
		return j;
	}

	public void focusGained(FocusEvent focusevent) {
		awtFocus = true;
		clearBackground = true;
		needsUIRedraw();
	}

	public void focusLost(FocusEvent focusevent) {
		awtFocus = false;
		for (int i = 0; i < 128; i++)
			keyStatus[i] = 0;

	}

	public void windowActivated(WindowEvent windowevent) {
	}

	public void windowClosed(WindowEvent windowevent) {
	}

	public void windowClosing(WindowEvent windowevent) {
		destroy();
	}

	public void windowDeactivated(WindowEvent windowevent) {
	}

	public void windowDeiconified(WindowEvent windowevent) {
	}

	public void windowIconified(WindowEvent windowevent) {
	}

	public void windowOpened(WindowEvent windowevent) {
	}

	public void load() {
	}

	public void method7(byte byte0) {
		if (byte0 != -111)
			anInt5 = -400;
	}

	public void cleanupShutdown() {
	}

	public void repaintGame() {
	}

	public void needsUIRedraw() {}

	public Component getParentComponent() {
		if (frame != null)
			return frame;
		else
			return this;
	}

	public void startThread(Runnable runnable, int priority) {
		Thread thread = new Thread(runnable);
		thread.start();
		thread.setPriority(priority);
	}

	public void drawLoadingText(int percent, String text) {
		ensureBackBuffer();
		Font font = new Font("Helvetica", 1, 13);
		FontMetrics fontmetrics = getParentComponent().getFontMetrics(font);
		Font font1 = new Font("Helvetica", 0, 13);
		getParentComponent().getFontMetrics(font1);
		if (clearBackground) {
			graphics.setColor(Color.black);
			graphics.fillRect(0, 0, width, height);
			clearBackground = false;
		}
		Color color = new Color(140, 17, 17);
		int j = height / 2 - 18;
		graphics.setColor(color);
		graphics.drawRect(width / 2 - 152, j, 304, 34);
		graphics.fillRect(width / 2 - 150, j + 2, percent * 3, 30);
		graphics.setColor(Color.black);
		graphics.fillRect((width / 2 - 150) + percent * 3, j + 2, 300 - percent * 3, 30);
		graphics.setFont(font);
		graphics.setColor(Color.white);
		graphics.drawString(text, (width - fontmetrics.stringWidth(text)) / 2, j + 22);
		presentBackBuffer();
	}
}
