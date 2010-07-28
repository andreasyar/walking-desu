package server.javatestserver;

public class NPC extends Unit {
    public NPC(long id, String nick, int maxHitPoints, int x, int y, double speed) {
        super(id, nick, maxHitPoints, x, y, speed);
        hitPoints = 1;
    }

    public void doHit(int dmg) {}
}
