package client;

import java.awt.Point;

public class Movement {
    private boolean isMove;
    private Point beg;
    private final Point cur = new Point();  // For temporary storage purpose
    private Point end;
    private long begTime;
    private long endTime;                   // Calculated value
    private double speed;

    public Movement(int x, int y, double speed) {
        isMove = false;
        cur.move(x, y);
        this.speed = speed;
    }

    public void move(final Point beg, final Point end, long begTime) {
        isMove = true;
        this.beg = beg;
        this.end = end;
        this.begTime = begTime;
        endTime = begTime + (long) (beg.distance(end) / speed);
        cur.move(beg.x, beg.y);
    }

    public boolean isMove() {
        if (!isMove) {
            return false;
        } else {
            getCurPos();
            return isMove;
        }
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        if (isMove) {
            this.speed = speed;
            endTime = begTime + (long) (beg.distance(end) / speed);
        } else {
            this.speed = speed;
        }
    }

    public Point getCurPos() {
        if (isMove) {
            long curTime = System.currentTimeMillis() - ServerInteraction.serverStartTime;
            double sqrt = Math.sqrt(Math.pow(Math.abs(end.x - beg.x), 2) + Math.pow(Math.abs(end.y - beg.y), 2));

            cur.x = (int) (beg.x + ((end.x - beg.x) / sqrt) * speed * (curTime - begTime));
            cur.y = (int) (beg.y + ((end.y - beg.y) / sqrt) * speed * (curTime - begTime));

            if (beg.x > end.x && end.x > cur.x
                    || beg.x < end.x && end.x < cur.x
                    || beg.y > end.y && end.y > cur.y
                    || beg.y < end.y && end.y < cur.y
                    || curTime > endTime) {
                cur.move(end.x, end.y);
                isMove = false;
            }
        }
        return cur;
    }

    public long getEndTime() {
        return endTime;
    }

    public Point getEndPoint() {
        return end;
    }

    public void stop() {
        isMove = false;
    }
}
