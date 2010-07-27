package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

public class GameField {
    private Player selfPlayer;
    private final ArrayList<Unit> units = new ArrayList<Unit>();
    private final ArrayList<Nuke> nukes = new ArrayList<Nuke>();

    public GameField() {}

    public Player getPlayer(long id) {
        synchronized (units) {
            for (ListIterator li = units.listIterator(); li.hasNext();) {
                Player p = (Player) li.next();
                if (p.getID() == id) {
                    return p;
                }
            }
        }

        return null;
    }

    public void addPlayer(Player player) {
        synchronized (units) {
            units.add(player);
        }
    }

    public void delPlayer(long id) {
        synchronized (units) {
            Player p = getPlayer(id);
            Unit u;
            units.remove(p);
            for (ListIterator<Unit> li = units.listIterator(); li.hasNext();) {
                u = li.next();
                if (u.getSelectedUnit().equals(p)) {
                    u.unselectUnit();
                }
            }
        }
    }

    public void addNPC(NPC npc) {
        synchronized (units) {
            units.add(npc);
        }
    }

    public ArrayList<Unit> getUnits() {
        return units;
    }

    public ArrayList<Unit> getYSortedUnits() {
        synchronized (units) {
            Collections.sort(units, new YAligner());
        }

        return units;
    }

    public Player getSelfPlayer() {
        return selfPlayer;
    }

    public void addSelfPlayer(Player selfPlayer) {
        this.selfPlayer = selfPlayer;
        synchronized (units) {
            units.add(this.selfPlayer);
        }
    }

    public void addNuke(Unit self, Unit selectedUnit, long begTime) {
        synchronized (nukes) {
            Nuke n = new PowerOfWateringPot();
            nukes.add(n);
            n.use(self, selectedUnit, begTime);
            self.setCurrentNuke(n);
        }
    }

    public ArrayList<Nuke> getNukes() {
        return nukes;
    }

    private class YAligner implements Comparator {
        public final int compare(Object a, Object b) {
            return ((Unit) a).getCurPos().y > ((Unit) b).getCurPos().y ? 1 : 0;
        }
    }
}
