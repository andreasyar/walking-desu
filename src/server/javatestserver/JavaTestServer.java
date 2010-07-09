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
    private int port;
    BlockingQueue<SocketProcessor> q = new LinkedBlockingQueue<SocketProcessor>();
    private long ids = 1;
    ArrayList<Long> playerIDs = new ArrayList<Long>();

    public JavaTestServer(int port) throws IOException {
        ss = new ServerSocket(port);
        this.port = port;
    }

    void run() {
        serverThread = Thread.currentThread();
        playerIDs.add(ids++);
        playerIDs.add(ids++);
        playerIDs.add(ids++);
        playerIDs.add(ids++);
        playerIDs.add(ids++);
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
                    q.offer(processor);

                    Random r = new Random();
                    boolean found = false;
                    System.out.println(playerIDs.size());
                    for (Long id:playerIDs) {
                        for (SocketProcessor sp:q) {
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
            shutdownServer();
        }
        return s;
    }

    private synchronized void shutdownServer() {
        for (SocketProcessor s: q) {
            s.close();
        }
        if (!ss.isClosed()) {
            try {
                ss.close();
            } catch (IOException ignored) {}
        }
    }

    public void sendToAll (String[] msgs) {
        for (String msg:msgs) {
            for (SocketProcessor sp:q) {
                sp.send(msg);
            }
        }
    }

    public void sendToAllOther (String[] msgs, long excl) {
        for (String msg:msgs) {
            for (SocketProcessor sp:q) {
                if (sp.playerID != excl) {
                    sp.send(msg);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        WDTimerTask ttask = new WDTimerTask();
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
            String[] commands = new String[playerIDs.size() - q.size()];
            boolean found = false;
            for (int i = 0; i < commands.length; i++) {
                for (SocketProcessor sp:q) {
                    if (sp.playerID != 0 && sp.playerID == playerIDs.get(i)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    commands[i] = "move " + playerIDs.get(i) + " " + WDTimerTask.wdtime + " " + r.nextInt(1024) + " " + r.nextInt(768);
                }
                found = false;
            }
            sendToAll(commands);
        }
    }

    private class WDTimeSync extends TimerTask {

        public Timer timer; // TODO Where gentle stop timer?

        public WDTimeSync() {
            timer = new Timer();
            timer.schedule(this, 0, 1000); // each 1 seconds
        }

        public void run(){
            sendToAll(new String[] {"timesync " + WDTimerTask.wdtime});
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
                    pieces = line.split(" ");
                    if ("hello".equals(pieces[0])) {
                        System.out.println("Received: " + line);
                        send("hello " + ids + " " + WDTimerTask.wdtime + " " + 0 + " " + 0);
                        playerID = ids;
                        playerIDs.add(ids);
                        sendToAllOther(new String[] {"newplayer " + playerID + " " + 0 + " " + 0}, playerID);
                        for (SocketProcessor sp:q) {
                            if (sp.playerID != 0 && sp.playerID != playerID) {
                                send("newplayer " + sp.playerID + " " + sp.lastPos.x + " " + sp.lastPos.y);
                            }
                        }
                        ids++;
                    } else if ("move".equals(pieces[0])) {
                        System.out.println("Received: " + line);
                        sendToAllOther(new String[] {"move " + playerID + " " + WDTimerTask.wdtime + " " + pieces[1] + " " + pieces[2]}, playerID);
                        lastPos.move(Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]));
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
            q.remove(this);
            if (!s.isClosed()) {
                try {
                    s.close();
                } catch (IOException ignored) {}
            }
            playerIDs.remove(playerID);
            sendToAll(new String[] {"delplayer " + playerID});
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            close();
        }
    }
}

class WDTimerTask extends TimerTask {

    public static long wdtime = 0;
    public Timer timer; // TODO Where gentle stop timer?

    public WDTimerTask() {
        timer = new Timer();
        timer.schedule(this, 0, 10); // 1000 / 10 = 100 times per second
    }

    public void run(){
        wdtime++;
    }
}
