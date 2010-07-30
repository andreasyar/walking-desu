package server.javatestserver;

import java.awt.Point;

public abstract class Unit {
    private Movement mv;

    private long id;
    private String nick;

    private String text;

    protected int maxHitPoints;
    protected int hitPoints;

    protected String spriteSetName;

    protected Unit(long id, String nick, int maxHitPoints, int x, int y, double speed) {
        this.id = id;
        this.nick = nick;
        this.maxHitPoints = maxHitPoints;
        mv = new Movement(x, y, speed);
    }

// <editor-fold defaultstate="collapsed" desc="Movement works">
    public void move(Point beg, Point end, long begTime) {
        mv.move(beg, end, begTime);
    }

    public boolean isMove() {
        return mv.isMove();
    }

    public double getSpeed() {
        return mv.getSpeed();
    }

    public void setSpeed(double speed) {
        mv.setSpeed(speed);
    }

    public Point getCurPos() {
        return mv.getCurPos();
    }

    public Point getDest() {
        return mv.getDest();
    }
// </editor-fold>
    public long getID() {
        return id;
    }
// <editor-fold defaultstate="collapsed" desc="Nick works">
    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Message works">
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="HP works">
    abstract public void doHit(int dmg);

    public int getHitPoints() {
        return hitPoints;
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Sprite set name works">
    public void setSpriteSetName(String spriteSetName) {
        this.spriteSetName = spriteSetName;
    }

    public String getSpriteSetName() {
        return spriteSetName;
    }
// </editor-fold>
}
