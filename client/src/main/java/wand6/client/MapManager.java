package wand6.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;
import wand6.common.MapFragment;
import wand6.common.exceptions.MessageManagerException;

class MapManager {
    private static final MapManager INSTANCE = new MapManager();
    private final Map<Long, CMapFragment> fragments = new HashMap<>();
    static MapManager getInstance() { return INSTANCE; }
    private static long key(int x, int y) { return ((long) y << 32) | x; }
    synchronized boolean isLoaded(int x, int y) {
        var fragment = fragments.get(key(x, y));
        return fragment != null && fragment.isLoaded();
    }
    synchronized void drawMap(Graphics g, Dimension size, Camera camera) {
        int right = (int) Math.min(MapFragment.maxIdX, ((long) camera.x() + size.width - 1) / MapFragment.width);
        int bottom = (int) Math.min(MapFragment.maxIdY, ((long) camera.y() + size.height - 1) / MapFragment.height);
        for (int y = camera.y() / MapFragment.height; y <= bottom; y++) {
            for (int x = camera.x() / MapFragment.width; x <= right; x++) {
                CMapFragment fragment = fragments.get(key(x, y));
                if (fragment == null) {
                    try {
                        MessageManager.sendMapFragmentRequest(x, y);
                        fragments.put(key(x, y), new CMapFragment());
                    } catch (MessageManagerException ex) { System.err.println(ex.getMessage()); }
                } else if (fragment.isLoaded()) {
                    fragment.draw(g, x * MapFragment.width - camera.x(),
                            y * MapFragment.height - camera.y(), size);
                } else {
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(x * MapFragment.width - camera.x(), y * MapFragment.height - camera.y(),
                            MapFragment.width, MapFragment.height);
                }
            }
        }
    }
    synchronized void addMapFragment(int x, int y, int[][] heights) {
        CMapFragment fragment = new CMapFragment(heights, x, y);
        // Bake here (message flush), not on first paint mid-walk.
        fragment.bake();
        fragment.setLoaded(true);
        fragments.put(key(x, y), fragment);
    }
}
