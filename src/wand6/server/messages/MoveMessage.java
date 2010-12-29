package wand6.server.messages;

import wand6.common.messages.Message;
import wand6.common.messages.MessageType;
import java.io.Serializable;

public class MoveMessage implements Message, Serializable {

    private final MessageType type = MessageType.MOVE;
    private long id;
    private int endX;
    private int endY;
    private long begTime;

    public MoveMessage(long id, int endX, int endY, long begTime) {
        this.id = id;
        this.endX = endX;
        this.endY = endY;
        this.begTime = begTime;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public long getId() {
        return id;
    }

    public long getBegTime() {
        return begTime;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    @Override
    public String toString() {
        return "MoveMessage{" + "id=" + id + " endX=" + endX + " endY=" + endY + " begTime=" + begTime + '}';
    }
}
