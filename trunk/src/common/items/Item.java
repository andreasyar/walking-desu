package common.items;

import common.Message;

/**
 * Abstract item.
 */
public abstract class Item {

    /**
     * Global identifer of item. Everything in Wand world have one.
     */
    private final long id;
    /**
     * Name of item.
     */
    private final String name;
    /**
     * X-axis of item on map.
     */
    private int x;
    /**
     * Y-axis of item on map.
     */
    private int y;
    /**
     * Width of item.
     */
    private int w;
    /**
     * Height of item.
     */
    private int h;
    /**
     * Count of item.
     */
    private int count;

    /**
     * Creates new item.
     * @param id id of new item.
     * @param name name of new item.
     * @param count count of new item.
     */
    protected Item(long id, String name, int count) {
        this.id = id;
        this.name = name;
        this.count = count;
    }

    /**
     * Creates new item.
     * @param id id of new item.
     * @param name name of new item.
     * @param x x-axis of this item on map.
     * @param y y-axis of this item on map.
     * @param w width of item.
     * @param h height of item.
     * @param count count of item.
     */
    protected Item(long id, String name, int x, int y, int w, int h, int count) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.count = count;
    }

    /**
     * Return id of item.
     * @return id of item.
     */
    public long getID() {
        return id;
    }

    // <editor-fold defaultstate="collapsed" desc="Name">
    /**
     * Return name of this item.
     * @return name of item.
     */
    public String getName() {
        return name;
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

    // <editor-fold defaultstate="collapsed" desc="Height">
    /**
     * Returns height of item.
     * @return height of item.
     */
    public int getH() {
        return h;
    }

    /**
     * Set height of item.
     * @param h height of item.
     */
    public void setH(int h) {
        this.h = h;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Width.">
    /**
     * Returns width of item.
     * @return width of item.
     */
    public int getW() {
        return w;
    }

    /**
     * Set width of item.
     * @param w width of item.
     */
    public void setW(int w) {
        this.w = w;
    }
    // </editor-fold>

    /**
     * Return TRUE if dot [<i>x</i>, <i>y</i>] inside item bounds, FALSE otherwise.
     * @param x x-axis of dot.
     * @param y y-axis of dot.
     */
    public boolean onItem(int x, int y) {
        if (x >= this.x && x <= this.x + this.w
                && y >= this.y && y <= this.y + this.h) {

            return true;
        }

        return false;
    }

    // <editor-fold defaultstate="collapsed" desc="Count">
    /**
     * Return count of item.
     * @return count of item.
     */
    public int getCount() {
        return count;
    }

    /**
     * Set item count to count.
     * @param count item new count.
     */
    public void setCount(int count) throws IllegalArgumentException {
        if (count <= 0) {
            throw new IllegalArgumentException("Count of item must be 1 or more.");
        }

        this.count = count;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Server-client interaction messages.">
    /**
     * Returns message notify user what item added to his inventory.
     * @return message what notify user what item added to his inventory.
     */
    public abstract Message getAddToInvenrotyMessage();

    /**
     * Returns message notify user what item removed from his inventory.
     * @return message waht notify user what item removed from his inventory.
     */
    public abstract Message getRemoveFromInventoryMessage();

    /**
     * Returns message notify user what item dropped to the ground.
     * @return message what notify user what item dropped to the ground.
     */
    public abstract Message getAppearMessage();
    // </editor-fold>

    @Override
    public String toString() {
        return "Item{" + "id=" + id + "name=" + name + "x=" + x + "y=" + y + "w=" + w + "h=" + h + "count=" + count + '}';
    }
}
