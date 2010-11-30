package server.javatestserver;

import java.util.ArrayList;

public class Player extends JTSUnit {

    private final ArrayList<JTSUnit> visibleUnits = new ArrayList<JTSUnit>();
    private static final double visibleRange = 500.0;

    public Player(long id, String nick, int maxHitPoints, int x, int y, double speed) {
        super(id, nick, maxHitPoints, x, y, speed);
        hitPoints = maxHitPoints;
        damage = 50;
    }

    @Override
    public void doHit(int dmg) {
        hitPoints -= dmg;
        if (hitPoints < 0) {
            hitPoints = 0;
        }
    }

    @Override
    public boolean dead() {
        return hitPoints <= 0;
    }

    public void restoreHitPoints() {
        hitPoints = maxHitPoints;
    }

    public void teleportToSpawn() {
        move(0, 0, 0, 0, System.currentTimeMillis() - JavaTestServer.serverStartTime);
    }

    public void teleportTo(int x, int y) {
        move(x, y, x, y, System.currentTimeMillis() - JavaTestServer.serverStartTime);
    }

    // <editor-fold defaultstate="collapsed" desc="Visible units works">
    public void addVisibleUnit(JTSUnit unit) {
        synchronized (visibleUnits) {
            visibleUnits.add(unit);
        }
    }

    public void delVisibleUnit(JTSUnit unit) {
        synchronized (visibleUnits) {
            while (visibleUnits.remove(unit)) {
            }
        }
    }

    public ArrayList<JTSUnit> getVisibleUnitsList() {
        return visibleUnits;
    }

    public boolean inRange(JTSUnit unit) {
        if (getCurPos().distance(unit.getCurPos()) <= visibleRange) {
            return true;
        } else {
            return false;
        }
    }

    public boolean see(JTSUnit unit) {
        return visibleUnits.contains(unit);
    }
    // </editor-fold>

    @Override
    public void kill() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
