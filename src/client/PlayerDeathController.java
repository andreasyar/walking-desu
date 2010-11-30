package client;

import java.util.ArrayList;

public class PlayerDeathController implements Runnable {

    private final ArrayList<Player> players;
    public static final Object listener = new Object();

    public PlayerDeathController(GameField field) {
        this.players = field.getPlayers();
    }

    @Override
    public void run() {
        WanderingLocks.lockPlayers();
        for (Player p : players) {
            if (p.dead() && p.deathAnimationDone()) {
                p.restoreHitPoints();
                p.teleportToSpawn();
                p.resurect();
            }
        }
        WanderingLocks.unlockPlayers();
    }
}
