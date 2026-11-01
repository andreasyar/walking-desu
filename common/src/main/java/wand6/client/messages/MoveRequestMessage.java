package wand6.client.messages;

import wand6.common.messages.MessageType;
import java.io.Serializable;
import wand6.common.messages.Message;

public class MoveRequestMessage implements Message, Serializable {

    private final MessageType type = MessageType.MOVEREQUEST;
    private int x;
    private int y;

    public MoveRequestMessage(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "MoveRequestMessage{" + "x=" + x + " y=" + y + '}';
    }
}
