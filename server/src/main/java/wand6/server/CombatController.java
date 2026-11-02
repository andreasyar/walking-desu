package wand6.server;

import java.util.*;
import wand6.common.*;
import wand6.server.messages.*;

/** Adapted from 3.2 CombatController/DeathController. All actions use the world monitor.
 * One clock owns flights; removing a shot BEFORE damage makes a hit terminal.
 */
final class CombatController {
    private final PlayerManager world;
    private final VisibleManager visibility;
    private final MessageManager messages;
    private final Map<Long, Shot> shots = new LinkedHashMap<>();
    private final Map<Long, AttackMessage> casts = new HashMap<>();
    private record Shot(AttackMessage event, long impact, Set<Long> recipients) { }
    CombatController(PlayerManager world, VisibleManager visibility, MessageManager messages) {
        this.world = world; this.visibility = visibility; this.messages = messages;
    }
    private long now() { return ServerTime.getInstance().getTimeSinceStart(); }
    void attack(long attackerId, long targetId) {
        synchronized (world) {
            Player a = world.find(attackerId), b = world.find(targetId);
            if (a == null || b == null || a == b || a.getHp() <= 0 || b.getHp() <= 0) return;
            long time = now();
            var origin = a.getMovement().position(time);
            var destination = b.getMovement().position(time);
            double distance = Math.hypot((double) origin.x() - destination.x(), (double) origin.y() - destination.y());
            if (distance > CombatRules.RANGE || time - a.getLastAttack() < CombatRules.REUSE) return;
            a.setLastAttack(time);
            visibility.update();
            var event = new AttackMessage(IdManager.nextId(), attackerId, targetId, time,
                    origin.x(), origin.y(), destination.x(), destination.y());
            casts.put(attackerId, event);
            Set<Long> recipients = visibility.recipients(attackerId, targetId);
            shots.put(event.shotId(), new Shot(event, time + CombatRules.WINDUP
                    + (long) Math.ceil(distance / CombatRules.PROJECTILE_SPEED), recipients));
            recipients.forEach(id -> messages.send(id, event));
        }
    }
    void appeared(long observer, long player) {
        for (Shot shot : shots.values()) if (shot.event.attackerId() == player || shot.event.targetId() == player) {
            // Returning observers may have discarded the effect at DISAPPEAR; shot ID deduplicates it.
            shot.recipients.add(observer);
            messages.send(observer, shot.event);
        }
    }
    AttackMessage currentAttack(long id) {
        AttackMessage event = casts.get(id);
        return event != null && now() < event.time() + CombatRules.ATTACK_ANIMATION ? event : null;
    }
    void tick() {
        synchronized (world) {
            long time = now();
            casts.values().removeIf(event -> time >= event.time() + CombatRules.ATTACK_ANIMATION);
            for (Shot shot : List.copyOf(shots.values())) {
                if (!shots.containsKey(shot.event.shotId()) || time < shot.impact) continue;
                visibility.update();
                Player a = world.find(shot.event.attackerId()), b = world.find(shot.event.targetId());
                finish(shot);
                if (a == null || b == null || a.getHp() <= 0 || b.getHp() <= 0) continue;
                b.setHp(Math.max(0, b.getHp() - CombatRules.DAMAGE));
                visibility.deliver(b.getId(), new HitMessage(shot.event.shotId(), a.getId(), b.getId(),
                        CombatRules.DAMAGE, b.getHp(), b.getLife(), time));
                if (b.getHp() == 0) {
                    var position = b.getMovement().position(time);
                    visibility.deliver(b.getId(), new DeathMessage(b.getId(), b.getLife(), position.x(), position.y(), time));
                    cancelFor(b.getId());
                    b.setLife(b.getLife() + 1);
                    b.setHp(CombatRules.MAX_HP);
                    // 3.2 restores immediately; use wand6's passable login spawn instead of (0,0).
                    b.place(512, 512);
                    visibility.deliver(b.getId(), new RespawnMessage(b.getId(), b.getLife(), 512, 512, b.getHp(), time));
                    visibility.update();
                }
            }
        }
    }
    private void finish(Shot shot) {
        shots.remove(shot.event.shotId());
        shot.recipients.forEach(id -> messages.send(id, new EndProjectileMessage(shot.event.shotId())));
    }
    void cancelFor(long id) {
        synchronized (world) {
            casts.remove(id);
            for (Shot shot : List.copyOf(shots.values()))
                if (shot.event.attackerId() == id || shot.event.targetId() == id) finish(shot);
        }
    }
    void clear() { synchronized (world) { for (Shot shot : List.copyOf(shots.values())) finish(shot); casts.clear(); } }
    int pending() { synchronized (world) { return shots.size(); } }
}
