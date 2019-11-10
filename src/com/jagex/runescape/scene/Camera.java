package com.jagex.runescape.scene;

import com.jagex.runescape.Model;

public final class Camera {

	public volatile int x;
	public volatile int y;
	public volatile int z;

	public volatile int yaw;
	public volatile int pitch;
	public volatile int roll;

	public synchronized void setCameraPosition(final int x, final int y, final int z, final int horizontal,
			final int vertical) {
		final int verticalDifference = 2048 - vertical & 0x7FF;
		final int horizontalDifference = 2048 - horizontal & 0x7FF;
		int offsetX = 0;
		int offsetZ = 0;
		int offsetY = 600 + vertical * 3;
		if (verticalDifference != 0) {
			final int sine = Model.SINE[verticalDifference];
			final int cos = Model.COSINE[verticalDifference];
			final int tmp = offsetZ * cos - offsetY * sine >> 16;
			offsetY = offsetZ * sine + offsetY * cos >> 16;
			offsetZ = tmp;
		}
		if (horizontalDifference != 0) {
			final int sin = Model.SINE[horizontalDifference];
			final int cos = Model.COSINE[horizontalDifference];
			final int tmp = offsetY * sin + offsetX * cos >> 16;
			offsetY = offsetY * cos - offsetX * sin >> 16;
			offsetX = tmp;
		}
		this.x = x - offsetX;
		this.z = z - offsetZ;
		this.y = y - offsetY;
		this.yaw = vertical;
		this.pitch = horizontal;
	}
}
