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
    private long id;

    /**
     * Nick name of this unit. For players it can be name like John or Boxxy.
     * For monsters and NPCs it is a name or class name: Wild woolf, Peter,
     * Teleporter hot girl, Robot etc.
     */
    private String nick;

    /**
     * Object what controls unit movements. Recalculate current coords. etc.
     */
    protected Movement mv;

    /**
     * Sprite set name of this unit. Every sprite set can be reffered by
     * name. If we want to draw this unit on the screen we must load apropriate
     * sprite set. For woolf we load woold sprites, for sexy angel sexy angel
     * sprites, etc. Why sprite defined in class on common package? Because both
     * server and client must know about it. Server can change sprite set if,
     * for example, unit morph to pig or wear/unwear some armor.
     */
    protected String spriteSetName;

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
    private String text;

    /**
     * Create new unit.
     * @param id New unit id.
     * @param nick New unit nick name. See comment for this field for more info.
     * @param maxHitPoints New unit maximum hit points.
     * @param x New unit x position on the world.
     * @param y New unit y position on the world.
     * @param speed New unit movement speed.
     */
    protected Unit(long id, String nick, int maxHitPoints, int x, int y, double speed) {
        this.id = id;
        this.nick = nick;
        this.maxHitPoints = maxHitPoints;
        mv = new Movement(x, y, speed);
    }

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

    public Point getEnd() {
        return mv.getEnd();
    }

    public Point getBeg() {
        return mv.getBeg();
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

    // <editor-fold defaultstate="collapsed" desc="Sprite set name works">
    /**
     * Return sprite set name of this unit. For more info about sprite set name
     * see comment to correspond field of this class.
     */
    public String getSpriteSetName() {
        return spriteSetName;
    }

    /**
     * Set sprite set name of this unit to spriteSetName. For more info about
     * sprite set name see comment to correspond field of this class.
     * It is not ennough to just change a this sprite set name to unit magically
     * morph to some creature. You need to send correspond message to all
     * players what this unit was morphed.
     * @param spriteSetName New sprite set name.
     */
    public void setSpriteSetName(String spriteSetName) {
        this.spriteSetName = spriteSetName;
    }
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

    /**
     * TODO: lol wtf?
     */
    public long getNukeAnimationDelay() {
        return 375L;
    }
}
