package wand6.server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import wand6.common.messages.Message;
import wand6.client.messages.*;

/** Queues are owned by the connection; login/world/close serialize on PlayerManager. */
final class ClientInteraction implements AutoCloseable {
    private final Socket socket;
    private final JavaServer server;
    private final PlayerManager players;
    private final MessageManager messages;
    private final MapManager maps;
    private final LinkedBlockingQueue<Message> outbound = new LinkedBlockingQueue<>();
    private final AtomicBoolean closing = new AtomicBoolean();
    private volatile long id;
    private volatile boolean ready;
    private Thread sender, receiver;

    ClientInteraction(Socket socket, JavaServer server, PlayerManager players, MessageManager messages, MapManager maps) {
        this.socket = socket; this.server = server; this.players = players;
        this.messages = messages; this.maps = maps;
    }
    void run() {
        receiver = new Thread(this::receive, "wand6-reader-" + socket.getPort());
        receiver.start();
    }
    long getSelfPlayerId() { return id; }
    boolean isReady() { return ready && !closing.get(); }
    boolean sendMessage(Message message) {
        if (closing.get()) return false;
        outbound.offer(message);
        return true;
    }
    private void receive() {
        try {
            // An incomplete stream header cannot stall accept or hold the world monitor.
            socket.setSoTimeout(10000);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            synchronized (players) {
                if (closing.get()) return;
                sender = new Thread(() -> send(output), "wand6-writer-" + socket.getPort());
                sender.start();
            }
            boolean hello = false;
            while (!closing.get()) {
                Object object = input.readObject();
                if (!(object instanceof Message message)) throw new IOException("Expected a message");
                synchronized (players) {
                    if (closing.get()) break;
                    if (!hello) {
                        if (!(message instanceof HelloMessage)) throw new IOException("Expected HELLO");
                        hello = true;
                    } else if (!ready) {
                        if (!(message instanceof NameMessage name)) throw new IOException("Expected NAME");
                        validateName(name.getName());
                        id = players.createPlayer(name.getName());
                        ready = true;
                        messages.sendWelcome(id);
                        players.joined();
                        server.defence.sendStatus(id);
                        socket.setSoTimeout(0);
                    } else if (message instanceof MoveRequestMessage move) {
                        players.movePlayer(id, move.getX(), move.getY(), move.isStop());
                    } else if (message instanceof PickupItemMessage pickup) {
                        server.items.pickup(id, pickup.id());
                    } else if (message instanceof BuildTowerMessage build) {
                        server.defence.build(id, build.x(), build.y());
                    } else if (message instanceof AttackRequestMessage attack) {
                        server.combat.attack(id, attack.targetId());
                    } else if (message instanceof TextCloudMessage text) {
                        players.setPlayerText(id, text.getText());
                    } else if (message instanceof MapFragmentRequestMessage map) {
                        if (map.getIdX() >= 0 && map.getIdY() >= 0
                                && map.getIdX() <= wand6.common.MapFragment.maxIdX
                                && map.getIdY() <= wand6.common.MapFragment.maxIdY) {
                            messages.sendMapFragment(id, maps.getMapFragment(map.getIdX(), map.getIdY()));
                        }
                    } else throw new IOException("Unexpected message: " + message.getType());
                }
            }
        } catch (EOFException ignored) {
        } catch (Exception ex) {
            if (!closing.get()) System.err.println("Client " + id + " disconnected: " + ex.getMessage());
        } finally { close(); }
    }
    private static void validateName(String name) {
        if (name == null || name.length() < 3 || name.length() > 10 || name.contains("\n")
                || name.contains("\r") || name.contains("\"")) throw new IllegalArgumentException("Invalid name");
    }
    private void send(ObjectOutputStream output) {
        try {
            while (!closing.get()) {
                Message message = outbound.take();
                output.writeObject(message); output.reset(); output.flush();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            if (!closing.get()) System.err.println("Client " + id + " send failed: " + ex.getMessage());
        } finally { close(); }
    }
    @Override public void close() {
        if (!closing.compareAndSet(false, true)) return;
        // Close the socket before taking the monitor, unblocking a concurrent write/read.
        try { socket.close(); } catch (IOException ignored) { }
        synchronized (players) {
            ready = false;
            server.remove(this);
            if (id != 0) players.removePlayer(id);
            if (sender != null) sender.interrupt();
            outbound.clear();
        }
    }
}
