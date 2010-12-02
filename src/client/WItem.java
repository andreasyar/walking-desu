package client;

/**
 * Items what has sprite and can be drawn.
 * @author sorc
 */
public interface WItem {

    public abstract Sprite getSprite();

    public abstract String getName();

    public abstract int getCount();
}
