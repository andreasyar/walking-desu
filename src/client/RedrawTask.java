package client;

import javax.swing.SwingWorker;

public class RedrawTask extends SwingWorker<Void, Void> {
    private final long fps = 50;
    private WanderingJPanel panel;

    public RedrawTask(WanderingJPanel panel) {
        this.panel = panel;
    }

    @Override
    protected Void doInBackground() {
        while (true) {
            panel.repaint();
            try {
                Thread.sleep(1000 / fps);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
