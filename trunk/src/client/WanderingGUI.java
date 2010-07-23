package client;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;

public class WanderingGUI implements Runnable {
    private boolean builded = false;

    public final WanderingJPanel panel = new WanderingJPanel();
    public final JFrame frame = new JFrame("Walking Desu 4");
    public final JTextField msgField = new JTextField(50);
    public final JButton sendBtn = new JButton("Send");

    public void run() {
        panel.add(sendBtn);
        panel.add(msgField);
        panel.setFocusable(true);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);
        frame.setVisible(true);
        panel.setVisible(false);
        panel.setVisible(true);
        builded = true;
    }

    public boolean builded() {
        return builded;
    }
}
