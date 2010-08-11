package client;

import java.awt.Point;

abstract public class Nuke {

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

    public abstract void use(long begTime);

    public abstract Point getCurPos();

    public abstract boolean isMove();
}
