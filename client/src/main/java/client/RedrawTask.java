package client;

import javax.swing.SwingWorker;

/**
 * Redraw task cause panel redraw with optional fps rate.
 */
public class RedrawTask extends SwingWorker<Void, Void> {

    private final long fps;
    private WanderingJPanel panel;

    /**
     * Redraw panel with default rate.
     */
    public RedrawTask(WanderingJPanel panel) {
        this.panel = panel;
        fps = 50;
    }

    /**
     * Redraw panel with specifed rate.
     */
    public RedrawTask(WanderingJPanel panel, long fps) throws IllegalArgumentException {
        if (fps <= 0) {
            throw new IllegalArgumentException("fps must be greater than zero.");
        }

        this.panel = panel;
        this.fps = fps;
    }

    @Override
    protected Void doInBackground() {
        try {
            while (true) {
                panel.repaint();
                Thread.sleep(1000 / fps);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
