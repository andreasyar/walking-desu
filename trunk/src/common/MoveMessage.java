package common;

import common.messages.MessageType;
import java.io.Serializable;

public class MoveMessage implements Message, Serializable {

    private final MessageType type = MessageType.MOVE;
    private long unitID;
    private long begTime;
    private int begX;
    private int begY;
    private int endX;
    private int endY;

    public MoveMessage(long unitID, long begTime, int begX, int begY, int endX, int endY) {
        this.unitID = unitID;
        this.begTime = begTime;
        this.begX = begX;
        this.begY = begY;
        this.endX = endX;
        this.endY = endY;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public long getUnitID() {
        return unitID;
    }

    public long getBegTime() {
        return begTime;
    }

    public int getBegX() {
        return begX;
    }

    public int getBegY() {
        return begY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    @Override
    public String toString() {
        return "Unit " + unitID + " at " + begTime + " move from (" + begX + ", " + begY + ") to (" + endX + ", " + endY + ").";
    }

}
