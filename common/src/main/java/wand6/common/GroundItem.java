package wand6.common;

public record GroundItem(ItemStack stack, int x, int y) implements java.io.Serializable {
    private static final long serialVersionUID = 12L;
    public GroundItem {
        if (stack == null || !MapFragment.contains(x, y)) throw new IllegalArgumentException("Invalid ground item");
    }
    public long id() { return stack.id(); }
}
