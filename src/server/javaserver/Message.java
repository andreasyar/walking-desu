package server.javaserver;

public interface Message {

    public abstract MessageTypes getType();

    @Override
    public abstract String toString();
}
