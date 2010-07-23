package client;

import java.awt.Point;

public class StandAnimation {
    private DirectionalSpriteSet set;
    private Direction direct;
    private int period;             // Количество спрайтов в наборе

    private final long step = 150;  // Время показа каждого спрайта (или время смены спрайта)
    private int delay = 3000;       // Задержка до очередного проигрывания
    private int repeat = 2;         // Число повторов показа всего набора спрайтов
    private long begTime = 0;
    private long endTime = 0;

    public StandAnimation(String spriteSet) {
        this.set = DirectionalSpriteSet.load(spriteSet + "_stand");
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
            if (sprIndex < period) { // TODO Auto of bound protection here.
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
