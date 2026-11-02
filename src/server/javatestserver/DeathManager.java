package server.javatestserver;

import newcommon.Monster;
import java.util.HashMap;
import java.util.Iterator;
import newcommon.Player;
import newcommon.ServerTime;

class DeathManager implements Runnable {

    public static long defaultDeathDelay = 3000L;
    private static DeathManager self = null;
    private final HashMap<Player, Long> playerCorpses = new HashMap<Player, Long>();
    private final HashMap<Monster, Long> monsterCorpses = new HashMap<Monster, Long>();

    private DeathManager() {}

    public static DeathManager getInstance() {
        if (self == null) {
            self = new DeathManager();
        }

        return self;
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

    private void update() {
        synchronized(playerCorpses) {
            Player corpse;
            for (Iterator<Player> i = playerCorpses.keySet().iterator(); i.hasNext();) {
                corpse = i.next();
                if (ServerTime.getInstance().getTimeSinceStart() >= playerCorpses.get(corpse) + corpse.getDeathDelay()) {
                    i.remove();
                    corpse.setDeathTime(0L);
                    corpse.setIsDeathDelay(false);
                    try {
                        MessageManager.getInstance().sendDeathDelayOver(corpse);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
        }
        synchronized(monsterCorpses) {
            Monster corpse;
            for (Iterator<Monster> i = monsterCorpses.keySet().iterator(); i.hasNext();) {
                corpse = i.next();
                if (ServerTime.getInstance().getTimeSinceStart() >= monsterCorpses.get(corpse) + corpse.getDeathDelay()) {
                    i.remove();
                    corpse.setDeathTime(0L);
                    corpse.setIsDeathDelay(false);
                }
            }
        }
    }

    public void addPlayerCorpse(Player corpse) {
        synchronized(playerCorpses) {
            if (!playerCorpses.containsKey(corpse)) {
                playerCorpses.put(corpse, corpse.getDeathTime());
            } else {
                System.err.println(corpse + " already in corpses map.");
            }
        }
    }

    public void addMonsterCorpse(Monster corpse) {
        synchronized(monsterCorpses) {
            if (!monsterCorpses.containsKey(corpse)) {
                monsterCorpses.put(corpse, corpse.getDeathTime());
            } else {
                System.err.println(corpse + " already in corpses map.");
            }
        }
    }
}
