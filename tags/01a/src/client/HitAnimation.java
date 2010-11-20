package client;

import java.awt.Point;

public abstract class HitAnimation {

    protected DirectionalSpriteSet set;
    protected Direction direction;
    protected boolean done = false;
    protected Point curPos;

    protected HitAnimation(String set, Point curPos, Direction direction) {
        this.set = DirectionalSpriteSet.load(set + "_hit");
        this.curPos = curPos;
        this.direction = direction;
    }

    public abstract Sprite getSprite(long curTime);

    public boolean isDone() {
        return done;
    }
}
