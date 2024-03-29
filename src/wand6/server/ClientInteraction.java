package wand6.server;

import java.io.EOFException;
import java.util.logging.Level;
import java.util.logging.Logger;
import wand6.common.messages.Message;
import wand6.client.messages.NameMessage;
import wand6.common.messages.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import wand6.client.exceptions.MessageManagerException;
import wand6.client.messages.MapFragmentRequestMessage;
import wand6.client.messages.MoveRequestMessage;
import wand6.client.messages.TextCloudMessage;
import wand6.server.exceptions.PlayerManagerException;

class ClientInteraction {

    private static int debugLevel = 1;

    private long selfPlayerId = 0;
    private PlayerManager playerManager;
    private MessageManager messageManager;
    private MapManager mapManager;

    private Socket socket;
    private MessageSender sender;
    private MessageReceiver receiver;
    private boolean ready = false;

    ClientInteraction(Socket socket, PlayerManager playerManager, MessageManager messageManager, MapManager mapManager) {
        this.socket = socket;
        this.playerManager = playerManager;
        this.messageManager = messageManager;
        this.mapManager = mapManager;
    }

    public void run() {
        try {
            sender = new MessageSender(this, new ObjectOutputStream(socket.getOutputStream()));
            receiver = new MessageReceiver(this,
                                           new ObjectInputStream(socket.getInputStream()),
                                                                 playerManager,
                                                                 messageManager,
                                                                 mapManager);
            new Thread(sender).start();
            new Thread(receiver).start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    void sendMessage(Message message) {
        sender.sendMessage(message);
    }

    long getSelfPlayerId() {
        return selfPlayerId;
    }

    void setSelfPlayerId(long id) throws IllegalArgumentException {
        if (id < 1) {
            throw new IllegalArgumentException("id must be 1 or greater.");
        }

        selfPlayerId = id;
    }

    boolean isReady() {
        return ready;
    }

    void setReady(boolean ready) {
        this.ready = ready;
    }
}

class MessageSender implements Runnable {

    private static int debugLevel = 1;

    private ClientInteraction client;
    private ObjectOutputStream outStream;
    private final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();

    MessageSender(ClientInteraction client, ObjectOutputStream outStream) {
        this.client = client;
        this.outStream = outStream;
    }

    public void run() {
        try {
            Message message;

            while (true) {
                while (!messages.isEmpty()) {
                    message = messages.poll();
                    outStream.writeObject(message);
                    outStream.reset();
                    if (debugLevel > 0) {
                        System.out.println(message + " --> [" + client.getSelfPlayerId() + "]");
                    }
                }
                if (messages.isEmpty()) {
                    try {
                        synchronized (messages) {
                            messages.wait();
                        }
                    } catch (InterruptedException ignore) {
                        // ignore
                    }
                }
            }
        } catch (SocketException ex) {
            System.out.println(ex.getMessage());
            client.setReady(false);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    void sendMessage(Message message) {
        messages.add(message);
        synchronized(messages) {
            messages.notify();
        }
    }
}

class MessageReceiver implements Runnable {

    private static int debugLevel = 1;

    private ClientInteraction client;
    private ObjectInputStream inStream;

    private PlayerManager playerManager;
    private MessageManager messageManager;
    private MapManager mapManager;

    private boolean helloReceived = false;

    MessageReceiver(ClientInteraction client,
                    ObjectInputStream inStream,
                    PlayerManager playerManager,
                    MessageManager messageManager,
                    MapManager mapManager) {

        this.client = client;
        this.inStream = inStream;
        this.playerManager = playerManager;
        this.messageManager = messageManager;
        this.mapManager = mapManager;
    }

    public void run() {
        Message message;

        try {
            while (true) {
                message = (Message) inStream.readObject();

                if (helloReceived) {
                    if (client.isReady()) {

                        commandHandler(message);

                    } else {
                        if (message.getType() == MessageType.NAME) {
                            if (debugLevel > 0) {
                                System.out.println("Name received.");
                            }
                            client.setSelfPlayerId(playerManager.createPlayer(((NameMessage) message).getName()));
                            client.setReady(true);
                            messageManager.sendWelcome(client.getSelfPlayerId());
                        }
                    }
                } else {
                    if (message.getType() == MessageType.HELLO) {
                        if (debugLevel > 0) {
                            System.out.println("Hello received.");
                        }
                        helloReceived = true;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (EOFException e) {
            if (debugLevel > 0) {
                System.out.println("Client id=" + client.getSelfPlayerId() + " disconnected.");
            }
            client.setReady(false);
            return;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (SocketException ex) {
            System.out.println(ex.getMessage());
            client.setReady(false);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (MessageManagerException e) {
            e.printStackTrace();
            if (debugLevel > 0) {
                System.exit(1);
            }
            return;
        }
    }

    void commandHandler(Message message) {
        if (debugLevel > 0) {
            System.out.println("[" + client.getSelfPlayerId() + "] --> " + message);
        }

        if (message.getType() == MessageType.TEXTCLOUD) {

            TextCloudMessage m = (TextCloudMessage) message;
            try {
                playerManager.setPlayerText(client.getSelfPlayerId(), m.getText());
            } catch (PlayerManagerException ex) {
                if (debugLevel > 0) {
                    ex.printStackTrace();
                    System.exit(1);
                } else {
                    System.err.println(ex.getMessage());
                }
            }

        } else if (message.getType() == MessageType.MAPFRAGMENTREQEST) {

            MapFragmentRequestMessage m = (MapFragmentRequestMessage) message;
            try {
                messageManager.sendMapFragment(client.getSelfPlayerId(), mapManager.getMapFragment(m.getIdX(), m.getIdY()));
            } catch (MessageManagerException ex) {
                ex.printStackTrace();
                if (debugLevel > 0) {
                    System.exit(1);
                }
                return;
            }

        } else if (message.getType() == MessageType.MOVEREQUEST) {

            MoveRequestMessage m = (MoveRequestMessage) message;
            try {
                playerManager.movePlayer(client.getSelfPlayerId(), m.getX(), m.getY());
            } catch (PlayerManagerException ex) {
                if (debugLevel > 0) {
                    ex.printStackTrace();
                    System.exit(1);
                } else {
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}
