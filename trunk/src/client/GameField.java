package client;

import client.items.Item;
import common.messages.Pickup;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class GameField {

    private static int debugLevel = 0;

    public static double visibleDistance = 500.0;
    public static double pickupDistance = 50.0;

    private Player selfPlayer;
    private PickupTask pickupTask;
    private final LinkedBlockingQueue<WUnit> units = new LinkedBlockingQueue<WUnit>();
    private final LinkedBlockingQueue<Player> players = new LinkedBlockingQueue<Player>();
    private final LinkedBlockingQueue<Nuke> nukes = new LinkedBlockingQueue<Nuke>();
    private final LinkedBlockingQueue<Tower> towers = new LinkedBlockingQueue<Tower>();
    private final LinkedBlockingQueue<Monster> monsters = new LinkedBlockingQueue<Monster>();
    private final LinkedBlockingQueue<HitAnimation> hitAnimations = new LinkedBlockingQueue<HitAnimation>();
    private final LinkedBlockingQueue<ClientMapFragment> mfagments = new LinkedBlockingQueue<ClientMapFragment>();
    /**
     * Items layed on the ground.
     */
    private final LinkedBlockingQueue<Item> items = new LinkedBlockingQueue<Item>();
    /**
     * Drawable units. It can be unit, item, nuke - anything what implemets
     * drawable interface.
     */
    private final LinkedBlockingQueue<Drawable> drawqueue = new LinkedBlockingQueue<Drawable>();

    private SelfExecutor selfExecutor;
    /**
     * Tower Defence mini game status: N/M xS Where N - monsters loss, M -
     * monsters loss limit, S - strength multiplyer.
     */
    private String tdStatus = null;
    //private static YAligner aligner = new YAligner();
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

//    /**
//     * Draws units, items, nukes, hit animation.
//     * @params g graphics context for drawing in.
//     * @params x x-axis of top left screen position on world map.
//     * @params y y-axis of top left screen position on world map.
//     */
//    public void drawAll(Graphics g, int x, int y, Dimension dim) {
//
//        for (Drawable d : units) {
//            if (d != null) {
//                d.draw(g, x, y, dim);
//            }
//            else {
//                System.err.println("WDrawable unit is null. LOL? It cannot be!");
//                continue;
//            }
//        }
//
//        for (Drawable d : nukes) {
//            if (d != null) {
//                d.draw(g, x, y, dim);
//            }
//            else {
//                System.err.println("Nuke is null. LOL? It cannot be!");
//                continue;
//            }
//        }
//
//        for (Drawable d : hitAnimations) {
//            if (d != null) {
//                d.draw(g, x, y, dim);
//            }
//            else {
//                System.err.println("Hit animation is null. LOL? It cannot be!");
//                continue;
//            }
//        }
//
//        for (Drawable d : items) {
//            if (d != null) {
//                d.draw(g, x, y, dim);
//            }
//            else {
//                System.err.println("Items is null. LOL? It cannot be!");
//                continue;
//            }
//        }
//    }

    /**
     * Fills the z-buffer for drawing sprites in proper order by y-axys
     * accedering.
     * @param zbuffer z-buffer of sprites.
     * @param x x-axys of left top drawing panel corner in world.
     * @param y y-axys of left top drawing panel corner in world.
     * @param dim dimensions of drawing panel.
     */
    public void updateZBuffer(ArrayList<ArrayList<Sprite>> zbuffer, int x, int y, Dimension dim) {
        Sprite s;

        synchronized (zbuffer) {
            for (Drawable d : drawqueue) {
                if (d != null) {
                    s = d.getSprite();
                    if (s.y - y >= 0 && s.y - y < zbuffer.size()
                            && s.x - x >= 0 && s.x - x <= dim.getWidth()) {

                        zbuffer.get(s.y - y).add(s);
                    }
                }
                else {
                    System.err.println("Drawable unit is null. It cannot be! Skip it.");
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

    /**
     * Draws gold coins count in clients inventory.
     * @params g graphics context for drawing in.
     * @params d drawing panel dimenstions.
     */
    public void drawGoldCoinCount(Graphics g, Dimension d) {
        if (selfPlayer != null) {
            g.drawString("Gold coins: " + selfPlayer.getGoldCount(),
                         100,
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
     * @return items queue.
     */
    public LinkedBlockingQueue<Item> getItems() {
        return items;
    }

    /**
     * Synchronously add item to items queue.
     * @param item new item to add to queue.
     */
    public void addItem(Item item) {
        if (!items.contains(item)) {
            items.add(item);
        }
    }

    /**
     * Asynchronously add item to items queue.
     * @param item new item to add to queue.
     * @throws IllegalArgumentException argument <i>item</i> cannot be null!
     */
    public void asyncAddItem(Item item) throws IllegalArgumentException {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add null in item queue!");
        }

        selfExecutor.add("addItem", new Class[]{ Item.class }, new Object[]{ item });
    }

    /**
     * Synchronously remove item from items queue.
     * @param item item to remove from queue.
     */
    public void removeItem(Item item) {
        items.remove(item);
    }

    /**
     * Synchronously remove item from items queue.
     * @param item item to remove from queue.
     * @throws IllegalArgumentException argument item cannot be null!
     */
    public void asyncRemoveItem(Item item) throws IllegalArgumentException {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add null in item queue!");
        }

        selfExecutor.add("removeItem", new Class[]{ Item.class }, new Object[]{ item });
    }

    /**
     * Returns item if dot [<i>x</i>, <i>y</i>] lay in some item bounds, NULL
     * otherwise.
     * @param x x-axys of dot.
     * @param y y-axys of dot.
     */
    public Item selectItem(int x, int y) {
        for (Item i : items) {
            if (i.onItem(x, y)) {
                return i;
            }
        }

        return null;
    }

    /**
     * Calculate and returns distace from self player to item <i>item</i>.
     * @param item item.
     */
    public double distanceToItem(Item item) {
        Point cur = selfPlayer.getCurPos();
        return Point.distance(item.getX(), item.getY(), cur.x, cur.y);
    }

    /**
     * Cause self player to pickup <i>item</i>.
     * @param item item to pickup.
     * @param inter server interaction instance to report server about pickup
     * try.
     */
    public void pickupItem(Item item, ServerInteraction inter) {

        // <editor-fold defaultstate="collapsed" desc="debug">
        if (debugLevel > 0) {
            if (item == null) {
                System.out.println("We try to pickup far NULL item!");
            }
            System.out.println("We try to pickup item " + item.getID() + ".");
        }// </editor-fold>
        if (pickupTask != null) {

            // <editor-fold defaultstate="collapsed" desc="debug">
            if (debugLevel > 0) {
                System.out.println("Pickup item " + pickupTask.getItemID() + " process already in progress. Stop it.");
            }// </editor-fold>
            pickupTask.stop();
        }
        // <editor-fold defaultstate="collapsed" desc="debug">
        if (debugLevel > 0) {
            System.out.println("Create and start new pickup process.");
        }// </editor-fold>
        pickupTask = new PickupTask(item, inter);
        new Thread(pickupTask).start();
    }

    /**
     * Detect if pickup in progress.
     */
    public boolean isPickup() {
        return pickupTask != null;
    }

    /**
     * Stop pickup process.
     */
    public void stopPickup() {
        if (pickupTask != null) {
            pickupTask.stop();
            selfPlayer.unselectItem();
        }
    }

    /**
     * Returns nearest item if it exist or NULL if not.
     * @return nearest item or NULL.
     */
    public Item getNearestItem() {
        Item nearest = null;
        double nearestDistance = -1.0;
        double curDistance = 0.0;

        for (Item i : items) {
            curDistance = distanceToItem(i);

            if (nearestDistance < 0.0 || curDistance < nearestDistance) {
                nearestDistance = curDistance;
                nearest = i;
            }
        }

        return nearest;
    }

    /**
     * Returns item by id or NULL if no item of this id layed on the ground.
     * @param id id of serched item.
     * @return founded item or NULL.
     */
    public Item getItem(long itemID) {
        for (Item item : items) {
            if (item.getID() == itemID) {
                return item;
            }
        }

        return null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Drawqueue.">
    /**
     * Return draw queue.
     * @return draw queue.
     */
    public LinkedBlockingQueue<Drawable> getDrawQueue() {
        return drawqueue;
    }

    /**
     * Synchronously add drawable unit to draw queue.
     * @param d new drawable unit to add to queue.
     */
    public void addDrawable(Drawable d) {
        if (!drawqueue.contains(d)) {
            drawqueue.add(d);
        }
    }

    /**
     * Asynchronously add drawable unit to draw queue.
     * @param d new drawable unit to add to queue.
     * @throws IllegalArgumentException argument <i>d</i> cannot be null!
     */
    public void asyncAddDrawable(Drawable d) throws IllegalArgumentException {
        if (d == null) {
            throw new IllegalArgumentException("Cannot add null in draw queue!");
        }

        selfExecutor.add("addDrawable", new Class[]{ Drawable.class }, new Object[]{ d });
    }

    /**
     * Synchronously remove drawable unit from draw queue.
     * @param d drawable unit to remove from draw queue.
     */
    public void removeDrawable(Drawable d) {
        drawqueue.remove(d);
    }

    /**
     * Asynchronously remove drawable unit from draw queue.
     * @param d drawable unit to remove from draw queue.
     * @throws IllegalArgumentException argument d cannot be null!
     */
    public void asyncRemoveDrawable(Drawable d) throws IllegalArgumentException {
        if (d == null) {
            throw new IllegalArgumentException("Cannot add drawable unit in item queue!");
        }

        selfExecutor.add("removeDrawable", new Class[]{ Drawable.class }, new Object[]{ d });
    }
    // </editor-fold>

//    private static class YAligner implements Comparator {
//
//        @Override
//        public final int compare(Object a, Object b) {
//            return ((WUnit) a).getCurPos().y > ((WUnit) b).getCurPos().y ? 1 : 0;
//        }
//    }

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
                //m.setAccessible(true);
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

    private class PickupTask implements Runnable {

        private final Item item;
        private final ServerInteraction inter;
        private boolean stopped = false;

        public PickupTask(Item item, ServerInteraction inter) throws IllegalArgumentException {
            if (item == null) {
                throw new IllegalArgumentException("Item cannot be null!");
            }
            if (inter == null) {
                throw new IllegalArgumentException("Server interaction cannot be null!");
            }

            this.item = item;
            this.inter = inter;
        }

        public void stop() {
            stopped = true;
        }

        @Override
        public void run() {
            double distance;
            stopped = false;
            Point cur = selfPlayer.getCurPos();

            try {
                while (!stopped && (distance = Point.distance(item.getX(), item.getY(), cur.x, cur.y)) > 10) {

                    // <editor-fold defaultstate="collapsed" desc="debug">
                    if (debugLevel > 0) {
                        System.out.println("Item " + getItemID() + " still far: " + distance + ".");
                    }// </editor-fold>
                    try {
                        synchronized (this) {
                            wait(500L);
                        }
                    } catch (InterruptedException ex) {

                        // <editor-fold defaultstate="collapsed" desc="debug">
                        if (debugLevel > 0) {
                            System.out.println("We was interrupted.");
                        }// </editor-fold>
                        if ((distance = Point.distance(item.getX(), item.getY(), cur.x, cur.y)) > 10) {
                            asyncRemoveItem(item);
                            inter.addCommand(new Pickup(selfPlayer.getID(), item.getID()));
                            selfPlayer.unselectItem(item);

                            // <editor-fold defaultstate="collapsed" desc="debug">
                            if (debugLevel > 0) {
                                System.out.println("We pickup far item " + getItemID() + ".");
                            }// </editor-fold>
                        }
                        return;
                    }

                    // Update current player position.
                    cur = selfPlayer.getCurPos();
                }

                if (!stopped) {
                    asyncRemoveItem(item);
                    inter.addCommand(new Pickup(selfPlayer.getID(), item.getID()));
                    selfPlayer.unselectItem(item);

                    // <editor-fold defaultstate="collapsed" desc="debug">
                    if (debugLevel > 0) {
                        System.out.println("We pickup far item " + getItemID() + ".");
                    }// </editor-fold>
                } else {
                    selfPlayer.unselectItem(item);

                    // <editor-fold defaultstate="collapsed" desc="debug">
                    if (debugLevel > 0) {
                        System.out.println("We was stopped.");
                    }// </editor-fold>
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }

        private long getItemID() {
            return item.getID();
        }
    }
}
