package client;

/**
 * Items what has sprite and can be drawn.
 * @author sorc
 */
public interface WItem extends WDrawable {

    public abstract String getName();

    public abstract int getCount();
}
