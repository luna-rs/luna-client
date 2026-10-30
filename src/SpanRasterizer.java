/**
 * Runs the original scanline kernels with state owned by one raster worker.
 *
 * @author lare96
 */
final class SpanRasterizer {
    private boolean jagged;
    private boolean hClip;
    private boolean lowMemory;
    private boolean aBoolean1529;
    private int anInt1531;
    private int halfParentWidth;
    private int lastPixelX;
    private int[] divTable;
    private int[] anIntArray1548;

    void configure(int lastPixelX, int halfParentWidth, int[] divTable, int[] palette) {
        this.lastPixelX = lastPixelX;
        this.halfParentWidth = halfParentWidth;
        this.divTable = divTable;
        anIntArray1548 = palette;
    }

    void state(int flags, int alpha) {
        hClip = (flags & ParallelRasterizer.CLIPPED) != 0;
        jagged = (flags & ParallelRasterizer.JAGGED) != 0;
        lowMemory = (flags & ParallelRasterizer.LOW_MEMORY) != 0;
        aBoolean1529 = (flags & ParallelRasterizer.OPAQUE_TEXTURE) != 0;
        anInt1531 = alpha;
    }
    void gouraudRaster(int ai[], int i, int j, int k, int l, int i1, int j1, int k1) {
		if (jagged) {
			int l1;
			if (hClip) {
				if (i1 - l > 3)
					l1 = (k1 - j1) / (i1 - l);
				else
					l1 = 0;
				if (i1 > lastPixelX)
					i1 = lastPixelX;
				if (l < 0) {
					j1 -= l * l1;
					l = 0;
				}
				if (l >= i1)
					return;
				i += l;
				k = i1 - l >> 2;
				l1 <<= 2;
			} else {
				if (l >= i1)
					return;
				i += l;
				k = i1 - l >> 2;
				if (k > 0)
					l1 = (k1 - j1) * divTable[k] >> 15;
				else
					l1 = 0;
			}
			if (anInt1531 == 0) {
				while (--k >= 0) {
					j = anIntArray1548[j1 >> 8];
					j1 += l1;
					ai[i++] = j;
					ai[i++] = j;
					ai[i++] = j;
					ai[i++] = j;
				}
				k = i1 - l & 3;
				if (k > 0) {
					j = anIntArray1548[j1 >> 8];
					do
						ai[i++] = j;
					while (--k > 0);
					return;
				}
			} else {
				int j2 = anInt1531;
				int l2 = 256 - anInt1531;
				while (--k >= 0) {
					j = anIntArray1548[j1 >> 8];
					j1 += l1;
					j = ((j & 0xff00ff) * l2 >> 8 & 0xff00ff) + ((j & 0xff00) * l2 >> 8 & 0xff00);
					ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
					ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
					ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
					ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
				}
				k = i1 - l & 3;
				if (k > 0) {
					j = anIntArray1548[j1 >> 8];
					j = ((j & 0xff00ff) * l2 >> 8 & 0xff00ff) + ((j & 0xff00) * l2 >> 8 & 0xff00);
					do
						ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
					while (--k > 0);
				}
			}
			return;
		}
		if (l >= i1) {
            return;
        }
		int i2 = (k1 - j1) / (i1 - l);
		if (hClip) {
			if (i1 > lastPixelX)
				i1 = lastPixelX;
			if (l < 0) {
				j1 -= l * i2;
				l = 0;
			}
			if (l >= i1)
				return;
		}
		i += l;
		k = i1 - l;
		if (anInt1531 == 0) {
			do {
				ai[i++] = anIntArray1548[j1 >> 8];
				j1 += i2;
			} while (--k > 0);
			return;
		}
		int k2 = anInt1531;
		int i3 = 256 - anInt1531;
		do {
			j = anIntArray1548[j1 >> 8];
			j1 += i2;
			j = ((j & 0xff00ff) * i3 >> 8 & 0xff00ff) + ((j & 0xff00) * i3 >> 8 & 0xff00);
			ai[i++] = j + ((ai[i] & 0xff00ff) * k2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * k2 >> 8 & 0xff00);
		} while (--k > 0);
	}

    void flatRaster(int ai[], int i, int j, int k, int l, int i1) {
		if (hClip) {
			if (i1 > lastPixelX)
				i1 = lastPixelX;
			if (l < 0)
				l = 0;
		}
		if (l >= i1)
			return;
		i += l;
		k = i1 - l >> 2;
		if (anInt1531 == 0) {
			while (--k >= 0) {
				ai[i++] = j;
				ai[i++] = j;
				ai[i++] = j;
				ai[i++] = j;
			}
			for (k = i1 - l & 3; --k >= 0;)
				ai[i++] = j;

			return;
		}
		int j1 = anInt1531;
		int k1 = 256 - anInt1531;
		j = ((j & 0xff00ff) * k1 >> 8 & 0xff00ff) + ((j & 0xff00) * k1 >> 8 & 0xff00);
		while (--k >= 0) {
			ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
			ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
			ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
			ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
		}
		for (k = i1 - l & 3; --k >= 0;)
			ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);

	}

    void textureRaster(int ai[], int ai1[], int i, int j, int k, int l, int i1, int j1, int k1, int l1,
									 int i2, int j2, int k2, int l2, int i3) {
		if (l >= i1) {
            return;
        }
		int j3;
		int k3;
		if (hClip) {
			j3 = (k1 - j1) / (i1 - l);
			if (i1 > lastPixelX) {
                i1 = lastPixelX;
            }
			if (l < 0) {
				j1 -= l * j3;
				l = 0;
			}
			if (l >= i1) {
                return;
            }
			k3 = i1 - l >> 3;
			j3 <<= 12;
			j1 <<= 9;
		} else {
			if (i1 - l > 7) {
				k3 = i1 - l >> 3;
				j3 = (k1 - j1) * divTable[k3] >> 6;
			} else {
				k3 = 0;
				j3 = 0;
			}
			j1 <<= 9;
		}
		k += l;
		if (lowMemory) {
			int i4 = 0;
			int k4 = 0;
			int k6 = l - halfParentWidth;
			l1 += (k2 >> 3) * k6;
			i2 += (l2 >> 3) * k6;
			j2 += (i3 >> 3) * k6;
			int i5 = j2 >> 12;
			if (i5 != 0) {
				i = l1 / i5;
				j = i2 / i5;
				if (i < 0)
					i = 0;
				else if (i > 4032)
					i = 4032;
			}
			l1 += k2;
			i2 += l2;
			j2 += i3;
			i5 = j2 >> 12;
			if (i5 != 0) {
				i4 = l1 / i5;
				k4 = i2 / i5;
				if (i4 < 7)
					i4 = 7;
				else if (i4 > 4032)
					i4 = 4032;
			}
			int i7 = i4 - i >> 3;
			int k7 = k4 - j >> 3;
			i += (j1 & 0x600000) >> 3;
			int i8 = j1 >> 23;
			if (aBoolean1529) {
				while (k3-- > 0) {
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i = i4;
					j = k4;
					l1 += k2;
					i2 += l2;
					j2 += i3;
					int j5 = j2 >> 12;
					if (j5 != 0) {
						i4 = l1 / j5;
						k4 = i2 / j5;
						if (i4 < 7)
							i4 = 7;
						else if (i4 > 4032)
							i4 = 4032;
					}
					i7 = i4 - i >> 3;
					k7 = k4 - j >> 3;
					j1 += j3;
					i += (j1 & 0x600000) >> 3;
					i8 = j1 >> 23;
				}
				for (k3 = i1 - l & 7; k3-- > 0;) {
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
				}

				return;
			}
			while (k3-- > 0) {
				int k8;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
					ai[k] = k8;
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
					ai[k] = k8;
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
					ai[k] = k8;
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
					ai[k] = k8;
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
					ai[k] = k8;
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
					ai[k] = k8;
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
					ai[k] = k8;
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
					ai[k] = k8;
				k++;
				i = i4;
				j = k4;
				l1 += k2;
				i2 += l2;
				j2 += i3;
				int k5 = j2 >> 12;
				if (k5 != 0) {
					i4 = l1 / k5;
					k4 = i2 / k5;
					if (i4 < 7)
						i4 = 7;
					else if (i4 > 4032)
						i4 = 4032;
				}
				i7 = i4 - i >> 3;
				k7 = k4 - j >> 3;
				j1 += j3;
				i += (j1 & 0x600000) >> 3;
				i8 = j1 >> 23;
			}
			for (k3 = i1 - l & 7; k3-- > 0;) {
				int l8;
				if ((l8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0)
					ai[k] = l8;
				k++;
				i += i7;
				j += k7;
			}

			return;
		}
		int j4 = 0;
		int l4 = 0;
		int l6 = l - halfParentWidth;
		l1 += (k2 >> 3) * l6;
		i2 += (l2 >> 3) * l6;
		j2 += (i3 >> 3) * l6;
		int l5 = j2 >> 14;
		if (l5 != 0) {
			i = l1 / l5;
			j = i2 / l5;
			if (i < 0)
				i = 0;
			else if (i > 16256)
				i = 16256;
		}
		l1 += k2;
		i2 += l2;
		j2 += i3;
		l5 = j2 >> 14;
		if (l5 != 0) {
			j4 = l1 / l5;
			l4 = i2 / l5;
			if (j4 < 7)
				j4 = 7;
			else if (j4 > 16256)
				j4 = 16256;
		}
		int j7 = j4 - i >> 3;
		int l7 = l4 - j >> 3;
		i += j1 & 0x600000;
		int j8 = j1 >> 23;
		if (aBoolean1529) {
			while (k3-- > 0) {
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i = j4;
				j = l4;
				l1 += k2;
				i2 += l2;
				j2 += i3;
				int i6 = j2 >> 14;
				if (i6 != 0) {
					j4 = l1 / i6;
					l4 = i2 / i6;
					if (j4 < 7)
						j4 = 7;
					else if (j4 > 16256)
						j4 = 16256;
				}
				j7 = j4 - i >> 3;
				l7 = l4 - j >> 3;
				j1 += j3;
				i += j1 & 0x600000;
				j8 = j1 >> 23;
			}
			for (k3 = i1 - l & 7; k3-- > 0;) {
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
			}

			return;
		}
		while (k3-- > 0) {
			int i9;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
				ai[k] = i9;
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
				ai[k] = i9;
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
				ai[k] = i9;
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
				ai[k] = i9;
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
				ai[k] = i9;
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
				ai[k] = i9;
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
				ai[k] = i9;
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
				ai[k] = i9;
			k++;
			i = j4;
			j = l4;
			l1 += k2;
			i2 += l2;
			j2 += i3;
			int j6 = j2 >> 14;
			if (j6 != 0) {
				j4 = l1 / j6;
				l4 = i2 / j6;
				if (j4 < 7)
					j4 = 7;
				else if (j4 > 16256)
					j4 = 16256;
			}
			j7 = j4 - i >> 3;
			l7 = l4 - j >> 3;
			j1 += j3;
			i += j1 & 0x600000;
			j8 = j1 >> 23;
		}
		for (int l3 = i1 - l & 7; l3-- > 0;) {
			int j9;
			if ((j9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0)
				ai[k] = j9;
			k++;
			i += j7;
			j += l7;
		}

	}
}
