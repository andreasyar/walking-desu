package client;

import java.util.concurrent.LinkedBlockingQueue;

public class PlayerDeathController implements Runnable {

    private final LinkedBlockingQueue<Player> players;
    public static final Object listener = new Object();

    public PlayerDeathController(GameField field) {
        this.players = field.getPlayers();
    }

    @Override
    public void run() {
        for (Player p : players) {
            if (p.dead() && p.deathAnimationDone()) {
                p.restoreHitPoints();
                p.teleportToSpawn();
                p.resurect();
            }
        }
    }
}
