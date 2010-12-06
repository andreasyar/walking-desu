package client;

import common.WanderingServerTime;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class GameField {

    public static double visibleDistance = 500.0;

    private Player selfPlayer;
    private final LinkedBlockingQueue<WUnit> units = new LinkedBlockingQueue<WUnit>();
    private final LinkedBlockingQueue<Player> players = new LinkedBlockingQueue<Player>();
    private final LinkedBlockingQueue<Nuke> nukes = new LinkedBlockingQueue<Nuke>();
    private final LinkedBlockingQueue<Tower> towers = new LinkedBlockingQueue<Tower>();
    private final LinkedBlockingQueue<Monster> monsters = new LinkedBlockingQueue<Monster>();
    private final LinkedBlockingQueue<HitAnimation> hitAnimations = new LinkedBlockingQueue<HitAnimation>();
    private final LinkedBlockingQueue<ClientMapFragment> mfagments = new LinkedBlockingQueue<ClientMapFragment>();
    private final LinkedBlockingQueue<WItem> items = new LinkedBlockingQueue<WItem>();
    private SelfExecutor selfExecutor;
    /**
     * Tower Defence mini game status: N/M xS Where N - monsters loss, M -
     * monsters loss limit, S - strength multiplyer.
     */
    private String tdStatus = null;
    private static YAligner aligner = new YAligner();
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);

    public GameField() {
        GeoDataController gdController = new GeoDataController(WanderingMap.getGeoData(), players);
        gdController.setScheduledFuture(executor.scheduleAtFixedRate(gdController, 0L, 200L, TimeUnit.MILLISECONDS));
        MonsterDeathController mdController = new MonsterDeathController(this);
        executor.scheduleAtFixedRate(mdController, 0L, 100L, TimeUnit.MILLISECONDS);
        PlayerDeathController pdController = new PlayerDeathController(this);
        executor.scheduleAtFixedRate(pdController, 0L, 100L, TimeUnit.MILLISECONDS);
        selfExecutor = new SelfExecutor(this);
    }

    /**
     * Draws units, items, nukes, hit animation.
     * @params g graphics context for drawing in.
     * @params x x-axis of top left screen position on world map.
     * @params y y-axis of top left screen position on world map.
     */
    public void drawAll(Graphics g, int x, int y, Dimension dim) {

        for (WDrawable d : units) {
            if (d != null) {
                d.draw(g, x, y, dim);
            }
            else {
                System.err.println("WDrawable unit is null. LOL? It cannot be!");
                continue;
            }
        }

        for (WDrawable d : nukes) {
            if (d != null) {
                d.draw(g, x, y, dim);
            }
            else {
                System.err.println("Nuke is null. LOL? It cannot be!");
                continue;
            }
        }

        for (WDrawable d : hitAnimations) {
            if (d != null) {
                d.draw(g, x, y, dim);
            }
            else {
                System.err.println("Hit animation is null. LOL? It cannot be!");
                continue;
            }
        }

        for (WDrawable d : items) {
            if (d != null) {
                d.draw(g, x, y, dim);
            }
            else {
                System.err.println("Items is null. LOL? It cannot be!");
                continue;
            }
        }
    }

    public void recalcUnitsZ(ArrayList<ArrayList<WDrawable>> zbuffer, int x, int y) {
        Point tmpPos;

        synchronized (zbuffer) {
            for (WUnit u : units) {
                if (u != null) {
                    tmpPos = u.getCurPos();
                    if (tmpPos.y - y >= 0 && tmpPos.y - y < zbuffer.size()) {
                        zbuffer.get(tmpPos.y - y).add(u);
                    }
                }
                else {
                    System.err.println("WUnit is null. LOL? It cannot be!");
                    continue;
                }
            }

            for (Nuke n : nukes) {
                if (n != null) {
                    tmpPos = n.getCurPos();
                    if (tmpPos.y - y >= 0 && tmpPos.y - y < zbuffer.size()) {
                        zbuffer.get(tmpPos.y - y).add(n);
                    }
                }
                else {
                    System.err.println("Nuke is null. LOL? It cannot be!");
                    continue;
                }
            }

            for (HitAnimation h : hitAnimations) {
                if (h != null) {
                    tmpPos = h.getCurPos();
                    if (tmpPos.y - y >= 0 && tmpPos.y - y < zbuffer.size()) {
                        zbuffer.get(tmpPos.y - y).add(h);
                    }
                }
                else {
                    System.err.println("Hit animation is null. LOL? It cannot be!");
                    continue;
                }
            }

            for (WItem i : items) {
                if (i != null) {
                    tmpPos = i.getCurPos();
                    if (tmpPos.y - y >= 0 && tmpPos.y - y < zbuffer.size()) {
                        zbuffer.get(tmpPos.y - y).add(i);
                    }
                }
                else {
                    System.err.println("Items is null. LOL? It cannot be!");
                    continue;
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Self player">
    public void addSelfPlayer(Player selfPlayer) {
        this.selfPlayer = selfPlayer;
        addPlayer(this.selfPlayer);
    }

    public Player getSelfPlayer() {
        return selfPlayer;
    }

    /**
     * Draw information about selected target.
     * @params g graphics context for drawing in.
     * @params d drawing panel dimenstions.
     */
    public void drawTargetInfo(Graphics g, Dimension d) {
        WUnit t = selfPlayer.getSelectedUnit();

        if (t != null) {
            g.drawString("deathAnim: " + t.deathAnimationDone(),
                         10,
                         d.height - 110);
            g.drawString("dead: " + t.dead(),
                         10,
                         d.height - 90);
            g.drawString("isMove: " + t.isMove(),
                         10,
                         d.height - 70);
            g.drawString(t.getNick() + " (" + t.getID() + ")",
                         10,
                         d.height - 50);
            g.drawString("HP: " + t.getHitPoints(),
                         10,
                         d.height - 30);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Units">
    public WUnit getUnit(long id) {
        for (WUnit u : units) {
            if (u.getID() == id) {
                return u;
            }
        }

        return null;
    }

    public LinkedBlockingQueue<WUnit> getUnits() {
        return units;
    }

    public void moveUnit(long id, int begX, int begY, int endX, int endY, long begTime) {
        WUnit u = getUnit(id);
        if (u != null) {
            u.move(begX, begY, endX, endY, begTime);
        }
    }

    public void asyncMoveUnit(long id, int begX, int begY, int endX, int endY, long begTime) {
        selfExecutor.add("moveUnit",
                         new Class[] { long.class, int.class, int.class, int.class, int.class, long.class },
                         new Object[] { id, begX, begY, endX, endY, begTime });
    }

    public ArrayList<WUnit> getYSortedUnits() {
        ArrayList<WUnit> tmp = new ArrayList<WUnit>(units);
        Collections.sort(tmp , aligner);
        return tmp;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Players">
    public void addPlayer(Player player) {
        units.add(player);
        players.add(player);
    }

    public void asyncAddPlayer(Player player) {
        selfExecutor.add("addPlayer", new Class[] { Player.class }, new Object[] { player });
    }

    public void delPlayer(long id) {
        Player p = getPlayer(id);
        if (p != null) {

            // Unselect player if it selected.
            for (Player curPlayer : players) {
                if (curPlayer.getSelectedUnit() != null && curPlayer.getSelectedUnit().equals(p)) {
                    curPlayer.unselectUnit();
                }
            }

            // Now we can safely remove him.
            players.remove(p);

            // Finally remove player from units list.
            for (WUnit curUnit : units) {
                if (curUnit.equals(p)) {
                    units.remove(curUnit);
                    return;
                }
            }
        }
    }

    public void asyncDelPlayer(long id) {
        selfExecutor.add("delPlayer", new Class[] { long.class }, new Object[] { id });
    }

    public Player getPlayer(long id) {
        for (Player p : players) {
            if (p.getID() == id) {
                return p;
            }
        }

        return null;
    }

    public LinkedBlockingQueue<Player> getPlayers() {
        return players;
    }

    /**
     * Draw players text cluds.
     * @params g graphics context for drawing in.
     * @params x x-axis of top left screen position on world map.
     * @params y y-axis of top left screen position on world map.
     */
    public void drawTextClouds(Graphics g, int x, int y) {
        for (Player p : players) {
            p.drawTextCloud(g, x, y);
        }
    }

    /**
     * Draw players movement track if he moves.
     * @params g graphics context for drawing in.
     * @params x x-axis of top left screen position on world map.
     * @params y y-axis of top left screen position on world map.
     */
    public void drawMoveTrack(Graphics g, int x, int y) {
        Point cur;

        for (Player p : players) {
            if (p.isMove()) {
                cur = p.getCurPos();
                g.drawLine(cur.x - x,
                           cur.y - y,
                           p.getEnd().x - x,
                           p.getEnd().y - y);
            }
        }
    }

    /**
     * Draw players visible range.
     * @params g graphics context for drawing in.
     * @params x x-axis of top left screen position on world map.
     * @params y y-axis of top left screen position on world map.
     */
    public void drawVisibleRange(Graphics g, int x, int y) {
        Point cur;

        for (Player p : players) {
            cur = p.getCurPos();
            g.drawOval(cur.x - x - 500,
                       cur.y - y - 500,
                       1000,
                       1000);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Nukes works">
    public void addNuke(WUnit self, WUnit selectedUnit, long begTime) {

        // Remove old nukes.
        for (Iterator<Nuke> l = nukes.iterator(); l.hasNext();) {
            if (!l.next().isMove()) {
                l.remove();
            }
        }

        Nuke n = self.getCurrentNuke();
        if (n != null) {
            n.use(begTime);
            nukes.add(n);
        } else {
            System.err.println("We try to add null!");
        }
    }

    public void asyncAddNuke(WUnit self, WUnit selectedUnit, long begTime) {
        selfExecutor.add("addNuke",
                         new Class[] { WUnit.class, WUnit.class, long.class },
                         new Object[] { self, selectedUnit, begTime });
    }

    public LinkedBlockingQueue<Nuke> getNukes() {
        return nukes;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Towers">
    public void addTower(Tower tower) {
        towers.add(tower);
        units.add(tower);
    }

    public void asyncAddTower(Tower tower) {
        selfExecutor.add("addTower", new Class[] { Tower.class }, new Object[] { tower });
    }

    public void delTower(long id) {
        Tower t = getTower(id);
        if (t != null) {
            towers.remove(t);
            units.remove(t);
        }
    }

    public void asyncDelTower(long id) {
        selfExecutor.add("delTower", new Class[] { long.class }, new Object[] { id });
    }

    public Tower getTower(long id) {
        for (Tower t : towers) {
            if (t.getID() == id) {
                return t;
            }
        }

        return null;
    }

    public LinkedBlockingQueue<Tower> getTowers() {
        return towers;
    }

    /**
     * Draw towers range.
     * @params g graphics context for drawing in.
     * @params x x-axis of top left screen position on world map.
     * @params y y-axis of top left screen position on world map.
     */
    public void drawTowersRange(Graphics g, int x, int y) {
        Point tPos;
        double range;

        for (Tower t : towers) {
            tPos = t.getCurPos();
            range = t.getRange();
            g.drawOval(tPos.x - x - (int) range,
                       tPos.y - y - (int) range,
                       (int) range * 2,
                       (int) range * 2);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Monsters">
    public void addMonster(Monster monster) {
        monsters.add(monster);
        units.add(monster);
    }

    public void asyncAddMonster(Monster monster) {
        selfExecutor.add("addMonster", new Class[] { Monster.class }, new Object[] { monster });
    }

    public void delMonster(long id) {
        Monster m = getMonster(id);
        if (m != null) {
            monsters.remove(m);
            units.remove(m);
        }
    }

    public void asyncDelMonster(long id) {
        selfExecutor.add("delMonster", new Class[] { long.class }, new Object[] { id });
    }

    void delDeadMonsters() {
        Monster m;

        for (Iterator<Monster> l = monsters.iterator(); l.hasNext();) {
            m = l.next();
            if (m.dead() && m.deathAnimationDone()) {
                units.remove(m);
                l.remove();
            }
        }
    }

    public LinkedBlockingQueue<Monster> getMonsters() {
        return monsters;
    }

    public Monster getMonster(long id) {
        for (Monster m : monsters) {
            if (m.getID() == id) {
                return m;
            }
        }

        return null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Hit animations">
    public void addHitAnimation(HitAnimation hitAnimation) {
        removeEndedHitAnimations();
        hitAnimations.add(hitAnimation);
    }

    public void asyncAddHitAnimation(HitAnimation hitAnimation) {
        selfExecutor.add("addHitAnimation", new Class[] { HitAnimation.class }, new Object[] { hitAnimation });
    }

    public LinkedBlockingQueue<HitAnimation> getHitAnimations() {
        return hitAnimations;
    }

    private void removeEndedHitAnimations() {
        // Remove old hits.
        for (Iterator<HitAnimation> l = hitAnimations.iterator(); l.hasNext();) {
            if (l.next().isDone()) {
                l.remove();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Tower Defence Status works">
    public String getTDStatus() {
        return tdStatus;
    }

    public void setTDStatus(String status) {
        tdStatus = status;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Map fragments">
    public ClientMapFragment getMapFragment(Point pos) {
        int x = pos.x / ClientMapFragment.getWidth();
        int y = pos.y / ClientMapFragment.getHeight();
        for (ClientMapFragment mf : mfagments) {
            if (mf.getIdx() == x && mf.getIdy() == y) {
                return mf;
            }
        }
        return null;
    }

    /**
     * Возвращает фрагмент карты, содержащий точку карты с координатами x, y.
     * @param x X точки карты.
     * @param y Y точки карты.
     * @return Фрагмент карты, содержащий точку карты с координатами x, y или
     * null, если такой фрагмент не найден.
     */
    public ClientMapFragment getMapFragmentContains(int x, int y) {
        int tmpX = x / ClientMapFragment.getWidth();
        int tmpY = y / ClientMapFragment.getHeight();
        for (ClientMapFragment mf : mfagments) {
            if (mf.getIdx() == tmpX && mf.getIdy() == tmpY) {
                return mf;
            }
        }
        return null;
    }

    /**
     * Returns neighbour map fragments count for defined screen resolution.
     * @return
     */
    public int getAroundFragCount(Dimension d) {
        return ((int) Math.max(d.width / ClientMapFragment.getWidth(), d.height / ClientMapFragment.getHeight())) + 1;
    }

    public ClientMapFragment getMapFragment(int idx, int idy) {
        for (ClientMapFragment mf : mfagments) {
            if (mf.getIdx() == idx && mf.getIdy() == idy) {
                return mf;
            }
        }
        return null;
    }

    public void addMapFragment(ClientMapFragment mf) {
        mfagments.add(mf);
    }

    public boolean availableCell(Point p) {
        return getMapFragment(p).availableCell(p);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Items">
    /**
     * Return items queue.
     * @return Items queue.
     */
    public LinkedBlockingQueue<WItem> getItems() {
        return items;
    }
    /**
     * Synchronously add item to items queue.
     * @param item New item to add to queue.
     */
    public void addItem(WItem item) {
        if (!items.contains(item)) {
            items.add(item);
        }
    }

    /**
     * Asynchronously add item to items queue.
     * @param item New item to add to queue.
     * @throws IllegalArgumentException argument item cannot be null!
     */
    public void asyncAddItem(WItem item) throws IllegalArgumentException {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add null in item queue!");
        }

        selfExecutor.add("addItem", new Class[]{ WItem.class }, new Object[]{ item });
    }
    /**
     * Synchronously remove item from items queue.
     * @param item Item to remove from queue.
     */
    public void removeItem(WItem item) {
        items.remove(item);
    }

    /**
     * Synchronously remove item from items queue.
     * @param item Item to remove from queue.
     * @throws IllegalArgumentException argument item cannot be null!
     */
    public void asyncRemoveItem(WItem item) throws IllegalArgumentException {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add null in item queue!");
        }

        selfExecutor.add("removeItem", new Class[]{ WItem.class }, new Object[]{ item });
    }

    /**
     * Check if x, y lay on some item sprite.
     */
    public WItem onItemSprite(int x, int y) {
        Polygon p;

        for (WItem i : items) {
            p = i.getDimensionOnWorld();
            if (p.contains(x, y)) {
                return i;
            }
        }

        return null;
    }

    public double distanceToItem(WItem i) {
        Polygon p = i.getDimensionOnWorld();
        Point cur = selfPlayer.getCurPos();
        return Point.distance(p.xpoints[0], p.ypoints[0], cur.x, cur.y);
    }
    // </editor-fold>

    private static class YAligner implements Comparator {

        @Override
        public final int compare(Object a, Object b) {
            return ((WUnit) a).getCurPos().y > ((WUnit) b).getCurPos().y ? 1 : 0;
        }
    }

    public void startSelfExecution() {
        new Thread(selfExecutor).start();
    }

    /**
     * Thread what invoke GameField methods asynchronously.
     */
    private class SelfExecutor implements Runnable {

        /**
         * Flag what cause thred to stop.
         */
        private boolean stoped = false;
        /**
         * Execution queue.
         */
        private final LinkedBlockingQueue<Invokable> q = new LinkedBlockingQueue<Invokable>();
        /**
         * GameField where method will be executed.
         */
        private GameField field;

        public SelfExecutor(GameField field) {
            this.field = field;
        }

        /**
         * Adds method to execution queue.
         * @param method Name of GameField's method.
         * @param paramTypes Types of method arguments.
         * @param args Params what will be passed to method.
         */
        public void add(String method, Class[] paramTypes, Object[] args) {
            try {
                Method m = field.getClass().getMethod(method, paramTypes);
                m.setAccessible(true);
                q.add(new Invokable(m, args));
                wakeup();
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
                System.exit(1);
            } catch (SecurityException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }

        /**
         * Thread function. Only one thread can be runned at time.
         */
        @Override
        public void run() {
            try {
                while (!stoped) {
                    while (q.isEmpty()) {
                        try {
                            synchronized (q) {
                                q.wait();
                            }
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                            System.exit(1);
                        }
                    }

                    Invokable i = q.remove();
                    i.getMethod().invoke(field, i.getArgs());
                    //System.out.println("I execute some method: " + i.getMethod().getName());
                }
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
                System.exit(1);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
                System.exit(1);
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }

        /**
         * Stop the thread.
         */
        public void stop() {
            stoped = true;
        }

        private void wakeup() {
            synchronized (q) {
                q.notify();
            }
        }

        private class Invokable {

            private Method method;
            private Object[] args;

            public Object[] getArgs() {
                return args;
            }

            public Method getMethod() {
                return method;
            }

            public Invokable(Method method, Object[] args) {
                this.method = method;
                this.args = args;
            }
        }
    }
}
