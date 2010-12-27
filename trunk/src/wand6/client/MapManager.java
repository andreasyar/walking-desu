package wand6.client;

import java.awt.Dimension;
import java.awt.Graphics;

class MapManager {

    private static MapManager self = null;

    static MapManager getInstance() {
        if (self == null) {
            self = new MapManager();
        }

        return self;
    }

    private MapManager() {}

    void drawMap(Graphics g, Dimension panelDim) {
        int panelX, panelY;
    }
}
