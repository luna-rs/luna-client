// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class JagSocket implements Runnable {

	public int anInt379;
	public byte aByte380;
	public boolean connectionClosed;
	public JagApplet anApplet_Sub1_385;
	public byte aByteArray386[];
	public int anInt387;
	public int anInt388;
	public boolean aBoolean389;
	public boolean ioError;

	public Socket socket;

	public InputStream inputStream;
	public OutputStream outputStream;

	public JagSocket(byte byte0, Socket socket, JagApplet applet_sub1) throws IOException {
		aByte380 = 2;
		connectionClosed = false;
		aBoolean389 = false;
		ioError = false;
		anApplet_Sub1_385 = applet_sub1;
		this.socket = socket;
		if (byte0 == aByte380)
			byte0 = 0;
		else
			anInt379 = -5;
		this.socket.setSoTimeout(30000);
		this.socket.setTcpNoDelay(true);
		inputStream = this.socket.getInputStream();
		outputStream = this.socket.getOutputStream();
	}

	public void closeConnection() {
		connectionClosed = true;
		try {
			if (inputStream != null)
				inputStream.close();
			if (outputStream != null)
				outputStream.close();
			if (socket != null)
				socket.close();
		} catch (IOException _ex) {
			System.out.println("Error closing stream");
		}
		aBoolean389 = false;
		synchronized (this) {
			notify();
		}
		aByteArray386 = null;
	}

	public int getByte() throws IOException {
		if (connectionClosed)
			return 0;
		else
			return inputStream.read();
	}

	public int available() throws IOException {
		if (connectionClosed)
			return 0;
		else
			return inputStream.available();
	}

	public void getBytes(byte abyte0[], int i, int j) throws IOException {
		if (connectionClosed)
			return;
		int k;
		for (; j > 0; j -= k) {
			k = inputStream.read(abyte0, i, j);
			if (k <= 0)
				throw new IOException("EOF");
			i += k;
		}

	}

	public void putBytes(int i, int j, int k, byte abyte0[]) throws IOException {
		if (connectionClosed)
			return;
		if (ioError) {
			ioError = false;
			throw new IOException("Error in writer thread");
		}
		if (aByteArray386 == null)
			aByteArray386 = new byte[5000];
		synchronized (this) {
			for (int l = 0; l < j; l++) {
				aByteArray386[anInt388] = abyte0[l + k];
				anInt388 = (anInt388 + 1) % 5000;
				if (anInt388 == (anInt387 + 4900) % 5000)
					throw new IOException("buffer overflow");
			}

			if (!aBoolean389) {
				aBoolean389 = true;
				anApplet_Sub1_385.startThread(this, 3);
			}
			notify();
		}
		if (i == 0)
			;
	}

	public void run() {
		while (aBoolean389) {
			int i;
			int j;
			synchronized (this) {
				if (anInt388 == anInt387)
					try {
						wait();
					} catch (InterruptedException _ex) {
					}
				if (!aBoolean389)
					return;
				j = anInt387;
				if (anInt388 >= anInt387)
					i = anInt388 - anInt387;
				else
					i = 5000 - anInt387;
			}
			if (i > 0) {
				try {
					outputStream.write(aByteArray386, j, i);
				} catch (IOException _ex) {
					ioError = true;
				}
				anInt387 = (anInt387 + i) % 5000;
				try {
					if (anInt388 == anInt387)
						outputStream.flush();
				} catch (IOException _ex) {
					ioError = true;
				}
			}
		}
	}

	public void debugPrint(boolean flag) {
		System.out.println("dummy:" + connectionClosed);
		System.out.println("tcycl:" + anInt387);
		System.out.println("tnum:" + anInt388);
		System.out.println("writer:" + aBoolean389);
		if (flag)
			return;
		System.out.println("ioerror:" + ioError);
		try {
			System.out.println("available:" + available());
			return;
		} catch (IOException _ex) {
			return;
		}
	}
}
