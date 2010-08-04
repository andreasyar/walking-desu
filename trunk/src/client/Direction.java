package client;

import java.awt.Point;

public enum Direction {
    EAST,
    NORTH_EAST,
    NORTH,
    NORTH_WEST,
    WEST,
    SOUTH_WEST,
    SOUTH,
    SOUTH_EAST;

    public static Direction getDirection(Point beg, Point end) {
        double angle = angle(beg, end);
        //System.out.println(angle(beg, end));
        if (angle > 337.5 || angle >= 0.0 && angle <= 22.5) {
            //System.out.println("EAST");
            return EAST;
        } else if (angle > 22.5 && angle <= 67.5) {
            //System.out.println("NORTH_EAST");
            return NORTH_EAST;
        } else if (angle > 67.5 && angle <= 102.5) {
            //System.out.println("NORTH");
            return NORTH;
        } else if (angle > 102.5 && angle <= 157.5) {
            //System.out.println("NORTH_WEST");
            return NORTH_WEST;
        } else if (angle > 157.5 && angle <= 202.5) {
            //System.out.println("WEST");
            return WEST;
        } else if (angle > 202.5 && angle <= 247.5) {
            //System.out.println("SOUTH_WEST");
            return SOUTH_WEST;
        } else if (angle > 247.5 && angle <= 292.5) {
            //System.out.println("SOUTH");
            return SOUTH;
        } else {
            //System.out.println("SOUTH_EAST");
            return SOUTH_EAST;
        }
        /*int diffX = end.x - beg.x;
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
        }*/
    }

    private static double angle(Point a, Point b) {
        // TANK YOU prometheuzz @ forums.sun.com
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
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
}
