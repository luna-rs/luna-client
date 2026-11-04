package luna.browser.applet;

import java.net.URL;

/**
 * Keeps legacy loading failures inside the browser diagnostic output.
 *
 * @author lare96
 */
public class AppletContext {
    public void showDocument(URL url) { System.err.println("Client requested document: " + url); }
}
