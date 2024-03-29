package wand6.common;

public abstract class MapFragment {

    public final static int cellWidth = 32;
    public final static int cellHeight = cellWidth;

    public final static int width = cellWidth * 32;
    public final static int height = width;

    public final static int maxIdX = Integer.MAX_VALUE / width;
    public final static int maxIdY = Integer.MAX_VALUE / height;

    private int[][] hmap;

    private int idX;
    private int idY;

    public MapFragment() {
    }

    public MapFragment(int[][] hmap, int idX, int idY) {
        this.hmap = hmap;
        this.idX = idX;
        this.idY = idY;
    }

    public int[][] getHmap() {
        return hmap;
    }

    public void setHmap(int[][] hmap) {
        this.hmap = hmap;
    }

    public int getIdX() {
        return idX;
    }

    public void setIdX(int idX) {
        this.idX = idX;
    }

    public int getIdY() {
        return idY;
    }

    public void setIdY(int idY) {
        this.idY = idY;
    }
}
