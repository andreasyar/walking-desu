package wand6.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import wand6.client.messages.HelloMessage;
import wand6.client.messages.NameMessage;
import wand6.client.messages.MapFragmentRequestMessage;
import wand6.client.messages.MoveRequestMessage;
import wand6.common.messages.Message;
import wand6.common.messages.MessageType;
import wand6.server.messages.MapFragmentMessage;
import wand6.server.messages.MoveMessage;
import wand6.server.messages.WelcomeMessage;

/** Separate JVM, isolated working directory, production server artifact only. */
public final class NetworkSmokeCheck {
    public static void main(String[] args) throws Exception {
        Path output = Path.of("build/network-smoke").toAbsolutePath();
        Files.createDirectories(output);
        Path log = output.resolve("server.log");
        String artifact = Path.of(args[0]).toAbsolutePath().toString();
        ProcessBuilder builder = artifact.endsWith(".jar")
                ? new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin/java").toString(), "-jar", artifact, "0")
                : new ProcessBuilder(artifact, "0");
        builder.directory(output.toFile()).redirectErrorStream(true).redirectOutput(log.toFile());
        Process server = builder.start();
        int port = 0;
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (port == 0 && System.nanoTime() < deadline) {
                for (String line : Files.readAllLines(log)) {
                    if (line.startsWith("WAND6_SERVER_PORT=")) {
                        port = Integer.parseInt(line.substring(line.indexOf('=') + 1));
                    }
                }
                if (!server.isAlive()) { throw new AssertionError(Files.readString(log)); }
                if (port == 0) { Thread.sleep(25); }
            }
            require(port > 0, "Server startup timeout");
            try (Socket socket = new Socket("localhost", port)) {
                socket.setSoTimeout(8000);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                send(out, new HelloMessage());
                send(out, new NameMessage("stage7-net"));
                WelcomeMessage welcome = (WelcomeMessage) receive(in, MessageType.WELCOME);
                require(welcome.getId() > 0 && welcome.getName().equals("stage7-net"), "WELCOME identity");
                send(out, new MapFragmentRequestMessage(0, 0));
                MapFragmentMessage map = (MapFragmentMessage) receive(in, MessageType.MAPFRAGMENT);
                require(map.getIdX() == 0 && map.getIdY() == 0 && map.getHmap().length == 32, "Map identity/height");
                for (int[] row : map.getHmap()) { require(row.length == 32, "Map width"); }
                send(out, new MoveRequestMessage(300, 400));
                MoveMessage move = (MoveMessage) receive(in, MessageType.MOVE);
                require(move.getId() == welcome.getId() && move.getEndX() == 300 && move.getEndY() == 400,
                        "Historical Dummy movement response");
            }
            System.out.println("PASS: production server " + artifact + "; port=" + port
                    + "; HELLO/NAME/WELCOME; 32x32 map; Dummy MOVE (unfinished).");
        } finally {
            server.destroy();
            if (!server.waitFor(5, TimeUnit.SECONDS)) { server.destroyForcibly().waitFor(); }
            System.out.println("Server process stopped; exit=" + server.exitValue());
        }
    }

    private static void send(ObjectOutputStream out, Message message) throws Exception {
        out.writeObject(message); out.reset(); out.flush();
    }
    private static Message receive(ObjectInputStream in, MessageType type) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            Message message = (Message) in.readObject();
            if (message.getType() == type) { return message; }
        }
        throw new AssertionError("Timeout awaiting " + type);
    }
    private static void require(boolean value, String message) {
        if (!value) { throw new AssertionError(message); }
    }
}
