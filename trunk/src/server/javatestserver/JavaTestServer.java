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
    BlockingQueue<SocketProcessor> clientQueue
            = new LinkedBlockingQueue<SocketProcessor>();

    private long curPlayerID = 1;
    ArrayList<Long> playerIDs = new ArrayList<Long>();

    private final ArrayList<Player> players = new ArrayList<Player>();
    private final ArrayList<NPC> npcs = new ArrayList<NPC>();
    private final ArrayList<Monster> monsters = new ArrayList<Monster>();
    private final ArrayList<Tower> towers = new ArrayList<Tower>();

    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);

    public static final long serverStartTime = System.currentTimeMillis();
    private static final int port = 45000;

    private TimeSyncTask ts;

    public JavaTestServer(int port) throws IOException {
        ss = new ServerSocket(port);
        ts = new TimeSyncTask();
    }

    /**
     * Главный цикл тестового ява сервера.
     */
    void run() {
        serverThread = Thread.currentThread();

        MonsterController c = new MonsterController(20 * 1000, 20);
        c.setScheduledFuture(executor.scheduleAtFixedRate(c, 0L, 100L, TimeUnit.MILLISECONDS));
        TowerController t = new TowerController();
        t.setScheduledFuture(executor.scheduleAtFixedRate(t, 0L, 100L, TimeUnit.MILLISECONDS));

        while (true) {
            Socket s = getNewConnection();

            if (serverThread.isInterrupted()) {
                break;
            } else if (s != null){
                try {
                    final SocketProcessor processor = new SocketProcessor(s);
                    final Thread thread = new Thread(processor);
                    thread.setDaemon(true);
                    thread.start();
                    clientQueue.offer(processor);
                }
                catch (IOException ignored) {}
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
     * Отправляет заданные сообщения всем пользователям.
     * @param msgs Сообщения.
     */
    public void sendToAll (String[] msgs) {
        for (String msg:msgs) {
            for (SocketProcessor sp:clientQueue) {
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
    public void sendToAll (String[] msgs, long excID) {
        for (String msg:msgs) {
            for (SocketProcessor sp:clientQueue) {
                if (sp.getSelfPlayer() != null && sp.getSelfPlayer().getID() != excID && sp.loggedIn()) {
                    sp.send(msg);
                }
            }
        }
    }

    private Player getPlayer(long id) {
        Player p = null;

        synchronized(players) {
            for(Iterator<Player> li = players.iterator(); li.hasNext();) {
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

    private class TimeSyncTask extends TimerTask {

        public Timer timer;

        public TimeSyncTask() {
            timer = new Timer();
            timer.schedule(this, 0, 5000); // each 5 seconds
        }

        public void run(){
            sendToAll(new String[] {"(timesync "
                    + (System.currentTimeMillis() - serverStartTime) + ")"});
        }

        @Override
        public boolean cancel(){
            boolean tmp = super.cancel();
            timer.cancel();
            return tmp;
        }
    }

    private class SocketProcessor implements Runnable{
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
                                                           "UTF-8") );
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
                                line = line.substring("move".length() + 1, line.length());
                                pieces = line.split(" ");
                                self.move(self.getCurPos(), new Point(Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1])), (System.currentTimeMillis() - serverStartTime));
                                sendToAll(new String[] {"(move " + self.getID() + " " + (System.currentTimeMillis() - serverStartTime) + " " + pieces[0] + " " + pieces[1] + ")"}, self.getID());
                                // </editor-fold>
                            } else if ("message".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Message message processing">
                                line = line.substring("message".length() + 1, line.length());
                                line = line.substring(1, line.length() - 1);
                                self.setText(line);
                                sendToAll(new String[] {"(message " + self.getID() + " \"" + line + "\")"}, self.getID());
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
                                sendToAll(new String[] {"(bolt " + self.getID() + " " + target.getID() + " " + (System.currentTimeMillis() - serverStartTime) + ")"}, self.getID());
                                // </editor-fold>
                            } else if ("tower".equals(pieces[0])) {
                                // <editor-fold defaultstate="collapsed" desc="Tower processing">
                                pieces = line.split(" ");
                                Tower t = new Tower(curPlayerID++, "Tower", 200.0, 10, 1000L, Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]));
                                sendToAll(new String[] {"(tower "
                                        + t.getID() + " "
                                        + t.getRange() + " "
                                        + t.getDamage() + " "
                                        + t.getCurPos().x + " "
                                        + t.getCurPos().y + " "
                                        + "\"" + t.getNick() + "\"" + ")"});
                                synchronized (towers) {
                                    towers.add(t);
                                }
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
                                    + "\"SOUTH\""  + " "
                                    + "\"" + self.getSpriteSetName() + "\"" + ")");
                            helloSended = true;
                            synchronized (players) {
                                players.add(self);
                            }
                            sendToAll(new String[] {"(newplayer "
                                    + self.getID() + " "
                                    + self.getHitPoints() + " "
                                    + 0.07 + " "
                                    + selfCurPos.x + " "
                                    + selfCurPos.y + " "
                                    + "\"" + self.getNick() + "\"" + " "
                                    + "\"SOUTH\""  + " "
                                    + "\"" + self.getSpriteSetName() + "\"" + ")"}, self.getID());
                            synchronized (players) {
                                Player p;
                                Point cur;

                                for (ListIterator<Player> li = players.listIterator(); li.hasNext();) {
                                    p = li.next();
                                    if (p != self) {
                                        cur = p.getCurPos();

                                        send("(newplayer "
                                                + p.getID() + " "
                                                + p.getHitPoints() + " "
                                                + 0.07 + " "
                                                + cur.x + " "
                                                + cur.y + " "
                                                + "\"" + p.getNick() + "\"" + " "
                                                + "\"SOUTH\""  + " "
                                                + "\"" + p.getSpriteSetName() + "\"" + ")");
                                        if (p.getText() != null) {
                                            send("(message " + p.getID() + " \"" + p.getText() + "\")");
                                        }
                                    }
                                }
                            }

                            // TODO NPC
                            synchronized (towers) {
                                Tower t;

                                for (ListIterator<Tower> li = towers.listIterator(); li.hasNext();) {
                                    t = li.next();

                                    send("(tower "
                                        + t.getID() + " "
                                        + t.getRange() + " "
                                        + t.getDamage() + " "
                                        + t.getCurPos().x + " "
                                        + t.getCurPos().y + " "
                                        + "\"" + t.getNick() + "\"" + ")");
                                }
                            }

                            synchronized (players) {
                                System.out.println("Welcome to WD Java Test Server. Online: " + players.size() + " players (with you).");
                            }
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
                } catch (IOException ignored) {}
            } else {
                return;
            }
            if (self != null) {
                sendToAll(new String[] {"(delplayer " + self.getID()  + ")"}, self.getID());
                synchronized (players) {
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
                    bolt.getTarget().doHit(10);
                    sendToAll(new String[] {"(hit " + bolt.getAttacker().getID() + " " + bolt.getTarget().getID() + " " + ((Tower) bolt.getAttacker()).getDamage() + ")"});
                    /*if (bolt.getTarget().getHitPoints() == 0) {
                        bolt.getTarget().teleportToSpawn();
                        bolt.getTarget().restoreHitPoints();
                        sendToAll(new String[] {"(teleport " + bolt.getTarget().getID() + " " + bolt.getTarget().getCurPos().x + " " + bolt.getTarget().getCurPos().y + ")"});
                        sendToAll(new String[] {"(heal " + bolt.getTarget().getID() + " " + bolt.getTarget().getHitPoints() + ")"});
                    }*/
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
        private final double[] speeds = new double[] {0.08, 0.07, 0.06};
        private Random rand = new Random();
        private ScheduledFuture future = null;
        private boolean canceled = false;

        private long step;

        public MonsterController(long spawnDelay, int monsterCount) {
            this.spawnDelay = spawnDelay;
            this.monsterCount = monsterCount;
            step = (System.currentTimeMillis() - JavaTestServer.serverStartTime) / spawnDelay;
        }

        public void run() {
            if (!canceled) {
                long tmpStep = (System.currentTimeMillis() - JavaTestServer.serverStartTime) / spawnDelay;
                Monster m;
                Point beg;
                Point end = new Point(600, 763);

                if (tmpStep > step) {
                    step = tmpStep;
                    synchronized (monsters) {
                        for (int i = 0; i < monsterCount; i++) {
                            beg = new Point(500 + rand.nextInt(500), 5 + rand.nextInt(50));
                            m = new Monster(curPlayerID++, "Monster", 20, beg.x, beg.y, speeds[rand.nextInt(2)]);
                            m.setSpriteSetName("poring");
                            monsters.add(m);
                            sendToAll(new String[] {"(newmonster "
                                    + m.getID() + " "
                                    + m.getHitPoints() + " "
                                    + m.getSpeed() + " "
                                    + m.getCurPos().x + " "
                                    + m.getCurPos().y + " "
                                    + "\"" + m.getNick() + "\"" + " "
                                    + "\"SOUTH\""  + " "
                                    + "\"" + m.getSpriteSetName() + "\"" + ")"});
                            m.move(beg, end, System.currentTimeMillis() - JavaTestServer.serverStartTime);
                            sendToAll(new String[] {"(move " + m.getID() + " " + (System.currentTimeMillis() - serverStartTime) + " " + end.x + " " + end.y + ")"});
                        }
                    }
                }
                synchronized (monsters) {
                    for (ListIterator<Monster> li = monsters.listIterator(); li.hasNext(); ) {
                        m = li.next();
                        if (m.dead() || !m.isMove()) {
                            sendToAll(new String[] {"(delmonster " + m.getID()  + ")"});
                            li.remove();
                        }
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

    private class TowerController implements Runnable {
        private ScheduledFuture future = null;
        private boolean canceled = false;

        public TowerController() {
        }

        public void run() {
            if (!canceled) {
                synchronized (towers) {
                    Tower t;

                    for (ListIterator<Tower> li = towers.listIterator(); li.hasNext();) {
                        t = li.next();
                        //System.out.println("Tower attemp to attack");
                        if (!attackRandomMonster(t)) {
                            t.selectMonster(monsters);
                            attackRandomMonster(t);
                        }
                    }
                }
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
                    sendToAll(new String[] {"(bolt " + t.getID() + " " + t.getTarget().getID() + " " + (System.currentTimeMillis() - serverStartTime) + ")"});
                    //System.out.println("Hit target.");
                    return true;
                }
                //System.out.println("Tower reuse.");

                return false;
            }
            //System.out.println("Target not in range.");

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