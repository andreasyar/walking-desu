package client;

import client.items.ClientItem;
import common.items.GoldCoin;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;

/**
 * Gold coin(s) for client.
 */
public class WGoldCoinItem extends GoldCoin implements ClientItem, Drawable {

    private LayGroundAnimation animation;

    public WGoldCoinItem(long id, int count) {
        super(id, count);
        animation = new LayGroundAnimation("coin_stacks_gold");
    }

    public WGoldCoinItem(long id) {
        super(id);
    }

    @Override
    public void draw(Graphics g, int x, int y, Dimension d) {
        Sprite s = animation.getSprite(this.x, this.y);

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

    @Override
    public Polygon getDimensionOnWorld() {
        Sprite s = animation.getSprite(x, y);
        Polygon p;

        if (s != null) {
            p = new Polygon(new int[] {s.x - s.image.getWidth() / 2,
                                       s.x - s.image.getWidth() / 2,
                                       s.x + s.image.getWidth() / 2,
                                       s.x + s.image.getWidth() / 2,},
                            new int[] {s.y + s.image.getHeight() / 2,
                                       s.y - s.image.getHeight() / 2,
                                       s.y - s.image.getHeight() / 2,
                                       s.y + s.image.getHeight() / 2,},
                            4);
        } else {
            p = new Polygon(new int[] {x - 10,
                                       x - 10,
                                       x + 10,
                                       x + 10,},
                            new int[] {y + 10,
                                       y - 10,
                                       y - 10,
                                       y + 10,},
                            4);
        }

        return p;
    }
}
