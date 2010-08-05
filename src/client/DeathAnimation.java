package client;

import java.awt.Point;

public abstract class DeathAnimation {

    protected DirectionalSpriteSet set;
    protected Direction direction;
    protected boolean done = false;

    protected DeathAnimation(String set) {
        this.set = DirectionalSpriteSet.load(set + "_death");
    }

    public abstract Sprite getSprite(long curTime, Point curPos);

    protected int getSpriteCount() {
        return set.getSpriteCount(direction);
    }

    public boolean isDone() {
        return done;
    }

    public abstract void run(Direction direction, long begTime);
}
