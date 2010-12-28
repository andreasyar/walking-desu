package wand6.common;

import java.awt.Graphics;

public abstract class MapFragment {

    public final static int width = 231;
    public final static int height = 231;

    public final static int maxIdX = (Integer.MAX_VALUE - 1) / width;
    public final static int maxIdY = (Integer.MAX_VALUE - 1) / height;

    public final static int cellWidth = 33;
    public final static int cellHeight = 33;

    private int[][] hmap;

    private int idX;
    private int idY;

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

    public abstract void draw(Graphics g, int x, int y);
}
