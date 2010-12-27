package wand6.server.messages;

import wand6.common.messages.Message;
import wand6.common.messages.MessageType;
import java.io.Serializable;

public class TimeSyncMessage implements Message, Serializable {

    private final MessageType type = MessageType.TIMESYNC;
    private long time;

    public TimeSyncMessage(long time) {
        this.time = time;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "TimeSyncMessage{" + "time=" + time + '}';
    }
}
