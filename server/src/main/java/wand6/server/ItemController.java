package wand6.server;

import java.util.*;
import wand6.common.*;
import wand6.server.messages.*;

/** One owner of ground items; all transfers and visibility use the world monitor. */
final class ItemController {
    static final double PICKUP_RANGE = 50;
    private final PlayerManager world;
    private final MessageManager messages;
    private final Map<Long, GroundItem> ground = new LinkedHashMap<>();
    private final Map<Long, Set<Long>> visible = new HashMap<>();
    private final Random random = new Random();
    ItemController(PlayerManager world, MessageManager messages) { this.world = world; this.messages = messages; }
    List<GroundItem> snapshot() { synchronized (world) { return List.copyOf(ground.values()); } }
    void add(GroundItem item) {
        synchronized (world) {
            if (ground.putIfAbsent(item.id(), item) != null) throw new IllegalArgumentException("Duplicate ground ID");
            update();
        }
    }
    void update() {
        synchronized (world) {
            long now = ServerTime.getInstance().getTimeSinceStart();
            for (Player observer : world.snapshot()) {
                var seen = visible.computeIfAbsent(observer.getId(), id -> new HashSet<>());
                var pos = observer.getMovement().position(now);
                for (GroundItem item : ground.values()) {
                    boolean near = Math.hypot((double) pos.x() - item.x(), (double) pos.y() - item.y()) <= VisibleManager.RANGE;
                    if (near && seen.add(item.id())) messages.send(observer.getId(), new AppearItemMessage(item));
                    else if (!near && seen.remove(item.id())) messages.send(observer.getId(), new DisappearItemMessage(item.id()));
                }
            }
        }
    }
    boolean pickup(long playerId, long itemId) {
        synchronized (world) {
            Player player = world.find(playerId); GroundItem item = ground.get(itemId);
            if (player == null || player.getHp() <= 0 || item == null) return false;
            var pos = player.getMovement().position(ServerTime.getInstance().getTimeSinceStart());
            if (Math.hypot((double) pos.x() - item.x(), (double) pos.y() - item.y()) > PICKUP_RANGE) return false;
            var inventory = player.inventory();
            synchronized (inventory) {
                try { inventory.validateAdd(item.stack()); }
                catch (IllegalArgumentException | ArithmeticException ex) { return false; }
                var canonical = inventory.add(item.stack());
                ground.remove(itemId);
                visible.forEach((observer, seen) -> { if (seen.remove(itemId)) messages.send(observer, new DisappearItemMessage(itemId)); });
                messages.send(playerId, new InventoryAddMessage(new ItemStack(canonical.id(), canonical.type(), item.stack().count())));
                messages.send(playerId, new PickedUpMessage(itemId));
            }
            return true;
        }
    }
    void died(Unit unit) {
        synchronized (world) {
            if (unit instanceof Player player) drop(player, true);
            else if (unit.getKind() == Unit.Kind.MONSTER) {
                var pos = unit.getMovement().position(ServerTime.getInstance().getTimeSinceStart());
                int x = Math.clamp(pos.x() + (random.nextBoolean() ? 10 : -10), 0, MapFragment.maxX);
                int y = Math.clamp(pos.y() + (random.nextBoolean() ? 10 : -10), 0, MapFragment.maxY);
                add(new GroundItem(new ItemStack(IdManager.nextId(), ItemStack.Type.GOLD, 1 + random.nextInt(99)), x, y));
            }
        }
    }
    private void drop(Player player, boolean notify) {
        var inventory = player.inventory();
        synchronized (inventory) {
            var pos = player.getMovement().position(ServerTime.getInstance().getTimeSinceStart());
            for (ItemStack stack : inventory.snapshot()) {
                inventory.remove(stack.id(), stack.count());
                if (notify) messages.send(player.getId(), new InventoryRemoveMessage(stack.id(), stack.count()));
                add(new GroundItem(stack, pos.x(), pos.y()));
            }
        }
    }
    void disconnected(Player player) {
        synchronized (world) { visible.remove(player.getId()); drop(player, false); visible.remove(player.getId()); }
    }
    void clear() { synchronized (world) { ground.clear(); visible.clear(); } }
}
