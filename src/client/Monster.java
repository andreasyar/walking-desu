package client;

import common.WanderingServerTime;

public class Monster extends WUnit {

    public Monster(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
        super(id, nick, maxHitPoints, speed, x, y, d, set);
        hitPoints = maxHitPoints;
        deathAnim = new DeathAnimation(set);
    }

    // <editor-fold defaultstate="collapsed" desc="HP works">
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
    // </editor-fold>

    @Override
    public void kill() {
        Direction d;

        if ( (d = moveAnim.getDirection()) == null && (d = standAnim.getDirection()) == null) {
            d = Direction.SOUTH;
        }
        // TODO looks like some times d is null!
        if (d == null) {
            System.err.println("Lol d is null. HOW?");
        }
        deathAnim.run(d, WanderingServerTime.getInstance().getTimeSinceStart());
        mv.stop();
        hitPoints = 0;
    }

    @Override
    public boolean dead() {
        return hitPoints <= 0;
    }
}
