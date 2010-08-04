package client;

import java.awt.Point;

abstract public class Nuke {

    protected long reuse;
    protected long lastUseTime;

    public boolean reuse() {
        return Math.abs(System.currentTimeMillis() - ServerInteraction.serverStartTime) - lastUseTime > reuse ? false : true;
    }

    public abstract Sprite getSprite();

    public abstract void use(long begTime);

    public abstract Point getCurPos();

    public abstract boolean isMove();
}
