package wand6.client;

import client.GameField;
import client.LoginDialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;

class GUI implements Runnable {
    public final WandJPanel panel;
    public final JFrame frame;
    public final JTextField msgField;
    public final JButton sendBtn;
    public final LoginDialog dialog;

    GUI(GameField field, ServerInteraction inter) {
        panel = new WandJPanel(field, inter);
        panel.addMouseListener(panel);
        panel.addComponentListener(panel);
        panel.addKeyListener(panel);
        frame = new JFrame("Walking Desu 5");
        dialog = new LoginDialog(frame);
        msgField = new JTextField(50);
        sendBtn = new JButton("Send");

        msgField.addKeyListener(new msgFieldKeyListener(inter));
        sendBtn.addActionListener(new sendBtnActionListener(inter));
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

    void showPanel() {
        panel.setVisible(false);
        panel.setVisible(true);
    }

    private class msgFieldKeyListener extends KeyAdapter {
        private ServerInteraction inter;

        public msgFieldKeyListener(ServerInteraction inter) {
            this.inter = inter;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                String msgText = msgField.getText();
                PlayerManager pm = PlayerManager.getInstance();
                pm.setSelfPlayerText(msgText.length() > 100 ? msgText.substring(0, 99) : msgText);
                inter.sendMessage(MessageManager.getInstance().getTextCloudMessage(pm.getSelfPlayerId()));
            }
        }
    }

    private class sendBtnActionListener implements ActionListener {

        private ServerInteraction inter;

        sendBtnActionListener(ServerInteraction inter) {
            this.inter = inter;
        }

        public void actionPerformed(ActionEvent e) {
            String msgText = msgField.getText();
            PlayerManager pm = PlayerManager.getInstance();
            pm.setSelfPlayerText(msgText.length() > 100 ? msgText.substring(0, 99) : msgText);
            inter.sendMessage(MessageManager.getInstance().getTextCloudMessage(pm.getSelfPlayerId()));
        }
    }
}
