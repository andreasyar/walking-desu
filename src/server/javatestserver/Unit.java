package server.javatestserver;

import client.GameField;
import java.awt.Point;

public abstract class Unit {

    protected Movement mv;
    private long id;
    private String nick;
    private String text;
    protected int maxHitPoints;
    protected int hitPoints;
    protected String spriteSetName;
    protected int damage;

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

    public long getBegTime() {
        return mv.getBegTime();
    }

    public long getEndTime() {
        return mv.getEndTime();
    }

    public Point getEnd() {
        return mv.getEnd();
    }

    public Point getBeg() {
        return mv.getBeg();
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

    public abstract void doHit(int dmg);

    public abstract boolean dead();

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

    public long getNukeAnimationDelay() {
        return 375L;
    }

    public int getDamage() {
        return damage;
    }
}