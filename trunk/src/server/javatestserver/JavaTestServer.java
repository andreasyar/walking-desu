package server.javatestserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.awt.Point;

/**
 * Главный класс тестового ява сервера блуждающей же.
 */
public class JavaTestServer {
    private ServerSocket ss;
    private Thread serverThread;
    BlockingQueue<SocketProcessor> clientQueue = new LinkedBlockingQueue<SocketProcessor>();

    private long currentID = 1;
    ArrayList<Long> playerIDs = new ArrayList<Long>();

    private static final long serverStartTime = System.currentTimeMillis();
    private static final int port = 45000;

    public JavaTestServer(int port) throws IOException {
        ss = new ServerSocket(port);
    }

    /**
     * Главный цикл тестового ява сервера.
     */
    void run() {
        serverThread = Thread.currentThread();
        WDTimeSync ts = new WDTimeSync();

        while (true) {
            Socket s = getNewConnection();

            if (serverThread.isInterrupted()) {
                break;
            } else if (s != null){
                try {
                    final SocketProcessor processor = new SocketProcessor(s);
                    final Thread thread = new Thread(processor);
                    thread.setDaemon(true);
                    thread.start();
                    clientQueue.offer(processor);
                }
                catch (IOException ignored) {}
            }
        }
    }

    /**
     * Ждет новых подключений к серверу. Возвращает новое подключение.
     */
    private Socket getNewConnection() {
        Socket cs = null;
        try {
            cs = ss.accept();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return cs;
    }

    /**
     * Отправляет заданные сообщения всем пользователям.
     * @param msgs Сообщения.
     */
    public void sendToAll (String[] msgs) {
        for (String msg:msgs) {
            for (SocketProcessor sp:clientQueue) {
                sp.send(msg);
            }
        }
    }

    /**
     * Отправляет заданные сообщения всем пользователям, за исключем
     * пользователя с заданным ID.
     * @param msgs Сообщения.
     * @param excID ID исключаемого из рассылки игрока.
     */
    public void sendToAllExcept (String[] msgs, long excID) {
        for (String msg:msgs) {
            for (SocketProcessor sp:clientQueue) {
                if (sp.playerID != excID) {
                    sp.send(msg);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Server starts at port: " + port);
        new JavaTestServer(port).run();
    }

    private class WDTimeSync extends TimerTask {

        public Timer timer; // TODO Where gentle stop timer?

        public WDTimeSync() {
            timer = new Timer();
            timer.schedule(this, 0, 5000); // each 5 seconds
        }

        public void run(){
            sendToAll(new String[] {"(timesync "
                    + (System.currentTimeMillis() - serverStartTime) + ")"});
        }
    }

    private class SocketProcessor implements Runnable{
        Socket s;
        BufferedReader br;
        BufferedWriter bw;

        long playerID = 0;
        Point lastPos = new Point(0, 0);
        String msg = null;
        String nick = null;

        SocketProcessor(Socket socketParam) throws IOException {
            s = socketParam;
            br = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
            bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8") );
        }

        public void run() {
            while (!s.isClosed()) {
                String line = null;
                String[] pieces;

                try {
                    line = br.readLine();
                } catch (IOException e) {
                    close();
                }

                if (line == null) {
                    close();
                } else if (line.length() > 0) {
                    System.out.println(s.getInetAddress() + " --> " + line);
                    if (line.startsWith("(")) {
                        line = line.substring(1, line.length() - 1); // remove ( )
                    }
                    pieces = line.split(" ");
                    if ("hello".equals(pieces[0])) {
                        send("(hello " + currentID + " "
                                + (System.currentTimeMillis() - serverStartTime)
                                + " " + 0 + " " + 0 + ")");
                        playerID = currentID;
                        playerIDs.add(currentID);
                        sendToAllExcept(new String[] {"(newplayer " + playerID
                                + " " + 0 + " " + 0 + ")"}, playerID);
                        for (SocketProcessor sp:clientQueue) {
                            if (sp.playerID != 0 && sp.playerID != playerID) {
                                send("(newplayer " + sp.playerID + " "
                                        + sp.lastPos.x + " " + sp.lastPos.y + ")");
                                if (sp.msg != null) {
                                    send("(message " + sp.playerID + " "
                                            + sp.msg + ")");
                                }
                                if (sp.nick != null) {
                                    send("(nick " + sp.playerID + " "
                                            + sp.nick + ")");
                                }
                            }
                        }
                        currentID++;
                    } else if ("move".equals(pieces[0])) {
                        sendToAllExcept(new String[] {"(move " + playerID + " "
                                + (System.currentTimeMillis() - serverStartTime)
                                + " " + pieces[1] + " " + pieces[2] + ")"}, playerID);
                        lastPos.move(Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]));
                    } else if ("message".equals(pieces[0])) {
                        pieces = line.split(" ", 2);
                        sendToAllExcept(new String[] {"(message " + playerID + " "
                                + pieces[1] + ")"}, playerID);
                        msg = pieces[1];
                    } else if ("nick".equals(pieces[0])) {
                        pieces = line.split(" ", 2);
                        sendToAllExcept(new String[] {"(nick " + playerID + " "
                                + pieces[1] + ")"}, playerID);
                        nick = pieces[1];
                    } else if ("bolt".equals(pieces[0])) {
                        pieces = line.split(" ");
                        sendToAllExcept(new String[] {"(bolt " + playerID + " "
                                + pieces[1] + " " + (System.currentTimeMillis() - serverStartTime) + ")"}, playerID);
                        nick = pieces[1];
                    }
                }
            }
        }

        public synchronized void send(String line) {
            System.out.println(s.getInetAddress() + " <-- " + line);
            try {
                bw.write(line);
                bw.write("\n");
                bw.flush();
            } catch (IOException e) {
                close();
            }
        }

        public synchronized void close() {
            clientQueue.remove(this);
            if (!s.isClosed()) {
                try {
                    s.close();
                } catch (IOException ignored) {}
            }
            playerIDs.remove(playerID);
            sendToAll(new String[] {"(delplayer " + playerID  + ")"});
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            close();
        }
    }
}