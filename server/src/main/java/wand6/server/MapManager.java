package wand6.server;

import java.util.HashMap;
import java.util.Map;
import wand6.common.MapFragment;
import wand6.common.Terrain;

class MapManager {
    private final Map<Long, SMapFragment> fragments = new HashMap<>();
    synchronized SMapFragment getMapFragment(int idX, int idY) {
        if (idX < 0 || idY < 0 || idX > MapFragment.maxIdX || idY > MapFragment.maxIdY) {
            throw new IllegalArgumentException("Fragment outside world: " + idX + "," + idY);
        }
        long key = ((long) idY << 32) | idX;
        return fragments.computeIfAbsent(key, ignored -> {
            int[][] heights = new int[32][32];
            for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) {
                heights[y][x] = Terrain.height((long) idX * 32 + x, (long) idY * 32 + y);
            }
            return new SMapFragment(heights, idX, idY);
        });
    }
}
