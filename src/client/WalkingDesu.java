package client;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.ListIterator;
import java.lang.reflect.InvocationTargetException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.awt.Graphics2D;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.TimerTask;
import java.util.Timer;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;

public class WalkingDesu {

    private static JFrame f;
    private static MyPanel p;
    private static ServerInteractTask sit = null;
    private static String serverIP = null;
    private static int serverPort = 0;
    private static JButton b;
    private static JTextField t;

    public static long serverStartTime;

    public static void main(String[] args) {
        serverStartTime = System.currentTimeMillis();
        if (args.length > 1) {
            serverIP = args[0];
            serverPort = Integer.parseInt(args[1]);
        } else {
            System.out.println("How about server ip and port?");
            System.exit(1);
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
        CustomDialog d = new CustomDialog();
        d.setSize(new Dimension(400, 200));
        d.startTimer();
        d.setVisible(true);
        p.init();
        p.getSelf().setNick(d.getValidatedText());
        Executor executor = Executors.newCachedThreadPool();
        executor.execute(new RedrawTask());
        sit = new ServerInteractTask();
        executor.execute(sit);
        p.addKeyListener(new MyPanelKeyListener(p));
        //(new RedrawTask()).execute();
        //sit.execute();
    }

    public static JFrame getFrame() {
        return f;
    }

    public static MyPanel getPanel() {
        return p;
    }

    private static void createAndShowGUI() {
        p = new MyPanel();
        f = new JFrame("Walking Desu 3");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        b = new JButton("Send");
        t = new JTextField(50);
        t.addKeyListener (new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    if (p != null && p.getSelf() != null && t.getText() != null && !p.getSelf().text.equals(t.getText())) {
                        p.getSelf().text = t.getText().length() > 100 ?t.getText().substring(0, 99) : t.getText();
                        p.getSelf().shouldUpdateTextCloud();
                        WalkingDesu.addOutCommand("message \"" +  p.getSelf().text + "\"");
                    }
                }
            }
        });
        p.add(b);
        p.add(t);
        p.setFocusable(true);
        f.add(p);
        f.setSize(800,600);
        f.setVisible(true);
        
    }

    public static void setButtonActionListener(Player p) {
        b.addActionListener(new MyListener(p, t));
    }

    public static void setTextfieldKeyListener() {
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
        private final ArrayList<String> outCommands = new ArrayList<String>();
        private ArrayList<String> inCommands = new ArrayList<String>();
        private Socket serverSocket = null;
        private PrintWriter out = null;
        private BufferedReader in = null;
        private final Lock lock = new ReentrantLock();

        @Override
        protected Void doInBackground() {
            try {
                ServerReader sreader = new ServerReader();
                serverSocket = new Socket(serverIP, serverPort);
                out = new PrintWriter(new OutputStreamWriter(serverSocket.getOutputStream(), "UTF-8"), true);
                in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream(), "UTF-8"));

                outCommands.add("(hello)");
                outCommands.add("(nick \"" + p.getSelf().getNick() + "\")");
                Executor executor = Executors.newCachedThreadPool();
                executor.execute(sreader);
                while (serverSocket.isConnected()) {
                    synchronized(outCommands) {
                        while (outCommands.size() > 0) {
                            out.println(outCommands.get(0));
                            outCommands.remove(0);
                        }
                        try {
                            outCommands.wait();
                        } catch (InterruptedException ignored) {}
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

        public void addOutCommand(String c) {
            if (!c.startsWith("(")) {
                c = "(" + c + ")";
            }
            outCommands.add(c);
            System.out.println("Command added: " + c);
            synchronized(outCommands) {
                outCommands.notify();
            }
        }

        private class ServerReader extends SwingWorker<Void, Void> {
            private String command;

            @Override
            protected Void doInBackground() {
                String[] pieces;

                try {
                    while (serverSocket.isConnected()) {
                        command = in.readLine();
                        System.out.println("Command received: " + command);
                        if (command.startsWith("(")) {
                            command = command.substring(1, command.length() - 1); // remove ( )
                        }
                        pieces = command.split(" ");
                        if (pieces[0].equals("hello")) {
                            p.selfLogin(Long.parseLong(pieces[1]), Long.parseLong(pieces[2]), Integer.parseInt(pieces[3]), Integer.parseInt(pieces[4]));
                        }
                        if (pieces[0].equals("newplayer")) {
                            p.addPlayer(Long.parseLong(pieces[1]), Integer.parseInt(pieces[2]), Integer.parseInt(pieces[3]));
                        }
                        if (pieces[0].equals("move")) {
                            long begTime = Long.parseLong(pieces[2]);
                            serverStartTime = System.currentTimeMillis() - begTime;
                            p.movePlayer(Long.parseLong(pieces[1]), begTime, Integer.parseInt(pieces[3]), Integer.parseInt(pieces[4]));
                        }
                        if (pieces[0].equals("timesync")) {
                            long remoteTimeDiff = Long.parseLong(pieces[1]);
                            System.out.println("Timesync " + Math.abs(System.currentTimeMillis() - serverStartTime) + " -> " + pieces[1]);
                            serverStartTime = System.currentTimeMillis() - remoteTimeDiff;
                        }
                        if (pieces[0].equals("delplayer")) {
                            p.delPlayer(Long.parseLong(pieces[1]));
                        }
                        if (pieces[0].equals("message")) {
                            pieces = command.split(" ", 3);
                            p.setPlayerText(Long.parseLong(pieces[1]), pieces[2].substring(1, pieces[2].length() - 1));
                        }
                        if (pieces[0].equals("nick")) {
                            pieces = command.split(" ", 3);
                            p.setPlayerNick(Long.parseLong(pieces[1]), pieces[2].substring(1, pieces[2].length() - 1));
                        }
                        if (pieces[0].equals("bolt")) {
                            Player atk = p.getPlayer(Long.parseLong(pieces[1]));
                            Player tgt = p.getPlayer(Long.parseLong(pieces[2]));
                            long begTime = Long.parseLong(pieces[3]);

                            if (atk != null && tgt != null) {
                                p.addBolt(atk, tgt, begTime);
                            }
                        }
                        if (pieces[0].equals("hit")) {
                            Player atk = p.getPlayer(Long.parseLong(pieces[1]));
                            Player tgt = p.getPlayer(Long.parseLong(pieces[2]));

                            if (atk != null && tgt != null) {
                                tgt.doHit(Integer.parseInt(pieces[3]));
                            }
                        }
                        if (pieces[0].equals("teleport")) {
                            Player player = p.getPlayer(Long.parseLong(pieces[1]));

                            if (player != null) {
                                player.teleportTo(Integer.parseInt(pieces[2]), Integer.parseInt(pieces[3]));
                            }
                        }
                        if (pieces[0].equals("heal")) {
                            Player player = p.getPlayer(Long.parseLong(pieces[1]));

                            if (player != null) {
                                player.doHeal(Integer.parseInt(pieces[2]));
                            }
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

class MyPanelKeyListener implements KeyListener {
    MyPanel p;
    Player self;

    public MyPanelKeyListener(MyPanel _p) {
        p = _p;
        self = p.getSelf();
    }
    public void keyTyped(KeyEvent e) {
        
    }
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_SHIFT) {
            p.disableSelMode();
        }
    }
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ESCAPE) {
            if (self.selectedPlayer != null) {
                self.selectedPlayer = null;
            }
        } else if (key == KeyEvent.VK_SHIFT) {
            p.enableleSelMode();
        } else if (key == KeyEvent.VK_F3) {
            if (self.selectedPlayer != null && self.allowNuke() && !self.equals(self.selectedPlayer)) {
                long tmp = Math.abs(System.currentTimeMillis() - WalkingDesu.serverStartTime);
                p.addBolt(self, self.selectedPlayer, tmp);
                self.setLastNukeTime(tmp);
                WalkingDesu.addOutCommand("(bolt " + self.selectedPlayer.id + ")");
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

    private ArrayList<NukeBolt> bolts;
    private ArrayList<Integer> deadBolts;

    private boolean selMode = false;

    private ArrayList<Polygon> geoData = new ArrayList<Polygon>();
    private BufferedImage geoDebugLayer = null;
    private Graphics geoDebugLayerGraph = null;
    private Dimension geoDebugLayerDim = null;

    public void addBolt(Player a, Player t, long bt){
        bolts.add(new NukeBolt(a, t, bt));
    }

    public void enableleSelMode() {
        selMode = true;
    }

    public void disableSelMode() {
        selMode = false;
    }

    public void setleSelMode(boolean sm) {
        selMode = sm;
    }

    public boolean getleSelMode() {
        return selMode;
    }

    public Player getPlayer(long id) {
        for (Player p:players) {
            if (p.id == id) {
                return p;
            }
        }
        return null;
    }

    public MyPanel() {

        addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                WalkingDesu.getPanel().requestFocus();
                BufferedImage map = WDMap.getInstance().getMapImg();
                Dimension m = new Dimension(map.getWidth(), map.getHeight());
                int x = e.getX();
                int y = e.getY();

                // Если клик был в пределах карты.
                if (x >= mapOfst.width && x <= m.width + mapOfst.width
                        && y >= mapOfst.height && y <= m.height + mapOfst.height) {
                    ArrayList<Point> points = new ArrayList<Point>();
                    Polygon poly;
                    Player p;
                    BufferedImage spr;
                    boolean selected = false;

                    if (selMode) {
                        for(int i = 0; i < players.size(); i++) {
                            p = players.get(i);
                            spr = p.getSprite();

                            points.clear();
                            points.add(new Point(p.cur.x - spr.getWidth() / 2, p.cur.y));
                            points.add(new Point(p.cur.x - spr.getWidth() / 2, p.cur.y - spr.getHeight()));
                            points.add(new Point(p.cur.x + spr.getWidth() / 2, p.cur.y - spr.getHeight()));
                            points.add(new Point(p.cur.x + spr.getWidth() / 2, p.cur.y));

                            if (inpoly(points, x - mapOfst.width, y - mapOfst.height)) {
                                self.selectedPlayer = p;
                                selected = true;
                                break;
                            }
                        }
                    }
                    if (!selected) {
                        self.move(Math.abs(System.currentTimeMillis() - WalkingDesu.serverStartTime),
                                x - mapOfst.width, y - mapOfst.height);
                        WalkingDesu.addOutCommand("move " + (x - mapOfst.width)
                                + " " + (y - mapOfst.height));
                    }
                }
            }
        });

        addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e){

                Dimension d = e.getComponent().getSize();

                if (buffImg != null
                        && (buffImg.getWidth() != d.width || buffImg.getHeight() != d.height)) {
                    buffImg = new BufferedImage(d.width, d.height,
                            BufferedImage.TYPE_INT_RGB);
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

        players = new ArrayList<Player>();

        // Нашего игрока нужно тоже добавить в список всех игроков, для
        // сортировки по Y координате при отрисовке спрайтов.
        players.add(self);

        // Поскольку наш игрок всегда находится в центре экрана и в начале
        // его положение на карте (0, 0) то точка (0, 0) карты должны быть в
        // центре экрана.
        mapOfst = new Dimension(getWidth() / 2, getHeight() / 2);

        // Буфер для рисования (для двойной буферизации).
        buffImg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        buffGraph = buffImg.getGraphics();
        buffDim = new Dimension(buffImg.getWidth(), buffImg.getHeight());

        geoDebugLayer = new BufferedImage(WDMap.getInstance().getMapImg().getWidth(), WDMap.getInstance().getMapImg().getHeight(), BufferedImage.TYPE_4BYTE_ABGR_PRE);
        geoDebugLayerGraph = geoDebugLayer.getGraphics();
        //((Graphics2D) geoDebugLayerGraph).setBackground(new Color((float)0.0, (float)0.0, (float)0.0, (float)0.7));
        /*((Graphics2D) geoDebugLayerGraph).setColor(new Color((float)0.1, (float)1.0, (float)0.3, (float)0.7));
        ((Graphics2D) geoDebugLayerGraph).fillRect(0, 0, geoDebugLayer.getWidth() - 1, geoDebugLayer.getHeight() - 1);
        ((Graphics2D) geoDebugLayerGraph).setColor(Color.BLACK);*/
        geoDebugLayerDim = new Dimension(geoDebugLayer.getWidth(), geoDebugLayer.getHeight());

        panelDim = getSize();

        WalkingDesu.setButtonActionListener(self);

        bolts = new ArrayList<NukeBolt>();
        deadBolts = new ArrayList<Integer>();

        Polygon tmp = new Polygon();
        tmp.addPoint(100, 100);
        tmp.addPoint(200, 100);
        tmp.addPoint(200, 200);
        tmp.addPoint(100, 200);
        geoData.add(tmp);
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
            BufferedImage textCloud;
            String nick;
            for(int i = 0; i < players.size(); i++) {
                p = players.get(i);
                buffGraph.drawImage(p.getSprite(),
                        p.cur.x + mapOfst.width - p.getSprite().getWidth(null) / 2,
                        p.cur.y + mapOfst.height - p.getSprite().getHeight(null),
                        null);
                textCloud = p.getTextCloud();
                if (textCloud != null) {
                    buffGraph.drawImage(textCloud,
                            p.cur.x + mapOfst.width + p.getSprite().getWidth(null) / 2,
                            p.cur.y + mapOfst.height - p.getSprite().getHeight(null),
                            null);
                }
                nick = p.getNick();
                if (nick != null) { // TODO рисовать по центру
                    buffGraph.drawString(nick,
                            p.cur.x + mapOfst.width - 20,
                            p.cur.y + mapOfst.height + 10);
                }
            }

            if (self.selectedPlayer != null) {
                buffGraph.drawString(self.selectedPlayer.getNick() + " (" + self.selectedPlayer.id + ")", 10, panelDim.height - 50);
                buffGraph.drawString("HP: " + self.selectedPlayer.hitPoints, 10, panelDim.height - 30);
            }

            if (bolts.size() > 0) {
                BufferedImage boltSpr;
                Point boltPos;
                Dimension atkSprDim;
                deadBolts.clear();

                for (int i = 0; i < bolts.size(); i++) {
                    if (bolts.get(i).isFlight()) {
                        boltSpr = bolts.get(i).getSprite();
                        boltPos = bolts.get(i).getCurPos();
                        atkSprDim = bolts.get(i).getAttackerSprDim();
                        buffGraph.drawImage(boltSpr,
                            boltPos.x + mapOfst.width,
                            boltPos.y + mapOfst.height - atkSprDim.height / 2,
                            null);
                    } else {
                        deadBolts.add(i);
                    }
                }
                for (Integer boltIndex:deadBolts) {
                    bolts.remove(boltIndex.intValue());
                }
            }

            if (geoData.size() > 0) {
                for (Polygon poly:geoData) {
                    geoDebugLayerGraph.drawPolygon(poly);
                }
                buffGraph.drawImage(geoDebugLayer,
                        mapOfst.width, mapOfst.height, null);
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
                    break;
                }
            }
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).selectedPlayer.id == id) {
                    players.get(i).selectedPlayer = null;
                }
            }
        }
    }

    public void setPlayerText(long id, String t) {
        if (players != null) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).id == id) {
                    players.get(i).setText(t);
                    players.get(i).shouldUpdateTextCloud();
                    return;
                }
            }
        }
    }

    public void setPlayerNick(long id, String t) {
        if (players != null) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).id == id) {
                    players.get(i).setNick(t);
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
            WalkingDesu.serverStartTime = System.currentTimeMillis() - tstamp;
            self.cur.move(x, y);
        }
    }

    public Player getSelf() {
        return self;
    }

    private boolean inpoly(ArrayList<Point> points, int xt, int yt) {
        int xnew,ynew;
        int xold,yold;
        int x1,y1;
        int x2,y2;
        int i;
        boolean inside = false;
        int npoints = points.size();

        if (npoints < 3) {
            return false;
        }
        xold=points.get(npoints - 1).x;
        yold=points.get(npoints - 1).y;
        for (i=0 ; i < npoints ; i++) {
            xnew=points.get(i).x;
            ynew=points.get(i).y;
            if (xnew > xold) {
                x1=xold;
                x2=xnew;
                y1=yold;
                y2=ynew;
            }
            else {
                x1=xnew;
                x2=xold;
                y1=ynew;
                y2=yold;
            }
            /* edge "open" at one end */
            if ((xnew < xt) == (xt <= xold) && ((long)yt-(long)y1)*(long)(x2-x1) < ((long)y2-(long)y1)*(long)(xt-x1)) {
                inside = !inside;
            }
            xold=xnew;
            yold=ynew;
        }
        return inside;
    }

    // Oh ty algolist.manual.ru
    private boolean segcross(int x11, int y11, int x12, int y12, int x21, int y21, int x22, int y22) {
        int maxx1 = Math.max(x11, x12), maxy1 = Math.max(y11, y12);
        int minx1 = Math.min(x11, x12), miny1 = Math.min(y11, y12);
        int maxx2 = Math.max(x21, x22), maxy2 = Math.max(y21, y22);
        int minx2 = Math.min(x21, x22), miny2 = Math.min(y21, y22);

        if (minx1 > maxx2 || maxx1 < minx2 || miny1 > maxy2 || maxy1 < miny2) {
            return false;  // Один из отрезков целиком лежит слева, справа, выше или ниже второго.
        }

        int dx1 = x12-x11, dy1 = y12-y11; // Длина проекций первой линии на ось x и y
        int dx2 = x22-x21, dy2 = y22-y21; // Длина проекций второй линии на ось x и y
        int dxx = x11-x21, dyy = y11-y21;
        int div, mul;

        if ((div = (int)((double)dy2*dx1-(double)dx2*dy1)) == 0) {
            return false; // Отрезки параллельны...
        }
        if (div > 0) {
            if ((mul = (int)((double)dx1*dyy-(double)dy1*dxx)) < 0 || mul > div) {
                return false; // Первый отрезок пересекается за своими границами...
            }
            if ((mul = (int)((double)dx2*dyy-(double)dy2*dxx)) < 0 || mul > div) {
                return false; // Второй отрезок пересекается за своими границами...
            }
        }

        if ((mul = -(int)((double)dx1*dyy-(double)dy1*dxx)) < 0 || mul > -div) {
            return false; // Первый отрезок пересекается за своими границами...
        }
        if ((mul = -(int)((double)dx2*dyy-(double)dy2*dxx)) < 0 || mul > -div) {
            return false; // Второй отрезок пересекается за своими границами...
        }

        return true;
    }
}

class NukeBolt {
    private Player attacker;
    private Dimension attackerSprDim = new Dimension(0, 0);
    private Player target;

    private long begTime;
    private final double speed = 1.0;
    NukeBoltMovement movement;

    private BufferedImage curSpr;
    private Point beg = new Point(0, 0);
    private Point cur = new Point(0, 0);
    private Point end = new Point(0, 0);

    private boolean flight = true;

    public static final long reuse = 2000;

    public NukeBolt(Player a, Player t, long bt) {
        attacker = a;
        target = t;
        begTime = bt;
        movement = new NukeBoltMovement(begTime);
    }

    public BufferedImage getSprite() {
        curSpr = movement.getSprite(Math.abs(System.currentTimeMillis() - WalkingDesu.serverStartTime));
        return curSpr;
    }

    public Point getCurPos() {
        beg.move(attacker.cur.x, attacker.cur.y);
        end.move(target.cur.x, target.cur.y);
        long curTime = Math.abs(System.currentTimeMillis() - WalkingDesu.serverStartTime);

        //System.out.print("(" + cur.x + ", " + cur.y + ") -> ");
        cur.x = (int) (beg.x + ((end.x - beg.x) / Math.sqrt(Math.pow(Math.abs(end.x - beg.x), 2) + Math.pow(Math.abs(end.y - beg.y), 2))) * speed * Math.abs(curTime - begTime));
        cur.y = (int) (beg.y + ((end.y - beg.y) / Math.sqrt(Math.pow(Math.abs(end.x - beg.x), 2) + Math.pow(Math.abs(end.y - beg.y), 2))) * speed * Math.abs(curTime - begTime));
        //System.out.println("(" + cur.x + ", " + cur.y + ")");

        if (beg.x > end.x && end.x > cur.x
                || beg.x < end.x && end.x < cur.x
                || beg.y > end.y && end.y > cur.y
                || beg.y < end.y && end.y < cur.y) {
            //System.out.print("Oops: " + "(" + cur.x + ", " + cur.y + ") fixed to ");
            cur.x = end.x;
            cur.y = end.y;
            //System.out.println("(" + cur.x + ", " + cur.y + ")");
            flight = false;
        }
        return cur;
    }

    public boolean isFlight() {
        return flight;
    }

    public Dimension getAttackerSprDim() {
        BufferedImage tmp = attacker.getSprite();
        attackerSprDim.setSize(tmp.getWidth(), tmp.getHeight());
        return attackerSprDim;
    }
}

class Player {
    private static final int maxHitPoints = 100;

    long id;
    String nick = null;
    Player selectedPlayer = null;
    int hitPoints = 100;
    private long lastNukeTime = 0;

    public Point cur; // Точка на карте, где находится игрок.
    double speed = 0.07;
    private SpriteSet set;

    // Movment
    boolean isMove;
    long begTime;
    Point beg;
    Point end;

    // Temp for animation
    boolean resetStandAnimationTimer = false;

    // For text cloud
    BufferedImage textCloud = null;
    String text = "";
    boolean updateTextCloud = false;

    public void setLastNukeTime(long time) {
        lastNukeTime = time;
    }

    public boolean allowNuke() {
        return Math.abs(System.currentTimeMillis() - WalkingDesu.serverStartTime) - lastNukeTime > NukeBolt.reuse ? true : false;
    }

    public void doHit(int dmg) {
        hitPoints -= dmg;
        if (hitPoints < 0) {
            hitPoints = 0;
        }
    }

    public void doHeal(int val) {
        hitPoints += val;
    }

    public void teleportTo(int x, int y) {
        cur.move(x, y);
        beg.move(x, y);
    }

    public void setNick(String n) {
        nick = n;
    }

    public String getNick () {
        return nick;
    }

    public void setText(String t) {
        text = t;
    }

    public void shouldUpdateTextCloud() {
        updateTextCloud = true;
    }

    public BufferedImage getTextCloud() {
        if (!updateTextCloud) {
            return textCloud;
        }
        if (text.equals("")) {
            return null;
        } else {
            textCloud = new BufferedImage(150, 100, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics g = textCloud.getGraphics();

            LineBreakMeasurer lineMeasurer;
            int paragraphStart;
            int paragraphEnd;
            float breakWidth = 149 - 2;
            float drawPosY = 0;
            Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
            Graphics2D g2d = (Graphics2D)g;
            g2d.setColor(new Color((float)0.1, (float)1.0, (float)0.3, (float)0.7));
            g2d.fillRoundRect(1, 1, 148, 98, 10, 10);
            g2d.setColor(Color.BLACK);
            AttributedCharacterIterator paragraph = (new AttributedString(text)).getIterator();
            FontRenderContext frc;

            map.put(TextAttribute.FAMILY, "Serif");
            map.put(TextAttribute.SIZE, new Float(18.0));
            paragraphStart = paragraph.getBeginIndex();
            paragraphEnd = paragraph.getEndIndex();
            frc = g2d.getFontRenderContext();
            lineMeasurer = new LineBreakMeasurer(paragraph, frc);

            lineMeasurer.setPosition(paragraphStart);
            while (lineMeasurer.getPosition() < paragraphEnd) {
                TextLayout layout = lineMeasurer.nextLayout(breakWidth);
                float drawPosX = layout.isLeftToRight()
                        ? 2 : breakWidth - layout.getAdvance();
                drawPosY += layout.getAscent();
                layout.draw(g2d, drawPosX, drawPosY);
                drawPosY += layout.getDescent() + layout.getLeading();
            }
            updateTextCloud = false;
            return textCloud;
        }
    }

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
            BufferedImage tmp = set.getMovement().getDirection(beg, end).getStandSpr(resetStandAnimationTimer,
                    Math.abs(System.currentTimeMillis() - WalkingDesu.serverStartTime));
            resetStandAnimationTimer = false;
            return tmp;
        }
    }

    private boolean isMove() {
        if (isMove) {
            long curTime = Math.abs(System.currentTimeMillis() - WalkingDesu.serverStartTime);

            if (curTime > begTime) {
                //System.out.print("(" + cur.x + ", " + cur.y + ") -> ");
                cur.x = (int) (beg.x + ((end.x - beg.x) / Math.sqrt(Math.pow(Math.abs(end.x - beg.x), 2) + Math.pow(Math.abs(end.y - beg.y), 2))) * speed * Math.abs(curTime - begTime));
                cur.y = (int) (beg.y + ((end.y - beg.y) / Math.sqrt(Math.pow(Math.abs(end.x - beg.x), 2) + Math.pow(Math.abs(end.y - beg.y), 2))) * speed * Math.abs(curTime - begTime));
                //System.out.println("(" + cur.x + ", " + cur.y + ")");

                if (beg.x > end.x && end.x > cur.x
                        || beg.x < end.x && end.x < cur.x
                        || beg.y > end.y && end.y > cur.y
                        || beg.y < end.y && end.y < cur.y) {
                    //System.out.print("Oops: " + "(" + cur.x + ", " + cur.y + ") fixed to ");
                    cur.x = end.x;
                    cur.y = end.y;
                    //System.out.println("(" + cur.x + ", " + cur.y + ")");
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

		mapImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
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

class YAligner implements Comparator {
    public final int compare(Object a, Object b) {
        return ((Player) a).cur.y > ((Player) b).cur.y ? 1 : 0;
    }
}

class MyListener implements ActionListener {
    Player self;
    JTextField msgField;

    public MyListener(Player p, JTextField tf) {
        self = p;
        msgField = tf;
    }

    public void actionPerformed(ActionEvent e) {
        if (msgField.getText() != null && !self.text.equals(msgField.getText())) {
            self.text = msgField.getText().length() > 100 ? msgField.getText().substring(0, 99) : msgField.getText();
            self.shouldUpdateTextCloud();
            WalkingDesu.addOutCommand("message \"" + self.text + "\"");
        }
    }
}

class CustomDialog extends JDialog implements ActionListener, PropertyChangeListener, KeyListener {
    private String typedText = "Desu";
    private JTextField textField;

    private JOptionPane optionPane;

    private String btnString1 = "Enter";
    private String btnString2 = "Cancel";

    private DefaultNameTask task = null;

    public String getValidatedText() {
        return typedText;
    }

    public CustomDialog() {
        super(WalkingDesu.getFrame(), true);

        setTitle("Choose nick");

        textField = new JTextField(10);
        textField.setText("Desu");

        String msgString1 = "Select your nick.";
        String msgString2 = "(It must be 3-10 symbols long.)";
        Object[] array = {msgString1, msgString2, textField};

        Object[] options = {btnString1, btnString2};

        optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION, null, options, options[0]);

        setContentPane(optionPane);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ce) {
                textField.requestFocusInWindow();
            }
        });

        textField.addActionListener(this);
        textField.addKeyListener(this);

        optionPane.addPropertyChangeListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        optionPane.setValue(btnString1);
        if (task != null) {
            task.cancel();
            task = null;
            setTitle("Choose nick");
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible()
                && (e.getSource() == optionPane)
                && (JOptionPane.VALUE_PROPERTY.equals(prop)
                || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                return;
            }

            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (btnString1.equals(value)) {
                typedText = textField.getText();
                if (typedText.length() >= 3 && typedText.length() <= 10) {
                    clearAndHide();
                } else {
                    textField.selectAll();
                    JOptionPane.showMessageDialog(CustomDialog.this,
                            "Nick must be 3-10 symbols long.",
                            "Try again",
                            JOptionPane.ERROR_MESSAGE);
                    typedText = "Desu";
                    textField.requestFocusInWindow();
                    if (task != null) {
                        task.cancel();
                        task = null;
                        setTitle("Choose nick");
                    }
                }
            } else {
                if (task != null) {
                    task.cancel();
                    task = null;
                }
                typedText = "Desu";
                clearAndHide();
            }
        }
    }

    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {
        if (task != null) {
            task.cancel();
            task = null;
            setTitle("Choose nick");
        }
    }

    public void clearAndHide() {
        textField.setText(null);
        setVisible(false);
    }

    public void startTimer() {
        task = new DefaultNameTask();
    }

    private class DefaultNameTask extends TimerTask {

        public Timer timer; // TODO Where gentle stop timer?
        private int repeat = 20;
        private int step = 1000; // 1 sec

        public DefaultNameTask() {
            timer = new Timer();
            timer.schedule(this, 0, step);
        }

        public void run(){
            if(--repeat < 0) {
                this.cancel();
                clearAndHide();
                return;
            }
            setTitle("Choose nick. " + repeat + " ...");
        }
    }
}