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

        MapManager.getInstance().drawMap(g, panelDim);
        PlayerManager.getInstance().drawPlayers(g, panelDim);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.requestFocus();
        int selfPlayerX, selfPlayerY;

        try {
            selfPlayerX = PlayerManager.getInstance().getSelfPlayerX();
        } catch (NullPointerException ex) {
            if (debugLevel > 0) {
                System.err.println("Self player X-axis cannot be calculated.");
            }
            return;
        }
        try {
            selfPlayerY = PlayerManager.getInstance().getSelfPlayerY();
        } catch (NullPointerException ex) {
            if (debugLevel > 0) {
                System.err.println("Self player Y-axis cannot be calculated.");
            }
            return;
        }

        // Upper left corner of drawing panel on world map.
        int panelX = selfPlayerX - panelDim.width / 2;
        int panelY = selfPlayerY - panelDim.height / 2;

        int clickX = panelX + e.getX();
        int clickY = panelY + e.getY();

        if (clickX >= 0 && clickY >= 0) {
            try {
                MessageManager.sendMoveRequest(clickX, clickY);
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
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            // Reserved for selection in a later stage.
        }
    }
}
