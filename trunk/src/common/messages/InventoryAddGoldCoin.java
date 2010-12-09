package common.messages;

import common.Message;
import common.MessageType;
import java.io.Serializable;

/**
 * This message notify client what some gold coins(s) was added to its
 * inventory.
 * @author sorc
 */
public class InventoryAddGoldCoin implements Message, Serializable {

    /**
     * Message type.
     */
    private final MessageType type = MessageType.INVADDGOLDCOIN;
    /**
     * ID of this gold coins item.
     */
    private long id;
    /**
     * Count of gold coins.
     */
    private int count;

    /**
     * Creates new message.
     * @param id id of gold coins item.
     * @param count count of gold coins.
     */
    public InventoryAddGoldCoin(long id, int count) {
        this.id = id;
        this.count = count;
    }

    /**
     * Returns message type.
     * @return Message type.
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Returns count of gold coins.
     * @return count of gold coins.
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns id of this gold coins item.
     * @return id of this gold coins item.
     */
    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Add Gold coin(" + getCount() + ") id=" + getId() + " to inventory.";
    }
}
