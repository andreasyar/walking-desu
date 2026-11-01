package client;


public class Monster extends WUnit {

    public Monster(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
        super(id, nick, maxHitPoints, speed, x, y, d, set);
        hitPoints = maxHitPoints;
    }

    // <editor-fold defaultstate="collapsed" desc="HP works">
    @Override
    public synchronized void doHit(int dmg) {
        hitPoints = Math.max(0, hitPoints - dmg);
        if (hitPoints == 0) {
            kill();
        }
    }

    public synchronized void restoreHitPoints() {
        hitPoints = maxHitPoints;
        resetAnimations();
    }
    // </editor-fold>


    @Override
    public synchronized boolean dead() {
        return hitPoints <= 0;
    }
}
