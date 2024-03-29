package client;

import java.awt.Point;

public class MovementAnimation {
    private DirectionalSpriteSet set;
    private Direction direct = null;
    private int period;

    private Point beg;
    private double step;
    private double stepCount;

    public MovementAnimation(String spriteSet) {
        this.set = DirectionalSpriteSet.load(spriteSet + "_walk");
    }

    public final Sprite getSprite(Point cur) {
        double tmpCount = beg.distance(cur) / step;
        Sprite tmpSpr;

        if (stepCount < tmpCount) {
            stepCount = tmpCount;
        }
        tmpSpr = set.getSprite(direct, (int) stepCount % period);
        tmpSpr.x = cur.x - tmpSpr.image.getWidth() / 2;
        tmpSpr.y = cur.y - tmpSpr.image.getHeight();
        tmpSpr.baseX = cur.x;
        tmpSpr.baseY = cur.y;
        return tmpSpr;
    }

    public void run(Point beg, Point end, double step) throws Exception {
        run(beg.x, beg.y, end.x, end.y, step);
    }

    public void run(int begX, int begY, int endX, int endY, double step) throws Exception {
        beg = new Point(begX, begY);
        direct = Direction.getDirection(beg, new Point(endX, endY));
        period = set.getSpriteCount(direct);
        if (period <= 0) {
            throw new Exception("Priod for direstion " + direct.name() + " is 0.");
        }
        this.step = step;
        stepCount = 0.0;
    }

    public Direction getDirection() {
        return direct;
    }
}
