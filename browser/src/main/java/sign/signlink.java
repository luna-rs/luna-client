package sign;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import luna.ClientPaths;
import luna.browser.applet.Applet;
import luna.browser.io.BrowserIO;
import luna.browser.io.RandomAccessFile;
import luna.browser.net.InetAddress;
import luna.browser.net.Socket;

/**
 * Opens TeaVM virtual cache stores and supplies browser implementations of legacy client services.
 *
 * @author lare96
 */
public final class signlink {
    public static final MidiPlayer musicPlayer = new MidiPlayer();
    public static final WavePlayer wavePlayer = new WavePlayer();
    public static final int clientversion = 377;
    public static int uid = 1;
    public static int storeid = 32;
    public static RandomAccessFile cache_dat;
    public static RandomAccessFile[] cache_idx = new RandomAccessFile[5];
    public static boolean sunjava;
    public static Applet mainapp;
    public static boolean active;
    public static String dns = "unknown";
    public static boolean reporterror = true;
    public static String errorname = "";
    public static int wavevol;
    private static InetAddress address;

    private signlink() {
    }

    public static void startpriv(InetAddress host) throws IOException {
        address = host;
        String directory = findcachedir();
        uid = getuid(directory);
        cache_dat = new RandomAccessFile(new File(directory, "main_file_cache.dat"), "rw");
        for (int index = 0; index < cache_idx.length; index++)
            cache_idx[index] = new RandomAccessFile(new File(directory, "main_file_cache.idx" + index), "rw");
        System.out.println("Browser cache handles: data=" + cache_dat.length() + ", idx0=" + cache_idx[0].length());
        active = true;
    }

    public static String findcachedir() {
        try {
            return ClientPaths.ensureCacheDirectory() + "/";
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static int getuid(String directory) {
        return BrowserIO.deviceId();
    }

    public static Socket opensocket(int port) throws IOException {
        return new Socket(address, port);
    }

    public static DataInputStream openurl(String path) throws IOException {
        if (mainapp == null) throw new IOException("No browser resource base for " + path);
        return new DataInputStream(new URL(mainapp.getCodeBase(), path).openStream());
    }

    public static void dnslookup(String host) {
        dns = host;
    }

    public static void startthread(Runnable runnable, int priority) {
        new Thread(runnable).start();
    }

    public static boolean wavesave(byte[] data, int length) {
        return wavePlayer.play(data, length);
    }

    public static boolean wavereplay() {
        return wavePlayer.replay();
    }

    public static void reporterror(String error) {
        if (reporterror) System.err.println("Client error: " + error);
    }
}
