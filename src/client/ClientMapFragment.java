/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.imageio.ImageIO;
import server.javatestserver.MapFragment;
/**
 *
 * @author Sorc
 */
public class ClientMapFragment extends MapFragment {
    int seaLevel = 50;
    int mountLevel = 200;

    private static HashMap<String, BufferedImage> tiles
            = new HashMap<String, BufferedImage>();
    private static HashMap<String, String> tileNamesMap
            = new HashMap<String, String>();
    
    private BufferedImage img = null;

    public ClientMapFragment(int idx, int idy, int [][] hmap) {
        super(idx, idy, hmap);
        
        // load tiles
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

    public BufferedImage getImage() {

        if (img == null) {
            img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics g = img.getGraphics();
            Color myGreen = new Color((float) 0.1, (float) 1.0, (float) 0.3, (float) 0.7);
            Color myBlack = new Color((float) 0.1, (float) 0.1, (float) 0.1, (float) 0.7);
            
            for (int i = 0; (i + 1) * cellW <= width; i++) {
                for (int j = 0; (j + 1) * cellH <= height; j++) {
//                    g.setColor(myBlack);
//                    g.fillRect(i *  cellW, j * cellH, cellW, cellH);
//                    g.setColor(myGreen);
//                    g.fillRect(i * cellW + 1, j * cellH + 1, cellW - 1, cellH - 1);
//                    g.setColor(myBlack);
////                    g.drawString(idx + "," + idy, i * cellW, j * cellH + cellH / 2);
//                    g.drawString(hmap[i][j] + "", i * cellW, j * cellH + cellH / 2);
                    g.drawImage(tiles.get(getTileKey(i, j)),
                                i * cellW,
                                j * cellH,
                                null);
//                    g.setColor(myBlack);
//                    g.drawString(isWater(i, j) ? "w" : (isLand(i, j) ? "l" : "m"), i * cellW, j * cellH + cellH / 2);
                }
            }
        }
        
        return img;
    }

    /**
     * Returns tile key for i,j height map cell depends
     * height of neighbour cells.
     */
    private String getTileKey(int i, int j) {
        String name = "";

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

    private boolean isWater(int i, int j) {
        int n = hmap[0].length; // hmap always n x n
        
        if (i >= 0 && j >= 0 && i < n && j < n) {
            return hmap[i][j] <= seaLevel;
        } else {
            return true;
        }
    }

    private boolean isMountian(int i, int j) {
        int n = hmap[0].length; // hmap always n x n

        if (i >= 0 && j >= 0 && i < n && j < n) {
            return hmap[i][j] >= mountLevel;
        } else {
            return false;
        }
    }

    private boolean isLand(int i, int j) {
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

    public boolean availableCell(Point p) {
        int x, y;

        x = p.x - idx * width;
        y = p.y - idy * height;

        return isLand(y / 32, x / 32);
    }
}
