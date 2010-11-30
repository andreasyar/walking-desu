package server.javatestserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.ListIterator;
import java.awt.Point;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;

import common.Movement;
import common.WanderingServerTime;

/**
 * Главный класс тестового ява сервера блуждающей же.
 */
public class JavaTestServer {

    private ServerSocket ss;
    private Thread serverThread;
    BlockingQueue<SocketProcessor> clientQueue = new LinkedBlockingQueue<SocketProcessor>();
    private long curPlayerID = 1;
    ArrayList<Long> playerIDs = new ArrayList<Long>();
    private final ArrayList<JTSUnit> units = new ArrayList<JTSUnit>();
    private final ArrayList<Player> players = new ArrayList<Player>();
    private final ArrayList<Monster> monsters = new ArrayList<Monster>();
    private final ArrayList<Tower> towers = new ArrayList<Tower>();
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
        vm = new VisibleManager(players, towers, monsters, this);
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
                sp.send(msg);
                sp.addMessage(msg);
            }
        }
    }

    public void sendTo(String msg, Player player) {
        for (SocketProcessor sp : clientQueue) {
            if (sp.loggedIn() && sp.getSelfPlayer().equals(player)) {
                sp.send(msg);
                sp.addMessage(msg);
                return;
            }
        }
    }

    public void sendTo(JTSMessage msg, Player player) {
        for (SocketProcessor sp : clientQueue) {
            if (sp.loggedIn() && sp.getSelfPlayer().equals(player)) {
                sp.addMessage(msg);
                return;
            }
        }
    }

    public void sendToAll(String[] msgs) {
        for (String msg : msgs) {
            for (SocketProcessor sp : clientQueue) {
                if (sp.loggedIn()) {
                    sp.send(msg);
                    sp.addMessage(msg);
                }
            }
        }
    }

    public void sendToAll(JTSMessage[] msgs) {
        for (JTSMessage msg : msgs) {
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
                    sp.send(msg);
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
        private LinkedBlockingQueue<JTSMessage> messages = new LinkedBlockingQueue<JTSMessage>();
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

            while (!s.isClosed()) {
                String line = null;
                String[] pieces;

                try {
                    line = br.readLine();
                } catch (IOException e) {
                    close();
                    // Мы не возвращаем управление, потому что в строке
                    // таки может что-то быть?
                }

                if (line == null) {
                    close();
                    return;
                }

                if (line.length() > 0) {
                    System.out.println(s.getInetAddress() + " --> " + line);

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
                                sendFromUnit("(move "
                                        + self.getID() + " "
                                        + (System.currentTimeMillis() - serverStartTime) + " "
                                        + selfCurPos.x + " "
                                        + selfCurPos.y + " "
                                        + pieces[0] + " "
                                        + pieces[1] + ")", self);
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
                                JTSLocks.lockPlayers();
                                Player target = getPlayer(Long.parseLong(line));
                                JTSLocks.unlockPlayers();
                                if (target != null) {
                                    NukeAction a = new NukeAction(new NukeBolt(self, target, System.currentTimeMillis() - serverStartTime));
                                    ScheduledFuture f = executor.scheduleAtFixedRate(a, 0L, 10L, TimeUnit.MILLISECONDS);
                                    a.setScheduledFuture(f);
                                }
                                sendToAll(new String[]{"(bolt "
                                            + self.getID() + " "
                                            + target.getID() + " "
                                            + (System.currentTimeMillis() - serverStartTime) + ")"}, self.getID());

                                // </editor-fold>
                            } else if ("attack".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Attack message processing">
                                line = line.substring("attack".length() + 1, line.length());
                                pieces = line.split(" ");

                                JTSLocks.lockAll();
                                JTSUnit target = getUnit(Long.parseLong(pieces[0]));
                                JTSLocks.unlockAll();
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
                                JTSLocks.lockAll();
                                towers.add(t);
                                units.add(t);
                                JTSLocks.unlockAll();
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
                                sendTo(new JTSMessage(JTSMessageTypes.HMAP,
                                           new ShortMapFragment(mapFrag.getHmap(), idx, idy)),
                                       self);

                                JTSLocks.lockAll();
                                Monster mon = new Monster(curPlayerID++, "Monster", 100, idx * MapFragment.getHeight() + 100, idy * MapFragment.getWidth() + 100, 0.5);
                                System.out.println("New mon spawned in " + (idx * MapFragment.getHeight() + 100)
                                                   + ", "
                                                   + (idy * MapFragment.getWidth() + 100));
                                mon.setSpriteSetName("poring");
                                monsters.add(mon);
                                units.add(mon);
                                JTSLocks.unlockAll();
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
                            JTSLocks.lockAll();
                            players.add(self);
                            units.add(self);
                            System.out.println("Welcome to WD Java Test Server. Online: " + players.size() + " players (with you).");
                            JTSLocks.unlockAll();
                            // </editor-fold>
                        }
                    } else if ("hello".equals(pieces[0])) {
                        helloReceived = true;
                    }
                }
            }
        }

        public synchronized void send(String line) {
            /*if (!line.startsWith("(")) {
                line = "(" + line + ")";
            }
            System.out.println(s.getInetAddress() + " <-- " + line);
            try {
                bw.write(line);
                bw.write("\n");
                bw.flush();
            } catch (IOException e) {
                close();
            }*/
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
                JTSLocks.lockAll();
                vm.removePlayer(self);
                players.remove(self);
                units.remove(self);
                self = null;
                JTSLocks.unlockAll();
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
                messages.put(new JTSMessage(JTSMessageTypes.OTHER, s));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        public void addMessage(JTSMessage m) {
            try {
                messages.put(m);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        private class PacketSender implements Runnable {

            private LinkedBlockingQueue<JTSMessage> messagesCopy = new LinkedBlockingQueue<JTSMessage>();

            @Override
            public void run() {
                JTSMessage message;

                try {
                    while (true) {
                        if (messages.size() > 0) {
                            for (Iterator<JTSMessage> i = messages.iterator(); i.hasNext();) {
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
                    System.err.println("Sendin packet to player " + self.getID() + " (" + self.getNick() + ") failed.");
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
                    JTSLocks.lockAll();
                    try {
                        JTSUnit attacker = bolt.getAttacker(),
                                target = bolt.getTarget();

                        target.doHit(attacker.getDamage());
                        sendFromUnit("(hit "
                                     + attacker.getID() + " "
                                     + target.getID() + " "
                                     + attacker.getDamage() + ")", target);

                        if (target.dead()) {
                            if (monsters.contains(target)) {
                                monsters.remove(target);
                                units.remove(target);
                                vm.killMonster((Monster) target);
                            } else if (players.contains(target)) {
                                Player p = (Player) target;
                                p.restoreHitPoints();
                                p.teleportToSpawn();
                                vm.killPlayer(p);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }
                    JTSLocks.unlockAll();

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

        private long spawnDelay;
        private int monsterCount;
        private final double[] speeds = new double[]{0.08, 0.07, 0.06};
        private Random rand = new Random();
        private ScheduledFuture future = null;
        private boolean canceled = false;
        private int curWaveMonLoss;
        private int monStrMult;
        private int monCountAdd;
        private long step;

        public MonsterController(long spawnDelay, int monsterCount) {
            this.spawnDelay = spawnDelay;
            //this.monsterCount = monsterCount;
            this.monsterCount = 0;
            step = (System.currentTimeMillis() - JavaTestServer.serverStartTime) / spawnDelay;
            curWaveMonLoss = 0;
            monStrMult = 0;
            monCountAdd = 0;
        }

        @Override
        public void run() {
            if (!canceled) {
                JTSLocks.lockAll();

                long tmpStep = (System.currentTimeMillis() - JavaTestServer.serverStartTime) / spawnDelay;
                Monster m;
                Point beg;
                Point end = new Point(705, 755);

                if (tmpStep > step) {
                    step = tmpStep;
                    tdMonsterLoss += curWaveMonLoss;
                    if (tdMonsterLoss >= tdMonsterLossLimit) {
                        tdMonsterLoss = 0;
                        curWaveMonLoss = 0;
                        monStrMult = 1;
                        monCountAdd = 0;
                        Tower t;

                        for (ListIterator<Tower> li = towers.listIterator(); li.hasNext();) {
                            t = li.next();
                            vm.removeTower(t);
                            units.remove(t);
                            li.remove();
                        }
                    } else if (curWaveMonLoss == 0) {
                        monStrMult++;
                        monCountAdd += 5;
                    }
                    sendToAll(new String[]{"(monsterloss "
                                + tdMonsterLoss + " "
                                + tdMonsterLossLimit + " "
                                + monStrMult + ")"});
                    curWaveMonLoss = 0;
                    for (int i = 0; i < monsterCount + monCountAdd; i++) {
                        beg = new Point(544 + rand.nextInt(287), 0 + rand.nextInt(64));
                        m = new Monster(curPlayerID++, "Monster", 20 * monStrMult, beg.x, beg.y, speeds[rand.nextInt(2)]);
                        m.setSpriteSetName("poring");
                        monsters.add(m);
                        units.add(m);
                        m.move(beg.x, beg.y, end.x, end.y, System.currentTimeMillis() - JavaTestServer.serverStartTime);
                    }
                }

                boolean isDead;
                boolean isStop;

                for (ListIterator<Monster> li = monsters.listIterator(); li.hasNext();) {
                    m = li.next();
                    isDead = m.dead();
                    isStop = !m.isMove();

                    if (isStop && !isDead) {
                        curWaveMonLoss++;
                        sendToAll(new String[]{"(monsterloss "
                                    + (tdMonsterLoss + curWaveMonLoss) + " "
                                    + tdMonsterLossLimit + " "
                                    + monStrMult + ")"});
                    }
                    if (!isDead && isStop) {
                        vm.killMonster(m);
                        units.remove(m);
                        li.remove();
                    }
                }
            }
            JTSLocks.unlockAll();
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

    private class TowerController implements Runnable {

        private ScheduledFuture future = null;
        private boolean canceled = false;

        public TowerController() {
        }

        @Override
        public void run() {
            if (!canceled) {
                JTSLocks.lockTowers();

                for (Tower t : towers) {
                    if (!attackRandomMonster(t)) {
                        t.selectMonster(monsters);
                        attackRandomMonster(t);
                    }
                }

                JTSLocks.unlockTowers();
            }
        }

        private boolean attackRandomMonster(Tower t) {
            if (t.targetInRange()) {
                if (!t.reuse()) {
                    //t.getTarget().doHit(t.getDamage());
                    long begTime = System.currentTimeMillis() - JavaTestServer.serverStartTime;
                    t.setLastAttackTime(begTime);
                    NukeAction a = new NukeAction(new NukeBolt((JTSUnit) t, (JTSUnit) t.getTarget(), begTime));
                    ScheduledFuture f = executor.scheduleAtFixedRate(a, 0L, 10L, TimeUnit.MILLISECONDS);
                    a.setScheduledFuture(f);
                    sendToAll(new String[]{"(bolt " + t.getID() + " " + t.getTarget().getID() + " " + (begTime) + ")"});
                    return true;
                }

                return false;
            }

            return false;
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

class NukeBolt {

    private JTSUnit attacker;
    private JTSUnit target;
    private Movement mv;

    public NukeBolt(JTSUnit attacker, JTSUnit target, long begTime) {
        this.attacker = attacker;
        this.target = target;

        Point tmp = attacker.getCurPos();
        Point cur = target.getCurPos();
        mv = new Movement(tmp.x, tmp.y, 1.0);
        mv.move(tmp.x, tmp.y, cur.x, cur.y, begTime);
    }

    public boolean isFlight() {
        return mv.isMove();
    }

    public JTSUnit getAttacker() {
        return attacker;
    }

    public JTSUnit getTarget() {
        return target;
    }
}
