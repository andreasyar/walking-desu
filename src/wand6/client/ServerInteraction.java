package wand6.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import javax.swing.SwingWorker;
import wand6.common.messages.Message;
import wand6.common.messages.MessageType;

public class ServerInteraction {

    private static int debugLevel = 1;
    private Socket socket;
    private MessageSender sender;
    private MessageReceiver receiver;
    private boolean welcomeReceived = false;

    public ServerInteraction(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            sender = new MessageSender(new ObjectOutputStream(socket.getOutputStream()));
            receiver = new MessageReceiver(new ObjectInputStream(socket.getInputStream()));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void run() {
        Executor executor = Executors.newCachedThreadPool();
        executor.execute(sender);
        executor.execute(receiver);
    }

    public void sendMessage(Message message) {
        sender.sendMessage(message);
    }

    private class MessageSender extends SwingWorker<Void, Void> {

        private final LinkedBlockingDeque<Message> messages = new LinkedBlockingDeque<Message>();
        private ObjectOutputStream outStream;

        public MessageSender(ObjectOutputStream outStream) {
            this.outStream = outStream;
        }

        @Override
        protected Void doInBackground() {
            try {
                Message message;

                if (debugLevel > 0) {
                    System.out.println("Message sender thread started.");
                }

                while (true) {
                    while (!messages.isEmpty()) {
                        message = messages.poll();
                        outStream.writeObject(message);
                        outStream.reset();
                        if (debugLevel > 0) {
                            System.out.println(message + " --> [server]");
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

            return null;
        }

        void sendMessage(Message message) {
            messages.add(message);
            synchronized(messages) {
                messages.notify();
            }
        }
    }

    private class MessageReceiver extends SwingWorker<Void, Void> {

        private ObjectInputStream inStream;

        private MessageReceiver(ObjectInputStream inStream) {
            this.inStream = inStream;
        }

        @Override
        protected Void doInBackground() {
            try {
                Message message;

                if (debugLevel > 0) {
                    System.out.println("Message receiver thread started.");
                }

                while (true) {
                    message = (Message) inStream.readObject();

                    if (welcomeReceived) {

                        commandHandler(message);

                    } else {
                        if (message.getType() == MessageType.WELCOME) {
                            welcomeReceived = true;
                            commandHandler(message);
                        }
                    }
                }
            } catch (IOException e) {
                if (connectionAlive()) {
                    e.printStackTrace();
                    System.exit(1);
                } else {
                    System.err.println("Connection lost.");
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return null;
        }

        private void commandHandler(String command) {
//            String[] pieces1;
//
//            if (command.startsWith("(")) {
//                command = command.substring(1, command.length() - 1);
//            }
//            pieces1 = command.split(" ");
//
//            if ("hello".equals(pieces1[0])) {
//                String[] pieces2;
//                Player p;
//
//                pieces1 = command.split(" ", 8);
//                pieces2 = pieces1[7].substring(1, pieces1[7].length() - 1).split("\" \"", 3);
//                serverStartTime = System.currentTimeMillis() - Long.parseLong(pieces1[6]);
//
//                // We initialize server time counter here. Hello message contains requied value.
//                WanderingServerTime.getInstance().setServerTime(System.currentTimeMillis() - Long.parseLong(pieces1[6]));
//
//                p = new Player(Integer.parseInt(pieces1[1]),
//                        pieces2[0],
//                        Integer.parseInt(pieces1[2]),
//                        Double.parseDouble(pieces1[3]),
//                        Integer.parseInt(pieces1[4]),
//                        Integer.parseInt(pieces1[5]),
//                        Direction.valueOf(pieces2[1]),
//                        pieces2[2]);
//                field.addSelfPlayer(p);
//                p.setCurrentNuke(new PeasantNuke(p, p.getNukeAnimationDelay()));
//            } else if ("timesync".equals(pieces1[0])) {
//                serverStartTime = System.currentTimeMillis() - Long.parseLong(pieces1[1]);
//
//                // Every time when we recv timesync we must update our local server time.
//                WanderingServerTime.getInstance().setServerTime(System.currentTimeMillis() - Long.parseLong(pieces1[1]));
//
//            } else if ("newplayer".equals(pieces1[0])) {
//                String[] pieces2;
//                Player p;
//
//                pieces1 = command.split(" ", 7);
//                pieces2 = pieces1[6].substring(1, pieces1[6].length() - 1).split("\" \"", 3);
//                p = new Player(Integer.parseInt(pieces1[1]),
//                        pieces2[0],
//                        Integer.parseInt(pieces1[2]),
//                        Double.parseDouble(pieces1[3]),
//                        Integer.parseInt(pieces1[4]),
//                        Integer.parseInt(pieces1[5]),
//                        Direction.valueOf(pieces2[1]),
//                        pieces2[2]);
//                p.setCurrentNuke(new PeasantNuke(p, p.getNukeAnimationDelay()));
//                field.asyncAddPlayer(p);
//            } else if ("delplayer".equals(pieces1[0])) {
//                field.asyncDelPlayer(Long.parseLong(pieces1[1]));
//            } else if ("message".equals(pieces1[0])) {
//                pieces1 = command.split(" ", 3);
//                Player p = field.getPlayer(Long.parseLong(pieces1[1]));
//
//                if (p != null) {
//                    p.setText(pieces1[2].substring(1, pieces1[2].length() - 1));
//                }
//            } else if ("teleport".equals(pieces1[0])) {
//                Player p = field.getPlayer(Long.parseLong(pieces1[1]));
//
//                if (p != null) {
//                    p.teleportTo(Integer.parseInt(pieces1[2]), Integer.parseInt(pieces1[3]));
//                }
//            } else if ("heal".equals(pieces1[0])) {
//                Player p = field.getPlayer(Long.parseLong(pieces1[1]));
//
//                if (p != null) {
//                    p.doHeal(Integer.parseInt(pieces1[2]));
//                }
//            } else if ("newmonster".equals(pieces1[0])) {
//                String[] pieces2;
//
//                pieces1 = command.split(" ", 7);
//                pieces2 = pieces1[6].substring(1, pieces1[6].length() - 1).split("\" \"", 3);
//                Monster m = new Monster(Integer.parseInt(pieces1[1]),
//                        pieces2[0],
//                        Integer.parseInt(pieces1[2]),
//                        Double.parseDouble(pieces1[3]),
//                        Integer.parseInt(pieces1[4]),
//                        Integer.parseInt(pieces1[5]),
//                        Direction.valueOf(pieces2[1]),
//                        pieces2[2]);
//                field.asyncAddMonster(m);
//            } else if ("delmonster".equals(pieces1[0])) {
//                field.asyncDelMonster(Long.parseLong(pieces1[1]));
//            } else if ("deathmonster".equals(pieces1[0])) {
//                WUnit selected = field.getSelfPlayer().getSelectedUnit();
//
//                Monster m = field.getMonster(Long.parseLong(pieces1[1]));
//                if (m != null) {
//                    m.kill();
//                    if (selected != null && m.equals(selected)) {
//                        field.getSelfPlayer().unselectUnit();
//                    }
//                }
//            } else if ("deathplayer".equals(pieces1[0])) {
//
//                WUnit selected = field.getSelfPlayer().getSelectedUnit();
//                Player p = field.getPlayer(Long.parseLong(pieces1[1]));
//                if (p != null) {
//                    p.kill();
//                    if (selected != null && p.equals(selected)) {
//                        field.getSelfPlayer().unselectUnit();
//                    }
//                }
//
//            } else if ("tower".equals(pieces1[0])) {
//                pieces1 = command.split(" ", 7);
//                pieces1[6] = pieces1[6].substring(1, pieces1[6].length() - 1);
//                Tower t = new Tower(Integer.parseInt(pieces1[1]),
//                        pieces1[6],
//                        Double.parseDouble(pieces1[2]),
//                        Integer.parseInt(pieces1[3]),
//                        0,
//                        Integer.parseInt(pieces1[4]),
//                        Integer.parseInt(pieces1[5]),
//                        Direction.SOUTH,
//                        "tower");
//                t.setCurrentNuke(new CanonNuke(t));
//                field.asyncAddTower(t);
//            } else if ("monsterloss".equals(pieces1[0])) {
//                pieces1 = command.split(" ");
//                field.setTDStatus(Integer.parseInt(pieces1[1]) + "/" + Integer.parseInt(pieces1[2]) + " x" + Integer.parseInt(pieces1[3]));
//            } else if ("deltower".equals(pieces1[0])) {
//                field.asyncDelTower(Long.parseLong(pieces1[1]));
//            } else if ("attack".equals(pieces1[0])) {
//                WUnit selected;
//
//                WUnit attacker = field.getUnit(Long.parseLong(pieces1[1]));
//                WUnit target = field.getUnit(Long.parseLong(pieces1[2]));
//                long begTime = Long.parseLong(pieces1[3]);
//
//                if (attacker != null && target != null) {
//                    selected = attacker.getSelectedUnit();
//                    if (selected == null || !selected.equals(target)) {
//                        attacker.selectUnit(target);
//                    }
//                    if (attacker.attack(begTime)) {
//                        field.asyncAddNuke(attacker, target, begTime + attacker.getNukeAnimationDelay());
//                    }
//                }
//                /*if (self.attack(System.currentTimeMillis() - ServerInteraction.serverStartTime)) {
//                    WanderingLocks.lockNukes();
//                    field.addNuke(self, self.getSelectedUnit(), System.currentTimeMillis() - ServerInteraction.serverStartTime + self.getNukeAnimationDelay());
//                    WanderingLocks.unlockNukes();
//                }*/
//            }
        }

        private void commandHandler(Message message) {
            if (debugLevel > 0) {
                System.out.println("[me] <-- " + message);
            }
//            if (m.getType() == MessageType.OTHER) {
//
//                commandHandler(((OtherMessage) m).getMessage());
//
//            } else if (m.getType() == MessageType.HMAP) {
//
//                HMapMessage tmpMessage = (HMapMessage) m;
//                field.addMapFragment(new ClientMapFragment(tmpMessage.getIdx(), tmpMessage.getIdy(), tmpMessage.getHmap()));
//
//            } else if (m.getType() == MessageType.HIT) {
//
//                HitMessage tmpMessage = (HitMessage) m;
//                WUnit attacker = field.getUnit(tmpMessage.getAttackerID());
//                WUnit target = field.getUnit(tmpMessage.getTargetID());
//                if (attacker != null && target != null) {
//                    field.asyncAddHitAnimation(new CanonHitAnimation("canon", (Point) target.getCurPos().clone(), Direction.SOUTH, System.currentTimeMillis() - serverStartTime));
//                    target.doHit(tmpMessage.getDamage());
//                } else {
//                    System.err.println("While processing HIT message, attacker or target was not found. It may be sync issue!");
//                }
//
//            } else if (m.getType() == MessageType.BOLT) {
//
//                BoltMessage tmpMessage = (BoltMessage) m;
//                WUnit attacker = field.getUnit(tmpMessage.getAttackerID());
//                WUnit target = field.getUnit(tmpMessage.getTargetID());
//                if (attacker != null && target != null) {
//                    attacker.selectUnit(target);
//                    field.asyncAddNuke(attacker, target, tmpMessage.getBegTime());
//                } else {
//                    System.err.println("While processing BOLT message, attacker or target was not found. It may be sync issue!");
//                }
//
//            } else if (m.getType() == MessageType.MOVE) {
//
//                MoveMessage tmpMessage = (MoveMessage) m;
//                field.asyncMoveUnit(tmpMessage.getUnitID(),
//                                    tmpMessage.getBegX(),
//                                    tmpMessage.getBegY(),
//                                    tmpMessage.getEndX(),
//                                    tmpMessage.getEndY(),
//                                    tmpMessage.getBegTime());
//
//            } else if (m.getType() == MessageType.INVENTORYADDETCITEM) {
//
//                InventoryAddEtcItem tmpMsg = (InventoryAddEtcItem) m;
//                ClientEtc etcItem = new ClientEtc(tmpMsg.getId(), tmpMsg.getItemType().getCustomName(), tmpMsg.getCount(), tmpMsg.getItemType());
//                field.getSelfPlayer().addEtc(etcItem);
//
//            } else if (m.getType() == MessageType.INVENTORYREMOVEETCITEM) {
//
//                InventoryRemoveEtcItem tmpMsg = (InventoryRemoveEtcItem) m;
//                ClientEtc etcItem = new ClientEtc(tmpMsg.getId(), tmpMsg.getItemType().getCustomName(), tmpMsg.getCount(), tmpMsg.getItemType());
//                try {
//                    field.getSelfPlayer().removeEtc(etcItem);
//                } catch (InventoryException ex) {
//                    Logger.getLogger(ServerInteraction.class.getName()).log(Level.SEVERE, null, ex);
//                    System.exit(1);
//                }
//
//            } else if (m.getType() == MessageType.PICKUPETCITEM) {
//
//                // TODO Not only players can pickup items!
//                PickupEtcItem tmpMsg = (PickupEtcItem) m;
//                Player p = field.getPlayer(tmpMsg.getPickerId());
//                ClientEtc i = field.getEtc(tmpMsg.getItemId());
//                if (p != null && i != null) {
//                    p.pickup(i);
//                    field.asyncRemoveEtc(i);
//                } else {
//                    if (p == null) {
//                        System.err.println("While processing " + m.getType().name() + " message, picker id=" + tmpMsg.getPickerId() + " was not found.");
//                    }
//                    if (i == null) {
//                        System.err.println("While processing " + m.getType().name() + " message, item id=" + tmpMsg.getItemId() + " was not found.");
//                    }
//                }
//
//            } else if (m.getType() == MessageType.APPEARETCITEM) {
//
//                AppearEtcItem tmpMsg = (AppearEtcItem) m;
//                ClientEtc etcItem = new ClientEtc(tmpMsg.getId(), tmpMsg.getItemType().getCustomName(), tmpMsg.getCount(), tmpMsg.getItemType());
//                etcItem.setX(tmpMsg.getX());
//                etcItem.setY(tmpMsg.getY());
//                field.asyncAddEtc(etcItem);
//                //field.asyncAddDrawable(etcItem);
//
//            } else if (m.getType() == MessageType.DISAPPEARETCITEM) {
//
//                DisappearEtcItem tmpMsg = (DisappearEtcItem) m;
//                field.asyncRemoveEtc(tmpMsg.getId());
//
//            }
        }
    }

    private boolean connectionAlive() {
        return socket != null && socket.isConnected() && !socket.isClosed() && (socket.isInputShutdown() || socket.isOutputShutdown());
    }
}
