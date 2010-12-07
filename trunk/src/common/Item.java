package common;

import java.awt.Point;

/**
 * Abstract item. Like sword, laser canon, money.
 */
public abstract class Item {

    /**
     * Global identifer of this itme. Everything in Wandering world have one.
     */
    protected final long id;

    /**
     * Name of this item. Like Gold, Shell, Long sword, Laser rifle.
     */
    protected final String name;

    /**
     * Flag what indicats when item lay on the ground or not.
     */
    protected boolean onGround;

    /**
     * X-coord in world of this item. Used for items what lay to the ground.
     */
    protected int x;

    /**
     * Y-coord in world of this item. Used for items what lay to the ground.
     */
    protected int y;

    protected Item(long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Return id of this item.
     */
    public long getID() {
        return id;
    }

    // <editor-fold defaultstate="collapsed" desc="Name">
    /**
     * Return name of this item.
     */
    public String getName() {
        return name;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layed on the ground">
    /**
     * Return item status: layed on ground or not.
     * @return item status: layed on ground or not.
     */
    public boolean isOnGround() {
        return onGround;
    }

    /**
     * Set item status: layed on ground or not.
     */
    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="X-coord">
    /**
     * Return X-coord in world of this item.
     * Used for items what lay to the ground.
     * @return X-coord in world of this item.
     */
    public int getX() {
        return x;
    }

    /**
     * Set item X-coord in world to x.
     * @param x items new X-coord in world.
     */
    public void setX(int x) {
        this.x = x;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Y-coord">
    /**
     * Return Y-coord in world of this item.
     * Used for items what lay to the ground.
     * @return Y-coord in world of this item.
     */
    public int getY() {
        return y;
    }


    /**
     * Set item Y-coord in world to y.
     * @param y items new Y-coord in world.
     */
    public void setY(int y) {
        this.y = y;
    }
    // </editor-fold>

    /**
     * Return position (in world coords) of item what layed on the ground.
     * If item not layed on the ground return null.
     * @return position (in world coords) of layed on the ground item or null.
     */
    public Point getCurPos() {
        return new Point(x, y);
    }

    public abstract Message getMessage();

    public abstract Message getPickupMessage();
}
