package common;

import java.awt.Point;

/**
 * Represent a movement process. Movement here is a changing point coords
 * in time on plane. Current position calculates and returns on getCurPos call.
 */
public class Movement {

    /**
     * This flag indicate when movement in progress or not.
     */
    private boolean isMove;

    /**
     * Start point of movement in the world coords.
     */
    private final Point beg = new Point();

    /**
     * End point of movement in the world coords.
     */
    private final Point end = new Point();

    /**
     * Current position in the world coords. For temporary storage purpose.
     */
    private final Point cur = new Point();

    /**
     * Start time of movement since server started.
     */
    private long begTime;

    /**
     * End time of movement since server started. Calculatable value.
     */
    private long endTime;

    /**
     * Movement speed.
     * TODO: в чем измеряется?
     */
    private double speed;

    /**
     * Creates a new movement. Actually we dont move on creation, just stand to
     * specifed point and set speed.
     * @param x Initial world x coord.
     * @param y Initial world y coord.
     * @param speed Initial movement speed.
     */
    public Movement(int x, int y, double speed) {
        isMove = false;
        cur.setLocation(x, y);
        beg.setLocation(x, y);
        end.setLocation(x, y);
        this.speed = speed;
    }

    /**
     * Return true if movement in progress. False otherwise.
     */
    public boolean isMove() {
        if (!isMove) {
            return false;
        } else {
            getCurPos();
            return isMove;
        }
    }

    /**
     * Return start point of movement in the world coords.
     */
    public final Point getBeg() {
        return beg;
    }

    /**
     * Return end point of movement in the world coords.
     */
    public final Point getEnd() {
        return end;
    }

    /**
     * Calculate and return current position in the world coords.
     */
    public final Point getCurPos() {
        if (isMove) {
            long curTime = System.currentTimeMillis() - WanderingServerTime.getInstance().getServerTime();
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

    /**
     * Return start time of movement since server started.
     */
    public long getBegTime() {
        return begTime;
    }

    /**
     * Return end time of movement since server started.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Return current movement speed.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Set current movement speed.
     * @param speed New movement speed.
     */
    public void setSpeed(double speed) {
        if (isMove) {
            this.speed = speed;
            endTime = begTime + (long) (beg.distance(end) / speed);
        } else {
            this.speed = speed;
        }
    }

    /**
     * Actually start movement.
     * @param begX Start x world coord of movement.
     * @param begY Start y world coord of movement.
     * @param endX End x world coord of movement.
     * @param endY End y world coord of movement.
     * @param begTime Movement begining time since server start.
     */
    public void move(int begX, int begY, int endX, int endY, long begTime) {
        isMove = true;
        beg.setLocation(begX, begY);
        end.setLocation(endX, endY);

        /*
         * TODO: We must stay on beg point. But what if we not? It can be.
         * We must catch this case and report because it may be bug. Here we
         * just hide them.
         */
        cur.setLocation(beg);

        this.begTime = begTime;
        endTime = begTime + (long) (beg.distance(end) / speed);
    }

    /**
     * Stop the movement on current position.
     */
    public void stop() {

        // Calculate current position.
        getCurPos();

        isMove = false;
    }
}
