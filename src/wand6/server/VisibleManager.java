package wand6.server;

import java.util.ArrayList;
import java.util.HashMap;

class VisibleManager {

    private static final HashMap<Long, ArrayList<Long>> visiblePlayersMap = new HashMap<Long, ArrayList<Long>>();
    private static final HashMap<Long, ArrayList<Long>> visibleMonstersMap = new HashMap<Long, ArrayList<Long>>();

    private VisibleManager() {}

    static void update() {
        // Dummy do nothing.
    }

    static void movementStartNotify(long id) {
        MessageManager.getInstance(id).sendMoveMessage(id);

        synchronized (visiblePlayersMap) {
            if (visiblePlayersMap.containsKey(id)) {
                for (long _id : visiblePlayersMap.get(id)) {
                    MessageManager.getInstance(_id).sendMoveMessage(id);
                }
            }
        }
    }
}
