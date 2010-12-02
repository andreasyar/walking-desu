package common;

/**
 * Abstract item what contains many same items inself.
 * For example 10 arrows. 9000 dollars.
 * 10 arrows is one item -- arrow what repeats 10 times.
 */
public abstract class MultiItem extends Item {

    /**
     * Count of this item. Used for items like
     */
    private int count = 1;

    public MultiItem(long id, String name) {
        super(id, name);
    }

    // <editor-fold defaultstate="collapsed" desc="Count">
    /**
     * Return count of this item.
     * @return count of this item.
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
}
