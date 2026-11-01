package server.javatestserver;

import common.Movement;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** One registry owns every pending hit, including tower projectiles. */
final class CombatController {
    private final WorldState world;
    private final ScheduledThreadPoolExecutor executor;
    private final MessageService messages;
    final DeathController deaths;
    private final Set<Projectile> tasks = new HashSet<>();
    CombatController(WorldState world, ScheduledThreadPoolExecutor executor, MessageService messages, VisibleManager visibility) {
        this.world = world; this.executor = executor; this.messages = messages;
        this.deaths = new DeathController(world, visibility, messages, this::cancelFor);
        executor.setRemoveOnCancelPolicy(true);
    }
    void attack(JTSUnit attacker, long targetId, boolean animated) {
        synchronized (world) {
            JTSUnit target = world.unit(targetId);
            if (world.unit(attacker.getID()) != attacker || attacker.dead() || target == null || target.dead()) return;
            long now = System.currentTimeMillis() - JavaTestServer.serverStartTime;
            long delay = animated ? attacker.getNukeAnimationDelay() : 0;
            Projectile action = new Projectile(attacker, target, now + delay);
            tasks.add(action);
            try {
                action.setScheduledFuture(executor.scheduleAtFixedRate(action, delay, 10, TimeUnit.MILLISECONDS));
            } catch (RuntimeException failure) { action.cancel(); throw failure; }
            messages.attack(attacker, targetId, now, animated);
        }
    }
    void cancelFor(long id) {
        synchronized (world) {
            for (Projectile task : Set.copyOf(tasks)) if (task.attackerId == id || task.targetId == id) task.cancel();
        }
    }
    void cancelAll() {
        synchronized (world) { for (Projectile task : Set.copyOf(tasks)) task.cancel(); }
    }
    int pending() { synchronized (world) { return tasks.size(); } }
    final class Projectile extends WorldTask {
        final long attackerId, targetId;
        private final Movement flight;
        Projectile(JTSUnit attacker, JTSUnit target, long start) {
            super(CombatController.this.world);
            attackerId = attacker.getID(); targetId = target.getID();
            Point origin = attacker.getCurPos(), destination = target.getCurPos();
            flight = new Movement(origin.x, origin.y, 1.0);
            flight.move(origin.x, origin.y, destination.x, destination.y, start);
        }
        @Override void step() {
            JTSUnit attacker = world.unit(attackerId), target = world.unit(targetId);
            if (attacker == null || target == null || attacker.dead() || target.dead()) { cancel(); return; }
            if (flight.isMove()) return;
            // Mark terminal BEFORE damage/death; re-entry and late ticks cannot reapply.
            cancel();
            target.doHit(attacker.getDamage());
            messages.hit(attacker, target);
            if (target.dead()) deaths.die(target);
        }
        @Override void cancel() { synchronized (world) { super.cancel(); tasks.remove(this); } }
        @Override void failed(RuntimeException failure) { cancel(); super.failed(failure); }
    }
}
