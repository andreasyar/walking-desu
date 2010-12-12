package client.items;

import common.Message;
import common.items.Etc;
import common.items.Items;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

/**
 * Etc item. Examples: gold, piece of metal, cap.
 * @author CatsPaw
 */
public class ClientEtc extends Etc implements ClientItem {

    /**
     * Creates new etc item.
     * @param id id of new etc item.
     * @param name name of new etc item.
     * @param type type of new etc item.
     */
    public ClientEtc (long id, String name, Items type) {
        super(id, name, type);
    }

    /**
     * Draw etc item.
     * @param g context for drawing.
     * @param x x-axis of left upper corner of drawing context on world map.
     * @param y y-axis of left upper corner of drawing context on world map.
     * @param d dimensions of drawing context on world map.
     */
    public void draw(Graphics g, int x, int y, Dimension d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns message notify user what etc item added to his inventory.
     * @return message what notify user what etc item added to his inventory.
     */
    @Override
    public Message getAddToInvenrotyMessage() {
        throw new UnsupportedOperationException("Client cannot send to server this message.");
    }

    /**
     * Returns message notify user what etc item removed from his inventory.
     * @return message waht notify user what etc item removed from his inventory.
     */
    @Override
    public Message getRemoveFromInventoryMessage() {
        throw new UnsupportedOperationException("Client cannot send to server this message.");
    }

    /**
     * Returns message notify user what etc item dropped to the ground.
     * @return message what notify user what etc item dropped to the ground.
     */
    @Override
    public Message getDropMessage() {
        throw new UnsupportedOperationException("Client cannot send to server this message.");
    }

    /**
     * Return current position of etc item on world map.
     * @return current position of etc item on world map.
     */
    public Point getCurPos() {
        return new Point(getX(), getY());
    }

    /**
     * Returns polygon what represents "hit-box" of etc item in world.
     * @return polygon what represents "hit-box" of etc item in world.
     */
    public Polygon getDimensionOnWorld() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
