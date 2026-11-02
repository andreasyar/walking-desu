package wand6.client;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import wand6.common.exceptions.MessageManagerException;

final class GUI {
    final WandJPanel panel = new WandJPanel();
    final JFrame frame;
    final Timer redraw;

    GUI(ServerInteraction inter) {
        String version = Wand.class.getPackage().getImplementationVersion();
        frame = new JFrame("Walking Desu wand6 " + (version == null ? "development" : version));
        frame.setName("wand6");
        panel.addMouseListener(panel);
        panel.addComponentListener(panel);
        panel.addKeyListener(panel);
        panel.setFocusable(true);
        JTextField message = new JTextField(35);
        JButton send = new JButton("Send");
        message.setName("chat-input");
        send.setName("chat-send");
        Runnable sendText = () -> {
            try {
                String text = message.getText();
                PlayerManager.getInstance().setSelfPlayerText(text.substring(0, Math.min(100, text.length())));
                MessageManager.sendTextCloudMessage();
            } catch (NullPointerException | MessageManagerException ex) {
                System.err.println("Cannot send text before WELCOME: " + ex.getMessage());
            }
        };
        message.addActionListener(e -> sendText.run());
        send.addActionListener(e -> sendText.run());
        JPanel controls = new JPanel();
        controls.add(message);
        controls.add(send);
        frame.add(new JLabel("B + клик — башня · ЛКМ — предмет/цель/движение · F3 — атака · Esc — отмена · ПКМ — стоп."), BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        redraw = new Timer(40, e -> { ItemManager.getInstance().tick(); panel.repaint(); });
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                redraw.stop();
                inter.close();
            }
        });
    }
}
