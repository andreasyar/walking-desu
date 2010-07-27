package client;

import java.awt.Point;

abstract public class Nuke {
    private Unit attacker;
    private Unit target;

    protected NukeAnimation nukeAnim;
    protected Movement move;

    protected long reuse;
    protected long lastUseTime;

    public boolean reuse() {
        return Math.abs(System.currentTimeMillis() - ServerInteraction.serverStartTime) - lastUseTime > reuse ? false : true;
    }

    public Sprite getSprite() {
        Sprite s = nukeAnim.getSprite(System.currentTimeMillis() - ServerInteraction.serverStartTime);
        Point cur = move.getCurPos();
        s.x = cur.x;
        s.y = cur.y;
        return s;
    }

    public void use(Unit attacker, Unit target, long begTime) {
        this.attacker = attacker;
        this.target = target;
        lastUseTime = begTime;
        move.move((Point) attacker.getCurPos().clone(), target.getCurPos(), begTime);
    }

    public Point getCurPos() {
        return move.getCurPos();
    }

    public boolean isMove() {
        return move.isMove();
    }
}
