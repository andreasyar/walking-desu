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
import java.util.Iterator;
import java.util.ListIterator;
import javax.swing.JPanel;

import common.WanderingServerTime;
import java.awt.Graphics2D;

public class WanderingJPanel extends JPanel implements KeyListener, MouseListener, ComponentListener {

    private boolean wasShown = false;
    private BufferedImage mapImg = WanderingMap.getMapImg();
    private Dimension mapDim = new Dimension(mapImg.getWidth(), mapImg.getHeight());
    //private Dimension mapOfst;
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

    /**
     * Tower build delay.
     */
    private static long buildDelay = 5000;  // 5 sec

    /**
     * Tower last build time.
     */
    private long lastBuildTime = 0;

    public WanderingJPanel(GameField field, ServerInteraction inter) {
        this.field = field;
        this.inter = inter;
    }

    @Override
    public synchronized void paintComponent(Graphics g) {
        try {
            super.paintComponent(g);

            // Наш игрок.
            Player selfPlayer = field.getSelfPlayer();

            if (selfPlayer != null) {

                // Координаты нашего игрока на карте.
                Point playerWorldPos = selfPlayer.getCurPos();

                // Координаты верхнего левого угла экрана на карте.
                int screenWorldX = screenWorldX = playerWorldPos.x - panelDim.width / 2;
                int screenWorldY = screenWorldY = playerWorldPos.y - panelDim.height / 2;

                // Text cloud contains the player message.
                BufferedImage textCloud;

                // Sprite for temporary storage purpose.
                Sprite s = null;

                // Buffered image for temporary storage purpose.
                BufferedImage img;

                // <editor-fold defaultstate="collapsed" desc="Draw map">
                ClientMapFragment curFragment, tmpFragment;

                // Номера верхнего левого фрагмента карты, который должен быть виден
                // на экране.
                int begFragmIdx = 0, begFragmIdy = 0;

                // Номера нижнего правого фрагмента карты, который может быть виден
                // на экране.
                int endFragmIdx = 0, endFragmIdy = 0;

                // Определим, какому фрагменту карты принадлежит верхняя левая точка
                // экрана. Если никакому, то значит начнем с фрагмента (0, 0).
                curFragment = field.getMapFragmentContains(playerWorldPos.x - panelDim.width / 2,
                                                           playerWorldPos.y - panelDim.height / 2);
                if (curFragment != null) {
                    begFragmIdx = curFragment.getIdx();
                    begFragmIdy = curFragment.getIdy();
                } else {
                    begFragmIdx = 0;
                    begFragmIdy = 0;
                }

                // Определим, какому фрагменту карты принадлежит нижняя правая точка
                // экрана. Если никакому, то неисправимая ошибка.
                curFragment = field.getMapFragmentContains(playerWorldPos.x + panelDim.width / 2,
                                                           playerWorldPos.y + panelDim.height / 2);
                if (curFragment != null) {
                    endFragmIdx = curFragment.getIdx();
                    endFragmIdy = curFragment.getIdy();
                } else {
                    System.err.println("Map fragment what contains buttom right screen point not found by unknown reason!");
                    System.exit(1);
                }

                // Координаты верхнего левого угла фаргмента карты.
                int mapFragmWorldX, mapFragmWorldY;

                // Число выводимых на экран фрагментов карты.
                int _count = 0;

                // Теперь, зная номера верхнего левого фрагмента карты, который ДОЛЖЕН быть виден
                // на экране и номера нижнего правого фрагмента карты, который МОЖЕТ быть
                // виден на экране, пройдём по имеющимся у нас фрагментам карты и нарисуем
                // их в нужных местах экрана.
                for (int i = begFragmIdx; i <= endFragmIdx; i++) {
                    for (int j = begFragmIdy; j <= endFragmIdy; j++) {

                        tmpFragment = field.getMapFragment(i, j);

                        // Если у нас есть такой фрагмент.
                        if (tmpFragment != null) {
                            if (i == 0 && j == 0) {

                                // Фрагмент с нулевыми номерами это наша карта,
                                // нарисованная от руки.
                                img = mapImg;
                            } else {
                                img = tmpFragment.getImage();
                            }

                            mapFragmWorldX = i * img.getWidth();
                            mapFragmWorldY = j * img.getHeight();
                            g.drawImage(img,
                                        mapFragmWorldX - screenWorldX,
                                        mapFragmWorldY - screenWorldY,
                                        null);
                            _count++;
                        } else {

                            // Если нет такого фрагмента, то будет пустота.
                        }
                    }
                }
                g.drawString("" + _count, 10, 20);// </editor-fold>

                // <editor-fold defaultstate="collapsed" desc="Draw info about selected unit.">
                if (selfPlayer.getSelectedUnit() != null) {
                    g.drawString("deathAnim: " + selfPlayer.getSelectedUnit().deathAnimationDone(), 10, panelDim.height - 110);
                    g.drawString("dead: " + selfPlayer.getSelectedUnit().dead(), 10, panelDim.height - 90);
                    g.drawString("isMove: " + selfPlayer.getSelectedUnit().isMove(), 10, panelDim.height - 70);
                    g.drawString(selfPlayer.getSelectedUnit().getNick() + " (" + selfPlayer.getSelectedUnit().getID() + ")", 10, panelDim.height - 50);
                    g.drawString("HP: " + selfPlayer.getSelectedUnit().getHitPoints(), 10, panelDim.height - 30);
                }// </editor-fold>

                field.drawItems(g, screenWorldX, screenWorldY);

                field.drawUnits(g, screenWorldX, screenWorldY);

                // <editor-fold defaultstate="collapsed" desc="Draw players text and other player-specifed things.">
                for (Player p : field.getPlayers()) {
                    s = p.getSprite();
                    textCloud = p.getTextCloud();
                    if (textCloud != null) {
                        g.drawImage(textCloud,
                                s.x - screenWorldX + s.image.getWidth(),
                                s.y - screenWorldY,
                                null);
                    }
                    if (showTowerRange && p.isMove()) {
                        playerWorldPos = p.getCurPos();
                        g.drawLine(playerWorldPos.x - screenWorldX,
                                playerWorldPos.y - screenWorldY,
                                p.getEnd().x - screenWorldX,
                                p.getEnd().y - screenWorldY);
                        g.drawOval(playerWorldPos.x - screenWorldX - 500,
                                playerWorldPos.y - screenWorldY - 500,
                                1000,
                                1000);
                    }
                }
                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc="Draw towers range.">
                if (showTowerRange) {
                    for (Tower t : field.getTowers()) {
                        g.drawOval(t.getCurPos().x - screenWorldX - (int) t.getRange(),
                                t.getCurPos().y - screenWorldY - (int) t.getRange(),
                                (int) t.getRange() * 2,
                                (int) t.getRange() * 2);
                    }
                }
                // </editor-fold>

                field.drawNukes(g, screenWorldX, screenWorldY);

                field.drawHitAnimation(g, screenWorldX, screenWorldY);

                g.drawImage(geoDebugLayer, - screenWorldX, - screenWorldY, null);

                String tdStatus = field.getTDStatus();
                if (tdStatus != null) {
                    g.drawString(tdStatus, panelDim.width / 2, 50);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.requestFocus();
        int x = e.getX();
        int y = e.getY();
        
        // Наш игрок.
        Player selfPlayer = field.getSelfPlayer();

        if (selfPlayer != null) {

            // Координаты нашего игрока на карте.
            Point playerWorldPos = selfPlayer.getCurPos();

            // Координаты верхнего левого угла экрана на карте.
            int screenWorldX = screenWorldX = playerWorldPos.x - panelDim.width / 2;
            int screenWorldY = screenWorldY = playerWorldPos.y - panelDim.height / 2;

            // Если клик был в пределах карты.
            if (x + screenWorldX >= 0
                    && y + screenWorldY >= 0
                    && x + screenWorldX <= Integer.MAX_VALUE
                    && y + screenWorldY <= Integer.MAX_VALUE) {

                boolean selected = false;
                Polygon poly;
                WUnit unit;
                Sprite spr;

                if (selectMode) {
                    for (Iterator<WUnit> li = field.getUnits().iterator(); li.hasNext();) {
                        unit = li.next();
                        spr = unit.getSprite();
                        poly = new Polygon();
                        poly.addPoint(spr.x, spr.y);
                        poly.addPoint(spr.x + spr.image.getWidth(), spr.y);
                        poly.addPoint(spr.x + spr.image.getWidth(), spr.y + spr.image.getHeight());
                        poly.addPoint(spr.x, spr.y + spr.image.getHeight());

                        if (poly.contains(x + screenWorldX, y + screenWorldY)) {
                            field.getSelfPlayer().selectUnit(unit);
                            selected = true;
                            break;
                        }
                    }
                }
                if (!selected) {
                    Point p = new Point(x + screenWorldX, y + screenWorldY);
                    Point cur = field.getSelfPlayer().getCurPos();
                    poly = new Polygon();
                    poly.addPoint(p.x, p.y);
                    poly.addPoint(p.x - 1, p.y - 1);
                    poly.addPoint(cur.x, cur.y);
                    poly.addPoint(cur.x - 1, cur.y - 1);
                    //BasicStroke str = new BasicStroke(1);
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
                        ClientMapFragment curFragment = field.getMapFragment(p);
                        int n = field.getAroundFragCount(mapDim);
                        for (int i = curFragment.getIdy() - n; i <= curFragment.getIdy() + n; i++) {
                            for (int j = curFragment.getIdx() - n; j <= curFragment.getIdx() + n; j++) {
                                if (i >= 0 && j >= 0 && field.getMapFragment(j, i) == null) {
                                    inter.addCommand("(getmapfragm " + j + " " + i + ")");
                                }
                            }
                        }

                        field.getSelfPlayer().move(cur.x, cur.y, p.x, p.y, System.currentTimeMillis() - ServerInteraction.serverStartTime);
                        inter.addCommand("(move " + p.x + " " + p.y + ")");
                    }
                }
            }
        }//selfPlayer != null
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
        Dimension d = e.getComponent().getSize();

        /*if (panelDim != null && d != null && mapOfst != null && (panelDim.width != d.width || panelDim.height != d.height)) {
            mapOfst.width += d.width / 2 - panelDim.width / 2;
            mapOfst.height += d.height / 2 - panelDim.height / 2;
        }*/

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
            //mapOfst = new Dimension(panelDim.width / 2, panelDim.height / 2);

            /*geoDebugLayer = new BufferedImage(mapDim.getSize().width, mapDim.getSize().height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            geoDebugLayerGraph = geoDebugLayer.getGraphics();
            ((Graphics2D) geoDebugLayerGraph).setBackground(new Color((float)0.0, (float)0.0, (float)0.0, (float)0.7));*/
            /*((Graphics2D) geoDebugLayerGraph).setColor(new Color((float)0.1, (float)1.0, (float)0.3, (float)0.7));
            ((Graphics2D) geoDebugLayerGraph).fillRect(0, 0, geoDebugLayer.getWidth() - 1, geoDebugLayer.getHeight() - 1);
            ((Graphics2D) geoDebugLayerGraph).setColor(Color.BLACK);*/
            /*geoDebugLayerDim = new Dimension(geoDebugLayer.getWidth(), geoDebugLayer.getHeight());
            Color c;
            for (WanderingPolygon poly : WanderingMap.getGeoData()) {
                geoDebugLayerGraph.drawPolygon(poly);*/
                /*c = geoDebugLayerGraph.getColor();
                if (poly.getType() == WanderingPolygon.WallType.MONOLITH) {
                geoDebugLayerGraph.setColor(Color.BLACK);
                } else if (poly.getType() == WanderingPolygon.WallType.SPECIAL) {
                geoDebugLayerGraph.setColor(Color.BLUE);
                } else if (poly.getType() == WanderingPolygon.WallType.TRANSPARENT) {
                geoDebugLayerGraph.setColor(new Color((float)1.0, (float)1.0, (float)1.0, (float)0.4));
                }
                geoDebugLayerGraph.fillPolygon(poly);
                geoDebugLayerGraph.setColor(c);*/
            //}
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
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ESCAPE) {
            field.getSelfPlayer().unselectUnit();
        } else if (key == KeyEvent.VK_SHIFT) {
            if (!selectMode) {
                selectMode = true;
            }
        } else if (key == KeyEvent.VK_F3) {
            WUnit self = field.getSelfPlayer();
            if (self.getSelectedUnit() != null
                    && (self.getCurrentNuke() == null || !self.getCurrentNuke().isReuse())
                    && !self.equals(self.getSelectedUnit())) {
                inter.addCommand("(attack " + self.getSelectedUnit().getID() + ")");
            }
        } else if (key == KeyEvent.VK_F4) {
            if (true || System.currentTimeMillis() - ServerInteraction.serverStartTime - lastBuildTime > buildDelay) {
                Point cur = field.getSelfPlayer().getCurPos();
                inter.addCommand("(tower " + cur.x + " " + cur.y + ")");
                lastBuildTime = System.currentTimeMillis() - ServerInteraction.serverStartTime;
            }
        } else if (key == KeyEvent.VK_T) {
            showTowerRange = true;
        } else if (key == KeyEvent.VK_B) {
            showGeoDataBorders = false;
        } else if (key == KeyEvent.VK_N) {
            showGeoDataTypes = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            selectMode = false;
        } else if (e.getKeyCode() == KeyEvent.VK_T) {
            showTowerRange = false;
        } else if (e.getKeyCode() == KeyEvent.VK_B) {
            showGeoDataBorders = false;
        } else if (e.getKeyCode() == KeyEvent.VK_N) {
            showGeoDataTypes = false;
        }
    }
}
