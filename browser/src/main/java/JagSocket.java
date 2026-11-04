import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import luna.browser.BrowserDiagnostics;
import luna.browser.net.Socket;
import org.teavm.jso.JSBody;

/**
 * Flushes shared protocol packets directly to the browser's asynchronous binary WebSocket transport.
 *
 * @author lare96
 */
public final class JagSocket {
    public boolean connectionClosed;
    public final Socket socket;
    public final InputStream inputStream;
    public final OutputStream outputStream;

    public JagSocket(byte unused, Socket socket, JagApplet applet) throws IOException {
        this.socket = socket;
        socket.setSoTimeout(30000);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    public void closeConnection() {
        trace("closing", 0);
        connectionClosed = true;
        socket.close();
    }

    public int getByte() throws IOException {
        try {
            return connectionClosed ? 0 : inputStream.read();
        } catch (IOException | RuntimeException failure) {
            report("reading a byte", 1, failure);
            throw failure;
        }
    }

    public int available() throws IOException {
        try {
            return connectionClosed ? 0 : inputStream.available();
        } catch (IOException | RuntimeException failure) {
            report("checking available bytes", 0, failure);
            throw failure;
        }
    }

    public void getBytes(byte[] bytes, int offset, int length) throws IOException {
        if (connectionClosed) return;
        int requested = length;
        try {
            while (length > 0) {
                int count = inputStream.read(bytes, offset, length);
                if (count <= 0) throw new IOException("Browser connection reached EOF");
                offset += count;
                length -= count;
            }
        } catch (IOException | RuntimeException failure) {
            report("reading packet", requested, failure);
            throw failure;
        }
    }

    public void putBytes(int unused, int length, int offset, byte[] bytes) throws IOException {
        if (connectionClosed) return;
        try {
            outputStream.write(bytes, offset, length);
        } catch (IOException | RuntimeException failure) {
            report("writing packet", length, failure);
            throw failure;
        }
    }

    public void debugPrint(boolean unused) {
        System.out.println("Browser transport closed=" + connectionClosed);
    }

    private static void report(String operation, int length, Throwable failure) {
        System.err.println("Browser transport failure while " + operation + " (" + length + " bytes): " + failure);
        failure.printStackTrace();
        BrowserDiagnostics.report(failure);
        trace(operation, length);
    }

    @JSBody(params = {"operation", "length"}, script = "console.warn('Browser transport ' + operation + ' (' + length + ' bytes): ' + new Error().stack);")
    private static native void trace(String operation, int length);
}
