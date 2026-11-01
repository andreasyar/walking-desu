package common;

import common.items.Etc;
import common.items.Items;
import java.util.ArrayList;

/**
 * Inventory.
 * @author sorc
 */
public class Inventory {

    /**
     * List of etc items.
     */
    protected final ArrayList<Etc> etcs = new ArrayList<Etc>();

    /**
     * Returns etc items of <i>type</i> type.
     * @param type type of etc items.
     */
    public ArrayList<Etc> getEtc(Items type) {
        ArrayList<Etc> tmpEtcs = null;

        synchronized (etcs) {
            for (Etc items : etcs) {
                if (items.getType() == type) {
                    if (tmpEtcs == null) {
                        tmpEtcs = new ArrayList<Etc>();
                    }
                    tmpEtcs.add(items);
                }
            }
        }

        return tmpEtcs;
    }

    /**
     * Adds etc item to inventory.
     * @param etc item item to add.
     * @return return TRUE if new item added to items list, FALSE otherwise.
     */
    public boolean addEtc(Etc item) {
        synchronized (etcs) {
            switch (item.getType()) {
                case GOLD:
                    ArrayList<Etc> tmpEtcs = getEtc(Items.GOLD);
                    if (tmpEtcs == null) {
                        return etcs.add(item);
                    } else {
                        tmpEtcs.get(0).setCount(tmpEtcs.get(0).getCount() + item.getCount());
                        return false;
                    }
                default:
                    return false;
            }
        }
    }

    /**
     * Removes etc item from inventory.
     * @param item etc item to remove.
     */
    public void removeEtc(Etc item) {
        synchronized (etcs) {
            switch (item.getType()) {
                case GOLD:
                    etcs.remove(item);
                    break;
            }
        }
    }

    /**
     * Removes item by id.
     * @param id item id.
     */
    public void removeById(long id) {
        for (Etc etc : etcs) {
            if (etc.getID() == id) {
                etc.removeFromInventory(this);
                return;
            }
        }
    }
}
