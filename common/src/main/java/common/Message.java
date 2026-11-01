package common;

import common.messages.MessageType;

public interface Message {

    public abstract MessageType getType();

    @Override
    public abstract String toString();
}
