package wand6.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import wand6.common.MapFragment;

class CMapFragment extends MapFragment {

    private static int debugLevel = 2;

    private static final HashMap<String, BufferedImage> tiles = new HashMap<String, BufferedImage>();
    private static final HashMap<String, BufferedImage> summerTiles = new HashMap<String, BufferedImage>();

    private static final int seaLevel = 50;
    private static final int mountLevel = 200;

    private boolean loaded;

    private BufferedImage img = null;

    public CMapFragment(int[][] hmap, int idX, int idY) {
        super(hmap, idX, idY);
    }

    public CMapFragment() {
        super();
        loaded = false;
    }

    boolean isLoaded() {
        return loaded;
    }

    void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void draw(Graphics g, int x, int y, Dimension panelDim) {
        // Test
        if (summerTiles.size() <= 0) {
            BufferedImage summerImage = loadImage("img/summer.png");

            summerTiles.put("water_dirt_01", summerImage.getSubimage(448, 384, 32, 32));
            summerTiles.put("water_dirt_02", summerImage.getSubimage(480, 384, 32, 32));
            summerTiles.put("water_dirt_03", summerImage.getSubimage(0, 416, 32, 32));
            summerTiles.put("water_dirt_04", summerImage.getSubimage(32, 416, 32, 32));
            summerTiles.put("water_dirt_05", summerImage.getSubimage(64, 416, 32, 32));
            summerTiles.put("water_dirt_06", summerImage.getSubimage(96, 416, 32, 32));
            summerTiles.put("water_dirt_07", summerImage.getSubimage(128, 416, 32, 32));
            summerTiles.put("water_dirt_08", summerImage.getSubimage(160, 416, 32, 32));
            summerTiles.put("water_dirt_09", summerImage.getSubimage(192, 416, 32, 32));
            summerTiles.put("water_dirt_10", summerImage.getSubimage(224, 416, 32, 32));
            summerTiles.put("water_dirt_11", summerImage.getSubimage(256, 416, 32, 32));
            summerTiles.put("water_dirt_12", summerImage.getSubimage(288, 416, 32, 32));
            summerTiles.put("water_dirt_13", summerImage.getSubimage(320, 416, 32, 32));
            summerTiles.put("water_dirt_14", summerImage.getSubimage(352, 416, 32, 32));
            summerTiles.put("water_dirt_15", summerImage.getSubimage(384, 416, 32, 32));
            summerTiles.put("water_dirt_16", summerImage.getSubimage(416, 416, 32, 32));
            summerTiles.put("water_dirt_17", summerImage.getSubimage(448, 416, 32, 32));
            summerTiles.put("water_dirt_18", summerImage.getSubimage(480, 416, 32, 32));
            summerTiles.put("water_dirt_19", summerImage.getSubimage(0, 448, 32, 32));
            summerTiles.put("water_dirt_20", summerImage.getSubimage(32, 448, 32, 32));
            summerTiles.put("water_dirt_21", summerImage.getSubimage(64, 448, 32, 32));
            summerTiles.put("water_dirt_22", summerImage.getSubimage(96, 448, 32, 32));
            summerTiles.put("water_dirt_23", summerImage.getSubimage(128, 448, 32, 32));
            summerTiles.put("water_dirt_24", summerImage.getSubimage(160, 448, 32, 32));
            summerTiles.put("water_dirt_25", summerImage.getSubimage(192, 448, 32, 32));
            summerTiles.put("water_dirt_26", summerImage.getSubimage(224, 448, 32, 32));
            summerTiles.put("water_dirt_27", summerImage.getSubimage(256, 448, 32, 32));
            summerTiles.put("water_dirt_28", summerImage.getSubimage(288, 448, 32, 32));
            summerTiles.put("water_dirt_29", summerImage.getSubimage(320, 448, 32, 32));
            summerTiles.put("water_dirt_30", summerImage.getSubimage(352, 448, 32, 32));
            summerTiles.put("water_dirt_31", summerImage.getSubimage(384, 448, 32, 32));
            summerTiles.put("water_dirt_32", summerImage.getSubimage(416, 448, 32, 32));

            summerTiles.put("water_29", summerImage.getSubimage(256, 640, 32, 32));
            summerTiles.put("water_30", summerImage.getSubimage(288, 640, 32, 32));
            summerTiles.put("water_31", summerImage.getSubimage(320, 640, 32, 32));
            summerTiles.put("dirt_01", summerImage.getSubimage(448, 640, 32, 32));
        }

        // create map image once
        if (img == null) {
            img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics tmpG = img.getGraphics();

            for (int i = 0; (i + 1) * MapFragment.cellWidth <= width; i++) {
                for (int j = 0; (j + 1) * MapFragment.cellHeight <= height; j++) {
                    tmpG.drawImage(summerTiles.get(getTileKey(i, j)),
                                i * MapFragment.cellWidth,
                                j * MapFragment.cellHeight,
                                null);
                }
            }
        }

        g.drawImage(img, x, y, null);

        if (debugLevel > 1) {
            Color savedColor = g.getColor();
            String lable;
            g.setColor(Color.BLACK);

            g.drawRect(x + 1,
                       y + 1,
                       CMapFragment.width - 1,
                       CMapFragment.height - 1);

            int[][] hmap = getHmap();
            int n = hmap[0].length;
            FontMetrics metrics = g.getFontMetrics();
            int renderedLableWidth;

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    g.drawRect(x + j * MapFragment.cellWidth,
                               y + i * MapFragment.cellHeight,
                               MapFragment.cellWidth,
                               MapFragment.cellHeight);
                    //lable = "" + hmap[i][j];
                    lable = isWater(j, i) ? "w" : "!w";
                    renderedLableWidth = metrics.stringWidth(lable);
                    g.drawString(lable,
                                 x + j * MapFragment.cellWidth + MapFragment.cellWidth / 2 - renderedLableWidth / 2,
                                 y + i * MapFragment.cellHeight + MapFragment.cellHeight / 2);
                }
            }

            g.setColor(savedColor);
        }

        for (int i = 0; i < summerTiles.size(); i++) {
            g.drawImage(summerTiles.get(i), x + i * 32, y, null);
        }
    }

    private void loadTile(String name, String path) {
        ClassLoader cl = this.getClass().getClassLoader();
        URL url = null;

        try {
            url = cl.getResource(path);
            if (url != null) {
                tiles.put(name, ImageIO.read(url));
            } else {
                tiles.put(name, ImageIO.read(new File(path)));
            }
        } catch (IOException e) {
            System.err.println(path);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private BufferedImage loadImage(String path) {
        ClassLoader cl = this.getClass().getClassLoader();
        URL url = null;

        try {
            url = cl.getResource(path);
            if (url != null) {
                return ImageIO.read(url);
            } else {
                return ImageIO.read(new File(path));
            }
        } catch (IOException e) {
            System.err.println(path);
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    private boolean isWater(int i, int j) {
        int[][] hmap = getHmap();

        int n = hmap[0].length; // hmap always n x n

        if (i >= 0 && j >= 0 && i < n && j < n) {
            return hmap[i][j] <= seaLevel;
        } else {
            return true;
        }
    }

    private boolean isMountian(int i, int j) {
        int[][] hmap = getHmap();

        int n = hmap[0].length; // hmap always n x n

        if (i >= 0 && j >= 0 && i < n && j < n) {
            return hmap[i][j] >= mountLevel;
        } else {
            return false;
        }
    }

    private boolean isLand(int i, int j) {
        int[][] hmap = getHmap();

        if (hmap.length == 0) {
            return true;
        }

        int n = hmap[0].length; // hmap always n x n

        if (i >= 0 && j >= 0 && i < n && j < n) {
            return hmap[i][j] > seaLevel && hmap[i][j] < mountLevel;
        } else {
            return true;
        }
    }

    private String getTileKey(int i, int j) {
        //WL
        //LL
        // <editor-fold defaultstate="collapsed" desc="comment">
        if (!isWater(i, j)
                && isWater(i - 1, j)
                && isWater(i - 1, j - 1)
                && isWater(i, j - 1)
                && !isWater(i + 1, j)
                && !isWater(i + 1, j + 1)
                && !isWater(i, j + 1)) {

            return "water_dirt_01";
        }// </editor-fold>
        //LW
        //LL
        // <editor-fold defaultstate="collapsed" desc="comment">
        if (!isWater(i, j)
                && isWater(i, j - 1)
                && isWater(i + 1, j - 1)
                && isWater(i + 1, j)
                && !isWater(i, j + 1)
                && !isWater(i - 1, j + 1)
                && !isWater(i - 1, j)) {

            return "water_dirt_03";
        }// </editor-fold>
        //LL
        //WL
        // <editor-fold defaultstate="collapsed" desc="comment">
        if (!isWater(i, j)
                && !isWater(i, j - 1)
                && !isWater(i + 1, j - 1)
                && !isWater(i + 1, j)
                && isWater(i, j + 1)
                && isWater(i - 1, j + 1)
                && isWater(i - 1, j)) {

            return "water_dirt_08";
        }// </editor-fold>
        //LL
        //LW
        // <editor-fold defaultstate="collapsed" desc="comment">
        if (!isWater(i, j)
                && !isWater(i - 1, j)
                && !isWater(i - 1, j - 1)
                && !isWater(i, j - 1)
                && isWater(i + 1, j)
                && isWater(i + 1, j + 1)
                && isWater(i, j + 1)) {

            return "water_dirt_16";
        }// </editor-fold>
        //WW
        //LL
        // <editor-fold defaultstate="collapsed" desc="comment">
        if (!isWater(i, j)
                && isWater(i - 1, j - 1)
                && isWater(i, j - 1)
                && isWater(i + 1, j - 1)
                && !isWater(i - 1, j + 1)
                && !isWater(i, j + 1)
                && !isWater(i + 1, j + 1)) {

            return "water_dirt_05";
        }// </editor-fold>
        //WL
        //WL
        // <editor-fold defaultstate="collapsed" desc="comment">
        if (!isWater(i, j)
                && !isWater(i + 1, j - 1)
                && !isWater(i + 1, j)
                && !isWater(i + 1, j + 1)
                && isWater(i - 1, j - 1)
                && isWater(i - 1, j)
                && isWater(i - 1, j + 1)) {

            return "water_dirt_10";
        }// </editor-fold>
        if (isWater(i, j)
                && !isWater(i - 1, j - 1)
                && !isWater(i + 1, j + 1)
                && isWater(i + 1, j - 1)
                && isWater(i - 1, j + 1)) {

            return "water_dirt_13";
        }
        if (!isWater(i, j)
                && !isWater(i + 1, j)
                && !isWater(i + 1, j + 1)
                && !isWater(i, j + 1)
                && isWater(i - 1, j)
                && isWater(i - 1, j - 1)
                && isWater(i, j - 1)) {

            return "water_dirt_14";
        }
        if (isWater(i, j)
                && isWater(i - 1, j - 1)
                && isWater(i + 1, j + 1)
                && !isWater(i + 1, j - 1)
                && !isWater(i - 1, j + 1)) {

            return "water_dirt_18";
        }
        if (isWater(i, j)
                && isWater(i + 1, j - 1)
                && isWater(i + 1, j)
                && isWater(i + 1, j + 1)
                && !isWater(i - 1, j - 1)
                && !isWater(i - 1, j)
                && !isWater(i - 1, j + 1)) {

            return "water_dirt_19";
        }
        if (isWater(i, j)
                && isWater(i, j - 1)
                && isWater(i + 1, j - 1)
                && isWater(i + 1, j)
                && !isWater(i, j + 1)
                && !isWater(i - 1, j + 1)
                && !isWater(i - 1, j)) {

            return "water_dirt_22";
        }
        if (isWater(i, j)
                && !isWater(i - 1, j - 1)
                && !isWater(i, j - 1)
                && !isWater(i + 1, j - 1)
                && isWater(i + 1, j + 1)
                && isWater(i, j + 1)
                && isWater(i - 1, j + 1)) {

            return "water_dirt_24";
        }
        if (isWater(i, j)
                && !isWater(i, j - 1)
                && !isWater(i + 1, j - 1)
                && !isWater(i + 1, j)
                && isWater(i, j + 1)
                && isWater(i - 1, j + 1)
                && isWater(i - 1, j)) {

            return "water_dirt_27";
        }
        if (isWater(i, j)
                && !isWater(i - 1, j)
                && !isWater(i - 1, j - 1)
                && !isWater(i, j - 1)
                && isWater(i + 1, j)
                && isWater(i + 1, j + 1)
                && isWater(i, j + 1)) {

            return "water_dirt_29";
        }
        if (!isWater(i, j)
                && !isWater(i - 1, j - 1)
                && !isWater(i + 1, j + 1)
                && isWater(i + 1, j - 1)
                && isWater(i - 1, j + 1)) {

            return "water_dirt_31";
        }
        if (!isWater(i, j)
                && isWater(i - 1, j - 1)
                && isWater(i + 1, j + 1)
                && !isWater(i + 1, j - 1)
                && !isWater(i - 1, j + 1)) {

            return "water_dirt_32";
        }
        if (isWater(i, j)) {
            return "water_29";
        }
        return "dirt_01";

//        if (isMountian(i, j)) {
//            return "m_0";
//        } else if (isWater(i, j)) {
//            if (isWater(i + 1, j) && isWater(i, j + 1)
//                    && isWater(i - 1, j) && isWater(i, j - 1)) {
//
//                return "w_0";
//            } else if (!isWater(i - 1, j) && isWater(i, j + 1)
//                    && isWater(i + 1, j) && !isWater(i, j - 1)) {
//
//                return "w_1_0";
//            } else if (!isWater(i - 1, j) && !isWater(i, j + 1)
//                    && isWater(i + 1, j) && isWater(i, j - 1)) {
//
//                return "w_1_3";
//            } else if (isWater(i - 1, j) && !isWater(i, j + 1)
//                    && !isWater(i + 1, j) && isWater(i, j - 1)) {
//
//                return "w_1_2";
//            } else if (isWater(i - 1, j) && isWater(i, j + 1)
//                    && !isWater(i + 1, j) && !isWater(i, j - 1)) {
//
//                return "w_1_1";
//            } else if (isWater(i - 1, j) && isWater(i, j + 1)
//                    && isWater(i + 1, j) && !isWater(i, j - 1)) {
//
//                return "w_2_0";
//            } else if (isWater(i - 1, j) && isWater(i, j + 1)
//                    && !isWater(i + 1, j) && isWater(i, j - 1)) {
//
//                return "w_2_1";
//            } else if (isWater(i - 1, j) && !isWater(i, j + 1)
//                    && isWater(i + 1, j) && isWater(i, j - 1)) {
//
//                return "w_2_2";
//            } else if (!isWater(i - 1, j) && isWater(i, j + 1)
//                    && isWater(i + 1, j) && isWater(i, j - 1)) {
//
//                return "w_2_3";
//
//            }else if (!isWater(i - 1, j) && !isWater(i, j + 1)
//                    && !isWater(i + 1, j) && isWater(i, j - 1)) {
//                return "w_5_1";
//            }else if (isWater(i - 1, j) && !isWater(i, j + 1)
//                    && !isWater(i + 1, j) && !isWater(i, j - 1)) {
//                return "w_5_0";
//            }else if (!isWater(i - 1, j) && isWater(i, j + 1)
//                    && !isWater(i + 1, j) && !isWater(i, j - 1)) {
//                return "w_5_3";
//            }else if (!isWater(i - 1, j) && !isWater(i, j + 1)
//                    && isWater(i + 1, j) && !isWater(i, j - 1)) {
//                return "w_5_2";
//            } else if (!isWater(i - 1, j) && !isWater(i, j + 1)
//                    && !isWater(i + 1, j) && !isWater(i, j - 1)) {
//                return "w_6_0";
//            } else if (isWater(i - 1, j) && !isWater(i, j + 1)
//                    && isWater(i + 1, j) && !isWater(i, j - 1)) {
//                return "w_7_0";
//            } else if (!isWater(i - 1, j) && isWater(i, j + 1)
//                    && !isWater(i + 1, j) && isWater(i, j - 1)) {
//                return "w_7_1";
//            } else {
//                return "w_0";
//            }
//        } else {
//            return "l_0";
//        }
    }
}
