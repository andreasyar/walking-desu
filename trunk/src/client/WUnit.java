package client;

import wand6.client.ServerInteraction;
import wand6.client.WandJPanel;
import common.items.Item;
import common.Unit;
import common.Movement;
import common.WanderingServerTime;

import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class WUnit extends Unit implements Drawable {

    protected MovementAnimation moveAnim;
    protected StandAnimation standAnim;
    protected DeathAnimation deathAnim;
    private UseSkillAnimation useAnim;
    private BufferedImage textCloud = null;
    protected WUnit selectedUnit;
    protected Nuke currentNuke;
    protected PickupAnimation pickupAnim;

    /**
     * Selected item. Item what we want to pick up.
     */
    protected Item selectedItem = null;

    public WUnit(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {

        super();

        this.id = id;
        this.nick = nick;
        this.maxHitPoints = maxHitPoints;

        try {
            useAnim = new UseSkillAnimation(set);
            moveAnim = new MovementAnimation(set);
            standAnim = new StandAnimation(set);
            standAnim.run(d, WanderingServerTime.getInstance().getTimeSinceStart());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        spriteSetName = set;
        mv = new Movement(x, y, speed);
    }

    // <editor-fold defaultstate="collapsed" desc="Movement">
    @Override
    public void move(int begX, int begY, int endX, int endY, long begTime) {
        mv.move(begX, begY, endX, endY, begTime);
        try {
            moveAnim.run(begX, begY, endX, endY, 10.0);
        } catch (Exception ex) {
            Logger.getLogger(WUnit.class.getName()).log(Level.SEVERE, "Looks like there is no spries for this direction.", ex);
            System.exit(1);
        }
        standAnim.run(moveAnim.getDirection(), mv.getEndTime());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sprite and Animation">
    void drawTextCloud(Graphics g, int x, int y) {
        Sprite s = getSprite();

        if (textCloud != null) {
            g.drawImage(textCloud,
                        s.x - x + s.image.getWidth(),
                        s.y - y,
                        null);
        }
    }

    public boolean deathAnimationDone() {
        if (deathAnim != null) {
            return deathAnim.isDone();
        } else {
            return true;
        }
    }

    public void changeSpriteSet(String spriteSetName) {
        moveAnim = new MovementAnimation(spriteSetName);
        standAnim = new StandAnimation(spriteSetName);
        standAnim.run(Direction.SOUTH, ServerInteraction.serverStartTime);
    }

    public long getNukeAnimationDelay() {
        return useAnim.getNukeAnimationDelay();
    }
    // </editor-fold>

    @Override
    public void setText(String text) {
        this.text = text;
        updateTextCloud();
    }

    // <editor-fold defaultstate="collapsed" desc="Text cloud">
    public BufferedImage getTextCloud() {
        return textCloud;
    }

    private void updateTextCloud() {
        if (text == null || text.equals("")) {
            textCloud = null;
        } else {
            textCloud = new BufferedImage(150, 100, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics g = textCloud.getGraphics();

            LineBreakMeasurer lineMeasurer;
            int paragraphStart;
            int paragraphEnd;
            float breakWidth = 149 - 2;
            float drawPosY = 0;
            Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color((float) 0.1, (float) 1.0, (float) 0.3, (float) 0.7));
            g2d.fillRoundRect(1, 1, 148, 98, 10, 10);
            g2d.setColor(Color.BLACK);
            AttributedCharacterIterator paragraph = (new AttributedString(text)).getIterator();
            FontRenderContext frc;

            map.put(TextAttribute.FAMILY, "Serif");
            map.put(TextAttribute.SIZE, new Float(18.0));
            paragraphStart = paragraph.getBeginIndex();
            paragraphEnd = paragraph.getEndIndex();
            frc = g2d.getFontRenderContext();
            lineMeasurer = new LineBreakMeasurer(paragraph, frc);

            lineMeasurer.setPosition(paragraphStart);
            while (lineMeasurer.getPosition() < paragraphEnd) {
                TextLayout layout = lineMeasurer.nextLayout(breakWidth);
                float drawPosX = layout.isLeftToRight()
                        ? 2 : breakWidth - layout.getAdvance();
                drawPosY += layout.getAscent();
                layout.draw(g2d, drawPosX, drawPosY);
                drawPosY += layout.getDescent() + layout.getLeading();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Selected unit">
    public WUnit getSelectedUnit() {
        return selectedUnit;
    }

    public void selectUnit(WUnit unit) {
        selectedUnit = unit;
    }

    public void unselectUnit() {
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
    public boolean attack(long begTime) {
        if (dead() || isAttack()) {
            return false;
        }
        if (mv.isMove()) {
            mv.stop();
        }
        useAnim.run(Direction.getDirection(getCurPos(), selectedUnit.getCurPos()), begTime);
        return true;
    }

    public boolean isAttack() {
        return useAnim != null && !useAnim.isStoped();
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
    public void draw(Graphics g, int x, int y, Dimension d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns sprite for delayed drawing in z-buffer or anoter purpose.
     * @return sprite.
     */
    public Sprite getSprite() {
        Sprite s = null;
        BufferedImage bimage;
        Graphics bimageGraphics;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();
        FontMetrics metrics = WandJPanel.getFontMetrics();

        try {
            boolean isMove = mv.isMove();
            boolean isAttack = isAttack();
            boolean isDead = dead();
            Point cur = mv.getCurPos();

            if (!isDead && isMove) {
                s = moveAnim.getSprite(cur);
            } else if (!isDead && !isMove && !isAttack) {
                s = standAnim.getSprite(WanderingServerTime.getInstance().getTimeSinceStart(), cur);
            } else if (!isDead && !isMove && isAttack) {
                s = useAnim.getSprite(WanderingServerTime.getInstance().getTimeSinceStart(), cur);
            } else if (isDead) {
                s = deathAnim.getSprite(WanderingServerTime.getInstance().getTimeSinceStart(), cur);
            } else {
                System.err.println("Illegal unit state.");
                System.exit(1);
            }

            // Get once. Use all time.
            if (metrics != null) {
                int renderedNickWidth = metrics.stringWidth(getNick());
                int sprImgW = s.image.getWidth();

                if (renderedNickWidth + 2 > sprImgW) {

                    bimage = gc.createCompatibleImage(renderedNickWidth + 2,
                            s.image.getHeight() + metrics.getHeight() + 1,
                            Transparency.BITMASK);
                    bimageGraphics = bimage.createGraphics();

                    bimageGraphics.drawImage(s.image,
                                             (renderedNickWidth + 2) / 2 - sprImgW / 2,
                                             metrics.getHeight() + 1,
                                             null);
                    bimageGraphics.setColor(new Color(0.0f, 0.0f, 0.0f, 1.0f));
                    bimageGraphics.drawString(getNick(), 1, 1 + metrics.getMaxAscent());
                } else {

                    bimage = gc.createCompatibleImage(sprImgW,
                            s.image.getHeight() + metrics.getHeight() + 1,
                            Transparency.BITMASK);
                    bimageGraphics = bimage.createGraphics();

                    bimageGraphics.drawImage(s.image, 0, metrics.getHeight(), null);
                    bimageGraphics.setColor(new Color(0.0f, 0.0f, 0.0f, 1.0f));
                    bimageGraphics.drawString(getNick(),
                            sprImgW / 2 - renderedNickWidth / 2,
                            1 + metrics.getMaxAscent());
                }

                s.image = bimage;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        return s;
    }
}
