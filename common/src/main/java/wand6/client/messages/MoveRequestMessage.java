package wand6.client.messages;

import wand6.common.messages.MessageType;
import java.io.Serializable;
import wand6.common.messages.Message;

public class MoveRequestMessage implements Message, Serializable {

    private final MessageType type = MessageType.MOVEREQUEST;
    private static final long serialVersionUID = 8L;
    private boolean stop;
    private int x;
    private int y;

    public MoveRequestMessage(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static MoveRequestMessage stop() {
        MoveRequestMessage request = new MoveRequestMessage(0, 0);
        request.stop = true;
        return request;
    }
    public boolean isStop() { return stop; }

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
