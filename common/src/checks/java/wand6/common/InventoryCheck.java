package wand6.common;

import static wand6.common.ItemStack.Type.GOLD;

public final class InventoryCheck {
    public static void main(String[] args) {
        var inventory = new Inventory();
        require(inventory.snapshot().isEmpty(), "empty snapshot");
        inventory.add(new ItemStack(10,GOLD,7));
        require(inventory.add(new ItemStack(11,GOLD,5)).equals(new ItemStack(10,GOLD,12)), "merge keeps ID");
        inventory.remove(10,2); require(inventory.find(10).count() == 10, "partial removal");
        reject(() -> inventory.remove(11,1)); reject(() -> inventory.remove(10,11));
        reject(() -> inventory.remove(10,0)); reject(() -> inventory.remove(10,-1));
        reject(() -> new ItemStack(12,GOLD,0)); reject(() -> new ItemStack(12,null,1));
        reject(() -> inventory.add(new ItemStack(12,GOLD,Integer.MAX_VALUE)));
        require(inventory.find(10).count() == 10, "failed changes atomic");
        inventory.remove(10,10); require(inventory.snapshot().isEmpty(), "full removal");
        inventory.add(new ItemStack(12,GOLD,Integer.MAX_VALUE));
        reject(() -> inventory.add(new ItemStack(13,GOLD,1)));
        require(inventory.count(GOLD)==Integer.MAX_VALUE, "overflow preserves quantity");
        System.out.println("PASS: inventory immutable snapshots, merge ID, partial/full removal, invalid input and overflow without mutation");
    }
    private static void reject(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException | ArithmeticException expected) { return; }
        throw new AssertionError("Invalid operation accepted");
    }
    private static void require(boolean condition, String label) { if (!condition) throw new AssertionError(label); }
}
