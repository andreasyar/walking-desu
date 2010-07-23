package client;

import javax.swing.SwingWorker;

public class RedrawTask extends SwingWorker<Void, Void> {
    private final long fps = 50;
    private WanderingGUI gui;

    public RedrawTask(WanderingGUI gui) {
        this.gui = gui;
    }

    @Override
    protected Void doInBackground() {
        while (!gui.builded()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
        while (true) {
            gui.panel.repaint();
            try {
                Thread.sleep(1000 / fps);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
