package client;

import java.awt.Point;

abstract public class Nuke {
    protected Unit attacker;

    protected NukeAnimation nukeAnim;
    protected Movement mv;

    protected long reuse;
    protected long lastUseTime;

    public Nuke(Unit attacker) {
        this.attacker = attacker;
    }

    public boolean reuse() {
        return Math.abs(System.currentTimeMillis() - ServerInteraction.serverStartTime) - lastUseTime > reuse ? false : true;
    }

    public Sprite getSprite() {
        Sprite s = nukeAnim.getSprite(System.currentTimeMillis() - ServerInteraction.serverStartTime);
        Point cur = mv.getCurPos();
        s.x = cur.x;
        s.y = cur.y;
        return s;
    }

    public void use(Unit target, long begTime) {
        lastUseTime = begTime;
        mv.move((Point) attacker.getCurPos().clone(), target.getCurPos(), begTime);
    }

    public Point getCurPos() {
        return mv.getCurPos();
    }

    public boolean isMove() {
        return mv.isMove();
    }
}
