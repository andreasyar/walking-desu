package server.javatestserver;

//import newcommon.Inventory;
//import newcommon.exceptions.InventoryException;
//import newcommon.items.Items;
//import newcommon.items.Item;
//import newcommon.items.Etc;
//import java.awt.Point;
//import java.util.ArrayList;
//
//public class Player extends JTSUnit {
//
//    /**
//     * List of visible units.
//     */
//    private final ArrayList<JTSUnit> visibleUnits = new ArrayList<JTSUnit>();
//    /**
//     * List of visible items.
//     */
//    private final ArrayList<Item> visibleItems = new ArrayList<Item>();
//    /**
//     * Visible range.
//     */
//    private static final double visibleRange = 500.0;
//    /**
//     * Inventory.
//     */
//    private final Inventory inventory = new Inventory();
//
//    public Player(long id, String nick, int maxHitPoints, int x, int y, double speed) {
//        super(id, nick, maxHitPoints, x, y, speed);
//        hitPoints = maxHitPoints;
//        damage = 50;
//    }
//
//    // <editor-fold defaultstate="collapsed" desc="Position.">
//    public void teleportTo(int x, int y) {
//        move(x, y, x, y, System.currentTimeMillis() - JavaTestServer.serverStartTime);
//    }
//
//    public void teleportToSpawn() {
//        move(0, 0, 0, 0, System.currentTimeMillis() - JavaTestServer.serverStartTime);
//    }
//    // </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="Hit points.">
//    @Override
//    public boolean dead() {
//        return hitPoints <= 0;
//    }
//
//    @Override
//    public void doHit(int dmg) {
//        hitPoints -= dmg;
//        if (hitPoints < 0) {
//            hitPoints = 0;
//        }
//    }
//
//    @Override
//    public void kill() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public void restoreHitPoints() {
//        hitPoints = maxHitPoints;
//    }
//    // </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="Visible units.">
//    public void addVisibleUnit(JTSUnit unit) {
//        synchronized (visibleUnits) {
//            visibleUnits.add(unit);
//        }
//    }
//
//    public void delVisibleUnit(JTSUnit unit) {
//        synchronized (visibleUnits) {
//            while (visibleUnits.remove(unit)) {
//            }
//        }
//    }
//
//    public ArrayList<JTSUnit> getVisibleUnitsList() {
//        return visibleUnits;
//    }
//
//    public boolean inRange(JTSUnit unit) {
//        if (getCurPos().distance(unit.getCurPos()) <= visibleRange) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public boolean see(JTSUnit unit) {
//        return visibleUnits.contains(unit);
//    }
//    // </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="Visible items.">
//    /**
//     * Adds item <i>i</i> to list of visible items if it not contain this item
//     * yet.
//     * @param i item to add to list of visible items.
//     * @return if list of visible items already contains item <i>i</i> return
//     * false, otherwise return true.
//     */
//    public boolean addVisibleItem(Item i) {
//        synchronized (visibleItems) {
//            if (visibleItems.contains(i)) {
//                return false;
//            } else {
//                visibleItems.add(i);
//                return true;
//            }
//        }
//    }
//
//    /**
//     * Deletes item <i>i</i> from list of visible items.
//     * @param i item to delete from list of visible items.
//     */
//    public boolean delVisibleItem(Item i) {
//        synchronized (visibleItems) {
//            return visibleItems.remove(i);
//        }
//    }
//
//    /**
//     * Checks what item <i>item</i> is in visible range.
//     * @param item item to check is it in visible range.
//     * @return return TRUE if item <i>item</i> is in visible range, FALSE
//     * otherwise.
//     */
//    public boolean inRange(Item item) {
//        if (getCurPos().distance(new Point(item.getX(), item.getY())) <= visibleRange) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    /**
//     * Check what item <i>items</i> is in list of visible items.
//     * @param item item to check is it in list of visible items.
//     * @return TRUE if item <i>item</i> is in list of visible items, FALSE
//     * otherwise.
//     */
//    public boolean see(Item item) {
//        return visibleItems.contains(item);
//    }
//    // </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="Etc items.">
//    /**
//     * Returns etc item by id.
//     * @param id id of etc item.
//     * @return etc item or <b>null</b> if etc item not found.
//     */
//    public Etc getEtc(long id) {
//        return inventory.getEtc(id);
//    }
//
//    /**
//     * Returns list of etc items by type.
//     * @param type type of etc items.
//     * @return list of etc items. List is empty if there is no items of this
//     * type on inventory.
//     */
//    public ArrayList<Etc> getEtc(Items type) {
//        return inventory.getEtc(type);
//    }
//
//    /**
//     * Adds etc item to inventory.
//     * @param item etc item to add.
//     * @return return <b>true</b> if new item added to inventory, <b>false</b>
//     * otherwise.
//     */
//    public boolean addEtc(Etc item) {
//        return inventory.addEtc(item);
//    }
//
//    /**
//     * Removes etc item from inventory.
//     * @param item etc item to remove.
//     */
//    public void removeEtc(Etc item) throws InventoryException {
//        inventory.removeEtc(item);
//    }
//
//    /**
//     * Use etc item from inventory.
//     * @param item etc item to use.
//     */
//    public void useEtc(Etc item) {
//        // TODO What happen when we use etc item?
//    }
//
//    /**
//     * Returns gold count.
//     * @return gold count. If there is no gold in inventory, return 0.
//     */
//    public int getGoldCount() {
//        int count = 0;
//
//        ArrayList<Etc> etc = inventory.getEtc(Items.GOLD);
//        if (etc != null) {
//            count += etc.get(0).getCount();
//        }
//
//        return count;
//    }
//    // </editor-fold>
//}
