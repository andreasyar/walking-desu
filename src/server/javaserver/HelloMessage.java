package server.javaserver;

import java.io.Serializable;

public class HelloMessage implements Message, Serializable {

    private final MessageTypes type = MessageTypes.HELLO;

    @Override
    public MessageTypes getType() {
        return type;
    }
}
