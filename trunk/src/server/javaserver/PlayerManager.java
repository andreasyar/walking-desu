package server.javaserver;

class PlayerManager {

    private static PlayerManager self = null;

    static PlayerManager getInstance() {
        if (self == null) {
            self = new PlayerManager();
        }

        return self;
    }

    private PlayerManager() {}

    long createPlayer(String name) {
        return IdManager.nextId();
    }

    String getPlayerName(long playerId) {
        return "";
    }

    String getPlayerSpriteSetName(long playerId) {
        return "";
    }

    int getPlayerCurX(long playerId) {
        return 0;
    }

    int getPlayerCurY(long playerId) {
        return 0;
    }
}
