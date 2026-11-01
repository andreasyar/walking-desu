package client;

import client.items.ClientEtc;
import common.Inventory;
import common.items.Item;
import common.items.Etc;
import common.items.Items;
import java.util.ArrayList;

public class Player extends WUnit {

    private PickupAnimation pickupAnim;

    private final Inventory inventory = new Inventory();

    public Player(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
        super(id, nick, maxHitPoints, speed, x, y, d, set);
        hitPoints = maxHitPoints;
        deathAnim = new PlayerDeathAnimation("peasant");
        //pickupAnim = new PickupAnimation(set);
    }

    @Override
    public void doHit(int dmg) {
        hitPoints -= dmg;
        if (hitPoints < 0) {
            hitPoints = 0;
        }
    }

    public void restoreHitPoints() {
        hitPoints = maxHitPoints;
    }

    public void teleportToSpawn() {
        move(0, 0, 0, 0, System.currentTimeMillis() - ServerInteraction.serverStartTime);
    }

    public void teleportTo(int x, int y) {
        move(x, y, x, y, System.currentTimeMillis() - ServerInteraction.serverStartTime);
    }

    public void doHeal(int val) {
        hitPoints += val;
    }

    @Override
    public void kill() {
        Direction d;

        if ((d = moveAnim.getDirection()) == null
                && (d = standAnim.getDirection()) == null) {
            d = Direction.SOUTH;
        }
        deathAnim.run(d, System.currentTimeMillis() - ServerInteraction.serverStartTime);
        mv.stop();
    }

    public void resurect() {
        hitPoints = maxHitPoints;
    }

    @Override
    public boolean dead() {
        return hitPoints <= 0;
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

    public int getGoldCount() {
        int count = 0;

        ArrayList<Etc> etc = inventory.getEtc(Items.GOLD);
        if (etc != null) {
            count += etc.get(0).getCount();
        }

        return count;
    }

    public Inventory getInventory() {
        return inventory;
    }

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
