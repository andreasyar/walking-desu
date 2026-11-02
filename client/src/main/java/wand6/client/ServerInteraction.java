package wand6.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import wand6.client.exceptions.PlayerManagerException;
import wand6.common.ServerTime;
import wand6.common.messages.Message;
import wand6.common.messages.MessageType;
import wand6.server.messages.MapFragmentMessage;
import wand6.server.messages.MoveMessage;
import wand6.server.messages.TimeSyncMessage;
import wand6.server.messages.WelcomeMessage;

final class ServerInteraction implements AutoCloseable {
    private final Socket socket;
    private final ObjectOutputStream output;
    private final ObjectInputStream input;
    private final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Message> inbound = new LinkedBlockingQueue<>();
    private final AtomicBoolean flushScheduled = new AtomicBoolean();
    private volatile boolean closed;
    private Thread sender;
    private Thread receiver;

    ServerInteraction(String host, int port) throws IOException {
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), 5000);
            socket.setSoTimeout(10000);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            socket.setSoTimeout(0);
        } catch (IOException ex) {
            socket.close();
            throw ex;
        }
    }

    void run() {
        MessageReceiver handler = new MessageReceiver();
        sender = new Thread(() -> {
            try {
                while (!closed) {
                    Message message = messages.take();
                    output.writeObject(message);
                    output.reset();
                    output.flush();
                    System.out.println(message + " --> [server]");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (IOException ex) {
                if (!closed) { System.err.println("Send failed: " + ex.getMessage()); }
            } finally { close(); }
        }, "wand6-sender");
        receiver = new Thread(() -> {
            boolean welcome = false;
            try {
                while (!closed) {
                    Message message = (Message) input.readObject();
                    if (message.getType() == MessageType.WELCOME) { welcome = true; }
                    if (welcome) {
                        inbound.add(message);
                        scheduleFlush(handler);
                    }
                }
            } catch (EOFException ex) {
                if (!closed) { System.err.println("Connection closed by server."); }
            } catch (Exception ex) {
                if (!closed) { ex.printStackTrace(); }
            } finally { close(); }
        }, "wand6-receiver");
        sender.setDaemon(true);
        receiver.setDaemon(true);
        sender.start();
        receiver.start();
    }

    private void scheduleFlush(MessageReceiver handler) {
        if (!flushScheduled.compareAndSet(false, true)) return;
        SwingUtilities.invokeLater(() -> {
            try {
                Message message;
                while (!closed && (message = inbound.poll()) != null) {
                    handler.commandHandler(message);
                }
            } finally {
                flushScheduled.set(false);
                if (!closed && !inbound.isEmpty()) scheduleFlush(handler);
            }
        });
    }

    synchronized void sendMessage(Message message) { if (!closed) messages.add(message); }

    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        messages.clear();
        inbound.clear();
        SwingUtilities.invokeLater(() -> {
            if (MessageManager.isCurrent(this)) PlayerManager.getInstance().disconnected();
        });
        if (sender != null) { sender.interrupt(); }
        try { socket.close(); } catch (IOException ignored) { }
    }
}

final class MessageReceiver {
    private static final int debugLevel = 1;
    void commandHandler(Message message) {
        if (debugLevel > 0) {
            System.out.println("[me] <-- " + message);
        }

        if (message.getType() == MessageType.WELCOME) {

            WelcomeMessage m = (WelcomeMessage) message;
            ServerTime.getInstance().adjust(m.getServerTime());
            PlayerManager.getInstance().createSelfPlayer(m.getId(),
                                                         m.getName(),
                                                         m.getSpriteSetName());
            PlayerManager.getInstance().selfPlayer().place(m.getX(), m.getY());

        } else if (message.getType() == MessageType.TIMESYNC) {

            TimeSyncMessage m = (TimeSyncMessage) message;
            long error = ServerTime.getInstance().reconcile(m.getTime());
            if (Math.abs(error) >= ServerTime.IGNORE_MS) {
                System.out.println("TIMESYNC resync errorMs=" + error);
            }

        } else if (message.getType() == MessageType.MAPFRAGMENT) {

            MapFragmentMessage m = (MapFragmentMessage) message;
            MapManager.getInstance().addMapFragment(m.getIdX(), m.getIdY(), m.getHmap());

        } else if (message instanceof wand6.server.messages.AppearItemMessage item) {
            ItemManager.getInstance().appear(item.item());
        } else if (message instanceof wand6.server.messages.DisappearItemMessage item) {
            ItemManager.getInstance().disappear(item.id());
        } else if (message instanceof wand6.server.messages.InventoryAddMessage item) {
            ItemManager.getInstance().add(item.delta());
        } else if (message instanceof wand6.server.messages.InventoryRemoveMessage item) {
            ItemManager.getInstance().remove(item.id(), item.count());
        } else if (message instanceof wand6.server.messages.PickedUpMessage) {
            // Inventory changes are carried only by inventory messages.
        } else if (message instanceof wand6.server.messages.AppearEntityMessage entity) {
            PlayerManager.getInstance().appearEntity(entity);
        } else if (message instanceof wand6.server.messages.RemoveEntityMessage entity) {
            PlayerManager.getInstance().removeEntity(entity.id(), entity.killed());
        } else if (message instanceof wand6.server.messages.TowerDefenceStatusMessage state) {
            PlayerManager.getInstance().defenceStatus(state);
        } else if (message instanceof wand6.server.messages.AppearPlayerMessage appear) {
            PlayerManager.getInstance().appear(appear);
        } else if (message instanceof wand6.server.messages.DisappearPlayerMessage disappear) {
            PlayerManager.getInstance().remove(disappear.id());
        } else if (message instanceof wand6.server.messages.PlayerTextMessage text) {
            PlayerManager.getInstance().setText(text.id(), text.text());
        } else if (message instanceof wand6.server.messages.AttackMessage attack) {
            PlayerManager.getInstance().attack(attack);
        } else if (message instanceof wand6.server.messages.EndProjectileMessage end) {
            PlayerManager.getInstance().endShot(end.shotId());
        } else if (message instanceof wand6.server.messages.HitMessage hit) {
            PlayerManager.getInstance().hit(hit);
        } else if (message instanceof wand6.server.messages.DeathMessage death) {
            PlayerManager.getInstance().die(death);
        } else if (message instanceof wand6.server.messages.RespawnMessage respawn) {
            PlayerManager.getInstance().respawn(respawn);
        } else if (message.getType() == MessageType.MOVE) {

            MoveMessage m = (MoveMessage) message;
            if (PlayerManager.getInstance().selfPlayer() != null && m.getId() == PlayerManager.getInstance().getSelfPlayerId()) ItemManager.getInstance().moved();
            try {
                PlayerManager.getInstance().movePlayer(m.getId(), m.getBegX(), m.getBegY(), m.getEndX(), m.getEndY(), m.getBegTime());
            } catch (PlayerManagerException ex) {
                ex.printStackTrace();
                throw new IllegalStateException(ex);
            }

        }
    }
}
