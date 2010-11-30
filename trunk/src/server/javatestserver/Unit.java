package server.javatestserver;

//import java.awt.Point;
//
//import common.Movement;
//
//public abstract class Unit {
//
//    private long id;
//
//    private String nick;
//
//    protected Movement mv;
//
//    /**
//     * Sprite set name of this unit. Every sprite set can be reffered by
//     * name. If we want to draw this unit on the screen we must load apropriate
//     * sprite set. For woolf we load woold sprites, for sexy angel sexy angel
//     * sprites, etc. Why sprite defined in class on common package? Because both
//     * server and client must know about it. Server can change sprite set if,
//     * for example, unit morph to pig or wear/unwear some armor.
//     */
//    protected String spriteSetName;
//
//    protected int maxHitPoints;
//
//    protected int hitPoints;
//
//    private String text;
//
//    protected int damage;
//
//    /**
//     * Create new unit.
//     * @param id New unit id.
//     * @param nick New unit nick name. See comment for this field for more info.
//     * @param maxHitPoints New unit maximum hit points.
//     * @param x New unit x position on the world.
//     * @param y New unit y position on the world.
//     * @param speed New unit movement speed.
//     */
//    protected Unit(long id, String nick, int maxHitPoints, int x, int y, double speed) {
//        this.id = id;
//        this.nick = nick;
//        this.maxHitPoints = maxHitPoints;
//        mv = new Movement(x, y, speed);
//    }
//
//    public long getID() {
//        return id;
//    }
//
//    // <editor-fold defaultstate="collapsed" desc="Nick works">
//    public String getNick() {
//        return nick;
//    }
//
//    public void setNick(String nick) {
//        this.nick = nick;
//    }
//    // </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="Movement works">
//    public boolean isMove() {
//        return mv.isMove();
//    }
//
//    public Point getBeg() {
//        return mv.getBeg();
//    }
//
//    public Point getEnd() {
//        return mv.getEnd();
//    }
//
//    public Point getCurPos() {
//        return mv.getCurPos();
//    }
//
//    public long getBegTime() {
//        return mv.getBegTime();
//    }
//
//    public long getEndTime() {
//        return mv.getEndTime();
//    }
//
//    public double getSpeed() {
//        return mv.getSpeed();
//    }
//
//    public void setSpeed(double speed) {
//        mv.setSpeed(speed);
//    }
//
//    public void move(Point beg, Point end, long begTime) {
//        mv.move(beg.x, beg.y, end.x, end.y, begTime);
//    }
//    // </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="Sprite set name works">
//    public void setSpriteSetName(String spriteSetName) {
//        this.spriteSetName = spriteSetName;
//    }
//
//    public String getSpriteSetName() {
//        return spriteSetName;
//    }
//    // </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="HP works">
//    public abstract boolean dead();
//
//    public abstract void doHit(int dmg);
//
//    public int getHitPoints() {
//        return hitPoints;
//    }
//    // </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="Message works">
//    public String getText() {
//        return text;
//    }
//
//    public void setText(String text) {
//        this.text = text;
//    }
//    // </editor-fold>
//
//    public long getNukeAnimationDelay() {
//        return 375L;
//    }
//
//    public int getDamage() {
//        return damage;
//    }
//}
