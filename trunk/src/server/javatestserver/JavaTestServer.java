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
import java.util.Iterator;
import java.awt.Point;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;

/**
 * Главный класс тестового ява сервера блуждающей же.
 */
public class JavaTestServer {

    private ServerSocket ss;
    private Thread serverThread;
    BlockingQueue<SocketProcessor> clientQueue = new LinkedBlockingQueue<SocketProcessor>();
    private long curPlayerID = 1;
    ArrayList<Long> playerIDs = new ArrayList<Long>();
    private final ArrayList<Player> players = new ArrayList<Player>();
    private final ArrayList<NPC> npcs = new ArrayList<NPC>();
    private final ArrayList<Monster> monsters = new ArrayList<Monster>();
    private final ArrayList<Tower> towers = new ArrayList<Tower>();
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
    }

    /**
     * Главный цикл тестового ява сервера.
     */
    void run() {
        serverThread = Thread.currentThread();

        MonsterController c = new MonsterController(20 * 1000, 5);
        c.setScheduledFuture(executor.scheduleAtFixedRate(c, 0L, 100L, TimeUnit.MILLISECONDS));
        TowerController t = new TowerController();
        t.setScheduledFuture(executor.scheduleAtFixedRate(t, 0L, 100L, TimeUnit.MILLISECONDS));
        vm = new VisibleManager(players, towers, monsters, this);
        executor.scheduleAtFixedRate(vm, 0L, 100L, TimeUnit.MILLISECONDS);

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

    /**
     * Ждет новых подключений к серверу. Возвращает новое подключение.
     */
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

    /**
     * Отправляет сообщение всем игрокам, у которых заданный юнит находится в
     * зоне видимости.
     * @param msg Сообщение
     * @param unit Юнит
     */
    public void sendFromUnit(String msg, Unit unit) {
        for (SocketProcessor sp : clientQueue) {
            if (sp.loggedIn() && sp.getSelfPlayer().getVisibleUnitsList().contains(unit)) {
                sp.send(msg);
            }
        }
    }

    /**
     * Отправляет сообщение заданному игроку.
     * @param msgs Сообщение.
     * @param player Игрок.
     */
    public void sendTo(String msg, Player player) {
        for (SocketProcessor sp : clientQueue) {
            if (sp.loggedIn() && sp.getSelfPlayer().equals(player)) {
                sp.send(msg);
                return;
            }
        }
    }

    /**
     * Отправляет заданные сообщения всем пользователям.
     * @param msgs Сообщения.
     */
    public void sendToAll(String[] msgs) {
        for (String msg : msgs) {
            for (SocketProcessor sp : clientQueue) {
                if (sp.loggedIn()) {
                    sp.send(msg);
                }
            }
        }
    }

    /**
     * Отправляет заданные сообщения всем пользователям, за исключем
     * пользователя с заданным ID.
     * @param msgs Сообщения.
     * @param excID ID исключаемого из рассылки игрока.
     */
    public void sendToAll(String[] msgs, long excID) {
        for (String msg : msgs) {
            for (SocketProcessor sp : clientQueue) {
                if (sp.getSelfPlayer() != null && sp.getSelfPlayer().getID() != excID && sp.loggedIn()) {
                    sp.send(msg);
                }
            }
        }
    }

    private Player getPlayer(long id) {
        Player p = null;

        synchronized (players) {
            for (Iterator<Player> li = players.iterator(); li.hasNext();) {
                p = li.next();
                if (p.getID() == id) {
                    return p;
                }
            }
        }

        return p;
    }

    public static void main(String[] args) throws IOException,
            InterruptedException {
        System.out.println("Server starts at port: " + port);
        new JavaTestServer(port).run();
    }

    private boolean willMeet(Player player1, Player player2, double range) {
        System.out.println("range = " + range);
        Point player1Pos = player1.getCurPos(),
            player2Pos = player2.getCurPos(),
            player1Dest = player1.getEnd(),
            player2Dest = player2.getEnd();
        long t0 = player1.isMove() ? player1.getBegTime() : System.currentTimeMillis() - serverStartTime;
        System.out.println("t0 = " + t0);
        long t0_ = player2.isMove() ? player2.getBegTime() : System.currentTimeMillis() - serverStartTime;
        System.out.println("t0_ = " + t0_);
        long tEnd = player1.isMove() ? player1.getEndTime() : t0;
        System.out.println("tEnd = " + tEnd);
        long tEnd_ = player2.isMove() ? player2.getEndTime() : t0_;
        System.out.println("tEnd_ = " + tEnd_);
        double x1 = player1Pos.x;
        System.out.println("x1 = " + x1);
        double y1 = player1Pos.y;
        System.out.println("y1 = " + y1);
        double x2 = player1Dest.x;
        System.out.println("x2 = " + x2);
        double y2 = player1Dest.y;
        System.out.println("y2 = " + y2);
        double v = player1.isMove() ? player1.getSpeed() : 0.0;
        System.out.println("v = " + v);
        double vX = (v == 0.0 || x1 == x2 && y1 == y2) ? 0.0 : v * (x2 - x1) / Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
        System.out.println("vX = " + vX);
        double vY = (v == 0.0 || x1 == x2 && y1 == y2) ? 0.0 : v * (y2 - y1) / Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
        System.out.println("vY = " + vY);
        double x1_ = player2Pos.x;
        System.out.println("x1_ = " + x1_);
        double y1_ = player2Pos.y;
        System.out.println("y1_ = " + y1_);
        double x2_ = player2Dest.x;
        System.out.println("x2_ = " + x2_);
        double y2_ = player2Dest.y;
        System.out.println("y2_ = " + y2_);
        double v_ = player2.isMove() ? player2.getSpeed() : 0.0;
        System.out.println("v_ = " + v_);
        double vX_ = (v_ == 0.0 || x1_ == x2_ && y1_ == y2_) ? 0.0 : (v_ * (x2_ - x1_)) / Math.sqrt(Math.pow(x2_ - x1_, 2.0) + Math.pow(y2_ - y1_, 2.0));
        System.out.println("vX_ = " + vX_);
        double vY_ = (v_ == 0.0 || x1_ == x2_ && y1_ == y2_) ? 0.0 : (v_ * (y2_ - y1_)) / Math.sqrt(Math.pow(x2_ - x1_, 2.0) + Math.pow(y2_ - y1_, 2.0));
        System.out.println("vY_ = " + vY_);
        double gamma = x1_ - vX_ * t0_ - x1 + vX * t0;
        System.out.println("gamma = " + gamma);
        double delta = y1_ - vY_ * t0_ - y1 + vY * t0;
        System.out.println("delta = " + delta);
        double a = Math.pow(vX_ - vX, 2.0) + Math.pow(vY_ - vY, 2.0);
        System.out.println("a = " + a);
        double b = 2 * ((vX_ - vX) * gamma + (vY_ - vY) * delta);
        System.out.println("b = " + b);
        double c = Math.pow(gamma, 2.0) + Math.pow(delta, 2.0) - Math.pow(range, 2.0);
        System.out.println("c = " + c);
        double D = Math.pow(b, 2.0) - 4.0 * a * c;
        System.out.println("D = " + D);
        if (D < 0) {
            System.out.println(player1.getID() + " will NOT meet with " + player2.getID());
            return false;
        } else {
            double t1 = (- b + Math.sqrt(D)) / (2.0 * a);
            System.out.println("t1 = " + t1);
            double t2 = (- b - Math.sqrt(D)) / (2.0 * a);
            System.out.println("t2 = " + t2);
            if (Math.min(t1, t2) <= Math.min(tEnd, tEnd_) && Math.max(t1, t2) >= Math.max(t0, t0_)) {
                System.out.println(player1.getID() + " will meet with " + player2.getID());
                return true;
            } else {
                System.out.println(player1.getID() + " will NOT meet with " + player2.getID());
                return false;
            }
        }
    }

    private class TimeSyncTask extends TimerTask {

        public Timer timer;

        public TimeSyncTask() {
            timer = new Timer();
            timer.schedule(this, 0, 10000);
        }

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

        SocketProcessor(Socket socketParam) throws IOException {
            s = socketParam;
            br = new BufferedReader(new InputStreamReader(s.getInputStream(),
                    "UTF-8"));
            bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),
                    "UTF-8"));
        }

        public void run() {
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
                                try {
                                    line = line.substring("move".length() + 1, line.length());
                                    pieces = line.split(" ");
                                    self.move((Point) selfCurPos.clone(), new Point(Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1])), (System.currentTimeMillis() - serverStartTime));
                                    //vm.update();
                                    sendFromUnit("(move "
                                            + self.getID() + " "
                                            + (System.currentTimeMillis() - serverStartTime) + " "
                                            + selfCurPos.x + " "
                                            + selfCurPos.y + " "
                                            + pieces[0] + " "
                                            + pieces[1] + ")", self);
                                    System.out.println("Move received.");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.exit(1);
                                }
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
                                sendToAll(new String[]{"(bolt " + self.getID() + " " + target.getID() + " " + (System.currentTimeMillis() - serverStartTime) + ")"}, self.getID());
                                // </editor-fold>
                            } else if ("tower".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Tower processing">
                                pieces = line.split(" ");

                                /* Создадим новую башню и добавим её в список
                                 * башен.
                                 */
                                Tower t = new Tower(curPlayerID++, "Tower", 200.0, 10, 1000L, Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]));
                                synchronized (towers) {
                                    towers.add(t);
                                }

                                /* Определим игроков, которые видят новую башню
                                 * и отправим им сообщение о новой башне.
                                 */
                                //vm.update();
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
                                self.setSpriteSetName("poring");
                            } else {
                                self.setSpriteSetName("desu");
                            }
                            send("(hello "
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
                            synchronized (players) {
                                players.add(self);
                                System.out.println("Welcome to WD Java Test Server. Online: " + players.size() + " players (with you).");
                            }

                            /* Определим игроков, которые видят нового игрока
                             * и отправим им сообщение о новом игроке.
                             */
                            //vm.update();
                            // </editor-fold>
                        }
                    } else if ("hello".equals(pieces[0])) {
                        helloReceived = true;
                    }
                }
            }
        }

        public synchronized void send(String line) {
            if (!line.startsWith("(")) {
                line = "(" + line + ")";
            }
            System.out.println(s.getInetAddress() + " <-- " + line);
            try {
                bw.write(line);
                bw.write("\n");
                bw.flush();
            } catch (IOException e) {
                close();
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
                synchronized (players) {
                    vm.removePlayer(self);
                    players.remove(self);
                }
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
    }

    private class NukeAction implements Runnable {

        private NukeBolt bolt;
        private ScheduledFuture future = null;
        private boolean canceled = false;

        public NukeAction(NukeBolt bolt) {
            this.bolt = bolt;
        }

        public void run() {
            if (!canceled) {
                if (!bolt.isFlight()) {
                    sendToAll(new String[]{"(hit "
                            + bolt.getAttacker().getID() + " "
                            + bolt.getTarget().getID() + " "
                            + ((Tower) bolt.getAttacker()).getDamage() + ")"});
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

        public void run() {
            try {
            if (!canceled) {
                long tmpStep = (System.currentTimeMillis() - JavaTestServer.serverStartTime) / spawnDelay;
                Monster m;
                Point beg;
                Point end = new Point(600, 763);

                if (tmpStep > step) {
                    step = tmpStep;
                    tdMonsterLoss += curWaveMonLoss;
                    if (tdMonsterLoss >= tdMonsterLossLimit) {
                        tdMonsterLoss = 0;
                        curWaveMonLoss = 0;
                        monStrMult = 1;
                        monCountAdd = 0;
                        synchronized (towers) {
                            Tower t;

                            for (ListIterator<Tower> li = towers.listIterator(); li.hasNext();) {
                                t = li.next();
                                vm.removeTower(t);
                                li.remove();
                            }
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
                    System.out.println(Thread.currentThread().getId() + " WE WANT MONSTERS.");
                    synchronized (monsters) {
                        System.out.println(Thread.currentThread().getId() + " WE TOOK MONSTERS.");
                        for (int i = 0; i < monsterCount + monCountAdd; i++) {
                            beg = new Point(500 + rand.nextInt(500), 5 + rand.nextInt(50));
                            m = new Monster(curPlayerID++, "Monster", 20 * monStrMult, beg.x, beg.y, speeds[rand.nextInt(2)]);
                            m.setSpriteSetName("poring");
                            monsters.add(m);
                            m.move(beg, end, System.currentTimeMillis() - JavaTestServer.serverStartTime);
                            //vm.update();
                        }
                    }
                    System.out.println(Thread.currentThread().getId() + " WE RELEASE MONSTERS.");
                }
                System.out.println(Thread.currentThread().getId() + " WE WANT MONSTERS.");
                synchronized (monsters) {
                    System.out.println(Thread.currentThread().getId() + " WE TOOK MONSTERS.");
                    boolean isDead;
                    boolean isStop;
                    try {
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
                        if (isDead || isStop) {
                            vm.removeMonster(m);
                            li.remove();
                        }
                    }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
                System.out.println(Thread.currentThread().getId() + " WE RELEASE MONSTERS.");
            }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
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

    private class TowerController implements Runnable {

        private ScheduledFuture future = null;
        private boolean canceled = false;

        public TowerController() {
        }

        public void run() {
            try {
            if (!canceled) {
                synchronized (towers) {
                    Tower t;

                    for (ListIterator<Tower> li = towers.listIterator(); li.hasNext();) {
                        t = li.next();
                        if (!attackRandomMonster(t)) {
                            t.selectMonster(monsters);
                            attackRandomMonster(t);
                        }
                    }
                }
            }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        private boolean attackRandomMonster(Tower t) {
            if (t.targetInRange()) {
                if (!t.reuse()) {
                    t.getTarget().doHit(t.getDamage());
                    t.setLastAttackTime(System.currentTimeMillis() - JavaTestServer.serverStartTime);
                    NukeAction a = new NukeAction(new NukeBolt((Unit) t, (Unit) t.getTarget(), System.currentTimeMillis() - serverStartTime));
                    ScheduledFuture f = executor.scheduleAtFixedRate(a, 0L, 10L, TimeUnit.MILLISECONDS);
                    a.setScheduledFuture(f);
                    sendToAll(new String[]{"(bolt " + t.getID() + " " + t.getTarget().getID() + " " + (System.currentTimeMillis() - serverStartTime) + ")"});
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

    private Unit attacker;
    private Unit target;
    private Movement mv;

    public NukeBolt(Unit attacker, Unit target, long begTime) {
        this.attacker = attacker;
        this.target = target;

        Point tmp = attacker.getCurPos();
        mv = new Movement(tmp.x, tmp.y, 1.0);
        mv.move(tmp, target.getCurPos(), begTime);
    }

    public boolean isFlight() {
        return mv.isMove();
    }

    public Unit getAttacker() {
        return attacker;
    }

    public Unit getTarget() {
        return target;
    }
}
