package wand6.server;

import java.util.ArrayList;
import wand6.common.Player;
import wand6.common.ServerTime;
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

    void setPlayerText(long playerId, String text) throws PlayerManagerException {
        synchronized(players) {
            for (Player player : players) {
                if (player.getId() == playerId) {
                    player.setText(text);
                    return;
                }
            }

            throw new PlayerManagerException("Player id=" + playerId + " not found.");
        }
    }

    void movePlayer(long playerId, int x, int y) throws PlayerManagerException {
        synchronized(players) {
            for (Player player : players) {
                if (player.getId() == playerId) {
                    
                    // TODO Add checks and path finding.
                    player.move(x, y, ServerTime.getInstance().getTimeSinceStart());
                    VisibleManager.movementStartNotify(player.getId());
                    return;
                }
            }

            throw new PlayerManagerException("Player id=" + playerId + " not found.");
        }
    }

    int getPlayerEndX(long playerId) throws PlayerManagerException {
        synchronized(players) {
            for (Player player : players) {
                if (player.getId() == playerId) {
                    return player.getEndX();
                }
            }

            throw new PlayerManagerException("Player id=" + playerId + " not found.");
        }
    }

    int getPlayerEndY(long playerId) throws PlayerManagerException {
        synchronized(players) {
            for (Player player : players) {
                if (player.getId() == playerId) {
                    return player.getEndY();
                }
            }

            throw new PlayerManagerException("Player id=" + playerId + " not found.");
        }
    }

    long getPlayerMovementBegTime(long playerId) throws PlayerManagerException {
        synchronized(players) {
            for (Player player : players) {
                if (player.getId() == playerId) {
                    return player.getMovementBegTime();
                }
            }

            throw new PlayerManagerException("Player id=" + playerId + " not found.");
        }
    }
}
