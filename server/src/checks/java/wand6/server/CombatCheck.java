package wand6.server;

import java.io.IOException;
import java.util.*;
import wand6.client.messages.*;
import wand6.common.*;
import wand6.server.messages.*;
import static wand6.server.MultiplayerCheck.*;

/** Real streams and production handlers; only position/HP fixtures use the world monitor. */
public final class CombatCheck {
    public static void main(String[] args) throws Exception {
        JavaServer server = new JavaServer(0);
        Thread accept = new Thread(() -> { try { server.run(); } catch (IOException ex) { throw new RuntimeException(ex); } }, "check-accept");
        accept.start();
        try (server; Peer one = new Peer(server.port(), "stage10-a"); Peer two = new Peer(server.port(), "stage10-b"); Peer observer = new Peer(server.port(), "stage10-c")) {
            long a = one.id(), b = two.id(), c = observer.id();
            place(server, a, 512, 512); place(server, b, 912, 512); place(server, c, 3000, 512);
            one.barrier(); two.barrier(); observer.barrier();
            int mark = one.mark(), victimMark = two.mark(), hiddenMark = observer.mark();
            one.send(new AttackRequestMessage(a)); one.send(new AttackRequestMessage(Long.MAX_VALUE)); one.send(new AttackRequestMessage(c));
            one.barrier();
            require(one.since(mark).stream().noneMatch(m -> m instanceof AttackMessage), "self/missing/far attack rejected");
            one.send(new AttackRequestMessage(b));
            var attack = (AttackMessage) one.await(m -> m instanceof AttackMessage, mark);
            for (int i = 0; i < 12; i++) one.send(new AttackRequestMessage(b));
            one.barrier();
            require(one.since(mark).stream().filter(m -> m instanceof AttackMessage).count() == 1, "reuse enforced by server");
            synchronized (server.players) { require(server.players.find(b).getHp() == 100, "windup does not damage early"); }
            try (Peer late = new Peer(server.port(), "stage10-d")) {
                late.await(m -> m instanceof AttackMessage shot && shot.shotId() == attack.shotId() && shot.time() == attack.time(), 0);
                var hit = (HitMessage) two.await(m -> m instanceof HitMessage h && h.shotId() == attack.shotId(), victimMark);
                require(hit.hp() == 50 && hit.damage() == 50 && hit.time() >= attack.time() + 775, "server flight and HP");
                one.await(m -> m instanceof HitMessage h && h.shotId() == attack.shotId(), mark);
                late.await(m -> m instanceof HitMessage h && h.shotId() == attack.shotId(), 0);
            }
            observer.barrier();
            require(observer.since(hiddenMark).stream().noneMatch(m -> m instanceof AttackMessage || m instanceof HitMessage || m instanceof DeathMessage), "hidden peer receives no combat");
            try (Peer lateHealth = new Peer(server.port(), "stage10-e")) {
                var state = (AppearPlayerMessage) lateHealth.await(m -> m instanceof AppearPlayerMessage p && p.id() == b, 0);
                require(state.hp() == 50 && state.life() == 0, "late appearance carries HP/life");
            }
            // A projectile follows the original destination and resolves current target identity.
            int lethalMark = two.mark();
            one.send(new AttackRequestMessage(b));
            var lethal = (AttackMessage) one.await(m -> m instanceof AttackMessage s && s.shotId() != attack.shotId(), mark);
            two.send(new MoveRequestMessage(950, 512));
            var death = (DeathMessage) two.await(m -> m instanceof DeathMessage, lethalMark);
            var respawn = (RespawnMessage) two.await(m -> m instanceof RespawnMessage, lethalMark);
            require(death.life() == 0 && respawn.life() == 1 && respawn.hp() == 100 && respawn.x() == 512, "one death and immediate restored life");
            var events = two.since(lethalMark);
            int h = index(events, HitMessage.class), d = index(events, DeathMessage.class), r = index(events, RespawnMessage.class);
            require(h < d && d < r && death.x() > 912, "HIT/death/respawn order and moving death position");
            await(() -> server.combat.pending() == 0, "death cancels old life shots");
            synchronized (server.players) { require(!server.players.find(b).getMovement().isMoving(), "respawn stops movement"); }
            // Kill with two concurrent in-flight hits: terminal state cancels the second one.
            place(server, a, 900, 512); place(server, b, 512, 512); place(server, c, 900, 512);
            int concurrent = two.mark();
            synchronized (server.players) {
                server.players.find(b).setHp(50);
                server.players.find(a).setLastAttack(-1000); server.players.find(c).setLastAttack(-1000);
                server.combat.attack(a, b); server.combat.attack(c, b);
            }
            two.await(m -> m instanceof RespawnMessage s && s.life() == 2, concurrent);
            await(() -> server.combat.pending() == 0, "all concurrent hits terminal");
            two.barrier();
            var simultaneous = two.since(concurrent);
            require(simultaneous.stream().filter(m -> m instanceof HitMessage).count() == 1
                    && simultaneous.stream().filter(m -> m instanceof DeathMessage).count() == 1
                    && simultaneous.stream().filter(m -> m instanceof RespawnMessage).count() == 1, "no hit from old life, double death or double respawn");
            // Cancel before impact on attacker and target removal, both EOF and RST.
            try (Peer gone = new Peer(server.port(), "stage10-f")) {
                long id = gone.id(); place(server, id, 1000, 512);
                synchronized (server.players) { server.players.find(id).setLastAttack(-1000); server.combat.attack(id, b); }
                require(server.combat.pending() == 1, "attacker projectile pending");
                gone.abort();
                await(() -> server.players.snapshot().stream().noneMatch(p -> p.getId() == id) && server.combat.pending() == 0, "RST attacker cancels projectile");
            }
            try (Peer gone = new Peer(server.port(), "stage10-g")) {
                long id = gone.id(); place(server, id, 1000, 512);
                synchronized (server.players) { server.players.find(a).setLastAttack(-1000); server.combat.attack(a, id); }
                require(server.combat.pending() == 1, "target projectile pending");
                gone.close();
                await(() -> server.players.snapshot().stream().noneMatch(p -> p.getId() == id) && server.combat.pending() == 0, "EOF target cancels projectile");
            }
            // Boundary departure during a shot: victim still gets HIT, hidden observers do not get HP.
            place(server, a, 512, 512); place(server, b, 1012, 512); place(server, c, 3000, 512);
            int crossing = two.mark();
            synchronized (server.players) { server.players.find(a).setLastAttack(-1000); server.combat.attack(a, b); }
            two.send(new MoveRequestMessage(1120, 512));
            two.await(m -> m instanceof DisappearPlayerMessage p && p.id() == a, crossing);
            two.await(m -> m instanceof HitMessage hit && hit.hp() == 50, crossing);
            require(server.combat.pending() == 0, "departed target still resolved once");
            one.barrier(); two.barrier();
            // Even after a short shot lands, a late observer receives the unfinished cast animation.
            place(server, a, 512, 512); place(server, c, 512, 512);
            int shortMark = one.mark();
            synchronized (server.players) { server.players.find(a).setLastAttack(-1000); }
            one.send(new AttackRequestMessage(c));
            var shortShot = (AttackMessage) one.await(m -> m instanceof AttackMessage, shortMark);
            one.await(m -> m instanceof HitMessage hit && hit.shotId() == shortShot.shotId(), shortMark);
            try (Peer lateCast = new Peer(server.port(), "stage10-h")) {
                var state = (AppearPlayerMessage) lateCast.await(m -> m instanceof AppearPlayerMessage p && p.id() == a, 0);
                require(state.attackAnimation() != null && state.attackAnimation().time() == shortShot.time(), "late appearance preserves unfinished attack animation");
                lateCast.barrier();
                require(lateCast.since(0).stream().noneMatch(m -> m instanceof AttackMessage), "finished projectile not resurrected by animation snapshot");
            }
            System.out.println("PASS: combat port=" + server.port() + "; real stream attacks, invalid targets/range/reuse, 375ms windup/1px-ms flight, authoritative HP, late HP/projectile/animation snapshots, hidden recipients, moving death, ordered respawn, simultaneous hits, old-life cancellation, EOF/RST, visibility crossing.");
        } finally { server.close(); accept.join(5000); require(!accept.isAlive(), "accept stopped"); }
        await(() -> Thread.getAllStackTraces().keySet().stream().noneMatch(t -> t.getName().startsWith("wand6-")), "server threads stopped");
    }
    private static int index(List<?> events, Class<?> type) {
        for (int i = 0; i < events.size(); i++) if (type.isInstance(events.get(i))) return i;
        throw new AssertionError("Missing " + type);
    }
    private static void place(JavaServer server, long id, int x, int y) {
        synchronized (server.players) { server.players.find(id).place(x, y); server.visibility.update(); }
    }
}
