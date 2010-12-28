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
}
