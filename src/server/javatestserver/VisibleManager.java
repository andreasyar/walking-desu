package server.javatestserver;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import newcommon.Monster;
import newcommon.Player;
import newcommon.exceptions.UnitException;
import newcommon.items.Item;

public class VisibleManager implements Runnable {

    private final double visibleRange = 500.0;

    private final ArrayList<Player> players = new ArrayList<Player>();
    private final HashMap<Player, ArrayList<Player>> visiblePlayers = new HashMap<Player, ArrayList<Player>>();
    private final ArrayList<Monster> monsters = new ArrayList<Monster>();
    private final ArrayList<Item> items = new ArrayList<Item>();

    @Override
    public void run() {
        try {
            update();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void update() {

        synchronized(players) {
            boolean isInRange, isSee;

            for (Player p1 : players) {

                try {
                    for (Player p2 : players) {
                        if (!p1.equals(p2)) {
                            isInRange = isInRange(p1, p2);
                            isSee = isSee(p1, p2);

                            if (isInRange && !isSee) {
                                MessageManager.getInstance().sendAppearPlayerMessage(p1, p2);
                                MessageManager.getInstance().sendPlayerStateMessage(p1, p2);
                                visiblePlayers.get(p1).add(p2);

                                MessageManager.getInstance().sendAppearPlayerMessage(p2, p1);
                                MessageManager.getInstance().sendPlayerStateMessage(p1, p2);
                                visiblePlayers.get(p2).add(p1);
                            }
                            if (!isInRange && isSee) {
                                MessageManager.getInstance().sendDisappearPlayerMessage(p1, p2);
                                MessageManager.getInstance().sendDisappearPlayerMessage(p2, p1);
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    System.exit(1);
                } catch (UnitException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }

//        Point beg, end, cur;
//        for (Player p1 : players) {
//            for (Player p2 : players) {
//                if (!p1.equals(p2)) {
//                    if (p1.inRange(p2) && !p1.see(p2)) {
//
//                        // Сообщим игрокам p1 и p2 друг о друге.
//                        p1.addVisibleUnit(p2);
//                        cur = p2.getCurPos();
//                        server.sendTo("(newplayer "
//                                + p2.getID() + " "
//                                + p2.getHitPoints() + " "
//                                + 0.07 + " "
//                                + cur.x + " "
//                                + cur.y + " "
//                                + "\"" + p2.getNick() + "\"" + " "
//                                + "\"SOUTH\"" + " "
//                                + "\"" + p2.getSpriteSetName() + "\"" + ")", p1);
//                        if (p2.getText() != null) {
//                            server.sendTo("(message " + p2.getID() + " \"" + p2.getText() + "\")", p1);
//                        }
//                        p2.addVisibleUnit(p1);
//
//                        cur = p1.getCurPos();
//                        server.sendTo("(newplayer "
//                                + p1.getID() + " "
//                                + p1.getHitPoints() + " "
//                                + 0.07 + " "
//                                + cur.x + " "
//                                + cur.y + " "
//                                + "\"" + p1.getNick() + "\"" + " "
//                                + "\"SOUTH\"" + " "
//                                + "\"" + p1.getSpriteSetName() + "\"" + ")", p2);
//                        if (p1.getText() != null) {
//                            server.sendTo("(message " + p1.getID() + " \"" + p1.getText() + "\")", p2);
//                        }
//                        if (p1.isMove()) {
//                            beg = p1.getBeg();
//                            end = p1.getEnd();
//                            server.sendTo(new MoveMessage(p1.getID(),
//                                                          p1.getBegTime(),
//                                                          beg.x,
//                                                          beg.y,
//                                                          end.x,
//                                                          end.y),
//                                          p2);
//                        }
//                        if (p2.isMove()) {
//                            beg = p2.getBeg();
//                            end = p2.getEnd();
//                            server.sendTo(new MoveMessage(p2.getID(),
//                                                          p2.getBegTime(),
//                                                          beg.x,
//                                                          beg.y,
//                                                          end.x,
//                                                          end.y),
//                                          p1);
//                        }
//                    } else if (!p1.inRange(p2) && p1.see(p2)) {
//
//                        // Игроки p1 и p2 больше не видят друг друга.
//                        p1.delVisibleUnit(p2);
//                        server.sendTo("(delplayer " + p2.getID() + ")", p1);
//                        p2.delVisibleUnit(p1);
//                        server.sendTo("(delplayer " + p1.getID() + ")", p2);
//                    }
//                }
//            }
//            for (Tower t : towers) {
//                if (p1.inRange(t) && !p1.see(t)) {
//
//                    // Игрок увидел башню.
//                    p1.addVisibleUnit(t);
//                    server.sendTo("(tower "
//                            + t.getID() + " "
//                            + t.getRange() + " "
//                            + t.getDamage() + " "
//                            + t.getCurPos().x + " "
//                            + t.getCurPos().y + " "
//                            + "\"" + t.getNick() + "\"" + ")", p1);
//                } else if (!p1.inRange(t) && p1.see(t)) {
//
//                    // Игрок больше не видит эту башню.
//                    p1.delVisibleUnit(t);
//                    server.sendTo("(deltower " + t.getID() + ")", p1);
//                }
//            }
//            for (Monster m : monsters) {
//                if (p1.inRange(m) && !p1.see(m)) {
//
//                    // Игрок увидел монстра.
//                    p1.addVisibleUnit(m);
//                    server.sendTo("(newmonster "
//                            + m.getID() + " "
//                            + m.getHitPoints() + " "
//                            + m.getSpeed() + " "
//                            + m.getCurPos().x + " "
//                            + m.getCurPos().y + " "
//                            + "\"" + m.getNick() + "\"" + " "
//                            + "\"SOUTH\"" + " "
//                            + "\"" + m.getSpriteSetName() + "\"" + ")", p1);
//                    if (m.isMove()) {
//                        beg = m.getBeg();
//                        end = m.getEnd();
//                        server.sendTo(new MoveMessage(m.getID(),
//                                                      m.getBegTime(),
//                                                      beg.x,
//                                                      beg.y,
//                                                      end.x,
//                                                      end.y),
//                                      p1);
//                    }
//                } else if (!p1.inRange(m) && p1.see(m)) {
//
//                    // Игрок больше не видит этого монстра.
//                    p1.delVisibleUnit(m);
//                    server.sendTo("(delmonster " + m.getID() + ")", p1);
//                }
//            }//monsters
//
//            for (ServerEtc etc : etcItems) {
//                if (p1.inRange(etc) && !p1.see(etc)) {
//
//                    // Игрок увидел предмет.
//                    p1.addVisibleItem(etc);
//                    server.sendTo(etc.getAppearMessage(), p1);
//                } else if (!p1.inRange(etc) && p1.see(etc)) {
//
//                    // Игрок больше не видит этот итем.
//                    p1.delVisibleItem(etc);
//                    server.sendTo(etc.getDisappearMessage(), p1);
//                    //server.sendTo(new PickupEtcItem(p1.getID(), etc.getID()), p1);
//                }
//            }//etc items
//        }
    }

    public void addPlayer(Player player) throws IllegalArgumentException {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }
        synchronized(players) {

            if (visiblePlayers.containsKey(player)) {
                System.err.println(player + " already in visible players map.");
            } else {
                visiblePlayers.put(player, new ArrayList<Player>());
            }

            if (players.contains(player)) {
                System.err.println(player + " already in players list.");
            } else {
                players.add(player);
            }
        }
    }

    public void removePlayer(Player player) throws IllegalArgumentException {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }
        synchronized(players) {
            players.remove(player);
            visiblePlayers.remove(player);
        }
    }

    private boolean isInRange(Player p1, Player p2) {
        if (Point.distance(p1.getCurX(), p1.getCurY(), p2.getCurX(), p2.getCurY()) <= visibleRange) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isSee(Player p1, Player p2) {
        return visiblePlayers.get(p1).contains(p2);
    }
}
