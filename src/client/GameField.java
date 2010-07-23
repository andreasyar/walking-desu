package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GameField {
    private static GameField instance = null;

    private Player selfPlayer;
    private final ArrayList<Unit> units = new ArrayList<Unit>();

    public static GameField getInstance() {
        if (instance == null) {
            instance = new GameField();
        }

        return instance;
    }

    private GameField() {
        selfPlayer = new Player();
        units.add(selfPlayer);
    }

    public void addPlayer(Player player) {
        synchronized (units) {
            units.add(player);
        }
    }

    public void addNPC(NPC npc) {
        synchronized (units) {
            units.add(npc);
        }
    }

    public ArrayList<Unit> getUnits() {
        return units;
    }

    public ArrayList<Unit> getYSortedUnits() {
        synchronized (units) {
            Collections.sort(units, new YAligner());
        }

        return units;
    }

    public Player getSelfPlayer() {
        return selfPlayer;
    }

    private class YAligner implements Comparator {
        public final int compare(Object a, Object b) {
            return ((Unit) a).getCurPos().y > ((Unit) b).getCurPos().y ? 1 : 0;
        }
    }
}
