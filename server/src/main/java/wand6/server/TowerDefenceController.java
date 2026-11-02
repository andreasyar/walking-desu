package wand6.server;

import java.util.*;
import java.util.function.LongSupplier;
import wand6.common.*;
import wand6.server.messages.*;
import static wand6.common.TowerDefenceRules.*;

/** One world monitor and clock for all waves, towers and combat. No per-NPC tasks. */
final class TowerDefenceController {
    private final PlayerManager world;
    private final VisibleManager visibility;
    private final MessageManager messages;
    private final CombatController combat;
    private final LongSupplier clock;
    private final Random random;
    private final Map<Long, Long> buildTimes = new HashMap<>();
    private final Map<Long, Long> targets = new HashMap<>();
    private long nextWave, wave, cycle;
    private int previousLosses, currentLosses, strength, count;

    TowerDefenceController(PlayerManager world, VisibleManager visibility, MessageManager messages, CombatController combat) {
        this(world, visibility, messages, combat, () -> ServerTime.getInstance().getTimeSinceStart(), new Random());
    }
    TowerDefenceController(PlayerManager world, VisibleManager visibility, MessageManager messages,
                           CombatController combat, LongSupplier clock, Random random) {
        this.world = world; this.visibility = visibility; this.messages = messages; this.combat = combat;
        this.clock = clock; this.random = random; nextWave = clock.getAsLong() + WAVE_DELAY;
    }
    boolean build(long playerId, int x, int y) {
        synchronized (world) {
            Player player = world.find(playerId);
            long now = clock.getAsLong();
            if (player == null || !MapFragment.contains(x, y) || !Terrain.passable(x, y)
                    || now - buildTimes.getOrDefault(playerId, -TOWER_REUSE) < TOWER_REUSE) return false;
            var p = player.getMovement().position(now);
            if (Math.hypot((double) x - p.x(), (double) y - p.y()) > BUILD_RANGE) return false;
            var towers = world.npcs.values().stream().filter(u -> u.getKind() == Unit.Kind.TOWER).toList();
            if (towers.size() >= MAX_TOWERS || towers.stream().anyMatch(u ->
                    Math.hypot((double) x - u.getCurX(), (double) y - u.getCurY()) < BUILD_SPACING)) return false;
            Unit tower = new Unit(IdManager.nextId(), "Tower", "tower", Unit.Kind.TOWER, 1, CurveMovement.SPEED);
            tower.place(x, y); tower.setLastAttack(-TOWER_REUSE);
            world.npcs.put(tower.getId(), tower); buildTimes.put(playerId, now);
            visibility.update();
            return true;
        }
    }
    void tick() {
        synchronized (world) {
            long now = clock.getAsLong();
            // Resolve arrivals before the wave boundary: each living arrival counts exactly once.
            for (Unit unit : List.copyOf(world.npcs.values())) {
                if (unit.getKind() == Unit.Kind.MONSTER && !unit.getMovement().isMovingAt(now)) {
                    currentLosses++; remove(unit.getId(), false); status();
                }
            }
            if (now >= nextWave) {
                nextWave = now + WAVE_DELAY; // No burst of missed waves after a paused world clock.
                previousLosses += currentLosses;
                if (previousLosses >= LOSS_LIMIT) {
                    clearEntities(); cycle++; wave = 0;
                    previousLosses = 0; currentLosses = 0; strength = 0; count = 0;
                    buildTimes.clear();
                }
                if (currentLosses == 0) { strength++; count += 5; }
                currentLosses = 0; wave++;
                for (int i = 0; i < count; i++) {
                    int x = 544 + random.nextInt(287), y = random.nextInt(64);
                    Unit monster = new Unit(IdManager.nextId(), "Monster", "poring", Unit.Kind.MONSTER,
                            20 * strength, random.nextBoolean() ? .08 : .07);
                    monster.getMovement().start(x, y, DEST_X, DEST_Y, now);
                    world.npcs.put(monster.getId(), monster);
                }
                status(); visibility.update();
            }
            for (Unit tower : world.npcs.values().stream().filter(u -> u.getKind() == Unit.Kind.TOWER).toList()) {
                Unit target = world.unit(targets.getOrDefault(tower.getId(), 0L));
                if (!inRange(tower, target, now)) {
                    target = world.npcs.values().stream().filter(u -> inRange(tower, u, now)).findFirst().orElse(null);
                }
                if (target == null) targets.remove(tower.getId());
                else { targets.put(tower.getId(), target.getId()); combat.attack(tower.getId(), target.getId()); }
            }
        }
    }
    private boolean inRange(Unit tower, Unit target, long time) {
        if (target == null || target.getKind() != Unit.Kind.MONSTER || target.getHp() <= 0) return false;
        var a = tower.getMovement().position(time); var b = target.getMovement().position(time);
        return Math.hypot((double) a.x() - b.x(), (double) a.y() - b.y()) <= TOWER_RANGE;
    }
    void remove(long id, boolean killed) {
        synchronized (world) {
            if (world.npcs.remove(id) == null) return;
            combat.cancelFor(id); targets.remove(id); targets.values().removeIf(target -> target == id);
            visibility.removeEntity(id, killed);
        }
    }
    TowerDefenceStatusMessage snapshot() {
        synchronized (world) { return new TowerDefenceStatusMessage(cycle, wave, previousLosses + currentLosses, LOSS_LIMIT, strength); }
    }
    void sendStatus(long observer) { messages.send(observer, snapshot()); }
    private void status() { var state = snapshot(); for (Player p : world.snapshot()) messages.send(p.getId(), state); }
    private void clearEntities() { for (long id : List.copyOf(world.npcs.keySet())) remove(id, false); targets.clear(); }
    void clear() { synchronized (world) { clearEntities(); buildTimes.clear(); } }
    void disconnected(long id) { synchronized (world) { buildTimes.remove(id); } }
}
