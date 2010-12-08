package common;

import common.messages.AddGoldCoin;

/**
 * Gold coin(s).
 */
public class GoldCoin extends MultiItem {

    public GoldCoin(long id) {
        super(id, "Gold");
    }

    public GoldCoin(long id, int count) {
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

    @Override
    public void addToInventory(Inventory inv) {
        inv.addGoldCoin(getID(), getCount());
    }

    @Override
    public Message getAddToInvenrotyMessage() {
        return new AddGoldCoin(getID(), getCount());
    }
}
