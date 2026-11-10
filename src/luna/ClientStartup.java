package luna;

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Coordinates an optional local launcher's splash handoff without changing client settings or startup arguments.
 *
 * @author lare96
 */
public final class ClientStartup {
    private static final AtomicBoolean PREPARED_REPORTED = new AtomicBoolean();
    private static final AtomicBoolean SHOWN_REPORTED = new AtomicBoolean();
    private static final Session SESSION = session(System.getenv(), SwingUtilities::invokeLater, 30_000L);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("LUNA_CLIENT_STOPPED");
            System.out.flush();
        }, "Luna-client-shutdown"));
    }

    private ClientStartup() {
    }

    /**
     * Starts the optional loopback connection before cache loading begins.
     */
    public static void initialize() {
        // Initializing this class starts the daemon handoff worker when valid launch metadata exists.
    }

    /**
     * Returns whether a launcher-managed window is still waiting for permission to appear.
     */
    public static boolean hidden() {
        return SESSION != null && SESSION.hidden();
    }

    /**
     * Returns whether valid transient metadata identifies a launcher-managed startup.
     */
    public static boolean managed() {
        return SESSION != null;
    }

    /**
     * Shows ordinary standalone windows immediately, or schedules a managed reveal on the AWT event thread.
     */
    public static void showWhenReady(Runnable reveal) {
        if (SESSION == null) reveal.run();
        else SESSION.attach(reveal);
    }

    /**
     * Announces a fully drawn usable login backbuffer without requiring a visible window.
     */
    public static void prepared() {
        if (SESSION == null) return;
        if (PREPARED_REPORTED.compareAndSet(false, true)) {
            System.out.println("LUNA_CLIENT_PREPARED");
            System.out.flush();
        }
        SESSION.prepared();
    }

    /**
     * Releases a known startup error screen after the launcher closes its splash, or if the parent is lost.
     */
    public static void failed() {
        if (SESSION != null) SESSION.failed();
    }

    /**
     * Returns the short content fade's opacity; native window decorations remain unchanged.
     */
    public static float presentationAlpha() {
        return SESSION == null ? 1f : SESSION.alpha();
    }

    /**
     * Acknowledges the first opaque, visible login frame after the handoff and content fade finish.
     */
    public static void frameShown() {
        if (SESSION == null) return;
        if (SHOWN_REPORTED.compareAndSet(false, true)) {
            System.out.println("LUNA_CLIENT_SHOWN");
            System.out.flush();
        }
        SESSION.shown();
    }

    static Session session(Map<String, String> environment, Consumer<Runnable> dispatch, long timeoutMillis) {
        String portValue = environment.get("LUNA_LAUNCHER_PORT");
        String token = environment.get("LUNA_LAUNCHER_TOKEN");
        if (portValue == null || !portValue.matches("[0-9]{1,5}") || token == null || !token.matches("[0-9a-fA-F]{64}"))
            return null;
        int port = Integer.parseInt(portValue);
        if (port < 1 || port > 65535) return null;
        return new Session(port, token, dispatch, timeoutMillis);
    }

    /**
     * Keeps parent communication off the game and event threads and fails open if the parent goes away.
     *
     * @author lare96
     */
    static final class Session {
        private final Object lock = new Object();
        private final int port;
        private final String token;
        private final Consumer<Runnable> dispatch;
        private final long timeoutMillis;
        private Runnable reveal;
        private boolean prepared;
        private boolean failed;
        private boolean showGranted;
        private boolean abandoned;
        private boolean revealQueued;
        private boolean shown;
        private long preparedAt;
        private volatile boolean revealed;
        private volatile long revealedAt;

        Session(int port, String token, Consumer<Runnable> dispatch, long timeoutMillis) {
            this.port = port;
            this.token = token;
            this.dispatch = dispatch;
            this.timeoutMillis = timeoutMillis;
            Thread worker = new Thread(this::communicate, "Luna-startup-handoff");
            worker.setDaemon(true);
            worker.start();
        }

        void attach(Runnable reveal) {
            synchronized (lock) {
                if (revealed) {
                    dispatch.accept(reveal);
                    return;
                }
                this.reveal = reveal;
            }
            revealIfAllowed();
        }

        void prepared() {
            synchronized (lock) {
                if (!prepared) {
                    prepared = true;
                    preparedAt = System.nanoTime();
                }
            }
            revealIfAllowed();
        }

        void failed() {
            synchronized (lock) {
                failed = true;
                if (preparedAt == 0) preparedAt = System.nanoTime();
            }
            revealIfAllowed();
        }

        boolean hidden() {
            return !revealed;
        }

        float alpha() {
            if (!revealed) return 0f;
            return Math.min(1f, Math.max(0f, (System.nanoTime() - revealedAt) / 300_000_000f));
        }

        void shown() {
            synchronized (lock) {
                if ((prepared || failed) && revealed) shown = true;
            }
        }

        private void revealIfAllowed() {
            synchronized (lock) {
                if (!prepared && !failed || !showGranted && !abandoned || reveal == null || revealQueued) return;
                revealQueued = true;
            }
            dispatch.accept(() -> {
                Runnable action;
                synchronized (lock) {
                    action = reveal;
                }
                revealedAt = System.nanoTime();
                revealed = true;
                action.run();
            });
        }

        private void communicate() {
            long connectedAt = System.nanoTime();
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("127.0.0.1", port), 1500);
                socket.setSoTimeout(100);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));
                send(output, "HELLO " + token);
                boolean preparedSent = false;
                boolean failureSent = false;
                StringBuilder line = new StringBuilder();
                while (true) {
                    boolean ready;
                    boolean complete;
                    boolean failure;
                    boolean usable;
                    long preparedTime;
                    synchronized (lock) {
                        usable = prepared;
                        failure = failed;
                        ready = usable || failure;
                        complete = shown;
                        preparedTime = preparedAt;
                    }
                    if (failure && !failureSent) {
                        send(output, "FAILED");
                        failureSent = true;
                    } else if (usable && !preparedSent) {
                        send(output, "PREPARED");
                        preparedSent = true;
                    }
                    revealIfAllowed();
                    if (complete) {
                        send(output, "SHOWN");
                        return;
                    }
                    long now = System.nanoTime();
                    if (ready && now - preparedTime >= timeoutMillis * 1_000_000L
                            || !ready && now - connectedAt >= 150_000_000_000L)
                        throw new SocketTimeoutException("The launcher handoff timed out.");
                    int value;
                    try {
                        value = input.read();
                    } catch (SocketTimeoutException exception) {
                        continue;
                    }
                    if (value < 0) throw new EOFException("The launcher handoff ended.");
                    if (value == '\n') {
                        if ("SHOW".contentEquals(line)) {
                            synchronized (lock) {
                                showGranted = true;
                            }
                        }
                        line.setLength(0);
                    } else if (value != '\r') {
                        if (line.length() >= 128) throw new IOException("The launcher handoff message is too long.");
                        line.append((char) value);
                    }
                }
            } catch (IOException exception) {
                System.out.println("LUNA_CLIENT_HANDOFF_FALLBACK " + exception.getClass().getSimpleName());
                System.out.flush();
                synchronized (lock) {
                    abandoned = true;
                }
                revealIfAllowed();
            }
        }

        private static void send(BufferedWriter output, String message) throws IOException {
            output.write(message);
            output.newLine();
            output.flush();
        }
    }
}
