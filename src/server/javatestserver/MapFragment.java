/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server.javatestserver;

import java.util.Random;

/**
 *
 * @author Sorc
 */
public class MapFragment {
    protected static int width;
    protected static int height;

    protected final int[][] hmap;

    public int[][] getHmap() {
        return hmap;
    }
    protected final int idx;
    protected final int idy;

    protected static int cellW;
    protected static int cellH;

    public static void setCellH(int cellH) {
        MapFragment.cellH = cellH;
    }

    public static void setCellW(int cellW) {
        MapFragment.cellW = cellW;
    }

    public static int getCellH() {
        return cellH;
    }

    public static int getCellW() {
        return cellW;
    }

    public static void setHeight(int height) {
        MapFragment.height = height;
    }

    public static void setWidth(int width) {
        MapFragment.width = width;
    }

    public static int getHeight() {
        return height;
    }

    public static int getWidth() {
        return width;
    }

    public int getIdx() {
        return idx;
    }

    public int getIdy() {
        return idy;
    }

    public MapFragment(int idx, int idy, int [][] hmap) {
        this.hmap = hmap;
        this.idx = idx;
        this.idy = idy;
        width = height = 1024;
    }

    public static int[][] create2DHMap() {
        int h = 300; // начальная высота измеений. Коэф-т изменения
        int n = 32; // размер картинки

        int mask = n; //
        int offset = h; // начальная высота изменений
        int hmatr[][];

        hmatr = new int[n+1][n+1];
        for(int i=0; i<n+1; ++i){
            for(int j = 0; j<n+1; ++j){
                hmatr[i][j] = -1;
            }
        }
        hmatr[0][0] = offset;
        hmatr[0][n] = offset;
        hmatr[n][0] = offset;
        hmatr[n][n] = offset;

        Random rand = new Random();
        while (mask > 0) {
            int hmask = (int) (mask / 2.0);
            for ( int i = hmask; i < n && i >= 1.0;) {
                for ( int j = hmask; j < n && j >= 1.0;) {
                    //
                    if (hmatr[i][j] == -1) {
                        hmatr[i][j] = (int) ((  hmatr[i - hmask][j - hmask]
                                +hmatr[i - hmask][j + hmask] +hmatr[i + hmask][j - hmask]
                                +hmatr[i + hmask][j + hmask]) / 4.0
                                + (-1 * rand.nextFloat()) * offset);
                    }

                    if (hmatr[i][j] > h)
                        hmatr[i][j] = h ;

                    if (hmatr[i][j] < 0)
                        hmatr[i][j] = 0;
                    //
                    if (hmatr[i - hmask][j] == -1) {
                        hmatr[i - hmask][j] = (int) ((hmatr[i - hmask][j - hmask]
                                + hmatr[i - hmask][j + hmask]) / 2.0
                                + (-1 * rand.nextFloat()) * offset);
                    }

                    if (hmatr[i - hmask][j] > h)
                        hmatr[i - hmask][j] = h;

                    if (hmatr[i - hmask][j] <= 0)
                        hmatr[i - hmask][j] = 1;
                    //
                    if (hmatr[i][j - hmask] == -1) {
                        hmatr[i][j - hmask] = (int) ((hmatr[i - hmask][j - hmask]
                                + hmatr[i + hmask][j - hmask]) / 2.0
                                + (-1 * rand.nextFloat()) * offset);
                    }
                    if (hmatr[i][j - hmask] > h)
                        hmatr[i][j - hmask] = h;

                    if (hmatr[i][j - hmask] <= 0)
                        hmatr[i][j - hmask] = 1;

                    //
                    if (hmatr[i + hmask][j] == -1) {
                    hmatr[i + hmask][j] = (int) ((hmatr[i + hmask][j - hmask]
                            + hmatr[i + hmask][j + hmask]) / 2.0
                            + (-1 * rand.nextFloat()) * offset);
                    }
                    if (hmatr[i + hmask][j] > h)
                        hmatr[i + hmask][j] = h;

                    if (hmatr[i + hmask][j] <= 0)
                        hmatr[i + hmask][j] = 1;

                    //
                    if (hmatr[i][j + hmask] == -1) {
                        hmatr[i][j + hmask] = (int) ((hmatr[i - hmask][j + hmask]
                                + hmatr[i + hmask][j + hmask]) / 2.0
                                + (-1 * rand.nextFloat()) * offset);
                    }
                    if (hmatr[i][j + hmask] > h)
                        hmatr[i][j + hmask] = h;

                    if (hmatr[i][j + hmask] <= 0)
                        hmatr[i][j + hmask] = 1;

                    j += mask;
                }//~ for
                i += mask;
            }//~ for
            offset *= 0.46;
            mask = hmask;
        }//~ while()

        return hmatr;
    }
}
