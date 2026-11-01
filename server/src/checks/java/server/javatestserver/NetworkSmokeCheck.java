package server.javatestserver;

import common.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;

/** Starts its own JVM and OS-assigned port; never connects to the dev server. */
public final class NetworkSmokeCheck {
    private static final class Peer implements AutoCloseable {
        final Socket socket;
        final ObjectOutputStream output;
        final ObjectInputStream input;
        final Deque<Message> inbox = new ArrayDeque<>();
        final long id;

        Peer(int port, String nick) throws Exception {
            socket = new Socket(InetAddress.getLoopbackAddress(), port);
            socket.setSoTimeout(10000);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            send("(hello)");
            send("(nick \"" + nick + "\")");
            Message hello = await(m -> text(m).startsWith("(hello "));
            id = Long.parseLong(text(hello).split(" ")[1]);
        }

        void send(String command) throws IOException {
            output.writeObject(new OtherMessage(command));
            output.flush();
            output.reset();
        }

        Message await(Predicate<Message> match) throws Exception {
            long deadline = System.nanoTime() + 10_000_000_000L;
            while (System.nanoTime() < deadline) {
                for (Iterator<Message> i = inbox.iterator(); i.hasNext();) {
                    Message message = i.next();
                    if (match.test(message)) { i.remove(); return message; }
                }
                Object packet = input.readObject();
                if (!(packet instanceof Collection<?> messages)) throw new AssertionError("Unexpected packet");
                for (Object message : messages) inbox.add((Message) message);
            }
            throw new AssertionError("Expected server message not received");
        }

        public void close() throws IOException { socket.close(); }
    }

    private static String text(Message message) {
        return message instanceof OtherMessage other ? other.getMessage() : "";
    }

    public static void main(String[] args) throws Exception {
        Path log = Files.createTempFile("walking-desu-smoke-", ".log");
        Process server = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"), JavaTestServer.class.getName(), "0")
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            int port = -1;
            for (int attempt = 0; attempt < 200; attempt++) {
                for (String line : Files.readAllLines(log)) {
                    if (line.startsWith("Server starts at port: ")) port = Integer.parseInt(line.substring(23));
                }
                if (port > 0) break;
                if (!server.isAlive()) throw new AssertionError("Test server exited");
                Thread.sleep(25);
            }
            if (port <= 0) throw new AssertionError("Test server did not bind a port");
            System.out.println("Isolated server port: " + port);
            // An aborted handshake must not terminate the server.
            try (Socket broken = new Socket(InetAddress.getLoopbackAddress(), port)) {
                broken.getInputStream().read();
            }
            try (Peer first = new Peer(port, "smoke-first"); Peer second = new Peer(port, "smoke-second")) {
                first.await(m -> text(m).startsWith("(newplayer " + second.id + " "));
                second.await(m -> text(m).startsWith("(newplayer " + first.id + " "));
                first.send("(getmapfragm 0 0)");
                HMapMessage map = (HMapMessage) first.await(m -> m instanceof HMapMessage);
                if (map.getIdx() != 0 || map.getIdy() != 0 || map.getHmap().length == 0)
                    throw new AssertionError("Map response");
                first.send("(move 100 100)");
                MoveMessage move = (MoveMessage) second.await(m -> m instanceof MoveMessage v && v.getUnitID() == first.id);
                if (move.getEndX() != 100 || move.getEndY() != 100) throw new AssertionError("Movement destination");
                first.send("(message \"smoke text\")");
                second.await(m -> text(m).contains("smoke text"));
                // Kill a moving player and verify death/respawn on both clients.
                first.send("(attack " + second.id + ")");
                second.await(m -> m instanceof HitMessage);
                second.send("(move 150 150)");
                first.await(m -> m instanceof MoveMessage v && v.getUnitID() == second.id);
                first.send("(attack " + second.id + ")");
                first.await(m -> text(m).equals("(deathplayer " + second.id + ")"));
                second.await(m -> text(m).equals("(teleport " + second.id + " 0 0)"));
                second.await(m -> text(m).equals("(deathplayer " + second.id + ")"));
                second.close();
                first.await(m -> text(m).equals("(delplayer " + second.id + ")"));
                long reconnectedId;
                try (Peer reconnected = new Peer(port, "smoke-reconnected")) {
                    reconnectedId = reconnected.id;
                    first.await(m -> text(m).startsWith("(newplayer " + reconnected.id + " "));
                    reconnected.send("(getmapfragm 0 0)");
                    reconnected.await(m -> m instanceof HMapMessage);
                    reconnected.socket.setSoLinger(true, 0); // emulate abrupt process loss / TCP reset
                }
                first.await(m -> text(m).equals("(delplayer " + reconnectedId + ")"));
            }
            if (!server.isAlive()) throw new AssertionError("Server stopped after client disconnect");
            String serverLog = Files.readString(log);
            if (serverLog.contains("EOFException")) throw new AssertionError("Normal disconnect printed an EOF stack trace");
            System.out.println("PASS: handshake failure, two logins, visibility, map, movement, text, moving PvP death, respawn, disconnect, reconnect");
        } catch (Throwable failure) {
            System.err.println("Server log: " + log);
            System.err.println(Files.readString(log));
            throw failure;
        } finally {
            server.destroy();
            if (!server.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) server.destroyForcibly();
        }
    }
}
