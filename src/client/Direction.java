package client;

import java.awt.Point;

public enum Direction {
    NORTH,
    NORTH_WEST,
    NORTH_EAST,
    SOUTH,
    SOUTH_WEST,
    SOUTH_EAST;

    public static Direction getDirection(Point beg, Point end) {
        int diffX = end.x - beg.x;
        int diffY = end.y - beg.y;

        if (diffX > 0 && diffY <= 0 && diffX > -diffY) {
            return NORTH_EAST;
        } else if (diffX > 0 && diffY < 0 && diffX <= -diffY) {
            return NORTH;
        } else if (diffX <= 0 && diffY < 0 && diffX > diffY) {
            return NORTH;
        } else if (diffX < 0 && diffY < 0 && diffX <= diffY) {
            return NORTH_WEST;
        } else if (diffX < 0 && diffY >= 0 && -diffX > diffY) {
            return SOUTH_WEST;
        } else if (diffX < 0 && diffY > 0 && -diffX <= diffY) {
            return SOUTH;
        } else if (diffX >= 0 && diffY > 0 && diffX < diffY) {
            return SOUTH;
        } else if (diffX > 0 && diffY > 0 && diffX >= diffY) {
            return SOUTH_EAST;
        } else if (diffX == 0 && diffY == 0) {
            return SOUTH; // TODO wtf?
        } else {
            return null;
        }
    }
}
