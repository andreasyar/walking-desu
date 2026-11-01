package common;

import common.items.Etc;
import common.items.Items;
import java.util.ArrayList;

/** Inventory owns stack changes. World transfers additionally require the server world lock. */
public class Inventory {
    private final ArrayList<Etc> etcs = new ArrayList<>();

    public synchronized ArrayList<Etc> getEtc(Items type) {
        ArrayList<Etc> result = new ArrayList<>();
        for (Etc item : etcs) if (item.getType() == type) result.add(item);
        return result;
    }

    public synchronized Etc getEtc(long id) {
        for (Etc item : etcs) if (item.getID() == id) return item;
        return null;
    }

    /** Validate before removing a ground item. Caller holds the world lock across transfer. */
    public synchronized void validateAdd(Etc item) {
        if (item == null || item.getType() == null || item.getCount() <= 0)
            throw new InventoryException("Invalid item or quantity");
        Etc byId = getEtc(item.getID());
        if (byId != null && byId.getType() != item.getType())
            throw new InventoryException("Item ID belongs to a different type");
        ArrayList<Etc> stacks = getEtc(item.getType());
        if (!stacks.isEmpty() && (long) stacks.get(0).getCount() + item.getCount() > Integer.MAX_VALUE)
            throw new InventoryException("Stack quantity overflow");
    }

    /** Adds a quantity and returns the canonical stack, retaining its existing ID. */
    public synchronized Etc addEtc(Etc item) {
        validateAdd(item);
        ArrayList<Etc> stacks = getEtc(item.getType());
        if (stacks.isEmpty()) {
            etcs.add(item);
            return item;
        }
        Etc stack = stacks.get(0);
        stack.setCount(stack.getCount() + item.getCount());
        return stack;
    }

    /** Subtract by authoritative ID and quantity, never by object identity or type. */
    public synchronized void removeEtc(long id, int count) {
        Etc stack = getEtc(id);
        if (stack == null) throw new InventoryException("Item not found: " + id);
        if (count <= 0 || count > stack.getCount())
            throw new InventoryException("Invalid removal quantity: " + count);
        if (count == stack.getCount()) etcs.remove(stack);
        else stack.setCount(stack.getCount() - count);
    }

    public synchronized void removeById(long id) {
        Etc stack = getEtc(id);
        if (stack == null) throw new InventoryException("Item not found: " + id);
        removeEtc(id, stack.getCount());
    }
}
