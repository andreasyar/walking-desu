package client;

import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Point;

class SpriteSet {

    private ArrayList<Animation> animations;

    public SpriteSet() {
        animations = new ArrayList<Animation>();
        animations.add(0, new Movement());
    }

    public Movement getMovement() {
        return (Movement)animations.get(0);
    }

}

abstract class Animation {

}

class Movement extends Animation {

    private MovementDirection north;
    private MovementDirection northWest;
    private MovementDirection northEast;
    private MovementDirection south;
    private MovementDirection southWest;
    private MovementDirection southEast;

    public Movement() {
        north = new MovementDirection(new String[] {"img/north_01.png"}, new String[] {"img/north_01.png", "img/north_02.png"});
        northWest = new MovementDirection(new String[] {"img/north_west_03.png"}, new String[] {"img/north_west_01.png", "img/north_west_02.png"});
        northEast = new MovementDirection(new String[] {"img/north_east_03.png"}, new String[] {"img/north_east_01.png", "img/north_east_02.png"});
        south = new MovementDirection(new String[] {"img/south_01.png", "img/south_02.png", "img/south_03.png", "img/south_02.png"}, new String[] {"img/south_04.png", "img/south_05.png"});
        southWest = new MovementDirection(new String[] {"img/south_west_03.png"}, new String[] {"img/south_west_01.png", "img/south_west_02.png"});
        southEast = new MovementDirection(new String[] {"img/south_east_03.png"}, new String[] {"img/south_east_01.png", "img/south_east_02.png"});
    }

    public MovementDirection getDirection(Point beg, Point end) {
        int diffX = end.x - beg.x;
        int diffY = end.y - beg.y;

        if (diffX > 0 && diffY <= 0 && diffX > -diffY) {
            return northEast;
        } else if (diffX > 0 && diffY < 0 && diffX <= -diffY) {
            return north;
        } else if (diffX <= 0 && diffY < 0 && diffX > diffY) {
            return north;
        } else if (diffX < 0 && diffY < 0 && diffX <= diffY) {
            return northWest;
        } else if (diffX < 0 && diffY >= 0 && -diffX > diffY) {
            return southWest;
        } else if (diffX < 0 && diffY > 0 && -diffX <= diffY) {
            return south;
        } else if (diffX >= 0 && diffY > 0 && diffX < diffY) {
            return south;
        } else if (diffX > 0 && diffY > 0 && diffX >= diffY) {
            return southEast;
        } else if (diffX == 0 && diffY == 0) {
            return south; // TODO wtf?
        } else {
            return null;
        }
    }

}

class MovementDirection {

    private ArrayList<BufferedImage> stand;
    private ArrayList<BufferedImage> move;

    private int standIndex = 0;
    private int moveIndex = 0;

    // Проходимый путь делится на Шаги с заданным Периодом. На каждом шаге
    // происходит смена спрайта на очередной.
    private final double period = 12.0;
    private BufferedImage curMoveSpr = null;
    private int lastStep = 0;

    // Аналогично проходимому пути, только опирается на время простоя.
    private final long timePeriod = 25;
    private long standBeg = 0;
    private long lastTimeStep = 0;
    private BufferedImage curStandSpr = null;

    public MovementDirection(String[] standS, String[] moveS) {
        stand = new ArrayList<BufferedImage>();

        for (int i = 0; i < standS.length; i++) {
            try {
                stand.add(ImageIO.read(new File(standS[i])));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        move = new ArrayList<BufferedImage>();

        for (int i = 0; i < moveS.length; i++) {
            try {
                move.add(ImageIO.read(new File(moveS[i])));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public BufferedImage getStandSpr(boolean rstTimer, long curTime) {
        long tmp;

        if (rstTimer) {
            standBeg = curTime;
            tmp = 0;
            curStandSpr = stand.get(0);
        }

        tmp = (long) ((curTime - standBeg) / timePeriod);

        if (tmp == 0) {
            lastTimeStep = tmp;
            standIndex = 0;
            curStandSpr = stand.get(standIndex);
        } else if (lastTimeStep != tmp) {
            if (standIndex >= stand.size() || stand.size() == 1) {
                standIndex = 0;
            }
            curStandSpr = stand.get(standIndex++);
            lastTimeStep = tmp;
        }

        return curStandSpr;
    }

    /**
     * Возвращает спрайт из анимации движения в зависимости от пройденного пути.
     * @param len Длина пройденного пути.
     */
    public BufferedImage getMoveSpr(double len) {
        int tmp = (int) (len / period);

        if (tmp == 0) {
            curMoveSpr = move.get(0);
            lastStep = tmp;
            moveIndex = 0;
        } else if (lastStep != tmp) {
            if (moveIndex >= move.size() || move.size() == 1) {
                moveIndex = 0;
            }
            curMoveSpr = move.get(moveIndex++);
            lastStep = tmp;
        }

        return curMoveSpr;
    }

}