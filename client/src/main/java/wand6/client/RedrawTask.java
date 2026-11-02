package wand6.client;

import javax.swing.SwingWorker;

public class RedrawTask extends SwingWorker<Void, Void> {

    private final long fps;
    private WandJPanel panel;

    public RedrawTask(WandJPanel panel) {
        this.panel = panel;
        fps = 25L;
    }

    public RedrawTask(WandJPanel panel, long fps) throws IllegalArgumentException {
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
