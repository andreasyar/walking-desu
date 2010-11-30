package client;

import java.awt.Point;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.SwingWorker;
import server.javatestserver.JTSMessage;
import server.javatestserver.ShortMapFragment;
import server.javatestserver.JTSMessageTypes;

import common.WanderingServerTime;

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

            /*try {
                command = in.readLine();
                System.out.println("<-- " + command);
                commandHandler(command);
                if (field.getSelfPlayer() == null) {
                    throw new Exception("Self player was not created. May be server was not respond correctly. ");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }*/
        }
        executor.execute(new ServerReaderTask());
        executor.execute(new ServerWriterTask());
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
        String[] pieces1;

        if (command.startsWith("(")) {
            command = command.substring(1, command.length() - 1);
        }
        pieces1 = command.split(" ");

        if ("hello".equals(pieces1[0])) {
            String[] pieces2;
            Player p;

            pieces1 = command.split(" ", 8);
            pieces2 = pieces1[7].substring(1, pieces1[7].length() - 1).split("\" \"", 3);
            serverStartTime = System.currentTimeMillis() - Long.parseLong(pieces1[6]);

            // We initialize server time counter here. Hello message contains requied value.
            WanderingServerTime.getInstance().setServerTime(System.currentTimeMillis() - Long.parseLong(pieces1[6]));

            p = new Player(Integer.parseInt(pieces1[1]),
                    pieces2[0],
                    Integer.parseInt(pieces1[2]),
                    Double.parseDouble(pieces1[3]),
                    Integer.parseInt(pieces1[4]),
                    Integer.parseInt(pieces1[5]),
                    Direction.valueOf(pieces2[1]),
                    pieces2[2]);
            field.addSelfPlayer(p);
            p.setCurrentNuke(new PeasantNuke(p, p.getNukeAnimationDelay()));
        } else if ("timesync".equals(pieces1[0])) {
            serverStartTime = System.currentTimeMillis() - Long.parseLong(pieces1[1]);

            // Every time when we recv timesync we must update our local server time.
            WanderingServerTime.getInstance().setServerTime(System.currentTimeMillis() - Long.parseLong(pieces1[1]));

        } else if ("newplayer".equals(pieces1[0])) {
            String[] pieces2;
            Player p;

            pieces1 = command.split(" ", 7);
            pieces2 = pieces1[6].substring(1, pieces1[6].length() - 1).split("\" \"", 3);
            p = new Player(Integer.parseInt(pieces1[1]),
                    pieces2[0],
                    Integer.parseInt(pieces1[2]),
                    Double.parseDouble(pieces1[3]),
                    Integer.parseInt(pieces1[4]),
                    Integer.parseInt(pieces1[5]),
                    Direction.valueOf(pieces2[1]),
                    pieces2[2]);
            p.setCurrentNuke(new PeasantNuke(p, p.getNukeAnimationDelay()));
            WanderingLocks.lockAll();
            field.addPlayer(p);
            WanderingLocks.unlockAll();
        } else if ("move".equals(pieces1[0])) {
            long begTime = Long.parseLong(pieces1[2]);
            WanderingLocks.lockAll();
            WUnit u = field.getUnit(Long.parseLong(pieces1[1]));
            WanderingLocks.unlockAll();
            //DONT DO IT!serverStartTime = System.currentTimeMillis() - begTime;
            if (u != null) {
                u.move(Integer.parseInt(pieces1[3]), Integer.parseInt(pieces1[4]), Integer.parseInt(pieces1[5]), Integer.parseInt(pieces1[6]), begTime);
            }
        } else if ("delplayer".equals(pieces1[0])) {
            WanderingLocks.lockAll();
            field.delPlayer(Long.parseLong(pieces1[1]));
            WanderingLocks.unlockAll();
        } else if ("message".equals(pieces1[0])) {
            pieces1 = command.split(" ", 3);
            WanderingLocks.lockPlayers();
            Player p = field.getPlayer(Long.parseLong(pieces1[1]));
            WanderingLocks.unlockPlayers();

            if (p != null) {
                p.setText(pieces1[2].substring(1, pieces1[2].length() - 1));
            }
        } else if ("bolt".equals(pieces1[0])) {
            WanderingLocks.lockAll();
            WUnit attacker = field.getUnit(Long.parseLong(pieces1[1]));
            WUnit target = field.getUnit(Long.parseLong(pieces1[2]));
            WanderingLocks.unlockAll();
            long begTime = Long.parseLong(pieces1[3]);

            if (attacker != null && target != null) {
                attacker.selectUnit(target);
                WanderingLocks.lockNukes();
                field.addNuke(attacker, target, begTime);
                WanderingLocks.unlockNukes();
            }
        } else if ("hit".equals(pieces1[0])) {
            WanderingLocks.lockAll();
            WUnit attacker = field.getUnit(Long.parseLong(pieces1[1]));
            WUnit target = field.getUnit(Long.parseLong(pieces1[2]));
            WanderingLocks.unlockAll();

            if (attacker != null && target != null) {
                WanderingLocks.lockHits();
                field.addHitAnimation(new CanonHitAnimation("canon", (Point) target.getCurPos().clone(), Direction.SOUTH, System.currentTimeMillis() - serverStartTime));
                WanderingLocks.unlockHits();
                target.doHit(Integer.parseInt(pieces1[3]));
            }
        } else if ("teleport".equals(pieces1[0])) {
            WanderingLocks.lockPlayers();
            Player p = field.getPlayer(Long.parseLong(pieces1[1]));
            WanderingLocks.unlockPlayers();

            if (p != null) {
                p.teleportTo(Integer.parseInt(pieces1[2]), Integer.parseInt(pieces1[3]));
            }
        } else if ("heal".equals(pieces1[0])) {
            WanderingLocks.lockPlayers();
            Player p = field.getPlayer(Long.parseLong(pieces1[1]));
            WanderingLocks.unlockPlayers();

            if (p != null) {
                p.doHeal(Integer.parseInt(pieces1[2]));
            }
        } else if ("newmonster".equals(pieces1[0])) {
            String[] pieces2;

            pieces1 = command.split(" ", 7);
            pieces2 = pieces1[6].substring(1, pieces1[6].length() - 1).split("\" \"", 3);
            WanderingLocks.lockAll();
            field.addMonster(new Monster(Integer.parseInt(pieces1[1]),
                    pieces2[0],
                    Integer.parseInt(pieces1[2]),
                    Double.parseDouble(pieces1[3]),
                    Integer.parseInt(pieces1[4]),
                    Integer.parseInt(pieces1[5]),
                    Direction.valueOf(pieces2[1]),
                    pieces2[2]));
            WanderingLocks.unlockAll();
        } else if ("delmonster".equals(pieces1[0])) {
            WanderingLocks.lockAll();
            field.delMonster(Long.parseLong(pieces1[1]));
            WanderingLocks.unlockAll();
        } else if ("deathmonster".equals(pieces1[0])) {
            WUnit selected = field.getSelfPlayer().getSelectedUnit();

            WanderingLocks.lockMonsters();
            Monster m = field.getMonster(Long.parseLong(pieces1[1]));
            WanderingLocks.unlockMonsters();
            if (m != null) {
                m.kill();
                if (selected != null && m.equals(selected)) {
                    field.getSelfPlayer().unselectUnit();
                }
            }
        } else if ("deathplayer".equals(pieces1[0])) {
            WUnit selected = field.getSelfPlayer().getSelectedUnit();

            WanderingLocks.lockPlayers();
            Player p = field.getPlayer(Long.parseLong(pieces1[1]));
            WanderingLocks.unlockPlayers();
            if (p != null) {
                p.kill();
                if (selected != null && p.equals(selected)) {
                    field.getSelfPlayer().unselectUnit();
                }
            }
        } else if ("tower".equals(pieces1[0])) {
            pieces1 = command.split(" ", 7);
            pieces1[6] = pieces1[6].substring(1, pieces1[6].length() - 1);
            Tower t = new Tower(Integer.parseInt(pieces1[1]),
                    pieces1[6],
                    Double.parseDouble(pieces1[2]),
                    Integer.parseInt(pieces1[3]),
                    0,
                    Integer.parseInt(pieces1[4]),
                    Integer.parseInt(pieces1[5]),
                    Direction.SOUTH,
                    "tower");
            t.setCurrentNuke(new CanonNuke(t));
            WanderingLocks.lockAll();
            field.addTower(t);
            WanderingLocks.unlockAll();
        } else if ("monsterloss".equals(pieces1[0])) {
            pieces1 = command.split(" ");
            field.setTDStatus(Integer.parseInt(pieces1[1]) + "/" + Integer.parseInt(pieces1[2]) + " x" + Integer.parseInt(pieces1[3]));
        } else if ("deltower".equals(pieces1[0])) {
            WanderingLocks.lockAll();
            field.delTower(Long.parseLong(pieces1[1]));
            WanderingLocks.unlockAll();
        } else if ("attack".equals(pieces1[0])) {
            WUnit selected;

            WanderingLocks.lockAll();
            WUnit attacker = field.getUnit(Long.parseLong(pieces1[1]));
            WUnit target = field.getUnit(Long.parseLong(pieces1[2]));
            WanderingLocks.unlockAll();
            long begTime = Long.parseLong(pieces1[3]);

            if (attacker != null && target != null) {
                selected = attacker.getSelectedUnit();
                if (selected == null || !selected.equals(target)) {
                    attacker.selectUnit(target);
                }
                if (attacker.attack(begTime)) {
                    WanderingLocks.lockNukes();
                    field.addNuke(attacker, target, begTime + attacker.getNukeAnimationDelay());
                    WanderingLocks.unlockNukes();
                }
            }
            /*if (self.attack(System.currentTimeMillis() - ServerInteraction.serverStartTime)) {
                WanderingLocks.lockNukes();
                field.addNuke(self, self.getSelectedUnit(), System.currentTimeMillis() - ServerInteraction.serverStartTime + self.getNukeAnimationDelay());
                WanderingLocks.unlockNukes();
            }*/
        }
    }

    private void commandHandler(JTSMessage command) {
        if (command.getType() == JTSMessageTypes.OTHER) {
            commandHandler((String) command.getData());
        } else if (command.getType() == JTSMessageTypes.HMAP) {
            ShortMapFragment tmpMapFrag = (ShortMapFragment) command.getData();
            field.addMapFragment(new ClientMapFragment(tmpMapFrag.idx, tmpMapFrag.idy, tmpMapFrag.hmap));
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
            JTSMessage command;
            ObjectInputStream ois;
            LinkedBlockingQueue<JTSMessage> messages;

            try {
                ois = new ObjectInputStream(serverSocket.getInputStream());

                while (serverSocket.isConnected()) {
                    //command = in.readLine();
                    messages = (LinkedBlockingQueue<JTSMessage>) ois.readObject();
                    for (JTSMessage m : messages) {
                        command = m;
                        System.out.println("<-- " + command);
                        try {
                            commandHandler(command);
                            if (field.getSelfPlayer() == null) {
                                throw new Exception("Self player was not created. May be server was not respond correctly. ");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return null;
        }
    }
}
