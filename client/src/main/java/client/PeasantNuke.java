package client;

import java.awt.Dimension;
import java.awt.Point;

import common.Movement;
import java.awt.Graphics;

public class PeasantNuke extends Nuke {
    private WUnit attacker;
    private PeasantNukeAnimation animation;
    private Movement movement;

    public PeasantNuke(WUnit attacker) {
        final Point cur = attacker.getCurPos();

        movement = new Movement(cur.x, cur.y, 1.0);
        animation = new PeasantNukeAnimation("peasant_bolt");
        this.attacker = attacker;
    }

    @Override
    public void use(long begTime) {
        WUnit target = attacker.getSelectedUnit();
        Point beg, cur;

        if (target != null) {
            beg = (Point) attacker.getCurPos().clone();
            cur = target.getCurPos();
            movement.move(beg.x, beg.y, cur.x, cur.y, begTime);
            animation.run(beg, new Point(cur), movement.getCurPos());
            lastUseTime = begTime;
        }
    }

    @Override
    public Point getCurPos() {
        return movement.getCurPos();
    }

    @Override
    public boolean isMove() {
        return WUnit.now() < lastUseTime || movement.isMove();
    }

    @Override
    public void draw(Graphics g, int x, int y, Dimension d) {
        Sprite s = getSprite();

        if (s != null && isMove()) {
            g.drawImage(s.image, s.x - x, s.y - y, null);
        }
    }

    @Override
    public Sprite getSprite() {
        if (WUnit.now() >= lastUseTime && movement.isMove()) {
            return animation.getSprite();
        } else {
            return null;
        }
    }
}
