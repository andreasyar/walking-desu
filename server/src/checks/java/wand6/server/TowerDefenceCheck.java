package wand6.server;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import wand6.common.*;
import wand6.client.messages.*;
import wand6.server.messages.*;
import static wand6.server.MultiplayerCheck.*;
import static wand6.common.TowerDefenceRules.*;

public final class TowerDefenceCheck {
    public static void main(String[] args) throws Exception {
        cycles();
        network();
    }
    private static void cycles() throws Exception {
        try (JavaServer server = new JavaServer(0)) {
            var world = server.players;
            var time = new AtomicLong(ServerTime.getInstance().getTimeSinceStart());
            var messages = new MessageManager(); messages.init(server, world);
            var defence = new TowerDefenceController(world, server.visibility, messages, server.combat, time::get, new Random(11));
            world.defence = defence;
            long player = world.createPlayer("stage11");
            require(defence.build(player, 700, 400), "valid build");
            require(!defence.build(player, 740, 400), "build burst rejected");
            time.addAndGet(1000);
            require(!defence.build(player, 700, 400), "duplicate position rejected");
            require(!defence.build(player, -1, 400) && !defence.build(player, Integer.MAX_VALUE, 400)
                    && !defence.build(player, 4000, 4000) && !defence.build(Long.MAX_VALUE, 700, 400), "invalid build bounds/range/player");
            int blockedX = -1, blockedY = -1;
            for (int x=32;x<1000 && blockedX<0;x+=32) for (int y=32;y<1000;y+=32)
                if (!Terrain.passable(x,y)) { blockedX=x; blockedY=y; break; }
            require(blockedX >= 0 && !defence.build(player, blockedX, blockedY), "terrain rejected");
            time.addAndGet(WAVE_DELAY); defence.tick();
            require(defence.snapshot().wave() == 1 && defence.snapshot().strength() == 1 && monsters(world).size() == 5, "first wave five strength one");
            for (Unit m : monsters(world)) {
                require(m.getMaxHp() == 20 && Set.of(.07,.08).contains(m.getMovement().getSpeed())
                        && m.getMovement().getBegX() >=544 && m.getMovement().getBegX() <=830
                        && m.getMovement().getEndX() == DEST_X && m.getMovement().getEndY() == DEST_Y, "baseline spawn/hp/speed/route");
                defence.remove(m.getId(), true);
            }
            time.addAndGet(WAVE_DELAY); defence.tick();
            require(defence.snapshot().strength() == 2 && monsters(world).size() == 10
                    && monsters(world).stream().allMatch(m -> m.getHp() == 40), "cleared wave strengthens next");
            // Force arrivals under the real state owner, then tick twice: no double counting.
            for (Unit m : monsters(world)) m.place(DEST_X, DEST_Y);
            defence.tick(); defence.tick();
            require(defence.snapshot().losses() == 10 && monsters(world).isEmpty(), "arrivals counted once");
            for (int i=0; i<9; i++) {
                time.addAndGet(WAVE_DELAY); defence.tick();
                require(defence.snapshot().strength() == 2 && monsters(world).size() == 10, "losses prevent strengthening");
                for (Unit m : monsters(world)) m.place(DEST_X, DEST_Y);
                defence.tick();
            }
            require(defence.snapshot().losses() == LOSS_LIMIT, "loss limit reached");
            // Include an active projectile in the reset boundary.
            long tower = world.npcs.values().stream().filter(u -> u.getKind() == Unit.Kind.TOWER).findFirst().orElseThrow().getId();
            Unit victim = new Unit(IdManager.nextId(), "Monster", "poring", Unit.Kind.MONSTER, 20, .07);
            victim.place(700,450); world.npcs.put(victim.getId(), victim);
            server.combat.attack(tower, victim.getId());
            require(server.combat.pending() == 1, "pending projectile before reset");
            time.addAndGet(WAVE_DELAY); defence.tick();
            require(defence.snapshot().cycle() == 1 && defence.snapshot().wave() == 1 && defence.snapshot().losses() == 0
                    && defence.snapshot().strength() == 1 && monsters(world).size() == 5, "reset begins new five-monster cycle");
            require(world.npcs.values().stream().noneMatch(u -> u.getKind() == Unit.Kind.TOWER)
                    && world.unit(victim.getId()) == null && server.combat.pending() == 0, "reset clears towers/old monsters/flights");
            server.combat.tick();
            require(monsters(world).stream().allMatch(m -> m.getHp() == 20), "stale projectile cannot damage new cycle");
            require(defence.build(player, 700,400), "rebuild after reset");
            defence.clear();
            require(world.npcs.isEmpty() && server.combat.pending() == 0, "shutdown cleanup");
            System.out.println("PASS: tower defence deterministic waves, build validation, baseline parameters, strengthening, 100 arrivals, reset during flight, rebuild and cleanup");
        }
    }
    private static List<Unit> monsters(PlayerManager world) {
        return world.npcs.values().stream().filter(u -> u.getKind() == Unit.Kind.MONSTER).toList();
    }
    private static void network() throws Exception {
        var offset = new AtomicLong();
        JavaServer server = new JavaServer(0, () -> ServerTime.getInstance().getTimeSinceStart() + offset.get());
        Thread accept = new Thread(() -> { try { server.run(); } catch(IOException ex) { throw new RuntimeException(ex); } });
        accept.start();
        try (server; Peer one = new Peer(server.port(),"stage11-a"); Peer two = new Peer(server.port(),"stage11-b")) {
            one.await(m -> m instanceof TowerDefenceStatusMessage s && s.wave() == 0, 0);
            one.send(new BuildTowerMessage(700,400));
            var tower = (AppearEntityMessage) one.await(m -> m instanceof AppearEntityMessage e && e.kind() == Unit.Kind.TOWER,0);
            two.await(m -> m instanceof AppearEntityMessage e && e.id() == tower.id(),0);
            one.send(new BuildTowerMessage(700,400)); one.barrier();
            synchronized(server.players) { require(server.players.npcs.size() == 1,"network duplicate build rejected"); }
            try (Peer late = new Peer(server.port(),"stage11-c")) {
                late.await(m -> m instanceof AppearEntityMessage e && e.id() == tower.id(),0);
                late.await(m -> m instanceof TowerDefenceStatusMessage,0);
            }
            // Isolated fixtures create NPCs only on server; production protocol has no spawn/reset cheats.
            Unit monster = new Unit(IdManager.nextId(),"Monster","poring", Unit.Kind.MONSTER,20,.08);
            synchronized(server.players) {
                monster.getMovement().start(700,450,705,755,ServerTime.getInstance().getTimeSinceStart()); server.players.npcs.put(monster.getId(),monster); server.visibility.update();
                server.combat.attack(tower.id(),monster.getId());
            }
            var shot = (AttackMessage) one.await(m -> m instanceof AttackMessage a && a.attackerId() == tower.id(),0);
            require(shot.windup() == 0,"tower has no player windup");
            one.await(m -> m instanceof HitMessage h && h.shotId() == shot.shotId() && h.hp() == 10 && h.damage() == 10,0);
            two.await(m -> m instanceof HitMessage h && h.shotId() == shot.shotId() && h.hp() == 10,0);
            two.await(m -> m instanceof RemoveEntityMessage e && e.id() == monster.getId() && e.killed(),0);
            one.await(m -> m instanceof DeathMessage d && d.id() == monster.getId(),0);
            one.barrier();
            require(one.since(0).stream().filter(m -> m instanceof DeathMessage d && d.id() == monster.getId()).count() == 1, "one monster death");
            require(one.since(0).stream().noneMatch(m -> m instanceof RespawnMessage p && p.id() == monster.getId()), "monster never respawns as player");
            // Missing targets are rejected; players can attack monsters and towers as in 3.2.
            int mark = one.mark(); one.send(new AttackRequestMessage(Long.MAX_VALUE)); one.barrier();
            require(one.since(mark).stream().noneMatch(m -> m instanceof AttackMessage a && a.attackerId() == oneId(one)),"missing target rejected");
            Unit far = new Unit(IdManager.nextId(),"Monster","poring", Unit.Kind.MONSTER,100,.07);
            synchronized(server.players) { far.getMovement().start(1050,512,1050,1000,ServerTime.getInstance().getTimeSinceStart()); server.players.npcs.put(far.getId(),far); server.visibility.update(); }
            int crossing = one.mark(); one.send(new MoveRequestMessage(750,512));
            var appeared = (AppearEntityMessage) one.await(m -> m instanceof AppearEntityMessage e && e.id() == far.getId(),crossing);
            require(appeared.speed() == .07 && appeared.hp() == 100,"NPC boundary snapshot");
            synchronized(server.players) {
                server.players.find(one.id()).place(512,512); server.visibility.update();
            }
            one.await(m -> m instanceof RemoveEntityMessage e && e.id() == far.getId() && !e.killed(),crossing);
            synchronized(server.players) { server.defence.remove(far.getId(),false); }
            Unit contested = new Unit(IdManager.nextId(),"Monster","poring", Unit.Kind.MONSTER,50,.07);
            int race = one.mark();
            synchronized(server.players) {
                contested.getMovement().start(900,512,900,1000,ServerTime.getInstance().getTimeSinceStart());
                server.players.npcs.put(contested.getId(),contested); server.visibility.update();
            }
            one.send(new AttackRequestMessage(contested.getId()));
            var contestedShot = (AttackMessage) one.await(m -> m instanceof AttackMessage a && a.targetId() == contested.getId(),race);
            two.send(new AttackRequestMessage(contested.getId()));
            try(Peer lateFlight = new Peer(server.port(),"stage11-e")) {
                var state = (AppearEntityMessage) lateFlight.await(m -> m instanceof AppearEntityMessage e && e.id() == contested.getId(),0);
                require(state.movement().getBegTime() == contested.getMovement().getBegTime() && state.hp() == 50,"late moving NPC snapshot");
                lateFlight.await(m -> m instanceof AttackMessage a && a.shotId() == contestedShot.shotId(),0);
                lateFlight.await(m -> m instanceof RemoveEntityMessage e && e.id() == contested.getId() && e.killed(),0);
            }
            one.await(m -> m instanceof RemoveEntityMessage e && e.id() == contested.getId() && e.killed(),race);
            one.barrier();
            require(one.since(race).stream().filter(m -> m instanceof HitMessage h && h.targetId() == contested.getId()).count() == 1
                    && one.since(race).stream().filter(m -> m instanceof DeathMessage d && d.id() == contested.getId()).count() == 1,
                    "concurrent shots cause one monster hit/death");
            require(server.combat.pending() == 0,"monster death cancels competing shot");
            synchronized(server.players) {
                Unit escaped = new Unit(IdManager.nextId(),"Monster","poring", Unit.Kind.MONSTER,20,.07);
                escaped.getMovement().start(700,590,705,755,ServerTime.getInstance().getTimeSinceStart());
                server.players.npcs.put(escaped.getId(),escaped); server.visibility.update();
                server.players.unit(tower.id()).setLastAttack(-TOWER_REUSE);
                server.combat.attack(tower.id(),escaped.getId());
                require(server.combat.pending() == 1,"escape during flight fixture");
                server.defence.remove(escaped.getId(),false);
                require(server.combat.pending() == 0,"escape cancels flight");
            }
            int demolition = one.mark();
            one.send(new BuildTowerMessage(650,650));
            var removable = (AppearEntityMessage) one.await(m -> m instanceof AppearEntityMessage e && e.kind() == Unit.Kind.TOWER && e.id() != tower.id(),demolition);
            one.send(new AttackRequestMessage(removable.id()));
            two.await(m -> m instanceof RemoveEntityMessage e && e.id() == removable.id() && e.killed(),0);
            one.await(m -> m instanceof DeathMessage d && d.id() == removable.id(),demolition);
            one.barrier();
            require(one.since(demolition).stream().noneMatch(m -> m instanceof RespawnMessage p && p.id() == removable.id()),"destroyed tower does not respawn");
            // Drive the production wave controller to the loss boundary over real streams.
            synchronized(server.players) {
                for (int wave=0; wave<21; wave++) {
                    offset.addAndGet(WAVE_DELAY); server.defence.tick();
                    for (Unit m : monsters(server.players)) m.place(DEST_X, DEST_Y);
                    server.defence.tick();
                }
            }
            one.await(m -> m instanceof TowerDefenceStatusMessage s && s.cycle() == 1,0);
            two.await(m -> m instanceof TowerDefenceStatusMessage s && s.cycle() == 1,0);
            one.await(m -> m instanceof RemoveEntityMessage e && e.id() == tower.id() && !e.killed(),0);
            two.await(m -> m instanceof RemoveEntityMessage e && e.id() == tower.id() && !e.killed(),0);
            try(Peer rejoin = new Peer(server.port(),"stage11-d")) {
                rejoin.await(m -> m instanceof TowerDefenceStatusMessage s && s.cycle() == 1,0);
                rejoin.barrier();
                require(rejoin.since(0).stream().noneMatch(m -> m instanceof AppearEntityMessage e && e.id() == tower.id()),"old tower absent after reset/rejoin");
            }
            System.out.println("PASS: tower defence network port="+server.port()+"; build/late state, two-client HIT/death/removal, no NPC respawn, invalid target, visibility crossing, two-client reset and rejoin");
        } finally { server.close(); accept.join(5000); require(!accept.isAlive(),"accept stopped"); }
    }
    private static long oneId(Peer peer) { try { return peer.id(); } catch(Exception ex) { throw new RuntimeException(ex); } }
}
