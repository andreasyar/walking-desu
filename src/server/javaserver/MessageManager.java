package server.javaserver;

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

        return new WelcomeMessage(playerId,
                                  pm.getPlayerName(playerId),
                                  pm.getPlayerSpriteSetName(playerId),
                                  pm.getPlayerCurX(playerId),
                                  pm.getPlayerCurY(playerId));
    }
}
