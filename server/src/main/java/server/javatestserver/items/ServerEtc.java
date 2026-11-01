package server.javatestserver.items;

import common.Message;
import common.items.Etc;
import common.items.Items;
import common.messages.AddEtcItem;
import common.messages.InventoryAddEtcItem;
import common.messages.InventoryRemoveEtcItem;

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
     */
    public ServerEtc(long id, String name, Items type) {
        super(id, name, type);
    }

    /**
     * Returns message notify user what etc item added to his inventory.
     * @return message what notify user what etc item added to his inventory.
     */
    public Message getAddToInvenrotyMessage() {
        return new InventoryAddEtcItem(getID(), getCount(), getType());
    }

    /**
     * Returns message notify user what etc item removed from his inventory.
     * @return message waht notify user what etc item removed from his inventory.
     */
    public Message getRemoveFromInventoryMessage() {
        return new InventoryRemoveEtcItem(getID(), getCount(), getType());
    }

    /**
     * Returns message notify user what etc item dropped to the ground.
     * @return message what notify user what etc item dropped to the ground.
     */
    public Message getDropMessage() {
        return new AddEtcItem(getID(), getX(), getY(), getCount(), getType());
    }
}
