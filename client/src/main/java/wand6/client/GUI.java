package wand6.client;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import wand6.common.exceptions.MessageManagerException;

final class GUI {
    /** ~60 Hz — closer to typical display refresh than the old 25 fps Timer. */
    private static final long FRAME_NS = 16_666_667L;
    final WandJPanel panel = new WandJPanel();
    final JFrame frame;
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean framePending = new AtomicBoolean();
    private Thread redraw;

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
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                stopRedraw();
                inter.close();
            }
        });
    }

    void startRedraw() {
        if (!running.compareAndSet(false, true)) return;
        redraw = new Thread(this::redrawLoop, "wand6-redraw");
        redraw.setDaemon(true);
        redraw.start();
    }

    void stopRedraw() {
        running.set(false);
        Thread thread = redraw;
        if (thread != null) thread.interrupt();
    }

    private void redrawLoop() {
        long next = System.nanoTime();
        while (running.get()) {
            if (framePending.compareAndSet(false, true)) {
                SwingUtilities.invokeLater(this::drawFrame);
            }
            next += FRAME_NS;
            long sleep = next - System.nanoTime();
            if (sleep > 0) {
                LockSupport.parkNanos(sleep);
            } else {
                next = System.nanoTime();
            }
        }
    }

    private void drawFrame() {
        try {
            if (!running.get() || !panel.isShowing()) return;
            ItemManager.getInstance().tick();
            // paintImmediately runs paint now; repaint() would clear framePending before paint.
            panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
            // On X11/XWayland, flush the back buffer so the compositor actually presents the frame.
            panel.getToolkit().sync();
        } finally {
            framePending.set(false);
        }
    }
}
