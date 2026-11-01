package wand6.server.messages;

import wand6.common.messages.Message;
import wand6.common.messages.MessageType;
import java.io.Serializable;

public class MoveMessage implements Message, Serializable {

    private final MessageType type = MessageType.MOVE;
    private static final long serialVersionUID = 8L;
    private int begX, begY;
    private long id;
    private int endX;
    private int endY;
    private long begTime;

    public MoveMessage(long id, int begX, int begY, int endX, int endY, long begTime) {
        this.id = id;
        this.begX = begX; this.begY = begY;
        this.endX = endX;
        this.endY = endY;
        this.begTime = begTime;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public int getBegX() { return begX; }
    public int getBegY() { return begY; }

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
