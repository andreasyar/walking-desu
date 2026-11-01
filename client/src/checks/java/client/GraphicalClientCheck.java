package client;

import client.items.ClientEtc;
import common.items.Items;
import java.awt.Dimension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Optional desktop integration check. Needs DISPLAY, GUI Java and an isolated server JAR. */
public final class GraphicalClientCheck {
    private static final Path OUTPUT = Path.of("client/build/graphical-check");
    static final class Session {
        final GameField field = new GameField();
        final ServerInteraction interaction;
        final WanderingGUI gui;
        Session(int port, String nick, int x) throws Exception {
            field.startSelfExecution();
            field.addMapFragment(new ClientMapFragment(0, 0, new int[][]{}));
            interaction = new ServerInteraction(field, Executors.newCachedThreadPool(), "localhost", port);
            common.PlayerName.requireValid(nick);
            System.setProperty("walkingdesu.testMode", "true");
            System.setProperty("walkingdesu.testNick", nick);
            try { gui = new WanderingGUI(field, interaction); }
            finally { System.clearProperty("walkingdesu.testMode"); System.clearProperty("walkingdesu.testNick"); }
            SwingUtilities.invokeAndWait(() -> {
                gui.run(); // Explicit test mode uses the same validated name without a modal dialog.
                gui.frame.setLocation(x, 40);
                new Timer(30, event -> gui.panel.repaint()).start();
            });
            interaction.run(gui.dialog.getValidatedText());
            await(() -> field.getSelfPlayer() != null, "login");
            SwingUtilities.invokeAndWait(gui::showPanel);
        }
    }

    private static void await(BooleanSupplier predicate, String scenario) throws Exception {
        long deadline = System.nanoTime() + 10_000_000_000L;
        while (!predicate.getAsBoolean()) {
            if (System.nanoTime() > deadline) throw new AssertionError("Timeout: " + scenario);
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

    private static Drawable fixedFrame(Sprite sprite) {
        return new Drawable() {
            public Sprite getSprite() { return sprite; }
            public void draw(java.awt.Graphics graphics, int x, int y, Dimension size) {
                graphics.drawImage(sprite.image, sprite.x - x, sprite.y - y, null);
            }
        };
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(OUTPUT);
        Process server = new ProcessBuilder("java", "-jar", args[0], "0")
                .redirectErrorStream(true).redirectOutput(OUTPUT.resolve("server.log").toFile()).start();
        int status = 1;
        try {
            ClientMapFragment.setWidth(1024); ClientMapFragment.setHeight(1024);
            ClientMapFragment.setCellW(32); ClientMapFragment.setCellH(32);
            await(() -> {
                try { return Files.readString(OUTPUT.resolve("server.log")).contains("Server starts at port: "); }
                catch (Exception ex) { return false; }
            }, "server startup");
            String line = Files.readAllLines(OUTPUT.resolve("server.log")).stream()
                    .filter(value -> value.startsWith("Server starts at port: ")).findFirst().orElseThrow();
            int port = Integer.parseInt(line.substring(23));
            System.out.println("Graphical isolated server port: " + port);
            Session first = new Session(port, "stage3-one", 20);
            Session second = new Session(port, "stage3-two", 850);
            await(() -> first.field.getPlayers().size() == 2 && second.field.getPlayers().size() == 2, "two-client visibility");
            first.interaction.addCommand("(move 100 100)");
            second.interaction.addCommand("(move 130 100)");
            await(() -> first.field.getSelfPlayer().getCurPos().distance(100, 100) < 2
                    && second.field.getSelfPlayer().getCurPos().distance(130, 100) < 2, "movement and stop");
            SwingUtilities.invokeAndWait(() -> {
                first.gui.msgField.setText("Stage 3: text and sprites");
                first.gui.sendBtn.doClick();
            });
            long victimId = second.field.getSelfPlayer().getID();
            await(() -> "Stage 3: text and sprites".equals(second.field.getPlayer(first.field.getSelfPlayer().getID()).getText()), "text delivery");
            capture(first, "01-movement-text");
            first.interaction.addCommand("(attack " + victimId + ")");
            await(() -> second.field.getSelfPlayer().getHitPoints() < 100, "first hit");
            Thread.sleep(800);
            first.interaction.addCommand("(attack " + victimId + ")");
            await(() -> second.field.getSelfPlayer().dead(), "PvP death");
            capture(second, "02-death");
            await(() -> !second.field.getSelfPlayer().dead(), "respawn");
            System.out.println("PASS: two actual GUI clients, login, map, movement/stop, text, PvP hit/death/respawn");

            // Explicit local visual fixtures supplement the network scenario for every supported set.
            Player self = first.field.getSelfPlayer();
            Player peon = new Player(-10, "peon", 100, .07, 170, 150, Direction.WEST, "peon");
            Player desu = new Player(-11, "desu", 100, .07, 250, 150, Direction.EAST, "desu");
            Monster poring = new Monster(-12, "poring", 20, .08, 320, 150, Direction.SOUTH, "poring");
            Tower tower = new Tower(-13, "tower", 100, 10, 1000, 390, 150, Direction.EAST, "tower");
            first.field.asyncAddPlayer(peon); first.field.asyncAddPlayer(desu);
            first.field.asyncAddMonster(poring); first.field.asyncAddTower(tower);
            ClientEtc gold = new ClientEtc(-14, "Gold", Items.GOLD, 1);
            gold.setX(140); gold.setY(180); first.field.asyncAddEtc(gold);
            await(() -> first.field.getDrawQueue().contains(gold), "visual fixtures");
            peon.selectUnit(self); peon.attack(WUnit.now());
            first.field.asyncAddHitAnimation(new CanonHitAnimation("canon", new java.awt.Point(320, 150), Direction.EAST, WUnit.now()));
            tower.selectUnit(poring); tower.setCurrentNuke(new CanonNuke(tower));
            first.field.asyncAddNuke(tower, poring, WUnit.now());
            // Hold representative effect frames for visual inspection; live timing is checked above/headlessly.
            Sprite boltFrame = new CanonNukeAnimation("canon_bolt").getSprite(390, 150, 320, 150, 355, 150);
            CanonHitAnimation previewHit = new CanonHitAnimation("canon", new java.awt.Point(320, 150), Direction.EAST, WUnit.now() + 60000);
            first.field.addDrawable(fixedFrame(boltFrame));
            first.field.addDrawable(previewHit);
            capture(first, "03-all-types-effects-gold");
            if (!gold.onItem(gold.getSprite().x, gold.getSprite().y)) throw new AssertionError("Gold click bounds");
            SwingUtilities.invokeAndWait(() -> first.gui.frame.setSize(new Dimension(540, 420)));
            Thread.sleep(150); capture(first, "04-resize-clipping");
            poring.kill(); Thread.sleep(200); capture(first, "05-monster-death");
            first.field.asyncDelPlayer(peon.getID()); first.field.asyncDelPlayer(desu.getID());
            first.field.asyncDelTower(tower.getID());
            await(() -> !first.field.getDrawQueue().contains(tower), "queued removal");
            System.out.println("PASS: local fixtures for all sprite sets, peon attack, tower projectile, hit, gold bounds, resize and removal");
            status = 0;
        } catch (Throwable failure) {
            failure.printStackTrace();
        } finally {
            server.destroy();
            server.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            // Historical client reader/writer threads have no graceful shutdown contract.
            System.exit(status);
        }
    }
}
