package wand6.common;

/** Deterministic global height field: adjacent chunks sample the same world.
 * Shared with the renderer for the one-cell halo used by shoreline tiles.
 */
public final class Terrain {
    private Terrain() { }
    public static int height(long cellX, long cellY) {
        long gx = Math.floorDiv(cellX, 16), gy = Math.floorDiv(cellY, 16);
        double tx = Math.floorMod(cellX, 16) / 16.0;
        double ty = Math.floorMod(cellY, 16) / 16.0;
        tx = tx * tx * (3 - 2 * tx); ty = ty * ty * (3 - 2 * ty);
        double top = noise(gx, gy) * (1 - tx) + noise(gx + 1, gy) * tx;
        double bottom = noise(gx, gy + 1) * (1 - tx) + noise(gx + 1, gy + 1) * tx;
        // The initial area is a safe land plateau, tapering into the world.
        double spawn = Math.max(0, 1 - Math.hypot(cellX - 16, cellY - 16) / 48.0);
        return (int) Math.round(Math.max(top * (1 - ty) + bottom * ty, 120 * spawn));
    }
    private static int noise(long x, long y) {
        long z = x * 0x9e3779b97f4a7c15L + y * 0x632be59bd9b4e019L + 42;
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return (int) Math.floorMod(z ^ (z >>> 31), 256);
    }
    public static boolean passable(int x, int y) {
        if (!MapFragment.contains(x, y)) return false;
        int h = height(x / MapFragment.cellWidth, y / MapFragment.cellHeight);
        return h > 50 && h < 200;
    }
}
