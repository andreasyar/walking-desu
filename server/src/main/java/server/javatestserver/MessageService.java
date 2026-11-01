package server.javatestserver;

import common.Message;
import common.OtherMessage;
import java.util.Collection;

/** Queues delivery under world; each connection's sender performs I/O independently. */
final class MessageService {
    interface Connection {
        boolean loggedIn();
        Player getSelfPlayer();
        void addMessage(String message);
        void addMessage(Message message);
    }
    private final Collection<? extends Connection> connections;
    private final WorldState world;
    MessageService(WorldState world, Collection<? extends Connection> connections) {
        this.world = world; this.connections = connections;
    }
    void attack(JTSUnit attacker, long targetId, long time, boolean animated) {
        if (animated) sendToAll(new String[]{"(attack " + attacker.getID() + " " + targetId + " " + time + ")"});
        else {
            Message[] packet = {new common.BoltMessage(attacker.getID(), targetId, time)};
            if (attacker instanceof Player) sendToAll(packet, attacker.getID());
            else sendToAll(packet);
        }
    }
    void hit(JTSUnit attacker, JTSUnit target) {
        var hit = new common.HitMessage(attacker.getID(), target.getID(), attacker.getDamage());
        sendFromUnit(hit, target);
        // A player does not include itself in its visible-unit list.
        if (target instanceof Player player) sendTo(hit, player);
    }
    void teleport(Player player) {
        sendToAll(new String[]{"(teleport " + player.getID() + " " + player.getCurPos().x + " " + player.getCurPos().y + ")"});
    }
    void sendFromUnit(Message message, JTSUnit unit) {
        synchronized (world) {
            for (Connection connection : connections)
                if (connection.loggedIn() && connection.getSelfPlayer().see(unit)) connection.addMessage(message);
        }
    }
    void sendTo(String message, Player player) { sendTo(new OtherMessage(message), player); }
    void sendTo(Message message, Player player) {
        synchronized (world) {
            for (Connection connection : connections) {
                if (connection.loggedIn() && connection.getSelfPlayer() == player) {
                    connection.addMessage(message);
                    return;
                }
            }
        }
    }
    void sendToAll(String[] messages) { sendToAll(messages, -1); }
    void sendToAll(Message[] messages) { sendToAll(messages, -1); }
    void sendToAll(String[] messages, long excluded) {
        for (String message : messages) sendToAll(new Message[]{new OtherMessage(message)}, excluded);
    }
    void sendToAll(Message[] messages, long excluded) {
        synchronized (world) {
            for (Message message : messages)
                for (Connection connection : connections)
                    if (connection.loggedIn() && connection.getSelfPlayer().getID() != excluded) connection.addMessage(message);
        }
    }
}
