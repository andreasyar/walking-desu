package wand6.server;

import java.util.ArrayList;
import wand6.common.Player;
import wand6.server.exceptions.PlayerManagerException;

class PlayerManager {

    private static PlayerManager self = null;

    private final ArrayList<Player> players = new ArrayList<Player>();

    private final String defaultSpriteSetName = "pesant";

    static PlayerManager getInstance() {
        if (self == null) {
            self = new PlayerManager();
        }

        return self;
    }

    private PlayerManager() {}

    long createPlayer(String name) {
        Player player = new Player(IdManager.nextId(), name, defaultSpriteSetName);
        synchronized (players) {
            players.add(player);
        }
        return player.getId();
    }

    String getPlayerName(long playerId) throws PlayerManagerException {
        synchronized(players) {
            for (Player player : players) {
                if (player.getId() == playerId) {
                    return player.getName();
                }
            }

            throw new PlayerManagerException("Player id=" + playerId + " not found.");
        }
    }

    String getPlayerSpriteSetName(long playerId) throws PlayerManagerException {
        synchronized(players) {
            for (Player player : players) {
                if (player.getId() == playerId) {
                    return player.getSpriteSetName();
                }
            }

            throw new PlayerManagerException("Player id=" + playerId + " not found.");
        }
    }

    int getPlayerCurX(long playerId) throws PlayerManagerException {
        synchronized(players) {
            for (Player player : players) {
                if (player.getId() == playerId) {
                    return player.getCurX();
                }
            }

            throw new PlayerManagerException("Player id=" + playerId + " not found.");
        }
    }

    int getPlayerCurY(long playerId) throws PlayerManagerException {
        synchronized(players) {
            for (Player player : players) {
                if (player.getId() == playerId) {
                    return player.getCurY();
                }
            }

            throw new PlayerManagerException("Player id=" + playerId + " not found.");
        }
    }
}
