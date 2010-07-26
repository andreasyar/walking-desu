package client;

import java.awt.Point;

abstract public class Unit {
    private MovementAnimation moveAnim;
    private StandAnimation standAnim;

    private Movement move;

    private int id;
    private String nick;
    private String text;

    protected int maxHitPoints;
    protected int hitPoints;

    public Unit(int id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
        this.id = id;
        this.nick = nick;
        this.maxHitPoints = maxHitPoints;
        hitPoints = this.maxHitPoints;
        moveAnim = new MovementAnimation(set);
        standAnim = new StandAnimation(set);
        standAnim.run(d, System.currentTimeMillis() - ServerInteraction.serverStartTime);
        move = new Movement(new Point(x, y), speed);
    }

    public void changeSpriteSet(String spriteSet) {
        moveAnim = new MovementAnimation(spriteSet);
        standAnim = new StandAnimation(spriteSet);
        standAnim.run(Direction.SOUTH, ServerInteraction.serverStartTime);
    }

    public Sprite getSprite() {
        return move.isMove() ?
            moveAnim.getSprite(move.getCurPos()) :
            standAnim.getSprite(System.currentTimeMillis() - ServerInteraction.serverStartTime, move.getCurPos());
    }

    public Point getCurPos() {
        return move.getCurPos();
    }

    public void move(Point beg, Point end, long begTime) {
        move.move(beg, end, begTime);
        moveAnim.run(beg, end, 10.0);
        standAnim.run(moveAnim.getDirection(), move.getEndTime());
    }

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

    public void setSpeed(double speed) {
        move.setSpeed(speed);
    }
}
