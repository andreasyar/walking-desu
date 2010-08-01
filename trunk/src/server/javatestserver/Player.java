package server.javatestserver;

import java.awt.Point;
import java.util.ArrayList;

public class Player extends Unit {

    private final ArrayList<Unit> visibleUnits = new ArrayList<Unit>();
    private static final double visibleRange = 500.0;

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
// <editor-fold defaultstate="collapsed" desc="Visible units works">
    public void addVisibleUnit(Unit unit) {
        synchronized (visibleUnits) {
            visibleUnits.add(unit);
        }
    }

    public void delVisibleUnit(Unit unit) {
        synchronized (visibleUnits) {
            while(visibleUnits.remove(unit)){
            }
        }
    }

    public ArrayList<Unit> getVisibleUnitsList() {
        return visibleUnits;
    }

    public boolean inRange(Unit unit) {
        if(getCurPos().distance(unit.getCurPos()) <= visibleRange) {
            return true;
        } else {
            return false;
        }
    }

    public boolean see(Unit unit) {
        return visibleUnits.contains(unit);
    }
// </editor-fold>
}
