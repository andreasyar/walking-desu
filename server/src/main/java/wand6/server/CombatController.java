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
            Unit a = world.unit(attackerId), b = world.unit(targetId);
            if (a == null || b == null || a == b || a.getHp() <= 0 || b.getHp() <= 0) return;
            if (a.getKind() == Unit.Kind.MONSTER
                    || (a.getKind() == Unit.Kind.TOWER && b.getKind() != Unit.Kind.MONSTER)) return;
            long time = now();
            boolean tower = a.getKind() == Unit.Kind.TOWER;
            var origin = a.getMovement().position(time);
            var destination = b.getMovement().position(time);
            double distance = Math.hypot((double) origin.x() - destination.x(), (double) origin.y() - destination.y());
            if (distance > (tower ? TowerDefenceRules.TOWER_RANGE : CombatRules.RANGE)
                    || time - a.getLastAttack() < (tower ? TowerDefenceRules.TOWER_REUSE : CombatRules.REUSE)) return;
            a.setLastAttack(time);
            visibility.update();
            var event = new AttackMessage(IdManager.nextId(), attackerId, targetId, time,
                    origin.x(), origin.y(), destination.x(), destination.y(), tower ? 0 : CombatRules.WINDUP);
            casts.put(attackerId, event);
            Set<Long> recipients = visibility.recipients(attackerId, targetId);
            shots.put(event.shotId(), new Shot(event, time + event.windup()
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
                Unit a = world.unit(shot.event.attackerId()), b = world.unit(shot.event.targetId());
                finish(shot);
                if (a == null || b == null || a.getHp() <= 0 || b.getHp() <= 0) continue;
                int damage = a.getKind() == Unit.Kind.TOWER ? TowerDefenceRules.TOWER_DAMAGE : CombatRules.DAMAGE;
                b.setHp(Math.max(0, b.getHp() - damage));
                visibility.deliver(b.getId(), new HitMessage(shot.event.shotId(), a.getId(), b.getId(),
                        damage, b.getHp(), b.getLife(), time));
                if (b.getHp() == 0) {
                    var position = b.getMovement().position(time);
                    visibility.deliver(b.getId(), new DeathMessage(b.getId(), b.getLife(), position.x(), position.y(), time));
                    cancelFor(b.getId());
                    if (b.getKind() != Unit.Kind.PLAYER) {
                        world.defence.remove(b.getId(), true);
                        continue;
                    }
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
