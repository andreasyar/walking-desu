package client;

import common.BoltMessage;
import common.GoldCoinMessage;
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
import common.Message;
import common.HitMessage;
import common.HMapMessage;
import common.OtherMessage;
import common.MessageType;
import common.MoveMessage;
import common.PickupGoldCoinItem;

import common.WanderingServerTime;
import common.messages.InventoryAddGoldCoin;
import common.messages.InventoryDelGoldCoin;
import common.messages.AddGoldCoin;
import common.messages.DelGoldCoin;
import common.messages.Pickup;
import java.io.ObjectOutputStream;

public class ServerInteraction {
    public static long serverStartTime = 0;

    private Socket serverSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private final LinkedBlockingDeque<Message> commands = new LinkedBlockingDeque<Message>();
    private Executor executor;
    private ObjectOutputStream oos = null;
    
    private GameField field;

    public ServerInteraction(GameField field, Executor executor, String ip, int port) {
        try {
            serverSocket = new Socket(ip, port);
            out = new PrintWriter(new OutputStreamWriter(serverSocket.getOutputStream(), "UTF-8"), true);
            in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream(), "UTF-8"));
            oos = new ObjectOutputStream(serverSocket.getOutputStream());
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
            oos.writeObject(new OtherMessage(command));
            System.out.println("--> " + command);
            command = "(nick \"" + nick + "\")";
            oos.writeObject(new OtherMessage(command));
            System.out.println("--> " + command);
        }
        executor.execute(new ServerReaderTask());
        executor.execute(new ServerWriterTask());
    }

    public void addCommand(String command) {
        synchronized(commands) {
            commands.offer(new OtherMessage(command));
            //System.out.println("Command added " + command);
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
            field.asyncAddPlayer(p);
        } else if ("delplayer".equals(pieces1[0])) {
            field.asyncDelPlayer(Long.parseLong(pieces1[1]));
        } else if ("message".equals(pieces1[0])) {
            pieces1 = command.split(" ", 3);
            Player p = field.getPlayer(Long.parseLong(pieces1[1]));

            if (p != null) {
                p.setText(pieces1[2].substring(1, pieces1[2].length() - 1));
            }
        } else if ("teleport".equals(pieces1[0])) {
            Player p = field.getPlayer(Long.parseLong(pieces1[1]));

            if (p != null) {
                p.teleportTo(Integer.parseInt(pieces1[2]), Integer.parseInt(pieces1[3]));
            }
        } else if ("heal".equals(pieces1[0])) {
            Player p = field.getPlayer(Long.parseLong(pieces1[1]));

            if (p != null) {
                p.doHeal(Integer.parseInt(pieces1[2]));
            }
        } else if ("newmonster".equals(pieces1[0])) {
            String[] pieces2;

            pieces1 = command.split(" ", 7);
            pieces2 = pieces1[6].substring(1, pieces1[6].length() - 1).split("\" \"", 3);
            field.asyncAddMonster(new Monster(Integer.parseInt(pieces1[1]),
                    pieces2[0],
                    Integer.parseInt(pieces1[2]),
                    Double.parseDouble(pieces1[3]),
                    Integer.parseInt(pieces1[4]),
                    Integer.parseInt(pieces1[5]),
                    Direction.valueOf(pieces2[1]),
                    pieces2[2]));
        } else if ("delmonster".equals(pieces1[0])) {
            field.asyncDelMonster(Long.parseLong(pieces1[1]));
        } else if ("deathmonster".equals(pieces1[0])) {
            WUnit selected = field.getSelfPlayer().getSelectedUnit();

            Monster m = field.getMonster(Long.parseLong(pieces1[1]));
            if (m != null) {
                m.kill();
                if (selected != null && m.equals(selected)) {
                    field.getSelfPlayer().unselectUnit();
                }
            }
        } else if ("deathplayer".equals(pieces1[0])) {
            WUnit selected = field.getSelfPlayer().getSelectedUnit();

            Player p = field.getPlayer(Long.parseLong(pieces1[1]));
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
            field.asyncAddTower(t);
        } else if ("monsterloss".equals(pieces1[0])) {
            pieces1 = command.split(" ");
            field.setTDStatus(Integer.parseInt(pieces1[1]) + "/" + Integer.parseInt(pieces1[2]) + " x" + Integer.parseInt(pieces1[3]));
        } else if ("deltower".equals(pieces1[0])) {
            field.asyncDelTower(Long.parseLong(pieces1[1]));
        } else if ("attack".equals(pieces1[0])) {
            WUnit selected;

            WUnit attacker = field.getUnit(Long.parseLong(pieces1[1]));
            WUnit target = field.getUnit(Long.parseLong(pieces1[2]));
            long begTime = Long.parseLong(pieces1[3]);

            if (attacker != null && target != null) {
                selected = attacker.getSelectedUnit();
                if (selected == null || !selected.equals(target)) {
                    attacker.selectUnit(target);
                }
                if (attacker.attack(begTime)) {
                    field.asyncAddNuke(attacker, target, begTime + attacker.getNukeAnimationDelay());
                }
            }
            /*if (self.attack(System.currentTimeMillis() - ServerInteraction.serverStartTime)) {
                WanderingLocks.lockNukes();
                field.addNuke(self, self.getSelectedUnit(), System.currentTimeMillis() - ServerInteraction.serverStartTime + self.getNukeAnimationDelay());
                WanderingLocks.unlockNukes();
            }*/
        } else if ("delitem".equals(pieces1[0])) {

            WItem item = field.getItem(Long.parseLong(pieces1[1]));
            if (item != null) {
                field.asyncRemoveItem(item);
            }

        } else if ("delitemfrominv".equals(pieces1[0])) {

            field.getSelfPlayer().removeItemFromInventory(Long.parseLong(pieces1[1]));

        }

    }

    private void commandHandler(Message m) {
        if (m.getType() == MessageType.OTHER) {

            commandHandler(((OtherMessage) m).getMessage());

        } else if (m.getType() == MessageType.HMAP) {

            HMapMessage tmpMessage = (HMapMessage) m;
            field.addMapFragment(new ClientMapFragment(tmpMessage.getIdx(), tmpMessage.getIdy(), tmpMessage.getHmap()));

        } else if (m.getType() == MessageType.HIT) {

            HitMessage tmpMessage = (HitMessage) m;
            WUnit attacker = field.getUnit(tmpMessage.getAttackerID());
            WUnit target = field.getUnit(tmpMessage.getTargetID());
            if (attacker != null && target != null) {
                field.asyncAddHitAnimation(new CanonHitAnimation("canon", (Point) target.getCurPos().clone(), Direction.SOUTH, System.currentTimeMillis() - serverStartTime));
                target.doHit(tmpMessage.getDamage());
            } else {
                System.err.println("While processing HIT message, attacker or target was not found. It may be sync issue!");
            }

        } else if (m.getType() == MessageType.BOLT) {

            BoltMessage tmpMessage = (BoltMessage) m;
            WUnit attacker = field.getUnit(tmpMessage.getAttackerID());
            WUnit target = field.getUnit(tmpMessage.getTargetID());
            if (attacker != null && target != null) {
                attacker.selectUnit(target);
                field.asyncAddNuke(attacker, target, tmpMessage.getBegTime());
            } else {
                System.err.println("While processing BOLT message, attacker or target was not found. It may be sync issue!");
            }

        } else if (m.getType() == MessageType.MOVE) {

            MoveMessage tmpMessage = (MoveMessage) m;
            field.asyncMoveUnit(tmpMessage.getUnitID(),
                                tmpMessage.getBegX(),
                                tmpMessage.getBegY(),
                                tmpMessage.getEndX(),
                                tmpMessage.getEndY(),
                                tmpMessage.getBegTime());

        } else if (m.getType() == MessageType.INVADDGOLDCOIN) {

            InventoryAddGoldCoin tmpMsg = (InventoryAddGoldCoin) m;
            field.getSelfPlayer().getInventory().addGoldCoin(tmpMsg.getId(), tmpMsg.getCount());

        } else if (m.getType() == MessageType.INVDELGOLDCOIN) {

            InventoryDelGoldCoin tmpMsg = (InventoryDelGoldCoin) m;
            field.getSelfPlayer().getInventory().delGoldCoin(tmpMsg.getId(), tmpMsg.getCount());

        } else if (m.getType() == MessageType.PICKUP) {

            // TODO Not only players can pickup items!
            Pickup tmpMsg = (Pickup) m;
            Player p = field.getPlayer(tmpMsg.getPickerId());
            WItem i = field.getItem(tmpMsg.getItemId());
            if (p != null && i != null) {
                p.pickup(i);
                field.asyncRemoveItem(i);
            }

        } else if (m.getType() == MessageType.ADDGOLDCOIN) {

            AddGoldCoin tmpMsg = (AddGoldCoin) m;
            WGoldCoinItem g = new WGoldCoinItem(tmpMsg.getId(),
                                                tmpMsg.getCount());
            g.setX(tmpMsg.getX());
            g.setY(tmpMsg.getY());
            g.setOnGround(true);
            field.addItem(g);

        } else if (m.getType() == MessageType.DELGOLDCOIN) {

            DelGoldCoin tmpMsg = (DelGoldCoin) m;
            WItem i = field.getItem(tmpMsg.getId());
            if (i != null) {
                field.asyncRemoveItem(i);
            }

        }
    }

    public void addCommand(Message m) {
        synchronized(commands) {
            commands.offer(m);
            //System.out.println("Message added " + m);
            commands.notify();
        }
    }

    private class ServerWriterTask extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() {
            Message command;

            try {
                while (serverSocket.isConnected()) {
                    synchronized(commands) {
                        while (commands.size() > 0) {
                            command = commands.poll();
                            oos.writeObject(command);
                            oos.reset();
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
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class ServerReaderTask extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() {
            Message command;
            ObjectInputStream ois;
            LinkedBlockingQueue<Message> messages;

            try {
                ois = new ObjectInputStream(serverSocket.getInputStream());

                while (serverSocket.isConnected()) {
                    //command = in.readLine();
                    messages = (LinkedBlockingQueue<Message>) ois.readObject();
                    for (Message m : messages) {
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
