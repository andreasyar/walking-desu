package wand6.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
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
                MessageReceiver handler = new MessageReceiver();
                while (!closed) {
                    Message message = (Message) input.readObject();
                    if (message.getType() == MessageType.WELCOME) { welcome = true; }
                    if (welcome) {
                        SwingUtilities.invokeAndWait(() -> { if (!closed) handler.commandHandler(message); });
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

    synchronized void sendMessage(Message message) { if (!closed) messages.add(message); }

    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        messages.clear();
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
            ServerTime.getInstance().adjust(m.getTime());

        } else if (message.getType() == MessageType.MAPFRAGMENT) {

            MapFragmentMessage m = (MapFragmentMessage) message;
            MapManager.getInstance().addMapFragment(m.getIdX(), m.getIdY(), m.getHmap());

        } else if (message instanceof wand6.server.messages.AppearPlayerMessage appear) {
            PlayerManager.getInstance().appear(appear);
        } else if (message instanceof wand6.server.messages.DisappearPlayerMessage disappear) {
            PlayerManager.getInstance().remove(disappear.id());
        } else if (message instanceof wand6.server.messages.PlayerTextMessage text) {
            PlayerManager.getInstance().setText(text.id(), text.text());
        } else if (message.getType() == MessageType.MOVE) {

            MoveMessage m = (MoveMessage) message;
            try {
                PlayerManager.getInstance().movePlayer(m.getId(), m.getBegX(), m.getBegY(), m.getEndX(), m.getEndY(), m.getBegTime());
            } catch (PlayerManagerException ex) {
                ex.printStackTrace();
                throw new IllegalStateException(ex);
            }

        }
    }
}
