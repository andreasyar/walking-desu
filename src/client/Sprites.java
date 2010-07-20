package client;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;

/*class SpriteSet {

    private ArrayList<Animation> animations;

    public SpriteSet() {
        animations = new ArrayList<Animation>();
        animations.add(0, new Movement());
    }

    public Movement getMovement() {
        return (Movement)animations.get(0);
    }

}*/

class Sprite {
    public BufferedImage image;
    public int x;
    public int y;
}

enum Direction {
    NORTH,
    NORTH_WEST,
    NORTH_EAST,
    SOUTH,
    SOUTH_WEST,
    SOUTH_EAST;

    public static Direction getDirection(Point beg, Point end) {
        int diffX = end.x - beg.x;
        int diffY = end.y - beg.y;

        if (diffX > 0 && diffY <= 0 && diffX > -diffY) {
            return NORTH_EAST;
        } else if (diffX > 0 && diffY < 0 && diffX <= -diffY) {
            return NORTH;
        } else if (diffX <= 0 && diffY < 0 && diffX > diffY) {
            return NORTH;
        } else if (diffX < 0 && diffY < 0 && diffX <= diffY) {
            return NORTH_WEST;
        } else if (diffX < 0 && diffY >= 0 && -diffX > diffY) {
            return SOUTH_WEST;
        } else if (diffX < 0 && diffY > 0 && -diffX <= diffY) {
            return SOUTH;
        } else if (diffX >= 0 && diffY > 0 && diffX < diffY) {
            return SOUTH;
        } else if (diffX > 0 && diffY > 0 && diffX >= diffY) {
            return SOUTH_EAST;
        } else if (diffX == 0 && diffY == 0) {
            return SOUTH; // TODO wtf?
        } else {
            return null;
        }
    }
}

class MovementAnimation {
    private DirectionalSpriteSet set;
    private Movement mov;

    private Point beg;
    private double step;
    private Direction direct;
    private int period;
    private int sprIndex;
    private double stepCount;

    public MovementAnimation(String spriteSet, Movement mov) {
        this.set = DirectionalSpriteSet.load(spriteSet + "_walk");
        this.mov = mov;
    }

    public final Sprite getSprite(Point cur) {
        double tmpCount = beg.distance(cur) / step;
        Sprite tmpSpr;

        if (stepCount < tmpCount) {
            stepCount = tmpCount;
            sprIndex = (int) stepCount % period;
            System.out.println("sprIndex: " + sprIndex);
        }
        tmpSpr = set.getSprite(direct, sprIndex);
        return tmpSpr;
    }

    public void run(Point beg, Point end, double step) {
        direct = Direction.getDirection(beg, end);
        period = set.getSpriteCount(direct);
        this.beg = beg;
        this.step = step;
        sprIndex = 0;
        stepCount = 0.0;
    }
}

class StandAnimation {
    private DirectionalSpriteSet set;

    public StandAnimation(DirectionalSpriteSet set) {}

    public final Sprite getSprite() {
        Sprite tmp = set.getSprite(Direction.NORTH, 0);
        tmp.x = 0;
        tmp.y = 0;
        return tmp;
    }

    public void run() {}
}

class DirectionalSpriteSet {
    private static HashMap<String, DirectionalSpriteSet> cache = new HashMap<String, DirectionalSpriteSet>();
    private HashMap<Direction, ArrayList<BufferedImage>> sprites = new HashMap<Direction, ArrayList<BufferedImage>>();
    private Sprite curSpr = new Sprite();

    public static DirectionalSpriteSet load(String name) {
        if (cache.containsKey(name)) {
            return (DirectionalSpriteSet) cache.get(name);
        } else {
            DirectionalSpriteSet set = new DirectionalSpriteSet(name);
            cache.put(name, set);
            return set;
        }
    }

    private DirectionalSpriteSet(String name) {
        for (Direction d:Direction.values()) {
            sprites.put(d, new ArrayList<BufferedImage>());
        }

        loadSprFiles(new String[] {"img/" + name + "/north_01.png", "img/" + name + "/north_02.png", });
        loadSprFiles(new String[] {"img/" + name + "/north_east_01.png", "img/" + name + "/north_east_02.png", });
        loadSprFiles(new String[] {"img/" + name + "/north_west_01.png", "img/" + name + "/north_west_02.png", });
        loadSprFiles(new String[] {"img/" + name + "/south_04.png", "img/" + name + "/south_05.png", });
        loadSprFiles(new String[] {"img/" + name + "/south_east_01.png", "img/" + name + "/south_east_02.png", });
        loadSprFiles(new String[] {"img/" + name + "/south_west_01.png", "img/" + name + "/south_west_02.png", });
    }

    public Sprite getSprite(Direction d, int index) {
        curSpr.image = sprites.get(d).get(index);
        return curSpr;
    }

    public int getSpriteCount(Direction d) {
        return sprites.get(d).size();
    }

    private void loadSprFiles(String[] paths) {
        ClassLoader cl = this.getClass().getClassLoader();
        URL url = null;

        for (int i = 0; i < paths.length; i++) {
            try {
                url = cl.getResource(paths[i]);
                if (url != null) {
                    sprites.get(Direction.NORTH).add(ImageIO.read(url));
                } else {
                    sprites.get(Direction.NORTH).add(ImageIO.read(new File(paths[i])));
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}

/*abstract class Animation {
    protected ArrayList<BufferedImage> sprites;

    public void loadSprites(String[] paths) {
        ClassLoader cl = this.getClass().getClassLoader();
        URL url = null;

        for (int i = 0; i < paths.length; i++) {
            try {
                url = cl.getResource(paths[i]);
                if (url != null) {
                    sprites.add(ImageIO.read(url));
                } else {
                    sprites.add(ImageIO.read(new File(paths[i])));
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}

class DistanceBasedAnimation extends Animation {
    // TODO May be privete?
    protected Point beg;
    protected Point end;
    protected double step;

    protected ArrayList<BufferedImage> northSprites = new ArrayList<BufferedImage>();
    protected ArrayList<BufferedImage> northWestSprites = new ArrayList<BufferedImage>();
    protected ArrayList<BufferedImage> northEastSprites = new ArrayList<BufferedImage>();
    protected ArrayList<BufferedImage> southSprites = new ArrayList<BufferedImage>();
    protected ArrayList<BufferedImage> southWestSprites = new ArrayList<BufferedImage>();
    protected ArrayList<BufferedImage> southEastSprites = new ArrayList<BufferedImage>();

    public DistanceBasedAnimation(Point beg, Point end, double step) {

    }

    public BufferedImage getSprite(Point cur) {
        return null;
    }
}

class UnitMovementAnimation extends DistanceBasedAnimation {
    public UnitMovementAnimation(Point beg, Point end, double step) {
        super(beg, end, step);
    }
}*/

/*class NukeBoltMovement extends Animation {
    public BufferedImage getSprite() {
        return null;
    }
    private static final String[] moveS = new String[] {"img/power_of_watering_pot_01.png"};
    private static ArrayList<BufferedImage> move = null;

    private int moveIndex = 0;

    private final long timePeriod = 75;
    private long begTime = 0;
    private long lastTimeStep = 0;
    private BufferedImage curMoveSpr = null;

    public NukeBoltMovement(long time) {
        begTime = time;
        if (move == null) {
            move = new ArrayList<BufferedImage>();
            ClassLoader cl = this.getClass().getClassLoader();
            URL url = null;

            move = new ArrayList<BufferedImage>();

            for (int i = 0; i < moveS.length; i++) {
                try {
                    url = cl.getResource(moveS[i]);
                    if (url != null) {
                        move.add(ImageIO.read(url));
                    } else {
                        move.add(ImageIO.read(new File(moveS[i])));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }

    public BufferedImage getSprite(long curTime) {
        long tmp;

        tmp = (long) ((curTime - begTime) / timePeriod);

        if (tmp == 0) {
            lastTimeStep = tmp;
            moveIndex = 0;
            curMoveSpr = move.get(moveIndex);
        } else if (lastTimeStep != tmp) {
            if (moveIndex >= move.size() || move.size() == 1) {
                moveIndex = 0;
            }
            curMoveSpr = move.get(moveIndex++);
            lastTimeStep = tmp;
        }

        return curMoveSpr;
    }
}*/

/*class Movement extends Animation {
    public BufferedImage getSprite() {
        return null;
    }

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

}*/

/*class MovementDirection {

    private ArrayList<BufferedImage> stand;
    private ArrayList<BufferedImage> move;

    private int standIndex = 0;
    private int moveIndex = 0;

    // Проходимый путь делится на Шаги с заданным Периодом. На каждом шаге
    // происходит смена спрайта на очередной.
    private final double period = 10.0;
    private BufferedImage curMoveSpr = null;
    private int lastStep = 0;

    // Аналогично проходимому пути, только опирается на время простоя.
    private final long timePeriod = 75;
    private long standBeg = 0;
    private long lastTimeStep = 0;
    private BufferedImage curStandSpr = null;
    private int delay = 3000;

    public MovementDirection(String[] standS, String[] moveS) {
        stand = new ArrayList<BufferedImage>();
        ClassLoader cl = this.getClass().getClassLoader();
        URL url = null;

        for (int i = 0; i < standS.length; i++) {
            try {
                url = cl.getResource(standS[i]);
                if (url != null) {
                    stand.add(ImageIO.read(url));
                } else {
                    stand.add(ImageIO.read(new File(standS[i])));
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        move = new ArrayList<BufferedImage>();

        for (int i = 0; i < moveS.length; i++) {
            try {
                url = cl.getResource(moveS[i]);
                if (url != null) {
                    move.add(ImageIO.read(url));
                } else {
                    move.add(ImageIO.read(new File(moveS[i])));
                }
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

        if (curTime - standBeg <= delay) {
            curStandSpr = stand.get(0);
            return curStandSpr;
        }

        if (curTime - standBeg > delay + timePeriod * stand.size()) {
            standBeg = curTime;
            tmp = 0;
            curStandSpr = stand.get(0);
            return curStandSpr;
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

}*/