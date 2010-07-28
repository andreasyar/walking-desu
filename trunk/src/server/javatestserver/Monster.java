package server.javatestserver;

public class Monster extends Unit {

    public Monster(long id, String nick, int maxHitPoints, int x, int y, double speed) {
        super(id, nick, maxHitPoints, x, y, speed);
        restoreHitPoints();
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

    public boolean dead() {
        return hitPoints <= 0;
    }

}
