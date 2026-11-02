package wand6.client;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

import java.awt.FontMetrics;
import wand6.common.exceptions.MessageManagerException;

public class WandJPanel extends JPanel implements KeyListener, MouseListener, ComponentListener {

    private static int debugLevel = 1;
    private boolean wasShown;
    private Dimension panelDim = new Dimension(640, 420);
    private int repaintCount;
    private long lastUpdateFPSTime;
    private int fps;
    private Camera camera = new Camera(0, 0);
    Camera camera() { return camera; }

    public WandJPanel() {}

    @Override
    public synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);

        repaintCount++;
        if (lastUpdateFPSTime == 0L) {
            lastUpdateFPSTime = System.currentTimeMillis() - 1;
        }
        g.drawString(fps + " fps", 10, 60);
        if (System.currentTimeMillis() - lastUpdateFPSTime > 1000L) {
            fps = repaintCount;
            repaintCount = 0;
            lastUpdateFPSTime = System.currentTimeMillis();
        }

        var player = PlayerManager.getInstance().selfPlayer();
        if (player == null) {
            g.drawString(PlayerManager.getInstance().status(), 20, 90);
            return;
        }
        long time = wand6.common.ServerTime.getInstance().getTimeSinceStart();
        var position = player.getMovement().position(time);
        panelDim = getSize();
        camera = Camera.follow(position.x(), position.y(), panelDim.width, panelDim.height);
        MapManager.getInstance().drawMap(g, panelDim, camera);
        PlayerManager.getInstance().drawPlayers(g, camera, time);
        PlayerManager.getInstance().drawEffects(g, camera, time);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.requestFocus();
        if (PlayerManager.getInstance().selfPlayer() == null) return;
        if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
            try { MessageManager.sendStopRequest(); }
            catch (MessageManagerException ex) { System.err.println(ex.getMessage()); }
            return;
        }
        long clickX = (long) camera.x() + e.getX();
        long clickY = (long) camera.y() + e.getY();
        if (clickX >= 0 && clickY >= 0 && clickX <= wand6.common.MapFragment.maxX
                && clickY <= wand6.common.MapFragment.maxY) {
            try {
                var players = PlayerManager.getInstance();
                players.selectAt((int) clickX, (int) clickY);
                if (players.selected() == 0) MessageManager.sendMoveRequest((int) clickX, (int) clickY);
            } catch (MessageManagerException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        } else {
            if (debugLevel > 0) {
                System.out.println("Click out of map.");
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void componentResized(ComponentEvent e) {
        panelDim = e.getComponent().getSize();
    }

    @Override
    public void componentShown(ComponentEvent e) {
        if (!wasShown) {
            wasShown = true;

            panelDim = getSize();
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F3) {
            try {
                long target = PlayerManager.getInstance().selected();
                if (target != 0) MessageManager.sendAttackRequest(target);
            } catch (MessageManagerException ex) { System.err.println(ex.getMessage()); }
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) PlayerManager.getInstance().unselect();
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            PlayerManager.getInstance().unselect();
        }
    }
}
