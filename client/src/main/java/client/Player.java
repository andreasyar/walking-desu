package client;

import common.Inventory;
import common.items.Etc;
import common.items.Items;
import java.util.ArrayList;

public class Player extends WUnit {

    private final Inventory inventory = new Inventory();

    public Player(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
        super(id, nick, maxHitPoints, speed, x, y, d, set);
        hitPoints = maxHitPoints;
    }

    @Override
    public synchronized void doHit(int dmg) {
        hitPoints = Math.max(0, hitPoints - dmg);
        if (hitPoints == 0) {
            kill();
        }
    }

    public synchronized void restoreHitPoints() {
        hitPoints = maxHitPoints;
        resetAnimations();
    }

    public synchronized void teleportToSpawn() {
        move(0, 0, 0, 0, System.currentTimeMillis() - ServerInteraction.serverStartTime);
    }

    public synchronized void teleportTo(int x, int y) {
        move(x, y, x, y, System.currentTimeMillis() - ServerInteraction.serverStartTime);
    }

    public synchronized void doHeal(int val) {
        hitPoints += val;
    }


    public synchronized void resurect() {
        restoreHitPoints();
    }

    @Override
    public synchronized boolean dead() {
        return hitPoints <= 0;
    }

    /**
     * Adds item to inventory.
     * @param item item to add.
     */
    public void addItemToInventory(Etc item) {
        getInventory().addEtc(item);
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
    public void removeItemFromInventory(long id, int count) {
        getInventory().removeEtc(id, count);
    }

    public int getGoldCount() {
        int count = 0;

        ArrayList<Etc> etc = inventory.getEtc(Items.GOLD);
        if (!etc.isEmpty()) {
            count += etc.get(0).getCount();
        }

        return count;
    }

    public Inventory getInventory() {
        return inventory;
    }

}
