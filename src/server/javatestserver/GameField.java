package server.javatestserver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import newcommon.Unit;
import newcommon.Player;
import newcommon.Monster;
import newcommon.items.Item;
import newcommon.items.Etc;

public class GameField implements Runnable {

    private final static int debugLevel = 0;
    private static GameField self = null;
    private boolean selfExecutionStoped = false;
    private final LinkedBlockingQueue<Invokable> selfExecutionQueue = new LinkedBlockingQueue<Invokable>();
    private final ArrayList<Unit> units = new ArrayList<Unit>();
    private final ArrayList<Player> players = new ArrayList<Player>();
    private final ArrayList<Monster> monsters = new ArrayList<Monster>();
    private final ArrayList<Item> items = new ArrayList<Item>();
    private final ArrayList<Etc> etcItems = new ArrayList<Etc>();

    public static GameField getInstance() {
        if (self == null) {
            self = new GameField();
            new Thread(self).start();
        }

        return self;
    }

    private GameField() {}

    private void addToSelfExecutionQueue(String method, Class[] paramTypes, Object[] args) {
        try {
            Method m = getClass().getMethod(method, paramTypes);
            selfExecutionQueue.add(new Invokable(m, args));
            wakeupSelfExecution();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            System.exit(1);
        } catch (SecurityException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            while (!selfExecutionStoped) {
                while (selfExecutionQueue.isEmpty()) {
                    try {
                        synchronized (selfExecutionQueue) {
                            selfExecutionQueue.wait();
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }
                }

                Invokable i = selfExecutionQueue.remove();
                i.getMethod().invoke(this, i.getArgs());
            }
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            System.exit(1);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private void wakeupSelfExecution() {
        synchronized (selfExecutionQueue) {
            selfExecutionQueue.notify();
        }
    }

    private class Invokable {

        private Method method;
        private Object[] args;

        public Invokable(Method method, Object[] args) {
            this.method = method;
            this.args = args;
        }

        public Object[] getArgs() {
            return args;
        }

        public Method getMethod() {
            return method;
        }
    }

    public void syncAddUnit(Unit unit){
        if (!units.contains(unit)) {
            units.add(unit);
        } else {
            if (debugLevel > 0) {
                System.err.println(unit + " already in units list!");
            }
        }
    }

    public void syncAddItem(Item item){
        if (!items.contains(item)) {
            items.add(item);
        } else {
            if (debugLevel > 0) {
                System.err.println(item + " already in items list!");
            }
        }
    }

    public void syncAddPlayer(Player player){
        if (!players.contains(player)) {
            players.add(player);
            syncAddUnit(player);
        } else {
            if (debugLevel > 0) {
                System.err.println(player + " already in players list!");
            }
        }
    }

    public void asyncAddPlayer(Player player) throws IllegalArgumentException {}

    public void syncRemovePlayer(Player player) {}

    public void asyncRemovePlayer(Player player) throws IllegalArgumentException {}

    public void syncAddMonster(Monster monster) {
        if (!monsters.contains(monster)) {
            monsters.add(monster);
            syncAddUnit(monster);
        } else {
            if (debugLevel > 0) {
                System.err.println(monster + " already in monsters list!");
            }
        }
    }

    public void asyncAddMonster(Monster monster) throws IllegalArgumentException {}

    public void syncRemoveMonster(Monster monster) {}

    public void asyncRemoveMonster(Monster monster) throws IllegalArgumentException {}

    public void syncAddEtc(Etc etc) {
        if (!etcItems.contains(etc)) {
            etcItems.add(etc);
            syncAddItem(etc);
        } else {
            if (debugLevel > 0) {
                System.err.println(etc + " already in etc items list!");
            }
        }
    }

    public void asyncAddEtc(Etc etc) throws IllegalArgumentException {}

    public void syncRemoveEtc(Etc etc) {}

    public void asyncRemoveEtc(Etc etc) throws IllegalArgumentException {}
}
