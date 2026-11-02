package wand6.server.messages;

import wand6.common.messages.Message;
import wand6.common.messages.MessageType;

public record TowerDefenceStatusMessage(long cycle, long wave, int losses, int limit, int strength) implements Message, java.io.Serializable {
    private static final long serialVersionUID = 11L;
    @Override public MessageType getType() { return MessageType.TDSTATUS; }
}
