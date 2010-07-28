package client;

import java.awt.Point;

public class PowerOfWateringPot extends Nuke {
    public PowerOfWateringPot(Unit attacker) {
        super(attacker);
        nukeAnim = new NukeAnimation("power_of_watering_pot");
        reuse = 2000;   // 2 sec.
        lastUseTime = 0;

        Point tmp = attacker.getCurPos();
        mv = new Movement(tmp.x, tmp.y, 1.0);
    }
}
