package wand6.server;

import wand6.server.exceptions.JavaServerException;
import wand6.server.exceptions.PlayerManagerException;
import wand6.server.messages.MapFragmentMessage;
import wand6.server.messages.MoveMessage;
import wand6.server.messages.WelcomeMessage;

class MessageManager {

    private static MessageManager self = null;
    private long clientId;

    static MessageManager getInstance(long clientId) {
        if (self == null) {
            self = new MessageManager();
        }

        self.clientId = clientId;
        return self;
    }

    private MessageManager() {}

    void sendWelcome() {
        PlayerManager pm = PlayerManager.getInstance();

        try {
            JavaServer.sendMessage(new WelcomeMessage(clientId,
                    pm.getPlayerName(clientId),
                    pm.getPlayerSpriteSetName(clientId),
                    pm.getPlayerCurX(clientId),
                    pm.getPlayerCurY(clientId)), clientId);
        } catch (PlayerManagerException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (JavaServerException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    void sendMapFragment(SMapFragment fragment) {
        try {
            JavaServer.sendMessage(new MapFragmentMessage(fragment.getIdX(),
                                                          fragment.getIdY(),
                                                          fragment.getHmap()),
                                   clientId);
        } catch (JavaServerException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    void sendMoveMessage(long playerId) {
        PlayerManager pm = PlayerManager.getInstance();
        try {
            JavaServer.sendMessage(new MoveMessage(playerId,
                                                   pm.getPlayerEndX(playerId),
                                                   pm.getPlayerEndY(playerId),
                                                   pm.getPlayerMovementBegTime(playerId)),
                                   clientId);
        } catch (JavaServerException ex) {
            ex.printStackTrace();
            System.exit(1);
        }catch (PlayerManagerException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
