package server.javatestserver.items;

import common.Message;
import common.items.Etc;
import common.items.Items;
import common.messages.InventoryAddEtcItem;
import common.messages.InventoryRemoveEtcItem;
import common.messages.AppearEtcItem;
import common.messages.DisappearEtcItem;

/**
 * Etc item for server use.
 * @author sorc
 */
public class ServerEtc extends Etc {

    /**
     * Creates new server etc item.
     * @param id id of new server etc item.
     * @param name name of new server etc item.
     * @param count count of new server etc item.
     * @param type type of new server etc item.
     */
    public ServerEtc(long id, String name, int count, Items type) {
        super(id, name, count, type);
    }

    /**
     * Creates new server etc item based on common etc item.
     * @param item etc item.
     */
    public ServerEtc(Etc item) {
        super(item.getID(), item.getName(), item.getCount(), item.getType());
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
     * Returns message waht notify user what etc item appears on the ground.
     * @return message what notify user what etc item appears on the ground.
     */
    public Message getAppearMessage() {
        return new AppearEtcItem(getID(), getX(), getY(), getCount(), getType());
    }

    /**
     * Returns message waht notify user what etc item disappears from the ground.
     * @return message what notify user what etc item disappears from the ground.
     */
    public Message getDisappearMessage() {
        return new DisappearEtcItem(getID());
    }
}
