package common.messages;

import common.Message;
import java.io.Serializable;

/**
 * Message waht notify user what etc item disappears from the ground.
 * @author sorc
 */
public class DisappearEtcItem implements Message, Serializable {

    /**
     * Message type.
     */
    private final MessageType type = MessageType.DISAPPEARETCITEM;
    /**
     * Id of etc item.
     */
    private long id;

    /**
     * Creates new message.
     * @param id id of etc item.
     * @param x x-axis of etc item on map.
     * @param y y-axis of etc item on map.
     * @param count count of etc item.
     * @param itemType type of etc item.
     */
    public DisappearEtcItem(long id) {
        this.id = id;
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
     * Returns id of etc item.
     * @return id of etc item.
     */
    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Etc item id=" + getId() + " disappears.";
    }
}
