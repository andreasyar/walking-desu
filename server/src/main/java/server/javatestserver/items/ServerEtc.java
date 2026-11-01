package server.javatestserver.items;

import common.Message;
import common.items.Etc;
import common.items.Items;
import common.messages.AppearEtcItem;

/**
 * Etc item for server use.
 * @author sorc
 */
public class ServerEtc extends Etc {

    /**
     * Creates new etc item.
     * @param id id of new etc item.
     * @param name name of new etc item.
     * @param type type of new etc item.
     * @param count initial positive quantity.
     */
    public ServerEtc(long id, String name, Items type, int count) {
        super(id, name, type, count);
    }

    /**
     * Returns message notify user what etc item dropped to the ground.
     * @return message what notify user what etc item dropped to the ground.
     */
    public Message getDropMessage() {
        return new AppearEtcItem(getID(), getX(), getY(), getCount(), getType());
    }
}
