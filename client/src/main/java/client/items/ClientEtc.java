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
     * @param type type of new etc item.
     * @param count initial positive quantity.
     */
    public ClientEtc (long id, String name, Items type, int count) {
        super(id, name, type, count);
        switch (type) {
            case GOLD:
                layAnimation = new LayGroundAnimation("coin_stacks_gold");
                break;
            default:
                layAnimation = new LayGroundAnimation(name);
                break;
        }
        Sprite sprite = layAnimation.getSprite(0, 0);
        setW(sprite.image.getWidth());
        setH(sprite.image.getHeight());
    }

    @Override
    public boolean onItem(int x, int y) {
        return x >= getX() - getW() / 2 && x < getX() - getW() / 2 + getW()
                && y >= getY() - getH() / 2 && y < getY() - getH() / 2 + getH();
    }

    /**
     * Returns message notify user what etc item dropped to the ground.
     * @return message what notify user what etc item dropped to the ground.
     */
    public Message getDropMessage() {
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
        Sprite s = getSprite();

        if (s != null) {
            g.drawImage(s.image,
                        s.x - x,
                        s.y - y,
                        null);
            g.drawString(getName() + "(" + getCount() + ")",
                         s.x - x,
                         s.y - y + 2 * s.image.getHeight());

            // Point where item placed in world map and border around sprite
            // image.
            g.drawLine(s.x - x, s.y - y, s.x - x, s.y - y);
            g.drawRect(s.x - x - 1,
                       s.y - y,
                       s.image.getWidth() + 1,
                       s.image.getHeight() + 1);
        }
    }

    /**
     * Returns sprite for delayed drawing in z-buffer or anoter purpose.
     * @return sprite.
     */
    public Sprite getSprite() {
        Sprite sprite = layAnimation.getSprite(getX(), getY());
        sprite.baseX = getX();
        sprite.baseY = getY();
        sprite.x -= getW() / 2;
        sprite.y -= getH() / 2;
        return sprite;
    }
}
