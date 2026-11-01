package client;

import client.items.ClientEtc;
import common.items.Items;
import java.awt.*;

/** Tests field-queue ordering, which must not resolve disappearance IDs on the socket thread. */
final class ItemClientCheck {
    private static void require(boolean ok, String name) { if (!ok) throw new AssertionError(name); }
    static void run() throws Exception {
        try (GameField field = new GameField()) {
            Player self = new Player(910, "inventory", 100, .07, 0, 0, Direction.SOUTH, "peasant");
            field.addSelfPlayer(self);
            ClientEtc gold = new ClientEtc(911, "Gold", Items.GOLD, 7);
            self.addItemToInventory(new ClientEtc(912, "Gold", Items.GOLD, 7));
            self.removeItemFromInventory(912, 3);
            require(self.getGoldCount() == 4, "partial inventory removal by ID");
            try { self.removeItemFromInventory(999, 1); throw new AssertionError("Missing ID accepted"); }
            catch (common.InventoryException expected) { }
            require(self.getGoldCount() == 4, "invalid removal leaves client quantity intact");
            self.removeItemFromInventory(912, 4);
            require(self.getGoldCount() == 0, "full inventory removal");
            // Enqueue everything before starting the worker: disappear must find the earlier add.
            self.selectItem(gold);
            field.asyncAddEtc(gold);
            field.asyncRemoveEtcById(gold.getID());
            field.asyncRemoveEtcById(gold.getID());
            Drawable marker = new Drawable() {
                public Sprite getSprite() { return null; }
                public void draw(Graphics g, int x, int y, Dimension size) { }
            };
            field.asyncAddDrawable(marker);
            field.startSelfExecution();
            long end = System.nanoTime() + 5_000_000_000L;
            while (!field.getDrawQueue().contains(marker)) {
                if (System.nanoTime() > end) throw new AssertionError("Item queue timed out");
                Thread.sleep(5);
            }
            require(field.getEtc(gold.getID()) == null && !field.getDrawQueue().contains(gold), "ordered repeated disappear removes model and drawable");
            require(self.getSelectedItem() == null, "removed item clears selection");
            require(self.getGoldCount() == 0, "visibility never credits inventory");
            ClientEtc replacement = new ClientEtc(gold.getID(), "Gold", Items.GOLD, 9);
            field.addEtc(gold); field.addEtc(replacement);
            require(field.getEtc(gold.getID()) == replacement && !field.getDrawQueue().contains(gold), "repeated appearance replaces prior drawable");
            field.removeEtcById(gold.getID());
        }
        System.out.println("PASS: client partial/full removal, queued appear/disappear/repeat, selection cleanup, no visibility reward or drawable duplicates");
    }
}
