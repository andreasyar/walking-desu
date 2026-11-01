package client;

import client.items.ClientEtc;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/** Real windows and a production server: natural first wave, tower projectiles and gold. */
public final class GraphicalCombatCheck {
    private static final Path OUTPUT = Path.of("client/build/combat-graphical-check");
    private static void await(BooleanSupplier condition, String name) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(35);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) throw new AssertionError("Timeout: " + name);
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
    public static void main(String[] args) throws Exception {
        Files.createDirectories(OUTPUT);
        Process server = new ProcessBuilder("java", "-jar", args[0], "0").redirectErrorStream(true)
                .redirectOutput(OUTPUT.resolve("server.log").toFile()).start();
        int status = 1;
        try {
            ClientMapFragment.setWidth(1024); ClientMapFragment.setHeight(1024);
            ClientMapFragment.setCellW(32); ClientMapFragment.setCellH(32);
            await(() -> {
                try { return Files.readString(OUTPUT.resolve("server.log")).contains("Server starts at port: "); }
                catch (Exception ex) { return false; }
            }, "server start");
            int port = Integer.parseInt(Files.readAllLines(OUTPUT.resolve("server.log")).stream()
                    .filter(s -> s.startsWith("Server starts at port: ")).findFirst().orElseThrow().substring(23));
            System.out.println("Graphical combat isolated server port: " + port);
            var first = new GraphicalClientCheck.Session(port, "stage5-one", 20);
            var second = new GraphicalClientCheck.Session(port, "stage5-two", 850);
            // Programmatic movement must request the neighbour that a normal mouse click preloads.
            first.interaction.addCommand("(getmapfragm 1 0)"); second.interaction.addCommand("(getmapfragm 1 0)");
            await(() -> first.field.getMapFragment(1, 0) != null && second.field.getMapFragment(1, 0) != null, "neighbour map loaded");
            first.interaction.addCommand("(move 700 100)"); second.interaction.addCommand("(move 730 100)");
            await(() -> first.field.getSelfPlayer().getCurPos().distance(700, 100) < 2
                    && second.field.getSelfPlayer().getCurPos().distance(730, 100) < 2, "move to tower defence lane");
            first.interaction.addCommand("(tower 700 20)");
            await(() -> first.field.getTowers().size() == 1 && second.field.getTowers().size() == 1, "both see constructed tower");
            capture(first, "01-built-tower");
            await(() -> first.field.getMonsters().stream().anyMatch(m -> m.getCurPos().x >= 500 && m.getHitPoints() == 10)
                    && second.field.getMonsters().stream().anyMatch(m -> m.getCurPos().x >= 500 && m.getHitPoints() == 10), "natural wave and tower HIT on both clients");
            capture(second, "02-wave-tower-hit");
            await(() -> first.field.getDrawQueue().stream().anyMatch(d -> d instanceof ClientEtc gold && gold.getX() >= 500)
                    && second.field.getDrawQueue().stream().anyMatch(d -> d instanceof ClientEtc gold && gold.getX() >= 500), "monster death drops gold on both clients");
            capture(second, "03-monster-death-gold");
            System.out.println("PASS: two GUI clients with valid automatic test nicknames, map/movement, tower construction/visibility, natural wave, real tower HIT/death/gold on both clients");
            status = 0;
        } catch (Throwable failure) { failure.printStackTrace(); }
        finally { server.destroy(); if (!server.waitFor(5, TimeUnit.SECONDS)) server.destroyForcibly(); System.exit(status); }
    }
}
