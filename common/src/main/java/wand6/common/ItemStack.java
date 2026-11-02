package wand6.common;

/** Immutable stack value: callers cannot mutate inventory through a snapshot. */
public record ItemStack(long id, Type type, int count) implements java.io.Serializable {
    private static final long serialVersionUID = 12L;
    public enum Type { GOLD }
    public ItemStack {
        if (id <= 0 || type == null || count <= 0) throw new IllegalArgumentException("Invalid stack");
    }
}
