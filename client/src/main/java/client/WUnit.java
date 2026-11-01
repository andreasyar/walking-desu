package client;

import common.items.Item;
import common.Unit;
import common.Movement;
import common.WanderingServerTime;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Graphics;

public abstract class WUnit extends Unit implements Drawable {

    private UnitAnimations animations;

    protected WUnit selectedUnit;

    protected Nuke currentNuke;

    /**
     * Selected item. Item what we want to pick up.
     */
    protected Item selectedItem = null;

    public WUnit(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {

        super();

        this.id = id;
        this.nick = nick;
        this.maxHitPoints = maxHitPoints;

        animations = new UnitAnimations(set, d, now());
        mv = new Movement(x, y, speed);
    }

    // <editor-fold defaultstate="collapsed" desc="Movement">
    @Override
    public synchronized void move(int begX, int begY, int endX, int endY, long begTime) {
        if (begX == endX && begY == endY) {
            mv = new Movement(endX, endY, mv.getSpeed());
            animations.move(begX, begY, endX, endY, begTime);
            return;
        }
        mv.move(begX, begY, endX, endY, begTime);
        animations.move(begX, begY, endX, endY, mv.getEndTime());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sprite and Animation">
    void drawTextCloud(Graphics g, int x, int y) {
        UnitPainter.drawText(this, g, x, y);
    }

    public synchronized boolean deathAnimationDone() {
        return animations.deathDone(now());
    }

    public synchronized void changeSpriteSet(String name) {
        UnitAnimations replacement = new UnitAnimations(name, Direction.SOUTH, now());
        if (dead()) replacement.die(now());
        else if (mv.isMove()) replacement.move(mv.getBeg().x, mv.getBeg().y,
                mv.getEnd().x, mv.getEnd().y, mv.getEndTime());
        animations.dispose();
        animations = replacement;
    }

    public synchronized long getNukeAnimationDelay() {
        return animations.nukeDelay();
    }

    @Override
    public synchronized void kill() {
        animations.die(now());
        mv.stop();
        hitPoints = 0;
    }
    protected synchronized void resetAnimations() { animations.revive(now()); }

    synchronized void releaseVisualState() {
        animations.dispose();
        selectedUnit = null;
        selectedItem = null;
        currentNuke = null;
        text = null;
    }

    static long now() { return WanderingServerTime.getInstance().getTimeSinceStart(); }
    // </editor-fold>

    @Override
    public synchronized void setText(String text) {
        this.text = text;
    }

    // <editor-fold defaultstate="collapsed" desc="Selected unit">
    public synchronized WUnit getSelectedUnit() {
        return selectedUnit;
    }

    public synchronized void selectUnit(WUnit unit) {
        selectedUnit = unit;
    }

    public synchronized void unselectUnit() {
        selectedUnit = null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Current nuke">
    public Nuke getCurrentNuke() {
        return currentNuke;
    }

    public void setCurrentNuke(Nuke currentNuke) {
        this.currentNuke = currentNuke;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Attack">
    public synchronized boolean attack(long begTime) {
        if (dead() || isAttack() || selectedUnit == null) return false;
        if (mv.isMove()) mv.stop();
        return animations.attack(Direction.getDirection(getCurPos(), selectedUnit.getCurPos()), begTime);
    }

    public synchronized boolean isAttack() {
        return animations.isAttacking(now());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Selected item">
    /**
     * Returns selected item.
     * @return Selected item.
     */
    public Item getSelectedItem() {
        return selectedItem;
    }

    /**
     * Selects <i>item</i>.
     * @param item New selected item.
     */
    public void selectItem(Item item) {
        this.selectedItem = item;
    }

    /**
     * Unselect item.
     */
    public void unselectItem() {
        selectItem(null);
    }

    /**
     * Return is item selected or not.
     * @return Is item selected or not.
     */
    public boolean isItemSelected() {
        return selectedItem == null;
    }

    /**
     * Unlesect <i>item</i> if it selected. If anoter item selected do nothig.
     * @param item Item to unselect.
     */
    public void unselectItem(Item item) {
            if (selectedItem == item) {
                selectedItem = null;
            }
        }
    // </editor-fold>

    /**
     * Draw unit.
     * @param g context for drawing.
     * @param x x-axis of left upper corner of drawing context on world map.
     * @param y y-axis of left upper corner of drawing context on world map.
     * @param d dimensions of drawing context on world map.
     */
    @Override
    public void draw(Graphics g, int x, int y, Dimension d) {
        Sprite sprite = UnitPainter.sprite(this, g);
        if (sprite != null) g.drawImage(sprite.image, sprite.x - x, sprite.y - y, null);
    }

    @Override
    public synchronized Sprite getSprite() {
        return spriteAt(now());
    }

    synchronized Sprite spriteAt(long time) {
        boolean moving = mv.isMove();
        Point position = new Point(mv.getCurPos());
        return animations.sprite(time, position, moving, dead());
    }
}
