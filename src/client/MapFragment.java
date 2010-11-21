/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 *
 * @author Sorc
 */
public class MapFragment {
    private static int width;
    private static int height;

    private final int[][] hmap;
    private final int idx;
    private final int idy;

    private BufferedImage img = null;

    private static int cellW;
    private static int cellH;

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
    }

    public BufferedImage getImage() {
        if (img == null) {
            img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics g = img.getGraphics();
            Color myGreen = new Color((float) 0.1, (float) 1.0, (float) 0.3, (float) 0.7);
            Color myBlack = new Color((float) 0.1, (float) 0.1, (float) 0.1, (float) 0.7);

            for (int i = 0; (i + 1) * cellW <= width; i++) {
                for (int j = 0; (j + 1) * cellH <= height; j++) {
                    g.setColor(myBlack);
                    g.fillRect(i *  cellW, j * cellH, cellW, cellH);
                    g.setColor(myGreen);
                    g.fillRect(i * cellW + 1, j * cellH + 1, cellW - 1, cellH - 1);
                    g.setColor(myBlack);
                    g.drawString(idx + "," + idy, i * cellW, j * cellH + cellH / 2);
                }
            }
        }
        
        return img;
    }
}
