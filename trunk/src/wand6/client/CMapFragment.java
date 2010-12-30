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
    private static final ArrayList<BufferedImage> summerTiles = new ArrayList<BufferedImage>();

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
            BufferedImage summerImage = loadImage("img/tiles/summer.png");
            for (int i = 0; i < summerImage.getWidth() / 32; i++) {
                for (int j = 0; j < summerImage.getHeight() / 32; j++) {
                    summerTiles.add(summerImage.getSubimage(i * 32, j * 32, 32, 32));
                }
            }
        }

        // load tiles once
        if (tiles.size() <= 0) {
            loadTile("w_0", "img/tiles/w_0.png");
            loadTile("m_0", "img/tiles/m_0.png");
            loadTile("l_0", "img/tiles/l_0.png");

            loadTile("w_1_0", "img/tiles/w_1_0.png");
            loadTile("w_1_1", "img/tiles/w_1_1.png");
            loadTile("w_1_2", "img/tiles/w_1_2.png");
            loadTile("w_1_3", "img/tiles/w_1_3.png");
            loadTile("w_2_0", "img/tiles/w_2_0.png");
            loadTile("w_2_1", "img/tiles/w_2_1.png");
            loadTile("w_2_2", "img/tiles/w_2_2.png");
            loadTile("w_2_3", "img/tiles/w_2_3.png");

            loadTile("w_5_0", "img/tiles/w_5_0.png");
            loadTile("w_5_1", "img/tiles/w_5_1.png");
            loadTile("w_5_2", "img/tiles/w_5_2.png");
            loadTile("w_5_3", "img/tiles/w_5_3.png");

            loadTile("w_6_0", "img/tiles/w_6_0.png");

            loadTile("w_7_0", "img/tiles/w_7_0.png");
            loadTile("w_7_1", "img/tiles/w_7_1.png");

            loadTile("w_4_0", "img/tiles/w_4_0.png");
            loadTile("w_4_1", "img/tiles/w_4_1.png");
            loadTile("w_4_2", "img/tiles/w_4_2.png");
            loadTile("w_4_3", "img/tiles/w_4_3.png");
        }

        // create map image once
        if (img == null) {
            img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics tmpG = img.getGraphics();

            for (int i = 0; (i + 1) * MapFragment.cellWidth <= width; i++) {
                for (int j = 0; (j + 1) * MapFragment.cellHeight <= height; j++) {
                    tmpG.drawImage(tiles.get(getTileKey(i, j)),
                                i * MapFragment.cellWidth,
                                j * MapFragment.cellHeight,
                                null);
                }
            }
        }

        g.drawImage(img, x, y, null);
        /*if (x >= 0 && y >= 0) {
            g.drawImage(img, x, y, null);
            if (debugLevel > 0) {
                System.out.println("Fragment idX=" + getIdX() + " idY=" + getIdY() + " normally drawn at x=" + x + " y= " + y + ".");
            }
        } else if (x < 0 && y >= 0) {
            g.drawImage(img.getSubimage(Math.abs(x), 0, img.getWidth() - Math.abs(x), img.getHeight()),
                        0,
                        y,
                        null);
            if (debugLevel > 0) {
                System.out.println("Fragment idX=" + getIdX() + " idY=" + getIdY() + " was cutted from left.");
            }
        } else if (x >= 0 && y < 0) {
            g.drawImage(img.getSubimage(0, Math.abs(y), img.getWidth(), img.getHeight() - Math.abs(y)),
                        x,
                        0,
                        null);
            if (debugLevel > 0) {
                System.out.println("Fragment idX=" + getIdX() + " idY=" + getIdY() + " was cutted from top.");
            }
        } else if (x < 0 && y < 0) {
            g.drawImage(img.getSubimage(Math.abs(x), Math.abs(y), img.getWidth() - Math.abs(x), img.getHeight() - Math.abs(y)),
                        0,
                        0,
                        null);
            if (debugLevel > 0) {
                System.out.println("Fragment idX=" + getIdX() + " idY=" + getIdY() + " was cutted from left and top.");
            }
        }*/

        if (debugLevel > 1) {
            Color savedColor = g.getColor();
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
                    renderedLableWidth = metrics.stringWidth("" + hmap[i][j]);
                    g.drawString("" + hmap[i][j],
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
        if (isMountian(i, j)) {
            return "m_0";
        } else if (isWater(i, j)) {
            if (isWater(i + 1, j) && isWater(i, j + 1)
                    && isWater(i - 1, j) && isWater(i, j - 1)) {

                return "w_0";
            } else if (!isWater(i - 1, j) && isWater(i, j + 1)
                    && isWater(i + 1, j) && !isWater(i, j - 1)) {

                return "w_1_0";
            } else if (!isWater(i - 1, j) && !isWater(i, j + 1)
                    && isWater(i + 1, j) && isWater(i, j - 1)) {

                return "w_1_3";
            } else if (isWater(i - 1, j) && !isWater(i, j + 1)
                    && !isWater(i + 1, j) && isWater(i, j - 1)) {

                return "w_1_2";
            } else if (isWater(i - 1, j) && isWater(i, j + 1)
                    && !isWater(i + 1, j) && !isWater(i, j - 1)) {

                return "w_1_1";
            } else if (isWater(i - 1, j) && isWater(i, j + 1)
                    && isWater(i + 1, j) && !isWater(i, j - 1)) {

                return "w_2_0";
            } else if (isWater(i - 1, j) && isWater(i, j + 1)
                    && !isWater(i + 1, j) && isWater(i, j - 1)) {

                return "w_2_1";
            } else if (isWater(i - 1, j) && !isWater(i, j + 1)
                    && isWater(i + 1, j) && isWater(i, j - 1)) {

                return "w_2_2";
            } else if (!isWater(i - 1, j) && isWater(i, j + 1)
                    && isWater(i + 1, j) && isWater(i, j - 1)) {

                return "w_2_3";

            }else if (!isWater(i - 1, j) && !isWater(i, j + 1)
                    && !isWater(i + 1, j) && isWater(i, j - 1)) {
                return "w_5_1";
            }else if (isWater(i - 1, j) && !isWater(i, j + 1)
                    && !isWater(i + 1, j) && !isWater(i, j - 1)) {
                return "w_5_0";
            }else if (!isWater(i - 1, j) && isWater(i, j + 1)
                    && !isWater(i + 1, j) && !isWater(i, j - 1)) {
                return "w_5_3";
            }else if (!isWater(i - 1, j) && !isWater(i, j + 1)
                    && isWater(i + 1, j) && !isWater(i, j - 1)) {
                return "w_5_2";
            } else if (!isWater(i - 1, j) && !isWater(i, j + 1)
                    && !isWater(i + 1, j) && !isWater(i, j - 1)) {
                return "w_6_0";
            } else if (isWater(i - 1, j) && !isWater(i, j + 1)
                    && isWater(i + 1, j) && !isWater(i, j - 1)) {
                return "w_7_0";
            } else if (!isWater(i - 1, j) && isWater(i, j + 1)
                    && !isWater(i + 1, j) && isWater(i, j - 1)) {
                return "w_7_1";
            } else {
                return "w_0";
            }
        } else {
            return "l_0";
        }
    }
}
