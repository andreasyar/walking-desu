package wand6.client;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;
import wand6.client.exceptions.PlayerManagerException;
import wand6.common.Player;
import wand6.common.Unit;
import wand6.server.messages.AppearPlayerMessage;

/** EDT-owned model and one animation owner per visible player, as in 3.2. */
final class PlayerManager {
    private static final PlayerManager INSTANCE = new PlayerManager();
    private Player selfPlayer;
    private final Map<Long, VisualPlayer> players = new LinkedHashMap<>();
    private long selected;
    private final Map<Long, wand6.server.messages.AttackMessage> shots = new LinkedHashMap<>();
    private final java.util.List<Corpse> corpses = new ArrayList<>();
    private record Corpse(long id, Unit.Kind kind, int x, int y, long time, DeathAnimation animation) { }
    private record HitEffect(wand6.server.messages.HitMessage event, int x, int y) { }
    private final Map<Long, HitEffect> hits = new HashMap<>();
    long latestAttackTime() { return shots.values().stream().mapToLong(wand6.server.messages.AttackMessage::time).max().orElse(-1); }
    int shotCount() { return shots.size(); }
    int corpseCount() { return corpses.size(); }
    long selected() { return selected; }
    void selectAt(int x, int y) {
        selected = 0;
        var ordered = new ArrayList<>(players.values());
        ordered.sort(Comparator.comparingInt(v -> -v.player.getCurY()));
        for (var v : ordered) {
            if (v.player == selfPlayer) continue;
            int px = v.player.getCurX(), py = v.player.getCurY();
            if (x >= px - 28 && x <= px + 28 && y >= py - 56 && y <= py) { selected = v.player.getId(); return; }
        }
    }
    void unselect() { selected = 0; }
    void attack(wand6.server.messages.AttackMessage event) {
        shots.putIfAbsent(event.shotId(), event);
        animateAttack(event);
    }
    private void animateAttack(wand6.server.messages.AttackMessage event) {
        Unit attacker = player(event.attackerId());
        if (attacker != null) {
            if (event.time() < attacker.getLastAttack()) return;
            attacker.setLastAttack(event.time());
        }
        var visual = players.get(event.attackerId());
        if (visual != null && visual.attack != null) {
            visual.direction = Direction.getDirection(event.bx(), event.by(), event.ex(), event.ey());
            visual.attack.run(visual.direction, event.time());
            visual.standing.run(visual.direction, event.time());
        }
    }
    void endShot(long id) { shots.remove(id); }
    void hit(wand6.server.messages.HitMessage event) {
        Unit target = player(event.targetId());
        if (target != null && target.getLife() == event.life()) { target.setHp(event.hp()); hits.put(target.getId(), new HitEffect(event, target.getCurX(), target.getCurY())); }
    }
    void die(wand6.server.messages.DeathMessage event) {
        var v = players.get(event.id());
        if (v == null || v.player.getLife() != event.life()) return;
        if (v.player.getKind() == Unit.Kind.TOWER) return;
        var animation = new DeathAnimation(v.player.getSpriteSetName());
        animation.run(v.direction, event.time());
        corpses.add(new Corpse(event.id(), v.player.getKind(), event.x(), event.y(), event.time(), animation));
        if (v.attack != null) v.attack.stop();
        shots.values().removeIf(s -> s.attackerId() == event.id() || s.targetId() == event.id());
        if (selected == event.id()) selected = 0;
    }
    void respawn(wand6.server.messages.RespawnMessage event) {
        var v = players.get(event.id());
        if (v == null || event.life() <= v.player.getLife()) return;
        v.player.setLife(event.life()); v.player.setHp(event.hp());
        v.player.place(event.x(), event.y()); if (v.attack != null) v.attack.stop();
        v.standing.run(v.direction, event.time());
    }
    private String status = "Connecting…";
    private wand6.server.messages.TowerDefenceStatusMessage defenceStatus;
    wand6.server.messages.TowerDefenceStatusMessage defenceStatus() { return defenceStatus; }
    long count(Unit.Kind kind) { return players.values().stream().filter(v -> v.player.getKind() == kind).count(); }
    java.util.List<Unit> units() { return players.values().stream().map(v -> v.player).toList(); }
    void defenceStatus(wand6.server.messages.TowerDefenceStatusMessage state) {
        if (defenceStatus != null && state.cycle() != defenceStatus.cycle()) {
            corpses.removeIf(c -> c.kind() != Unit.Kind.PLAYER);
            hits.entrySet().removeIf(e -> player(e.getKey()) == null || player(e.getKey()).getKind() != Unit.Kind.PLAYER);
        }
        defenceStatus = state;
    }
    void appearEntity(wand6.server.messages.AppearEntityMessage m) {
        if (m.kind() == Unit.Kind.PLAYER) return;
        Unit unit = new Unit(m.id(), m.name(), m.spriteSetName(), m.kind(), m.maxHp(), m.speed());
        unit.setHp(m.hp()); players.put(m.id(), new VisualPlayer(unit));
        var move = m.movement();
        try { movePlayer(m.id(), move.getBegX(), move.getBegY(), move.getEndX(), move.getEndY(), move.getBegTime()); }
        catch (PlayerManagerException ex) { throw new IllegalStateException(ex); }
    }
    void removeEntity(long id, boolean killed) {
        var v = players.get(id);
        if (v == null) return;
        players.remove(id); hits.remove(id);
        if (!killed) corpses.removeIf(c -> c.id() == id);
        shots.values().removeIf(s -> s.attackerId() == id || s.targetId() == id);
        if (selected == id) selected = 0;
    }
    void drawDefenceStatus(Graphics g) {
        g.setColor(Color.WHITE);
        if (defenceStatus != null) g.drawString("Волна " + defenceStatus.wave() + " · Пропущено "
                + defenceStatus.losses() + "/" + defenceStatus.limit() + " · Сила ×" + defenceStatus.strength(), 12, 24);
    }
    private static final class VisualPlayer {
        final Unit player;
        final MovementAnimation walking;
        final StandAnimation standing;
        final UseSkillAnimation attack;
        Direction direction = Direction.SOUTH;
        VisualPlayer(Unit player) {
            this.player = player;
            walking = new MovementAnimation(player.getSpriteSetName());
            attack = player.getKind() == Unit.Kind.PLAYER ? new UseSkillAnimation(player.getSpriteSetName()) : null;
            standing = new StandAnimation(player.getSpriteSetName());
            standing.run(direction, 0);
        }
    }
    static PlayerManager getInstance() { return INSTANCE; }
    private PlayerManager() { }
    Player selfPlayer() { return selfPlayer; }
    Unit player(long id) { var entry = players.get(id); return entry == null ? null : entry.player; }
    int size() { return (int) count(Unit.Kind.PLAYER); }
    String status() { return status; }
    void disconnected() { shots.clear(); corpses.clear(); hits.clear(); selected = 0; defenceStatus = null; players.clear(); selfPlayer = null; status = "Соединение закрыто. Закройте окно и подключитесь снова."; }
    long createPlayer(long id, String name, String spriteSetName) {
        players.put(id, new VisualPlayer(new Player(id, name, spriteSetName)));
        return id;
    }
    long createSelfPlayer(long id, String name, String spriteSetName) {
        shots.clear(); corpses.clear(); hits.clear(); selected = 0; defenceStatus = null;
        players.clear();
        createPlayer(id, name, spriteSetName);
        selfPlayer = (Player) player(id);
        status = "";
        return id;
    }
    void appear(AppearPlayerMessage message) {
        if (selfPlayer != null && selfPlayer.getId() == message.id()) return;
        createPlayer(message.id(), message.name(), message.spriteSetName());
        var move = message.movement();
        try { movePlayer(message.id(), move.getBegX(), move.getBegY(), move.getEndX(), move.getEndY(), move.getBegTime()); }
        catch (PlayerManagerException ex) { throw new IllegalStateException(ex); }
        player(message.id()).setHp(message.hp());
        player(message.id()).setLife(message.life());
        setText(message.id(), message.text());
        if (message.attackAnimation() != null) animateAttack(message.attackAnimation());
    }
    void remove(long id) {
        if (selfPlayer != null && selfPlayer.getId() == id) return;
        players.remove(id); hits.remove(id); corpses.removeIf(c -> c.id() == id);
        shots.values().removeIf(s -> s.attackerId() == id || s.targetId() == id);
        if (selected == id) selected = 0;
    }
    void setText(long id, String text) { Unit player = player(id); if (player != null) player.setText(text); }
    long getSelfPlayerId() { return selfPlayer.getId(); }
    void setSelfPlayerText(String text) { selfPlayer.setText(text); }
    int getSelfPlayerX() { return selfPlayer.getCurX(); }
    int getSelfPlayerY() { return selfPlayer.getCurY(); }
    String getSelfPlayerText() { return selfPlayer.getText(); }

    void drawPlayers(Graphics g, Camera camera, long time) {
        // Feet determine ordering; overlapping players keep independent animation state.
        var ordered = new ArrayList<>(players.values());
        ordered.sort(Comparator.comparingInt(v -> v.player.getMovement().position(time).y()));
        for (VisualPlayer visual : ordered) {
            var player = visual.player;
            var movement = player.getMovement();
            var position = movement.position(time);
            var point = new java.awt.Point(position.x(), position.y());
            Sprite sprite = visual.attack != null && visual.attack.isPlaying(time) ? visual.attack.getSprite(time, point)
                    : movement.isMovingAt(time) ? visual.walking.getSprite(point) : visual.standing.getSprite(time, point);
            if (selected == player.getId()) {
                g.setColor(Color.YELLOW); g.drawOval(point.x - camera.x() - 24, point.y - camera.y() - 8, 48, 16);
            }
            g.drawImage(sprite.image, sprite.x - camera.x(), sprite.y - camera.y(), null);
            g.setColor(Color.WHITE);
            int nameY = point.y - camera.y() - 60;
            g.setColor(Color.DARK_GRAY); g.fillRect(point.x - camera.x() - 28, nameY - 22, 56, 6);
            g.setColor(Color.GREEN); g.fillRect(point.x - camera.x() - 28, nameY - 22, 56 * player.getHp() / player.getMaxHp(), 6);
            g.setColor(Color.WHITE);
            g.drawString(player.getHp() + "/" + player.getMaxHp(), point.x - camera.x() - 20, nameY - 26);
            g.drawString(player.getName(), point.x - camera.x() - g.getFontMetrics().stringWidth(player.getName()) / 2, nameY);
            if (player.getText() != null && !player.getText().isEmpty()) {
                g.setColor(Color.YELLOW);
                g.drawString(player.getText(), point.x - camera.x() - g.getFontMetrics().stringWidth(player.getText()) / 2,
                        nameY - 52);
            }
        }
    }
    void drawEffects(Graphics g, Camera camera, long time) {
        corpses.removeIf(c -> c.animation().isDone(time));
        hits.values().removeIf(h -> time - h.event().time() > 650);
        // Defensive expiry also bounds effects if the corresponding actor leaves visibility.
        shots.values().removeIf(s -> time > s.time() + 2000);
        for (Corpse corpse : corpses) {
            Sprite sprite = corpse.animation().getSprite(time, new java.awt.Point(corpse.x(), corpse.y()));
            g.drawImage(sprite.image, sprite.x - camera.x(), sprite.y - camera.y(), null);
        }
        for (var shot : shots.values()) {
            long start = shot.time() + shot.windup();
            double duration = Math.hypot(shot.ex() - shot.bx(), shot.ey() - shot.by()) / wand6.common.CombatRules.PROJECTILE_SPEED;
            if (time < start || time > start + duration) continue;
            double fraction = duration == 0 ? 1 : Math.clamp((time - start) / duration, 0, 1);
            int x = (int) Math.round(shot.bx() + (shot.ex() - shot.bx()) * fraction);
            int y = (int) Math.round(shot.by() + (shot.ey() - shot.by()) * fraction);
            var direction = Direction.getDirection(shot.bx(), shot.by(), shot.ex(), shot.ey());
            Sprite sprite = DirectionalSpriteSet.load(shot.windup() == 0 ? "canon_bolt" : "peasant_bolt").getSprite(direction, shot.windup() == 0 ? (int) ((time - start) / 100 % 3) : 0);
            g.drawImage(sprite.image, x - camera.x(), y - camera.y(), null);
        }
        g.setColor(Color.RED);
        for (var hit : hits.values()) {
            if (hit.event().damage() == wand6.common.TowerDefenceRules.TOWER_DAMAGE) {
                var image = DirectionalSpriteSet.load("canon_hit").getSprite(Direction.EAST,
                        (int) Math.min(3, Math.max(0, time - hit.event().time()) / 150)).image;
                g.drawImage(image, hit.x() - camera.x() - image.getWidth()/2,
                        hit.y() - camera.y() - image.getHeight(), null);
            }
            g.drawOval(hit.x() - camera.x() - 20, hit.y() - camera.y() - 45, 40, 40);
            g.drawString("-" + hit.event().damage(), hit.x() - camera.x() + 26, hit.y() - camera.y() - 25);
        }
    }
    void movePlayer(long id, int bx, int by, int x, int y, long time) throws PlayerManagerException {
        VisualPlayer visual = players.get(id);
        if (visual == null) return; // Safe for an obsolete update after removal.
        visual.player.getMovement().start(bx, by, x, y, time);
        if (bx != x || by != y) {
            visual.walking.run(bx, by, x, y, 10.0);
            visual.direction = visual.walking.getDirection();
        }
        visual.standing.run(visual.direction, visual.player.getMovement().getEndTime());
    }
}
