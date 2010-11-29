package client;

import java.awt.Point;

import common.Movement;

public class CanonNuke extends Nuke {
    private Unit attacker;
    private CanonNukeAnimation animation;
    private Movement movement;

    public CanonNuke(Unit attacker) {
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
        Unit target = attacker.getSelectedUnit();
        Point beg, cur;

        if (target != null) {
            beg = (Point) attacker.getCurPos().clone();
            cur = target.getCurPos();
            movement.move(beg.x, beg.y, cur.x, cur.y, begTime);
            animation.run(beg, target.getEndPoint(), movement.getCurPos());
            lastUseTime = begTime;
        }
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
