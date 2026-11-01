package client;

import common.WanderingServerTime;

public class Monster extends WUnit {

    private boolean deathStarted;

    public Monster(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
        super(id, nick, maxHitPoints, speed, x, y, d, set);
        hitPoints = maxHitPoints;
        deathAnim = new MonsterDeathAnimation(set);
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
        deathStarted = false;
    }
    // </editor-fold>

    @Override
    public synchronized void kill() {
        if (deathStarted) {
            return;
        }
        deathStarted = true;
        Direction d;

        if ( (d = moveAnim.getDirection()) == null && (d = standAnim.getDirection()) == null) {
            d = Direction.SOUTH;
        }
        deathAnim.run(d, WanderingServerTime.getInstance().getTimeSinceStart());
        mv.stop();
        hitPoints = 0;
    }

    @Override
    public synchronized void move(int begX, int begY, int endX, int endY, long begTime) {
        super.move(begX, begY, endX, endY, begTime);
    }

    @Override
    public synchronized Sprite getSprite() {
        return super.getSprite();
    }

    @Override
    public synchronized boolean deathAnimationDone() {
        return super.deathAnimationDone();
    }

    @Override
    public synchronized boolean dead() {
        return hitPoints <= 0;
    }
}
