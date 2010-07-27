package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Hashtable;

abstract public class Unit {
    private MovementAnimation moveAnim;
    private StandAnimation standAnim;

    private Movement move;

    private int id;
    private String nick;

    // В начале текст и облако пусты.
    private String text = null;
    private BufferedImage textCloud = null;

    protected int maxHitPoints;
    protected int hitPoints;

    protected Unit selectedUnit;
    protected Nuke currentNuke;

    public Unit(int id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
        this.id = id;
        this.nick = nick;
        this.maxHitPoints = maxHitPoints;
        hitPoints = this.maxHitPoints;
        moveAnim = new MovementAnimation(set);
        standAnim = new StandAnimation(set);
        standAnim.run(d, System.currentTimeMillis() - ServerInteraction.serverStartTime);
        move = new Movement(new Point(x, y), speed);
    }

    public void changeSpriteSet(String spriteSet) {
        moveAnim = new MovementAnimation(spriteSet);
        standAnim = new StandAnimation(spriteSet);
        standAnim.run(Direction.SOUTH, ServerInteraction.serverStartTime);
    }

    public Sprite getSprite() {
        return move.isMove() ?
            moveAnim.getSprite(move.getCurPos()) :
            standAnim.getSprite(System.currentTimeMillis() - ServerInteraction.serverStartTime, move.getCurPos());
    }

    public Point getCurPos() {
        return move.getCurPos();
    }

    public void move(Point beg, Point end, long begTime) {
        move.move(beg, end, begTime);
        moveAnim.run(beg, end, 10.0);
        standAnim.run(moveAnim.getDirection(), move.getEndTime());
    }

    public long getID() {
        return id;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    abstract public void doHit(int dmg);

    public int getHitPoints() {
        return hitPoints;
    }

    public void setSpeed(double speed) {
        move.setSpeed(speed);
    }

    public void updateTextCloud() {
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
            Graphics2D g2d = (Graphics2D)g;
            g2d.setColor(new Color((float)0.1, (float)1.0, (float)0.3, (float)0.7));
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

    public BufferedImage getTextCloud() {
        return textCloud;
    }

    public void unselectUnit() {
        selectedUnit = null;
    }

    public void selectUnit(Unit unit) {
        selectedUnit = unit;
    }

    public Unit getSelectedUnit() {
        return selectedUnit;
    }

    public void setCurrentNuke(Nuke currentNuke) {
        this.currentNuke = currentNuke;
    }

    public Nuke getCurrentNuke() {
        return currentNuke;
    }
}
