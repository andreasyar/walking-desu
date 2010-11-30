package server.javatestserver;

import common.Unit;
import common.Movement;

public abstract class JTSUnit extends Unit {

    /**
     * Sprite set name of this unit. Every sprite set can be reffered by
     * name. If we want to draw this unit on the screen we must load apropriate
     * sprite set. For woolf we load woold sprites, for sexy angel sexy angel
     * sprites, etc. Why sprite defined in class on common package? Because both
     * server and client must know about it. Server can change sprite set if,
     * for example, unit morph to pig or wear/unwear some armor.
     */
    protected String spriteSetName;

    protected int damage;

    /**
     * Create new unit.
     * @param id New unit id.
     * @param nick New unit nick name. See comment for this field for more info.
     * @param maxHitPoints New unit maximum hit points.
     * @param x New unit x position on the world.
     * @param y New unit y position on the world.
     * @param speed New unit movement speed.
     */
    protected JTSUnit(long id, String nick, int maxHitPoints, int x, int y, double speed) {
        this.id = id;
        this.nick = nick;
        this.maxHitPoints = maxHitPoints;
        mv = new Movement(x, y, speed);
    }

    // <editor-fold defaultstate="collapsed" desc="Movement">
    @Override
    public void move(int begX, int begY, int endX, int endY, long begTime) {
        mv.move(begX, begY, endX, endY, begTime);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sprite set name">
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

    // <editor-fold defaultstate="collapsed" desc="Message">
    @Override
    public void setText(String text) {
        this.text = text;
    }
    // </editor-fold>
}
