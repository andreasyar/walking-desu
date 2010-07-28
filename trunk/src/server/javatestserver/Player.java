package server.javatestserver;

import java.awt.Point;

public class Player extends Unit {
    public Player(long id, String nick, int maxHitPoints, int x, int y, double speed) {
        super(id, nick, maxHitPoints, x, y, speed);
        restoreHitPoints();
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
        move(new Point(0, 0), new Point(0, 0), System.currentTimeMillis() - JavaTestServer.serverStartTime);
    }
}
