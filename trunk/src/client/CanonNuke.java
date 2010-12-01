package client;

import java.awt.Point;

import common.Movement;

public class CanonNuke extends Nuke {
    private WUnit attacker;
    private CanonNukeAnimation animation;
    private Movement movement;

    public CanonNuke(WUnit attacker) {
        final Point cur = attacker.getCurPos();

        this.attacker = attacker;
        animation = new CanonNukeAnimation("canon_bolt");
        movement = new Movement(cur.x, cur.y, 1.0);
    }

    @Override
    public Sprite getSprite() {
        return animation.getSprite();
    }

    @Override
    public void use(long begTime) {
        WUnit target = attacker.getSelectedUnit();
        Point beg, cur;

        if (target != null) {
            beg = (Point) attacker.getCurPos().clone();
            cur = target.getCurPos();
            movement.move(beg.x, beg.y, cur.x, cur.y, begTime);
            //animation.run(beg, target.getEnd(), movement.getCurPos());
            animation.run(beg.x, beg.y, target.getEnd().x, target.getEnd().y, movement.getCurPos().x, movement.getCurPos().y);
            lastUseTime = begTime;
        }
    }

    public final Point getBeg() {
        return movement.getBeg();
    }

    @Override
    public Point getCurPos() {
        return movement.getCurPos();
    }

    @Override
    public boolean isMove() {
        return movement.isMove();
    }
}
