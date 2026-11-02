package wand6.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import wand6.common.messages.Message;
import wand6.client.messages.*;
import wand6.server.messages.*;

/** Real object streams; only positioning fixtures use package access to the world monitor. */
public final class MultiplayerCheck {
    public static void main(String[] args) throws Exception {
        JavaServer server = new JavaServer(0);
        Thread accept = new Thread(() -> {
            try { server.run(); } catch (IOException ex) { throw new RuntimeException(ex); }
        }, "check-accept");
        accept.start();
        try (server; Peer one = new Peer(server.port(), "stage9-one")) {
            long a = one.id();
            // Partial header must neither stall accept nor leave resources after close.
            try (Socket partial = new Socket("localhost", server.port()); Peer two = new Peer(server.port(), "stage9-two")) {
                long b = two.id();
                var first = (AppearPlayerMessage) one.await(m -> m instanceof AppearPlayerMessage p && p.id() == b, 0);
                require(first.name().equals("stage9-two") && first.movement().getBegX() == 512, "initial world snapshot");
                two.await(m -> m instanceof AppearPlayerMessage p && p.id() == a, 0);
                try (Peer hidden = new Peer(server.port(), "stage9-tri")) {
                    long c = hidden.id();
                    place(server, b, 1005, 512);
                    place(server, c, 1400, 512);
                    hidden.await(m -> m instanceof DisappearPlayerMessage p && p.id() == a, 0);
                    // Exact radius is inclusive; 501 pixels must remove visibility.
                    int inclusiveMark = one.mark();
                    place(server, b, 1012, 512);
                    one.barrier();
                    require(one.since(inclusiveMark).stream().noneMatch(m -> m instanceof DisappearPlayerMessage p && p.id() == b), "exactly 500 remains visible");
                    int edgeMark = one.mark();
                    place(server, b, 1013, 512);
                    one.await(m -> m instanceof DisappearPlayerMessage p && p.id() == b, edgeMark);
                    place(server, b, 1005, 512);
                    one.await(m -> m instanceof AppearPlayerMessage p && p.id() == b, edgeMark);
                    int oneMark = one.mark(), hiddenMark = hidden.mark();
                    one.send(MoveRequestMessage.stop());
                    one.await(m -> m instanceof MoveMessage v && v.getId() == a, oneMark);
                    one.send(new TextCloudMessage("Привет, игрок!"));
                    two.await(m -> m instanceof PlayerTextMessage t && t.id() == a && t.text().equals("Привет, игрок!"), 0);
                    one.await(m -> m instanceof PlayerTextMessage t && t.id() == a, oneMark);
                    // An ordered map reply is a delivery barrier for the prior command.
                    one.barrier();
                    hidden.barrier();
                    require(hidden.since(hiddenMark).stream().noneMatch(m -> m instanceof PlayerTextMessage t && t.id() == a), "text must not reach hidden peer");
                    require(hidden.since(hiddenMark).stream().noneMatch(m -> m instanceof MoveMessage v && v.getId() == a), "movement must not reach hidden peer");
                    int departure = one.mark();
                    two.send(new MoveRequestMessage(1120, 512));
                    var outgoing = (MoveMessage) two.await(m -> m instanceof MoveMessage v && v.getId() == b && v.getEndX() == 1120, 0);
                    one.await(m -> m instanceof MoveMessage v && v.getId() == b && v.getBegTime() == outgoing.getBegTime(), departure);
                    one.await(m -> m instanceof DisappearPlayerMessage p && p.id() == b, departure);
                    int invisibleMark = one.mark();
                    two.send(new TextCloudMessage("Вне области"));
                    two.await(m -> m instanceof PlayerTextMessage t && t.id() == b, 0);
                    one.barrier();
                    require(one.since(invisibleMark).stream().noneMatch(m -> m instanceof PlayerTextMessage t && t.id() == b), "departed peer text filtered");
                    int clearMark = one.mark();
                    one.send(new TextCloudMessage(""));
                    one.await(m -> m instanceof PlayerTextMessage t && t.id() == a && t.text().isEmpty(), clearMark);
                    one.send(new TextCloudMessage("Привет, игрок!"));
                    one.await(m -> m instanceof PlayerTextMessage t && t.id() == a && t.text().equals("Привет, игрок!"), clearMark);
                    // Wait for arrival; retarget back across the boundary, no commands at crossing.
                    await(() -> server.players.snapshot().stream().filter(p -> p.getId() == b)
                            .noneMatch(p -> p.getMovement().isMoving()), "outgoing arrival");
                    int arrivalMark = one.mark();
                    two.send(new MoveRequestMessage(900, 512));
                    var returning = (MoveMessage) two.await(m -> m instanceof MoveMessage v && v.getId() == b && v.getEndX() == 900, 0);
                    var appeared = (AppearPlayerMessage) one.await(m -> m instanceof AppearPlayerMessage p && p.id() == b, arrivalMark);
                    require(appeared.movement().getBegTime() == returning.getBegTime()
                            && appeared.movement().getBegX() == 1120 && appeared.movement().getEndX() == 900,
                            "crossing sends original running segment");
                    require(appeared.text().equals("Вне области"), "crossing restores current text");
                    require(one.since(arrivalMark).stream().filter(m -> m instanceof AppearPlayerMessage p && p.id() == b).count() == 1, "no duplicate appearance");
                    // Late login receives a moving player's original segment.
                    try (Peer late = new Peer(server.port(), "stage9-new")) {
                        var state = (AppearPlayerMessage) late.await(m -> m instanceof AppearPlayerMessage p && p.id() == b, 0);
                        require(state.movement().getBegTime() == returning.getBegTime(), "late login preserves movement time");
                    }
                    int disconnected = one.mark();
                    two.abort();
                    one.await(m -> m instanceof DisappearPlayerMessage p && p.id() == b, disconnected);
                    await(() -> server.players.snapshot().size() == 2 && server.connectionCount() == 3, "RST removes player and sender");
                    // partial connection still counts until its socket closes below.
                    try (Peer rejoin = new Peer(server.port(), "stage9-two")) {
                        long freshId = rejoin.id();
                        require(freshId != b, "reconnect gets fresh identity");
                        one.await(m -> m instanceof AppearPlayerMessage p && p.id() == freshId, disconnected);
                        var state = (AppearPlayerMessage) rejoin.await(m -> m instanceof AppearPlayerMessage p && p.id() == a, 0);
                        require(state.text().equals("Привет, игрок!"), "late login restores prior text");
                    }
                }
            }
            // Invalid name is isolated; the surviving player can still request a map.
            try (Peer invalid = new Peer(server.port(), null)) {
                invalid.send(new HelloMessage()); invalid.send(new NameMessage("x"));
                await(() -> invalid.ended, "invalid login closed");
            }
            for (int i = 0; i < 12; i++) {
                try (Peer reconnect = new Peer(server.port(), "cycle-" + i)) { reconnect.id(); }
            }
            await(() -> server.players.snapshot().size() == 1 && server.connectionCount() == 1
                    && server.visibility.trackedPlayers() == 1, "no stale players/connections/visibility");
            await(() -> Thread.getAllStackTraces().keySet().stream().filter(t -> t.getName().startsWith("wand6-reader-")
                    || t.getName().startsWith("wand6-writer-")).count() == 2, "only survivor reader and writer remain");
            one.barrier();
            System.out.println("PASS: multiplayer port=" + server.port() + "; initial/late world, original movement time, 500px boundary crossed without click, recipients, Cyrillic text, RST/reconnect, partial header, invalid login, 12 reconnects, resource cleanup.");
        } finally {
            server.close(); accept.join(5000);
            require(!accept.isAlive(), "accept thread stopped");
        }
        await(() -> Thread.getAllStackTraces().keySet().stream().noneMatch(t -> t.getName().startsWith("wand6-")), "all server workers stopped");
    }
    private static void place(JavaServer server, long id, int x, int y) {
        synchronized (server.players) {
            server.players.snapshot().stream().filter(p -> p.getId() == id).findFirst().orElseThrow().place(x, y);
            server.visibility.update();
        }
    }
    static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static void await(BooleanSupplier condition, String description) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        while (System.nanoTime() < end) { if (condition.getAsBoolean()) return; Thread.sleep(20); }
        throw new AssertionError("Timeout: " + description);
    }
    static final class Peer implements AutoCloseable {
        final Socket socket;
        final ObjectOutputStream output;
        final ObjectInputStream input;
        final CopyOnWriteArrayList<Message> inbox = new CopyOnWriteArrayList<>();
        final Thread reader;
        volatile boolean ended;
        Peer(int port, String name) throws Exception {
            socket = new Socket("localhost", port);
            socket.setSoTimeout(5000);
            output = new ObjectOutputStream(socket.getOutputStream()); output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            socket.setSoTimeout(0);
            reader = new Thread(() -> {
                try { while (true) inbox.add((Message) input.readObject()); }
                catch (Exception ex) { ended = true; }
            }, "check-peer");
            reader.start();
            if (name != null) { send(new HelloMessage()); send(new NameMessage(name)); id(); }
        }
        long id() throws Exception { return ((WelcomeMessage) await(m -> m instanceof WelcomeMessage, 0)).getId(); }
        synchronized void send(Message message) throws Exception { output.writeObject(message); output.reset(); output.flush(); }
        int mark() { return inbox.size(); }
        List<Message> since(int mark) { return List.copyOf(inbox.subList(mark, inbox.size())); }
        Message await(Predicate<Message> predicate, int mark) throws Exception {
            final Message[] found = new Message[1];
            MultiplayerCheck.await(() -> { for (Message m : since(mark)) if (predicate.test(m)) { found[0] = m; return true; } return false; }, "network message");
            return found[0];
        }
        void barrier() throws Exception { int mark = mark(); send(new MapFragmentRequestMessage(0, 0)); await(m -> m instanceof MapFragmentMessage, mark); }
        void abort() throws Exception { socket.setSoLinger(true, 0); close(); }
        @Override public void close() throws Exception {
            socket.close(); reader.join(2000);
            require(!reader.isAlive(), "peer reader stopped");
            Set<Long> known = new HashSet<>();
            for (Message message : inbox) {
                if (message instanceof WelcomeMessage w) known.add(w.getId());
                else if (message instanceof AppearPlayerMessage p) require(known.add(p.id()), "duplicate appearance");
                else if (message instanceof DisappearPlayerMessage p) require(known.remove(p.id()), "duplicate disappearance");
                else if (message instanceof MoveMessage m) require(known.contains(m.getId()), "MOVE before appearance/after removal");
                else if (message instanceof PlayerTextMessage t) require(known.contains(t.id()), "text before appearance/after removal");
            }
        }
    }
}
