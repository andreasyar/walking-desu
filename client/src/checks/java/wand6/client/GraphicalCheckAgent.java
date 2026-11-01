package wand6.client;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Check-only agent: observes the real application launched by its distribution script. */
public final class GraphicalCheckAgent {
    public static void premain(String argument, Instrumentation instrumentation) {
        Thread check = new Thread(() -> {
            try { check(Path.of(argument)); }
            catch (Throwable ex) { ex.printStackTrace(); System.exit(2); }
        }, "wand6-gui-check");
        check.setDaemon(true);
        check.start();
    }

    private static void check(Path output) throws Exception {
        Files.createDirectories(output);
        JFrame[] frame = new JFrame[1];
        WandJPanel[] panel = new WandJPanel[1];
        await(() -> edt(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof JFrame candidate && "wand6".equals(candidate.getName()) && window.isShowing()) {
                    frame[0] = candidate;
                    panel[0] = findPanel(candidate);
                }
            }
            return panel[0] != null && MapManager.getInstance().isLoaded(0, 0)
                    && PlayerManager.getInstance().getSelfPlayerId() > 0;
        }), "WELCOME and rendered initial map");
        edt(() -> {
            if (!frame[0].getTitle().contains("4.2.0-dev")) {
                throw new AssertionError("Distribution version absent from window title: " + frame[0].getTitle());
            }
            capture(frame[0], panel[0], output.resolve("01-login-map.png"));
            return true;
        });
        // Network request through the actual client's message queue; Dummy movement remains intentional.
        edt(() -> { MessageManager.sendMoveRequest(512, 512); return true; });
        await(() -> edt(() -> PlayerManager.getInstance().getSelfPlayerX() == 512
                && PlayerManager.getInstance().getSelfPlayerY() == 512), "Dummy MOVE response");
        edt(() -> { frame[0].setSize(1000, 720); return true; });
        await(() -> edt(() -> panel[0].getWidth() > 900 && MapManager.getInstance().isLoaded(0, 0)), "resize");
        edt(() -> { capture(frame[0], panel[0], output.resolve("02-map-resize.png")); return true; });
        System.out.println("PASS: distribution GUI; WELCOME; 32x32 map; Dummy MOVE; resize; live-window PNGs.");
        System.out.flush();
        edt(() -> { frame[0].dispatchEvent(new WindowEvent(frame[0], WindowEvent.WINDOW_CLOSING)); return true; });
    }

    private static WandJPanel findPanel(Container parent) {
        for (Component child : parent.getComponents()) {
            if (child instanceof WandJPanel panel) { return panel; }
            if (child instanceof Container nested) {
                WandJPanel panel = findPanel(nested);
                if (panel != null) { return panel; }
            }
        }
        return null;
    }

    private static void capture(JFrame frame, WandJPanel panel, Path path) throws Exception {
        panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
        BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try { frame.printAll(graphics); } finally { graphics.dispose(); }
        if (!ImageIO.write(image, "png", path.toFile())) { throw new AssertionError("No PNG encoder"); }
    }

    private static <T> T edt(Callable<T> task) throws Exception {
        FutureTask<T> future = new FutureTask<>(task);
        SwingUtilities.invokeAndWait(future);
        return future.get();
    }
    private static void await(Callable<Boolean> condition, String message) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (System.nanoTime() < deadline) {
            if (condition.call()) { return; }
            Thread.sleep(40);
        }
        throw new AssertionError("Timeout awaiting " + message);
    }
}
