package common.messages;

import common.Message;
import common.MessageType;
import java.io.Serializable;

/**
 * Message for notify clients about gold coins what dissappeared from the ground.
 * @author sorc
 */
public class DelGoldCoin implements Message, Serializable {

    /**
     * Message type.
     */
    private final MessageType type = MessageType.DELGOLDCOIN;
    /**
     * Id of deleted gold coin.
     */
    private long id;

    /**
     * Returns message type.
     * @return Message type.
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Creates new message.
     * @param id id of deleted gold coin.
     */
    public DelGoldCoin(long id) {
        this.id = id;
    }

    /**
     * Returns id of deleted gold coin.
     * @return id of deleted gold coin.
     */
    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Gold coin id=" + getId() + " dissappeared.";
    }
}
