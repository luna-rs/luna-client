// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;

@SuppressWarnings("serial")
public class JagFrame extends Frame {

	public JagApplet applet;

	public JagFrame(int width, int height, JagApplet applet, boolean resizable) {
		this.applet = applet;
		setTitle("Jagex");
		setResizable(resizable);
		// create the native peer first so the real window insets are known
		addNotify();
		setContentSize(width, height);
		setVisible(true);
		toFront();
	}

	/**
	 * Sizes the window so that the drawable area (inside the borders and title bar) is width x height.
	 */
	public void setContentSize(int width, int height) {
		Insets insets = getInsets();
		setSize(width + insets.left + insets.right, height + insets.top + insets.bottom);
	}

	/**
	 * Sets the smallest allowed drawable area (inside the borders and title bar).
	 */
	public void setMinimumContentSize(int width, int height) {
		Insets insets = getInsets();
		setMinimumSize(new java.awt.Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom));
	}

	public int getContentWidth() {
		Insets insets = getInsets();
		return getWidth() - insets.left - insets.right;
	}

	public int getContentHeight() {
		Insets insets = getInsets();
		return getHeight() - insets.top - insets.bottom;
	}

	@Override
	public Graphics getGraphics() {
		Graphics g = super.getGraphics();
		if (g == null) {
			return null;
		}
		Insets insets = getInsets();
		g.translate(insets.left, insets.top);
		g.setClip(0, 0, getContentWidth(), getContentHeight());
		return g;
	}

	@Override
	public void update(Graphics g) {
		applet.update(g);
	}

	@Override
	public void paint(Graphics g) {
		applet.paint(g);
	}
}
