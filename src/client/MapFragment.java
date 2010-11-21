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
            g.setColor(new Color((float) 0.1, (float) 1.0, (float) 0.3, (float) 0.7));
            g.fillRect(0, 0, width - 1, height - 1);
            g.setColor(Color.GREEN);
        }
        
        return img;
    }
}
