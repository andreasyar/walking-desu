package client;

public class PowerOfWateringPot extends Nuke {
    public PowerOfWateringPot() {
        nukeAnim = new NukeAnimation("power_of_watering_pot");
        reuse = 2000;   // 2 sec.
        lastUseTime = 0;
        move = new Movement();
    }
}
