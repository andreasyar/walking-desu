package wand6.server;

import java.util.*;
import wand6.common.Player;
import wand6.common.ServerTime;
import wand6.common.messages.Message;
import wand6.server.messages.*;

/** Adapted from 3.2 VisibleManager/Player/MessageService, player scope only.
 * All visibility and queue insertions use the PlayerManager world monitor.
 */
final class VisibleManager {
    static final double RANGE = 500.0;
    private MessageManager messages;
    private PlayerManager world;
    private final Map<Long, Set<Long>> visible = new HashMap<>();
    void init(MessageManager messages, PlayerManager world) { this.messages = messages; this.world = world; }

    void update() {
        synchronized (world) {
            long now = ServerTime.getInstance().getTimeSinceStart();
            var players = world.snapshot();
            for (Player observer : players) {
                Set<Long> seen = visible.computeIfAbsent(observer.getId(), id -> new HashSet<>());
                var origin = observer.getMovement().position(now);
                for (Player target : players) {
                    if (observer == target) continue;
                    var position = target.getMovement().position(now);
                    boolean inRange = Math.hypot((double) origin.x() - position.x(),
                            (double) origin.y() - position.y()) <= RANGE;
                    if (inRange && seen.add(target.getId())) {
                        messages.send(observer.getId(), new AppearPlayerMessage(target.getId(), target.getName(),
                                target.getSpriteSetName(), segment(target), target.getText()));
                    } else if (!inRange && seen.remove(target.getId())) {
                        messages.send(observer.getId(), new DisappearPlayerMessage(target.getId()));
                    }
                }
            }
        }
    }
    private MoveMessage segment(Player player) {
        var move = player.getMovement();
        return new MoveMessage(player.getId(), move.getBegX(), move.getBegY(), move.getEndX(), move.getEndY(), move.getBegTime());
    }
    void movementStartNotify(long id) {
        synchronized (world) {
            update();
            Player player = world.snapshot().stream().filter(p -> p.getId() == id).findFirst().orElseThrow();
            deliver(id, segment(player));
        }
    }
    void textChanged(Player player) {
        synchronized (world) {
            update();
            deliver(player.getId(), new PlayerTextMessage(player.getId(), player.getText()));
        }
    }
    private void deliver(long id, Message message) {
        messages.send(id, message);
        visible.forEach((observer, seen) -> { if (seen.contains(id)) messages.send(observer, message); });
    }
    void remove(long id) {
        synchronized (world) {
            visible.remove(id);
            visible.forEach((observer, seen) -> {
                if (seen.remove(id)) messages.send(observer, new DisappearPlayerMessage(id));
            });
        }
    }
    int trackedPlayers() { synchronized (world) { return visible.size(); } }
}
