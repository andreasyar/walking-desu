package common.items;

import common.GoldCoinMessage;
import common.Inventory;
import common.Message;
import common.MultiItem;
import common.PickupGoldCoinItem;
import common.messages.InventoryAddGoldCoin;
import common.messages.AddGoldCoin;
import common.messages.DelGoldCoin;
import common.messages.InventoryDelGoldCoin;

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
    public void addToInventory(Inventory inv) {
        inv.addGoldCoin(getID(), getCount());
    }

    @Override
    public Message getAddToInvenrotyMessage() {
        return new InventoryAddGoldCoin(getID(), getCount());
    }

    @Override
    public Message getDropMessage() {
        return new AddGoldCoin(getID(), getX(), getY(), getCount());
    }

    @Override
    public Message getPickupMessage() {
        return new DelGoldCoin(getID());
    }

    @Override
    public Message getRemoveFromInventoryMessage() {
        return new InventoryDelGoldCoin(getID(), getCount());
    }
}
