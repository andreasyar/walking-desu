package client;

import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Interface for drawable things in Wand world.
 * @author CatsPaw
 */
public interface WDrawable {

    //public abstract Sprite getSprite(Graphics g, int x, int y);

    public abstract void draw(Graphics g, int x, int y, Dimension d);

}
