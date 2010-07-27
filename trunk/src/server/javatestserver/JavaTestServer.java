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
    private long curActionID = 1;
    ArrayList<Long> playerIDs = new ArrayList<Long>();

    private final ArrayList<Player> players = new ArrayList<Player>();
    private final ArrayList<NPC> npcs = new ArrayList<NPC>();

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
                            }
                        } else if ("nick".equals(pieces[0])) {
                            // <editor-fold defaultstate="collapsed" desc="Nick message processing">
                            Point selfCurPos;

                            nickReceived = true;
                            pieces = line.split(" ", 2);
                            pieces[1] = pieces[1].substring(1, pieces[1].length() - 1);
                            self = new Player(curPlayerID++, pieces[1]);
                            selfCurPos = self.getCurPos();
                            if ("localhost".equals(s.getInetAddress().getHostName())) {
                                self.setSpriteSet("poring");
                            } else {
                                self.setSpriteSet("desu");
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
                                    + "\"" + self.getSpriteSet() + "\"" + ")");
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
                                    + "\"" + self.getSpriteSet() + "\"" + ")"}, self.getID());
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
                                                + "\"" + p.getSpriteSet() + "\"" + ")");
                                        if (p.getText() != null) {
                                            send("(message " + p.getID() + " \"" + p.getText() + "\")");
                                        }
                                    }
                                }
                            }

                            // TODO NPC
                            //

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

    class NukeAction implements Runnable {
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
                    sendToAll(new String[] {"(hit " + bolt.getAttacker().getID() + " " + bolt.getTarget().getID() + " " + 10 + ")"});
                    if (bolt.getTarget().getHitPoints() == 0) {
                        bolt.getTarget().teleportToSpawn();
                        bolt.getTarget().restoreHitPoints();
                        sendToAll(new String[] {"(teleport " + bolt.getTarget().getID() + " " + bolt.getTarget().getCurPos().x + " " + bolt.getTarget().getCurPos().y + ")"});
                        sendToAll(new String[] {"(heal " + bolt.getTarget().getID() + " " + bolt.getTarget().getHitPoints() + ")"});
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
}

abstract class Unit {
    protected long id;
    protected String nick;
    protected String text;
    protected int hitPoints;
    protected String spriteSet;

    protected Movement mv;

    public long getID() {
        return id;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    abstract public void doHit(int dmg);

    public int getHitPoints() {
        return hitPoints;
    }

    public boolean isMove() {
        return mv.isMove();
    }

    public void move(Point beg, Point end, long begTime, double speed) {
        mv = new Movement(beg, end, begTime, speed);
    }

    public void setSpeed(double speed) {
        mv.setSpeed(speed);
    }

    public Point getCurPos() {
        return mv.getCurPos();
    }

    public void setSpriteSet(String spriteSet) {
        this.spriteSet = spriteSet;
    }

    public String getSpriteSet() {
        return spriteSet;
    }
}

class Player extends Unit {
    private static final double speed = 0.07;
    private static final int maxHitPoints = 100;

    public Player(long id, String nick) { // TODO Когда создаётся Игрок то какая у него начальная позиция?
        this.id = id;
        this.nick = nick;
        mv = new Movement(new Point(0, 0), new Point(0, 0), System.currentTimeMillis() - JavaTestServer.serverStartTime, speed);
        hitPoints = maxHitPoints;
    }

    public void move(Point beg, Point end, long begTime) {
        mv = new Movement(beg, end, begTime, speed);
    }
    
    public void doHit(int dmg) {
        hitPoints -= dmg;
        if (hitPoints < 0) {
            hitPoints = 0;
        }
    }

    public void restoreHitPoints() {
        hitPoints = maxHitPoints;
    }

    public void teleportToSpawn() {
        mv = new Movement(new Point(0, 0), new Point(0, 0), System.currentTimeMillis() - JavaTestServer.serverStartTime, speed);
    }
}

class NPC extends Unit {

    public NPC(long id, String nick) {
        this.id = id;
        this.nick = nick;
        hitPoints = 1;
    }

    public void doHit(int dmg) {}
}

class Movement {
    private boolean isMove;
    private Point beg;
    private Point cur; // For temporary storage purpose
    private Point end;
    private long begTime;
    private long endTime; // Calculated value
    private double speed;

    public Movement(Point beg, Point end, long begTime, double speed) {
        isMove = true;
        this.beg = beg;
        this.end = end;
        this.begTime = begTime;
        this.speed = speed;
        endTime = begTime + (long) (beg.distance(end) / speed);
        cur = (Point) beg.clone();
    }

    public boolean isMove() {
        if (!isMove) {
            return false;
        } else {
            cur = getCurPos();
            return isMove;
        }
    }


    public void setSpeed(double speed) {
        if (isMove) {
            this.speed = speed;
            endTime = begTime + (long) (beg.distance(end) / speed);
        } else {
            this.speed = speed;
        }
    }

    public Point getCurPos() {
        if (isMove) {
            long curTime = System.currentTimeMillis() - JavaTestServer.serverStartTime;
            double sqrt = Math.sqrt(Math.pow(Math.abs(end.x - beg.x), 2) + Math.pow(Math.abs(end.y - beg.y), 2));

            cur.x = (int) (beg.x + ((end.x - beg.x) / sqrt) * speed * (curTime - begTime));
            cur.y = (int) (beg.y + ((end.y - beg.y) / sqrt) * speed * (curTime - begTime));

            if (beg.x > end.x && end.x > cur.x
                    || beg.x < end.x && end.x < cur.x
                    || beg.y > end.y && end.y > cur.y
                    || beg.y < end.y && end.y < cur.y
                    || curTime > endTime) {
                cur.move(end.x, end.y);
                isMove = false;
            }
        }
        return cur;
    }
}

class NukeBolt {
    private Player attacker;
    private Player target;

    private Movement mv;

    private static final double speed = 1;

    public NukeBolt(Player attacker, Player target, long begTime) {
        this.attacker = attacker;
        this.target = target;
        mv = new Movement(attacker.getCurPos(), target.getCurPos(), begTime, speed);
    }

    public boolean isFlight() {
        return mv.isMove();
    }

    public Player getAttacker() {
        return attacker;
    }

    public Player getTarget() {
        return target;
    }
}