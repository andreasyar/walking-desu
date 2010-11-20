package server.javatestserver;

public class PacketSenderTask implements Runnable {
    private JavaTestServer server;

    public PacketSenderTask(JavaTestServer server) {
        this.server = server;
    }

    @Override
    public void run() {
    }
}
