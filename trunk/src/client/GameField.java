package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GameField {

    private Player selfPlayer;
    private final ArrayList<Unit> units = new ArrayList<Unit>();
    private final ArrayList<Player> players = new ArrayList<Player>();
    private final ArrayList<Nuke> nukes = new ArrayList<Nuke>();
    private final ArrayList<Tower> towers = new ArrayList<Tower>();
    private final ArrayList<Monster> monsters = new ArrayList<Monster>();
    private final ArrayList<HitAnimation> hitAnimations = new ArrayList<HitAnimation>();
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
    }

    // <editor-fold defaultstate="collapsed" desc="Self player works">
    public void addSelfPlayer(Player selfPlayer) {
        this.selfPlayer = selfPlayer;
        addPlayer(this.selfPlayer);
    }

    public Player getSelfPlayer() {
        return selfPlayer;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Units works">

    /**
     * Require lock all.
     */
    public Unit getUnit(long id) {
        for (Unit u : units) {
            if (u.getID() == id) {
                return u;
            }
        }

        return null;
    }

    public ArrayList<Unit> getUnits() {
        return units;
    }

    /**
     * Require lock all.
     */
    public ArrayList<Unit> getYSortedUnits() {
        Collections.sort(units, aligner);
        return units;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Players works">

    /**
     * Require lock all.
     */
    public void addPlayer(Player player) {
        debug_test1(Thread.currentThread().getStackTrace());
        units.add(player);
        players.add(player);
    }

    /**
     * Require lock all.
     */
    public void delPlayer(long id) {
        debug_test1(Thread.currentThread().getStackTrace());
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
            for (Unit curUnit : units) {
                if (curUnit.equals(p)) {
                    units.remove(curUnit);
                    return;
                }
            }
        }
    }

    /**
     * Require lock players.
     */
    public Player getPlayer(long id) {
        for (Player p : players) {
            if (p.getID() == id) {
                return p;
            }
        }

        return null;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Nukes works">

    /**
     * Require lock nukes.
     */
    public void addNuke(Unit self, Unit selectedUnit, long begTime) {

        // remove old nukes
        for (ListIterator<Nuke> l = nukes.listIterator(); l.hasNext();) {
            if (!l.next().isMove()) {
                l.remove();
            }
        }

        Nuke n = self.getCurrentNuke();
        n.use(begTime);
        nukes.add(n);
    }

    public ArrayList<Nuke> getNukes() {
        return nukes;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Towers works">

    /**
     * Require lock all.
     */
    public void addTower(Tower tower) {
        debug_test1(Thread.currentThread().getStackTrace());
        towers.add(tower);
        units.add(tower);
    }

    /**
     * Require lock all.
     */
    public void delTower(long id) {
        debug_test1(Thread.currentThread().getStackTrace());
        Tower t = getTower(id);
        if (t != null) {
            towers.remove(t);
            units.remove(t);
        }
    }

    /**
     * Require lock towers.
     */
    public Tower getTower(long id) {
        for (Tower t : towers) {
            if (t.getID() == id) {
                return t;
            }
        }

        return null;
    }

    public ArrayList<Tower> getTowers() {
        return towers;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Monsters works">

    /**
     * Require lock all.
     */
    public void addMonster(Monster monster) {
        debug_test1(Thread.currentThread().getStackTrace());
        monsters.add(monster);
        units.add(monster);
    }

    /**
     * Require lock all.
     */
    void delDeadMonsters() {
        debug_test1(Thread.currentThread().getStackTrace());
        Monster m;

        for (ListIterator<Monster> l = monsters.listIterator(); l.hasNext();) {
            m = l.next();
            if (m.isDead() && m.deathAnimationDone()) {
                units.remove(m);
                l.remove();
            }
        }
    }

    public ArrayList<Monster> getMonsters() {
        return monsters;
    }

    /**
     * Require monsters lock.
     */
    public Monster getMonster(long id) {
        for (Monster m : monsters) {
            if (m.getID() == id) {
                return m;
            }
        }

        return null;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Hit animations works">

    /**
     * Require hit animation lock.
     */
    public void addHitAnimation(HitAnimation hitAnimation) {

        // Remove old hits.
        for (ListIterator<HitAnimation> l = hitAnimations.listIterator(); l.hasNext();) {
            if (l.next().isDone()) {
                l.remove();
            }
        }

        hitAnimations.add(hitAnimation);
    }

    public ArrayList<HitAnimation> getHitAnimations() {
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

    private static class YAligner implements Comparator {

        @Override
        public final int compare(Object a, Object b) {
            return ((Unit) a).getCurPos().y > ((Unit) b).getCurPos().y ? 1 : 0;
        }
    }

    private void debug_test1(StackTraceElement[] trace) {
        if (WanderingJPanel.resourcesInProcess && Thread.currentThread().getId() != WanderingJPanel.threadId) {
            System.err.println("Some idiot try to bad thing!");
            for (StackTraceElement e : trace) {
                System.err.println(e.getClassName() + ":" + e.getMethodName() + ":" + e.getFileName() + ":" + e.getLineNumber());
            }
        }

    }
}