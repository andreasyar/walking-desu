package client;

import java.awt.Point;

/** Visual attack only; gameplay timing and damage remain server-owned. */
public final class UseSkillAnimation {
    private final DirectionalSpriteSet set;
    private Direction direction = Direction.SOUTH;
    private static final long STEP = 150;
    private long begTime;
    private boolean started;

    public UseSkillAnimation(String name) {
        if (!"peasant".equals(name) && !"peon".equals(name)) {
            throw new IllegalArgumentException("Unsupported attack animation: " + name);
        }
        set = DirectionalSpriteSet.load(name + "_attack");
    }

    public Sprite getSprite(long time, Point position) {
        int count = set.getSpriteCount(direction);
        int frame = (int) Math.min(Math.max(0, time - begTime) / STEP, count - 1L);
        Sprite sprite = set.getSprite(direction, frame);
        sprite.baseX = position.x;
        sprite.baseY = position.y;
        sprite.x = position.x - sprite.image.getWidth() / 2;
        sprite.y = position.y - sprite.image.getHeight();
        return sprite;
    }

    public boolean isPlaying(long time) {
        return started && time < begTime + STEP * set.getSpriteCount(direction);
    }

    public void stop() { started = false; }

    public void run(Direction direction, long begTime) {
        this.direction = direction;
        this.begTime = begTime;
        started = true;
    }

    public long getNukeAnimationDelay() {
        return STEP * set.getSpriteCount(direction) / 2;
    }
}
