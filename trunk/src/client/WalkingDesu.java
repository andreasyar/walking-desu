package client;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingWorker;

import java.awt.Point;
import java.awt.Image;
import java.awt.image.*;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.Random;

public class WalkingDesu {

    private static JFrame f;
    private static MyPanel p;

    public static void main(String[] args) {
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

}

class MyPanel extends JPanel {

    private Dimension mapOfst; // Смещение 0,0 карты относительно 0,0 панели.

    private Dimension panelDim = null; // Размер этой панели.

    private BufferedImage buffImg = null;
    private Graphics buffGraph = null;
    private Dimension buffDim = null;

    private Player self = null;
    private ArrayList<Player> players;

    public MyPanel() {

        addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                Dimension d = getSize();
                BufferedImage map = WDMap.getInstance().getMapImg();
                Dimension m = new Dimension(map.getWidth(), map.getHeight());
                int x = e.getX();
                int y = e.getY();

                // Если клик был в пределах карты.
                if (x >= mapOfst.width && x <= m.width + mapOfst.width
                        && y >= mapOfst.height && y <= m.height + mapOfst.height) {

                    // На самом деле тут будет запуск движения. А сейчас мы портуемся.
                    // То есть сразу вычисляется новое смещение карты (движение нашего
                    // игрока) и новая позиция нашего игрока на карте. Так что
                    // методу paintComponent() остаётся только отрисовать карту
                    // и спрайт игрока в новой позиции.
                    System.out.println("Go to: (" + self.cur.x + ", " + self.cur.y + ") -> (" + (x - mapOfst.width) + ", " + (y - mapOfst.height) + ")");

                    // Вычислим новое смещение карты.
                    if (e.getX() > d.width / 2) {
                        mapOfst.width -= e.getX() - d.width / 2;
                    } else {
                        mapOfst.width += d.width / 2 - e.getX();
                    }

                    if (e.getY() > d.height / 2) {
                        mapOfst.height -= e.getY() - d.height / 2;
                    } else {
                        mapOfst.height += d.height / 2 - e.getY();
                    }

                    // Сохраним нашу новую позицию на карте.
                    self.cur.move(d.width / 2 - mapOfst.width, d.height / 2 - mapOfst.height);
                }

                // Когда мы начинаем движение, боты тоже начинают движение в
                // случайную точку на карте.
                Random r = new Random();
                for(int i = 0; i < players.size(); i++) {
                    players.get(i).cur.move(r.nextInt(m.width), r.nextInt(m.height));
                }
            }
        });

        addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e){

                // TODO Update player position. Align map offset.
                Dimension d = e.getComponent().getSize();

                if (buffImg != null
                        && (buffImg.getWidth() != d.width || buffImg.getHeight() != d.height)) {
                    buffImg = new BufferedImage(d.width, d.height,
                            BufferedImage.TYPE_BYTE_INDEXED);
                    buffDim = new Dimension(buffImg.getWidth(),
                            buffImg.getHeight());
                    buffGraph = buffImg.getGraphics();
                }

                if (panelDim != null && d != null // TODO Concurency issue
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
        self = new Player(0, 0);

        // Добавим ботов.
        players = new ArrayList<Player>();
        players.add(new Player(0, 0));
        players.add(new Player(0, 0));
        players.add(new Player(0, 0));
        players.add(new Player(0, 0));
        players.add(new Player(0, 0));

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

            // Рисуем спрайт нашего игрока.
            buffGraph.drawImage(self.getSprite(),
                    self.cur.x + mapOfst.width - self.getSprite().getWidth(null) / 2,
                    self.cur.y + mapOfst.height - self.getSprite().getHeight(null),
                    null);

            // Рисуем ботов.
            for(int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                buffGraph.drawImage(p.getSprite(),
                        p.cur.x + mapOfst.width - p.getSprite().getWidth(null) / 2,
                        p.cur.y + mapOfst.height - p.getSprite().getHeight(null),
                        null);
            }

            g.drawImage(buffImg, 0, 0, null);
        }
    }

}

class Player {

    public Point cur; // Точка на карте, где находится игрок.
    int speed;

    // Movment
    boolean isMove;
    long begTime;
    Point beg;
    Point end;

    // Temp
    private BufferedImage sprite = null;

    public Player(int x, int y) {
        isMove = false;
        try {
            sprite = ImageIO.read(new File("img/south_01.png"));
        } catch (IOException e) {
            e.printStackTrace(); // TODO Crash
        }
        cur = new Point(x, y);
    }

    public Image getSprite() {
        return sprite;
    }

    public boolean isMove() {
        return isMove;
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