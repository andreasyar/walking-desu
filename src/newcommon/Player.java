package newcommon;

import newcommon.exceptions.InventoryException;
import java.util.ArrayList;
import newcommon.items.Etc;
import newcommon.items.Items;

public class Player extends Unit {

    private final Inventory inventory = new Inventory();

    public Player(long id, String name, int maxHealth, int health, String spriteSetName, String text, long deathDelay) {
        super(id, name, maxHealth, health, spriteSetName, text, deathDelay);
    }

    public Etc getEtc(long id) {
        return inventory.getEtc(id);
    }

    public ArrayList<Etc> getEtc(Items type) {
        return inventory.getEtc(type);
    }

    public boolean addEtc(Etc item) {
        return inventory.addEtc(item);
    }

    public void removeEtc(Etc item) throws InventoryException {
        inventory.removeEtc(item);
    }

    public void useEtc(Etc item) {
        // TODO What happen when we use etc item?
    }

    public int getGoldCount() {
        int count = 0;

        ArrayList<Etc> etc = inventory.getEtc(Items.GOLD);
        if (!etc.isEmpty()) {
            count += etc.get(0).getCount();
        }

        return count;
    }

    public void teleportTo(int x, int y) {
        move(getCurX(), getCurY(), x, y, ServerTime.getInstance().getTimeSinceStart(), 1000L);
    }

    public boolean isDead() {
        return getHealth() <= 0;
    }

    public void hit(int dmg) {
        setHealth(getHealth() - dmg);
        if (getHealth() < 0) {
            setHealth(0);
        }
    }

    public void kill() {
        setHealth(0);
        stop();
    }

    public void resurect() {
        setHealth(getMaxHealth());
    }
}
