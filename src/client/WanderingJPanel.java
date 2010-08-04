package client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.JPanel;

public class WanderingJPanel extends JPanel implements KeyListener, MouseListener, ComponentListener {
    private boolean wasShown = false;

    private BufferedImage mapImg = WanderingMap.getMapImg();
    private Dimension mapDim = new Dimension(mapImg.getWidth(), mapImg.getHeight());
    private Dimension mapOfst;

    private Dimension panelDim = null;

    private BufferedImage buffImg = null;
    private Graphics buffGraph = null;
    private Dimension buffDim = null;

    private BufferedImage geoDebugLayer = null;
    private Graphics geoDebugLayerGraph = null;
    private Dimension geoDebugLayerDim = null;

    private boolean selectMode = false;
    private boolean showTowerRange = false;

    private GameField field;
    private ServerInteraction inter;

    private static long buildDelay = 5000;  // 5 sec
    private long lastBuildTime = 0;

    public WanderingJPanel(GameField field, ServerInteraction inter) {
        this.field = field;
        this.inter = inter;
        addMouseListener(this);
        addComponentListener(this);
        addKeyListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Player selfPlayer = field.getSelfPlayer();

        if (field.getSelfPlayer() != null && buffDim != null) { // TODO Concurency issue

            // Поле вне карты цвета фона и по краям черная рамочка в 1 пиксел.
            buffGraph.setColor(getBackground());
            buffGraph.fillRect(0, 0, buffDim.width - 1, buffDim.height - 1);
            buffGraph.setColor(Color.BLACK);
            buffGraph.drawRect(0, 0, buffDim.width - 1, buffDim.height - 1);

            // TODO На самом деле надо рисовать лишь видимую часть карты
            // которую можно получить с помощью метода getSubimage()
            buffGraph.drawImage(mapImg, mapOfst.width, mapOfst.height, null);

            // Вычислим новое смещение карты (временно это делается здесь)
            Point curPos = field.getSelfPlayer().getCurPos();
            if (panelDim.width / 2 != mapOfst.width + curPos.x || panelDim.height / 2 != mapOfst.height + curPos.y) {
                mapOfst.width += panelDim.width / 2 - (curPos.x + mapOfst.width);
                mapOfst.height += panelDim.height / 2 - (curPos.y + mapOfst.height);
            }

            if (selfPlayer.getSelectedUnit() != null) {
                buffGraph.drawString(selfPlayer.getSelectedUnit().getNick() + " (" + selfPlayer.getSelectedUnit().getID() + ")", 10, panelDim.height - 50);
                buffGraph.drawString("HP: " + selfPlayer.getSelectedUnit().getHitPoints(), 10, panelDim.height - 30);
            }

            // For all units we draw their sprite and nick name.
            ArrayList<Unit> units = field.getYSortedUnits();
            synchronized (units) {
                Sprite s;
                Point selfPos = field.getSelfPlayer().getCurPos(),
                      pos;

                for (Unit u : units) {
                    s = u.getSprite();
                    pos = u.getCurPos();
                    if (selfPos.distance(pos) <= 500.0) {
                        buffGraph.drawImage(s.image, s.x + mapOfst.width, s.y + mapOfst.height, null);
                        buffGraph.drawString(u.getNick(), s.x + mapOfst.width + s.image.getWidth() / 2 - 20, s.y + mapOfst.height + s.image.getHeight() + 20);
                    }
                }
            }

            // For players we draw text cloud if it.
            ArrayList<Player> players = field.getPlayers();
            synchronized (players) {
                BufferedImage textCloud;
                Sprite s;

                for (Player p : players) {
                    s = p.getSprite();
                    textCloud = p.getTextCloud();
                    if (textCloud != null) {
                        buffGraph.drawImage(textCloud, s.x + mapOfst.width + s.image.getWidth(), s.y + mapOfst.height, null);
                    }
                    if (showTowerRange && p.isMove()) {
                        curPos = p.getCurPos();
                        buffGraph.drawLine(curPos.x + mapOfst.width, curPos.y + mapOfst.height, p.getEndPoint().x + mapOfst.width, p.getEndPoint().y + mapOfst.height);
                        buffGraph.drawOval(curPos.x + mapOfst.width - 500, curPos.y + mapOfst.height - 500, 1000, 1000);
                    }
                }
            }

            // Draw nuke bolts.
            ArrayList<Nuke> nukes = field.getNukes();
            synchronized (nukes) {
                Sprite s;

                for (Nuke n : nukes) {
                    if (n.isMove()) {
                        s = n.getSprite();
                        System.out.println((s.x + mapOfst.width) + " " + (s.y + mapOfst.height));
                        buffGraph.drawImage(s.image, s.x + mapOfst.width, s.y + mapOfst.height, null);
                    }
                }
            }

            // Draw towers range if enabled.
            if (showTowerRange) {
                ArrayList<Tower> towers = field.getTowers();
                synchronized (towers) {
                    for (Tower t : towers) {
                        buffGraph.drawOval(t.getCurPos().x + mapOfst.width - (int) t.getRange(), t.getCurPos().y + mapOfst.height - (int) t.getRange(), (int) t.getRange() * 2, (int) t.getRange() * 2);
                    }
                }
            }
            

            buffGraph.drawImage(geoDebugLayer, mapOfst.width, mapOfst.height, null);

            String tdStatus = field.getTDStatus();
            if (tdStatus != null) {
                buffGraph.drawString(tdStatus, panelDim.width / 2, 50);
            }

            g.drawImage(buffImg, 0, 0, null);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.requestFocus();
        int x = e.getX();
        int y = e.getY();

        // Если клик был в пределах карты.
        if (x >= mapOfst.width && x <= mapDim.width + mapOfst.width && y >= mapOfst.height && y <= mapDim.height + mapOfst.height) {
            boolean selected = false;
            Polygon poly;
            Unit unit;
            Sprite spr;

            if (selectMode) {
                synchronized (field.getUnits()) {
                    for(ListIterator<Unit> li = field.getUnits().listIterator(); li.hasNext(); ) {
                        unit = li.next();
                        spr = unit.getSprite();
                        poly = new Polygon();
                        poly.addPoint(spr.x, spr.y);
                        poly.addPoint(spr.x + spr.image.getWidth(), spr.y);
                        poly.addPoint(spr.x + spr.image.getWidth(), spr.y + spr.image.getHeight());
                        poly.addPoint(spr.x, spr.y + spr.image.getHeight());

                        if (poly.contains(x - mapOfst.width, y - mapOfst.height)) {
                            field.getSelfPlayer().selectUnit(unit);
                            selected = true;
                            break;
                        }
                    }
                }
            }
            if (!selected) {
                Point p = new Point(x - mapOfst.width, y - mapOfst.height);
                Point cur = field.getSelfPlayer().getCurPos();
                poly = new Polygon();
                poly.addPoint(p.x, p.y);
                poly.addPoint(p.x - 1, p.y - 1);
                poly.addPoint(cur.x, cur.y);
                poly.addPoint(cur.x - 1, cur.y - 1);
                BasicStroke str = new BasicStroke(1);
                Area c = new Area(poly);
                boolean canGo = true;
                for (ListIterator<WanderingPolygon> li = WanderingMap.getGeoData().listIterator(); li.hasNext();) {
                    WanderingPolygon curPoly = li.next();
                    c.intersect(new Area(curPoly));
                    if (curPoly.getType() == WanderingPolygon.WallType.MONOLITH) {
                        if (curPoly.contains(p) || !c.isEmpty()) {
                            canGo = false;
                        }
                    }
                }
                if (canGo) {
                    field.getSelfPlayer().move((Point) field.getSelfPlayer().getCurPos().clone(), p, System.currentTimeMillis() - ServerInteraction.serverStartTime);
                    inter.addCommand("(move " + p.x + " " + p.y + ")");
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void componentResized(ComponentEvent e) {
        Dimension d = e.getComponent().getSize();

        if (buffImg != null && (buffImg.getWidth() != d.width || buffImg.getHeight() != d.height)) {
            buffImg = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
            buffDim = new Dimension(buffImg.getWidth(), buffImg.getHeight());
            buffGraph = buffImg.getGraphics();
        }

        if (panelDim != null && d != null && mapOfst != null && (panelDim.width != d.width || panelDim.height != d.height)) {
            mapOfst.width += d.width / 2 - panelDim.width / 2;
            mapOfst.height += d.height / 2 - panelDim.height / 2;
        }

        panelDim = d;
    }

    @Override
    public void componentShown(ComponentEvent e) {
        if (!wasShown) {
            wasShown = true;

            panelDim = getSize();

            // Поскольку наш игрок всегда находится в центре экрана и в начале
            // его положение на карте (0, 0) то точка (0, 0) карты должны быть в
            // центре экрана.
            mapOfst = new Dimension(panelDim.width / 2, panelDim.height / 2);

            // Буфер для двойной буферизации.
            buffImg = new BufferedImage(panelDim.width, panelDim.height, BufferedImage.TYPE_INT_RGB);
            buffGraph = buffImg.getGraphics();
            buffDim = new Dimension(buffImg.getWidth(), buffImg.getHeight());

            geoDebugLayer = new BufferedImage(mapDim.getSize().width, mapDim.getSize().height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            geoDebugLayerGraph = geoDebugLayer.getGraphics();
            //((Graphics2D) geoDebugLayerGraph).setBackground(new Color((float)0.0, (float)0.0, (float)0.0, (float)0.7));
            /*((Graphics2D) geoDebugLayerGraph).setColor(new Color((float)0.1, (float)1.0, (float)0.3, (float)0.7));
            ((Graphics2D) geoDebugLayerGraph).fillRect(0, 0, geoDebugLayer.getWidth() - 1, geoDebugLayer.getHeight() - 1);
            ((Graphics2D) geoDebugLayerGraph).setColor(Color.BLACK);*/
            geoDebugLayerDim = new Dimension(geoDebugLayer.getWidth(), geoDebugLayer.getHeight());
            Color c;
            for (WanderingPolygon poly : WanderingMap.getGeoData()) {
                geoDebugLayerGraph.drawPolygon(poly);
                c = geoDebugLayerGraph.getColor();
                if (poly.getType() == WanderingPolygon.WallType.MONOLITH) {
                    geoDebugLayerGraph.setColor(Color.BLACK);
                } else if (poly.getType() == WanderingPolygon.WallType.SPECIAL) {
                    geoDebugLayerGraph.setColor(Color.BLUE);
                } else if (poly.getType() == WanderingPolygon.WallType.TRANSPARENT) {
                    geoDebugLayerGraph.setColor(new Color((float)1.0, (float)1.0, (float)1.0, (float)0.4));
                }
                geoDebugLayerGraph.fillPolygon(poly);
                geoDebugLayerGraph.setColor(c);
            }
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {}
    @Override
    public void componentHidden(ComponentEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ESCAPE) {
            field.getSelfPlayer().unselectUnit();
        } else if (key == KeyEvent.VK_SHIFT) {
            if (!selectMode) {
                selectMode = true;
            }
        } else if (key == KeyEvent.VK_F3) {
            Unit self =  field.getSelfPlayer();
            long tmp = Math.abs(System.currentTimeMillis() - ServerInteraction.serverStartTime);
            if (self.getSelectedUnit() != null && (self.getCurrentNuke() == null || !self.getCurrentNuke().reuse()) && !self.equals(self.getSelectedUnit())) {
                field.addNuke(self, self.getSelectedUnit(), System.currentTimeMillis() - ServerInteraction.serverStartTime);
                inter.addCommand("(bolt " + self.getSelectedUnit().getID() + ")");
            }
        } else if (key == KeyEvent.VK_F4) {
            if (System.currentTimeMillis() - ServerInteraction.serverStartTime - lastBuildTime > buildDelay) {
                Point cur =  field.getSelfPlayer().getCurPos();
                inter.addCommand("(tower " + cur.x + " " + cur.y + ")");
                lastBuildTime = System.currentTimeMillis() - ServerInteraction.serverStartTime;
            }
        } else if (key == KeyEvent.VK_T) {
            showTowerRange = true;
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            selectMode = false;
        } else if (e.getKeyCode() == KeyEvent.VK_T) {
            showTowerRange = false;
        }
    }
}
