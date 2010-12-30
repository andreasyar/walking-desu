package wand6.server;

import java.util.ArrayList;
import java.util.HashMap;
import wand6.client.exceptions.MessageManagerException;
import wand6.server.exceptions.VisibleManagerException;

class VisibleManager {

    private static int debugLevel = 1;

    private MessageManager messageManager;

    private final HashMap<Long, ArrayList<Long>> visiblePlayersMap = new HashMap<Long, ArrayList<Long>>();
    private final HashMap<Long, ArrayList<Long>> visibleMonstersMap = new HashMap<Long, ArrayList<Long>>();

    private boolean initialized = false;

    void init(MessageManager messageManager) {
        this.messageManager = messageManager;
        initialized = true;
    }

    void movementStartNotify(long playerId) throws VisibleManagerException {
        if (!initialized) {
            throw new VisibleManagerException(this + " not initialized yet and cannot be used.");
        }

        try {
            messageManager.sendMoveMessage(playerId, playerId);

            synchronized (visiblePlayersMap) {
                if (visiblePlayersMap.containsKey(playerId)) {
                    for (long visiblePlayerId : visiblePlayersMap.get(playerId)) {
                        messageManager.sendMoveMessage(visiblePlayerId, playerId);
                    }
                }
            }
        } catch (MessageManagerException ex) {
            ex.printStackTrace();
            if (debugLevel > 0) {
                System.exit(1);
            }
            return;
        }
    }
}
