package client;

import java.awt.Point;

public class PeasantNukeAnimation extends NukeAnimation {

    private Point beg, end, cur;

    public PeasantNukeAnimation(String set) {
        super(set);
    }

    public Sprite getSprite() {
        double length = beg.distance(end);
        double curLength = beg.distance(cur);
        Sprite s;

        if (curLength > length / 4.0) {
            s = set.getSprite(direction, 0);
            s.x = cur.x;
            s.y = cur.y;
            return s;
        } else if (curLength < length / 4.0 && curLength > length / 6.0) {
            s = set.getSprite(direction, 0);
            s.x = cur.x;
            s.y = cur.y;
            return s;
        } else {
            s = set.getSprite(direction, 0);
            s.x = cur.x;
            s.y = cur.y;
            return s;
        }
    }

    public void run(final Point beg, final Point end, final Point cur) {
        this.cur = cur;
        this.beg = beg;
        this.end = end;
        direction = Direction.getDirection(beg, end);
    }
}
