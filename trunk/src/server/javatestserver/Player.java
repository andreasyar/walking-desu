package server.javatestserver;

import common.Inventory;
import client.items.Item;
import java.awt.Point;
import java.util.ArrayList;

public class Player extends JTSUnit {

    private final ArrayList<JTSUnit> visibleUnits = new ArrayList<JTSUnit>();
    /**
     * List of visible items.
     */
    private final ArrayList<Item> visibleItems = new ArrayList<Item>();
    private static final double visibleRange = 500.0;
    private final Inventory inventory = new Inventory();

    public Player(long id, String nick, int maxHitPoints, int x, int y, double speed) {
        super(id, nick, maxHitPoints, x, y, speed);
        hitPoints = maxHitPoints;
        damage = 50;
    }

    @Override
    public void doHit(int dmg) {
        hitPoints -= dmg;
        if (hitPoints < 0) {
            hitPoints = 0;
        }
    }

    @Override
    public boolean dead() {
        return hitPoints <= 0;
    }

    public void restoreHitPoints() {
        hitPoints = maxHitPoints;
    }

    public void teleportToSpawn() {
        move(0, 0, 0, 0, System.currentTimeMillis() - JavaTestServer.serverStartTime);
    }

    public void teleportTo(int x, int y) {
        move(x, y, x, y, System.currentTimeMillis() - JavaTestServer.serverStartTime);
    }

    // <editor-fold defaultstate="collapsed" desc="Visible units works">
    public void addVisibleUnit(JTSUnit unit) {
        synchronized (visibleUnits) {
            visibleUnits.add(unit);
        }
    }

    /**
     * Adds item <i>i</i> to list of visible items if it not contain this item
     * yet.
     * @param i item to add to list of visible items.
     * @return if list of visible items already contains item <i>i</i> return
     * false, otherwise return true.
     */
    public boolean addVisibleItem(Item i) {
        synchronized (visibleItems) {
            if (visibleItems.contains(i)) {
                return false;
            } else {
                visibleItems.add(i);
                return true;
            }
        }
    }

    public void delVisibleUnit(JTSUnit unit) {
        synchronized (visibleUnits) {
            while (visibleUnits.remove(unit)) {
            }
        }
    }

    /**
     * Deletes item <i>i</i> from list of visible items.
     * @param i item to delete from list of visible items.
     */
    public void delVisibleItem(Item i) {
        synchronized (visibleItems) {
            while (visibleItems.remove(i)) {}
        }
    }

    public ArrayList<JTSUnit> getVisibleUnitsList() {
        return visibleUnits;
    }

    public boolean inRange(JTSUnit unit) {
        if (getCurPos().distance(unit.getCurPos()) <= visibleRange) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks what item <i>item</i> is in visible range.
     * @param item item to check is it in visible range.
     * @return return TRUE if item <i>item</i> is in visible range, FALSE
     * otherwise.
     */
    public boolean inRange(Item item) {
        if (getCurPos().distance(new Point(item.getX(), item.getY())) <= visibleRange) {
            return true;
        } else {
            return false;
        }
    }

    public boolean see(JTSUnit unit) {
        return visibleUnits.contains(unit);
    }

    /**
     * Check what item <i>items</i> is in list of visible items.
     * @param item item to check is it in list of visible items.
     * @return TRUE if item <i>item</i> is in list of visible items, FALSE
     * otherwise.
     */
    public boolean see(Item item) {
        return visibleItems.contains(item);
    }
    // </editor-fold>

    @Override
    public void kill() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Adds item to inventory.
     * @param item item to add.
     */
    public void addItemToInventory(Item item) {
        item.addToInventory(getInventory());
    }

    /**
     * Removes item from inventory by item <i>id</i>.
     * @param id item id.
     */
    public void removeItemFromInventory(long id) {
        getInventory().removeById(id);
    }

    /**
     * Removes item from inventory.
     * @param item item.
     */
    void removeItemFromInventory(Item item) {
        item.removeFromInventory(getInventory());
    }

    /**
     * Returns players inventory.
     * @param players inventory.
     */
    public Inventory getInventory() {
        return inventory;
    }
}
