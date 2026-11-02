package wand6.client;

import java.awt.*;
import java.awt.event.*;
import java.lang.instrument.Instrumentation;
import java.nio.file.*;
import javax.swing.*;
import static wand6.client.GraphicalCheckAgent.*;

/** Production clients in separate JVMs; controls, network handlers and live Swing captures. */
public final class CombatGraphicalAgent {
    public static void premain(String argument, Instrumentation instrumentation) {
        Thread check = new Thread(() -> {
            try { run(Path.of(argument), System.getProperty("wand6.checkRole")); }
            catch (Throwable ex) { ex.printStackTrace(); System.exit(2); }
        }, "wand6-combat-gui-check");
        check.setDaemon(true); check.start();
    }
    private static void run(Path output, String role) throws Exception {
        JFrame[] frame = new JFrame[1]; WandJPanel[] panel = new WandJPanel[1];
        var manager = PlayerManager.getInstance();
        await(() -> edt(() -> {
            for (Window w : Window.getWindows()) if (w instanceof JFrame f && w.isShowing() && "wand6".equals(f.getName())) {
                frame[0] = f; panel[0] = findPanel(f);
            }
            return panel[0] != null && manager.selfPlayer() != null && manager.size() == 2 && MapManager.getInstance().isLoaded(0, 0);
        }), "two players and map");
        long other = edt(() -> {
            for (long id = 1; id < 100; id++) if (manager.player(id) != null && id != manager.getSelfPlayerId()) return id;
            throw new AssertionError("missing remote");
        });
        int x = role.equals("one") ? 800 : 512, y = role.equals("one") ? 512 : 700;
        edt(() -> { frame[0].setSize(1000,720); MessageManager.sendMoveRequest(x,y); return true; });
        await(() -> edt(() -> manager.getSelfPlayerX() == x && manager.getSelfPlayerY() == y
                && !manager.player(other).getMovement().isMoving()), "positioned combatants");
        Files.writeString(output.resolve(role + "-ready"), "ready");
        await(() -> Files.exists(output.resolve("one-ready")) && Files.exists(output.resolve("two-ready")), "both ready");
        if (role.equals("one")) {
            selectAttack(panel[0], other);
            await(() -> edt(() -> manager.shotCount() == 1), "server accepted first attack");
            // Reject a burst on the server; controls do not predict damage locally.
            edt(() -> { for (int i=0;i<5;i++) panel[0].keyReleased(key(panel[0],KeyEvent.VK_F3)); return true; });
            await(() -> edt(() -> manager.selfPlayer().getLastAttack() >= 0), "attack timestamp");
            captureFlight(frame[0], panel[0], output.resolve("01-projectile-one.png"));
            await(() -> edt(() -> manager.player(other).getHp() == 50), "remote HP 50");
            edt(() -> { capture(frame[0],panel[0],output.resolve("03-hit-one.png")); return true; });
            await(() -> Files.exists(output.resolve("two-hit")), "victim HP and screenshot");
            selectAttack(panel[0], other);
            await(() -> edt(() -> manager.player(other).getLife() == 1 && manager.corpseCount() > 0), "remote death and respawn");
            edt(() -> {
                require(manager.player(other).getHp() == 100 && manager.player(other).getCurX() == 512 && manager.player(other).getCurY() == 512,"remote restored");
                capture(frame[0],panel[0],output.resolve("05-death-one.png")); return true;
            });
            await(() -> Files.exists(output.resolve("two-respawn")), "victim respawn observed");
            // Other player retaliates; verify independent animation and HP state.
            Files.writeString(output.resolve("retaliate"), "ready");
            await(() -> edt(() -> manager.selfPlayer().getHp() == 50), "own retaliatory HIT");
            await(() -> edt(() -> manager.corpseCount() == 0 && manager.shotCount() == 0), "finished effect cleanup");
            edt(() -> { capture(frame[0],panel[0],output.resolve("07-retaliation-one.png")); return true; });
            Files.writeString(output.resolve("one-done"), "ready");
            await(() -> Files.exists(output.resolve("two-done")), "two done");
        } else {
            captureFlight(frame[0], panel[0], output.resolve("02-projectile-two.png"));
            await(() -> edt(() -> manager.selfPlayer().getHp() == 50), "victim HP 50");
            edt(() -> { capture(frame[0],panel[0],output.resolve("04-hit-two.png")); return true; });
            Files.writeString(output.resolve("two-hit"), "ready");
            await(() -> edt(() -> manager.selfPlayer().getLife() == 1 && manager.corpseCount() > 0), "own death/respawn");
            edt(() -> {
                require(manager.selfPlayer().getHp() == 100 && manager.getSelfPlayerX() == 512 && manager.getSelfPlayerY() == 512,"own restored");
                capture(frame[0],panel[0],output.resolve("06-death-two.png")); return true;
            });
            Files.writeString(output.resolve("two-respawn"), "ready");
            await(() -> Files.exists(output.resolve("retaliate")), "retaliation allowed");
            selectAttack(panel[0], other);
            await(() -> edt(() -> manager.player(other).getHp() == 50), "retaliation on remote");
            await(() -> Files.exists(output.resolve("one-done")), "leader done");
            edt(() -> { capture(frame[0],panel[0],output.resolve("08-retaliation-two.png")); return true; });
            Files.writeString(output.resolve("two-done"), "ready");
        }
        System.out.println("PASS: combat GUI role=" + role + "; selection/F3, attack animation/projectile, HP/hit, death/respawn, retaliation, cleanup, live Swing captures");
        System.out.flush();
        edt(() -> { frame[0].dispatchEvent(new WindowEvent(frame[0],WindowEvent.WINDOW_CLOSING)); return true; });
    }
    private static void captureFlight(JFrame frame, WandJPanel panel, Path path) throws Exception {
        await(() -> edt(() -> {
            long time = wand6.common.ServerTime.getInstance().getTimeSinceStart();
            long start = PlayerManager.getInstance().latestAttackTime();
            if (start < 0 || time < start + 440 || time > start + 590) return false;
            capture(frame,panel,path); return true;
        }), "visible projectile after windup");
    }
    private static void selectAttack(WandJPanel panel, long target) throws Exception {
        edt(() -> {
            var p = PlayerManager.getInstance().player(target);
            panel.mouseClicked(new MouseEvent(panel,MouseEvent.MOUSE_CLICKED,System.currentTimeMillis(),0,
                    p.getCurX()-panel.camera().x(),p.getCurY()-panel.camera().y()-20,1,false,MouseEvent.BUTTON1));
            require(PlayerManager.getInstance().selected() == target,"mouse selects opponent");
            panel.keyReleased(key(panel,KeyEvent.VK_F3)); return true;
        });
    }
    private static KeyEvent key(WandJPanel panel,int code) { return new KeyEvent(panel,KeyEvent.KEY_RELEASED,System.currentTimeMillis(),0,code,KeyEvent.CHAR_UNDEFINED); }
    private static void require(boolean value,String label) { if (!value) throw new AssertionError(label); }
}
