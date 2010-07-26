package client;

import java.awt.Point;
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

    private Socket serverSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private final LinkedBlockingDeque<String> commands = new LinkedBlockingDeque<String>();
    private Executor executor;
    
    private GameField field;

    public ServerInteraction(GameField field, Executor executor, String ip, int port) {
        try {
            serverSocket = new Socket(ip, port);
            out = new PrintWriter(new OutputStreamWriter(serverSocket.getOutputStream(), "UTF-8"), true);
            in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream(), "UTF-8"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        this.executor = executor;
        this.field = field;
    }

    public void run(String nick) throws Exception {
        String command = null;

        synchronized(commands) {
            commands.clear();
            command = "(hello)";
            out.println(command);
            System.out.println("--> " + command);
            command = "(nick \"" + nick + "\")";
            out.println(command);
            System.out.println("--> " + command);

            try {
                command = in.readLine();
                System.out.println("<-- " + command);
                commandHandler(command);
                if (field.getSelfPlayer() == null) {
                    throw new Exception("Self player was not created. May be server was not respond correctly. ");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        executor.execute(new ServerWriterTask());
        executor.execute(new ServerReaderTask());
    }

    public void addCommand(String command) {
        synchronized(commands) {
            commands.offer(command);
            commands.notify();
        }
    }

    public void disconnect() {
        if (serverSocket.isConnected()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
        }
    }

    private void commandHandler(String command) {
        String[] pieces;

        if (command.startsWith("(")) {
            command = command.substring(1, command.length() - 1);
        }
        pieces = command.split(" ");

        if ("hello".equals(pieces[0])) {
            pieces = command.split(" ", 4);
            serverStartTime = System.currentTimeMillis() - Long.parseLong(pieces[1]);
            int id = Integer.parseInt(pieces[2]);
            String nick = pieces[3].substring(1, pieces[3].indexOf("\"", 1));
            String[] pieces2 = pieces[3].substring(pieces[3].indexOf("\"", 1) + 2, pieces[3].length()).split(" ", 5);
            int maxHitPoints = Integer.parseInt(pieces2[0]);
            double speed = Double.parseDouble(pieces2[1]);
            int x = Integer.parseInt(pieces2[2]);
            int y = Integer.parseInt(pieces2[3]);
            Direction d = Direction.valueOf(pieces2[4].substring(1, pieces2[4].indexOf("\"", 1)));
            String set = pieces2[4].substring(pieces2[4].indexOf("\"", 1) + 3, pieces2[4].length() - 1);
            field.addSelfPlayer(new Player(id, nick, maxHitPoints, speed, x, y, d, "poring"));
        } else if ("timesync".equals(pieces[0])) {
            serverStartTime = System.currentTimeMillis() - Long.parseLong(pieces[1]);
        } else if ("newplayer".equals(pieces[0])) {
            pieces = command.split(" ", 4);
            serverStartTime = System.currentTimeMillis() - Long.parseLong(pieces[1]);
            int id = Integer.parseInt(pieces[2]);
            String nick = pieces[3].substring(1, pieces[3].indexOf("\"", 1));
            String[] pieces2 = pieces[3].substring(pieces[3].indexOf("\"", 1) + 2, pieces[3].length()).split(" ", 5);
            int maxHitPoints = Integer.parseInt(pieces2[0]);
            double speed = Double.parseDouble(pieces2[1]);
            int x = Integer.parseInt(pieces2[2]);
            int y = Integer.parseInt(pieces2[3]);
            Direction d = Direction.valueOf(pieces2[4].substring(1, pieces2[4].indexOf("\"", 1)));
            String set = pieces2[4].substring(pieces2[4].indexOf("\"", 1) + 3, pieces2[4].length() - 1);
            field.addPlayer(new Player(id, nick, maxHitPoints, speed, x, y, d, set));
        } else if ("move".equals(pieces[0])) {
            long begTime = Long.parseLong(pieces[2]);
            Player p = field.getPlayer(Long.parseLong(pieces[1]));
            serverStartTime = System.currentTimeMillis() - begTime;
            p.move((Point) p.getCurPos().clone(), new Point(Integer.parseInt(pieces[3]), Integer.parseInt(pieces[4])), begTime);
        } else if (pieces[0].equals("delplayer")) {
            field.delPlayer(Long.parseLong(pieces[1]));
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
                        try {
                            commands.wait();
                        } catch (InterruptedException ignore) {}
                    }
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
                    commandHandler(command);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
