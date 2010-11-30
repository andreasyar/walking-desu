package common;

import java.awt.Point;

/**
 * Unit is a main abstraction for all alive creatures in the Wandering world.
 * Player, Monster, NPC, etc - this is all Units.
 */
public abstract class Unit {

    /**
     * Global identifer of this unit. Everything in Wandering world have one.
     */
    protected long id;

    /**
     * Nick name of this unit. For players it can be name like John or Boxxy.
     * For monsters and NPCs it is a name or class name: Wild woolf, Peter,
     * Teleporter hot girl, Robot etc.
     */
    protected String nick;

    /**
     * Object what controls unit movements. Recalculate current coords. etc.
     */
    protected Movement mv;

    /**
     * Maximum hit points of this unit.
     */
    protected int maxHitPoints;

    /**
     * Curent hit points of this unit.
     */
    protected int hitPoints;

    /**
     * This text shows in cloud over units head.
     */
    protected String text;

    /**
     * Return id of this unit.
     */
    public long getID() {
        return id;
    }

    // <editor-fold defaultstate="collapsed" desc="Nick works">
    /**
     * Return nick name of this unit.
     */
    public String getNick() {
        return nick;
    }

    /**
     * Set unit nick name to nick.
     * @param nick New nick name for this unit.
     */
    public void setNick(String nick) {
        this.nick = nick;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Movement works">
    public boolean isMove() {
        return mv.isMove();
    }

    public Point getBeg() {
        return mv.getBeg();
    }

    public Point getEnd() {
        return mv.getEnd();
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

    public double getSpeed() {
        return mv.getSpeed();
    }

    public void setSpeed(double speed) {
        mv.setSpeed(speed);
    }

    /**
     * Moves unit.
     * @param begX Start x world coord of movement.
     * @param begY Start y world coord of movement.
     * @param endX End x world coord of movement.
     * @param endY End y world coord of movement.
     * @param begTime Movement begining time since server start.
     */
    public abstract void move(int begX, int begY, int endX, int endY, long begTime);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="HP works">
    /**
     * Check if curret unit is dead.
     */
    public abstract boolean dead();

    /**
     * Hit this unit for specifed damage.
     * @param dmg Damage
     */
    public abstract void doHit(int dmg);

    /**
     * Return current hit points of this unit.
     */
    public int getHitPoints() {
        return hitPoints;
    }

    public abstract void kill();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Text works">
    /**
     * Return text of this unit. See comment for text field of Unit class for
     * more info about text.
     */
    public String getText() {
        return text;
    }

    /**
     * Set text of this unit to text. See comment for text field of Unit class
     * for more info about text.
     * @param text New text.
     */
    public abstract void setText(String text);
    // </editor-fold>
}
