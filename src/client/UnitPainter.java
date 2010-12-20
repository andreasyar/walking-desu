package client;

import common.Unit;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 * @author sorc
 */
class UnitPainter {

    private int debug = 1;
    private final ZBuffer zbuffer = new ZBuffer();
    private final LinkedBlockingQueue<Unit> units = new LinkedBlockingQueue<Unit>();

    void drawUnits(Graphics g, int x, int y, Dimension dim) {
        Sprite s;

        synchronized (zbuffer) {
            if (dim.height != zbuffer.getSize()) {
                // <editor-fold defaultstate="collapsed" desc="Debug.">
                if (debug > 0) {
                    System.out.println("Zbuffer will be resized.");
                }
                // </editor-fold>
                zbuffer.resize(dim.height);
            }

            for (Unit u : units) {
                if (u != null) {
                    s = UnitAnimator.getInstance().getSprite(u);
                    if (s != null
                            && s.y - y >= 0 && s.y - y + s.image.getHeight() < zbuffer.getSize()
                            && s.x - x >= 0 && s.x - x + s.image.getWidth() <= dim.getWidth()) {

                        zbuffer.addSprite(s, s.y - y + s.image.getHeight());
                    }
                }
                else {
                    // <editor-fold defaultstate="collapsed" desc="Debug.">
                    if (debug > 0) {
                        System.err.println("Drawable unit is null. It cannot be! Skip it.");
                    }
                    // </editor-fold>
                    continue;
                }
            }
        }

        zbuffer.draw(g, x, y, dim);
    }

    void addUnit(Unit u) {
        units.add(u);
    }

    void removeUnit(Unit u) {
        units.remove(u);
    }

    /**
     * Enable or disable draw rectangle sprite border.
     * @param on <b>true</b> to enable, <b>false</b> to disable.
     */
    void drawSpriteBounds(boolean on) {
        zbuffer.drawSpriteBounds(on);
    }
}
