package wand6.server.messages;

import java.io.Serializable;
import wand6.common.messages.Message;
import wand6.common.messages.MessageType;

/** Atomic identity and current segment, adapted from 3.2 newplayer + MOVE + text. */
public record AppearPlayerMessage(long id, String name, String spriteSetName,
        MoveMessage movement, String text) implements Message, Serializable {
    private static final long serialVersionUID = 9L;
    @Override public MessageType getType() { return MessageType.APPEARPLAYER; }
}
