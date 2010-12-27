package wand6.client;

import java.util.ArrayList;
import wand6.common.Player;

class PlayerManager {

    private static PlayerManager self = null;

    private Player selfPlayer = null;
    private final ArrayList<Player> players = new ArrayList<Player>();

    static PlayerManager getInstance() {
        if (self == null) {
            self = new PlayerManager();
        }

        return self;
    }

    private PlayerManager() {}

    long createPlayer(long id, String name, String spriteSetName) {
        Player player = new Player(id, name, spriteSetName);
        synchronized (players) {
            players.add(player);
        }
        return player.getId();
    }

    long createSelfPlayer(long id, String name, String spriteSetName) {
        selfPlayer = new Player(id, name, spriteSetName);
        synchronized (players) {
            players.add(selfPlayer);
        }
        return selfPlayer.getId();
    }

    long getSelfPlayerId() throws NullPointerException {
        return selfPlayer.getId();
    }

    void setSelfPlayerText(String string) throws NullPointerException {
        selfPlayer.setText(string);
    }
}
