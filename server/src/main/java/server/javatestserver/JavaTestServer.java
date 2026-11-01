package server.javatestserver;

import common.MapFragment;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Point;
import java.util.Iterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import common.HMapMessage;
import common.items.Item;
import common.Message;
import common.messages.MessageType;
import common.MoveMessage;
import common.OtherMessage;
import common.WanderingServerTime;
import common.items.Etc;
import common.messages.PickupEtcItem;

/**
 * Главный класс тестового ява сервера блуждающей же.
 */
public class JavaTestServer implements AutoCloseable {

    private final ServerSocket ss;
    private volatile boolean stopping;
    private Thread serverThread;
    private final BlockingQueue<SocketProcessor> clientQueue = new LinkedBlockingQueue<SocketProcessor>();
    final WorldState world = new WorldState();
    final MessageService messages = new MessageService(world, clientQueue);
    final CombatController combat;
    final DeathController deaths;
    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
    public static final long serverStartTime = System.currentTimeMillis();
    private static final int port = 45000;
    private final TimeSyncTask ts;
    // Visibility uses the same world monitor as commands, tasks and item transfers.
    private final VisibleManager vm;

    VisibleManager visibility() { return vm; }

    public JavaTestServer(int port) throws IOException {
        ss = new ServerSocket(port);
        vm = new VisibleManager(world, messages);
        combat = new CombatController(world, executor, messages, vm);
        deaths = combat.deaths;
        ts = new TimeSyncTask();

        // Classes what server use also whant to know server start time.
        WanderingServerTime.getInstance().setServerTime(serverStartTime);
    }

    /**
     * Servers main loop.
     */
    void run() {
        serverThread = Thread.currentThread();

        MonsterController c = new MonsterController(world, vm, messages, combat, 20 * 1000);
        c.setScheduledFuture(executor.scheduleAtFixedRate(c, 0L, 100L, TimeUnit.MILLISECONDS));
        TowerController t = new TowerController(world, combat);
        t.setScheduledFuture(executor.scheduleAtFixedRate(t, 0L, 100L, TimeUnit.MILLISECONDS));
        executor.scheduleAtFixedRate(vm, 0L, 100L, TimeUnit.MILLISECONDS);
        GeoDataController gdController = new GeoDataController(world);
        gdController.setScheduledFuture(executor.scheduleAtFixedRate(gdController, 0L, 200L, TimeUnit.MILLISECONDS));

        try {
            while (!stopping) {
                Socket s = getNewConnection();

                if (serverThread.isInterrupted()) {
                    break;
                } else if (s != null) {
                    try {
                        final SocketProcessor processor = new SocketProcessor(s);
                        final Thread thread = new Thread(processor);
                        thread.setDaemon(true);
                        synchronized (world) {
                            if (stopping) processor.close();
                            else { clientQueue.offer(processor); thread.start(); }
                        }
                    } catch (IOException ignored) {
                        try { s.close(); } catch (IOException closeFailure) { }
                    }
                }
            }

        } finally { close(); }
    }

    private Socket getNewConnection() {
        try { return ss.accept(); }
        catch (IOException failure) {
            if (!stopping) throw new UncheckedIOException(failure);
            return null;
        }
    }

    int localPort() { return ss.getLocalPort(); }

    @Override public void close() {
        synchronized (world) {
            if (stopping) return;
            stopping = true;
            for (SocketProcessor processor : clientQueue) processor.close();
            ts.cancel();
            combat.cancelAll();
            executor.shutdownNow();
            try { ss.close(); } catch (IOException ignored) { }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int requestedPort = args.length == 0 ? port : Integer.parseInt(args[0]);
        JavaTestServer server = new JavaTestServer(requestedPort);
        System.out.println("Server starts at port: " + server.ss.getLocalPort());
        server.run();
    }

    private class TimeSyncTask extends TimerTask {

        public Timer timer;

        public TimeSyncTask() {
            timer = new Timer();
            timer.schedule(this, 0, 10000);
        }

        @Override
        public void run() {
            messages.sendToAll(new String[]{"(timesync "
                        + (System.currentTimeMillis() - serverStartTime) + ")"});
        }

        @Override
        public boolean cancel() {
            boolean tmp = super.cancel();
            timer.cancel();
            return tmp;
        }
    }

    private class SocketProcessor implements Runnable, MessageService.Connection {

        Socket s;
        private boolean helloReceived = false;
        private boolean nickReceived = false;
        private volatile boolean helloSended = false;
        private volatile Player self = null;
        private volatile boolean closed;
        private Thread senderThread;
        private final LinkedBlockingQueue<Message> outbound = new LinkedBlockingQueue<Message>();
        private ObjectOutputStream oos;

        SocketProcessor(Socket socketParam) throws IOException {
            s = socketParam;
            oos = new ObjectOutputStream(s.getOutputStream());
        }

        @Override
        public void run() {
            try {
                process();
            } finally {
                close();
            }
        }

        private void process() {
            PacketSender sender = new PacketSender();
            senderThread = new Thread(sender);
            senderThread.setDaemon(true);
            senderThread.start();

            Message message;
            ObjectInputStream ois = null;

            try {
                ois = new ObjectInputStream(s.getInputStream());
            } catch (IOException ex) {
                return;
            }

            while (!s.isClosed()) {

                String line = null;
                try {
                    //line = br.readLine();
                    message = (Message) ois.readObject();

                    System.out.println(s.getInetAddress() + " --> " + message);

                    if (message.getType() == MessageType.OTHER) {

                        line = ((OtherMessage) message).getMessage();

                    } else if (message.getType() == MessageType.PICKUPETCITEM) {

                        pickup((PickupEtcItem) message);
                        continue;

                    } else {

                        System.err.println("Unknown message type.");
                        return;

                    }

                } catch (EOFException e) {
                    // Closing the client stream is a normal disconnect.
                    return;
                } catch (IOException e) {
                    if (!closed) {
                        System.err.println("Client disconnected: " + e.getMessage());
                    }
                    return;
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                    return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }

                if (line == null) {
                    close();
                    return;
                }

                try {
                    command(line);
                } catch (RuntimeException rejected) {
                    System.err.println("Rejected client command: " + rejected);
                    return;
                }
            }
        }

        private void command(String line) {
            synchronized (world) {
                if (closed) return;
                String[] pieces;
                if (line.length() > 0) {
                    //System.out.println(s.getInetAddress() + " --> " + line);

                    if (line.startsWith("(")) {
                        line = line.substring(1, line.length() - 1); // remove ( )
                    }

                    pieces = line.split(" ", 2);
                    if (helloReceived) {
                        if (nickReceived) {
                            if ("move".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Move message processing">
                                Point selfCurPos = self.getCurPos();
                                line = line.substring("move".length() + 1, line.length());
                                pieces = line.split(" ");
                                self.move(selfCurPos.x, selfCurPos.y, Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1]), (System.currentTimeMillis() - serverStartTime));
                                Message m = new MoveMessage(self.getID(),
                                                            WanderingServerTime.getInstance().getTimeSinceStart(),
                                                            selfCurPos.x,
                                                            selfCurPos.y,
                                                            Integer.parseInt(pieces[0]),
                                                            Integer.parseInt(pieces[1]));
                                messages.sendFromUnit(m, self);
                                messages.sendTo(m, self);
                                // </editor-fold>
                            } else if ("message".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Message message processing">
                                line = line.substring("message".length() + 1, line.length());
                                line = line.substring(1, line.length() - 1);
                                self.setText(line);
                                messages.sendToAll(new String[]{"(message " + self.getID() + " \"" + line + "\")"}, self.getID());
                                // </editor-fold>
                            } else if ("bolt".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Bolt message processing">
                                line = line.substring("bolt".length() + 1, line.length());
                                combat.attack(self, Long.parseLong(line), false);

                                // </editor-fold>
                            } else if ("attack".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Attack message processing">
                                line = line.substring("attack".length() + 1, line.length());
                                pieces = line.split(" ");

                                combat.attack(self, Long.parseLong(pieces[0]), true);
                                // </editor-fold>
                            } else if ("tower".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Tower processing">
                                pieces = line.split(" ");

                                /* Создадим новую башню и добавим её в список
                                 * башен.
                                 */
                                Tower t = new Tower(world.nextId(), "Tower", 200.0, 10, 1000L, Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]));
                                world.towers.add(t);
                                world.units.add(t);
                                // </editor-fold>
                            } else if ("getmapfragm".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Map fragment request processing">
                                pieces = line.split(" ");

                                int idx = Integer.parseInt(pieces[1]);
                                int idy = Integer.parseInt(pieces[2]);
                                boolean found = false;
                                MapFragment mapFrag = null;
                                for (MapFragment f: world.mapfragments) {
                                    if (idx == f.getIdx() && idy == f.getIdy()) {
                                        found = true;
                                        mapFrag = f;
                                        break;
                                    }
                                }
                                if (!found) {
                                    mapFrag = new MapFragment(idx,
                                                              idy,
                                                              MapFragment.create2DHMap());
                                    world.mapfragments.add(mapFrag);
                                }
                                messages.sendTo(new HMapMessage(mapFrag.getHmap(), idx, idy), self);

                                Monster mon = new Monster(world.nextId(), "Monster", 100, idx * MapFragment.getHeight() + 100, idy * MapFragment.getWidth() + 100, 0.5);
                                System.out.println("New mon spawned in " + (idx * MapFragment.getHeight() + 100)
                                                   + ", "
                                                   + (idy * MapFragment.getWidth() + 100));
                                mon.setSpriteSetName("poring");
                                world.monsters.add(mon);
                                world.units.add(mon);
                                // </editor-fold>
                            }
                        } else if ("nick".equals(pieces[0])) {
                            // <editor-fold defaultstate="collapsed" desc="Nick message processing">
                            Point selfCurPos;

                            if (closed) return;
                            nickReceived = true;
                            pieces = line.split(" ", 2);
                            pieces[1] = pieces[1].substring(1, pieces[1].length() - 1);
                            common.PlayerName.requireValid(pieces[1]);
                            self = new Player(world.nextId(), pieces[1], 100, 0, 0, 0.07);
                            selfCurPos = self.getCurPos();
                            if ("localhost".equals(s.getInetAddress().getHostName())) {
                                self.setSpriteSetName("peasant");
                            } else {
                                self.setSpriteSetName("peon");
                            }
                            addMessage("(hello "
                                    + self.getID() + " "
                                    + self.getHitPoints() + " "
                                    + 0.07 + " "
                                    + selfCurPos.x + " "
                                    + selfCurPos.y + " "
                                    + (System.currentTimeMillis() - serverStartTime) + " "
                                    + "\"" + self.getNick() + "\"" + " "
                                    + "\"SOUTH\"" + " "
                                    + "\"" + self.getSpriteSetName() + "\"" + ")");
                            helloSended = true;
                            world.players.add(self);
                            world.units.add(self);
                            System.out.println("Welcome to WD Java Test Server. Online: " + world.players.size() + " players (with you).");
                            // </editor-fold>
                        }
                    } else if ("hello".equals(pieces[0])) {
                        helloReceived = true;
                    }
                }
            }
        }

        private void pickup(PickupEtcItem request) {
            synchronized (world) {
                if (closed || !loggedIn() || self == null || !world.players.contains(self) || self.dead()) return;
                Etc item = null;
                for (Item candidate : world.items) {
                    if (candidate.getID() == request.getItemId() && candidate instanceof Etc etc) {
                        item = etc;
                        break;
                    }
                }
                if (item == null || self.getCurPos().distance(item.getX(), item.getY()) > 50.0) return;
                synchronized (self.getInventory()) {
                    try {
                        self.getInventory().validateAdd(item);
                    } catch (common.InventoryException rejected) {
                        return;
                    }
                    if (!world.items.remove(item)) return;
                    Etc stack = self.getInventory().addEtc(item);
                    // Delta quantity, canonical inventory stack ID (may differ from ground ID).
                    messages.sendTo(new common.messages.InventoryAddEtcItem(stack.getID(), item.getCount(), stack.getType()), self);
                }
                vm.removeItem(item);
                messages.sendTo(new PickupEtcItem(self.getID(), item.getID()), self);
            }
        }

        public void close() {
            synchronized (world) {
                if (closed) return;
                closed = true;
                helloSended = false;
                clientQueue.remove(this);
                if (self != null) {
                    // A committed pickup survives a simultaneous disconnect as a world drop.
                    deaths.dropGold(self, false);
                    combat.cancelFor(self.getID());
                    world.players.remove(self);
                    world.units.remove(self);
                    vm.removePlayer(self);
                }
            }
            if (senderThread != null) senderThread.interrupt();
            try { s.close(); } catch (IOException ignored) { }
        }

        public Player getSelfPlayer() {
            return self;
        }

        public boolean loggedIn() {
            return helloSended;
        }

        public void addMessage(String s) {
            outbound.offer(new OtherMessage(s));
        }

        public void addMessage(Message m) {
            outbound.offer(m);
        }

        private class PacketSender implements Runnable {

            private LinkedBlockingQueue<Message> messagesCopy = new LinkedBlockingQueue<Message>();

            @Override
            public void run() {
                Message message = null;

                try {
                    while (!closed) {
                        if (outbound.size() > 0) {
                            for (Iterator<Message> i = outbound.iterator(); i.hasNext();) {
                                message = i.next();
                                i.remove();
                                System.out.println(s.getInetAddress() + " <-- " + message);
                                messagesCopy.add(message);
                            }
                            oos.writeObject(messagesCopy);
                            messagesCopy.clear();
                            oos.reset();
                        }
                        Thread.sleep(50L);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (IOException ex) {
                    System.err.println(message);
                    if (self != null) {
                        System.err.println("Sendin packet to player " + self.getID() + " (" + self.getNick() + ") failed.");
                    } else {
                        System.err.println("Omg self player is null Sending packed failed also.");
                    }
                    ex.printStackTrace();
                } finally {
                    close();
                }
            }
        }
    }

}
