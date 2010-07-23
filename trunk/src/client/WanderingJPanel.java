package client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.util.ListIterator;
import javax.swing.JPanel;

public class WanderingJPanel extends JPanel implements KeyListener, MouseListener, ComponentListener {
    private boolean wasShown = false;

    private BufferedImage mapImg = WanderingMap.getMapImg();
    private Dimension mapDim = new Dimension(mapImg.getWidth(), mapImg.getHeight());
    private Dimension mapOfst;

    private Dimension panelDim = null;

    private BufferedImage buffImg = null;
    private Graphics buffGraph = null;
    private Dimension buffDim = null;

    private boolean selectMode = false;

    private GameField field = GameField.getInstance();

    public WanderingJPanel() {
        addMouseListener(this);
        addComponentListener(this);
        addKeyListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (field.getSelfPlayer() != null && buffDim != null) { // TODO Concurency issue

            // Поле вне карты цвета фона и по краям черная рамочка в 1 пиксел.
            buffGraph.setColor(getBackground());
            buffGraph.fillRect(0, 0, buffDim.width - 1, buffDim.height - 1);
            buffGraph.setColor(Color.BLACK);
            buffGraph.drawRect(0, 0, buffDim.width - 1, buffDim.height - 1);

            // TODO На самом деле надо рисовать лишь видимую часть карты
            // которую можно получить с помощью метода getSubimage()
            buffGraph.drawImage(mapImg, mapOfst.width, mapOfst.height, null);

            // Вычислим новое смещение карты (временно это делается здесь)
            Point curPos = field.getSelfPlayer().getCurPos();
            if (panelDim.width / 2 != mapOfst.width + curPos.x || panelDim.height / 2 != mapOfst.height + curPos.y) {
                mapOfst.width += panelDim.width / 2 - (curPos.x + mapOfst.width);
                mapOfst.height += panelDim.height / 2 - (curPos.y + mapOfst.height);
            }

            // Да, придётся отсортировать игроков по возрастанию Y координаты
            // чтобы "нижелещаие" спрайты не перекрывали вышележащие.
            // TODO optimization
            for(ListIterator<Unit> li = field.getYSortedUnits().listIterator(); li.hasNext(); ) {
                Unit u = li.next();
                Sprite s = u.getSprite();
                buffGraph.drawImage(s.image, s.x + mapOfst.width, s.y + mapOfst.height, null);
            }

            g.drawImage(buffImg, 0, 0, null);
        }
    }

    public void mouseClicked(MouseEvent e) {
        this.requestFocus();
        int x = e.getX();
        int y = e.getY();

        // Если клик был в пределах карты.
        if (x >= mapOfst.width && x <= mapDim.width + mapOfst.width && y >= mapOfst.height && y <= mapDim.height + mapOfst.height) {
            field.getSelfPlayer().move((Point) field.getSelfPlayer().getCurPos().clone(), new Point(x - mapOfst.width, y - mapOfst.height), System.currentTimeMillis() - ServerInteraction.serverStartTime, 0.07);
        }
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void componentResized(ComponentEvent e) {
        Dimension d = e.getComponent().getSize();

        if (buffImg != null && (buffImg.getWidth() != d.width || buffImg.getHeight() != d.height)) {
            buffImg = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
            buffDim = new Dimension(buffImg.getWidth(), buffImg.getHeight());
            buffGraph = buffImg.getGraphics();
        }

        if (panelDim != null && d != null && mapOfst != null && (panelDim.width != d.width || panelDim.height != d.height)) {
            mapOfst.width += d.width / 2 - panelDim.width / 2;
            mapOfst.height += d.height / 2 - panelDim.height / 2;
        }

        panelDim = d;
    }

    public void componentShown(ComponentEvent e) {
        if (!wasShown) {
            wasShown = true;

            panelDim = getSize();

            // Поскольку наш игрок всегда находится в центре экрана и в начале
            // его положение на карте (0, 0) то точка (0, 0) карты должны быть в
            // центре экрана.
            mapOfst = new Dimension(panelDim.width / 2, panelDim.height / 2);

            // Буфер для двойной буферизации.
            buffImg = new BufferedImage(panelDim.width, panelDim.height, BufferedImage.TYPE_INT_RGB);
            buffGraph = buffImg.getGraphics();
            buffDim = new Dimension(buffImg.getWidth(), buffImg.getHeight());
        }
    }

    public void componentMoved(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}

    public void keyTyped(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void keyReleased(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
