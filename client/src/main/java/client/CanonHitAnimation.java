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
        long elapsed = Math.max(0, WanderingServerTime.getInstance().getTimeSinceStart() - begTime);
        done = elapsed >= step * period;
        Sprite sprite = set.getSprite(direction, (int) Math.min(elapsed / step, period - 1L));
        sprite.x = curPos.x - sprite.image.getWidth() / 2;
        sprite.y = curPos.y - sprite.image.getHeight();
        sprite.baseX = curPos.x;
        sprite.baseY = curPos.y;
        return sprite;
    }

    @Override
    public void draw(Graphics g, int x, int y, Dimension d) {
        Sprite s = getSprite();
        if (!isDone()) {
            g.drawImage(s.image, s.x - x, s.y - y, null);
        }
    }
}
