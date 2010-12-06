package client;

import common.WanderingServerTime;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

public class CanonHitAnimation extends HitAnimation {

    private int period = 4; // Количество спрайтов в наборе

    private long step;      // Время показа каждого спрайта (или время смены спрайта)
    private int delay;      // Задержка до очередного проигрывания
    private long begTime = 0;

    public CanonHitAnimation(String set, Point curPos, Direction direction, long begTime) {
        super(set, curPos, direction);
        if ("canon".equals(set)) {
            this.step = 100;
            this.delay = 0;
        } else {
            System.err.println("Hit sprite set " + set + " not supported.");
            System.exit(1);
        }
        this.begTime = begTime;
    }

    @Override
    public Sprite getSprite() {
        long tmpTime;
        Sprite tmpSpr;
        long curTime = WanderingServerTime.getInstance().getTimeSinceStart();

        tmpTime = (curTime - begTime) % (step * period + delay);

        if (curTime > begTime + step * period + delay) {
            done = true;
            return set.getSprite(direction, period - 1);
        }

        if (tmpTime <= step * period) {
            int sprIndex = (int) (((tmpTime % (step * period)) / step));
            if (sprIndex < 0) {
                System.err.println("Something wery bad happen right now.");
            }
            if (sprIndex < period && sprIndex >= 0) {
                tmpSpr = set.getSprite(direction, sprIndex);
            } else {
                tmpSpr = set.getSprite(direction, 0);
            }
            tmpSpr.x = curPos.x - tmpSpr.image.getWidth() / 2;
            tmpSpr.y = curPos.y - tmpSpr.image.getHeight();
            return tmpSpr;
        } else {

            // Now time to delay before new animation cycle. So return last
            // sprite from stand animation.
            tmpSpr = set.getSprite(direction, period - 1);
            tmpSpr.x = curPos.x - tmpSpr.image.getWidth() / 2;
            tmpSpr.y = curPos.y - tmpSpr.image.getHeight();
            return tmpSpr;
        }
    }

    @Override
    public void draw(Graphics g, int x, int y, Dimension d) {
        Sprite s = getSprite();
        if (!isDone()) {
            g.drawImage(s.image, s.x - x, s.y - y, null);
        }
    }
}
