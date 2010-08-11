package client;

import java.awt.Point;

public class PeasantNuke extends Nuke {
    private Unit attacker;
    private PeasantNukeAnimation animation;
    private Movement movement;
    private long delay;

    public PeasantNuke(Unit attacker, long delay) {
        final Point cur = attacker.getCurPos();

        movement = new Movement(cur.x, cur.y, 1.0);
        animation = new PeasantNukeAnimation("peasant_bolt");
        this.attacker = attacker;
        this.delay = delay;
    }

    @Override
    public Sprite getSprite() {
        if (lastUseTime + delay >= System.currentTimeMillis() - ServerInteraction.serverStartTime) {
            return animation.getSprite();
        } else {
            return null;
        }
    }

    @Override
    public void use(long begTime) {
        Unit target = attacker.getSelectedUnit();
        Point beg;

        if (target != null) {
            beg = (Point) attacker.getCurPos().clone();
            movement.move(beg, target.getCurPos(), begTime);
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
