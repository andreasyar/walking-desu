package client;

import client.items.ClientEtc;
import common.Inventory;
import common.items.Item;
import common.items.Etc;
import common.items.Items;
import java.util.ArrayList;

public class Player extends WUnit {

    private PickupAnimation pickupAnim;
    private boolean deathStarted;

    private final Inventory inventory = new Inventory();

    public Player(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
        super(id, nick, maxHitPoints, speed, x, y, d, set);
        hitPoints = maxHitPoints;
        deathAnim = new PlayerDeathAnimation("peasant");
        //pickupAnim = new PickupAnimation(set);
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
        deathStarted = false;
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

    @Override
    public synchronized void kill() {
        if (deathStarted) {
            return;
        }
        deathStarted = true;
        Direction d;

        if ((d = moveAnim.getDirection()) == null
                && (d = standAnim.getDirection()) == null) {
            d = Direction.SOUTH;
        }
        deathAnim.run(d, System.currentTimeMillis() - ServerInteraction.serverStartTime);
        mv.stop();
        hitPoints = 0;
    }

    public synchronized void resurect() {
        restoreHitPoints();
    }

    @Override
    public synchronized boolean dead() {
        return hitPoints <= 0;
    }

    @Override
    public synchronized void move(int begX, int begY, int endX, int endY, long begTime) {
        super.move(begX, begY, endX, endY, begTime);
    }

    @Override
    public synchronized Sprite getSprite() {
        return super.getSprite();
    }

    @Override
    public synchronized boolean deathAnimationDone() {
        return super.deathAnimationDone();
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
