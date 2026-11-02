package server.javatestserver;

import common.BoltMessage;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.awt.Point;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;

import common.HMapMessage;
import common.HitMessage;
import common.Message;
import common.messages.MessageType;
import common.MoveMessage;
import common.Movement;
import common.OtherMessage;
import common.WanderingServerTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import newcommon.items.Etc;
import newcommon.items.Items;
import common.messages.PickupEtcItem;
import server.javatestserver.GeoDataController;
import server.javatestserver.JTSUnit;
import server.javatestserver.MapFragment;
import server.javatestserver.Monster;
import server.javatestserver.Player;
import server.javatestserver.VisibleManager;
import server.javatestserver.items.ServerEtc;

/**
 * Главный класс тестового ява сервера блуждающей же.
 */
public class JavaTestServer {

    private static JavaTestServer jts = null;

    static JavaTestServer getInstance() {
        if (jts == null) {
            try {
                jts = new JavaTestServer(port);
            } catch (IOException ex) {
                Logger.getLogger(JavaTestServer.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
        }

        return jts;
    }

    private ServerSocket ss;
    private Thread serverThread;
    BlockingQueue<SocketProcessor> clientQueue = new LinkedBlockingQueue<SocketProcessor>();
    private long curPlayerID = 1;
    ArrayList<Long> playerIDs = new ArrayList<Long>();
    private final LinkedBlockingQueue<JTSUnit> units = new LinkedBlockingQueue<JTSUnit>();
    private final LinkedBlockingQueue<Player> players = new LinkedBlockingQueue<Player>();
    private final LinkedBlockingQueue<Monster> monsters = new LinkedBlockingQueue<Monster>();
    private final LinkedBlockingQueue<Tower> towers = new LinkedBlockingQueue<Tower>();
    /**
     * Etc items layed on the groud.
     */
    private final LinkedBlockingQueue<ServerEtc> etcItems = new LinkedBlockingQueue<ServerEtc>();
    private final ArrayList<MapFragment> mapfragments = new ArrayList<MapFragment>();
    private final int tdMonsterLossLimit = 100;
    private int tdMonsterLoss = 0;
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
    public static final long serverStartTime = System.currentTimeMillis();
    private static final int port = 45000;
    private TimeSyncTask ts;
    private VisibleManager vm;

    public JavaTestServer(int port) throws IOException {
        ss = new ServerSocket(port);
        ts = new TimeSyncTask();

        // Classes what server use also whant to know server start time.
        WanderingServerTime.getInstance().setServerTime(serverStartTime);
    }

    /**
     * Servers main loop.
     */
    void run() {
        serverThread = Thread.currentThread();

        MonsterController c = new MonsterController(20 * 1000, 5);
        c.setScheduledFuture(executor.scheduleAtFixedRate(c, 0L, 100L, TimeUnit.MILLISECONDS));
        TowerController t = new TowerController();
        t.setScheduledFuture(executor.scheduleAtFixedRate(t, 0L, 100L, TimeUnit.MILLISECONDS));
        vm = new VisibleManager(players, towers, monsters, etcItems, this);
        executor.scheduleAtFixedRate(vm, 0L, 100L, TimeUnit.MILLISECONDS);
        GeoDataController gdController = new GeoDataController(players);
        gdController.setScheduledFuture(executor.scheduleAtFixedRate(gdController, 0L, 200L, TimeUnit.MILLISECONDS));

        while (true) {
            Socket s = getNewConnection();

            if (serverThread.isInterrupted()) {
                break;
            } else if (s != null) {
                try {
                    final SocketProcessor processor = new SocketProcessor(s);
                    final Thread thread = new Thread(processor);
                    thread.setDaemon(true);
                    thread.start();
                    clientQueue.offer(processor);
                } catch (IOException ignored) {
                }
            }
        }

        ts.cancel();
    }

    private Socket getNewConnection() {
        Socket cs = null;
        try {
            cs = ss.accept();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return cs;
    }

    public void sendFromUnit(String msg, JTSUnit unit) {
        for (SocketProcessor sp : clientQueue) {
            if (sp.loggedIn() && sp.getSelfPlayer().getVisibleUnitsList().contains(unit)) {
                sp.addMessage(msg);
            }
        }
    }

    /**
     * Sends message to all players what see unit.
     * @param msg message to send.
     * @param unit unit.
     */
    public void sendFromUnit(Message msg, JTSUnit unit) {
        for (SocketProcessor sp : clientQueue) {
            if (sp.loggedIn() && sp.getSelfPlayer().getVisibleUnitsList().contains(unit)) {
                sp.addMessage(msg);
            }
        }
    }

    public void sendTo(String msg, Player player) {
        for (SocketProcessor sp : clientQueue) {
            if (sp.loggedIn() && sp.getSelfPlayer().equals(player)) {
                sp.addMessage(msg);
                return;
            }
        }
    }

    /**
     * Отправляет сообщение игроку.
     * @param msg сообщение.
     * @param player игрок.
     */
    public void sendTo(Message msg, Player player) {
        for (SocketProcessor sp : clientQueue) {
            if (sp.loggedIn() && sp.getSelfPlayer().equals(player)) {
                sp.addMessage(msg);
                return;
            }
        }

        System.err.println("Message processor for player " + player + " was not found or player not logged in.");
        System.err.println("Message " + msg + " was not send.");
    }

    public void sendToAll(String[] msgs) {
        for (String msg : msgs) {
            for (SocketProcessor sp : clientQueue) {
                if (sp.loggedIn()) {
                    sp.addMessage(msg);
                }
            }
        }
    }

    public void sendToAll(Message[] msgs) {
        for (Message msg : msgs) {
            for (SocketProcessor sp : clientQueue) {
                if (sp.loggedIn()) {
                    sp.addMessage(msg);
                }
            }
        }
    }

    public void sendToAll(String[] msgs, long excID) {
        for (String msg : msgs) {
            for (SocketProcessor sp : clientQueue) {
                if (sp.getSelfPlayer() != null && sp.getSelfPlayer().getID() != excID && sp.loggedIn()) {
                    sp.addMessage(msg);
                }
            }
        }
    }

    public void sendToAll(Message[] msgs, long excID) {
        for (Message msg : msgs) {
            for (SocketProcessor sp : clientQueue) {
                if (sp.getSelfPlayer() != null && sp.getSelfPlayer().getID() != excID && sp.loggedIn()) {
                    sp.addMessage(msg);
                }
            }
        }
    }

    /**
     * Require player lock.
     */
    private Player getPlayer(long id) {
        for (Player p : players) {
            if (p.getID() == id) {
                return p;
            }
        }

        return null;
    }
    /**
     * Require lock all.
     */
    private JTSUnit getUnit(long id) {
        for (JTSUnit u : units) {
            if (u.getID() == id) {
                return u;
            }
        }

        return null;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Server starts at port: " + port);
        new JavaTestServer(port).run();
    }

    private class TimeSyncTask extends TimerTask {

        public Timer timer;

        public TimeSyncTask() {
            timer = new Timer();
            timer.schedule(this, 0, 10000);
        }

        @Override
        public void run() {
            sendToAll(new String[]{"(timesync "
                        + (System.currentTimeMillis() - serverStartTime) + ")"});
        }

        @Override
        public boolean cancel() {
            boolean tmp = super.cancel();
            timer.cancel();
            return tmp;
        }
    }

    private class SocketProcessor implements Runnable {

        Socket s;
        BufferedReader br;
        BufferedWriter bw;
        private boolean helloReceived = false;
        private boolean nickReceived = false;
        private boolean helloSended = false;
        private Player self = null;
        private LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();
        private ObjectOutputStream oos;

        SocketProcessor(Socket socketParam) throws IOException {
            s = socketParam;
            br = new BufferedReader(new InputStreamReader(s.getInputStream(),
                    "UTF-8"));
            bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),
                    "UTF-8"));
            oos = new ObjectOutputStream(s.getOutputStream());
        }

        @Override
        public void run() {
            PacketSender sender = new PacketSender();
            Thread senderThread = new Thread(sender);
            senderThread.setDaemon(true);
            senderThread.start();

            Message message;
            ObjectInputStream ois = null;

            try {
                ois = new ObjectInputStream(s.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(1);
            }

            while (!s.isClosed()) {

                String line = null;
                String[] pieces;

                try {
                    //line = br.readLine();
                    message = (Message) ois.readObject();

                    System.out.println(s.getInetAddress() + " --> " + message);

                    if (message.getType() == MessageType.OTHER) {

                        line = ((OtherMessage) message).getMessage();

                    } else if (message.getType() == MessageType.PICKUPETCITEM) {

                        System.out.println(s.getInetAddress() + " --> " + message);

                        // We ignore pickerId because we expect what all PICKUP
                        // messages sends from self player.
                        
                        PickupEtcItem tmpMsg = ((PickupEtcItem) message);
                        ServerEtc tmpItem = null;
                        for (ServerEtc item : etcItems) {
                            if (item.getID() == tmpMsg.getItemId()) {
                                tmpItem = item;
                                break;
                            }
                        }
                        if (tmpItem != null) {

                            // We can pickup this item.
                            // TODO lol may be some check if we actually can pickup?

                            etcItems.remove(tmpItem);
                            sendToAll(new Message[] {new PickupEtcItem(self.getID(), tmpItem.getID())});
                            vm.removeEtcItem(tmpItem);
                            self.addEtc(tmpItem);
                            sendTo(tmpItem.getAddToInvenrotyMessage(), self);
                        } else {
                            System.err.println("Item " + tmpMsg.getItemId() + " not found!");
                        }

                        // Read next message from client.
                        continue;

                    } else {

                        System.err.println("Unknown message type.");
                        System.exit(1);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                    // Мы не возвращаем управление, потому что в строке
                    // таки может что-то быть?
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                    System.exit(1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }

                if (line == null) {
                    close();
                    return;
                }

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
                                sendFromUnit(m, self);
                                sendTo(m, self);
                                // </editor-fold>
                            } else if ("message".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Message message processing">
                                line = line.substring("message".length() + 1, line.length());
                                line = line.substring(1, line.length() - 1);
                                self.setText(line);
                                sendToAll(new String[]{"(message " + self.getID() + " \"" + line + "\")"}, self.getID());
                                // </editor-fold>
                            } else if ("bolt".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Bolt message processing">
                                line = line.substring("bolt".length() + 1, line.length());
                                Player target = getPlayer(Long.parseLong(line));
                                if (target != null) {
                                    NukeAction a = new NukeAction(new NukeBolt(self, target, System.currentTimeMillis() - serverStartTime));
                                    ScheduledFuture f = executor.scheduleAtFixedRate(a, 0L, 10L, TimeUnit.MILLISECONDS);
                                    a.setScheduledFuture(f);
                                }
                                sendToAll(new Message[] { new BoltMessage(self.getID(),
                                                                          target.getID(),
                                                                          System.currentTimeMillis() - serverStartTime)},
                                          self.getID());

                                // </editor-fold>
                            } else if ("attack".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Attack message processing">
                                line = line.substring("attack".length() + 1, line.length());
                                pieces = line.split(" ");

                                JTSUnit target = getUnit(Long.parseLong(pieces[0]));
                                long begTime = System.currentTimeMillis() - serverStartTime;

                                if (target != null) {
                                    NukeAction a = new NukeAction(new NukeBolt(self, target, begTime + self.getNukeAnimationDelay()));
                                    ScheduledFuture f = executor.scheduleAtFixedRate(a, self.getNukeAnimationDelay(), 10L, TimeUnit.MILLISECONDS);
                                    a.setScheduledFuture(f);
                                    sendToAll(new String[]{"(attack "
                                                + self.getID() + " "
                                                + target.getID() + " "
                                                + begTime + ")"});
                                } else {
                                    System.err.println("Taget not found: " + Long.parseLong(pieces[0]));
                                }
                                // </editor-fold>
                            } else if ("tower".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Tower processing">
                                pieces = line.split(" ");

                                /* Создадим новую башню и добавим её в список
                                 * башен.
                                 */
                                Tower t = new Tower(curPlayerID++, "Tower", 200.0, 10, 1000L, Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]));
                                towers.add(t);
                                units.add(t);
                                // </editor-fold>
                            } else if ("getmapfragm".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Map fragment request processing">
                                pieces = line.split(" ");

                                int idx = Integer.parseInt(pieces[1]);
                                int idy = Integer.parseInt(pieces[2]);
                                boolean found = false;
                                MapFragment mapFrag = null;
                                for (MapFragment f: mapfragments) {
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
                                    mapfragments.add(mapFrag);
                                }
                                sendTo(new HMapMessage(mapFrag.getHmap(), idx, idy), self);

                                Monster mon = new Monster(curPlayerID++, "Monster", 100, idx * MapFragment.getHeight() + 100, idy * MapFragment.getWidth() + 100, 0.5);
                                System.out.println("New mon spawned in " + (idx * MapFragment.getHeight() + 100)
                                                   + ", "
                                                   + (idy * MapFragment.getWidth() + 100));
                                mon.setSpriteSetName("poring");
                                monsters.add(mon);
                                units.add(mon);
                                // </editor-fold>
                            }
                        } else if ("nick".equals(pieces[0])) {
                            // <editor-fold defaultstate="collapsed" desc="Nick message processing">
                            Point selfCurPos;

                            nickReceived = true;
                            pieces = line.split(" ", 2);
                            pieces[1] = pieces[1].substring(1, pieces[1].length() - 1);
                            self = new Player(curPlayerID++, pieces[1], 100, 0, 0, 0.07);
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
                            players.add(self);
                            units.add(self);
                            System.out.println("Welcome to WD Java Test Server. Online: " + players.size() + " players (with you).");
                            // </editor-fold>
                        }
                    } else if ("hello".equals(pieces[0])) {
                        helloReceived = true;
                    }
                }
            }
        }

        public synchronized void close() {
            clientQueue.remove(this);
            if (!s.isClosed()) {
                try {
                    s.close();
                } catch (IOException ignored) {
                }
            } else {
                return;
            }
            if (self != null) {
                vm.removePlayer(self);
                players.remove(self);
                units.remove(self);
                self = null;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            close();
        }

        public Player getSelfPlayer() {
            return self;
        }

        public boolean loggedIn() {
            return helloSended;
        }

        public void addMessage(String s) {
            try {
                messages.put(new OtherMessage(s));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        public void addMessage(Message m) {
            //System.out.println("Message " + m + " added.");
            try {
                messages.put(m);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        private class PacketSender implements Runnable {

            private LinkedBlockingQueue<Message> messagesCopy = new LinkedBlockingQueue<Message>();

            @Override
            public void run() {
                Message message = null;

                try {
                    while (true) {
                        if (messages.size() > 0) {
                            for (Iterator<Message> i = messages.iterator(); i.hasNext();) {
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
                    ex.printStackTrace();
                } catch (IOException ex) {
                    System.err.println(message);
                    if (self != null) {
                        System.err.println("Sendin packet to player " + self.getID() + " (" + self.getNick() + ") failed.");
                    } else {
                        System.err.println("Omg self player is null Sending packed failed also.");
                    }
                    ex.printStackTrace();
                }
            }
        }
    }

    private class NukeAction implements Runnable {

        private NukeBolt bolt;
        private ScheduledFuture future = null;
        private boolean canceled = false;

        public NukeAction(NukeBolt bolt) {
            this.bolt = bolt;
        }

        @Override
        public void run() {
            if (!canceled) {
                if (!bolt.isFlight()) {
                    try {
                        JTSUnit attacker = bolt.getAttacker();
                        JTSUnit target = bolt.getTarget();

                        target.doHit(attacker.getDamage());
                        sendFromUnit(new HitMessage(attacker.getID(),
                                                    target.getID(),
                                                    attacker.getDamage()),
                                     target);
                        if (target.dead()) {
                            if (monsters.contains(target)) {
                                monsters.remove(target);
                                units.remove(target);
                                vm.killMonster((Monster) target);
                            } else if (players.contains(target)) {
                                Player p = (Player) target;
                                ArrayList<Etc> tmpEtcs = p.getEtc(Items.GOLD);
                                if (!tmpEtcs.isEmpty()) {
                                    ServerEtc gold = (ServerEtc) tmpEtcs.get(0);
                                    p.removeEtc(gold);
                                    sendTo(gold.getRemoveFromInventoryMessage(), p);
                                    gold.setX(p.getCurPos().x);
                                    gold.setY(p.getCurPos().y);
                                    etcItems.add(gold);
                                }
                                p.restoreHitPoints();
                                p.teleportToSpawn();
                                sendToAll(new String[] { "(teleport " + p.getID() + " " + p.getCurPos().x + " " + p.getCurPos().y + ")" });
                                vm.killPlayer(p);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }

                    this.cancel();
                }
            }
        }

        public void setScheduledFuture(ScheduledFuture future) {
            this.future = future;
        }

        public void cancel() {
            if (future != null) {
                future.cancel(true);
                canceled = true;
            }
        }
    }

    private class MonsterController implements Runnable {

        /**
         * Absolute delay between spawn monsters in milliseconds.
         */
        private final long spawnDelay;

        /**
         * Current monster count.
         */
        private int monsterCount;

        /**
         * Possible monster speeds.
         */
        private final double[] speeds = new double[] {0.08, 0.07, 0.06};

        /**
         * Count of monsters losses in current wave.
         */
        private int curWaveMonLoss;

        /**
         * Current monster strength multiplyer.
         */
        private int monStrMult;

        /**
         * Current monster count summand.
         */
        private int monCountAdd;

        /**
         * Current wave number since server start.
         */
        private long wave;

        private Random rand = new Random();

        private ScheduledFuture future = null;

        private boolean canceled = false;

        private final LinkedBlockingQueue<Monster> tdMonsters = new LinkedBlockingQueue<Monster>();

        public MonsterController(long spawnDelay, int monsterCount) {
            this.spawnDelay = spawnDelay;
            //this.monsterCount = monsterCount;
            this.monsterCount = 0;
            wave = WanderingServerTime.getInstance().getTimeSinceStart() / spawnDelay;
            curWaveMonLoss = 0;
            monStrMult = 0;
            monCountAdd = 0;
        }

        @Override
        public void run() {
            if (!canceled) {
                long curWave = WanderingServerTime.getInstance().getTimeSinceStart() / spawnDelay;
                Monster m;
                Point beg;

                // For monster current position temporary storage purpose.
                Point mCurPos;

                // Monsters destenation point.
                Point end = new Point(705, 755);

                // We need to start a new wave of monsters.
                if (curWave > wave) {
                    wave = curWave;
                    tdMonsterLoss += curWaveMonLoss;

                    // If we reach the monster loss limit we remove all towers
                    // and rest game parameters to initial values.
                    if (tdMonsterLoss >= tdMonsterLossLimit) {
                        tdMonsterLoss = 0;
                        curWaveMonLoss = 0;
                        monStrMult = 0;
                        monCountAdd = 0;

                        Tower t;
                        for (Iterator<Tower> li = towers.iterator(); li.hasNext();) {
                            t = li.next();
                            vm.removeTower(t);
                            units.remove(t);
                            li.remove();
                        }
                    }

                    // If we have no monster loss previous wave (we kill all
                    // monsters) we monsters stronger and incrase monsters count.
                    if (curWaveMonLoss == 0) {
                        monStrMult++;
                        monCountAdd += 5;
                    }

                    // Update tower defence game status on clients.
                    sendToAll(new String[]{"(monsterloss "
                                + tdMonsterLoss + " "
                                + tdMonsterLossLimit + " "
                                + monStrMult + ")"});

                    curWaveMonLoss = 0;

                    // Spawn new monsters.
                    for (int i = 0; i < monsterCount + monCountAdd; i++) {
                        beg = new Point(544 + rand.nextInt(287), 0 + rand.nextInt(64));
                        m = new Monster(curPlayerID++, "Monster", 20 * monStrMult, beg.x, beg.y, speeds[rand.nextInt(2)]);
                        m.setSpriteSetName("poring");
                        tdMonsters.add(m);
                        monsters.add(m);
                        units.add(m);
                        m.move(beg.x, beg.y, end.x, end.y, WanderingServerTime.getInstance().getTimeSinceStart());
                    }
                }

                // Process current wave of monsters.

                boolean isDead;
                boolean isStop;

                for (Iterator<Monster> li = tdMonsters.iterator(); li.hasNext();) {
                    m = li.next();
                    isDead = m.dead();
                    isStop = !m.isMove();

                    if (isStop && !isDead) {
                        curWaveMonLoss++;
                        sendToAll(new String[]{"(monsterloss "
                                    + (tdMonsterLoss + curWaveMonLoss) + " "
                                    + tdMonsterLossLimit + " "
                                    + monStrMult + ")"});
                        vm.killMonster(m);
                        units.remove(m);
                        monsters.remove(m);
                        li.remove();
                    }

                    if (isDead) {
                        mCurPos = m.getCurPos();

                        // Drop coins.
                        ServerEtc coins = new ServerEtc(curPlayerID++, Items.GOLD.getCustomName(), 1 + rand.nextInt(99), Items.GOLD);
                        coins.setX(mCurPos.x + (int) Math.pow(-1, rand.nextInt(2)) * 10);
                        coins.setY(mCurPos.y + (int) Math.pow(-1, rand.nextInt(2)) * 10);
                        etcItems.add(coins);

                        units.remove(m);
                        monsters.remove(m);
                        li.remove();
                    }
                }
            }
        }

        public void setScheduledFuture(ScheduledFuture future) {
            this.future = future;
        }

        public void cancel() {
            if (future != null) {
                future.cancel(true);
                canceled = true;
            }
        }
    }
}
