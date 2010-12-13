package server.javatestserver;

import client.items.Item;
import common.MoveMessage;
import common.messages.Pickup;
import java.awt.Point;
import java.util.concurrent.LinkedBlockingQueue;

public class VisibleManager implements Runnable {

    private final LinkedBlockingQueue<Player> players;
    private final LinkedBlockingQueue<Tower> towers;
    private final LinkedBlockingQueue<Monster> monsters;
    private final LinkedBlockingQueue<Item> items;
    private final JavaTestServer server;

    public VisibleManager(LinkedBlockingQueue<Player> players, LinkedBlockingQueue<Tower> towers, LinkedBlockingQueue<Monster> monsters, LinkedBlockingQueue<Item> items, JavaTestServer server) {
        this.players = players;
        this.towers = towers;
        this.monsters = monsters;
        this.items = items;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            update();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Require players lock.
     */
    public void update() {
        Point beg, end, cur;
        for (Player p1 : players) {
            for (Player p2 : players) {
                if (!p1.equals(p2)) {
                    if (p1.inRange(p2) && !p1.see(p2)) {

                        // Сообщим игрокам p1 и p2 друг о друге.
                        p1.addVisibleUnit(p2);
                        cur = p2.getCurPos();
                        server.sendTo("(newplayer "
                                + p2.getID() + " "
                                + p2.getHitPoints() + " "
                                + 0.07 + " "
                                + cur.x + " "
                                + cur.y + " "
                                + "\"" + p2.getNick() + "\"" + " "
                                + "\"SOUTH\"" + " "
                                + "\"" + p2.getSpriteSetName() + "\"" + ")", p1);
                        if (p2.getText() != null) {
                            server.sendTo("(message " + p2.getID() + " \"" + p2.getText() + "\")", p1);
                        }
                        p2.addVisibleUnit(p1);

                        cur = p1.getCurPos();
                        server.sendTo("(newplayer "
                                + p1.getID() + " "
                                + p1.getHitPoints() + " "
                                + 0.07 + " "
                                + cur.x + " "
                                + cur.y + " "
                                + "\"" + p1.getNick() + "\"" + " "
                                + "\"SOUTH\"" + " "
                                + "\"" + p1.getSpriteSetName() + "\"" + ")", p2);
                        if (p1.getText() != null) {
                            server.sendTo("(message " + p1.getID() + " \"" + p1.getText() + "\")", p2);
                        }
                        if (p1.isMove()) {
                            beg = p1.getBeg();
                            end = p1.getEnd();
                            server.sendTo(new MoveMessage(p1.getID(),
                                                          p1.getBegTime(),
                                                          beg.x,
                                                          beg.y,
                                                          end.x,
                                                          end.y),
                                          p2);
                        }
                        if (p2.isMove()) {
                            beg = p2.getBeg();
                            end = p2.getEnd();
                            server.sendTo(new MoveMessage(p2.getID(),
                                                          p2.getBegTime(),
                                                          beg.x,
                                                          beg.y,
                                                          end.x,
                                                          end.y),
                                          p1);
                        }
                    } else if (!p1.inRange(p2) && p1.see(p2)) {

                        // Игроки p1 и p2 больше не видят друг друга.
                        p1.delVisibleUnit(p2);
                        server.sendTo("(delplayer " + p2.getID() + ")", p1);
                        p2.delVisibleUnit(p1);
                        server.sendTo("(delplayer " + p1.getID() + ")", p2);
                    }
                }
            }
            for (Tower t : towers) {
                if (p1.inRange(t) && !p1.see(t)) {

                    // Игрок увидел башню.
                    p1.addVisibleUnit(t);
                    server.sendTo("(tower "
                            + t.getID() + " "
                            + t.getRange() + " "
                            + t.getDamage() + " "
                            + t.getCurPos().x + " "
                            + t.getCurPos().y + " "
                            + "\"" + t.getNick() + "\"" + ")", p1);
                } else if (!p1.inRange(t) && p1.see(t)) {

                    // Игрок больше не видит эту башню.
                    p1.delVisibleUnit(t);
                    server.sendTo("(deltower " + t.getID() + ")", p1);
                }
            }
            for (Monster m : monsters) {
                if (p1.inRange(m) && !p1.see(m)) {

                    // Игрок увидел монстра.
                    p1.addVisibleUnit(m);
                    server.sendTo("(newmonster "
                            + m.getID() + " "
                            + m.getHitPoints() + " "
                            + m.getSpeed() + " "
                            + m.getCurPos().x + " "
                            + m.getCurPos().y + " "
                            + "\"" + m.getNick() + "\"" + " "
                            + "\"SOUTH\"" + " "
                            + "\"" + m.getSpriteSetName() + "\"" + ")", p1);
                    if (m.isMove()) {
                        beg = m.getBeg();
                        end = m.getEnd();
                        server.sendTo(new MoveMessage(m.getID(),
                                                      m.getBegTime(),
                                                      beg.x,
                                                      beg.y,
                                                      end.x,
                                                      end.y),
                                      p1);
                    }
                } else if (!p1.inRange(m) && p1.see(m)) {

                    // Игрок больше не видит этого монстра.
                    p1.delVisibleUnit(m);
                    server.sendTo("(delmonster " + m.getID() + ")", p1);
                }
            }//monsters

            for (Item i : items) {
                if (p1.inRange(i) && !p1.see(i)) {

                    // Игрок увидел предмет.
                    p1.addVisibleItem(i);
                    server.sendTo(i.getDropMessage(), p1);
                } else if (!p1.inRange(i) && p1.see(i)) {

                    // Игрок больше не видит этот итем.
                    p1.delVisibleItem(i);
                    server.sendTo(new Pickup(p1.getID(), i.getID()), p1);
                }
            }//items
        }
    }

    /**
     * Require players lock.
     */
    public void removePlayer(Player player) {
        for (Player p : players) {
            if (p.getVisibleUnitsList().remove(player)) {
                server.sendTo("(delplayer " + player.getID() + ")", p);
            }
        }
    }

    /**
     * Require players lock.
     */
    public void removeTower(Tower tower) {
        for (Player p : players) {
            if (p.getVisibleUnitsList().remove(tower)) {
                server.sendTo("(deltower " + tower.getID() + ")", p);
            }
        }
    }

    /**
     * Require players lock.
     */
    public void removeMonster(Monster monster) {
        for (Player p : players) {
            if (p.getVisibleUnitsList().remove(monster)) {
                server.sendTo("(delmonster " + monster.getID() + ")", p);
            }
        }
    }

    /**
     * Require players lock.
     */
    public void killMonster(Monster monster) {
        for (Player p : players) {
            if (p.getVisibleUnitsList().remove(monster)) {
                server.sendTo("(deathmonster " + monster.getID() + ")", p);
            }
        }
    }

    /**
     * Require players lock.
     */
    public void killUnit(JTSUnit unit) {
        for (Player p : players) {
            if (p.getVisibleUnitsList().remove(unit)) {
                server.sendTo("(deathunit " + unit.getID() + ")", p);
            }
        }
    }

    /**
     * Require players lock.
     */
    public void killPlayer(Player player) {
        for (Player p : players) {
            if (p.getVisibleUnitsList().contains(player)) {
                server.sendTo("(deathplayer " + player.getID() + ")", p);
            }
        }
        server.sendTo("(deathplayer " + player.getID() + ")", player);
    }
}
