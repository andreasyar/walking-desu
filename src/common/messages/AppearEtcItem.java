package common.messages;

import common.Message;
import newcommon.items.Items;
import java.io.Serializable;

/**
 * Message waht notify user what etc item appears on the ground.
 * @author sorc
 */
public class AppearEtcItem implements Message, Serializable {

    /**
     * Message type.
     */
    private final MessageType type = MessageType.APPEARETCITEM;
    /**
     * Id of etc item.
     */
    private long id;
    /**
     * X-axis of etc item on map.
     */
    private int x;
    /**
     * Y-axis of etc item on map.
     */
    private int y;
    /**
     * Count of etc item.
     */
    private int count;
    /**
     * Type of etc item.
     */
    private Items itemType;

    /**
     * Creates new message.
     * @param id id of etc item.
     * @param x x-axis of etc item on map.
     * @param y y-axis of etc item on map.
     * @param count count of etc item.
     * @param itemType type of etc item.
     */
    public AppearEtcItem(long id, int x, int y, int count, Items itemType) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.count = count;
        this.itemType = itemType;
    }

    /**
     * Returns message type.
     * @return message type.
     */
    @Override
    public MessageType getType() {
        return type;
    }

    /**
     * Returns etc item type.
     * @return etc item type.
     */
    public Items getItemType() {
        return itemType;
    }

    /**
     * Returns id of etc item.
     * @return id of etc item.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns x-axis of etc item on map.
     * @return x-axis of etc item on map.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns y-axis of etc item on map.
     * @return y-axis of etc item on map.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns count of etc item.
     * @return count of etc item.
     */
    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Etc item " + itemType.name() + "(" + count + ") id=" + getId() + " appears at (" + x + ", " + y + ").";
    }
}
