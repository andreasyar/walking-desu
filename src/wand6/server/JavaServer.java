package wand6.server;

import wand6.server.exceptions.JavaServerException;
import wand6.common.messages.Message;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class JavaServer {

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
            for (ClientInteraction client : clients) {
                if (client.getId() == clientId) {
                    if (client.isReady()) {
                        client.sendMessage(message);
                        return true;
                    } else {
                        clients.remove(client);
                        throw new JavaServerException("Client with id=" + clientId + " not ready.");
                    }
                }
            }

            throw new JavaServerException("Client with id=" + clientId + " not exists.");
        }
    }
}
