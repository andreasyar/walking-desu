package server.javatestserver;

import common.*;
import common.items.*;
import common.messages.*;
import server.javatestserver.items.ServerEtc;
import java.io.*;
import java.net.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/** Real sockets + authoritative world inspection. All fixtures stay in the checks source set. */
public final class ItemNetworkCheck {
    private static JavaTestServer server;
    private static VisibleManager world;
    private static Queue<Item> ground;
    private static Queue<Player> players;
    private static int port;

    @SuppressWarnings("unchecked")
    private static <T> T field(String name) throws Exception {
        Field field = JavaTestServer.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(server);
    }
    private static void require(boolean value, String name) {
        if (!value) throw new AssertionError(name);
    }
    private static void await(BooleanSupplier predicate, String name) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!predicate.getAsBoolean()) {
            if (System.nanoTime() > end) throw new AssertionError("Timeout: " + name);
            Thread.sleep(10);
        }
    }
    private static String text(Message message) { return message instanceof OtherMessage m ? m.getMessage() : ""; }

    private static final class Peer implements AutoCloseable {
        final Socket socket;
        final ObjectOutputStream out;
        final List<Message> history = new CopyOnWriteArrayList<>();
        final Inventory inventory = new Inventory();
        volatile Throwable failure;
        long id;
        Peer(String nick) throws Exception {
            socket = new Socket(InetAddress.getLoopbackAddress(), port);
            out = new ObjectOutputStream(socket.getOutputStream()); out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Thread reader = new Thread(() -> {
                try {
                    while (!socket.isClosed()) {
                        Collection<?> packet = (Collection<?>) in.readObject();
                        for (Object value : packet) {
                            Message message = (Message) value;
                            if (message instanceof InventoryAddEtcItem m) inventory.addEtc(coins(m.getId(), m.getCount(), 0));
                            if (message instanceof InventoryRemoveEtcItem m) inventory.removeEtc(m.getId(), m.getCount());
                            history.add(message);
                        }
                    }
                } catch (Throwable ex) { if (!socket.isClosed()) failure = ex; }
            }, "item-check-reader");
            reader.setDaemon(true); reader.start();
            // An unauthenticated pickup must be ignored, not terminate the server.
            send(new PickupEtcItem(999, 100001));
            send(new OtherMessage("(hello)")); send(new OtherMessage("(nick \"" + nick + "\")"));
            await(() -> history.stream().anyMatch(m -> text(m).startsWith("(hello ")), "login");
            id = Long.parseLong(history.stream().map(ItemNetworkCheck::text).filter(t -> t.startsWith("(hello ")).findFirst().orElseThrow().split(" ")[1]);
        }
        synchronized void send(Message message) throws IOException { out.writeObject(message); out.flush(); out.reset(); }
        void pickup(long item) throws IOException { send(new PickupEtcItem(-999, item)); }
        long count(Predicate<Message> predicate) { return history.stream().filter(predicate).count(); }
        int gold() { synchronized (inventory) { var gold = inventory.getEtc(Items.GOLD); return gold.isEmpty() ? 0 : gold.get(0).getCount(); } }
        long stackId() { return inventory.getEtc(Items.GOLD).get(0).getID(); }
        Player player() { return players.stream().filter(p -> p.getID() == id).findFirst().orElseThrow(); }
        public void close() throws IOException { socket.close(); }
    }
    private static ServerEtc coins(long id, int quantity, int x) {
        ServerEtc coins = new ServerEtc(id, "Gold", Items.GOLD, quantity); coins.setX(x); return coins;
    }
    private static void seed(long id, int quantity, int x) {
        synchronized (world) { ground.add(coins(id, quantity, x)); world.update(); }
    }
    private static boolean onGround(long id) { synchronized (world) { return ground.stream().anyMatch(i -> i.getID() == id); } }
    private static int serverGold(Peer peer) {
        synchronized (world) { var gold = peer.player().getInventory().getEtc(Items.GOLD); return gold.isEmpty() ? 0 : gold.get(0).getCount(); }
    }
    private static void synced(Peer peer, int quantity) throws Exception {
        await(() -> peer.gold() == quantity && serverGold(peer) == quantity, "client/server quantity " + quantity);
        require(peer.failure == null, "reader stays alive: " + peer.failure);
    }
    private static void barrier(Peer peer) throws Exception {
        long previous = peer.count(m -> m instanceof HMapMessage);
        peer.send(new OtherMessage("(getmapfragm 0 0)"));
        await(() -> peer.count(m -> m instanceof HMapMessage) > previous, "request processing barrier");
    }
    private static void scenarios() throws Exception {
        try (Peer first = new Peer("items-one"); Peer second = new Peer("items-two")) {
            seed(100001, 7, 0);
            await(() -> first.count(m -> m instanceof AppearEtcItem a && a.getId() == 100001) == 1
                    && second.count(m -> m instanceof AppearEtcItem a && a.getId() == 100001) == 1, "both see ground item");
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService race = Executors.newFixedThreadPool(2);
            try {
                Future<?> one = race.submit(() -> { try { start.await(); first.pickup(100001); } catch (Exception ex) { throw new RuntimeException(ex); } });
                Future<?> two = race.submit(() -> { try { start.await(); second.pickup(100001); } catch (Exception ex) { throw new RuntimeException(ex); } });
                start.countDown(); one.get(); two.get();
            } finally { race.shutdownNow(); }
            barrier(first); barrier(second);
            require(first.gold() + second.gold() == 7 && !onGround(100001), "exactly one winner");
            Peer winner = first.gold() == 7 ? first : second;
            Peer loser = winner == first ? second : first;
            synced(winner, 7); synced(loser, 0);
            require(winner.stackId() == 100001, "initial stack ID");
            await(() -> first.count(m -> m instanceof DisappearEtcItem d && d.getId() == 100001) == 1
                    && second.count(m -> m instanceof DisappearEtcItem d && d.getId() == 100001) == 1, "disappear for both observers");
            require(loser.count(m -> m instanceof PickupEtcItem) == 0, "no false pickup acknowledgement");
            first.pickup(100001); second.pickup(100001); barrier(first); barrier(second);
            synced(winner, 7); synced(loser, 0);
            seed(100002, 5, 0); winner.pickup(100002); barrier(winner); synced(winner, 12);
            require(winner.stackId() == 100001, "canonical ID after merge");
            require(winner.count(m -> m instanceof InventoryAddEtcItem a && a.getId() == 100001 && a.getCount() == 5) == 1, "merge delta uses canonical ID");

            seed(100003, 3, 100); loser.pickup(100003); barrier(loser);
            synced(loser, 0); require(onGround(100003), "far pickup rejected without removal");
            long disappears = loser.count(m -> m instanceof DisappearEtcItem d && d.getId() == 100003);
            synchronized (world) { loser.player().teleportTo(1000, 0); world.update(); }
            await(() -> loser.count(m -> m instanceof DisappearEtcItem d && d.getId() == 100003) > disappears, "visibility exit");
            synced(loser, 0);
            synchronized (world) { loser.player().teleportTo(100, 0); world.update(); }
            await(() -> loser.count(m -> m instanceof AppearEtcItem a && a.getId() == 100003) == 2, "visibility reentry");
            loser.pickup(100003); barrier(loser); synced(loser, 3);
            synchronized (world) { loser.player().teleportTo(1000, 0); world.update(); loser.player().teleportTo(0, 0); world.update(); }
            barrier(loser);
            require(loser.count(m -> m instanceof AppearEtcItem a && a.getId() == 100003) == 2, "no ghost after picked item reentry");

            // The real PvP/death path must remove the canonical inventory stack and drop it.
            loser.send(new OtherMessage("(attack " + winner.id + ")"));
            await(() -> winner.count(m -> m instanceof HitMessage) > 0, "first hit");
            Thread.sleep(800);
            loser.send(new OtherMessage("(attack " + winner.id + ")"));
            await(() -> winner.count(m -> text(m).equals("(deathplayer " + winner.id + ")")) > 0, "death drops gold");
            synced(winner, 0); require(onGround(100001), "death drop retains canonical ID");
            loser.pickup(100001); barrier(loser); synced(loser, 15);
            require(loser.stackId() == 100003, "repick merges into recipient stack");

            seed(100004, 2, 0);
            try (Peer interrupted = new Peer("items-disconnect")) {
                interrupted.pickup(100004);
                interrupted.socket.setSoLinger(true, 0);
            }
            await(() -> players.size() == 2, "disconnect cleaned");
            // Either the request was rejected before commit, or disconnect returned its stack.
            require(onGround(100004), "disconnect conserves item");
            loser.pickup(100004); barrier(loser); synced(loser, 17);
            seed(100005, 4, 0);
            try (Peer committed = new Peer("items-committed")) {
                committed.pickup(100005); barrier(committed); synced(committed, 4);
            }
            await(() -> players.size() == 2 && onGround(100005), "committed pickup returned on disconnect");
            loser.pickup(100005); barrier(loser); synced(loser, 21);
            // Overflow on the server must leave the ground and the inventory unchanged.
            synchronized (world) { winner.player().getInventory().addEtc(coins(100006, Integer.MAX_VALUE, 0)); }
            seed(100007, 1, 0); winner.pickup(100007); barrier(winner);
            require(serverGold(winner) == Integer.MAX_VALUE && onGround(100007), "server overflow rejection is atomic");
            require(first.failure == null && second.failure == null, "both socket readers alive");
        }
        System.out.println("PASS: real two-client item protocol, race, spoofed picker ID, repeat/far pickup, visibility/reentry, stack merge, death/drop/repick, disconnect, overflow");
    }
    public static void main(String[] args) throws Exception {
        int status = 1;
        try {
            server = new JavaTestServer(0); world = field("vm"); ground = field("items"); players = field("players");
            port = ((ServerSocket) field("ss")).getLocalPort();
            Thread runner = new Thread(server::run, "item-check-server"); runner.setDaemon(true); runner.start();
            System.out.println("Isolated item server port: " + port);
            if (args.length > 0 && args[0].equals("fixture")) {
                seed(200001, 7, 10); seed(200002, 5, 20); seed(200003, 3, 120);
                runner.join();
            } else { scenarios(); status = 0; }
        } catch (Throwable ex) { ex.printStackTrace(); }
        finally { System.exit(status); } // This check owns the entire isolated JVM/server.
    }
}
