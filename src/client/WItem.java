package client;

import java.awt.Point;
import java.awt.Polygon;

/**
 * Items what has sprite and can be drawn.
 * @author sorc
 */
public interface WItem extends WDrawable {

    public abstract String getName();

    public abstract int getCount();

    public abstract Point getCurPos();

    public abstract Polygon getDimensionOnWorld();

    public abstract long getID();
}
