package client;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * Z-Buffer for drawing sprites on panel in proper order.
 * @author sorc
 */
public class ZBuffer {

    /**
     * Z-buffer.
     */
    private final ArrayList<ArrayList<Sprite>> zbuffer = new ArrayList<ArrayList<Sprite>>();
    /**
     * Flag what enable or disable draw rectangle sprite border.
     */
    private boolean drawSpriteBounds;

    /**
     * Adds sprite into z-buffer to specifed layer.
     * @param s sprite to add.
     * @param layer layer in z-buffer.
     * @throws IllegalArgumentException sprite and sprite image cannot be null
     * also layer must be in z-buffer bounds.
     */
    public void addSprite(Sprite s, int layer) throws IllegalArgumentException {
        if (s == null) {
            throw new IllegalArgumentException("Sprite cannot be null!");
        }
        if (s.image == null) {
            throw new IllegalArgumentException("Sprite image cannot be null!");
        }

        synchronized (zbuffer) {
            if (layer <= 0 || layer >= zbuffer.size()) {
                throw new IllegalArgumentException("Sprite layer must be in z-buffer bounds!");
            }

            zbuffer.get(layer).add(s);
        }
    }

    /**
     * Enable or disable draw rectangle sprite border.
     * @param on TRUE to enable, FLASE to disable.
     */
    public void drawSpriteBounds(boolean on) {
        drawSpriteBounds = on;
    }

    /**
     * Return size of z-buffer. Non thread safe.
     * @return size of z-buffer.
     */
    public int getSize() {
        return zbuffer.size();
    }

    /**
     * Resize z-buffer to <i>h</i>.
     * @param h new size.
     * @throws IllegalArgumentException new z-buffer size must be greater than
     * zero.
     */
    public void resize(int h) throws IllegalArgumentException {
        if (h <= 0) {
            throw new IllegalArgumentException("New z-buffer size must be greater than zero.");
        }

        synchronized (zbuffer) {
            zbuffer.clear();
            for (int i = 0; i < h; i++) {
                zbuffer.add(new ArrayList<Sprite>());
            }
        }
    }

    /**
     * Draw sprites from z-buffer to context <i>g</i> and clean z-buffer layers.
     * @param g context fror drawing.
     * @param x x-axys of left top drawing panel corner in world.
     * @param y y-axys of left top drawing panel corner in world.
     * @param dim dimensions of drawing panel.
     */
    public void draw(Graphics g, int x, int y, Dimension d) {
        synchronized (zbuffer) {
            for (int i = 0; i < zbuffer.size(); i++) {
                for (Sprite s : zbuffer.get(i)) {
                    g.drawImage(s.image,
                                s.x - x,
                                s.y - y,
                                null);
                    if (drawSpriteBounds) {
                        g.drawRect(s.x - x - 1,
                                   s.y - y - 1,
                                   s.image.getWidth() + 1,
                                   s.image.getHeight() + 1);
                    }
                }
                zbuffer.get(i).clear();
            }
        }
    }
}
