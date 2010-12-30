package wand6.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.HashMap;
import wand6.client.exceptions.MessageManagerException;
import wand6.common.MapFragment;

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

        int leftFragmIdX, leftFragmIdY, rightFragmIdX, rightFragmIdY;

        if (panelX < 0) {
            leftFragmIdX = 0;
        } else {
            leftFragmIdX = panelX / CMapFragment.width;
        }
        if (panelY < 0) {
            leftFragmIdY = 0;
        } else {
            leftFragmIdY = panelY / CMapFragment.height;
        }
        if (panelDim.width > MapFragment.maxIdX * MapFragment.width - (panelX < 0 ? 0 : panelX)) {
            rightFragmIdX = MapFragment.maxIdX;
        } else {
            rightFragmIdX = (panelX + panelDim.width) / MapFragment.width + 1;
        }
        if (panelDim.height > MapFragment.maxIdY * MapFragment.height - (panelY < 0 ? 0 : panelY)) {
            rightFragmIdY = MapFragment.maxIdY;
        } else {
            rightFragmIdY = (panelY + panelDim.height) / MapFragment.height + 1;
        }

        for (int x = leftFragmIdX; x <= rightFragmIdX; x++) {
            for (int y = leftFragmIdY; y <= rightFragmIdY; y++) {
                if (fragmentVisible(x, y, panelX, panelY, panelDim)) {
                    drawFragment(g,
                                 x,
                                 y,
                                 x * CMapFragment.width - panelX,
                                 y * CMapFragment.height - panelY,
                                 "",
                                 panelDim);
                }
            }
        }
    }

    private boolean fragmentVisible(int idX, int idY, int panelX, int panelY, Dimension panelDim) {
        if (idX * MapFragment.width >= panelX + panelDim.width) {
            if (debugLevel > 0) {
                //System.out.println("Фрагмент справа за границей панели");
            }
            return false;   // справа за границей панели
        }

        if (idY * MapFragment.height >= panelY + panelDim.height) {
            if (debugLevel > 0) {
                //System.out.println("Фрагмент idX=" + idX + " idY=" + idY + " снизу за границей панели: " + (idY * MapFragment.height) + " >= " + (panelY + panelDim.height) + ".");
            }
            return false;   // снизу за границей панели
        }

        if (idX * MapFragment.width + MapFragment.width <= panelX) {
            if (debugLevel > 0) {
                //System.out.println("Фрагмент слева за границей панели");
            }
            return false;   // слева за границей панели
        }

        if (idY * MapFragment.height + MapFragment.height <= panelY) {
            if (debugLevel > 0) {
                //System.out.println("Фрагмент сверху за границей панели");
            }
            return false;   // сверху за границей панели
        }

        if (debugLevel > 0) {
            //System.out.println("Фрагмент idX=" + idX + " idY=" + idY + " виден.");
        }
        return true;
    }

    private void drawFragment(Graphics g, int idX, int idY, int x, int y, String suffix, Dimension panelDim) {
        Color savedColor = g.getColor();
        String lable;
        FontMetrics metrics = g.getFontMetrics();
        int renderedLableWidth;

        if (fragments.containsKey(idY)) {
            HashMap row = fragments.get(idY);
            if (row.containsKey(idX)) {
                CMapFragment fragment = (CMapFragment) row.get(idX);
                if (fragment.isLoaded()) {
                    fragment.draw(g, x, y, panelDim);
                    /*lable = idX + "," + idY + " " + suffix;
                    renderedLableWidth = metrics.stringWidth(lable);
                    g.drawString(lable,
                                 x + CMapFragment.width / 2 - renderedLableWidth / 2,
                                 y + CMapFragment.height / 2);*/
                } else {
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
                try {
                    fragments.get(idY).put(idX, new CMapFragment());
                    MessageManager.sendMapFragmentRequest(idX, idY);
                } catch (MessageManagerException e) {
                    System.err.println(e.getMessage());
                }
                g.setColor(Color.RED);
                g.drawRect(x + 1,
                           y + 1,
                           CMapFragment.width - 1,
                           CMapFragment.height - 1);
                lable = idX + "," + idY + " " + suffix + "request... ";
                renderedLableWidth = metrics.stringWidth(lable);
                g.drawString(lable,
                             x + CMapFragment.width / 2 - renderedLableWidth / 2,
                             y + CMapFragment.height / 2);
                g.setColor(savedColor);
            }
        } else {
            try {
                fragments.put(idY, new HashMap<Integer, CMapFragment>());
                fragments.get(idY).put(idX, new CMapFragment());
                MessageManager.sendMapFragmentRequest(idX, idY);
            } catch (MessageManagerException e) {
                System.err.println(e.getMessage());
            }
            g.setColor(Color.RED);
            g.drawRect(x,
                       y,
                       CMapFragment.width,
                       CMapFragment.height);
            lable = idX + "," + idY + " " + suffix + "request... ";
            renderedLableWidth = metrics.stringWidth(lable);
            g.drawString(lable,
                         x + CMapFragment.width / 2 - renderedLableWidth / 2,
                         y + CMapFragment.height / 2);
            g.setColor(savedColor);
        }
    }

    void addMapFragment(int idX, int idY, int[][] hmap) {
        synchronized (fragments) {
            if (!fragments.containsKey(idY)) {
                fragments.put(idY, new HashMap<Integer, CMapFragment>());
            }

            CMapFragment fragment;
            if (fragments.get(idY).containsKey(idX)) {
                fragment = fragments.get(idY).get(idX);
                fragment.setIdX(idX);
                fragment.setIdY(idY);
                fragment.setHmap(hmap);
                fragment.setLoaded(true);
                if (debugLevel > 0) {
                    System.out.println("Fragment idX=" + idX + " idY=" + idY + " already exist. Set params.");
                }
            } else {
                fragment = new CMapFragment(hmap, idX, idY);
                fragment.setLoaded(true);
                fragments.get(idY).put(idX, fragment);
                if (debugLevel > 0) {
                    System.out.println("Create new fragment idX=" + idX + " idY=" + idY + ".");
                }
            }
        }
    }
}
