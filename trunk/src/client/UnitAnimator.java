package client;

import common.Unit;
import common.WanderingServerTime;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 *
 * @author sorc
 */
class UnitAnimator {

    private static UnitAnimator self = null;
    private final HashMap<Unit, MovementAnimation> moveAnims = new HashMap<Unit, MovementAnimation>();
    private final HashMap<Unit, StandAnimation> standAnims = new HashMap<Unit, StandAnimation>();
    private final HashMap<Unit, DeathAnimation> deathAnims = new HashMap<Unit, DeathAnimation>();
    private final HashMap<Unit, UseSkillAnimation> useAnims = new HashMap<Unit, UseSkillAnimation>();

    static UnitAnimator getInstance() {
        if (self == null) {
            self = new UnitAnimator();
        }

        return self;
    }

    private UnitAnimator() {}

    void animate(Unit u) {
        moveAnims.put(u, new MovementAnimation(u.getSpriteSetName()));
        standAnims.put(u, new StandAnimation(u.getSpriteSetName()));
        deathAnims.put(u, new DeathAnimation(u.getSpriteSetName()));
        useAnims.put(u, new UseSkillAnimation(u.getSpriteSetName()));
    }

    Sprite getSprite(Unit u) {
        MovementAnimation moveAnim;
        StandAnimation standAnim;
        DeathAnimation deathAnim;
        UseSkillAnimation useAnim;

        Sprite s = null;
        BufferedImage bimage;
        Graphics bimageGraphics;

        try {
            if (!animated(u)) {
                animate(u);
            }

            moveAnim = moveAnims.get(u);
            standAnim = standAnims.get(u);
            deathAnim = deathAnims.get(u);
            useAnim = useAnims.get(u);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            FontMetrics metrics = WanderingJPanel.getFontMetrics();

            boolean isMove = u.isMove();
            boolean isAttack = u.isUseSkill();
            boolean isDead = u.dead();
            Point cur = u.getCurPos();

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
                int renderedNickWidth = metrics.stringWidth(u.getNick());
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
                    bimageGraphics.drawString(u.getNick(), 1, 1 + metrics.getMaxAscent());
                } else {

                    bimage = gc.createCompatibleImage(sprImgW,
                                                      s.image.getHeight() + metrics.getHeight() + 1,
                                                      Transparency.BITMASK);
                    bimageGraphics = bimage.createGraphics();

                    bimageGraphics.drawImage(s.image, 0, metrics.getHeight(), null);
                    bimageGraphics.setColor(new Color(0.0f, 0.0f, 0.0f, 1.0f));
                    bimageGraphics.drawString(u.getNick(),
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

    private boolean animated(Unit u) {
        return moveAnims.containsKey(u)
                && standAnims.containsKey(u)
                && deathAnims.containsKey(u)
                && useAnims.containsKey(u);
    }
}
