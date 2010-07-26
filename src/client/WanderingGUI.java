package client;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;

public class WanderingGUI implements Runnable {
    public final WanderingJPanel panel;
    public final JFrame frame;
    public final JTextField msgField;
    public final JButton sendBtn;
    public final LoginDialog dialog;

    public WanderingGUI(GameField field, ServerInteraction inter) {
        panel = new WanderingJPanel(field, inter);
        frame = new JFrame("Walking Desu 4");
        dialog = new LoginDialog(frame);
        msgField = new JTextField(50);
        sendBtn = new JButton("Send");
    }

    public void run() {
        panel.add(sendBtn);
        panel.add(msgField);
        panel.setFocusable(true);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);
        dialog.setSize(new Dimension(400, 200));
        frame.setVisible(true);
        dialog.startTimer();
        dialog.setVisible(true);
    }

    public void showPanel() {
        panel.setVisible(false);
        panel.setVisible(true);
    }
}
