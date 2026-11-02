package wand6.common.messages;

import wand6.common.messages.MessageType;

public interface Message {

    public abstract MessageType getType();

    @Override
    public abstract String toString();
}
