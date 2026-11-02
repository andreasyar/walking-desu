package wand6.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import wand6.common.*;

/** EDT-owned ground model and inventory. Pickup intent is cancelled on removal/death/exit. */
final class ItemManager {
    private static final ItemManager INSTANCE = new ItemManager();
    static ItemManager getInstance() { return INSTANCE; }
    private final Map<Long, GroundItem> ground = new LinkedHashMap<>();
    private final Inventory inventory = new Inventory();
    private BufferedImage gold;
    private long pending;
    private boolean awaitingMove;
    private BufferedImage image() { if (gold == null) gold = ClientAssets.image("img/coin_stacks_gold.png"); return gold; }
    java.util.List<GroundItem> snapshot() { return java.util.List.copyOf(ground.values()); }
    Inventory inventory() { return inventory; }
    void appear(GroundItem item) { ground.put(item.id(), item); }
    void disappear(long id) { ground.remove(id); if (pending == id) pending = 0; }
    void add(ItemStack delta) {
        var existing = inventory.snapshot().stream().filter(s -> s.type() == delta.type()).findFirst().orElse(null);
        if (existing != null && existing.id() != delta.id()) throw new IllegalStateException("Inventory ID mismatch");
        inventory.add(delta);
    }
    void remove(long id, int count) { inventory.remove(id, count); }
    void cancel() { pending = 0; awaitingMove = false; }
    void moved() { awaitingMove = false; }
    void clear() { ground.clear(); inventory.clear(); cancel(); }
    boolean click(int x, int y) throws wand6.common.exceptions.MessageManagerException {
        var image = image();
        var items = new ArrayList<>(ground.values()); Collections.reverse(items);
        for (GroundItem item : items) {
            if (x >= item.x() - image.getWidth()/2 && x < item.x() - image.getWidth()/2 + image.getWidth()
                    && y >= item.y() - image.getHeight()/2 && y < item.y() - image.getHeight()/2 + image.getHeight()) {
                pending = item.id();
                var self = PlayerManager.getInstance().selfPlayer();
                var p = self.getMovement().position(ServerTime.getInstance().getTimeSinceStart());
                if (Math.hypot((double)p.x()-item.x(), (double)p.y()-item.y()) > 50) {
                    awaitingMove = true; MessageManager.sendMoveRequest(item.x(), item.y());
                }
                tick(); return true;
            }
        }
        return false;
    }
    void tick() {
        var item = ground.get(pending); var self = PlayerManager.getInstance().selfPlayer();
        if (item == null || self == null) { cancel(); return; }
        var p = self.getMovement().position(ServerTime.getInstance().getTimeSinceStart());
        if (Math.hypot((double)p.x()-item.x(), (double)p.y()-item.y()) <= 50) {
            long id = pending; cancel();
            try { MessageManager.sendPickup(id); }
            catch (wand6.common.exceptions.MessageManagerException ex) { throw new IllegalStateException(ex); }
        } else if (!awaitingMove && !self.getMovement().isMoving()) {
            cancel(); // Server clipped the route before pickup range.
        }
    }
    void draw(Graphics g, Camera camera) {
        if (ground.isEmpty()) return;
        var image = image();
        for (var item : ground.values()) {
            int x = item.x()-camera.x()-image.getWidth()/2, y = item.y()-camera.y()-image.getHeight()/2;
            g.drawImage(image,x,y,null); g.setColor(Color.YELLOW);
            g.drawString("Gold ("+item.stack().count()+")", x, y+image.getHeight()+14);
            if (pending == item.id()) g.drawRect(x-1,y-1,image.getWidth()+2,image.getHeight()+2);
        }
    }
    void drawInventory(Graphics g, int width) {
        String text = "Золото: " + inventory.count(ItemStack.Type.GOLD);
        int x = Math.max(12,width-g.getFontMetrics().stringWidth(text)-14);
        g.setColor(new Color(0,0,0,180)); g.fillRect(x-5,32,g.getFontMetrics().stringWidth(text)+10,22);
        g.setColor(Color.YELLOW); g.drawString(text,x,48);
    }
}
