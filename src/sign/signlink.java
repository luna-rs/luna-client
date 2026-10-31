// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   signlink.java

package sign;

import java.applet.Applet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;

import luna.ClientPaths;

public class signlink implements Runnable {

	public static final MidiPlayer musicPlayer = new MidiPlayer();
	public static final WavePlayer wavePlayer = new WavePlayer();
	public static void startpriv(InetAddress inetaddress) {
		findcachedir();
		threadliveid = (int) (Math.random() * 99999999D);
		if (active) {
			try {
				Thread.sleep(500L);
			} catch (Exception _ex) {
			}
			active = false;
		}
		socketreq = 0;
		threadreq = null;
		dnsreq = null;
		savereq = null;
		urlreq = null;
		socketip = inetaddress;
		Thread thread = new Thread(new signlink());
		thread.setDaemon(true);
		thread.start();
		while (!active)
			try {
				Thread.sleep(50L);
			} catch (Exception _ex) {
			}
	}

	public void run() {
		String s = findcachedir();
		uid = getuid(s);
		try {
			cache_dat = new RandomAccessFile(new File(s, "main_file_cache.dat"), "rw");
			for (int j = 0; j < 5; j++)
				cache_idx[j] = new RandomAccessFile(new File(s, "main_file_cache.idx" + j), "rw");

		} catch (Exception exception) {
			exception.printStackTrace();
		}
		active = true;
		for (int i = threadliveid; threadliveid == i;) {
			if (socketreq != 0) {
				try {
					socket = new Socket(socketip, socketreq);
				} catch (Exception _ex) {
					socket = null;
				}
				socketreq = 0;
			} else if (threadreq != null) {
				Thread thread = new Thread(threadreq);
				thread.setDaemon(true);
				thread.start();
				thread.setPriority(threadreqpri);
				threadreq = null;
			} else if (dnsreq != null) {
				try {
					dns = InetAddress.getByName(dnsreq).getHostName();
				} catch (Exception _ex) {
					dns = "unknown";
				}
				dnsreq = null;
			} else if (savereq != null) {
				boolean saved = true;
				if (savebuf != null) {
					try (FileOutputStream fileoutputstream = new FileOutputStream(new File(s, savereq))) {
						fileoutputstream.write(savebuf, 0, savelen);
					} catch (IOException exception) {
						saved = false;
						System.err.println("Unable to save sound: " + exception);
					}
				}
				if (waveplay && saved) {
						wavePlayer.play(new File(s, savereq));
				}
				waveplay = false;
				savebuf = null;
				savereq = null;
			} else if (urlreq != null) {
				try {
					urlstream = new DataInputStream((new URL(mainapp.getCodeBase(), urlreq)).openStream());
				} catch (Exception _ex) {
					urlstream = null;
				}
				urlreq = null;
			}
			try {
				Thread.sleep(50L);
			} catch (Exception _ex) {
			}
		}

	}

	public static String findcachedir() {
		try {
			return ClientPaths.ensureCacheDirectory().toString() + File.separator;
		} catch (IOException exception) {
			throw new UncheckedIOException(exception.getMessage(), exception);
		}
	}

	public static int getuid(String s) {
		try {
			File file = new File(s, "uid.dat");
			if (!file.exists() || file.length() < 4L) {
				DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(s + "uid.dat"));
				dataoutputstream.writeInt((int) (Math.random() * 99999999D));
				dataoutputstream.close();
			}
		} catch (Exception _ex) {
		}
		try {
			DataInputStream datainputstream = new DataInputStream(new FileInputStream(new File(s, "uid.dat")));
			int i = datainputstream.readInt();
			datainputstream.close();
			return i + 1;
		} catch (Exception _ex) {
			return 0;
		}
	}

	public static synchronized Socket opensocket(int i) throws IOException {
		for (socketreq = i; socketreq != 0;)
			try {
				Thread.sleep(50L);
			} catch (Exception _ex) {
			}

		if (socket == null)
			throw new IOException("could not open socket");
		else
			return socket;
	}

	public static synchronized DataInputStream openurl(String s) throws IOException {
		for (urlreq = s; urlreq != null;)
			try {
				Thread.sleep(50L);
			} catch (Exception _ex) {
			}

		if (urlstream == null)
			throw new IOException("could not open: " + s);
		else
			return urlstream;
	}

	public static synchronized void dnslookup(String s) {
		dns = s;
		dnsreq = s;
	}

	public static synchronized void startthread(Runnable runnable, int i) {
		threadreqpri = i;
		threadreq = runnable;
	}

	public static synchronized boolean wavesave(byte abyte0[], int i) {
		if (abyte0 == null || i < 0 || i > abyte0.length || i > 0x1e8480)
			return false;
		if (savereq != null) {
			return false;
		} else {
			wavepos = (wavepos + 1) % 5;
			savelen = i;
			savebuf = Arrays.copyOf(abyte0, i);
			waveplay = true;
			savereq = "sound" + wavepos + ".wav";
			return true;
		}
	}

	public static synchronized boolean wavereplay() {
		if (savereq != null) {
			return false;
		} else {
			savebuf = null;
			waveplay = true;
			savereq = "sound" + wavepos + ".wav";
			return true;
		}
	}

	public static void reporterror(String s) {
		if (!reporterror)
			return;
		if (!active)
			return;
		System.out.println("Error: " + s);
		try {
			s = s.replace(':', '_');
			s = s.replace('@', '_');
			s = s.replace('&', '_');
			s = s.replace('#', '_');
			DataInputStream datainputstream = openurl("reporterror" + 377 + ".cgi?error=" + errorname + " " + s);
			for (int read = datainputstream.read(); (read != 10 && read != -1); read = datainputstream.read());
			datainputstream.close();
			return;
		} catch (IOException _ex) {
			return;
		}
	}

	public signlink() {
	}

	public static final int clientversion = 377;
	public static int uid;
	public static int storeid = 32;
	public static RandomAccessFile cache_dat = null;
	public static RandomAccessFile cache_idx[] = new RandomAccessFile[5];
	public static boolean sunjava;
	public static Applet mainapp = null;
	public static volatile boolean active;
	public static int threadliveid;
	public static InetAddress socketip;
	public static int socketreq;
	public static Socket socket = null;
	public static int threadreqpri = 1;
	public static Runnable threadreq = null;
	public static String dnsreq = null;
	public static String dns = null;
	public static String urlreq = null;
	public static DataInputStream urlstream = null;
	public static int savelen;
	public static volatile String savereq = null;
	public static byte savebuf[] = null;
	public static boolean waveplay;
	public static int wavepos;
	public static String wave = null;
	public static boolean reporterror = true;
	public static String errorname = "";

}
