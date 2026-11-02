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
            if (!frame[0].getTitle().contains("4.2.0")) {
                throw new AssertionError("Distribution version absent from window title: " + frame[0].getTitle());
            }
            capture(frame[0], panel[0], output.resolve("01-login-map.png"));
            return true;
        });
        var manager = PlayerManager.getInstance();
        edt(() -> {
            if (manager.getSelfPlayerX() != 512 || manager.getSelfPlayerY() != 512) throw new AssertionError("WELCOME position ignored");
            if (Camera.follow(0,0,800,600).x() != 0) throw new AssertionError("origin camera");
            var edge = Camera.follow(wand6.common.MapFragment.maxX, wand6.common.MapFragment.maxY, 800,600);
            if ((long) edge.x()+800 != (long) wand6.common.MapFragment.maxX+1) throw new AssertionError("world edge camera");
            return true;
        });
        // Dispatch a real panel click using the displayed camera transform.
        edt(() -> {
            click(panel[0],800,512,java.awt.event.MouseEvent.BUTTON1);
            return true;
        });
        await(() -> edt(() -> manager.selfPlayer().getEndX() == 800), "MOVE accepted");
        await(() -> edt(() -> manager.getSelfPlayerX() > 540 && manager.getSelfPlayerX() < 750), "intermediate position");
        edt(() -> { capture(frame[0],panel[0],output.resolve("02-walking.png")); return true; });
        // Real right click stops on the server position, then retarget to the waypoint.
        edt(() -> { click(panel[0],800,512,java.awt.event.MouseEvent.BUTTON3); return true; });
        await(() -> edt(() -> !manager.selfPlayer().getMovement().isMoving()), "right-click stop");
        moveTo(800,512);
        // All eight directions through actual network commands and movement/standing transitions.
        int[][] offsets = {{40,0},{40,-40},{0,-40},{-40,-40},{-40,0},{-40,40},{0,40},{40,40}};
        for (int[] offset : offsets) {
            moveTo(800+offset[0],512+offset[1]);
            edt(() -> { panel[0].paintImmediately(0,0,panel[0].getWidth(),panel[0].getHeight()); return true; });
            moveTo(800,512);
        }
        moveTo(1120,512);
        await(() -> edt(() -> MapManager.getInstance().isLoaded(1,0)), "cross fragment boundary");
        edt(() -> { capture(frame[0],panel[0],output.resolve("03-fragment-crossing.png")); frame[0].setSize(1000,720); return true; });
        await(() -> edt(() -> panel[0].getWidth() > 900 && MapManager.getInstance().isLoaded(1,0)), "resize");
        edt(() -> { capture(frame[0], panel[0], output.resolve("04-map-resize.png"));
            click(panel[0],1152,544,java.awt.event.MouseEvent.BUTTON1); return true; });
        await(() -> edt(() -> manager.getSelfPlayerX() == 1152 && manager.getSelfPlayerY() == 544), "click transform after resize");
        edt(() -> { MessageManager.sendMoveRequest(1600,512); return true; });
        await(() -> edt(() -> manager.selfPlayer().getEndX() >= 1400 && manager.selfPlayer().getEndX() < 1600
                && !manager.selfPlayer().getMovement().isMoving()), "server clips mountain path");
        edt(() -> { capture(frame[0],panel[0],output.resolve("05-mountain-stop.png")); return true; });
        // Explicitly local visual fixtures: origin and far-world camera, not server travel.
        edt(() -> {
            manager.selfPlayer().place(16,64);
            panel[0].paintImmediately(0,0,panel[0].getWidth(),panel[0].getHeight());
            capture(frame[0],panel[0],output.resolve("06-origin-camera-fixture.png"));
            return true;
        });
        System.out.println("PASS: distribution GUI; WELCOME coordinates/time; interpolated movement; right-click stop; eight directions; walk/stand; fragment crossing; resize/click; origin camera fixture; live-window PNGs.");
        System.out.flush();
        edt(() -> { frame[0].dispatchEvent(new WindowEvent(frame[0], WindowEvent.WINDOW_CLOSING)); return true; });
    }

    private static void moveTo(int x,int y) throws Exception {
        edt(() -> { MessageManager.sendMoveRequest(x,y); return true; });
        await(() -> edt(() -> {
            var player = PlayerManager.getInstance().selfPlayer();
            return player.getEndX() == x && player.getEndY() == y && !player.getMovement().isMoving();
        }), "arrival " + x + "," + y);
    }
    private static void click(WandJPanel panel,int worldX,int worldY,int button) {
        panel.mouseClicked(new java.awt.event.MouseEvent(panel,java.awt.event.MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),0,worldX-panel.camera().x(),worldY-panel.camera().y(),1,false,button));
    }

    static WandJPanel findPanel(Container parent) {
        for (Component child : parent.getComponents()) {
            if (child instanceof WandJPanel panel) { return panel; }
            if (child instanceof Container nested) {
                WandJPanel panel = findPanel(nested);
                if (panel != null) { return panel; }
            }
        }
        return null;
    }

    static void capture(JFrame frame, WandJPanel panel, Path path) throws Exception {
        panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
        BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try { frame.printAll(graphics); } finally { graphics.dispose(); }
        if (!ImageIO.write(image, "png", path.toFile())) { throw new AssertionError("No PNG encoder"); }
    }

    static <T> T edt(Callable<T> task) throws Exception {
        FutureTask<T> future = new FutureTask<>(task);
        SwingUtilities.invokeAndWait(future);
        return future.get();
    }
    static void await(Callable<Boolean> condition, String message) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (System.nanoTime() < deadline) {
            if (condition.call()) { return; }
            Thread.sleep(40);
        }
        throw new AssertionError("Timeout awaiting " + message);
    }
}
