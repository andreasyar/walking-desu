package client;

import java.awt.Point;

public class DeathAnimation {

    protected DirectionalSpriteSet set;
    protected Direction direction;
    protected boolean done = false;

    private int period; // Количество спрайтов в наборе

    private long step;  // Время показа каждого спрайта (или время смены спрайта)
    private int delay;  // Задержка до очередного проигрывания
    private long begTime = 0;

    public DeathAnimation(String set) {
        if ("peasant".equals(set)) {
            this.step = 150;
            this.delay = 0;
        } else if ("poring".equals(set)) {
            this.step = 150;
            this.delay = 0;
        } else {
            System.err.println("Death sprite set " + set + " not supported.");
            System.exit(1);
        }
        this.set = DirectionalSpriteSet.load(set + "_death");
    }

    public Sprite getSprite(long curTime, Point curPos) {
        long tmpTime;
        Sprite tmpSpr;

        if (delay == 0) {
            System.err.println("Something very very bad happen right now.");
            delay = 100;
        }
        tmpTime = (curTime - begTime) % (step * period + delay);

        if (curTime > begTime + step * period + delay) {
            done = true;
            return set.getSprite(direction, period - 1);
        }

        if (tmpTime <= step * period) {
            int sprIndex = (int) (((tmpTime % (step * period)) / step));
            if (sprIndex < 0) {
                System.err.println("Something very bad happen right now.");
            }
            if (sprIndex < period && sprIndex >= 0) {
                tmpSpr = set.getSprite(direction, sprIndex);
            } else {
                tmpSpr = set.getSprite(direction, 0);
            }
            tmpSpr.x = curPos.x - tmpSpr.image.getWidth() / 2;
            tmpSpr.y = curPos.y - tmpSpr.image.getHeight();
            tmpSpr.baseX = curPos.x;
            tmpSpr.baseY = curPos.y;
            return tmpSpr;
        } else {

            // Now time to delay before new animation cycle. So return last
            // sprite from stand animation.
            tmpSpr = set.getSprite(direction, period - 1);
            tmpSpr.x = curPos.x - tmpSpr.image.getWidth() / 2;
            tmpSpr.y = curPos.y - tmpSpr.image.getHeight();
            tmpSpr.baseX = curPos.x;
            tmpSpr.baseY = curPos.y;
            return tmpSpr;
        }
    }

    protected int getSpriteCount() {
        return set.getSpriteCount(direction);
    }

    public boolean isDone() {
        return done;
    }

    public void run(Direction direction, long begTime) {
        done = false;
        this.begTime = begTime;
        this.direction = direction;
        period = getSpriteCount();
    }
}
