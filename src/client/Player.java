package client;

import common.Inventory;
import common.InventoryException;
import common.WanderingServerTime;
import common.items.Item;
import common.items.Etc;
import common.items.Items;
import java.util.ArrayList;

public class Player extends WUnit {

    private final Inventory inventory = new Inventory();

    public Player(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
        super(id, nick, maxHitPoints, speed, x, y, d, set);
        hitPoints = maxHitPoints;
        deathAnim = new DeathAnimation("peasant");
        //pickupAnim = new PickupAnimation(set);
    }

    // <editor-fold defaultstate="collapsed" desc="Position.">
    public void teleportTo(int x, int y) {
        move(x, y, x, y, System.currentTimeMillis() - ServerInteraction.serverStartTime);
    }

    public void teleportToSpawn() {
        move(0, 0, 0, 0, System.currentTimeMillis() - ServerInteraction.serverStartTime);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Hit points.">
    @Override
    public boolean dead() {
        return hitPoints <= 0;
    }

    public void doHeal(int val) {
        hitPoints += val;
    }

    @Override
    public void doHit(int dmg) {
        hitPoints -= dmg;
        if (hitPoints < 0) {
            hitPoints = 0;
        }
    }

    @Override
    public void kill() {
        Direction d = Direction.SOUTH;

        if (moveAnim.getDirection() == null) {
            d = moveAnim.getDirection();
        }
        if (standAnim.getDirection() == null) {
            d = standAnim.getDirection();
        }
        deathAnim.run(d, WanderingServerTime.getInstance().getTimeSinceStart());
        mv.stop();
    }

    public void restoreHitPoints() {
        hitPoints = maxHitPoints;
    }

    public void resurect() {
        hitPoints = maxHitPoints;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Etc items.">
    /**
     * Returns etc item by id.
     * @param id id of etc item.
     * @return etc item or <b>null</b> if etc item not found.
     */
    public Etc getEtc(long id) {
        return inventory.getEtc(id);
    }

    /**
     * Returns list of etc items by type.
     * @param type type of etc items.
     * @return list of etc items. List is empty if there is no items of this
     * type on inventory.
     */
    public ArrayList<Etc> getEtc(Items type) {
        return inventory.getEtc(type);
    }

    /**
     * Adds etc item to inventory.
     * @param item etc item to add.
     * @return return <b>true</b> if new item added to inventory, <b>false</b>
     * otherwise.
     */
    public boolean addEtc(Etc item) {
        return inventory.addEtc(item);
    }

    /**
     * Removes etc item from inventory.
     * @param item etc item to remove.
     */
    public void removeEtc(Etc item) throws InventoryException {
        inventory.removeEtc(item);
    }

    /**
     * Use etc item from inventory.
     * @param item etc item to use.
     */
    public void useEtc(Etc item) {
        // TODO What happen when we use etc item?
    }

    /**
     * Returns gold count.
     * @return gold count. If there is no gold in inventory, return 0.
     */
    public int getGoldCount() {
        int count = 0;

        ArrayList<Etc> etc = inventory.getEtc(Items.GOLD);
        if (!etc.isEmpty()) {
            count += etc.get(0).getCount();
        }

        return count;
    }
    // </editor-fold>

    /**
     * Pickup item <i>i</i>.
     * @param i item.
     */
    public void pickup(Item i) {
        if (pickupAnim != null) {
            // play pickup animation here.
        }
    }
}
