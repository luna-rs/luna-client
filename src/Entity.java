// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public class Entity extends QueueNode {

	public VertexNormal normals[];
	public int height;

	public void render(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2) {
		Model model = getModel();
		if (model != null) {
			height = ((Entity) (model)).height;
			model.render(i, j, k, l, i1, j1, k1, l1, i2);
		}
	}

	/**
	 * Draws cached scenery with an explicit visual range and independent mouse-picking eligibility.
	 */
	public void render(int yaw, int sinPitch, int cosPitch, int sinYaw, int cosYaw,
			int relativeX, int relativeY, int relativeZ, int uid, int farClip, boolean allowPicking) {
		Model model = getModel();
		if (model != null) {
			height = model.height;
			model.render(yaw, sinPitch, cosPitch, sinYaw, cosYaw,
					relativeX, relativeY, relativeZ, uid, farClip, allowPicking);
		}
	}

	public Model getModel() {
		return null;
	}

	public Entity() {
		height = 1000;
	}

}
