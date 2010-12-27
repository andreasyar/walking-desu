package wand6.server;

import wand6.server.exceptions.JavaServerException;
import wand6.common.messages.Message;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ListIterator;
import wand6.common.ServerTime;

public class JavaServer {

    private static int debugLevel = 1;

    private ServerSocket socket;
    private final ArrayList<ClientInteraction> clients = new ArrayList<ClientInteraction>();

    public static void main(String[] args) {
        try {
            JavaServer server = new JavaServer(45000);
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    JavaServer(int port) throws IOException {
        socket = new ServerSocket(port);
    }

    void run() throws IOException {
        Socket clientSocket;
        ClientInteraction clientInteraction;
        ServerTime.getInstance();   // Initialize server start time.
        new TimeSyncTask(this).schedule(10000L);

        while (true) {
            clientSocket = socket.accept();

            if (clientSocket != null) {
                clientInteraction = new ClientInteraction(clientSocket);
                synchronized(clients) {
                    clients.add(clientInteraction);
                }
                clientInteraction.run();
            }
        }
    }

    boolean sendMessage(Message message, long clientId) throws JavaServerException {
        synchronized(clients) {
            ClientInteraction client;

            for (ListIterator<ClientInteraction> i = clients.listIterator(); i.hasNext();) {
                client = i.next();
                if (client.getSelfPlayerId() == clientId) {
                    if (client.isReady()) {
                        client.sendMessage(message);
                        return true;
                    } else {
                        i.remove();
                        throw new JavaServerException("Client with id=" + clientId + " not ready.");
                    }
                }
            }

            throw new JavaServerException("Client with id=" + clientId + " not exists.");
        }
    }

    void sendMessage(Message message) {
        synchronized(clients) {
            ClientInteraction client;

            for (ListIterator<ClientInteraction> i = clients.listIterator(); i.hasNext();) {
                client = i.next();
                if (client.isReady()) {
                    client.sendMessage(message);
                } else {
                    i.remove();
                    if (debugLevel > 0) {
                        System.err.println("Client was not ready and removed.");
                    }
                }
            }
        }
    }
}
