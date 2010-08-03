package client;

import java.awt.Point;

public class StandAnimation {
    private DirectionalSpriteSet set;
    private Direction direct;
    private int period;             // Количество спрайтов в наборе

    private long step;  // Время показа каждого спрайта (или время смены спрайта)
    private int delay;  // Задержка до очередного проигрывания
    private int repeat; // Число повторов показа всего набора спрайтов
    private long begTime = 0;
    private long endTime = 0;

    public StandAnimation(String spriteSet) {
        this.set = DirectionalSpriteSet.load(spriteSet + "_stand");
        if ("desu".equals(spriteSet)) {
            this.step = 150;
            this.delay = 3000;
            this.repeat = 2;
        } else if ("poring".equals(spriteSet)) {
            this.step = 150;
            this.delay = 0;
            this.repeat = 1;
        } else if ("tower".equals(spriteSet)) {
            this.step = 150;
            this.delay = 0;
            this.repeat = 1;
        } else if ("peasant".equals(spriteSet)) {
            this.step = 150;
            this.delay = 0;
            this.repeat = 1;
        }
    }

    public final Sprite getSprite(long curTime, Point curPos) {
        long tmpTime;
        Sprite tmpSpr;

        // Animation ends. Return the last sprite from stand animation.
        if (endTime != 0 && curTime > endTime) {
            tmpSpr = set.getSprite(direct, period - 1);
            System.out.println();
            tmpSpr.x = curPos.x - tmpSpr.image.getWidth() / 2;
            tmpSpr.y = curPos.y - tmpSpr.image.getHeight();
            return tmpSpr;
        }

        tmpTime = (curTime - begTime) % (step * period * repeat + delay);

        if (tmpTime <= step * period * repeat) {
            int sprIndex = (int) (((tmpTime % (step * period)) / step));
            if (sprIndex < 0) {
                System.err.println("Something wery bad happen right now.");
            }
            if (sprIndex < period && sprIndex >= 0) {
                tmpSpr = set.getSprite(direct, sprIndex);
            } else {
                tmpSpr = set.getSprite(direct, 0);
            }
            //System.out.print(sprIndex);
            tmpSpr.x = curPos.x - tmpSpr.image.getWidth() / 2;
            tmpSpr.y = curPos.y - tmpSpr.image.getHeight();
            return tmpSpr;
        } else {
            // Now time to delay before new animation cycle. So return last
            // sprite from stand animation.
            tmpSpr = set.getSprite(direct, period - 1);
            //System.out.println();
            tmpSpr.x = curPos.x - tmpSpr.image.getWidth() / 2;
            tmpSpr.y = curPos.y - tmpSpr.image.getHeight();
            return tmpSpr;
        }
    }

    public void run(Direction direct, long begTime) {
        this.begTime = begTime;
        this.direct = direct;
        period = set.getSpriteCount(direct);
        endTime = 0;
    }

    public void run(Direction direct, long begTime, long endTime) {
        this.begTime = begTime;
        this.direct = direct;
        period = set.getSpriteCount(direct);
        this.endTime = endTime;
    }
}
