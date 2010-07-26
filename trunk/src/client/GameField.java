package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

public class GameField {
    private Player selfPlayer;
    private final ArrayList<Unit> units = new ArrayList<Unit>();

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
            units.remove(getPlayer(id));
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

    private class YAligner implements Comparator {
        public final int compare(Object a, Object b) {
            return ((Unit) a).getCurPos().y > ((Unit) b).getCurPos().y ? 1 : 0;
        }
    }
}
