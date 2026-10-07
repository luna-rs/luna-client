// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.util.Random;

public class JagFont extends Drawable {

	public int anInt1498;
	public int anInt1499;
	public byte glyphData[][];
	public int glyphWidth[];
	public int glyphHeight[];
	public int glyphAttribute1_1503[];
	public int glyphAttribute2_1504[];
	public int anIntArray1505[];
	public int anInt1506;
	public Random random_7;
	public boolean strikethrough;

	public JagFont(Archive archive, String name, boolean flag) {
		anInt1498 = 3;
		anInt1499 = 3;
		glyphData = new byte[256][];
		glyphWidth = new int[256];
		glyphHeight = new int[256];
		glyphAttribute1_1503 = new int[256];
		glyphAttribute2_1504 = new int[256];
		anIntArray1505 = new int[256];
		random_7 = new Random();
		strikethrough = false;
		JagBuffer buf = new JagBuffer(archive.get(name + ".dat"));
		JagBuffer indexBuf = new JagBuffer(archive.get("index.dat"));
		indexBuf.position = buf.getShort() + 4;
		int k = indexBuf.getByte();
		if (k > 0) {
            indexBuf.position += 3 * (k - 1);
        }
		for (int l = 0; l < 256; l++) {
			glyphAttribute1_1503[l] = indexBuf.getByte();
			glyphAttribute2_1504[l] = indexBuf.getByte();
			int width_i1 = glyphWidth[l] = indexBuf.getShort();
			int height_j1 = glyphHeight[l] = indexBuf.getShort();
			int dataType = indexBuf.getByte();
			int dataLength = width_i1 * height_j1;
			glyphData[l] = new byte[dataLength];
			if (dataType == 0) {
				for (int i2 = 0; i2 < dataLength; i2++) {
                    glyphData[l][i2] = buf.getSignedByte();
                }
			} else if (dataType == 1) {
				for (int j2 = 0; j2 < width_i1; j2++) {
					for (int l2 = 0; l2 < height_j1; l2++) {
                        glyphData[l][j2 + l2 * width_i1] = buf.getSignedByte();
                    }
				}
			}
			if (height_j1 > anInt1506 && l < 128) {
                anInt1506 = height_j1;
            }
			glyphAttribute1_1503[l] = 1;
			anIntArray1505[l] = width_i1 + 2;
			int k2 = 0;
			for (int i3 = height_j1 / 7; i3 < height_j1; i3++) {
                k2 += glyphData[l][i3 * width_i1];
            }
			if (k2 <= height_j1 / 7) {
				anIntArray1505[l]--;
				glyphAttribute1_1503[l] = 0;
			}
			k2 = 0;
			for (int j3 = height_j1 / 7; j3 < height_j1; j3++) {
                k2 += glyphData[l][(width_i1 - 1) + j3 * width_i1];
            }
			if (k2 <= height_j1 / 7) {
                anIntArray1505[l]--;
            }
		}

		if (flag) {
			anIntArray1505[32] = anIntArray1505[73];
		} else {
			anIntArray1505[32] = anIntArray1505[105];
		}
	}

	public void method469(boolean flag, String s, int i, int j, int k) {
		drawString_474(s, 2245, j - method473(s, (byte) -53), i, k);
	}

	public void drawHorizontallyCenteredString(int i, int k, int l, String s) {
		drawString_474(s, 2245, i - method473(s, (byte) -53) / 2, l, k);
	}

	public void drawString(String text, int x, int y, boolean flag, int j) {
		this.drawString(text, j, x - method472((byte) 35, text) / 2, y, flag);
    }

	public int method472(byte byte0, String input) {
		if (input == null)
			return 0;
		int i = 0;
		for (int j = 0; j < input.length(); j++) {
            if (input.charAt(j) == '@' && j + 4 < input.length() && input.charAt(j + 4) == '@') {
                j += 4;
            } else {
                i += anIntArray1505[input.charAt(j)];
            }
        }
		if (byte0 != 35) {
			for (int k = 1; k > 0; k++);
		}
		return i;
	}

	public int method473(String input, byte byte0) {
		if (input == null) {
            return 0;
        }
		int i = 0;
		for (int k = 0; k < input.length(); k++) {
            i += anIntArray1505[input.charAt(k)];
        }
		return i;
	}

	public void drawString_474(String input, int i, int j, int k, int l) {
		if (input == null) {
            return;
        }
		l -= anInt1506;
		for (int inputIterator = 0; inputIterator < input.length(); inputIterator++) {
			char c = input.charAt(inputIterator);
			if (c != ' ') {
                drawGlyph_481(glyphData[c], j + glyphAttribute1_1503[c], l + glyphAttribute2_1504[c], glyphWidth[c],
                        glyphHeight[c], k);
            }
			j += anIntArray1505[c];
		}

	}

	public void method475(int i, int j, String string, int k, int l) {
		if (string == null)
			return;
		k -= method473(string, (byte) -53) / 2;
		i -= anInt1506;
		for (int i1 = 0; i1 < string.length(); i1++) {
			char c = string.charAt(i1);
			if (c != ' ') {
                drawGlyph_481(glyphData[c], k + glyphAttribute1_1503[c], i + glyphAttribute2_1504[c]
                        + (int) (Math.sin(i1 / 2D + j / 5D) * 5D), glyphWidth[c], glyphHeight[c], l);
            }
			k += anIntArray1505[c];
		}

	}

	public void drawString_476(String input, int i, int j, byte byte0, int k, int l) {
		if (input == null) {
            return;
        }
		k -= method473(input, (byte) -53) / 2;
		if (byte0 != 1) {
			for (int i1 = 1; i1 > 0; i1++);
		}
		i -= anInt1506;
		for (int j1 = 0; j1 < input.length(); j1++) {
			char c = input.charAt(j1);
			if (c != ' ') {
                drawGlyph_481(glyphData[c], k + glyphAttribute1_1503[c] + (int) (Math.sin(j1 / 5D + l / 5D) * 5D), i
                        + glyphAttribute2_1504[c] + (int) (Math.sin(j1 / 3D + l / 5D) * 5D), glyphWidth[c],
                        glyphHeight[c], j);
            }
			k += anIntArray1505[c];
		}

	}

	public void method477(int i, String s, int j, int k, int l, int i1, int j1) {
		if (s == null)
			return;
		double d = 7D - i1 / 8D;
		while (i >= 0) {
			for (int k1 = 1; k1 > 0; k1++);
		}
		if (d < 0.0D)
			d = 0.0D;
		k -= method473(s, (byte) -53) / 2;
		l -= anInt1506;
		for (int l1 = 0; l1 < s.length(); l1++) {
			char c = s.charAt(l1);
			if (c != ' ')
				drawGlyph_481(glyphData[c], k + glyphAttribute1_1503[c], l + glyphAttribute2_1504[c]
						+ (int) (Math.sin(l1 / 1.5D + j1) * d), glyphWidth[c], glyphHeight[c], j);
			k += anIntArray1505[c];
		}

	}

	public void drawString(String text, int i, int j, int k, boolean flag) {
		strikethrough = false;
		int i1 = j;
		if (text == null)
			return;
		k -= anInt1506;
		for (int j1 = 0; j1 < text.length(); j1++)
			if (text.charAt(j1) == '@' && j1 + 4 < text.length() && text.charAt(j1 + 4) == '@') {
				int k1 = processFormattingCode(text.substring(j1 + 1, j1 + 4));
				if (k1 != -1)
					i = k1;
				j1 += 4;
			} else {
				char c = text.charAt(j1);
				if (c != ' ') {
					if (flag)
						drawGlyph_481(glyphData[c], j + glyphAttribute1_1503[c] + 1, k + glyphAttribute2_1504[c] + 1,
								glyphWidth[c], glyphHeight[c], 0);
					drawGlyph_481(glyphData[c], j + glyphAttribute1_1503[c], k + glyphAttribute2_1504[c], glyphWidth[c],
							glyphHeight[c], i);
				}
				j += anIntArray1505[c];
			}

		if (strikethrough)
			Drawable.drawHorizontalLine(0x800000, i1, k + (int) (anInt1506 * 0.69999999999999996D), j - i1);
	}

	public void method479(boolean flag, int i, int j, int k, int l, String s, int i1) {
		if (s == null)
			return;
		random_7.setSeed(i);
		int j1 = 192 + (random_7.nextInt() & 0x1f);
		l -= anInt1506;
		if (i1 != 0)
			anInt1499 = 489;
		for (int k1 = 0; k1 < s.length(); k1++)
			if (s.charAt(k1) == '@' && k1 + 4 < s.length() && s.charAt(k1 + 4) == '@') {
				int l1 = processFormattingCode(s.substring(k1 + 1, k1 + 4));
				if (l1 != -1)
					k = l1;
				k1 += 4;
			} else {
				char c = s.charAt(k1);
				if (c != ' ') {
					if (flag)
						method483(j + glyphAttribute1_1503[c] + 1, true, 0, glyphData[c],
								l + glyphAttribute2_1504[c] + 1, glyphHeight[c], glyphWidth[c], 192);
					method483(j + glyphAttribute1_1503[c], true, k, glyphData[c], l + glyphAttribute2_1504[c],
							glyphHeight[c], glyphWidth[c], j1);
				}
				j += anIntArray1505[c];
				if ((random_7.nextInt() & 3) == 0)
					j++;
			}

	}

	public int processFormattingCode(String code) {
        switch (code) {
            case "red":
                return 0xff0000;
            case "gre":
                return 65280;
            case "blu":
                return 255;
            case "yel":
                return 0xffff00;
            case "cya":
                return 65535;
            case "mag":
                return 0xff00ff;
            case "whi":
                return 0xffffff;
            case "bla":
                return 0;
            case "lre":
                return 0xff9040;
            case "dre":
                return 0x800000;
            case "dbl":
                return 128;
            case "or1":
                return 0xffb000;
            case "or2":
                return 0xff7000;
            case "or3":
                return 0xff3000;
            case "gr1":
                return 0xc0ff00;
            case "gr2":
                return 0x80ff00;
            case "gr3":
                return 0x40ff00;
            case "str":
                strikethrough = true;
                break;
			case "end":
				strikethrough = false;
				break;
        }
		return -1;
	}

	public void drawGlyph_481(byte[] abyte0, int x, int y, int width, int height, int i1) {
		int graphicsPixel = x + y * Drawable.width;
		int remainingWidth = Drawable.width - width;
		int characterPixelOffset = 0;
		int characterPixel = 0;
		if (y < Drawable.startY) {
			int offsetY = Drawable.startY - y;
			height -= offsetY;
			y = Drawable.startY;
			characterPixel += offsetY * width;
			graphicsPixel += offsetY * Drawable.width;
		}
		if (y + height >= Drawable.endY)
			height -= ((y + height) - Drawable.endY) + 1;
		if (x < Drawable.startX) {
			int offsetX = Drawable.startX - x;
			width -= offsetX;
			x = Drawable.startX;
			characterPixel += offsetX;
			graphicsPixel += offsetX;
			characterPixelOffset += offsetX;
			remainingWidth += offsetX;
		}
		if (x + width >= Drawable.endX) {
			int l2 = ((x + width) - Drawable.endX) + 1;
			width -= l2;
			characterPixelOffset += l2;
			remainingWidth += l2;
		}
		if (width <= 0 || height <= 0) {
			return;
		} else {
			method482(Drawable.pixels, abyte0, i1, characterPixel, graphicsPixel, width, height, remainingWidth, characterPixelOffset);
			return;
		}
	}

	public void method482(int dest[], byte abyte0[], int i, int j, int k, int width, int i1, int j1, int k1) {
		int negativeQuarterWidth = -(width >> 2);
		width = -(width & 3);
		for (int heightCounter = -i1; heightCounter < 0; heightCounter++) {
			for (int widthCounter = negativeQuarterWidth; widthCounter < 0; widthCounter++) {
				if (abyte0[j++] != 0)
					dest[k++] = i;
				else
					k++;
				if (abyte0[j++] != 0)
					dest[k++] = i;
				else
					k++;
				if (abyte0[j++] != 0)
					dest[k++] = i;
				else
					k++;
				if (abyte0[j++] != 0)
					dest[k++] = i;
				else
					k++;
			}

			for (int k2 = width; k2 < 0; k2++)
				if (abyte0[j++] != 0)
					dest[k++] = i;
				else
					k++;

			k += j1;
			j += k1;
		}

	}

	public void method483(int i, boolean flag, int j, byte abyte0[], int k, int l, int i1, int j1) {
		int k1 = i + k * Drawable.width;
		int l1 = Drawable.width - i1;
		int i2 = 0;
		int j2 = 0;
		if (!flag)
			return;
		if (k < Drawable.startY) {
			int k2 = Drawable.startY - k;
			l -= k2;
			k = Drawable.startY;
			j2 += k2 * i1;
			k1 += k2 * Drawable.width;
		}
		if (k + l >= Drawable.endY)
			l -= ((k + l) - Drawable.endY) + 1;
		if (i < Drawable.startX) {
			int l2 = Drawable.startX - i;
			i1 -= l2;
			i = Drawable.startX;
			j2 += l2;
			k1 += l2;
			i2 += l2;
			l1 += l2;
		}
		if (i + i1 >= Drawable.endX) {
			int i3 = ((i + i1) - Drawable.endX) + 1;
			i1 -= i3;
			i2 += i3;
			l1 += i3;
		}
		if (i1 <= 0 || l <= 0) {
			return;
		} else {
			applyOpacityOnGlyph(j2, l1, i2, k1, j1, Drawable.pixels, j, 2, l, i1, abyte0);
			return;
		}
	}

	public void applyOpacityOnGlyph(int i, int j, int k, int l, int opacity, int dest[], int color, int k1, int l1, int i2, byte abyte0[]) {
		color = ((color & 0xff00ff) * opacity & 0xff00ff00) + ((color & 0xff00) * opacity & 0xff0000) >> 8;
		opacity = 256 - opacity;
		for (int j2 = -l1; j2 < 0; j2++) {
			for (int k2 = -i2; k2 < 0; k2++) {
                if (abyte0[i++] != 0) {
                    int l2 = dest[l];
                    dest[l++] = (((l2 & 0xff00ff) * opacity & 0xff00ff00) + ((l2 & 0xff00) * opacity & 0xff0000) >> 8) + color;
                } else {
                    l++;
                }
            }
			l += j;
			i += k;
		}

	}
}
