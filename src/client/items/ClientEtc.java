package client.items;

import client.Drawable;
import client.LayGroundAnimation;
import client.Sprite;
import common.Message;
import common.items.Etc;
import common.items.Items;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Etc item for client use.
 * @author CatsPaw
 */
public class ClientEtc extends Etc implements Drawable {

    /**
     * Lay on the ground animation.
     */
    private final LayGroundAnimation layAnimation;

    /**
     * Creates new etc item.
     * @param id id of new etc item.
     * @param name name of new etc item.
     * @param count count of new item.
     * @param type type of new etc item.
     */
    public ClientEtc (long id, String name, int count, Items type) {
        super(id, name, count, type);
        switch (type) {
            case GOLD:
                layAnimation = new LayGroundAnimation("coin_stacks_gold");
                break;
            default:
                layAnimation = new LayGroundAnimation(name);
                break;
        }
        setW(layAnimation.getSprite(getX(), getY()).image.getWidth());
        setH(layAnimation.getSprite(getX(), getY()).image.getHeight());
    }

    /**
     * Returns message notify user what etc item added to his inventory.
     * @return message what notify user what etc item added to his inventory.
     */
    public Message getAddToInvenrotyMessage() {
        throw new UnsupportedOperationException("Client cannot send to server this message.");
    }

    /**
     * Returns message notify user what etc item removed from his inventory.
     * @return message waht notify user what etc item removed from his inventory.
     */
    public Message getRemoveFromInventoryMessage() {
        throw new UnsupportedOperationException("Client cannot send to server this message.");
    }

    /**
     * Returns message notify user what etc item dropped to the ground.
     * @return message what notify user what etc item dropped to the ground.
     */
    public Message getAppearMessage() {
        throw new UnsupportedOperationException("Client cannot send to server this message.");
    }

    /**
     * Draw etc item.
     * @param g context for drawing.
     * @param x x-axis of left upper corner of drawing context on world map.
     * @param y y-axis of left upper corner of drawing context on world map.
     * @param d dimensions of drawing context on world map.
     */
    public void draw(Graphics g, int x, int y, Dimension d) {
        Sprite s = layAnimation.getSprite(getX(), getY());

        if (s != null) {
            g.drawImage(s.image,
                        s.x - x - s.image.getWidth() / 2,
                        s.y - y - s.image.getHeight() / 2,
                        null);
            g.drawString(getName() + "(" + getCount() + ")",
                         s.x - x,
                         s.y - y + 2 * s.image.getHeight());

            // Point where item placed in world map and border around sprite
            // image.
            g.drawLine(s.x - x, s.y - y, s.x - x, s.y - y);
            g.drawRect(s.x - x - s.image.getWidth() / 2 - 1,
                       s.y - y - s.image.getHeight() / 2,
                       s.image.getWidth() + 1,
                       s.image.getHeight() + 1);
        }
    }

    /**
     * Returns sprite for delayed drawing in z-buffer or anoter purpose.
     * @return sprite.
     */
    public Sprite getSprite() {
        return layAnimation.getSprite(getX(), getY());
    }
}
