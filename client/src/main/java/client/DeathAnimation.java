package client;

import java.awt.Point;

public final class DeathAnimation {

    private final DirectionalSpriteSet set;
    private Direction direction;
    private boolean done;

    private int period; // Количество спрайтов в наборе

    private long step;  // Время показа каждого спрайта (или время смены спрайта)
    private long begTime = 0;

    public DeathAnimation(String set) {
        this.set = DirectionalSpriteSet.load(set + "_death");
        if ("peasant".equals(set) || "peon".equals(set) || "poring".equals(set)) {
            this.step = 150;
            run(Direction.SOUTH, 0);
        } else {
            throw new IllegalArgumentException("Unsupported death animation: " + set);
        }
    }

    public synchronized final Sprite getSprite(long curTime, Point curPos) {
        long elapsed = Math.max(0, curTime - begTime);
        int frame = (int) Math.min(elapsed / step, period - 1L);
        done = elapsed >= step * period;
        Sprite sprite = set.getSprite(direction, frame);
        sprite.x = curPos.x - sprite.image.getWidth() / 2;
        sprite.y = curPos.y - sprite.image.getHeight();
        sprite.baseX = curPos.x;
        sprite.baseY = curPos.y;
        return sprite;
    }

    public synchronized boolean isDone() { return done; }

    public synchronized boolean isDone(long time) {
        done = Math.max(0, time - begTime) >= step * period;
        return done;
    }

    public synchronized void run(Direction direction, long begTime) {
        done = false;
        this.begTime = begTime;
        this.direction = direction;
        period = set.getSpriteCount(direction);
    }
}
