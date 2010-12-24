package wand6.client;

import client.GameField;
import client.LoginDialog;
import client.Player;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import wand6.client.messages.TextCloudMessage;

public class GUI implements Runnable {
    public final WandJPanel panel;
    public final JFrame frame;
    public final JTextField msgField;
    public final JButton sendBtn;
    public final LoginDialog dialog;

    public GUI(GameField field, ServerInteraction inter) {
        panel = new WandJPanel(field, inter);
        panel.addMouseListener(panel);
        panel.addComponentListener(panel);
        panel.addKeyListener(panel);
        frame = new JFrame("Walking Desu 5");
        dialog = new LoginDialog(frame);
        msgField = new JTextField(50);
        sendBtn = new JButton("Send");

        msgField.addKeyListener(new msgFieldKeyListener(field, inter));
        sendBtn.addActionListener(new sendBtnActionListener(field, inter));
    }

    @Override
    public void run() {
        panel.add(sendBtn);
        panel.add(msgField);
        panel.setFocusable(true);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640,480);
        dialog.setSize(new Dimension(400, 200));
        frame.setVisible(true);
        dialog.startTimer();
        dialog.setVisible(true);
    }

    public void showPanel() {
        panel.setVisible(false);
        panel.setVisible(true);
    }

    private class msgFieldKeyListener extends KeyAdapter {
        private GameField field;
        private ServerInteraction inter;

        public msgFieldKeyListener(GameField field, ServerInteraction inter) {
            this.field = field;
            this.inter = inter;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                String msgText = msgField.getText();
                Player self = field.getSelfPlayer();

                if (self != null && msgText != null && (self.getText() == null || !self.getText().equals(msgText))) {
                    self.setText(msgText.length() > 100 ? msgText.substring(0, 99) : msgText);
                    inter.sendMessage(new TextCloudMessage(self.getText()));
                }
            }
        }
    }

    private class sendBtnActionListener implements ActionListener {
        private GameField field;
        private ServerInteraction inter;

        public sendBtnActionListener(GameField field, ServerInteraction inter) {
            this.field = field;
            this.inter = inter;
        }

        public void actionPerformed(ActionEvent e) {
            String msgText = msgField.getText();
            Player self = field.getSelfPlayer();

            if (self != null && msgText != null && (self.getText() == null || !self.getText().equals(msgText))) {
                self.setText(msgText.length() > 100 ? msgText.substring(0, 99) : msgText);
                inter.sendMessage(new TextCloudMessage(self.getText()));
            }
        }
    }
}
