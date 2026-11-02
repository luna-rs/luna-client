package luna.browser.awt;

/**
 * Browser image decoding already waits for the decoded pixels.
 *
 * @author lare96
 */
public class MediaTracker {
    public MediaTracker(Component component) { }
    public void addImage(Image image, int id) { }
    public void waitForAll() { }
}
