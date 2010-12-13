package common;

import common.messages.MessageType;
import java.io.Serializable;

public class BoltMessage implements Message, Serializable {

    private final MessageType type = MessageType.BOLT;
    private long attackerID;
    private long targetID;
    private long begTime;

    public BoltMessage(long attackerID, long targetID, long begTime) {
        this.attackerID = attackerID;
        this.targetID = targetID;
        this.begTime = begTime;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public long getAttackerID() {
        return attackerID;
    }

    public long getTargetID() {
        return targetID;
    }

    public long getBegTime() {
        return begTime;
    }

    @Override
    public String toString() {
        return attackerID + " nuke " + targetID + " at " + begTime + ".";
    }

}
