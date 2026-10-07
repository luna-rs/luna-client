// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public class Varp {

	public static int count;
	public static Varp varpTable[];
	public static int index_705;
	public static int anIntArray706[];
	public String aString707;
	public int anInt708;
	public int anInt709;
	public boolean aBoolean710;
	public boolean aBoolean711;
	public int type;
	public boolean aBoolean713;
	public int anInt714;
	public int anInt715;
	public boolean aBoolean716;
	public int anInt717;
	public boolean aBoolean718;

	public Varp() {
		aBoolean710 = false;
		aBoolean711 = true;
		aBoolean713 = false;
		aBoolean716 = false;
		anInt717 = -1;
		aBoolean718 = true;
	}

	public static void unpack(Archive archive) {
		JagBuffer buf = new JagBuffer(archive.get("varp.dat"));
		index_705 = 0;
		count = buf.getShort();

		if (varpTable == null)
			varpTable = new Varp[count];

		if (anIntArray706 == null)
			anIntArray706 = new int[count];

		for (int j = 0; j < count; j++) {
			if (varpTable[j] == null)
				varpTable[j] = new Varp();
			varpTable[j].init(j, buf);
		}

		if (buf.position != buf.buffer.length)
			System.out.println("varptype load mismatch");
	}

	public void init(int index, JagBuffer buf) {
		do {
			int opcode = buf.getByte();
			if (opcode == 0)
				return;
			if (opcode == 1)
				anInt708 = buf.getByte();
			else if (opcode == 2)
				anInt709 = buf.getByte();
			else if (opcode == 3) {
				aBoolean710 = true;
				anIntArray706[index_705++] = index;
			} else if (opcode == 4)
				aBoolean711 = false;
			else if (opcode == 5)
				type = buf.getShort();
			else if (opcode == 6)
				aBoolean713 = true;
			else if (opcode == 7)
				anInt714 = buf.getInt();
			else if (opcode == 8) {
				anInt715 = 1;
				aBoolean716 = true;
			} else if (opcode == 10)
				aString707 = buf.getString();
			else if (opcode == 11)
				aBoolean716 = true;
			else if (opcode == 12)
				anInt717 = buf.getInt();
			else if (opcode == 13) {
				anInt715 = 2;
				aBoolean716 = true;
			} else if (opcode == 14)
				aBoolean718 = false;
			else
				System.out.println("Error unrecognised config code: " + opcode);
		} while (true);
	}
}
