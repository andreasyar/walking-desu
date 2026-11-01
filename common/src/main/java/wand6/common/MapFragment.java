package wand6.common;

public abstract class MapFragment {

    public final static int cellWidth = 32;
    public final static int cellHeight = cellWidth;

    public final static int width = cellWidth * 32;
    public final static int height = width;

    public final static int maxIdX = Integer.MAX_VALUE / width - 1;
    public final static int maxIdY = Integer.MAX_VALUE / height - 1;

    public static final int maxX = (maxIdX + 1) * width - 1;
    public static final int maxY = (maxIdY + 1) * height - 1;

    public static boolean contains(int x, int y) {
        return x >= 0 && y >= 0 && x <= maxX && y <= maxY;
    }

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
