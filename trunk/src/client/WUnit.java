package client;

import common.Unit;

public abstract class WUnit extends Unit {

    public WUnit(long id, String nick, int maxHitPoints, int x, int y, double speed) {
        super(id, nick, maxHitPoints, x, y, speed);
    }
}
