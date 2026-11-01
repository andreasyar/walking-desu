package wand6.server;

import java.util.ArrayList;
import wand6.common.Player;
import wand6.common.ServerTime;
import wand6.server.exceptions.PlayerManagerException;
import wand6.server.exceptions.VisibleManagerException;

class PlayerManager {

    private static int debugLevel = 1;

    private VisibleManager visibleManager;

    private final ArrayList<Player> players = new ArrayList<Player>();

    private final String defaultSpriteSetName = "pesant";

    private boolean initialized = false;

    void init(VisibleManager visibleManager) {
        this.visibleManager = visibleManager;
        initialized = true;
    }

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
        if (!initialized) {
            throw new PlayerManagerException(this + " not initialized yet and cannot be used.");
        }

        synchronized(players) {
            try {
                for (Player player : players) {
                    if (player.getId() == playerId) {

                        // TODO Add checks and path finding.
                        player.move(x, y, ServerTime.getInstance().getTimeSinceStart());
                        visibleManager.movementStartNotify(player.getId());
                        return;
                    }
                }
            } catch (VisibleManagerException ex) {
                ex.printStackTrace();
                if (debugLevel > 0) {
                    System.exit(1);
                }
                return;
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
