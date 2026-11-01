package common;

import common.items.*;
import common.messages.*;
import java.io.*;

/** Stack conservation, failure atomicity and protocol round trips without client assets. */
public final class InventoryCheck {
    private static final class Coins extends Etc {
        Coins(long id, int count) { super(id, "Gold", Items.GOLD, count); }
        public Message getDropMessage() { return new AppearEtcItem(getID(), getX(), getY(), getCount(), getType()); }
    }
    private static void require(boolean value, String name) {
        if (!value) throw new AssertionError(name);
    }
    private static void rejected(Runnable action) {
        try { action.run(); throw new AssertionError("Expected rejection"); }
        catch (IllegalArgumentException expected) { }
    }
    private static Message roundtrip(Message message) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) { out.writeObject(message); }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (Message) in.readObject();
        }
    }
    public static void main(String[] args) throws Exception {
        Inventory inv = new Inventory();
        require(inv.getEtc(Items.GOLD).isEmpty() && inv.getEtc(404L) == null, "empty inventory");
        rejected(() -> new Coins(1, 0)); rejected(() -> new Coins(1, -1));
        inv.addEtc(new Coins(10, 7));
        require(inv.addEtc(new Coins(20, 5)).getID() == 10, "merge keeps canonical ID");
        require(inv.getEtc(20L) == null && inv.getEtc(10L).getCount() == 12, "merged quantity");
        inv.removeEtc(10, 3);
        require(inv.getEtc(10L).getCount() == 9, "partial removal");
        rejected(() -> inv.removeEtc(20, 1)); rejected(() -> inv.removeEtc(10, 10));
        rejected(() -> inv.removeEtc(10, 0)); rejected(() -> inv.removeEtc(10, -1));
        rejected(() -> inv.addEtc(new Coins(30, Integer.MAX_VALUE)));
        require(inv.getEtc(10L).getCount() == 9 && inv.getEtc(Items.GOLD).size() == 1, "rejection never mutates");
        inv.removeEtc(10, 9);
        require(inv.getEtc(Items.GOLD).isEmpty(), "full removal");
        rejected(() -> inv.removeById(10));
        inv.addEtc(new Coins(40, Integer.MAX_VALUE));
        rejected(() -> inv.addEtc(new Coins(50, 1)));
        require(inv.getEtc(40L).getCount() == Integer.MAX_VALUE, "overflow boundary");
        inv.removeById(40);
        require(inv.getEtc(Items.GOLD).isEmpty(), "remove by ID");
        AppearEtcItem appear = (AppearEtcItem) roundtrip(new AppearEtcItem(90, 12, 13, 14, Items.GOLD));
        require(appear.getType() == MessageType.APPEARETCITEM && appear.getId() == 90 && appear.getX() == 12
                && appear.getY() == 13 && appear.getCount() == 14 && appear.getItemType() == Items.GOLD, "appear serialization");
        DisappearEtcItem disappear = (DisappearEtcItem) roundtrip(new DisappearEtcItem(90));
        require(disappear.getType() == MessageType.DISAPPEARETCITEM && disappear.getId() == 90, "disappear serialization");
        InventoryAddEtcItem add = (InventoryAddEtcItem) roundtrip(new InventoryAddEtcItem(10, 5, Items.GOLD));
        require(add.getId() == 10 && add.getCount() == 5 && add.getItemType() == Items.GOLD, "add serialization");
        InventoryRemoveEtcItem remove = (InventoryRemoveEtcItem) roundtrip(new InventoryRemoveEtcItem(10, 3, Items.GOLD));
        require(remove.getId() == 10 && remove.getCount() == 3 && remove.getItemType() == Items.GOLD, "remove serialization");
        PickupEtcItem pickup = (PickupEtcItem) roundtrip(new PickupEtcItem(1, 90));
        require(pickup.getPickerId() == 1 && pickup.getItemId() == 90, "pickup serialization");
        System.out.println("PASS: inventory conservation, canonical ID, removals, atomic rejection/overflow, message serialization");
    }
}
