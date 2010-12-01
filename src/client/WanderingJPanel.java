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
    //debug
    public static long threadId;
    public static boolean resourcesInProcess;

    static int repaintCalls = 0;

    public WanderingJPanel(GameField field, ServerInteraction inter) {
        this.field = field;
        this.inter = inter;
        addMouseListener(this);
        addComponentListener(this);
        addKeyListener(this);
    }

    @Override
    public synchronized void paintComponent(Graphics g) {
        try {
            repaintCalls++;

            if (repaintCalls > 1) {
                System.out.println("OH SHIT! " + repaintCalls);
            }

            threadId = Thread.currentThread().getId();
            super.paintComponent(g);
            Player selfPlayer = field.getSelfPlayer();

            if (selfPlayer != null && buffDim != null) { // TODO Concurency issue

                // Поле вне карты цвета фона и по краям черная рамочка в 1 пиксел.
                g.setColor(getBackground());
                g.fillRect(0, 0, buffDim.width - 1, buffDim.height - 1);
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, buffDim.width - 1, buffDim.height - 1);

                Point curPos = selfPlayer.getCurPos();

                ClientMapFragment curFragment, tmpFragment;
                BufferedImage tmpImg;

                // Номера верхнего левого фрагмента карты, который должен быть виден
                // на экране.
                int begFragmIdx = 0, begFragmIdy = 0;

                // Номера нижнего правого фрагмента карты, который может быть виден
                // на экране.
                int endFragmIdx = 0, endFragmIdy = 0;

                // Определим, какому фрагменту карты принадлежит верхняя левая точка
                // экрана. Если никакому, то значит начнем с фрагмента (0, 0).
                curFragment = field.getMapFragmentContains(curPos.x - panelDim.width / 2,
                                                           curPos.y - panelDim.height / 2);
                if (curFragment != null) {
                    begFragmIdx = curFragment.getIdx();
                    begFragmIdy = curFragment.getIdy();
                } else {
                    begFragmIdx = 0;
                    begFragmIdy = 0;
                }

                // Определим, какому фрагменту карты принадлежит нижняя правая точка
                // экрана. Если никакому, то неисправимая ошибка.
                curFragment = field.getMapFragmentContains(curPos.x + panelDim.width / 2,
                                                           curPos.y + panelDim.height / 2);
                if (curFragment != null) {
                    endFragmIdx = curFragment.getIdx();
                    endFragmIdy = curFragment.getIdy();
                } else {
                    System.err.println("Map fragment what contains buttom right screen point not found by unknown reason!");
                    System.exit(1);
                }
                // Координаты верхнего левого угла экрана на карте.
                int screenWorldX, screenWorldY;

                // Координаты верхнего левого угла фаргмента карты.
                int mapFragmWorldX, mapFragmWorldY;

                // Теперь, зная номера верхнего левого фрагмента карты, который ДОЛЖЕН быть виден
                // на экране и номера нижнего правого фрагмента карты, который МОЖЕТ быть
                // виден на экране, пройдём по имеющимся у нас фрагментам карты и нарисуем
                // их в нужных местах экрана.
                int _count = 0;
                for (int i = begFragmIdx; i <= endFragmIdx; i++) {
                    for (int j = begFragmIdy; j <= endFragmIdy; j++) {

                        tmpFragment = field.getMapFragment(i, j);

                        // Если у нас есть такой фрагмент.
                        if (tmpFragment != null) {
                            if (i == 0 && j == 0) {

                                // Фрагмент с нулевыми номерами это наша карта, нарисованная
                                // от руки.
                                tmpImg = mapImg;
                            } else {
                                tmpImg = tmpFragment.getImage();
                            }

                            screenWorldX = curPos.x - panelDim.width / 2;
                            screenWorldY = curPos.y - panelDim.height / 2;
                            mapFragmWorldX = i * tmpImg.getWidth();
                            mapFragmWorldY = j * tmpImg.getHeight();
                            g.drawImage(tmpImg,
                                        mapFragmWorldX - screenWorldX,
                                        mapFragmWorldY - screenWorldY,
                                        null);
                            _count++;
                        } else {

                            // А если нет, то будет пустота.
                        }
                    }
                }
                g.drawString("" + _count, 10, 20);

                // Вычислим новое смещение карты (временно это делается здесь)
                if (panelDim.width / 2 != mapOfst.width + curPos.x
                        || panelDim.height / 2 != mapOfst.height + curPos.y) {
                    mapOfst.width += panelDim.width / 2 - (curPos.x + mapOfst.width);
                    mapOfst.height += panelDim.height / 2 - (curPos.y + mapOfst.height);
                }

                if (selfPlayer.getSelectedUnit() != null) {
                    g.drawString("deathAnim: " + selfPlayer.getSelectedUnit().deathAnimationDone(), 10, panelDim.height - 110);
                    g.drawString("dead: " + selfPlayer.getSelectedUnit().dead(), 10, panelDim.height - 90);
                    g.drawString("isMove: " + selfPlayer.getSelectedUnit().isMove(), 10, panelDim.height - 70);
                    g.drawString(selfPlayer.getSelectedUnit().getNick() + " (" + selfPlayer.getSelectedUnit().getID() + ")", 10, panelDim.height - 50);
                    g.drawString("HP: " + selfPlayer.getSelectedUnit().getHitPoints(), 10, panelDim.height - 30);
                }

                // Lets lock all lists.
                Sprite s = null;
                resourcesInProcess = true;

                // For all units we draw their sprite and nick name.
                Point selfPos = field.getSelfPlayer().getCurPos();
                Point pos;

                for (WUnit u : field.getYSortedUnits()) {
                    s = u.getSprite();
                    pos = u.getCurPos();
                    if (selfPos.distance(pos) <= 500.0) {
                        g.drawImage(s.image, s.x + mapOfst.width, s.y + mapOfst.height, null);
                        g.drawString(u.getNick(), s.x + mapOfst.width + s.image.getWidth() / 2 - 20, s.y + mapOfst.height + s.image.getHeight() + 20);
                    }
                }

                // For players we draw text cloud if it.
                BufferedImage textCloud;

                for (Player p : field.getPlayers()) {
                    s = p.getSprite();
                    textCloud = p.getTextCloud();
                    if (textCloud != null) {
                        g.drawImage(textCloud, s.x + mapOfst.width + s.image.getWidth(), s.y + mapOfst.height, null);
                    }
                    if (showTowerRange && p.isMove()) {
                        curPos = p.getCurPos();
                        g.drawLine(curPos.x + mapOfst.width, curPos.y + mapOfst.height, p.getEnd().x + mapOfst.width, p.getEnd().y + mapOfst.height);
                        g.drawOval(curPos.x + mapOfst.width - 500, curPos.y + mapOfst.height - 500, 1000, 1000);
                    }
                }

                // Draw towers range if enabled.
                if (showTowerRange) {
                    for (Tower t : field.getTowers()) {
                        g.drawOval(t.getCurPos().x + mapOfst.width - (int) t.getRange(), t.getCurPos().y + mapOfst.height - (int) t.getRange(), (int) t.getRange() * 2, (int) t.getRange() * 2);
                    }
                }

                // Draw nuke bolts.
                for (Nuke n : field.getNukes()) {

                    if (n == null) {
                        System.err.println("OMG nuke is null LOAL.");
                        continue;
                    }

                    if (n.isMove() && ((s = n.getSprite()) != null)) {
                        g.drawImage(s.image, s.x + mapOfst.width, s.y + mapOfst.height, null);
                    }
                }

                // Draw hit animation.
                for (HitAnimation a : field.getHitAnimations()) {
                    if (!a.isDone()) {
                        s = a.getSprite(System.currentTimeMillis() - ServerInteraction.serverStartTime);
                        g.drawImage(s.image, s.x + mapOfst.width, s.y + mapOfst.height, null);
                    }
                }

                // Lets lock all lists.
                resourcesInProcess = false;

                g.drawImage(geoDebugLayer, mapOfst.width, mapOfst.height, null);

                String tdStatus = field.getTDStatus();
                if (tdStatus != null) {
                    g.drawString(tdStatus, panelDim.width / 2, 50);
                }

                //g.drawImage(buffImg, 0, 0, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            repaintCalls--;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.requestFocus();
        int x = e.getX();
        int y = e.getY();

        // Если клик был в пределах карты.
        if (true || x >= mapOfst.width && x <= mapDim.width + mapOfst.width && y >= mapOfst.height && y <= mapDim.height + mapOfst.height) {
            boolean selected = false;
            Polygon poly;
            WUnit unit;
            Sprite spr;

            if (selectMode) {
                synchronized (field.getUnits()) {
                    for (Iterator<WUnit> li = field.getUnits().iterator(); li.hasNext();) {
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
            //Color c;
            for (WanderingPolygon poly : WanderingMap.getGeoData()) {
                //geoDebugLayerGraph.drawPolygon(poly);
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
            }
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

    /* else if (key == KeyEvent.VK_F5) {
        ConcurencyDebugClientKiller killer = new ConcurencyDebugClientKiller();
        Thread killerThread = new Thread(killer);
        killerThread.start();
        }*/
    /*private class ConcurencyDebugClientKiller implements Runnable {

    @Override
    public void run() {
    ArrayList<Unit> units = field.getUnits();
    Unit unit;

    while (true) {
    unit = units.get(0);
    units.remove(0);
    units.add(unit);
    }
    }
    }*/
}
