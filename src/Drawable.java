// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public class Drawable extends QueueNode {

	public static int startX;
	public static int startY;
	public static int endX;
	public static int endY;
	public static int lastPixelX;
	public static int halfWidthX;
	public static int halfHeightY;

	public static int pixels[];
	public static int width;
	public static int height;

	public Drawable() {
	}

	public static void putPixels(int width, int heíght, int pixels[]) {
		Drawable.pixels = pixels;
		Drawable.width = width;
		Drawable.height = heíght;
		recalcEdges(0, 0, heíght, width, true);
	}

	public static void recalcSize() {
		startX = 0;
		startY = 0;
		endX = width;
		endY = height;
		lastPixelX = endX - 1;
		halfWidthX = endX / 2;
	}

	public static void recalcEdges(int startX, int startY, int endY, int endX, boolean fullrecalc) {
		if (startY < 0)
			startY = 0;
		if (startX < 0)
			startX = 0;
		if (endX > width) {
            endX = width;
        }
		if (endY > height) {
            endY = height;
        }
		Drawable.startX = startY;
		Drawable.startY = startX;
		Drawable.endX = endX;
		Drawable.endY = endY;
        if (fullrecalc) {
            lastPixelX = Drawable.endX - 1;
            halfWidthX = Drawable.endX / 2;
            halfHeightY = Drawable.endY / 2;
        }
    }

	public static void clearScreen() {
		int pixelsTotal = width * height;
		for (int k = 0; k < pixelsTotal; k++) {
            pixels[k] = 0;
        }
	}

	public static void drawTransparentFullRect(int x, int y, int width, int height, int color, int opacity) {
		if (x < startX) {
			width -= startX - x;
			x = startX;
		}
		if (y < startY) {
			height -= startY - y;
			y = startY;
		}
		if (x + width > endX) {
            width = endX - x;
        }
		if (y + height > endY) {
            height = endY - y;
        }
		int saturation = 256 - opacity;
		int l1 = (color >> 16 & 0xff) * opacity;
		int i2 = (color >> 8 & 0xff) * opacity;
		int j2 = (color & 0xff) * opacity;
		int j3 = Drawable.width - width;
		int pixelOffset = x + y * Drawable.width;
		for (int l3 = 0; l3 < height; l3++) {
			for (int i4 = -width; i4 < 0; i4++) {
				int k2 = (pixels[pixelOffset] >> 16 & 0xff) * saturation;
				int l2 = (pixels[pixelOffset] >> 8 & 0xff) * saturation;
				int i3 = (pixels[pixelOffset] & 0xff) * saturation;
				int totalColor = ((l1 + k2 >> 8) << 16) + ((i2 + l2 >> 8) << 8) + (j2 + i3 >> 8);
				pixels[pixelOffset++] = totalColor;
			}
			pixelOffset += j3;
		}

	}

	public static void drawFullRect(int x, int y, int width, int height, int color) {
		if (x < startX) {
			width -= startX - x;
			x = startX;
		}
		if (y < startY) {
			height -= startY - y;
			y = startY;
		}
		if (x + width > endX) {
            width = endX - x;
        }
		if (y + height > endY) {
            height = endY - y;
        }
		int indexLineIncrease = Drawable.width - width;
		int pixelIndex = x + y * Drawable.width;
		for (int l1 = -height; l1 < 0; l1++) {
			for (int i2 = -width; i2 < 0; i2++) {
                pixels[pixelIndex++] = color;
            }
			pixelIndex += indexLineIncrease;
		}
	}

	public static void drawRect(int x, int y, int width, int height, int color) {
		drawHorizontalLine(color, x, y, width);
		drawHorizontalLine(color, x, (y + height) - 1, width);
		drawVerticalLine(color, x, y, height);
		drawVerticalLine(color, (x + width) - 1, y, height);
	}

	public static void drawTransparentRect(int x, int y, int width, int height, int color, int opacity) {
		drawTransparentHorizontalLine(color, x, y, width, opacity);
		drawTransparentHorizontalLine(color, x, (y + height) - 1, width, opacity);
		if (height >= 3) {
			drawTransparentVerticalLine(color, x, y + 1, height - 2, opacity);
			drawTransparentVerticalLine(color, (x + width) - 1, y + 1, height - 2, opacity);
		}
	}

	public static void drawHorizontalLine(int color, int x, int y, int length) {
		if (y < startY || y >= endY) {
            return;
        }
		if (x < startX) {
			length -= startX - x;
			x = startX;
		}
		if (x + length > endX) {
            length = endX - x;
        }
		int pixelOffset = x + y * width;
		for (int counter = 0; counter < length; counter++) {
            pixels[pixelOffset + counter] = color;
        }
	}

	public static void drawTransparentHorizontalLine(int color, int x, int y, int length, int opacity) {
		if (y < startY || y >= endY)
			return;
		if (x < startX) {
			length -= startX - x;
			x = startX;
		}
		if (x + length > endX) {
            length = endX - x;
        }
		int inversedOpacity = 256 - opacity;
		int red = (color >> 16 & 0xff) * opacity;
		int green = (color >> 8 & 0xff) * opacity;
		int blue = (color & 0xff) * opacity;
		int pixelOffset = x + y * width;
		for (int counter = 0; counter < length; counter++) {
			int red2 = (pixels[pixelOffset] >> 16 & 0xff) * inversedOpacity;
			int green2 = (pixels[pixelOffset] >> 8 & 0xff) * inversedOpacity;
			int blue2 = (pixels[pixelOffset] & 0xff) * inversedOpacity;
			int totalColor = ((red + red2 >> 8) << 16) + ((green + green2 >> 8) << 8) + (blue + blue2 >> 8);
			pixels[pixelOffset++] = totalColor;
		}
	}

	public static void drawVerticalLine(int color, int x, int y, int length) {
		if (x < startX || x >= endX) {
            return;
        }
		if (y < startY) {
			length -= startY - y;
			y = startY;
		}
		if (y + length > endY) {
            length = endY - y;
        }
		int pixelOffset = x + y * width;
		for (int counter = 0; counter < length; counter++) {
            pixels[pixelOffset + counter * width] = color;
        }
	}

	// not obvious when knockout
	public static void drawTransparentVerticalLine(int color, int x, int y, int length, int opacity) {
		if (x < startX || x >= endX) {
            return;
        }
		if (y < startY) {
			length -= startY - y;
			y = startY;
		}
		if (y + length > endY) {
            length = endY - y;
        }
		int saturation = 256 - opacity;
		int red = (color >> 16 & 0xff) * opacity;
		int green = (color >> 8 & 0xff) * opacity;
		int blue = (color & 0xff) * opacity;
		int pixelOffset = x + y * Drawable.width;
		for (int counter = 0; counter < length; counter++) {
			int red2 = (pixels[pixelOffset] >> 16 & 0xff) * saturation;
			int green2 = (pixels[pixelOffset] >> 8 & 0xff) * saturation;
			int blue2 = (pixels[pixelOffset] & 0xff) * saturation;
			int pixelColor = ((red + red2 >> 8) << 16) + ((green + green2 >> 8) << 8) + (blue + blue2 >> 8);
			pixels[pixelOffset] = pixelColor;
			pixelOffset += Drawable.width;
		}
	}
}
