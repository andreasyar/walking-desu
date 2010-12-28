package wand6.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.HashMap;

class MapManager {

    private final static int debugLevel = 1;

    private static MapManager self = null;

    private final HashMap<Integer, HashMap<Integer, CMapFragment>> fragments = new HashMap<Integer, HashMap<Integer, CMapFragment>>();

    static MapManager getInstance() {
        if (self == null) {
            self = new MapManager();
        }

        return self;
    }

    private MapManager() {}

    void drawMap(Graphics g, Dimension panelDim) {
        int selfPlayerX, selfPlayerY;
        
        try {
            selfPlayerX = PlayerManager.getInstance().getSelfPlayerX();
        } catch (NullPointerException e) {
            if (debugLevel > 0) {
                System.err.println("Self player X-axis cannot be calculated.");
            }
            return;
        }
        try {
            selfPlayerY = PlayerManager.getInstance().getSelfPlayerY();
        } catch (NullPointerException e) {
            if (debugLevel > 0) {
                System.err.println("Self player Y-axis cannot be calculated.");
            }
            return;
        }

        // Upper left corner of drawing panel on world map.
        int panelX = selfPlayerX - panelDim.width / 2;
        int panelY = selfPlayerY - panelDim.height / 2;

        // Map fragment what contains upper left corner of drawing panel.
        int fragmIdX, fragmIdY;

        if (panelX < 0) {
            if (panelX % CMapFragment.width > 0) {
                fragmIdX = CMapFragment.maxIdX + panelX / CMapFragment.width - 1;
            } else {
                fragmIdX = CMapFragment.maxIdX + panelX / CMapFragment.width;
            }
        } else {
            if (panelX % CMapFragment.width > 0) {
                fragmIdX = panelX / CMapFragment.width + 1;
            } else {
                fragmIdX = panelX / CMapFragment.width;
            }
        }
        if (panelY < 0) {
            if (panelY % CMapFragment.height > 0) {
                fragmIdY = CMapFragment.maxIdY + panelY / CMapFragment.height - 1;
            } else {
                fragmIdY = CMapFragment.maxIdY + panelY / CMapFragment.height;
            }
        } else {
            if (panelY % CMapFragment.height > 0) {
                fragmIdY = panelY / CMapFragment.height + 1;
            } else {
                fragmIdY = panelY / CMapFragment.height;
            }
        }

        // Count of map fragments waht must be shown on the screen.
        int fragmN = panelDim.width / CMapFragment.width + (panelDim.width % CMapFragment.width > 0 ? 1 : 0);
        int fragmM = panelDim.height / CMapFragment.height + (panelDim.height % CMapFragment.height > 0 ? 1 : 0);

        if (debugLevel > 0) {
            System.out.println("fragmIdX=" + fragmIdX + " fragmIdY=" + fragmIdY + " fragmN=" + fragmN + " fragmM=" + fragmM);
        }

        if (fragmIdX + fragmN > CMapFragment.maxIdX && fragmIdY + fragmM <= CMapFragment.maxIdY) {

            for (int x = fragmIdX; x <= CMapFragment.maxIdX; x++) {
                for (int y = fragmIdY; y < fragmIdY + fragmM; y++) {
                    drawFragment(g,
                                 x,
                                 y,
                                 x * CMapFragment.width - panelX,
                                 y * CMapFragment.height - panelY,
                                 "1 ");
                }
            }
            for (int x = 0; x < fragmN - (CMapFragment.maxIdX - fragmIdX); x++) {
                for (int y = fragmIdY; y < fragmIdY + fragmM; y++) {
                    drawFragment(g,
                                 x,
                                 y,
                                 (CMapFragment.maxIdX * CMapFragment.width - panelX) + (x + 1) * CMapFragment.width,
                                 y * CMapFragment.height - panelY,
                                 "1* ");
                }
            }

            if (debugLevel > 0) {
                System.out.println("Case 1");
            }

        } else if (fragmIdX + fragmN > CMapFragment.maxIdX && fragmIdY + fragmM > CMapFragment.maxIdY) {

            for (int x = fragmIdX; x <= CMapFragment.maxIdX; x++) {
                for (int y = fragmIdY; y <= CMapFragment.maxIdY; y++) {
                    drawFragment(g,
                                 x,
                                 y,
                                 x * CMapFragment.width - panelX,
                                 y * CMapFragment.height - panelY,
                                 "2 ");
                }
            }
            for (int x = 0; x < fragmN - (CMapFragment.maxIdX - fragmIdX); x++) {
                for (int y = fragmIdY; y <= CMapFragment.maxIdY; y++) {
                    drawFragment(g,
                                 x,
                                 y,
                                 (CMapFragment.maxIdX * CMapFragment.width - panelX) + (x + 1) * CMapFragment.width,
                                 y * CMapFragment.height - panelY,
                                 "2* ");
                }
            }
            for (int x = fragmIdX; x <= CMapFragment.maxIdX; x++) {
                for (int y = 0; y < fragmM - (CMapFragment.maxIdY - fragmIdY); y++) {
                    drawFragment(g,
                                 x,
                                 y,
                                 x * CMapFragment.width - panelX,
                                 (CMapFragment.maxIdY * CMapFragment.height - panelY) + (y + 1) * CMapFragment.height,
                                 "2** ");
                }
            }
            for (int x = 0; x < fragmN - (CMapFragment.maxIdX - fragmIdX); x++) {
                for (int y = 0; y < fragmM - (CMapFragment.maxIdY - fragmIdY); y++) {
                    drawFragment(g,
                                 x,
                                 y,
                                 (CMapFragment.maxIdX * CMapFragment.width - panelX) + (x + 1) * CMapFragment.width,
                                 (CMapFragment.maxIdY * CMapFragment.height - panelY) + (y + 1) * CMapFragment.height,
                                 "2*** ");
                }
            }

            if (debugLevel > 0) {
                System.out.println("Case 2");
            }

        } else if (fragmIdX + fragmN <= CMapFragment.maxIdX && fragmIdY + fragmM > CMapFragment.maxIdY) {

            for (int x = fragmIdX; x < fragmIdX + fragmN; x++) {
                for (int y = fragmIdY; y <= CMapFragment.maxIdY; y++) {
                    drawFragment(g,
                                 x,
                                 y,
                                 x * CMapFragment.width - panelX,
                                 y * CMapFragment.height - panelY,
                                 "3 ");
                }
            }
            for (int x = fragmIdX; x < fragmIdX + fragmN; x++) {
                for (int y = 0; y < fragmM - (CMapFragment.maxIdY - fragmIdY); y++) {
                    drawFragment(g,
                                 x,
                                 y,
                                 x * CMapFragment.width - panelX,
                                 (CMapFragment.maxIdY * CMapFragment.height - panelY) + (y + 1) * CMapFragment.height,
                                 "3* ");
                }
            }

            if (debugLevel > 0) {
                System.out.println("Case 3");
            }

        } else {

            for (int x = fragmIdX; x < fragmIdX + fragmN; x++) {
                for (int y = fragmIdY; y < fragmIdX + fragmN; y++) {
                    drawFragment(g,
                                 x,
                                 y,
                                 x * CMapFragment.width - panelX,
                                 y * CMapFragment.height - panelY,
                                 "4 ");
                }
            }

            if (debugLevel > 0) {
                System.out.println("Case 4");
            }

        }
    }

    private void drawFragment(Graphics g, int idX, int idY, int x, int y, String suffix) {
        Color savedColor = g.getColor();
        String lable;
        FontMetrics metrics = g.getFontMetrics();
        int renderedLableWidth;

        if (fragments.containsKey(idY)) {
            HashMap row = fragments.get(idY);
            if (row.containsKey(idX)) {
                ((CMapFragment) row.get(idX)).draw(g, x, y);
                lable = idX + "," + idY + " " + suffix;
                renderedLableWidth = metrics.stringWidth(lable);
                g.drawString(lable,
                             x + CMapFragment.width / 2 - renderedLableWidth / 2,
                             y + CMapFragment.height / 2);
            } else {
                // TODO Request.
                g.setColor(Color.RED);
                g.drawRect(x + 1,
                           y + 1,
                           CMapFragment.width - 1,
                           CMapFragment.height - 1);
                lable = idX + "," + idY + " " + suffix + "loading... ";
                renderedLableWidth = metrics.stringWidth(lable);
                g.drawString(lable,
                             x + CMapFragment.width / 2 - renderedLableWidth / 2,
                             y + CMapFragment.height / 2);
                g.setColor(savedColor);
            }
        } else {
            // TODO Request.
            g.setColor(Color.RED);
            g.drawRect(x,
                       y,
                       CMapFragment.width,
                       CMapFragment.height);
            lable = idX + "," + idY + " " + suffix + "loading... ";
            renderedLableWidth = metrics.stringWidth(lable);
            g.drawString(lable,
                         x + CMapFragment.width / 2 - renderedLableWidth / 2,
                         y + CMapFragment.height / 2);
            g.setColor(savedColor);
        }
    }
}
