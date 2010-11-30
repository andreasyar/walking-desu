package common;

public interface Message {

    public abstract MessageType getType();

    @Override
    public abstract String toString();
}
