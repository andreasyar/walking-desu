package client;

import common.Unit;
import common.Movement;

import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Hashtable;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public abstract class WUnit extends Unit implements WDrawable {

    protected MovementAnimation moveAnim;

    protected StandAnimation standAnim;

    protected DeathAnimation deathAnim;

    private UseSkillAnimation useAnim;

    private BufferedImage textCloud = null;

    protected WUnit selectedUnit;

    protected Nuke currentNuke;

    public WUnit(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {

        super();

        this.id = id;
        this.nick = nick;
        this.maxHitPoints = maxHitPoints;

        moveAnim = new MovementAnimation(set);
        standAnim = new StandAnimation(set);
        standAnim.run(d, System.currentTimeMillis() - ServerInteraction.serverStartTime);
        if ("peasant".equals(set)) {
            try {
                useAnim = new UseSkillAnimation(set);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        } else if ("peon".equals(set)) {
            try {
                useAnim = new UseSkillAnimation("peasant");
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }

        mv = new Movement(x, y, speed);
    }

    // <editor-fold defaultstate="collapsed" desc="Movement">
    @Override
    public void move(int begX, int begY, int endX, int endY, long begTime) {
        mv.move(begX, begY, endX, endY, begTime);
        moveAnim.run(begX, begY, endX, endY, 10.0);
        standAnim.run(moveAnim.getDirection(), mv.getEndTime());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sprite and Animation">
    @Override
    public Sprite getSprite() {
        boolean isMove = mv.isMove();
        boolean isAttack = isAttack();
        boolean isDead = dead();

        if (!isDead && isMove) {
            return moveAnim.getSprite(mv.getCurPos());
        } else if (!isDead && !isMove && !isAttack) {
            return standAnim.getSprite(System.currentTimeMillis() - ServerInteraction.serverStartTime, mv.getCurPos());
        } else if (!isDead && !isMove && isAttack) {
            Sprite s = null;
            try {
                s = useAnim.getSprite(System.currentTimeMillis() - ServerInteraction.serverStartTime, mv.getCurPos());
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
            return s;
        } else if (isDead) {
            return deathAnim.getSprite(System.currentTimeMillis() - ServerInteraction.serverStartTime, mv.getCurPos());
        } else {
            System.err.println("Illegal unit state.");
            System.exit(1);
            return null;
        }
    }

    public boolean deathAnimationDone() {
        return deathAnim.isDone();
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
}
