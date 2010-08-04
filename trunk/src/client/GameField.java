package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GameField {

    private Player selfPlayer;
    private final ArrayList<Unit> units = new ArrayList<Unit>();
    private final ArrayList<Player> players = new ArrayList<Player>();
    private final ArrayList<Nuke> nukes = new ArrayList<Nuke>();
    private final ArrayList<Tower> towers = new ArrayList<Tower>();
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

    public Unit getUnit(long id) {
        synchronized (units) {
            for (Unit u : units) {
                if (u.getID() == id) {
                    return u;
                }
            }
        }

        return null;
    }

    public ArrayList<Unit> getUnits() {
        return units;
    }

    public ArrayList<Unit> getYSortedUnits() {
        synchronized (units) {
            Collections.sort(units, aligner);
        }

        return units;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Players works">

    public void addPlayer(Player player) {
        synchronized (units) {
            units.add(player);
        }
        synchronized (players) {
            players.add(player);
        }
    }

    public void delPlayer(long id) {
        Player p = getPlayer(id);
        if (p != null) {
            synchronized (players) {

                // Unselect player if it selected.
                for (Player curPlayer : players) {
                    if (curPlayer.getSelectedUnit() != null && curPlayer.getSelectedUnit().equals(p)) {
                        curPlayer.unselectUnit();
                    }
                }

                // Now we can safely remove him.
                players.remove(p);
            }
            synchronized (units) {

                // Finally remove player from units list.
                for (Unit curUnit : units) {
                    if (curUnit.equals(p)) {
                        units.remove(curUnit);
                        return;
                    }
                }
            }
        }
    }

    public Player getPlayer(long id) {
        synchronized (players) {
            for (Player p : players) {
                if (p.getID() == id) {
                    return p;
                }
            }
        }

        return null;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Nukes works">

    public void addNuke(Unit self, Unit selectedUnit, long begTime) {
        Nuke n;

        synchronized (nukes) {
            n = self.getCurrentNuke();//new PowerOfWateringPot(self);
            //n.use(selectedUnit, begTime);
            //self.setCurrentNuke(n);
            n.use(begTime);
            nukes.add(n);
        }
    }

    public ArrayList<Nuke> getNukes() {
        return nukes;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Towers works">

    public void addTower(Tower tower) {
        synchronized (towers) {
            towers.add(tower);
        }
        synchronized (units) {
            units.add(tower);
        }
    }

    public void delTower(long id) {
        Tower t = getTower(id);
        if (t != null) {
            synchronized (towers) {
                towers.remove(t);
            }
            synchronized (units) {

                // Remove tower from units list.
                for (Unit curUnit : units) {
                    if (curUnit.equals(t)) {
                        units.remove(curUnit);
                        return;
                    }
                }
            }
        }
    }

    public Tower getTower(long id) {
        synchronized (towers) {
            for (Tower t : towers) {
                if (t.getID() == id) {
                    return t;
                }
            }
        }

        return null;
    }

    public ArrayList<Tower> getTowers() {
        return towers;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Tower Defence Status works">

    public String getTDStatus() {
        return tdStatus;
    }

    public void setTDStatus(String status) {
        tdStatus = status;
    }

    public void deathPlayer(long id) {
        
    }
    // </editor-fold>

    private static class YAligner implements Comparator {

        public final int compare(Object a, Object b) {
            return ((Unit) a).getCurPos().y > ((Unit) b).getCurPos().y ? 1 : 0;
        }
    }
}
