package wand6.common;

public class CurveMovement {

    private int curX;
    private int curY;

    private final int maxMapX = MapFragment.maxIdX * MapFragment.width;
    private final int maxMapY = MapFragment.maxIdY * MapFragment.height;

    public int getCurX() {
        return curX;
    }

    public int getCurY() {
        return curY;
    }

    public void start(int x, int y, long begTime) {

        // Dummy
        curX = x;
        curY = y;
    }

    int getEndX() {
        // Dummy
        return curX;
    }

    int getEndY() {
        // Dummy
        return curX;
    }

    long getBegTime() {
        // Dummy
        return ServerTime.getInstance().getTimeSinceStart();
    }
}
