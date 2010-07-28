package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

public class GameField {
    private Player selfPlayer;
    private final ArrayList<Unit> units = new ArrayList<Unit>();
    private final ArrayList<Nuke> nukes = new ArrayList<Nuke>();
    private final ArrayList<Tower> towers = new ArrayList<Tower>();

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
            try {
                Player p = getPlayer(id);
                if (p != null) {
                    Unit u;
                    for (ListIterator<Unit> li = units.listIterator(); li.hasNext();) {
                        u = li.next();
                        if (u.getSelectedUnit() != null && u.getSelectedUnit().equals(p)) {
                            u.unselectUnit();
                        }
                    }
                    units.remove(p);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
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
            Nuke n = new PowerOfWateringPot(self);
            n.use(selectedUnit, begTime);
            self.setCurrentNuke(n);
            nukes.add(n);
        }
    }

    public ArrayList<Nuke> getNukes() {
        return nukes;
    }

    public void addTower(Tower tower) {
        synchronized (towers) {
            towers.add(tower);
        }
    }

    public Tower getTower(long id) {
        synchronized (towers) {
            for (ListIterator li = towers.listIterator(); li.hasNext();) {
                Tower t = (Tower) li.next();
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

    public Unit getUnit(long id) {
        synchronized (units) {
            for (ListIterator<Unit> li = units.listIterator(); li.hasNext();) {
                Unit p = li.next();
                if (p.getID() == id) {
                    return p;
                }
            }
        }

        synchronized (towers) {
            for (ListIterator<Tower> li = towers.listIterator(); li.hasNext();) {
                Tower t = li.next();
                if (t.getID() == id) {
                    return t;
                }
            }
        }

        return null;
    }

    private class YAligner implements Comparator {
        public final int compare(Object a, Object b) {
            return ((Unit) a).getCurPos().y > ((Unit) b).getCurPos().y ? 1 : 0;
        }
    }
}
