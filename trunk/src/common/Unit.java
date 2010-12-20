package common;

import common.skills.Skill;
import java.awt.Point;
import java.util.ArrayList;

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
     * Sprite set name of this unit. Every sprite set can be reffered by
     * name. If we want to draw this unit on the screen we must load apropriate
     * sprite set. For woolf we load woold sprites, for sexy angel sexy angel
     * sprites, etc. Why sprite defined in class on common package? Because both
     * server and client must know about it. Server can change sprite set if,
     * for example, unit morph to pig or wear/unwear some armor.
     */
    protected String spriteSetName;

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
     * Unit skills.
     */
    private ArrayList<Skill> skills = new ArrayList<Skill>();

    /**
     * Return id of this unit.
     */
    public long getID() {
        return id;
    }

    // <editor-fold defaultstate="collapsed" desc="Nick.">
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

    // <editor-fold defaultstate="collapsed" desc="Sprite set name.">
    /**
     * Returns sprite set name.
     */
    public String getSpriteSetName() {
        return spriteSetName;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Movement.">
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

    /**
     * Return start time of movement since server started.
     */
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

    // <editor-fold defaultstate="collapsed" desc="Hit points.">
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

    // <editor-fold defaultstate="collapsed" desc="Text.">
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

    // <editor-fold defaultstate="collapsed" desc="Skills.">
    /**
     * Return <b>true</b> if unit use skill now, <b>false</b> otherwise.
     */
    public boolean isUseSkill() {
        return false;
    }

    /**
     * Return skill what unit use now, <b>null</b> otherwise.
     */
    public Skill skillInUse() {
        return null;
    }
    // </editor-fold>
}
