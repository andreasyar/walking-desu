package newcommon;

import newcommon.exceptions.InventoryException;
import newcommon.items.Etc;
import newcommon.items.Items;
import java.util.ArrayList;

public class Inventory {

    protected final ArrayList<Etc> etcs = new ArrayList<Etc>();

    public Etc getEtc(long id) {
        synchronized (etcs) {
            for (Etc item : etcs) {
                if (item.getId() == id) {
                    return item;
                }
            }
        }

        return null;
    }

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
