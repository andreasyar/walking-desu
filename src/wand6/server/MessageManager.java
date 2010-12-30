package wand6.server;

import wand6.client.exceptions.MessageManagerException;
import wand6.server.exceptions.JavaServerException;
import wand6.server.exceptions.PlayerManagerException;
import wand6.server.messages.MapFragmentMessage;
import wand6.server.messages.MoveMessage;
import wand6.server.messages.WelcomeMessage;

class MessageManager {

    private static int debugLevel = 1;

    private JavaServer server;
    private PlayerManager playerManager;
    private boolean initialized = false;

    void init(JavaServer server, PlayerManager playerManager) {
        this.server = server;
        this.playerManager = playerManager;
        initialized = true;
    }

    void sendWelcome(long clientId) throws MessageManagerException {
        if (!initialized) {
            throw new MessageManagerException(this + " not initialized yet and cannot be used.");
        }

        try {
            server.sendMessage(new WelcomeMessage(clientId,
                                                  playerManager.getPlayerName(clientId),
                                                  playerManager.getPlayerSpriteSetName(clientId),
                                                  playerManager.getPlayerCurX(clientId),
                                                  playerManager.getPlayerCurY(clientId)),
                               clientId);
        } catch (PlayerManagerException e) {
            e.printStackTrace();
            if (debugLevel > 0) {
                System.exit(1);
            }
        } catch (JavaServerException e) {
            e.printStackTrace();
            if (debugLevel > 0) {
                System.exit(1);
            }
        }
    }

    void sendMapFragment(long clientId, SMapFragment fragment) throws MessageManagerException {
        if (!initialized) {
            throw new MessageManagerException(this + " not initialized yet and cannot be used.");
        }

        try {
            server.sendMessage(new MapFragmentMessage(fragment.getIdX(),
                                                      fragment.getIdY(),
                                                      fragment.getHmap()),
                               clientId);
        } catch (JavaServerException e) {
            e.printStackTrace();
            if (debugLevel > 0) {
                System.exit(1);
            }
        }
    }

    void sendMoveMessage(long clientId, long playerId) throws MessageManagerException {
        if (!initialized) {
            throw new MessageManagerException(this + " not initialized yet and cannot be used.");
        }

        try {
            server.sendMessage(new MoveMessage(playerId,
                                               playerManager.getPlayerEndX(playerId),
                                               playerManager.getPlayerEndY(playerId),
                                               playerManager.getPlayerMovementBegTime(playerId)),
                               clientId);
        } catch (JavaServerException ex) {
            ex.printStackTrace();
            if (debugLevel > 0) {
                System.exit(1);
            }
        }catch (PlayerManagerException ex) {
            ex.printStackTrace();
            if (debugLevel > 0) {
                System.exit(1);
            }
        }
    }
}
