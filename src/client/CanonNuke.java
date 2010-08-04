package client;

import java.awt.Point;

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
        Point beg;

        if (target != null) {
            beg = (Point) attacker.getCurPos().clone();
            movement.move(beg, target.getCurPos(), begTime);
            animation.run(beg, target.getEndPoint(), movement.getCurPos());
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
