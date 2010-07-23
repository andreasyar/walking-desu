package client;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Executor;
import javax.swing.SwingWorker;

public class ServerInteraction {
    public static long serverStartTime = 0;

    private static ServerInteraction instance = null;

    private Socket serverSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    private final LinkedBlockingDeque<String> commands = new LinkedBlockingDeque<String>();

    public static ServerInteraction run(Executor executor, String ip, int port) {
        if (instance == null) {
            instance = new ServerInteraction(executor, ip, port);
        }

        return instance;
    }

    private ServerInteraction(Executor executor, String ip, int port) {
        try {
            serverSocket = new Socket(ip, port);
            out = new PrintWriter(new OutputStreamWriter(serverSocket.getOutputStream(), "UTF-8"), true);
            in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream(), "UTF-8"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        executor.execute(new ServerWriterTask());
        executor.execute(new ServerReaderTask());
    }

    public void addCommand(String command) {
        synchronized(commands) {
            commands.offer(command);
        }
    }

    private class ServerWriterTask extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() {
            String command;

            try {
                while (serverSocket.isConnected()) {
                    synchronized(commands) {
                        while (commands.size() > 0) {
                            command = commands.poll();
                            out.println(command);
                            System.out.println("--> " + command);
                        }
                    }
                    try {
                        commands.wait();
                    } catch (InterruptedException ignored) {}
                }

                out.close();
                in.close();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class ServerReaderTask extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() {
            String command;

            try {
                while (serverSocket.isConnected()) {
                    command = in.readLine();
                    System.out.println("<-- " + command);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
