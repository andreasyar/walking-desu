package client;

import java.util.ArrayList;
import java.util.ListIterator;

public class Tower extends NPC {
    private int damage;
    private double range;

    private long reuse;
    private long lastAttackTime;

    private WUnit target = null;

    public Tower(long id, String nick, double range, int damage, long reuse, int x, int y, Direction d, String set) {
        super(id, nick, 1, 0.0, x, y, d, set);
        hitPoints = 1;
        this.range = range;
        this.damage = damage;
        this.reuse = reuse;
        lastAttackTime = 0;
    }

    public boolean targetInRange() {
        if (target == null || target.getCurPos().distance(getCurPos()) > range || ((Monster) target).dead()) {
            target = null;
            return false;
        } else {
            return true;
        }
    }

    public void selectMonster(final ArrayList<Monster> targets) {
        boolean found = false;

        synchronized (targets) {
            for (ListIterator<Monster> li = targets.listIterator(); li.hasNext();) {
                target = li.next();
                if (targetInRange() && !((Monster) target).dead()) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            target = null;
        }
    }

    public void setLastAttackTime(long lastAttackTime) {
        this.lastAttackTime = lastAttackTime;
    }

    public boolean reuse() {
        if (lastAttackTime == 0) {
            return false;
        }
        return System.currentTimeMillis() - ServerInteraction.serverStartTime - lastAttackTime > reuse;
    }

    public WUnit getTarget() {
        return target;
    }

    public int getDamage() {
        return damage;
    }

    public double getRange() {
        return range;
    }
}
