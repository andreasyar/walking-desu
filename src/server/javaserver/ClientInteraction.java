package server.javaserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

class ClientInteraction {

    private static int debugLevel = 1;
    private long selfPlayerId = 0;
    private Socket socket;
    private MessageSender sender;
    private MessageReceiver receiver;
    private boolean helloReceived = false;
    private boolean ready = false;

    ClientInteraction(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            sender = new MessageSender(new ObjectOutputStream(socket.getOutputStream()));
            receiver = new MessageReceiver(new ObjectInputStream(socket.getInputStream()));
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

    private class MessageSender implements Runnable {

        private final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();
        private ObjectOutputStream outStream;

        MessageSender(ObjectOutputStream outStream) {
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
                            System.out.println(message + " --> [" + selfPlayerId + "]");
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

    private class MessageReceiver implements Runnable {

        private ObjectInputStream inStream;

        MessageReceiver(ObjectInputStream inStream) {
            this.inStream = inStream;
        }

        public void run() {
            Message message;

            try {
                while (true) {
                    message = (Message) inStream.readObject();

                    if (helloReceived) {
                        if (isReady()) {

                            commandHandler(message);

                        } else {
                            if (message.getType() == MessageTypes.NAME) {
                                if (debugLevel > 0) {
                                    System.out.println("Name received.");
                                }
                                setId(PlayerManager.getInstance().createPlayer(((NameMessage) message).getName()));
                                sendMessage(MessageManager.getInstance().getWelcomeMessage(getId()));
                                setReady(true);
                            }
                        }
                    } else {
                        if (message.getType() == MessageTypes.HELLO) {
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
            } catch (IOException e) {
                if (connectionAlive()) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }

        void commandHandler(Message message) {
            if (debugLevel > 0) {
                System.out.println("[" + selfPlayerId + "] --> " + message);
            }
        }
    }

    private boolean connectionAlive() {
        return socket != null && socket.isConnected() && !socket.isClosed() && (socket.isInputShutdown() || socket.isOutputShutdown());
    }

    long getId() {
        return selfPlayerId;
    }

    private void setId(long id) {
        selfPlayerId = id;
    }

    boolean isReady() {
        return ready;
    }

    private void setReady(boolean ready) {
        this.ready = ready;
    }
}
