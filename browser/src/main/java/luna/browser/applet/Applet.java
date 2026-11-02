package luna.browser.applet;

import java.net.URL;
import luna.browser.awt.Component;

/**
 * Browser component facade retaining the shared client's historical applet surface.
 *
 * @author lare96
 */
public class Applet extends Component {
    public String getParameter(String name) { return null; }
    public URL getCodeBase() { return null; }
    public URL getDocumentBase() { return getCodeBase(); }
    public AppletContext getAppletContext() { return new AppletContext(); }
    public void start() { }
    public void stop() { }
    public void destroy() { }
}
