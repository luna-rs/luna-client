/**
 * Selects software graphics before the client's AWT classes initialize.
 *
 * @author lare96
 */
public final class ClientLauncher {

    private ClientLauncher() {
    }

    public static void main(String[] args) {
        configureGraphics();
        client.main(args);
    }

    /**
     * Applies the graphics settings shared by packaged and direct IDE launches.
     */
    static void configureGraphics() {
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.opengl", "false");
        System.setProperty("sun.java2d.noddraw", "true");
    }
}
