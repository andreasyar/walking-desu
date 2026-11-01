package client;

import java.awt.Point;

/**
 * Анимация снаряда пушки.
 */
public class CanonNukeAnimation extends NukeAnimation {

    /**
     * Создаёт новую анимацию снаряда пушки.
     * @param Имя набора направленных спрайтов.
     */
    public CanonNukeAnimation(String dsSet) {
        super(dsSet);
    }

    /**
     * Возвращает текущий спрайт анимациии снаряда пушки.
     * @param cX текущяя X-координата пушки на карте.
     * @param cY текущяя Y-координата пушки на карте.
     * @param tX текущяя X-координата цели на карте.
     * @param tY текущяя Y-координата цели на карте.
     * @param nX текущяя X-координата снаряда на карте.
     * @param nY текущяя Y-координата снаряда на карте.
     */
    public Sprite getSprite(int cX, int cY, int tX, int tY, int nX, int nY) {
        Direction d = Direction.getDirection(cX, cY, tX, tY);
        double length = Point.distance(cX, cY, tX, tY);
        double curLength = Point.distance(cX, cY, nX, nY);
        Sprite s;

        if (curLength > length / 4.0) {
            s = dsSet.getSprite(d, 2);
            s.x = nX;
            s.y = nY;
            return s;
        } else if (curLength < length / 4.0 && curLength > length / 6.0) {
            s = dsSet.getSprite(d, 1);
            s.x = nX;
            s.y = nY;
            return s;
        } else {
            s = dsSet.getSprite(d, 0);
            s.x = nX;
            s.y = nY;
            return s;
        }
    }

    /**
     * Возвращает текущий спрайт анимациии снаряда пушки.
     * @param canon текущее место пушки на карте.
     * @param target текущее место цели на карте.
     * @param nuke текущее место снаряда на карте.
     */
    public Sprite getSprite(Point canon, Point target, Point nuke) {
        return getSprite(canon.x, canon.y, target.x, target.y, nuke.x, nuke.y);
    }
}
