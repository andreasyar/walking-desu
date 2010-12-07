package common;

/**
 * Gold coin(s).
 */
public class GoldCoinItem extends MultiItem {

    public GoldCoinItem(long id) {
        super(id, "Gold");
    }

    public GoldCoinItem(long id, int count) {
        super(id, "Gold");
        setCount(count);
    }

    @Override
    public Message getMessage() {
        return new GoldCoinMessage(getID(), getX(), getY(), getCount());
    }

    @Override
    public Message getPickupMessage() {
        return new PickupGoldCoinItem(getID(), getCount());
    }
}
