package client;

import java.awt.Dimension;
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

    // <editor-fold defaultstate="collapsed" desc="Self player">
    public void addSelfPlayer(Player selfPlayer) {
        this.selfPlayer = selfPlayer;
        addPlayer(this.selfPlayer);
    }

    public Player getSelfPlayer() {
        return selfPlayer;
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

        // Remove old hits.
        for (Iterator<HitAnimation> l = hitAnimations.iterator(); l.hasNext();) {
            if (l.next().isDone()) {
                l.remove();
            }
        }

        hitAnimations.add(hitAnimation);
    }

    public void asyncAddHitAnimation(HitAnimation hitAnimation) {
        selfExecutor.add("addHitAnimation", new Class[] { HitAnimation.class }, new Object[] { hitAnimation });
    }

    public LinkedBlockingQueue<HitAnimation> getHitAnimations() {
        return hitAnimations;
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
