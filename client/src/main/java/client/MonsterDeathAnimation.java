package client;

import java.awt.Point;

public class MonsterDeathAnimation extends DeathAnimation {

    private int period; // Количество спрайтов в наборе

    private long step;  // Время показа каждого спрайта (или время смены спрайта)
    private long begTime = 0;

    public MonsterDeathAnimation(String set) {
        super(set);
        if ("poring".equals(set)) {
            this.step = 150;
            run(Direction.SOUTH, 0);
        } else {
            System.err.println("Death sprite set " + set + " not supported.");
            System.exit(1);
        }
    }

    @Override
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

    @Override
    public synchronized void run(Direction direction, long begTime) {
        done = false;
        this.begTime = begTime;
        this.direction = direction;
        period = super.getSpriteCount();
    }
}
