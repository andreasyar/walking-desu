package wand6.client;

import wand6.common.MapFragment;

record Camera(int x, int y) {
    static Camera follow(int x, int y, int width, int height) {
        return new Camera(axis(x, width, MapFragment.maxX), axis(y, height, MapFragment.maxY));
    }
    private static int axis(int position, int size, int max) {
        return (int) Math.clamp((long) position - size / 2, 0, Math.max(0L, (long) max + 1 - size));
    }
}
