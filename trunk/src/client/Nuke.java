package client;

import wand6.client.ServerInteraction;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

abstract public class Nuke implements Drawable {

    /**
     * Время между использованиями нюка.
     */
    protected long reuse;
    /**
     * Время последнего использования нюка.
     */
    protected long lastUseTime;

    public boolean isReuse() {
        return Math.abs(System.currentTimeMillis() - ServerInteraction.serverStartTime) - lastUseTime > reuse ? false : true;
    }

    public abstract Sprite getSprite();

    @Override
    public abstract void draw(Graphics g, int x, int y, Dimension d);

    public abstract void use(long begTime);

    public abstract Point getCurPos();

    public abstract boolean isMove();
}
