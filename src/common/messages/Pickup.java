package common.messages;

import common.Message;
import java.io.Serializable;

/**
 * Pickup message what used both on client and server. Cliet use this message
 * to request pickup action from server. Server use this message to notify
 * client about any pickup actions.
 * @author sorc
 */
public class Pickup implements Message, Serializable {

    /**
     * Message type.
     */
    private final MessageType type = MessageType.PICKUP;
    /**
     * Id of subject who pickup item.
     */
    private long pickerId;
    /**
     * Id of item what picked up.
     */
    private long itemId;

    /**
     * Creates new message.
     * @param pickerId id of subject who pickup item.
     * @param itemId id of item what picked up.
     */
    public Pickup(long pickerId, long itemId) {
        this.pickerId = pickerId;
        this.itemId = itemId;
    }

    /**
     * Returns message type.
     * @return Message type.
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Returns id of item what picked up.
     * @return id of item what picked up.
     */
    public long getItemId() {
        return itemId;
    }

    /**
     * Returns id of subject who pickup item.
     * @return id of subject who pickup item.
     */
    public long getPickerId() {
        return pickerId;
    }

    @Override
    public String toString() {
        return "Subject id=" + pickerId + " pickup item id=" + itemId + ".";
    }
}
