package server.javatestserver;

import common.MapFragment;
import common.items.Item;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/** One world per server. All reads/mutations and visibility use this monitor, then Inventory.
 * Queues keep iteration safe for diagnostics; they do not replace the world transaction lock.
 */
final class WorldState {
    private final AtomicLong nextId = new AtomicLong(1);
    final LinkedBlockingQueue<JTSUnit> units = new LinkedBlockingQueue<>();
    final LinkedBlockingQueue<Player> players = new LinkedBlockingQueue<>();
    final LinkedBlockingQueue<Monster> monsters = new LinkedBlockingQueue<>();
    final LinkedBlockingQueue<Tower> towers = new LinkedBlockingQueue<>();
    final LinkedBlockingQueue<Item> items = new LinkedBlockingQueue<>();
    final LinkedBlockingQueue<Monster> waveMonsters = new LinkedBlockingQueue<>();
    final ArrayList<MapFragment> mapfragments = new ArrayList<>();
    final int tdMonsterLossLimit = 100;
    int tdMonsterLoss;

    long nextId() { return nextId.getAndIncrement(); }
    JTSUnit unit(long id) {
        for (JTSUnit unit : units) if (unit.getID() == id) return unit;
        return null;
    }
}
