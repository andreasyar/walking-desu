package client;

import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Interface for drawable units in Wand world.
 * @author CatsPaw
 */
public interface Drawable {

    /**
     * Draw drawable unit directly in context.
     * @param g context for drawing.
     * @param x x-axis of left upper corner of drawing context on world map.
     * @param y y-axis of left upper corner of drawing context on world map.
     * @param d dimensions of drawing context on world map.
     */
    public abstract void draw(Graphics g, int x, int y, Dimension d);

    /**
     * Returns sprite for delayed drawing in z-buffer or anoter purpose.
     * @return sprite.
     */
    public abstract Sprite getSprite();
}
