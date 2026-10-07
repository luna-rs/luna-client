// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

public class JagImageProducer implements ImageProducer, ImageObserver {

	public int pixels[];
	public int width;
	public int height;
	public ColorModel colorModel;
	public ImageConsumer consumer;
	public Image image;

	public JagImageProducer(int _width, int _height, Component component) {
		this.width = _width;
		this.height = _height;
		this.pixels = new int[_width * _height];
		this.colorModel = new DirectColorModel(32, 0xff0000, 65280, 255);
		this.image = component.createImage(this);
		flipBuffer();
		component.prepareImage(image, this);
		flipBuffer();
		component.prepareImage(image, this);
		flipBuffer();
		component.prepareImage(image, this);
		pushPixels();
	}

	public void pushPixels() {
		Drawable.putPixels(width, height, pixels);
	}

	public void drawImage(int x, int y, Graphics g) {
		flipBuffer();
		g.drawImage(image, x, y, this);
	}

	/**
	 * Draws only a part of the image: the area of width x height starting at (sourceX, sourceY) is drawn at (x, y).
	 */
	public void drawImageRegion(int x, int y, int sourceX, int sourceY, int width, int height, Graphics g) {
		flipBuffer();
		g.drawImage(image, x, y, x + width, y + height, sourceX, sourceY, sourceX + width, sourceY + height, this);
	}

	public void drawImage(java.awt.Point position, Graphics g) {
		drawImage(position.x, position.y, g);
	}

	public void drawImage(java.awt.Rectangle area, Graphics g) {
		drawImage(area.x, area.y, g);
	}

	public synchronized void addConsumer(ImageConsumer imageconsumer) {
		consumer = imageconsumer;
		imageconsumer.setDimensions(width, height);
		imageconsumer.setProperties(null);
		imageconsumer.setColorModel(colorModel);
		imageconsumer.setHints(14);
	}

	public synchronized boolean isConsumer(ImageConsumer imageconsumer) {
		return consumer == imageconsumer;
	}

	public synchronized void removeConsumer(ImageConsumer imageconsumer) {
		if (consumer == imageconsumer) {
            consumer = null;
        }
	}

	public void startProduction(ImageConsumer imageconsumer) {
		addConsumer(imageconsumer);
	}

	public void requestTopDownLeftRightResend(ImageConsumer imageconsumer) {
		System.out.println("TDLR");
	}

	public synchronized void flipBuffer() {
		if (consumer == null) {
			return;
		} else {
			consumer.setPixels(0, 0, width, height, colorModel, pixels, 0, width);
			consumer.imageComplete(2);
			return;
		}
	}

	public boolean imageUpdate(Image image, int i, int j, int k, int l, int i1) {
		return true;
	}
}
