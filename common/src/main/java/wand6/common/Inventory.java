package wand6.common;

import java.util.*;

/** All server transfers additionally hold the world monitor, before this monitor. */
public final class Inventory {
    private final Map<ItemStack.Type, ItemStack> stacks = new EnumMap<>(ItemStack.Type.class);
    public synchronized List<ItemStack> snapshot() { return List.copyOf(stacks.values()); }
    public synchronized ItemStack find(long id) {
        return stacks.values().stream().filter(s -> s.id() == id).findFirst().orElse(null);
    }
    public synchronized int count(ItemStack.Type type) { var stack = stacks.get(type); return stack == null ? 0 : stack.count(); }
    public synchronized void validateAdd(ItemStack item) {
        if (item == null) throw new IllegalArgumentException("Missing stack");
        var byId = find(item.id());
        if (byId != null && byId.type() != item.type()) throw new IllegalArgumentException("Conflicting ID");
        Math.addExact(count(item.type()), item.count());
    }
    public synchronized ItemStack add(ItemStack item) {
        validateAdd(item);
        var old = stacks.get(item.type());
        var result = old == null ? item : new ItemStack(old.id(), old.type(), Math.addExact(old.count(), item.count()));
        stacks.put(item.type(), result); return result;
    }
    public synchronized void remove(long id, int count) {
        var old = find(id);
        if (old == null || count <= 0 || count > old.count()) throw new IllegalArgumentException("Invalid removal");
        if (old.count() == count) stacks.remove(old.type());
        else stacks.put(old.type(), new ItemStack(id, old.type(), old.count() - count));
    }
    public synchronized void clear() { stacks.clear(); }
}
