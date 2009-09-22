import java.applet.Applet;
import java.awt.Graphics;
import java.net.URL;
import java.awt.event.*;
import java.awt.Image;
import java.awt.image.*;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Point;
import java.util.Random;

public class WalkingDesu extends Applet implements MouseListener, Runnable
{
	private Thread redrawThread = null;

	/** Изображение для двойной буферизации при перерисовке. */
	Image buffImage;
    /** Размеры изображения для двойной буферизации. */
	Dimension buffDimension;
    /** Контекст для доступа к изображению для двойной буферизации. */
	Graphics buffGraphics;

	/** Размер рабочей области апплета. */
	private Dimension appletDimention;
	public static int delay = 20;	// 1000 / 20 = 50 fps

	/** Изображение, представляющее карту. */
	public static BufferedImage map;
	/** Размеры карты. */
	public static Dimension mapDimension;
	/** Контекст для доступа к карте. */
	public static Graphics mapGraphics;

	Player enemy;
	Player enemy1;
	Player self;

	public void run()
	{
		boolean redraw = true;
		while(Thread.currentThread() == redrawThread)
		{
			if(!enemy.isMove() && !self.isMove())
			{
				/* Игроки не двигаются. Значит незачем больше принудительно
				 перерисовывать игровое поле. Однако нужно нарисовать
				 стоящие на месте фигуры игроков, если вдруг мы этого не
				 успели в предыдущей итерации. */
				redraw = false;
				repaint();
			}
			else
			{
				redraw = true;
			}
			if(redraw)
			{
				repaint();
			}
			try
			{
				Thread.sleep(delay);
			}
			catch(InterruptedException e)
			{
				repaint();
				System.out.println(e.getMessage());
				return;
			}
		}
	}

	@Override
	public void init()
	{
		System.out.println("initializing... ");
		addMouseListener(this);
		/* Загрузка наборов спрайтов. */
		SouthDesu.init(getCodeBase(), this);
		SouthWestDesu.init(getCodeBase(), this);
		SouthEastDesu.init(getCodeBase(), this);
		NorthDesu.init(getCodeBase(), this);
		NorthWestDesu.init(getCodeBase(), this);
		NorthEastDesu.init(getCodeBase(), this);

		appletDimention = getSize();		
		/* Создание изображения для двойной буферизации. */
		buffDimension = appletDimention;
		buffImage = createImage(appletDimention.width, appletDimention.height);
		buffGraphics = buffImage.getGraphics();
		/* Создание карты. */
		mapDimension = new Dimension(800, 600);
		map = (BufferedImage)createImage(mapDimension.width,
				mapDimension.height);
		mapGraphics = map.getGraphics();
		mapGraphics.setColor(Color.gray);
		mapGraphics.fillRect(0, 0, appletDimention.width - 1,
				appletDimention.height - 1);
		/* Создание игроков. */
		enemy = new Player(400, 300, 2);
		enemy.initMovement(new AbsoluteMovement(400, 300, enemy));
		enemy1 = new Player(400, 300, 2);
		enemy1.initMovement(new AbsoluteMovement(400, 300, enemy1));
		self = new Player(400, 300, 2);
		self.initMovement(new AbsoluteMovement(400, 300, self));
	}

	@Override
	public void start()
	{
		System.out.println("starting... ");
		if(redrawThread == null)
			redrawThread = new Thread(this);
		redrawThread.start();
	}

	@Override
	public void stop()
	{
		System.out.println("stopping... ");
		redrawThread = null;
	}

	@Override
	public void destroy()
	{
		System.out.println("preparing for unloading...");
	}

	@Override
	public void update(Graphics g)
	{
		buffGraphics.setColor(getBackground());
		buffGraphics.fillRect(0, 0, buffDimension.width, buffDimension.height);
		buffGraphics.setColor(Color.BLACK);
		buffGraphics.drawRect(0, 0, appletDimention.width - 1,
				appletDimention.height - 1);
		int shiftX = self.getPosition().x - 400;
		int shiftY = self.getPosition().y - 300;
		buffGraphics.drawImage(map, -shiftX, -shiftY, null);
		buffGraphics.drawImage(enemy.getSprite(),
				enemy.getPosition().x - SpriteSet.getSpriteXOffset() - shiftX,
				enemy.getPosition().y - SpriteSet.getSpriteYOffset() - shiftY, null);
		buffGraphics.drawImage(enemy1.getSprite(),
				enemy1.getPosition().x - SpriteSet.getSpriteXOffset() - shiftX,
				enemy1.getPosition().y - SpriteSet.getSpriteYOffset() - shiftY, null);
		buffGraphics.drawImage(self.getSprite(),
				400 - SpriteSet.getSpriteXOffset(),
				300 - SpriteSet.getSpriteYOffset(), null);
		g.drawImage(buffImage, 0, 0, null);
	}

	@Override
	public void paint(Graphics g)
	{
		update(g);
	}

	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseClicked(MouseEvent event)
	{
		Point p = event.getPoint();
		if(redrawThread.isAlive())
		{
			redrawThread.interrupt();
		}
		redrawThread = new Thread(this);
		redrawThread.start();
		Random r = new Random();
		enemy.Move(r.nextInt(700),r.nextInt(500));
		enemy1.Move(r.nextInt(700),r.nextInt(500));
		self.Move(p.x, p.y);
	}
}

class Player
{
	/** Положение игрока. */
	Point position;
	/** Делиель для задания числа шагов прохождения пути. Путь будет пройден
	 * за Длина пути в пикселях / v шагов. */
	private int v;
	/** Размер текущего спрайта. */
	Dimension spriteDimension;
	/** Текущий спрайт. */
	private Image sprite;
	/** Тип движения игрока. Необходим для движения игрока. */
	private Movement movement;
	/** Тред, в реальном времени изменяющий координаты игрока при движении. */
	private Thread movementThread;

	Player(int x, int y, int v)
	{
		position = new Point(x, y);
		this.v = v;
		this.sprite = SouthDesu.getInstance().getStand();
		spriteDimension = new Dimension(sprite.getWidth(null),
										sprite.getHeight(null));
		/*movement = new AbsoluteMovement(x, y, x, y, this);
		movementThread = new Thread(movement);*/
	}
	/** Перемещает игрока из текущей точки в точку (endX, endY) */
	public void Move(int endX, int endY)
	{
		if(movementThread.isAlive())
		{
			/* Если движение всё ещё идёт, то остановим его. */
			movementThread.interrupt();
		}
		movement.setCoords(position.x, position.y, endX, endY);
		movementThread = new Thread(movement);
		movementThread.start();
	}
	/** Возвращает делиель для задания числа шагов прохождения пути. */
	public int getV()
	{
		return v;
	}
	/** Возвращает текущее положение игрока. */
	public Point getPosition()
	{
		return setPosition(null);
	}
	/** Устанавливает текущее положение игрока в p и возвращает новое положение.
	 Если p равно null то просто возвращает текущее положение. */
	public synchronized Point setPosition(Point p)
	{
		if(p == null)
		{
			return position;
		}
		if(position != p)
		{
			position.setLocation(p.x, p.y);
		}
		return position;
	}
	/** Возвращает текущий спрайт игрока. */
	public Image getSprite()
	{
		return setSprite(null);
	}
	/** Устанавливает текущий спрайт игрока в sprite и возвращает его. Если
	 sprite равен null, то просто возвращает текущий спрайт. */
	public synchronized Image setSprite(Image sprite)
	{
		if(sprite == null)
		{
			return this.sprite;
		}
		if(this.sprite != sprite)
		{
			this.sprite = sprite;
			spriteDimension.setSize(sprite.getWidth(null),
									sprite.getHeight(null));
		}
		return this.sprite;
	}
	/** Двигается ли игрок в данный момент. */
	public boolean isMove()
	{
		if(movementThread == null)
		{
			return false;
		}
		return movementThread.isAlive();
	}
	/** Инициализирует тип движения игрока. */
	public void initMovement(Movement m)
	{
		if(movement != null)
		{
			/* Инициализация проходит лишь однажды. */
			return;
		}
		movement = m;
		movementThread = new Thread(movement);
	}
}

abstract class Movement implements Runnable
{
	/** Набор спрайтов для движения. */
	SpriteSet set;
	/** Игрок. */
	Player player;
	/** Координаты начала движения. */
	Point begin;
	/** Координаты конца движения. */
	Point end;
	/** Делиель для задания числа шагов прохождения пути. Путь будет пройден
	 * за (Длина пути в пикселях / v) шагов. */
	int v;

	abstract public void run();
	/** Проверяет корректность коррдинат. */
	abstract boolean isValidCoords(Point p);
	/** Устанавливает координаты начала и конца движения. */
	public void setCoords(int beginX, int beginY, int endX, int endY)
	{
		if(begin == null)
		{
			begin = new Point(beginX, beginY);
		}
		else
		{
			begin.x = beginX;
			begin.y = beginY;
		}
		if(end == null)
		{
			end = new Point(endX, endY);
		}
		else
		{
			end.x = endX + (beginX - 400);
			if(end.x < 0)
			{
				end.x = 0;
			}
			else if(end.x >= 800)
			{
				end.x = 799;
			}
			end.y = endY + (beginY - 300);
			if(end.y < 0)
			{
				end.y = 0;
			}
			else if(end.y >= 600)
			{
				end.y = 599;
			}
		}
	}
	/** Ищет нужный набор спрайтов, исходя из направления движения от точки
	 begin к точке end. */
	public SpriteSet getSpriteSet()
	{
		if(begin == null && end == null)
		{
			return null;
		}

		int diffX = end.x - begin.x;
		int diffY = end.y - begin.y;
		if(diffX > 0 && diffY <= 0 && diffX > -diffY)
		{
			return NorthEastDesu.getInstance();
		}
		else if(diffX > 0 && diffY < 0 && diffX <= -diffY)
		{
			return NorthDesu.getInstance();
		}
		else if(diffX <= 0 && diffY < 0 && diffX > diffY)
		{
			return NorthDesu.getInstance();
		}
		else if(diffX < 0 && diffY < 0 && diffX <= diffY)
		{
			return NorthWestDesu.getInstance();
		}
		else if(diffX < 0 && diffY >= 0 && -diffX > diffY)
		{
			return SouthWestDesu.getInstance();
		}
		else if(diffX < 0 && diffY > 0 && -diffX <= diffY)
		{
			return SouthDesu.getInstance();
		}
		else if(diffX >= 0 && diffY > 0 && diffX < diffY)
		{
			return SouthDesu.getInstance();
		}
		else if(diffX > 0 && diffY > 0 && diffX >= diffY)
		{
			return SouthEastDesu.getInstance();
		}
		else if(diffX == 0 && diffY == 0)
		{
			return SouthDesu.getInstance();	// !
		}
		else
		{
			return null;
		}
	}
}

class AbsoluteMovement extends Movement
{
	AbsoluteMovement(int beginX, int beginY, Player player)
	{
		this.player = player;
		v  = player.getV();
		setCoords(beginX, beginY, beginX, beginY);
	}

	public void run()
	{
		System.out.println(">>>" + isValidCoords(begin) + " " + isValidCoords(end) + ">>>" + begin.toString() + " " + end.toString());
		/* Длина пути. */
		Double length = begin.distance(end);
		/* Число итераций. */
		int iterations = (int)(length / v);
		/* Обратный отсчет итераций. */
		int iterationsCounter = iterations;
		/* Интервал, через который будут меняться спрайты движения. */
		double imageRepeat = 10;
		/* Счетчик числа интервалов, через которые будут меняться спрайты
		 движения. */
		double imageRepeatCounter = 1;
		/* Индекс текущего спрайта движения. 0 или 1. */
		int spriteIndex = 0;
		/* Текуще положение игрока. */
		double currentX = (double)begin.x;
		double currentY = (double)begin.y;
		/* Текуще положение игрока в пикселях. */
		Point current = new Point(begin);
		/* Приращение координат игрока. */
		double deltaX = (end.x- begin.x) * (1 / (double)iterations);
		double deltaY = (end.y - begin.y) * (1 / (double)iterations);
		/* Приращение пути. */
		double deltaL = (double)length / (double)iterations;
		/* Длина пройденного пути. */
		double currentLength = 0.0;
		/* Выбор подходящего набора спрайтов. */
		set = getSpriteSet();
		/* Поворачиваем игрока в нужную сторону. */
		player.setSprite(set.getStand());
		/* Начинаем движение. */
		while(iterationsCounter > 0 && isValidCoords(current))
		{
			currentLength += deltaL;
			if(currentLength >= imageRepeatCounter * imageRepeat)
			{
				/* Закончился очередной цикл показа одного из спрайтов,
				 надо показывать другой и начать цикл снова. */
				imageRepeatCounter++;
				spriteIndex = (spriteIndex == 0) ? 1 : 0;
			}
			currentX += deltaX;
			currentY += deltaY;
			current.setLocation(currentX, currentY);
			player.setPosition(current);
			player.setSprite(set.getStep(spriteIndex));
			try
			{
				Thread.sleep(WalkingDesu.delay);
			}
			catch(InterruptedException e)
			{
				player.setPosition(current);
				player.setSprite(set.getStand());
				System.out.println(e.getMessage());
				return;
			}
			iterationsCounter--;
		}
		player.setPosition(current);
		player.setSprite(set.getStand());
		System.out.println("Movement complete");
	}

	boolean isValidCoords(Point p)
	{
		/* Не можем выйти за пределы карты. */
		if(p == null
				|| p.x < 0
				|| p.x > WalkingDesu.mapDimension.width
				|| p.y < 0
				|| p.y > WalkingDesu.mapDimension.height)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}

abstract class SpriteSet
{
	private Image[] movement;
	private Image stand;
	private int currentFrame;
	private static SouthWestDesu instance;

	public abstract Image getStep(int num);
	public abstract Image getStand();
	public abstract int getStepCount();
	public static int getSpriteXOffset() { return 75; }
	public static int getSpriteYOffset() { return 175; }
}

class SouthWestDesu extends SpriteSet
{
	Image[] movement;
	Image stand;
	int currentFrame;
	private static SouthWestDesu instance = null;

	private SouthWestDesu(URL codeBase, WalkingDesu player)
	{
		this.currentFrame = 0;
		this.movement = new Image[2];
		this.stand = player.getImage(codeBase, "img/south_west_03.png");
		this.movement[0] = player.getImage(codeBase, "img/south_west_01.png");
		this.movement[1] = player.getImage(codeBase, "img/south_west_02.png");
	}

	public static void init(URL codeBase, WalkingDesu player)
	{
		if(instance == null)
			instance = new SouthWestDesu(codeBase, player);
	}

	public static SouthWestDesu getInstance()
	{
		return instance;
	}

	public Image getStep(int num)
	{
		if(num > movement.length || num < 0)
			return stand;
		else
			return movement[num];
	}

	public Image getStand()
	{
		return stand;
	}

	public int getStepCount()
	{
		return movement.length;
	}
}

class SouthDesu extends SpriteSet
{
	Image[] movement;
	Image stand;
	int currentFrame;
	private static SouthDesu instance = null;

	private SouthDesu(URL codeBase, WalkingDesu client)
	{
		this.currentFrame = 0;
		this.movement = new Image[2];
		this.stand = client.getImage(codeBase, "img/south_01.png");
		this.movement[0] = client.getImage(codeBase, "img/south_04.png");
		this.movement[1] = client.getImage(codeBase, "img/south_05.png");
	}

	public static void init(URL codeBase, WalkingDesu client)
	{
		if(instance == null)
			instance = new SouthDesu(codeBase, client);
	}

	public static SouthDesu getInstance()
	{
		return instance;
	}

	public Image getStep(int num)
	{
		if(num > movement.length || num < 0)
			return stand;
		else
			return movement[num];
	}

	public Image getStand()
	{
		return stand;
	}

	public int getStepCount()
	{
		return movement.length;
	}
}

class SouthEastDesu extends SpriteSet
{
	Image[] movement;
	Image stand;
	int currentFrame;
	private static SouthEastDesu instance = null;

	private SouthEastDesu(URL codeBase, WalkingDesu client)
	{
		this.currentFrame = 0;
		this.movement = new Image[2];
		this.stand = client.getImage(codeBase, "img/south_east_03.png");
		this.movement[0] = client.getImage(codeBase, "img/south_east_01.png");
		this.movement[1] = client.getImage(codeBase, "img/south_east_02.png");
	}

	public static void init(URL codeBase, WalkingDesu client)
	{
		if(instance == null)
			instance = new SouthEastDesu(codeBase, client);
	}

	public static SouthEastDesu getInstance()
	{
		return instance;
	}

	public Image getStep(int num)
	{
		if(num > movement.length || num < 0)
			return stand;
		else
			return movement[num];
	}

	public Image getStand()
	{
		return stand;
	}

	public int getStepCount()
	{
		return movement.length;
	}
}

class NorthEastDesu extends SpriteSet
{
	Image[] movement;
	Image stand;
	int currentFrame;
	private static NorthEastDesu instance = null;

	private NorthEastDesu(URL codeBase, WalkingDesu client)
	{
		this.currentFrame = 0;
		this.movement = new Image[2];
		this.stand = client.getImage(codeBase, "img/north_east_03.png");
		this.movement[0] = client.getImage(codeBase, "img/north_east_01.png");
		this.movement[1] = client.getImage(codeBase, "img/north_east_02.png");
	}

	public static void init(URL codeBase, WalkingDesu client)
	{
		if(instance == null)
			instance = new NorthEastDesu(codeBase, client);
	}

	public static NorthEastDesu getInstance()
	{
		return instance;
	}

	public Image getStep(int num)
	{
		if(num > movement.length || num < 0)
			return stand;
		else
			return movement[num];
	}

	public Image getStand()
	{
		return stand;
	}

	public int getStepCount()
	{
		return movement.length;
	}
}

class NorthWestDesu extends SpriteSet
{
	Image[] movement;
	Image stand;
	int currentFrame;
	private static NorthWestDesu instance = null;

	private NorthWestDesu(URL codeBase, WalkingDesu client)
	{
		this.currentFrame = 0;
		this.movement = new Image[2];
		this.stand = client.getImage(codeBase, "img/north_west_03.png");
		this.movement[0] = client.getImage(codeBase, "img/north_west_01.png");
		this.movement[1] = client.getImage(codeBase, "img/north_west_02.png");
	}

	public static void init(URL codeBase, WalkingDesu client)
	{
		if(instance == null)
			instance = new NorthWestDesu(codeBase, client);
	}

	public static NorthWestDesu getInstance()
	{
		return instance;
	}

	public Image getStep(int num)
	{
		if(num > movement.length || num < 0)
			return stand;
		else
			return movement[num];
	}

	public Image getStand()
	{
		return stand;
	}

	public int getStepCount()
	{
		return movement.length;
	}
}

class NorthDesu extends SpriteSet
{
	Image[] movement;
	Image stand;
	int currentFrame;
	private static NorthDesu instance = null;

	private NorthDesu(URL codeBase, WalkingDesu client)
	{
		this.currentFrame = 0;
		this.movement = new Image[2];
		this.stand = client.getImage(codeBase, "img/north_01.png");
		this.movement[0] = client.getImage(codeBase, "img/north_01.png");
		this.movement[1] = client.getImage(codeBase, "img/north_02.png");
	}

	public static void init(URL codeBase, WalkingDesu client)
	{
		if(instance == null)
			instance = new NorthDesu(codeBase, client);
	}

	public static NorthDesu getInstance()
	{
		return instance;
	}

	public Image getStep(int num)
	{
		if(num > movement.length || num < 0)
			return stand;
		else
			return movement[num];
	}

	public Image getStand()
	{
		return stand;
	}

	public int getStepCount()
	{
		return movement.length;
	}
}