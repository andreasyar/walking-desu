package wand6.server;

import java.util.Arrays;
import java.util.concurrent.Executors;
import wand6.common.MapFragment;
import wand6.common.Terrain;

public final class MapCheck {
    public static void main(String[] args) throws Exception {
        int[][] ids = {{0,0},{1,0},{0,1},{1,1},{MapFragment.maxIdX,MapFragment.maxIdY}};
        MapManager forward = new MapManager(), reverse = new MapManager();
        for (int[] id : ids) forward.getMapFragment(id[0],id[1]);
        for (int i=ids.length-1;i>=0;i--) reverse.getMapFragment(ids[i][0],ids[i][1]);
        for (int[] id : ids) {
            var fragment = forward.getMapFragment(id[0],id[1]);
            require(Arrays.deepEquals(fragment.getHmap(),reverse.getMapFragment(id[0],id[1]).getHmap()), "request order");
            require(fragment == forward.getMapFragment(id[0],id[1]), "cache repeat");
            for(int y=0;y<32;y++) for(int x=0;x<32;x++) {
                require(fragment.getHmap()[y][x] == Terrain.height((long) id[0]*32+x,(long) id[1]*32+y), "global samples including boundaries");
            }
        }
        // There are no duplicated border cells: the next chunk continues at cell 32.
        for (int y=0;y<32;y++) {
            require(forward.getMapFragment(1,0).getHmap()[y][0] == Terrain.height(32,y), "east seam");
            require(forward.getMapFragment(0,1).getHmap()[0][y] == Terrain.height(y,32), "south seam");
        }
        for (int[] bad : new int[][]{{-1,0},{0,-1},{MapFragment.maxIdX+1,0},{0,Integer.MAX_VALUE}}) {
            try { forward.getMapFragment(bad[0],bad[1]); throw new AssertionError("invalid accepted"); }
            catch (IllegalArgumentException expected) { }
        }
        try (var executor = Executors.newFixedThreadPool(4)) {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<SMapFragment>>();
            for(int i=0;i<20;i++) futures.add(executor.submit(() -> forward.getMapFragment(2,2)));
            for(var future:futures) require(future.get() == forward.getMapFragment(2,2), "concurrent cache");
        }
        System.out.println("PASS: map request order, repeat, seams, world boundary, invalid IDs, concurrent requests.");
    }
    private static void require(boolean condition,String label) { if(!condition) throw new AssertionError(label); }
}
