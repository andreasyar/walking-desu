package wand6.client;

import java.awt.*;
import java.awt.event.*;
import java.lang.instrument.Instrumentation;
import java.nio.file.*;
import javax.swing.*;
import wand6.common.*;
import static wand6.client.GraphicalCheckAgent.*;

/** Check-only fixtures seed gold/NPC; all interactions use production controls and network. */
public final class ItemGraphicalAgent {
    public static void premain(String argument, Instrumentation instrumentation) {
        Thread check = new Thread(() -> {
            try { run(Path.of(argument),System.getProperty("wand6.checkRole")); }
            catch(Throwable ex) { ex.printStackTrace(); System.exit(2); }
        },"wand6-item-gui");
        check.setDaemon(true); check.start();
    }
    private static void run(Path output,String role) throws Exception {
        JFrame[] frame=new JFrame[1]; WandJPanel[] panel=new WandJPanel[1];
        var players=PlayerManager.getInstance(); var items=ItemManager.getInstance();
        await(() -> edt(() -> {
            for(Window w:Window.getWindows()) if(w instanceof JFrame f && w.isShowing() && "wand6".equals(f.getName())) {
                frame[0]=f; panel[0]=findPanel(f);
            }
            return panel[0]!=null && players.selfPlayer()!=null && players.size()==2 && items.snapshot().size()==2 && MapManager.getInstance().isLoaded(0,0);
        }),"two production clients/map/seeded gold");
        long other=edt(() -> players.units().stream().filter(u -> u.getKind()==Unit.Kind.PLAYER && u!=players.selfPlayer()).findFirst().orElseThrow().getId());
        edt(() -> { frame[0].setSize(1000,720); return true; });
        snap(frame[0],panel[0],output,"01-ground-"+role);
        signal(output,role+"-initial"); waitFile(output,"one-initial"); waitFile(output,"two-initial");
        if(role.equals("one")) {
            pick(panel[0],7); awaitCount(7);
            pick(panel[0],5); awaitCount(12);
            edt(() -> { frame[0].setSize(640,500); return true; });
            snap(frame[0],panel[0],output,"02-merge-resize-one");
            edt(() -> { frame[0].setSize(1000,720); return true; });
            move(880,512);
            signal(output,"merged");
            await(() -> edt(() -> players.selfPlayer().getLife()==1 && items.inventory().count(ItemStack.Type.GOLD)==0),"own death and gold removed");
            await(() -> edt(() -> items.snapshot().stream().anyMatch(i -> i.stack().count()==12)),"own dropped gold");
            snap(frame[0],panel[0],output,"03-death-drop-one"); signal(output,"one-death");
            waitFile(output,"repicked");
            await(() -> edt(() -> items.snapshot().isEmpty()),"remote repick clears ground");
            require(edt(() -> items.inventory().snapshot().isEmpty()),"remote pickup does not credit own inventory");
            snap(frame[0],panel[0],output,"04-remote-pick-one"); signal(output,"one-repicked");
            await(() -> edt(() -> items.snapshot().stream().anyMatch(i -> i.stack().count()>=1 && i.stack().count()<=99)),"tower kill reward visible");
            snap(frame[0],panel[0],output,"05-monster-drop-one"); signal(output,"one-reward");
            waitFile(output,"reward-picked");
            await(() -> edt(() -> items.snapshot().isEmpty()),"reward disappears remotely");
            snap(frame[0],panel[0],output,"06-reward-picked-one"); signal(output,"one-reward-picked");
            await(() -> edt(() -> players.size()==1 && items.snapshot().size()==1),"disconnect returns remote gold");
            snap(frame[0],panel[0],output,"07-disconnect-drop-one");
            int total=edt(() -> items.snapshot().get(0).stack().count());
            require(total>=13 && total<=111,"12 plus one monster reward");
            pick(panel[0],total); awaitCount(total);
            await(() -> edt(() -> items.snapshot().isEmpty()),"final pickup removes gold");
            snap(frame[0],panel[0],output,"08-final-inventory-one");
        } else {
            waitFile(output,"merged");
            require(edt(() -> items.inventory().snapshot().isEmpty() && items.snapshot().isEmpty()),"observer never credited");
            snap(frame[0],panel[0],output,"02-merge-observer-two");
            move(512,700);
            attack(panel[0],other);
            await(() -> edt(() -> players.player(other).getHp()==50),"first real PvP hit");
            await(() -> edt(() -> ServerTime.getInstance().getTimeSinceStart()-players.selfPlayer().getLastAttack()>=CombatRules.REUSE),"attack reuse");
            attack(panel[0],other);
            await(() -> edt(() -> players.player(other).getLife()==1 && items.snapshot().stream().anyMatch(i -> i.stack().count()==12)),"remote death/drop");
            snap(frame[0],panel[0],output,"03-death-drop-two"); waitFile(output,"one-death");
            pick(panel[0],12); awaitCount(12);
            snap(frame[0],panel[0],output,"04-repicked-two"); signal(output,"repicked"); waitFile(output,"one-repicked");
            edt(() -> {
                panel[0].keyReleased(key(panel[0],KeyEvent.VK_B)); click(panel[0],700,400); return true;
            });
            await(() -> edt(() -> items.snapshot().size()==1),"tower kills seeded NPC and drops reward");
            int reward=edt(() -> items.snapshot().get(0).stack().count());
            snap(frame[0],panel[0],output,"05-monster-drop-two"); waitFile(output,"one-reward");
            pick(panel[0],reward); awaitCount(12+reward);
            snap(frame[0],panel[0],output,"06-reward-inventory-two"); signal(output,"reward-picked"); waitFile(output,"one-reward-picked");
        }
        System.out.println("PASS: items GUI role="+role+"; actual controls, approach/pickup/merge, owner-only inventory, resize, PvP death/drop/repick, tower kill reward, disconnect/drop/final pickup; live Swing captures");
        System.out.flush();
        edt(() -> { frame[0].dispatchEvent(new WindowEvent(frame[0],WindowEvent.WINDOW_CLOSING)); return true; });
    }
    private static void pick(WandJPanel panel,int count) throws Exception {
        edt(() -> {
            GroundItem item=ItemManager.getInstance().snapshot().stream().filter(i -> i.stack().count()==count).findFirst().orElseThrow();
            click(panel,item.x(),item.y()); return true;
        });
    }
    private static void click(WandJPanel panel,int x,int y) {
        panel.paintImmediately(0,0,panel.getWidth(),panel.getHeight());
        panel.mouseClicked(new MouseEvent(panel,MouseEvent.MOUSE_CLICKED,System.currentTimeMillis(),0,x-panel.camera().x(),y-panel.camera().y(),1,false,MouseEvent.BUTTON1));
    }
    private static KeyEvent key(WandJPanel panel,int code) { return new KeyEvent(panel,KeyEvent.KEY_RELEASED,System.currentTimeMillis(),0,code,KeyEvent.CHAR_UNDEFINED); }
    private static void attack(WandJPanel panel,long id) throws Exception {
        edt(() -> {
            Unit target=PlayerManager.getInstance().player(id); click(panel,target.getCurX(),target.getCurY()-30);
            require(PlayerManager.getInstance().selected()==id,"mouse selects player");
            panel.keyReleased(key(panel,KeyEvent.VK_F3)); return true;
        });
    }
    private static void move(int x,int y) throws Exception {
        edt(() -> { MessageManager.sendMoveRequest(x,y); return true; });
        await(() -> edt(() -> { var p=PlayerManager.getInstance().selfPlayer(); return p.getCurX()==x && p.getCurY()==y && !p.getMovement().isMoving(); }),"positioned "+x+","+y);
    }
    private static void awaitCount(int count) throws Exception { await(() -> edt(() -> ItemManager.getInstance().inventory().count(ItemStack.Type.GOLD)==count),"inventory="+count); }
    private static void snap(JFrame frame,WandJPanel panel,Path output,String name) throws Exception {
        edt(() -> {
            var repaint = RepaintManager.currentManager(panel);
            boolean buffered = repaint.isDoubleBufferingEnabled();
            try { repaint.setDoubleBufferingEnabled(false); capture(frame,panel,output.resolve(name+".png")); }
            finally { repaint.setDoubleBufferingEnabled(buffered); }
            return true;
        });
    }
    private static void signal(Path output,String name) throws Exception { Files.writeString(output.resolve(name),"ready"); }
    private static void waitFile(Path output,String name) throws Exception { await(() -> Files.exists(output.resolve(name)),name); }
    private static void require(boolean condition,String label) { if(!condition) throw new AssertionError(label); }
}
