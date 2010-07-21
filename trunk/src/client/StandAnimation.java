package client;

import java.awt.Point;

public class StandAnimation {
    private DirectionalSpriteSet set;
    private Direction direct;
    private int period;

    private final long step = 75;
    private int delay = 3000;
    private long begTime = 0;
    private long endTime = 0;

    public StandAnimation(String spriteSet) {
        this.set = DirectionalSpriteSet.load(spriteSet + "_stand");
    }

    public final Sprite getSprite(long curTime, Point curPos) {
        long tmpTime;
        Sprite tmpSpr;

        // Animetion ends. Return the last sprite from stand animation.
        if (endTime != 0 && curTime > endTime) {
            tmpSpr = set.getSprite(direct, period - 1);
            tmpSpr.x = curPos.x;
            tmpSpr.y = curPos.y;
            return tmpSpr;
        }

        tmpTime = (curTime - begTime) % (step * period + delay);

        if (tmpTime <= step * period) {
            tmpSpr = set.getSprite(direct, (int) (tmpTime % period));
            tmpSpr.x = curPos.x;
            tmpSpr.y = curPos.y;
            return tmpSpr;
        } else {
            // Now time to delay before new animation cycle. So return last
            // sprite from stand animation.
            tmpSpr = set.getSprite(direct, period - 1);
            tmpSpr.x = curPos.x;
            tmpSpr.y = curPos.y;
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
