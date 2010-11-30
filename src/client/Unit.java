package client;

//import java.awt.Color;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Point;
//import java.awt.font.FontRenderContext;
//import java.awt.font.LineBreakMeasurer;
//import java.awt.font.TextAttribute;
//import java.awt.font.TextLayout;
//import java.awt.image.BufferedImage;
//import java.text.AttributedCharacterIterator;
//import java.text.AttributedString;
//import java.util.Hashtable;
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
//    protected MovementAnimation moveAnim;
//    protected StandAnimation standAnim;
//    protected DeathAnimation deathAnim;
//    private UseSkillAnimation useAnim;
//
//    protected int maxHitPoints;
//
//    protected int hitPoints;
//
//    private String text = null;
//
//    private BufferedImage textCloud = null;
//
//    protected Unit selectedUnit;
//
//    protected Nuke currentNuke;
//
//    protected boolean isDead = false;
//
//    private boolean isMove;
//
//    private boolean isAttack = false;
//
//    public Unit(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
//        this.id = id;
//        this.nick = nick;
//        this.maxHitPoints = maxHitPoints;
//        moveAnim = new MovementAnimation(set);
//        standAnim = new StandAnimation(set);
//        standAnim.run(d, System.currentTimeMillis() - ServerInteraction.serverStartTime);
//        if ("peasant".equals(set)) {
//            try {
//                useAnim = new UseSkillAnimation(set);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                System.exit(1);
//            }
//        } else if ("peon".equals(set)) {
//            try {
//                useAnim = new UseSkillAnimation("peasant");
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                System.exit(1);
//            }
//        }
//        mv = new Movement(x, y, speed);
//
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
//        moveAnim.run(beg, end, 10.0);
//        standAnim.run(moveAnim.getDirection(), mv.getEndTime());
//    }
//    // </editor-fold>
//
//    public Sprite getSprite() {
//        isMove = mv.isMove();
//        isAttack = isAttack();
//
//        if (!isDead && isMove) {
//            return moveAnim.getSprite(mv.getCurPos());
//        } else if (!isDead && !isMove && !isAttack) {
//            return standAnim.getSprite(System.currentTimeMillis() - ServerInteraction.serverStartTime, mv.getCurPos());
//        } else if (!isDead && !isMove && isAttack) {
//            Sprite s = null;
//            try {
//                s = useAnim.getSprite(System.currentTimeMillis() - ServerInteraction.serverStartTime, mv.getCurPos());
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                System.exit(1);
//            }
//            return s;
//        } else if (isDead) {
//            return deathAnim.getSprite(System.currentTimeMillis() - ServerInteraction.serverStartTime, mv.getCurPos());
//        } else {
//            System.err.println("Illegal unit state.");
//            System.exit(1);
//            return null;
//        }
//    }
//
//    public boolean deathAnimationDone() {
//        return deathAnim.isDone();
//    }
//
//    public void changeSpriteSet(String spriteSetName) {
//        moveAnim = new MovementAnimation(spriteSetName);
//        standAnim = new StandAnimation(spriteSetName);
//        standAnim.run(Direction.SOUTH, ServerInteraction.serverStartTime);
//    }
//
//    public long getNukeAnimationDelay() {
//        return useAnim.getNukeAnimationDelay();
//    }
//
//    // <editor-fold defaultstate="collapsed" desc="HP works">
//    public boolean dead() {
//        return isDead;
//    }
//
//    abstract public void doHit(int dmg);
//
//    public int getHitPoints() {
//        return hitPoints;
//    }
//
//    public abstract void kill();
//    // </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="Message works">
//    public String getText() {
//        return text;
//    }
//
//    public void setText(String text) {
//        this.text = text;
//        updateTextCloud();
//    }
//
//    public BufferedImage getTextCloud() {
//        return textCloud;
//    }
//
//    private void updateTextCloud() {
//        if (text == null || text.equals("")) {
//            textCloud = null;
//        } else {
//            textCloud = new BufferedImage(150, 100, BufferedImage.TYPE_4BYTE_ABGR_PRE);
//            Graphics g = textCloud.getGraphics();
//
//            LineBreakMeasurer lineMeasurer;
//            int paragraphStart;
//            int paragraphEnd;
//            float breakWidth = 149 - 2;
//            float drawPosY = 0;
//            Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
//            Graphics2D g2d = (Graphics2D) g;
//            g2d.setColor(new Color((float) 0.1, (float) 1.0, (float) 0.3, (float) 0.7));
//            g2d.fillRoundRect(1, 1, 148, 98, 10, 10);
//            g2d.setColor(Color.BLACK);
//            AttributedCharacterIterator paragraph = (new AttributedString(text)).getIterator();
//            FontRenderContext frc;
//
//            map.put(TextAttribute.FAMILY, "Serif");
//            map.put(TextAttribute.SIZE, new Float(18.0));
//            paragraphStart = paragraph.getBeginIndex();
//            paragraphEnd = paragraph.getEndIndex();
//            frc = g2d.getFontRenderContext();
//            lineMeasurer = new LineBreakMeasurer(paragraph, frc);
//
//            lineMeasurer.setPosition(paragraphStart);
//            while (lineMeasurer.getPosition() < paragraphEnd) {
//                TextLayout layout = lineMeasurer.nextLayout(breakWidth);
//                float drawPosX = layout.isLeftToRight()
//                        ? 2 : breakWidth - layout.getAdvance();
//                drawPosY += layout.getAscent();
//                layout.draw(g2d, drawPosX, drawPosY);
//                drawPosY += layout.getDescent() + layout.getLeading();
//            }
//        }
//    }
//    // </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="Selected unit works">
//    public Unit getSelectedUnit() {
//        return selectedUnit;
//    }
//
//    public void selectUnit(Unit unit) {
//        selectedUnit = unit;
//    }
//
//    public void unselectUnit() {
//        selectedUnit = null;
//    }
//    // </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="Current nuke works">
//    public Nuke getCurrentNuke() {
//        return currentNuke;
//    }
//
//    public void setCurrentNuke(Nuke currentNuke) {
//        this.currentNuke = currentNuke;
//    }
//    // </editor-fold>
//
//    // <editor-fold defaultstate="collapsed" desc="Attack works">
//    public boolean attack(long begTime) {
//        if (isDead || isAttack()) {
//            return false;
//        }
//        if (mv.isMove()) {
//            mv.stop();
//        }
//        useAnim.run(Direction.getDirection(getCurPos(), selectedUnit.getCurPos()), begTime);
//        return true;
//    }
//
//    public boolean isAttack() {
//        return useAnim != null && !useAnim.isStoped();
//    }
//    // </editor-fold>
//}
