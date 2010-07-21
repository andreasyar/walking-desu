package client;

import java.util.ArrayList;

public class GameField {
    private static GameField instance = null;

    private Player self;
    private final ArrayList<Unit> units = new ArrayList<Unit>();

    public static GameField getInstance() {
        if (instance == null) {
            instance = new GameField();
        }

        return instance;
    }

    private GameField() {
        
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

    public Player getSelf() {
        return self;
    }
}
