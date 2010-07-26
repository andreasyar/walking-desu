package client;

import java.awt.Point;

public class Player extends Unit {
    public Player(int id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
        super(id, nick, maxHitPoints, speed, x, y, d, set);
    }

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
        move(new Point(0, 0), new Point(0, 0), System.currentTimeMillis() - ServerInteraction.serverStartTime);
    }
}
