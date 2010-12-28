package wand6.client;

import client.GameField;
import client.WanderingMap;
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

public class WandJPanel extends JPanel implements KeyListener, MouseListener, ComponentListener {

    private boolean wasShown = false;
    private BufferedImage mapImg = WanderingMap.getMapImg();
    private Dimension mapDim = new Dimension(mapImg.getWidth(), mapImg.getHeight());
    private Dimension panelDim = null;
    private BufferedImage geoDebugLayer = null;
    private Graphics geoDebugLayerGraph = null;
    private Dimension geoDebugLayerDim = null;
    private boolean selectMode = false;
    private boolean showTowerRange = false;
    private GameField field;
    private ServerInteraction inter;
    private boolean showGeoDataBorders = false;
    private boolean showGeoDataTypes = false;
    private static long buildDelay = 5000;
    private long lastBuildTime = 0;
    private boolean showSpriteBounds;
    private static FontMetrics fontMetrics = null;

    public WandJPanel(GameField field, ServerInteraction inter) {
        this.field = field;
        this.inter = inter;
    }

    @Override
    public synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);

        MapManager.getInstance().drawMap(g, panelDim);
        PlayerManager.getInstance().drawPlayers(g, panelDim);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.requestFocus();
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
            selectMode = false;
        }
    }

    public static FontMetrics getFontMetrics() {
        return fontMetrics;
    }
}
