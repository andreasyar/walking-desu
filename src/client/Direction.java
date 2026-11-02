package client;

import java.awt.Point;
import java.util.Random;

public enum Direction {
    EAST,
    NORTH_EAST,
    NORTH,
    NORTH_WEST,
    WEST,
    SOUTH_WEST,
    SOUTH,
    SOUTH_EAST;

    public static Direction getRandom() {
        Direction[] d = Direction.values();
        Random r = new Random();
        return d[r.nextInt(d.length)];
    }

    public static Direction getDirection(int begX, int begY, int endX, int endY) {
        double angle = angle(begX, begY, endX, endY);
        if (angle > 337.5 || angle >= 0.0 && angle <= 22.5) {
            return EAST;
        } else if (angle > 22.5 && angle <= 67.5) {
            return NORTH_EAST;
        } else if (angle > 67.5 && angle <= 102.5) {
            return NORTH;
        } else if (angle > 102.5 && angle <= 157.5) {
            return NORTH_WEST;
        } else if (angle > 157.5 && angle <= 202.5) {
            return WEST;
        } else if (angle > 202.5 && angle <= 247.5) {
            return SOUTH_WEST;
        } else if (angle > 247.5 && angle <= 292.5) {
            return SOUTH;
        } else {
            return SOUTH_EAST;
        }
    }

    public static Direction getDirection(Point beg, Point end) {
        return getDirection(beg.x, beg.y, end.x, end.y);
    }

    private static double angle(int aX, int aY, int bX, int bY) {
        // TANK YOU prometheuzz @ forums.sun.com
        double dx = bX - aX;
        double dy = bY - aY;
        double angle = 0.0d;

        if (dx == 0.0) {
            if(dy == 0.0)     angle = 0.0;
            else if(dy > 0.0) angle = Math.PI / 2.0;
            else              angle = (Math.PI * 3.0) / 2.0;
        }
        else if(dy == 0.0) {
            if(dx > 0.0)      angle = 0.0;
            else              angle = Math.PI;
        }
        else {
            if(dx < 0.0)      angle = Math.atan(dy/dx) + Math.PI;
            else if(dy < 0.0) angle = Math.atan(dy/dx) + (2*Math.PI);
            else              angle = Math.atan(dy/dx);
        }
        return Math.abs(360 - (angle * 180) / Math.PI);
    }

    private static double angle(Point a, Point b) {
        return angle(a.x, a.y, b.x, b.y);
    }
}
