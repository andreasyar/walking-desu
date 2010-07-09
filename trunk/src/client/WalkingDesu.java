package client;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Comparator;
import java.util.Collections;
import java.lang.reflect.InvocationTargetException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class WalkingDesu {

    private static JFrame f;
    private static MyPanel p;
    private static ServerInteractTask sit = null;
    private static String serverIP = null;

    public static void main(String[] args) {
        if (args.length > 0) {
            serverIP = args[0];
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createAndShowGUI();
                }
            });
        } catch (InvocationTargetException e) {
            System.err.println(e.getMessage());
            return;
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
            return;
        }
        p.init();
        (new RedrawTask()).execute();
        WDTimerTask ttask = new WDTimerTask();
        sit = new ServerInteractTask();
        sit.execute();
    }

    private static void createAndShowGUI() {
        p = new MyPanel();
        f = new JFrame("Walking Desu 3");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(p);
        f.setSize(800,600);
        f.setVisible(true);
    }

    private static class RedrawTask extends SwingWorker<Void, Void> {
        public final long delay = 20; // 1000 ms / 20 ms = 50 fps

        @Override
        protected Void doInBackground() {
            while (true) {
                p.repaint();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                    return null;
                }
            }
        }
    }

    public static void addOutCommand(String c) {
        if (sit != null) {
            sit.addOutCommand(c);
        }
    }

    private static class ServerInteractTask extends SwingWorker<Void, Void> {
        private ArrayList<String> outCommands = new ArrayList<String>();
        private ArrayList<String> inCommands = new ArrayList<String>();
        private Socket serverSocket = null;
        private PrintWriter out = null;
        private BufferedReader in = null;

        public void addOutCommand(String c) {
            outCommands.add(c);
        }

        @Override
        protected Void doInBackground() {
            try {
                ServerReader sreader = new ServerReader();
                serverSocket = new Socket(serverIP, 45000);
                out = new PrintWriter(serverSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

                outCommands.add("hello");
                sreader.execute();
                while (serverSocket.isConnected()) {
                    while (outCommands.size() > 0) {
                        out.println(outCommands.get(0));
                        outCommands.remove(0);
                    }
                }

                sreader.cancel(true);
                out.close();
                in.close();
                serverSocket.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        private class ServerReader extends SwingWorker<Void, Void> {
            private String command;

            @Override
            protected Void doInBackground() {
                String[] pieces;

                try {
                    while (serverSocket.isConnected()) {
                        command = in.readLine();
                        System.out.println(command);
                        pieces = command.split(" ");
                        if (pieces[0].equals("hello")) {
                            p.selfLogin(Long.parseLong(pieces[1]), Long.parseLong(pieces[2]), Integer.parseInt(pieces[3]), Integer.parseInt(pieces[4]));
                            System.out.println(p.getSelf().id);
                        }
                        if (pieces[0].equals("newplayer")) {
                            p.addPlayer(Long.parseLong(pieces[1]), Integer.parseInt(pieces[2]), Integer.parseInt(pieces[3]));
                        }
                        if (pieces[0].equals("move")) {
                            long begTime = Long.parseLong(pieces[2]);
                            if (begTime > WDTimerTask.wdtime) { // Мы слильно опаздываем
                                WDTimerTask.wdtime = begTime;
                            }
                            p.movePlayer(Long.parseLong(pieces[1]), begTime, Integer.parseInt(pieces[3]), Integer.parseInt(pieces[4]));
                        }
                        if (pieces[0].equals("timesync")) {
                            System.out.println("Timesync " + WDTimerTask.wdtime + " -> " + pieces[1]);
                            WDTimerTask.wdtime = Long.parseLong(pieces[1]);
                        }
                        if (pieces[0].equals("delplayer")) {
                            p.delPlayer(Long.parseLong(pieces[1]));
                        }
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }

}

class MyPanel extends JPanel {

    public Dimension mapOfst; // Смещение 0,0 карты относительно 0,0 панели.

    public Dimension panelDim = null; // Размер этой панели.

    private BufferedImage buffImg = null;
    private Graphics buffGraph = null;
    private Dimension buffDim = null;

    private Player self = null;
    private ArrayList<Player> players;

    public MyPanel() {

        addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                BufferedImage map = WDMap.getInstance().getMapImg();
                Dimension m = new Dimension(map.getWidth(), map.getHeight());
                int x = e.getX();
                int y = e.getY();

                // Если клик был в пределах карты.
                if (x >= mapOfst.width && x <= m.width + mapOfst.width
                        && y >= mapOfst.height && y <= m.height + mapOfst.height) {
                    self.move(WDTimerTask.wdtime, x - mapOfst.width, y - mapOfst.height);
                    WalkingDesu.addOutCommand("move " + (x - mapOfst.width) + " " + (y - mapOfst.height));
                }

                // Когда мы начинаем движение, боты тоже начинают движение в
                // случайную точку на карте.
                /*Random r = new Random();
                int tmpW, tmpH;
                for(int i = 0; i < players.size(); i++) {
                    if(players.get(i) != self) {
                        tmpW = r.nextInt(m.width);
                        tmpH = r.nextInt(m.height);
                        players.get(i).move(WDTimerTask.wdtime, tmpW, tmpH);
                    }
                }*/
            }
        });

        addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e){

                Dimension d = e.getComponent().getSize();

                if (buffImg != null
                        && (buffImg.getWidth() != d.width || buffImg.getHeight() != d.height)) {
                    buffImg = new BufferedImage(d.width, d.height,
                            BufferedImage.TYPE_BYTE_INDEXED);
                    buffDim = new Dimension(buffImg.getWidth(),
                            buffImg.getHeight());
                    buffGraph = buffImg.getGraphics();
                }

                if (panelDim != null && d != null && mapOfst != null // TODO Concurency issue
                        && (panelDim.width != d.width || panelDim.height != d.height)) {
                    mapOfst.width += d.width / 2 - panelDim.width / 2;
                    mapOfst.height += d.height / 2 - panelDim.height / 2;
                }

                panelDim = d;
            }
        });
    }

    public void init() {
        // Наш игрок.
        self = new Player(0, 0, 0);

        // Добавим ботов.
        players = new ArrayList<Player>();
        /*players.add(new Player(0, 0));
        players.add(new Player(0, 0));
        players.add(new Player(0, 0));
        players.add(new Player(0, 0));
        players.add(new Player(0, 0));*/

        // Нашего игрока нужно тоже добавить в список всех игроков, для
        // сортировки по Y координате при отрисовке спрайтов.
        players.add(self);

        // Поскольку наш игрок всегда находится в центре экрана и в начале
        // его положение на карте (0, 0) то точка (0, 0) карты должны быть в
        // центре экрана.
        mapOfst = new Dimension(getWidth() / 2, getHeight() / 2);

        // Буфер для рисования (для двойной буферизации).
        buffImg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
        buffGraph = buffImg.getGraphics();
        buffDim = new Dimension(buffImg.getWidth(), buffImg.getHeight());

        panelDim = getSize();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (self != null && buffDim != null) { // TODO Concurency issue

            // Поле вне карты цвета фона и по краям черная рамочка в 1 пиксел.
            buffGraph.setColor(getBackground());
            buffGraph.fillRect(0, 0, buffDim.width - 1, buffDim.height - 1);
            buffGraph.setColor(Color.BLACK);
            buffGraph.drawRect(0, 0, buffDim.width - 1, buffDim.height - 1);

            // TODO На самом деле надо рисовать лишь видимую часть карты
            // которую можно получить с помощью метода getSubimage()
            buffGraph.drawImage(WDMap.getInstance().getMapImg(), mapOfst.width,
                    mapOfst.height, null);

            // Вычислим новое смещение карты (временно это делается здесь)
            if (panelDim.width / 2 != mapOfst.width + self.cur.x
                    || panelDim.height / 2 != mapOfst.height + self.cur.y) {
                mapOfst.width += panelDim.width / 2 - (self.cur.x + mapOfst.width);
                mapOfst.height += panelDim.height / 2 - (self.cur.y + mapOfst.height);
            }

            // Да, придётся отсортировать игроков по возрастанию Y координаты
            // чтобы "нижелещаие" спрайты не перекрывали вышележащие.
            // TODO optimization
            Collections.sort(players, new YAligner());
            Player p;
            for(int i = 0; i < players.size(); i++) {
                p = players.get(i);
                buffGraph.drawImage(p.getSprite(),
                        p.cur.x + mapOfst.width - p.getSprite().getWidth(null) / 2,
                        p.cur.y + mapOfst.height - p.getSprite().getHeight(null),
                        null);
            }

            g.drawImage(buffImg, 0, 0, null);
        }
    }

    public void addPlayer(long id, int x, int y) {
        if (players != null) {
            players.add(new Player(id, x, y));
        }
    }

    public void delPlayer(long id) {
        if (players != null) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).id == id) {
                    players.remove(i);
                    return;
                }
            }
        }
    }

    public void movePlayer(long id, long tstamp, int x, int y) {
        if (players != null) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).id == id) {
                    players.get(i).move(tstamp, x, y);
                    return;
                }
            }
        }
    }

    public void selfLogin(long id, long tstamp, int x, int y) {
        if (self != null) {
            self.id = id;
            WDTimerTask.wdtime = tstamp;
            self.cur.move(x, y);
        }
    }

    public Player getSelf() {
        return self;
    }
}

class Player {

    long id;

    public Point cur; // Точка на карте, где находится игрок.
    double speed = 1;
    private SpriteSet set;

    // Movment
    boolean isMove;
    long begTime;
    Point beg;
    Point end;

    // Temp for animation
    boolean resetStandAnimationTimer = false;

    public Player(int x, int y) {
        beg = new Point(0, 0);
        end = new Point(1, 1);
        set = new SpriteSet();
        cur = new Point(x, y);
        isMove = false;
    }

    public Player(long _id, int x, int y) {
        id = _id;
        beg = new Point(0, 0);
        end = new Point(1, 1);
        set = new SpriteSet();
        cur = new Point(x, y);
        isMove = false;
    }

    public BufferedImage getSprite() {
        if (isMove()) {
            resetStandAnimationTimer = true;
            return set.getMovement().getDirection(beg, end).getMoveSpr(beg.distance(cur));
        } else {
            BufferedImage tmp = set.getMovement().getDirection(beg, end).getStandSpr(resetStandAnimationTimer, WDTimerTask.wdtime);
            resetStandAnimationTimer = false;
            return tmp;
        }
    }

    private boolean isMove() {
        if (isMove) {
            long curTime = WDTimerTask.wdtime;

            if (curTime > begTime) {
                cur.x = (int) (beg.x + ((end.x - beg.x) / Math.sqrt(Math.pow(Math.abs(end.x - beg.x), 2) + Math.pow(Math.abs(end.y - beg.y), 2))) * speed * Math.abs(curTime - begTime));
                cur.y = (int) (beg.y + ((end.y - beg.y) / Math.sqrt(Math.pow(Math.abs(end.x - beg.x), 2) + Math.pow(Math.abs(end.y - beg.y), 2))) * speed * Math.abs(curTime - begTime));

                if ((beg.x > cur.x && end.x > cur.x || beg.x < cur.x && end.x < cur.x) && (beg.y > cur.y && end.y > cur.y || beg.y < cur.y && end.y < cur.y)) {
                    cur.x = end.x;
                    cur.y = end.y;
                    isMove = false;
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public void move(long _begTime, int x, int y) {
        //System.out.println("Lets move to: (" + x +  ", " + y + ")");
        isMove = true;
        begTime = _begTime;
        beg.move(cur.x, cur.y);
        end.move(x, y);
    }

}

class WDMap {

    private static WDMap map = null;
    private BufferedImage mapImg;

    private WDMap(int w, int h) {
        Graphics g;

		mapImg = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED);
		g = mapImg.getGraphics();
		g.setColor(Color.gray);
		g.fillRect(1, 1, w - 2, h - 2);
    }

    public static WDMap getInstance() {
        if (map == null) {
            map = new WDMap(1024, 768);
        }
        return map;
    }

    public BufferedImage getMapImg() {
        return mapImg;
    }

}

class WDTimerTask extends TimerTask {

    public static long wdtime = 0;
    public Timer timer; // TODO Where gentle stop timer?

    public WDTimerTask() {
        timer = new Timer();
        timer.schedule(this, 0, 10); // 1000 / 10 = 100 times per second
    }

    public void run(){
        wdtime++;
    }
}

class YAligner implements Comparator {
    public final int compare(Object a, Object b) {
        return ((Player) a).cur.y > ((Player) b).cur.y ? 1 : 0;
    }
}