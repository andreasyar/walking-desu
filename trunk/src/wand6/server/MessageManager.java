package wand6.server;

import wand6.server.exceptions.PlayerManagerException;
import wand6.server.messages.WelcomeMessage;

class MessageManager {

    private static MessageManager self = null;

    static MessageManager getInstance() {
        if (self == null) {
            self = new MessageManager();
        }

        return self;
    }

    private MessageManager() {}

    WelcomeMessage getWelcomeMessage(long playerId) {
        PlayerManager pm = PlayerManager.getInstance();

        try {
            return new WelcomeMessage(playerId,
                                      pm.getPlayerName(playerId),
                                      pm.getPlayerSpriteSetName(playerId),
                                      pm.getPlayerCurX(playerId),
                                      pm.getPlayerCurY(playerId));
        } catch (PlayerManagerException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }
}
