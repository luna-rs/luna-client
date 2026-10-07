// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public class GameObject extends Entity {

	public static client aClient1723;
	public int anInt1715;
	public int anInt1716;
	public int anInt1717;
	public int anInt1718;
	public int anInt1719;
	public int objectId;
	public int anInt1721;
	public int anInt1722;
	public Animation aClass14_1724;
	public int varBitId;
	public int anInt1726;
	public int childrenIds[];
	public int nextFrameTime;
	public int frame;

	public GameObject(int objectId, int i, int j, int k, int l, int i1, boolean flag, int k1, int l1) {
		this.objectId = objectId;
		anInt1721 = i1;
		anInt1722 = l1;
		anInt1715 = k1;
		anInt1716 = l;
		anInt1717 = j;
		anInt1718 = k;
		if (i != -1) {
			aClass14_1724 = Animation.animations[i];
			frame = 0;
			nextFrameTime = client.pulseCycle - 1;
			if (flag && aClass14_1724.anInt298 != -1) {
				frame = (int) (Math.random() * aClass14_1724.anInt294);
				nextFrameTime -= (int) (Math.random() * aClass14_1724.method205(0, frame));
			}
		}
		ObjectDefinition class47 = ObjectDefinition.forId(this.objectId);
		varBitId = class47.varbitId;
		anInt1726 = class47.anInt781;
		childrenIds = class47.anIntArray805;
	}

	public ObjectDefinition getObjectDefinition() {
		int child = -1;
		if (varBitId != -1) {
			Varbit varbit = Varbit.varbitTable[varBitId];
			int configId = varbit.varpId;
			int lsb = varbit.leastSignificantBit;
			int msb = varbit.mostSignificantBit;
			int j1 = client.BITFIELD_MAX_VALUES[msb - lsb];
			child = aClient1723.localVarps[configId] >> lsb & j1;
		} else if (anInt1726 != -1)
			child = aClient1723.localVarps[anInt1726];
		if (child < 0 || child >= childrenIds.length || childrenIds[child] == -1)
			return null;
		else
			return ObjectDefinition.forId(childrenIds[child]);
	}

	@Override
	public Model getModel() {
		int i = -1;
		if (aClass14_1724 != null) {
			int j = client.pulseCycle - nextFrameTime;
			if (j > 100 && aClass14_1724.anInt298 > 0)
				j = 100;
			while (j > aClass14_1724.method205(0, frame)) {
				j -= aClass14_1724.method205(0, frame);
				frame++;
				if (frame < aClass14_1724.anInt294)
					continue;
				frame -= aClass14_1724.anInt298;
				if (frame >= 0 && frame < aClass14_1724.anInt294)
					continue;
				aClass14_1724 = null;
				break;
			}
			nextFrameTime = client.pulseCycle - j;
			if (aClass14_1724 != null)
				i = aClass14_1724.anIntArray295[frame];
		}
		ObjectDefinition class47;
		if (childrenIds != null)
			class47 = getObjectDefinition();
		else
			class47 = ObjectDefinition.forId(objectId);
		if (class47 == null) {
			return null;
		} else {
			Model class50_sub1_sub4_sub4 = class47.method431(anInt1721, anInt1722, anInt1715,
					anInt1716, anInt1717, anInt1718, i);
			return class50_sub1_sub4_sub4;
		}
	}
}
