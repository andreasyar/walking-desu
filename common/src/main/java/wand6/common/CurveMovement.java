package wand6.common;

import java.util.function.LongSupplier;

/** Straight segment adapted from 3.2 common.Movement; no desktop dependency.
 * Speed is pixels/millisecond. A new segment always starts at server position.
 */
public final class CurveMovement {
    public static final double SPEED = 0.07;
    private final LongSupplier clock;
    private int begX, begY, endX, endY;
    private long begTime;
    private double duration;

    public record Position(int x, int y) { }
    public CurveMovement() { this(() -> ServerTime.getInstance().getTimeSinceStart()); }
    public CurveMovement(LongSupplier clock) { this.clock = clock; }

    public synchronized Position position(long time) {
        double fraction = duration == 0 ? 1 : Math.clamp((time - begTime) / duration, 0, 1);
        return new Position((int) Math.round(begX + ((double) endX - begX) * fraction),
                            (int) Math.round(begY + ((double) endY - begY) * fraction));
    }
    public synchronized Position position() { return position(clock.getAsLong()); }
    public int getCurX() { return position().x(); }
    public int getCurY() { return position().y(); }
    public synchronized boolean isMoving() { return isMovingAt(clock.getAsLong()); }
    public synchronized boolean isMovingAt(long time) { return duration > 0 && time < getEndTime(); }
    public synchronized void place(int x, int y) { start(x, y, x, y, clock.getAsLong()); }
    public synchronized void start(int x, int y, long time) {
        Position current = position(time);
        start(current.x(), current.y(), x, y, time);
    }
    public synchronized void start(int bx, int by, int ex, int ey, long time) {
        begX = bx; begY = by; endX = ex; endY = ey; begTime = time;
        duration = Math.hypot((double) ex - bx, (double) ey - by) / SPEED;
    }
    public synchronized void stop() {
        Position current = position();
        place(current.x(), current.y());
    }
    public synchronized int getBegX() { return begX; }
    public synchronized int getBegY() { return begY; }
    public synchronized int getEndX() { return endX; }
    public synchronized int getEndY() { return endY; }
    public synchronized long getBegTime() { return begTime; }
    public synchronized long getEndTime() { return begTime + (long) Math.ceil(duration); }
}
