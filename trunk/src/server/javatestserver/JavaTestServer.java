package server.javatestserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.util.ArrayList;
import java.awt.Point;

public class JavaTestServer {
    private ServerSocket ss;
    private Thread serverThread;
    BlockingQueue<SocketProcessor> clientQueue = new LinkedBlockingQueue<SocketProcessor>();

    private long currentID = 1;
    ArrayList<Long> playerIDs = new ArrayList<Long>();

    private long serverStartTime;

    public JavaTestServer(int port) throws IOException {
        ss = new ServerSocket(port);
        serverStartTime = System.currentTimeMillis();
    }

    void run() {
        serverThread = Thread.currentThread();

        /*playerIDs.add(currentID++);
        playerIDs.add(currentID++);
        playerIDs.add(currentID++);
        playerIDs.add(currentID++);
        playerIDs.add(currentID++);*/

        WDBotMoveTask bmt = new WDBotMoveTask();
        WDTimeSync ts = new WDTimeSync();

        while (true) {
            Socket s = getNewConn();

            if (serverThread.isInterrupted()) {
                break;
            } else if (s != null){
                try {
                    final SocketProcessor processor = new SocketProcessor(s);
                    final Thread thread = new Thread(processor);
                    thread.setDaemon(true);
                    thread.start();
                    clientQueue.offer(processor);

                    Random r = new Random();
                    boolean found = false;
                    System.out.println(playerIDs.size());
                    for (Long id:playerIDs) {
                        for (SocketProcessor sp:clientQueue) {
                            if (sp.playerID == id) {
                                found = true;
                            }
                        }
                        if (!found) { // bot
                            processor.send("(newplayer " + id + " " + r.nextInt(1024) + " " + r.nextInt(768) + ")");
                        }
                        found = false;
                    }
                }
                catch (IOException ignored) {}
            }
        }
    }

    private Socket getNewConn() {
        Socket s = null;
        try {
            s = ss.accept();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return s;
    }

    public void sendToAll (String[] msgs) {
        for (String msg:msgs) {
            for (SocketProcessor sp:clientQueue) {
                sp.send(msg);
            }
        }
    }

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
        new JavaTestServer(45000).run();
    }

    private class WDBotMoveTask extends TimerTask {

        public Timer timer; // TODO Where gentle stop timer?

        public WDBotMoveTask() {
            timer = new Timer();
            timer.schedule(this, 0, 15000); // each 15 seconds
        }

        public void run(){
            Random r = new Random();
            String[] commands = new String[playerIDs.size() - clientQueue.size()];
            boolean found = false;

            for (int i = 0; i < commands.length; i++, found = false) {
                for (SocketProcessor sp:clientQueue) {
                    if (sp.playerID != 0 && sp.playerID == playerIDs.get(i)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    commands[i] = "(move " + playerIDs.get(i) + " "
                            + (System.currentTimeMillis() - serverStartTime)
                            + " " + r.nextInt(1024) + " " + r.nextInt(768) + ")";
                }
            }

            sendToAll(commands);
        }
    }

    private class WDTimeSync extends TimerTask {

        public Timer timer; // TODO Where gentle stop timer?

        public WDTimeSync() {
            timer = new Timer();
            timer.schedule(this, 0, 5000); // each 5 seconds
        }

        public void run(){
            sendToAll(new String[] {"(timesync " + (System.currentTimeMillis() - serverStartTime) + ")"});
        }
    }

    private class SocketProcessor implements Runnable{
        Socket s;
        BufferedReader br;
        BufferedWriter bw;

        long playerID = 0;
        Point lastPos = new Point(0, 0);

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
                    if (line.startsWith("(")) {
                        line = line.substring(1, line.length() - 1); // remove ( )
                    }
                    pieces = line.split(" ");
                    if ("hello".equals(pieces[0])) {
                        System.out.println("Received: " + line);
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
                            }
                        }
                        currentID++;
                    } else if ("move".equals(pieces[0])) {
                        System.out.println("Received: " + line);
                        sendToAllExcept(new String[] {"(move " + playerID + " "
                                + (System.currentTimeMillis() - serverStartTime)
                                + " " + pieces[1] + " " + pieces[2] + ")"}, playerID);
                        lastPos.move(Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]));
                    } else if ("message".equals(pieces[0])) {
                        pieces = line.split(" ", 2);
                        sendToAllExcept(new String[] {"(message " + playerID + " \""
                                + pieces[1] + "\")"}, playerID);
                    } else {
                        System.out.println("Received: " + line);
                    }
                }
            }
        }

        public synchronized void send(String line) {
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
