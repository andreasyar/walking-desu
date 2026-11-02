package wand6.client;

import java.awt.*;
import java.awt.event.*;
import java.lang.instrument.Instrumentation;
import java.nio.file.*;
import javax.swing.*;
import wand6.common.Unit;
import static wand6.client.GraphicalCheckAgent.*;

/** Production controls, natural waves and real windows in two independent client JVMs. */
public final class TowerDefenceGraphicalAgent {
    public static void premain(String argument, Instrumentation instrumentation) {
        Thread check = new Thread(() -> {
            try { run(Path.of(argument), System.getProperty("wand6.checkRole")); }
            catch (Throwable ex) { ex.printStackTrace(); System.exit(2); }
        }, "wand6-tower-defence-gui");
        check.setDaemon(true); check.start();
    }
    private static void run(Path output, String role) throws Exception {
        JFrame[] frame = new JFrame[1]; WandJPanel[] panel = new WandJPanel[1];
        var manager = PlayerManager.getInstance();
        await(() -> edt(() -> {
            for (Window w : Window.getWindows()) if (w instanceof JFrame f && w.isShowing() && "wand6".equals(f.getName())) {
                frame[0]=f; panel[0]=findPanel(f);
            }
            return panel[0] != null && manager.selfPlayer() != null && manager.size() == 2 && MapManager.getInstance().isLoaded(0,0);
        }), "two players and map");
        edt(() -> { frame[0].setSize(1000,900); MessageManager.sendMoveRequest(role.equals("one") ? 450 : 550,512); return true; });
        await(() -> edt(() -> !manager.selfPlayer().getMovement().isMoving()), "positioned observer");
        if (role.equals("one")) edt(() -> {
            panel[0].keyReleased(new KeyEvent(panel[0],KeyEvent.KEY_RELEASED,System.currentTimeMillis(),0,KeyEvent.VK_B,'B'));
            panel[0].paintImmediately(0,0,panel[0].getWidth(),panel[0].getHeight());
            panel[0].mouseClicked(new MouseEvent(panel[0],MouseEvent.MOUSE_CLICKED,System.currentTimeMillis(),0,
                    700-panel[0].camera().x(),400-panel[0].camera().y(),1,false,MouseEvent.BUTTON1));
            return true;
        });
        await(() -> edt(() -> manager.count(Unit.Kind.TOWER) == 1), "server tower appearance");
        snap(frame[0],panel[0],output.resolve("01-tower-"+role+".png"));
        // Natural production timer: first wave at 20 seconds, second at 40.
        waitLong(() -> edt(() -> manager.units().stream().anyMatch(u -> u.getKind() == Unit.Kind.MONSTER && u.getCurY() > panel[0].camera().y()+40) && manager.defenceStatus().wave() == 1), "natural first wave");
        snap(frame[0],panel[0],output.resolve("02-wave-"+role+".png"));
        await(() -> edt(() -> {
            long now = wand6.common.ServerTime.getInstance().getTimeSinceStart();
            long attack = manager.latestAttackTime();
            if (manager.shotCount() == 0 || now < attack) return false;
            capture(frame[0],panel[0],output.resolve("03-projectile-"+role+".png")); return true;
        }), "tower projectile");
        await(() -> edt(() -> manager.corpseCount() > 0), "monster death animation");
        snap(frame[0],panel[0],output.resolve("04-death-"+role+".png"));
        waitLong(() -> edt(() -> manager.defenceStatus().wave() >= 2), "natural second wave and status");
        require(edt(() -> manager.defenceStatus().losses() > 0),"unintercepted monsters counted");
        snap(frame[0],panel[0],output.resolve("05-next-wave-"+role+".png"));
        if (role.equals("one")) edt(() -> {
            var tower = manager.units().stream().filter(u -> u.getKind() == Unit.Kind.TOWER).findFirst().orElseThrow();
            panel[0].mouseClicked(new MouseEvent(panel[0],MouseEvent.MOUSE_CLICKED,System.currentTimeMillis(),0,
                    tower.getCurX()-panel[0].camera().x(),tower.getCurY()-panel[0].camera().y()-20,1,false,MouseEvent.BUTTON1));
            require(manager.selected() == tower.getId(),"select tower for demolition");
            panel[0].keyReleased(new KeyEvent(panel[0],KeyEvent.KEY_RELEASED,System.currentTimeMillis(),0,KeyEvent.VK_F3,KeyEvent.CHAR_UNDEFINED));
            return true;
        });
        await(() -> edt(() -> manager.count(Unit.Kind.TOWER) == 0 && manager.shotCount() == 0),"destroyed tower removed and flights cleared");
        snap(frame[0],panel[0],output.resolve("06-demolished-"+role+".png"));
        Files.writeString(output.resolve(role+"-demolished"),"ready");
        await(() -> Files.exists(output.resolve("one-demolished")) && Files.exists(output.resolve("two-demolished")),"both saw demolition");
        if (role.equals("one")) edt(() -> {
            panel[0].keyReleased(new KeyEvent(panel[0],KeyEvent.KEY_RELEASED,System.currentTimeMillis(),0,KeyEvent.VK_B,'B'));
            panel[0].mouseClicked(new MouseEvent(panel[0],MouseEvent.MOUSE_CLICKED,System.currentTimeMillis(),0,
                    700-panel[0].camera().x(),400-panel[0].camera().y(),1,false,MouseEvent.BUTTON1));
            return true;
        });
        await(() -> edt(() -> manager.count(Unit.Kind.TOWER) == 1),"rebuild after demolition");
        snap(frame[0],panel[0],output.resolve("07-rebuilt-"+role+".png"));
        Files.writeString(output.resolve(role+"-done"),"ready");
        await(() -> Files.exists(output.resolve("one-done")) && Files.exists(output.resolve("two-done")),"both clients done");
        System.out.println("PASS: tower defence GUI role="+role+"; B/click building, natural waves, moving poring, cannon flight/HIT/death, loss status and next wave, tower demolition/rebuild, live Swing captures");
        System.out.flush();
        edt(() -> { frame[0].dispatchEvent(new WindowEvent(frame[0],WindowEvent.WINDOW_CLOSING)); return true; });
    }
    private static void snap(JFrame frame,WandJPanel panel,Path path) throws Exception {
        edt(() -> { capture(frame,panel,path); return true; });
    }
    private static void waitLong(java.util.concurrent.Callable<Boolean> condition,String label) throws Exception {
        long end=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
        while(System.nanoTime()<end) { if(condition.call()) return; Thread.sleep(30); }
        throw new AssertionError("Timeout: "+label);
    }
    private static void require(boolean value,String label) { if(!value)throw new AssertionError(label); }
}
