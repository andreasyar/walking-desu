package client;

import java.awt.Point;

public class UseSkillAnimation {

    /**
     * Directional sprite set.
     */
    private DirectionalSpriteSet set;
    /**
     * Direction.
     */
    private Direction direction;
    /**
     * Флаг окончания проигрывания анимации.
     */
    private boolean stoped = true;
    /**
     * Число спрайтов анимации.
     */
    private int sprCount;
    /**
     * Промежуток времени в милисекундах, через который поочерёдно меняются
     * спрайты анимации.
     */
    private long STEP;
    /**
     * Время начала проигрывания анимации.
     */
    private long begTime;

    public UseSkillAnimation(String set) {
        if ("peasant".equals(set)) {
            STEP = 150;
        } else {
            //throw new Exception("Attack sprite set " + set + " not supported.");
            set = "peasant";
            STEP = 150;
        }
        this.set = DirectionalSpriteSet.load(set + "_attack");
    }

    public Sprite getSprite(long curTime, Point curPos) throws Exception {
        if (stoped) {
            throw new Exception("Attack animation is not played now.");
        }

        Sprite tmpSpr;
        long tmpTime;
        int curSprIndex;

        if (curTime > begTime + STEP * sprCount) {
            stoped = true;
            tmpSpr = set.getSprite(direction, sprCount - 1);
            tmpSpr.x = curPos.x - tmpSpr.image.getWidth() / 2;
            tmpSpr.y = curPos.y - tmpSpr.image.getHeight();
            tmpSpr.baseX = curPos.x;
            tmpSpr.baseY = curPos.y;
            return tmpSpr;
        }

        tmpTime = (curTime - begTime) % (STEP * sprCount);

        if (tmpTime <= STEP * sprCount) {
            curSprIndex = (int) (((tmpTime % (STEP * sprCount)) / STEP));
            if (curSprIndex < 0) {
                System.err.println("Something very bad happen right now.");
            }
            if (curSprIndex < sprCount && curSprIndex >= 0) {
                tmpSpr = set.getSprite(direction, curSprIndex);
            } else {
                tmpSpr = set.getSprite(direction, 0);
            }
            tmpSpr.x = curPos.x - tmpSpr.image.getWidth() / 2;
            tmpSpr.y = curPos.y - tmpSpr.image.getHeight();
            tmpSpr.baseX = curPos.x;
            tmpSpr.baseY = curPos.y;
            return tmpSpr;
        } else {
            tmpSpr = set.getSprite(direction, sprCount - 1);
            tmpSpr.x = curPos.x - tmpSpr.image.getWidth() / 2;
            tmpSpr.y = curPos.y - tmpSpr.image.getHeight();
            tmpSpr.baseX = curPos.x;
            tmpSpr.baseY = curPos.y;
            return tmpSpr;
        }
    }

    public boolean isStoped() {
        return stoped;
    }

    public void run(Direction direction, long begTime) {
        this.direction = direction;
        this.begTime = begTime;
        sprCount = set.getSpriteCount(direction);
        stoped = false;
    }

    public long getNukeAnimationDelay() {
        return (STEP * sprCount) / 2;
    }
}
