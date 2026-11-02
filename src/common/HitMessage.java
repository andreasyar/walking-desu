package common;

import common.messages.MessageType;
import java.io.Serializable;

public class HitMessage implements Message, Serializable {

    private final MessageType type = MessageType.HIT;
    private long attackerID;
    private long targetID;
    private int damage;

    public HitMessage(long attackerID, long targetID, int damage) {
        this.attackerID = attackerID;
        this.targetID = targetID;
        this.damage = damage;
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

    public int getDamage() {
        return damage;
    }

    @Override
    public String toString() {
        return attackerID + " hit " + targetID + " for " + damage + ".";
    }
}
