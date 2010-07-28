package client;

public class NPC extends Unit {
    public NPC(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
        super(id, nick, maxHitPoints, speed, x, y, d, set);
        hitPoints = 1;
    }

    public void doHit(int dmg) {}
}
