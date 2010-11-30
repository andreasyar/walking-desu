package client;

import java.awt.Point;

public class PowerOfWateringPot extends Nuke {

    public PowerOfWateringPot(WUnit attacker) {
        /*super(attacker);
        animation = new NukeAnimation("power_of_watering_pot");
        reuse = 2000;   // 2 sec.
        lastUseTime = 0;

        Point tmp = attacker.getCurPos();
        mv = new Movement(tmp.x, tmp.y, 1.0);*/
    }

    @Override
    public Sprite getSprite() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void use(long begTime) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Point getCurPos() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isMove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
