package common.messages;

import common.Message;
import java.io.Serializable;

/**
 * Client pickup request; the server derives the player from the connection.
 * The server acknowledges success only to the picker. This message changes
 * neither visibility nor inventory: those have separate messages.
 * @author sorc
 */
public class PickupEtcItem implements Message, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Message type.
     */
    private final MessageType type = MessageType.PICKUPETCITEM;
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
    public PickupEtcItem(long pickerId, long itemId) {
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
