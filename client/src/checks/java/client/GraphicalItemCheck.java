package client;

import common.items.Items;
import java.nio.file.*;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/** Optional two-window test using the check-only seeded server, real handlers and pickup task. */
public final class GraphicalItemCheck {
    private static final Path OUTPUT = Path.of("client/build/item-graphical-check");
    private static void await(BooleanSupplier predicate, String name) throws Exception {
        long end = System.nanoTime() + 15_000_000_000L;
        while (!predicate.getAsBoolean()) {
            if (System.nanoTime() > end) throw new AssertionError("Timeout: " + name);
            Thread.sleep(10);
        }
    }
    private static void capture(GraphicalClientCheck.Session session, String name) throws Exception {
        // Capture the live Swing window on the EDT; avoids Wayland's screen-sharing permission dialog.
        java.awt.image.BufferedImage[] image = new java.awt.image.BufferedImage[1];
        SwingUtilities.invokeAndWait(() -> {
            session.gui.panel.paintImmediately(0, 0, session.gui.panel.getWidth(), session.gui.panel.getHeight());
            image[0] = new java.awt.image.BufferedImage(session.gui.frame.getWidth(), session.gui.frame.getHeight(),
                    java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D graphics = image[0].createGraphics();
            try { session.gui.frame.printAll(graphics); } finally { graphics.dispose(); }
        });
        ImageIO.write(image[0], "png", OUTPUT.resolve(name + ".png").toFile());
    }
    private static void pickup(GraphicalClientCheck.Session session, long id) {
        var item = session.field.getEtc(id);
        session.field.pickupItem(item, session.interaction);
        // The panel sends movement alongside the pickup task on a ground-item click.
        session.interaction.addCommand("(move " + item.getX() + " " + item.getY() + ")");
    }
    public static void main(String[] args) throws Exception {
        Files.createDirectories(OUTPUT);
        Process server = new ProcessBuilder("java", "-cp", args[0], "server.javatestserver.ItemNetworkCheck", "fixture")
                .redirectErrorStream(true).redirectOutput(OUTPUT.resolve("server.log").toFile()).start();
        int status = 1;
        try {
            ClientMapFragment.setWidth(1024); ClientMapFragment.setHeight(1024);
            ClientMapFragment.setCellW(32); ClientMapFragment.setCellH(32);
            await(() -> {
                try { return Files.readString(OUTPUT.resolve("server.log")).contains("Isolated item server port: "); }
                catch (Exception ex) { return false; }
            }, "fixture server startup");
            String line = Files.readAllLines(OUTPUT.resolve("server.log")).stream().filter(l -> l.startsWith("Isolated item server port: ")).findFirst().orElseThrow();
            int port = Integer.parseInt(line.split(": ")[1]);
            GraphicalClientCheck.Session first = new GraphicalClientCheck.Session(port, "items-one", 20);
            GraphicalClientCheck.Session second = new GraphicalClientCheck.Session(port, "items-two", 850);
            await(() -> first.field.getEtc(200001) != null && second.field.getEtc(200001) != null, "ground appearance");
            if (!first.field.getEtc(200001).onItem(10, 0)) throw new AssertionError("Gold click anchor");
            capture(first, "01-ground-counts");
            pickup(first, 200001);
            await(() -> first.field.getSelfPlayer().getGoldCount() == 7 && first.field.getEtc(200001) == null
                    && second.field.getEtc(200001) == null, "pickup on both clients");
            if (second.field.getSelfPlayer().getGoldCount() != 0) throw new AssertionError("Observer was credited");
            pickup(first, 200002);
            await(() -> first.field.getSelfPlayer().getGoldCount() == 12 && second.field.getEtc(200002) == null, "merge");
            if (first.field.getSelfPlayer().getInventory().getEtc(Items.GOLD).get(0).getID() != 200001) throw new AssertionError("Canonical stack ID");
            capture(first, "02-merged-inventory");
            pickup(second, 200003);
            await(() -> second.field.getSelfPlayer().getGoldCount() == 3 && first.field.getEtc(200003) == null, "walk and pickup");
            second.interaction.addCommand("(move 0 0)");
            await(() -> second.field.getSelfPlayer().getCurPos().distance(0, 0) < 2, "return to victim");
            long victim = first.field.getSelfPlayer().getID();
            second.interaction.addCommand("(attack " + victim + ")");
            await(() -> first.field.getSelfPlayer().getHitPoints() < 100, "first hit");
            Thread.sleep(800);
            second.interaction.addCommand("(attack " + victim + ")");
            await(() -> first.field.getSelfPlayer().getGoldCount() == 0 && second.field.getEtc(200001) != null, "death inventory removal and drop");
            capture(second, "03-death-drop");
            pickup(second, 200001);
            await(() -> second.field.getSelfPlayer().getGoldCount() == 15 && first.field.getEtc(200001) == null
                    && second.field.getEtc(200001) == null, "repick after death");
            capture(second, "04-repicked-inventory");
            System.out.println("PASS: two real GUI clients, ground counts/click bounds, pickup task, observer cleanup, merge 7+5=12, walk/pick 3, death/drop/repick 3+12=15");
            status = 0;
        } catch (Throwable ex) { ex.printStackTrace(); }
        finally { server.destroy(); server.waitFor(5, java.util.concurrent.TimeUnit.SECONDS); System.exit(status); }
    }
}
