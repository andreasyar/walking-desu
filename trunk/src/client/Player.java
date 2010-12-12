package client;

import client.items.ClientItem;
import common.Inventory;
import common.Item;
import common.MultiItem;
import java.util.ArrayList;

public class Player extends WUnit {

    private PickupAnimation pickupAnim;

    private final ArrayList<ClientItem> inv = new ArrayList<ClientItem>();

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

    public void addItemToInventory(Item item) {
        item.addToInventory(getInventory());
    }

    public void removeItemFromInventory(long itemID) {
        ClientItem toRemove = null;

        for (ClientItem i : inv) {
            if (i.getID() == itemID) {
                toRemove = i;
                break;
            }
        }

        if (toRemove != null) {
            synchronized (inv) {
                inv.remove(toRemove);
            }
        } else {
            System.err.println("Item " + itemID + " not found in inventory.");
        }
    }

    public int getGoldCount() {
        int count = 0;

        for (ClientItem item : inv) {
            if (item instanceof MultiItem) {
                if (item instanceof WGoldCoinItem) {
                    WGoldCoinItem invCoins = (WGoldCoinItem) item;
                    count += invCoins.getCount();
                }
            }
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
    public void pickup(ClientItem i) {
        if (pickupAnim != null) {
            // play pickup animation here.
        }
    }
}
