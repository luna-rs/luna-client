import luna.ClientConfiguration;

/**
 * Applies the selected file's graphics settings before the client's AWT classes initialize.
 *
 * @author lare96
 */
public final class ClientLauncher {

    private ClientLauncher() {
    }

    public static void main(String[] args) {
        validateArguments(args);
        configureGraphics();
        client.main(new String[0]);
    }

    /**
     * Requires settings changes to be made in the selected properties file.
     */
    static void validateArguments(String[] args) {
        if (args.length != 0) {
            throw new IllegalArgumentException("Client startup arguments are not supported. Edit the selected client properties file instead.");
        }
    }

    /**
     * Applies the graphics settings shared by packaged and direct IDE launches.
     */
    static void configureGraphics() {
        ClientConfiguration configuration = ClientConfiguration.get();
        System.setProperty("sun.java2d.d3d", Boolean.toString(configuration.bool("graphicsD3d")));
        System.setProperty("sun.java2d.opengl", Boolean.toString(configuration.bool("graphicsOpenGl")));
        System.setProperty("sun.java2d.noddraw", Boolean.toString(configuration.bool("graphicsNoDraw")));
    }
}
