package wand6.client;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;
import wand6.client.exceptions.PlayerManagerException;
import wand6.common.Player;
import wand6.server.messages.AppearPlayerMessage;

/** EDT-owned model and one animation owner per visible player, as in 3.2. */
final class PlayerManager {
    private static final PlayerManager INSTANCE = new PlayerManager();
    private Player selfPlayer;
    private final Map<Long, VisualPlayer> players = new LinkedHashMap<>();
    private String status = "Connecting…";
    private static final class VisualPlayer {
        final Player player;
        final MovementAnimation walking;
        final StandAnimation standing;
        Direction direction = Direction.SOUTH;
        VisualPlayer(Player player) {
            this.player = player;
            walking = new MovementAnimation(player.getSpriteSetName());
            standing = new StandAnimation(player.getSpriteSetName());
            standing.run(direction, 0);
        }
    }
    static PlayerManager getInstance() { return INSTANCE; }
    private PlayerManager() { }
    Player selfPlayer() { return selfPlayer; }
    Player player(long id) { var entry = players.get(id); return entry == null ? null : entry.player; }
    int size() { return players.size(); }
    String status() { return status; }
    void disconnected() { players.clear(); selfPlayer = null; status = "Соединение закрыто. Закройте окно и подключитесь снова."; }
    long createPlayer(long id, String name, String spriteSetName) {
        players.put(id, new VisualPlayer(new Player(id, name, spriteSetName)));
        return id;
    }
    long createSelfPlayer(long id, String name, String spriteSetName) {
        players.clear();
        createPlayer(id, name, spriteSetName);
        selfPlayer = player(id);
        status = "";
        return id;
    }
    void appear(AppearPlayerMessage message) {
        if (selfPlayer != null && selfPlayer.getId() == message.id()) return;
        createPlayer(message.id(), message.name(), message.spriteSetName());
        var move = message.movement();
        try { movePlayer(message.id(), move.getBegX(), move.getBegY(), move.getEndX(), move.getEndY(), move.getBegTime()); }
        catch (PlayerManagerException ex) { throw new IllegalStateException(ex); }
        setText(message.id(), message.text());
    }
    void remove(long id) { if (selfPlayer == null || selfPlayer.getId() != id) players.remove(id); }
    void setText(long id, String text) { Player player = player(id); if (player != null) player.setText(text); }
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
            Sprite sprite = movement.isMovingAt(time)
                    ? visual.walking.getSprite(point) : visual.standing.getSprite(time, point);
            g.drawImage(sprite.image, sprite.x - camera.x(), sprite.y - camera.y(), null);
            g.setColor(Color.WHITE);
            int nameY = sprite.y - camera.y() - 4;
            g.drawString(player.getName(), point.x - camera.x() - g.getFontMetrics().stringWidth(player.getName()) / 2, nameY);
            if (player.getText() != null && !player.getText().isEmpty()) {
                g.setColor(Color.YELLOW);
                g.drawString(player.getText(), point.x - camera.x() - g.getFontMetrics().stringWidth(player.getText()) / 2,
                        nameY - g.getFontMetrics().getHeight());
            }
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
