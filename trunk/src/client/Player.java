package client;

import common.GoldCoinItem;
import common.MultiItem;
import java.util.ArrayList;

public class Player extends WUnit {

    private PickupAnimation pickupAnim;

    private final ArrayList<WItem> inventory = new ArrayList<WItem>();

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

    public void addItemToInventory(WItem item) {
        System.out.println(inventory.size() + " items in inventory.");

        if (item instanceof MultiItem) {
            if (item instanceof WGoldCoinItem) {
                WGoldCoinItem newCoins = (WGoldCoinItem) item;
                for (WItem i : inventory) {
                    if (i instanceof WGoldCoinItem) {
                        WGoldCoinItem invCoins = (WGoldCoinItem) i;
                        invCoins.setCount(invCoins.getCount() + newCoins.getCount());
                        return;
                    }
                }

                // not found!
                synchronized (inventory) {
                    inventory.add(item);
                }
            } else {
                synchronized (inventory) {
                    inventory.add(item);
                }
            }
        } else {
            synchronized (inventory) {
                inventory.add(item);
            }
        }
    }

    public int getGoldCount() {
        int count = 0;

        for (WItem item : inventory) {
            if (item instanceof MultiItem) {
                if (item instanceof WGoldCoinItem) {
                    WGoldCoinItem invCoins = (WGoldCoinItem) item;
                    count += invCoins.getCount();
                }
            }
        }

        return count;
    }

    public void removeItemFromInventory(long itemID) {
        WItem toRemove = null;

        for (WItem i : inventory) {
            if (i.getID() == itemID) {
                toRemove = i;
                break;
            }
        }

        if (toRemove != null) {
            synchronized (inventory) {
                inventory.remove(toRemove);
            }
        } else {
            System.err.println("Item " + itemID + " not found in inventory.");
        }
    }
}
