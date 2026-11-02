package wand6.client;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.lang.instrument.Instrumentation;
import java.nio.file.*;
import javax.swing.*;
import static wand6.client.GraphicalCheckAgent.*;

/** Two production client JVMs, real GUI controls and EDT snapshots; no local player fixtures. */
public final class MultiplayerGraphicalAgent {
    public static void premain(String argument, Instrumentation instrumentation) {
        Thread check = new Thread(() -> {
            try { check(Path.of(argument), System.getProperty("wand6.checkRole")); }
            catch (Throwable ex) { ex.printStackTrace(); System.exit(2); }
        }, "wand6-multiplayer-gui-check");
        check.setDaemon(true); check.start();
    }
    private static void check(Path output, String role) throws Exception {
        Files.createDirectories(output);
        JFrame[] frame = new JFrame[1];
        WandJPanel[] panel = new WandJPanel[1];
        var manager = PlayerManager.getInstance();
        await(() -> edt(() -> {
            for (Window w : Window.getWindows()) if (w instanceof JFrame f && w.isShowing() && "wand6".equals(f.getName())) {
                frame[0] = f; panel[0] = findPanel(f);
            }
            return panel[0] != null && manager.selfPlayer() != null
                    && MapManager.getInstance().isLoaded(0, 0) && manager.size() == 2;
        }), role + " initial two players");
        if (role.equals("two")) {
            edt(() -> { frame[0].setSize(1000, 720); return true; });
        }
        long remote = edt(() -> findRemote(manager));
        if (role.equals("rejoin")) {
            await(() -> edt(() -> "Привет, второй!".equals(manager.player(remote).getText())), "initial text snapshot on rejoin");
            sendText(frame[0], "Снова в игре");
            await(() -> edt(() -> "Снова в игре".equals(manager.selfPlayer().getText())), "own text");
            edt(() -> { capture(frame[0], panel[0], output.resolve("06-rejoined-two.png")); return true; });
            await(() -> Files.exists(output.resolve("leader-done")), "leader observed rejoin");
        } else {
            int x = role.equals("one") ? 800 : 512;
            int y = role.equals("one") ? 512 : 700;
            String text = role.equals("one") ? "Привет, второй!" : "Привет, первый!";
            edt(() -> { MessageManager.sendMoveRequest(x, y); return true; });
            sendText(frame[0], text);
            await(() -> edt(() -> manager.selfPlayer().getMovement().isMoving()
                    && manager.player(remote).getMovement().isMoving()
                    && Math.hypot(manager.getSelfPlayerX() - manager.player(remote).getCurX(),
                            manager.getSelfPlayerY() - manager.player(remote).getCurY()) > 100), "simultaneous independent animation");
            edt(() -> { capture(frame[0], panel[0], output.resolve(role.equals("one") ? "01-both-moving-one.png" : "02-both-moving-two.png")); return true; });
            await(() -> edt(() -> manager.getSelfPlayerX() == x && manager.getSelfPlayerY() == y
                    && !manager.player(remote).getMovement().isMoving()
                    && (role.equals("one") ? "Привет, первый!" : "Привет, второй!").equals(manager.player(remote).getText())), "positions and Cyrillic text on both clients");
            edt(() -> { capture(frame[0], panel[0], output.resolve(role.equals("one") ? "03-text-one.png" : "04-text-two.png")); return true; });
            if (role.equals("one")) {
                edt(() -> { MessageManager.sendMoveRequest(1120, 512); return true; });
                await(() -> edt(() -> manager.getSelfPlayerX() == 1120 && manager.size() == 1), "GUI exit from visibility");
                await(() -> Files.exists(output.resolve("boundary-hidden")), "other client saw departure");
                edt(() -> { MessageManager.sendMoveRequest(800, 512); return true; });
                await(() -> edt(() -> manager.size() == 2 && manager.getSelfPlayerX() == 800), "GUI return across visibility");
                await(() -> Files.exists(output.resolve("boundary-returned")), "other client saw moving appearance");
                Files.writeString(output.resolve("disconnect-two"), "ready");
                await(() -> edt(() -> manager.size() == 1 && manager.player(remote) == null), "remote removed after disconnect");
                edt(() -> { capture(frame[0], panel[0], output.resolve("05-survivor.png")); return true; });
                Files.writeString(output.resolve("survivor-ready"), "ready");
                await(() -> edt(() -> manager.size() == 2 && manager.player(remote) == null), "new identity on reconnect");
                long rejoined = edt(() -> findRemote(manager));
                await(() -> edt(() -> manager.player(rejoined) != null && "Снова в игре".equals(manager.player(rejoined).getText())), "rejoined text delivered");
                edt(() -> { capture(frame[0], panel[0], output.resolve("07-rejoined-one.png")); return true; });
                Files.writeString(output.resolve("leader-done"), "ready");
            } else {
                await(() -> edt(() -> manager.size() == 1), "observer sees boundary departure");
                edt(() -> { capture(frame[0], panel[0], output.resolve("08-out-of-range.png")); return true; });
                Files.writeString(output.resolve("boundary-hidden"), "ready");
                await(() -> edt(() -> manager.player(remote) != null && manager.player(remote).getMovement().isMoving()
                        && manager.player(remote).getCurX() < 930), "observer sees moving reappearance inside viewport");
                edt(() -> {
                    if (!"Привет, второй!".equals(manager.player(remote).getText())) throw new AssertionError("Missing text on reappearance");
                    capture(frame[0], panel[0], output.resolve("09-moving-reappearance.png")); return true;
                });
                Files.writeString(output.resolve("boundary-returned"), "ready");
                await(() -> Files.exists(output.resolve("disconnect-two")), "leader ready for disconnect");
            }
        }
        System.out.println("PASS: multiplayer GUI role=" + role + "; actual player state, independent movement/animation, Cyrillic text, live Swing PNGs, disconnect/rejoin.");
        System.out.flush();
        edt(() -> { frame[0].dispatchEvent(new WindowEvent(frame[0], WindowEvent.WINDOW_CLOSING)); return true; });
    }
    private static long findRemote(PlayerManager manager) {
        // Identity is discovered from the production appearance handler; IDs need not be sequential.
        for (long id = 1; id < 1000; id++) if (manager.player(id) != null && id != manager.getSelfPlayerId()) return id;
        throw new AssertionError("No remote player");
    }
    private static void sendText(JFrame frame, String text) throws Exception {
        edt(() -> {
            JTextField input = (JTextField) named(frame, "chat-input");
            input.setText(text);
            ((JButton) named(frame, "chat-send")).doClick();
            return true;
        });
    }
    private static Component named(Container parent, String name) {
        for (Component c : parent.getComponents()) {
            if (name.equals(c.getName())) return c;
            if (c instanceof Container nested) { Component found = named(nested, name); if (found != null) return found; }
        }
        return null;
    }
}
