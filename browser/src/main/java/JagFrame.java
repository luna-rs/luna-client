import luna.ClientConfiguration;
import luna.browser.awt.BrowserDisplay;
import luna.browser.awt.Component;

/**
 * Uses the browser canvas instead of creating a native desktop window.
 *
 * @author lare96
 */
public class JagFrame extends Component {
    public final JagApplet applet;
    private boolean resizable;
    public JagFrame(int width, int height, JagApplet applet, boolean resizable) {
        this.applet = applet;
        this.resizable = resizable;
        setContentSize(width, height);
        String title = ClientConfiguration.get().string("clientName");
        BrowserDisplay.setTitle(title);
        System.out.println("Browser title: " + title);
    }
    public void setContentSize(int width, int height) { setSize(width, height); }
    public int getContentWidth() { return getWidth(); }
    public int getContentHeight() { return getHeight(); }
    public void setMinimumContentSize(int width, int height) { }
    public void setResizable(boolean resizable) { this.resizable = resizable; }
    public void setExtendedState(int state) { }
}
