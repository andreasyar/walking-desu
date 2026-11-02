package wand6.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import wand6.common.ServerTime;
import wand6.common.messages.Message;
import wand6.server.exceptions.JavaServerException;
import wand6.server.messages.TimeSyncMessage;

/** PlayerManager is the world monitor; connection I/O never runs under it. */
public final class JavaServer implements AutoCloseable {
    private final ServerSocket socket;
    private final CopyOnWriteArrayList<ClientInteraction> clients = new CopyOnWriteArrayList<>();
    final PlayerManager players = new PlayerManager();
    final VisibleManager visibility = new VisibleManager();
    private final MessageManager messages = new MessageManager();
    private final MapManager maps = new MapManager();
    private final ScheduledExecutorService clock = Executors.newSingleThreadScheduledExecutor(r ->
            new Thread(r, "wand6-world-clock"));
    private volatile boolean closed;

    public static void main(String[] args) throws IOException {
        try (JavaServer server = new JavaServer(args.length == 0 ? 45000 : Integer.parseInt(args[0]))) {
            Runtime.getRuntime().addShutdownHook(new Thread(server::close, "wand6-shutdown"));
            server.run();
        }
    }

    JavaServer(int port) throws IOException {
        socket = new ServerSocket(port);
        ServerTime.getInstance();
        messages.init(this, players);
        visibility.init(messages, players);
        players.init(visibility);
        System.out.println("WAND6_SERVER_PORT=" + port());
    }

    int port() { return socket.getLocalPort(); }
    int connectionCount() { return clients.size(); }
    void remove(ClientInteraction client) { clients.remove(client); }

    void run() throws IOException {
        clock.scheduleAtFixedRate(visibility::update, 0, 50, TimeUnit.MILLISECONDS);
        clock.scheduleAtFixedRate(() -> sendMessage(new TimeSyncMessage(
                ServerTime.getInstance().getTimeSinceStart())), 0, 10, TimeUnit.SECONDS);
        try {
            while (!closed) {
                var accepted = socket.accept();
                synchronized (players) {
                    if (closed) { accepted.close(); break; }
                    ClientInteraction client = new ClientInteraction(accepted, this, players, messages, maps);
                    clients.add(client);
                    client.run(); // Starts workers; stream handshake is performed by the reader.
                }
            }
        } catch (SocketException ex) {
            if (!closed) throw ex;
        } finally { close(); }
    }

    public boolean sendMessage(Message message, long clientId) throws JavaServerException {
        for (ClientInteraction client : clients) {
            if (client.getSelfPlayerId() == clientId && client.isReady()) {
                return client.sendMessage(message);
            }
        }
        return false; // A disconnected recipient must not terminate the world or other deliveries.
    }

    public void sendMessage(Message message) {
        synchronized (players) {
            for (ClientInteraction client : clients) if (client.isReady()) client.sendMessage(message);
        }
    }

    @Override public void close() {
        synchronized (players) {
            if (closed) return;
            closed = true;
            try { socket.close(); } catch (IOException ignored) { }
            for (ClientInteraction client : clients) client.close();
            clock.shutdownNow();
        }
    }
}
