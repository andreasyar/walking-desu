package server.javatestserver;

import common.*;
import common.items.*;
import java.util.*;
import java.util.concurrent.*;

/** Deterministic scheduler interleavings plus real controllers, world and delivery service. */
public final class ServerCombatCheck {
    private static void require(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
    }
    static final class Ticket implements ScheduledFuture<Object> {
        boolean canceled;
        public long getDelay(TimeUnit unit) { return 0; }
        public int compareTo(Delayed other) { return 0; }
        public boolean cancel(boolean interrupt) { canceled = true; return true; }
        public boolean isCancelled() { return canceled; }
        public boolean isDone() { return canceled; }
        public Object get() { throw new UnsupportedOperationException(); }
        public Object get(long timeout, TimeUnit unit) { throw new UnsupportedOperationException(); }
    }
    static final class Scheduler extends ScheduledThreadPoolExecutor {
        final List<Runnable> ticks = new ArrayList<>();
        final List<Ticket> tickets = new ArrayList<>();
        Scheduler() { super(1); }
        @Override public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long delay, long period, TimeUnit unit) {
            // Deliberately invoke BEFORE returning its Future. No game state may change yet.
            task.run();
            ticks.add(task);
            Ticket ticket = new Ticket(); tickets.add(ticket); return ticket;
        }
        void tick() throws Exception { Thread.sleep(3); for (Runnable task : List.copyOf(ticks)) task.run(); }
    }
    static final class Connection implements MessageService.Connection {
        final Player player;
        final List<Message> history = new ArrayList<>();
        Connection(Player player) { this.player = player; }
        public boolean loggedIn() { return true; }
        public Player getSelfPlayer() { return player; }
        public void addMessage(String text) { history.add(new OtherMessage(text)); }
        public void addMessage(Message message) { history.add(message); }
        long hits(long id) { return history.stream().filter(m -> m instanceof HitMessage hit && hit.getTargetID() == id).count(); }
        int index(String text) {
            for (int i = 0; i < history.size(); i++) if (history.get(i) instanceof OtherMessage m && m.getMessage().equals(text)) return i;
            return -1;
        }
    }
    static final class Fixture implements AutoCloseable {
        final WorldState world = new WorldState();
        final Scheduler scheduler = new Scheduler();
        final List<Connection> connections = new ArrayList<>();
        final MessageService messages = new MessageService(world, connections);
        final VisibleManager visibility = new VisibleManager(world, messages);
        final CombatController combat = new CombatController(world, scheduler, messages, visibility);
        final DeathController deaths = combat.deaths;
        Connection player(String name) {
            Player player = new Player(world.nextId(), PlayerName.requireValid(name), 100, 0, 0, .07);
            player.setSpriteSetName("peasant"); world.players.add(player); world.units.add(player);
            Connection connection = new Connection(player); connections.add(connection); visibility.update(); return connection;
        }
        Monster monster(int hp, int x) {
            Monster monster = new Monster(world.nextId(), "Monster", hp, x, 0, .08);
            monster.setSpriteSetName("poring"); world.units.add(monster); world.monsters.add(monster); visibility.update(); return monster;
        }
        public void close() { scheduler.shutdownNow(); }
    }
    private static void cancellationAndErrors() throws Exception {
        WorldState world = new WorldState();
        int[] calls = {0};
        WorldTask task = new WorldTask(world) { void step() { calls[0]++; } };
        task.run(); task.cancel(); Ticket future = new Ticket(); task.setScheduledFuture(future); task.run();
        require(calls[0] == 0 && future.canceled, "cancel before Future and pre-bind tick");
        try (Fixture f = new Fixture()) {
            Connection attacker = f.player("attacker"), victim = f.player("victim");
            f.combat.attack(attacker.player, -123, false);
            require(f.combat.pending() == 0, "missing target rejected");
            f.combat.attack(attacker.player, victim.player.getID(), false);
            require(victim.player.getHitPoints() == 100, "early scheduler invocation cannot hit");
            f.scheduler.tick(); f.scheduler.tick();
            require(victim.player.getHitPoints() == 50 && victim.hits(victim.player.getID()) == 1, "completed task cannot repeat");
            f.combat.attack(attacker.player, victim.player.getID(), true);
            f.combat.cancelFor(attacker.player.getID()); f.scheduler.tick();
            require(victim.player.getHitPoints() == 50 && f.combat.pending() == 0, "attacker exit cancels flight");
            victim.player.teleportTo(10000, 0);
            f.combat.attack(attacker.player, victim.player.getID(), false);
            f.world.units.remove(victim.player); // Even an external removal must invalidate ID resolution.
            f.scheduler.tick();
            require(f.combat.pending() == 0 && victim.player.getHitPoints() == 50, "removed target resolved by ID");
            Monster broken = new Monster(f.world.nextId(), "Broken", 100, 0, 0, .08) {
                @Override public void doHit(int damage) { throw new IllegalStateException("injected hit failure"); }
            };
            f.world.units.add(broken); f.world.monsters.add(broken);
            f.combat.attack(attacker.player, broken.getID(), false); f.scheduler.tick();
            require(f.combat.pending() == 0, "failed combat task cleaned");
            Monster healthy = f.monster(100, 0);
            f.combat.attack(attacker.player, healthy.getID(), false); f.scheduler.tick();
            require(healthy.getHitPoints() == 50, "server continues after operation failure");
        }
    }
    private static void simultaneousDeath() throws Exception {
        try (Fixture f = new Fixture()) {
            Connection first = f.player("first"), second = f.player("second"), victim = f.player("victim");
            long id = victim.player.getID();
            var coins = new server.javatestserver.items.ServerEtc(f.world.nextId(), "Gold", Items.GOLD, 17);
            victim.player.getInventory().addEtc(coins);
            // Four same-life hits: only two may apply; pending hits must not attack the respawn.
            for (int i = 0; i < 4; i++) f.combat.attack(i % 2 == 0 ? first.player : second.player, id, false);
            Thread.sleep(3);
            ExecutorService race = Executors.newFixedThreadPool(4);
            try {
                CountDownLatch start = new CountDownLatch(1);
                List<Future<?>> work = new ArrayList<>();
                for (Runnable tick : f.scheduler.ticks) work.add(race.submit(() -> { try { start.await(); tick.run(); } catch (InterruptedException e) { throw new RuntimeException(e); } }));
                start.countDown(); for (Future<?> future : work) future.get(5, TimeUnit.SECONDS);
            } finally { race.shutdownNow(); }
            f.scheduler.tick();
            require(victim.player.getHitPoints() == 100 && f.combat.pending() == 0, "same-life hits canceled before respawn");
            require(f.world.items.size() == 1 && ((Etc) f.world.items.peek()).getCount() == 17, "single death drop");
            require(victim.player.getInventory().getEtc(Items.GOLD).isEmpty(), "victim inventory cleared");
            for (Connection connection : f.connections) {
                require(connection.hits(id) == 2, "HIT once per hit to victim and each observer");
                int teleport = connection.index("(teleport " + id + " 0 0)"), death = connection.index("(deathplayer " + id + ")");
                int lastHit = -1;
                for (int i = 0; i < connection.history.size(); i++) if (connection.history.get(i) instanceof HitMessage) lastHit = i;
                require(lastHit < teleport && teleport < death, "baseline HIT/teleport/death order");
            }
            Monster monster = f.monster(50, 0); f.world.waveMonsters.add(monster);
            f.combat.attack(first.player, monster.getID(), false); f.combat.attack(second.player, monster.getID(), false);
            f.scheduler.tick(); f.scheduler.tick();
            require(f.world.items.size() == 2 && !f.world.units.contains(monster) && !f.world.waveMonsters.contains(monster), "one monster reward and complete removal");
            require(!first.player.see(monster) && first.hits(monster.getID()) == 1, "dead target has no second hit or stale visibility");
        }
    }
    private static void towersAndWaves() throws Exception {
        try (Fixture f = new Fixture()) {
            Connection observer = f.player("observer");
            Tower tower = new Tower(f.world.nextId(), "Tower", 200, 10, 1000, 0, 0);
            f.world.towers.add(tower); f.world.units.add(tower);
            Monster outside = f.monster(100, 201), first = f.monster(100, 40), nearer = f.monster(100, 0);
            TowerController towers = new TowerController(f.world, f.combat); towers.setScheduledFuture(new Ticket());
            towers.run(); require(tower.getTarget() == first, "first eligible target, radius honored");
            Thread.sleep(45); f.scheduler.tick(); require(first.getHitPoints() == 90, "tower damage preserved");
            towers.run(); f.scheduler.tick(); require(first.getHitPoints() == 90, "reuse prevents repeat");
            f.world.monsters.remove(first); f.world.units.remove(first);
            tower.setLastAttackTime(-2000); towers.run(); f.scheduler.tick();
            require(tower.getTarget() == nearer && nearer.getHitPoints() == 90, "removed target replaced after reuse");
            nearer.doHit(100); f.deaths.die(nearer);
            towers.run(); require(f.combat.pending() == 0 && !tower.targetInRange(), "dead/outside targets not attacked");
            f.world.monsters.remove(outside); f.world.units.remove(outside);
            MonsterController waves = new MonsterController(f.world, f.visibility, f.messages, f.combat, 20000);
            long wave = common.WanderingServerTime.getInstance().getTimeSinceStart() / 20000 + 1;
            waves.advanceTo(wave);
            require(f.world.waveMonsters.size() == 5 && f.world.waveMonsters.peek().getHitPoints() == 20, "initial wave strength/count");
            for (Monster monster : List.copyOf(f.world.waveMonsters)) { monster.doHit(100); f.deaths.die(monster); }
            waves.advanceTo(++wave);
            require(f.world.waveMonsters.size() == 10 && f.world.waveMonsters.peek().getHitPoints() == 40, "cleared wave strengthens");
            // Lose 100 monsters through the real endpoint path, without waiting for wall-clock waves.
            for (int cycle = 0; cycle < 10; cycle++) {
                for (Monster monster : f.world.waveMonsters) monster.move(705, 755, 705, 755, -1000);
                waves.advanceTo(wave); // stopped monsters become losses, not rewards
                int drops = f.world.items.size();
                if (cycle == 9) {
                    Monster distant = f.monster(100, 10000);
                    f.combat.attack(tower, distant.getID(), false);
                    require(f.combat.pending() == 1, "pending tower flight before reset");
                }
                waves.advanceTo(++wave);
                require(f.world.items.size() == drops, "escaped monsters award no gold");
            }
            f.scheduler.tick();
            require(f.world.towers.isEmpty() && !f.world.units.contains(tower) && !observer.player.see(tower), "loss limit resets towers/visibility");
            require(f.combat.pending() == 0 && f.world.tdMonsterLoss == 0 && f.world.waveMonsters.size() == 5, "new cycle without stale projectiles");
            Tower rebuilt = new Tower(f.world.nextId(), "Tower", 200, 10, 1000, 700, 20);
            f.world.towers.add(rebuilt); f.world.units.add(rebuilt); towers.run();
            require(f.combat.pending() > 0, "second cycle can build and attack");
        }
    }
    private static void ids() throws Exception {
        WorldState world = new WorldState(); Set<Long> ids = ConcurrentHashMap.newKeySet();
        ExecutorService threads = Executors.newFixedThreadPool(4);
        try {
            List<Future<?>> work = new ArrayList<>();
            for (int t = 0; t < 4; t++) work.add(threads.submit(() -> { for (int i = 0; i < 5000; i++) require(ids.add(world.nextId()), "unique ID"); }));
            for (Future<?> future : work) future.get();
        } finally { threads.shutdownNow(); }
        require(ids.size() == 20000, "all parallel IDs issued");
    }
    private static void shutdown() throws Exception {
        try (JavaTestServer server = new JavaTestServer(0)) {
            java.util.concurrent.atomic.AtomicReference<Throwable> failure = new java.util.concurrent.atomic.AtomicReference<>();
            Thread runner = new Thread(() -> { try { server.run(); } catch (Throwable ex) { failure.set(ex); } }, "shutdown-check");
            runner.start();
            try (var socket = new java.net.Socket(java.net.InetAddress.getLoopbackAddress(), server.localPort())) {
                socket.setSoTimeout(3000);
                var out = new java.io.ObjectOutputStream(socket.getOutputStream()); out.flush();
                new java.io.ObjectInputStream(socket.getInputStream());
                server.close(); server.close();
            }
            runner.join(3000);
            require(!runner.isAlive() && failure.get() == null, "shutdown unblocks accept without JVM exit");
            require(server.executor.awaitTermination(3, TimeUnit.SECONDS) && server.combat.pending() == 0,
                    "shutdown stops periodic tasks and pending projectiles");
        }
    }

    public static void main(String[] args) throws Exception {
        WanderingServerTime.getInstance().setServerTime(JavaTestServer.serverStartTime);
        cancellationAndErrors(); simultaneousDeath(); towersAndWaves(); ids(); shutdown();
        System.out.println("PASS: pre-bind cancellation/tick, ID resolution, simultaneous hits/death, one reward, message order/recipients, tower damage/range/reuse, waves/strength/loss/reset/rebuild, 20000 parallel IDs, graceful isolated server shutdown");
    }
}
