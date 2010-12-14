package common.items;

import common.Inventory;
import common.Message;

/**
 * Abstract item. Like sword, laser canon, money.
 */
public abstract class Item {

    // <editor-fold defaultstate="collapsed" desc="Fields">
    /**
     * Global identifer of this item. Everything in Wand world have one.
     */
    private final long id;
    /**
     * Name of this item. Like Gold, Shell, Long sword, Laser rifle.
     */
    private final String name;
    /**
     * X-coord in world of this item. Used for items what lay to the ground.
     */
    private int x;
    /**
     * Y-coord in world of this item. Used for items what lay to the ground.
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
     * Count of this item.
     */
    private int count;
    // </editor-fold>

    /**
     * Creates new item.
     * @param id id of new item.
     * @param name name of new item.
     */
    protected Item(long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Return id of this item.
     * @return id of this item.
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
     * @param x x-axys of dot.
     * @param y y-axys of dot.
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

    // <editor-fold defaultstate="collapsed" desc="Inventory">
    /**
     * Adds item to inventory <i>inv</i>.
     * @param inv inventory.
     */
    public abstract void addToInventory(Inventory inv);

    /**
     * Removes item from inventory <i>inv</i>.
     * @param inv inventory.
     */
    public abstract void removeFromInventory(Inventory inv);
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
    public abstract Message getDropMessage();
    // </editor-fold>

    @Override
    public String toString() {
        return "Item{" + "id=" + id + "name=" + name + "x=" + x + "y=" + y + "w=" + w + "h=" + h + "count=" + count + '}';
    }
}
