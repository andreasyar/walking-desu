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
     * Returns etc item by id.
     * @param id id of etc item.
     * @return etc item or <b>null</b> if etc item not found.
     */
    public Etc getEtc(long id) {
        synchronized (etcs) {
            for (Etc item : etcs) {
                if (item.getID() == id) {
                    return item;
                }
            }
        }

        return null;
    }

    /**
     * Returns list of etc items by type.
     * @param type type of etc items.
     * @return list of etc items. List is empty if there is no items of this
     * type on inventory.
     */
    public ArrayList<Etc> getEtc(Items type) {
        ArrayList<Etc> tmpEtcs = new ArrayList<Etc>();

        synchronized (etcs) {
            for (Etc items : etcs) {
                if (items.getType() == type) {
                    tmpEtcs.add(items);
                }
            }
        }

        return tmpEtcs;
    }

    /**
     * Adds etc item to inventory.
     * @param item etc item to add.
     * @return return <b>true</b> if new item added to inventory, <b>false</b>
     * otherwise.
     */
    public boolean addEtc(Etc item) {
        synchronized (etcs) {
            switch (item.getType()) {
                case GOLD:
                    ArrayList<Etc> tmpEtcs = getEtc(Items.GOLD);
                    if (tmpEtcs.isEmpty()) {
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
    public void removeEtc(Etc item) throws InventoryException {
        synchronized (etcs) {
            switch (item.getType()) {
                case GOLD:
                    ArrayList<Etc> gold = getEtc(Items.GOLD);
                    if (!gold.isEmpty()) {
                        if (gold.get(0).getCount() == item.getCount()) {
                            etcs.remove(gold.get(0));
                        } else if (gold.get(0).getCount() > item.getCount()) {
                            gold.get(0).setCount(gold.get(0).getCount() - item.getCount());
                        } else {
                            throw new InventoryException("Cannot remove more gold whan we have.");
                        }
                    } else {
                        throw new InventoryException("We have no gold but try to remove them.");
                    }
                    break;
            }
        }
    }
}
