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

    private final String defaultSpriteSetName = "peasant";

    private boolean initialized = false;

    void init(VisibleManager visibleManager) {
        this.visibleManager = visibleManager;
        initialized = true;
    }

    long createPlayer(String name) {
        Player player = new Player(IdManager.nextId(), name, defaultSpriteSetName);
        player.place(512, 512);
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

    void movePlayer(long playerId, int x, int y, boolean stop) throws PlayerManagerException {
        if (!initialized) {
            throw new PlayerManagerException(this + " not initialized yet and cannot be used.");
        }

        synchronized(players) {
            try {
                for (Player player : players) {
                    if (player.getId() == playerId) {

                        long time = ServerTime.getInstance().getTimeSinceStart();
                        var position = player.getMovement().position(time);
                        int bx = position.x(), by = position.y();
                        int ex = bx, ey = by;
                        double distance = Math.hypot((double) x - bx, (double) y - by);
                        // Invalid or excessively long commands stop at the current position.
                        if (!stop && wand6.common.MapFragment.contains(x, y) && distance <= 4096) {
                            int steps = (int) Math.ceil(distance);
                            for (int step = 1; step <= steps; step++) {
                                int px = (int) Math.round(bx + ((double) x - bx) * step / steps);
                                int py = (int) Math.round(by + ((double) y - by) * step / steps);
                                if (!wand6.common.Terrain.passable(px, py)) break;
                                // Do not cut diagonally between blocked cells.
                                if (!wand6.common.Terrain.passable(px, ey)
                                        || !wand6.common.Terrain.passable(ex, py)) break;
                                ex = px; ey = py;
                            }
                        }
                        player.getMovement().start(bx, by, ex, ey, time);
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

    wand6.server.messages.MoveMessage movementMessage(long id) throws PlayerManagerException {
        synchronized (players) {
            for (Player player : players) if (player.getId() == id) {
                var m = player.getMovement();
                return new wand6.server.messages.MoveMessage(id, m.getBegX(), m.getBegY(),
                        m.getEndX(), m.getEndY(), m.getBegTime());
            }
            throw new PlayerManagerException("Player not found: " + id);
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
