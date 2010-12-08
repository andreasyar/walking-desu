package common.messages;

import common.Message;
import common.MessageType;
import java.io.Serializable;

/**
 * Drop gold coin message used by server to notify client about gold coins
 * dropped to the ground.
 * @author sorc
 */
public class DropGoldCoin implements Message, Serializable {

    /**
     * Message type.
     */
    private final MessageType type = MessageType.GOLDCOIN;
    /**
     * Id of dropped gold coin.
     */
    private long id;
    /**
     * X-axis of dropped gold coin on world map.
     */
    private int x;
    /**
     * Y-axis of dropped gold coin on world map.
     */
    private int y;
    /**
     * Count of gold coins.
     */
    private int count;

    /**
     * Creates new message.
     * @param id id of dropped gold coin.
     * @param x x-axis of dropped gold coin on world map.
     * @param y y-axis of dropped gold coin on world map.
     * @param count count of gold coins.
     */
    public DropGoldCoin(long id, int x, int y, int count) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.count = count;
    }

    /**
     * Returns message type.
     * @return Message type.
     */
    @Override
    public MessageType getType() {
        return type;
    }

    /**
     * Returns id of dropped gold coin.
     * @return id of dropped gold coin.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns x-axis of dropped gold coin on world map.
     * @return x-axis of dropped gold coin on world map.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns y-axis of dropped gold coin on world map.
     * @return y-axis of dropped gold coin on world map.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns count of gold coins.
     * @return count of gold coins.
     */
    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return id + " Gold coin(" + count + ") dropped at (" + x + ", " + y + ").";
    }
}
