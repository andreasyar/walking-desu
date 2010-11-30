package client;

public class NPC extends WUnit {

    public NPC(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
        super(id, nick, maxHitPoints, speed, x, y, d, set);
        hitPoints = 1;
    }

    @Override
    public void doHit(int dmg) {
    }

    @Override
    public boolean dead() {
        return false;
    }

    @Override
    public void kill() {
    }
}
