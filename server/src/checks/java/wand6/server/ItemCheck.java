package wand6.server;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import wand6.common.*;
import wand6.client.messages.*;
import wand6.server.messages.*;
import static wand6.server.MultiplayerCheck.*;
import static wand6.common.ItemStack.Type.GOLD;

public final class ItemCheck {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("fixture")) { fixture(); return; }
        network();
    }
    private static GroundItem add(JavaServer server, int count, int x, int y) {
        var item = new GroundItem(new ItemStack(IdManager.nextId(),GOLD,count),x,y);
        server.items.add(item); return item;
    }
    private static void fixture() throws Exception {
        try (JavaServer server = new JavaServer(0, () -> 0L)) {
            add(server,7,600,512); add(server,5,680,512);
            // A stationary target moving toward a distant point, for real production attack/drop.
            Unit monster = new Unit(IdManager.nextId(),"Monster","poring",Unit.Kind.MONSTER,20,.07);
            long now = ServerTime.getInstance().getTimeSinceStart();
            monster.getMovement().start(780,520,780,1200,now + 600000);
            server.players.npcs.put(monster.getId(),monster);
            server.run();
        }
    }
    private static void network() throws Exception {
        JavaServer server = new JavaServer(0, () -> 0L); // No unrelated natural waves in race checks.
        Thread accept = new Thread(() -> { try { server.run(); } catch(IOException ex) { throw new RuntimeException(ex); } });
        accept.start();
        try (server; Peer one = new Peer(server.port(),"item-one"); Peer two = new Peer(server.port(),"item-two")) {
            long a=one.id(), b=two.id();
            var first = add(server,7,512,512);
            one.await(m -> m instanceof AppearItemMessage i && i.item().id()==first.id(),0);
            two.await(m -> m instanceof AppearItemMessage i && i.item().id()==first.id(),0);
            try (Peer late = new Peer(server.port(),"item-late")) {
                late.await(m -> m instanceof AppearItemMessage i && i.item().id()==first.id(),0);
            }
            int mark=one.mark(), other=two.mark();
            place(server,a,563,512); one.send(new PickupItemMessage(first.id())); one.barrier();
            require(server.players.find(a).inventory().snapshot().isEmpty() && server.items.snapshot().contains(first),"51px pickup rejected");
            synchronized(server.players) { server.players.find(a).setHp(0); }
            place(server,a,512,512); one.send(new PickupItemMessage(first.id())); one.barrier();
            require(server.items.snapshot().contains(first),"dead picker rejected");
            synchronized(server.players) { server.players.find(a).setHp(100); }
            place(server,a,562,512); // Inclusive authoritative 50px boundary.
            one.send(new PickupItemMessage(first.id()));
            var gain=(InventoryAddMessage)one.await(m -> m instanceof InventoryAddMessage,mark);
            require(gain.delta().id()==first.id() && gain.delta().count()==7,"canonical first stack");
            two.await(m -> m instanceof DisappearItemMessage i && i.id()==first.id(),other);
            one.await(m -> m instanceof PickedUpMessage i && i.id()==first.id(),mark);
            two.barrier(); require(two.since(other).stream().noneMatch(m -> m instanceof InventoryAddMessage || m instanceof PickedUpMessage),"owner only inventory and confirmation");
            place(server,a,512,512);
            var second=add(server,5,512,512);
            one.send(new PickupItemMessage(second.id()));
            one.await(m -> m instanceof InventoryAddMessage i && i.delta().id()==first.id() && i.delta().count()==5,mark);
            one.barrier(); require(server.players.find(a).inventory().count(GOLD)==12,"merge authoritative 12");
            int repeat=one.mark(); one.send(new PickupItemMessage(first.id())); one.send(new PickupItemMessage(second.id())); one.send(new PickupItemMessage(Long.MAX_VALUE)); one.barrier();
            require(one.since(repeat).stream().noneMatch(m -> m instanceof InventoryAddMessage),"repeat/missing rejected");
            var distant=add(server,3,1013,512);
            int before=one.mark(); one.send(new PickupItemMessage(distant.id())); one.barrier();
            require(one.since(before).stream().noneMatch(m -> m instanceof InventoryAddMessage || m instanceof AppearItemMessage i && i.item().id()==distant.id()),"far invisible pickup rejected");
            place(server,a,513,512);
            one.await(m -> m instanceof AppearItemMessage i && i.item().id()==distant.id(),before);
            place(server,a,512,512);
            one.await(m -> m instanceof DisappearItemMessage i && i.id()==distant.id(),before);
            // Overflow keeps both ground and inventory unchanged.
            var overflow=add(server,Integer.MAX_VALUE,512,512);
            one.send(new PickupItemMessage(overflow.id())); one.barrier();
            require(server.items.snapshot().contains(overflow) && server.players.find(a).inventory().count(GOLD)==12,"overflow atomic transfer");
            // Two independent network readers contend on exactly one item.
            var race=add(server,9,512,512);
            var start=new CountDownLatch(1);
            ExecutorService pool=Executors.newFixedThreadPool(2);
            try {
                var f1=pool.submit(() -> { start.await(); one.send(new PickupItemMessage(race.id())); return true; });
                var f2=pool.submit(() -> { start.await(); two.send(new PickupItemMessage(race.id())); return true; });
                start.countDown(); f1.get(); f2.get();
            } finally { pool.shutdownNow(); }
            one.barrier(); two.barrier();
            int total=server.players.find(a).inventory().count(GOLD)+server.players.find(b).inventory().count(GOLD);
            require(total==21 && server.items.snapshot().stream().noneMatch(i -> i.id()==race.id()),"contested pickup exactly once");
            require(one.since(0).stream().filter(m -> m instanceof PickedUpMessage p && p.id()==race.id()).count()
                    +two.since(0).stream().filter(m -> m instanceof PickedUpMessage p && p.id()==race.id()).count()==1,"one race acknowledgement");
            // Death via production combat, then pick up using the other client's real command.
            synchronized(server.players) { server.players.find(a).setHp(50); }
            int death=one.mark(); two.send(new AttackRequestMessage(a));
            one.await(m -> m instanceof RespawnMessage r && r.id()==a,death);
            var loss=(InventoryRemoveMessage)one.await(m -> m instanceof InventoryRemoveMessage,death);
            require(loss.id()==first.id() && loss.count()>=12,"death removes canonical gold");
            var dropped=(AppearItemMessage)two.await(m -> m instanceof AppearItemMessage i && i.item().id()==first.id(),other);
            two.send(new PickupItemMessage(dropped.item().id())); two.barrier();
            require(server.players.find(a).inventory().count(GOLD)==0 && server.players.find(b).inventory().count(GOLD)==21,"death/drop/repick preserves total");
            // Concurrent lethal attacks produce exactly one monster reward.
            Unit monster=new Unit(IdManager.nextId(),"Monster","poring",Unit.Kind.MONSTER,50,.07);
            synchronized(server.players) {
                monster.getMovement().start(700,512,700,1200,ServerTime.getInstance().getTimeSinceStart());
                server.players.npcs.put(monster.getId(),monster); server.visibility.update();
                server.players.find(a).setLastAttack(-10000); server.players.find(b).setLastAttack(-10000);
            }
            Set<Long> prior=new HashSet<>(); server.items.snapshot().forEach(i -> prior.add(i.id()));
            one.send(new AttackRequestMessage(monster.getId())); two.send(new AttackRequestMessage(monster.getId()));
            one.await(m -> m instanceof RemoveEntityMessage r && r.id()==monster.getId(),0);
            one.barrier(); two.barrier();
            var rewards=server.items.snapshot().stream().filter(i -> !prior.contains(i.id())).toList();
            require(rewards.size()==1 && rewards.get(0).stack().count()>=1 && rewards.get(0).stack().count()<=99,"one 1..99 monster reward");
            // Tower deletion, escape and reset are not deaths and never create rewards.
            synchronized(server.players) {
                int size=server.items.snapshot().size();
                Unit escaped=new Unit(IdManager.nextId(),"Monster","poring",Unit.Kind.MONSTER,20,.07);
                escaped.place(705,755); server.players.npcs.put(escaped.getId(),escaped); server.defence.tick();
                server.defence.clear();
                require(server.items.snapshot().size()==size,"escape/reset preserves ground items and adds no reward");
            }
            // Race pickup against a RST; whether accepted or rejected, its gold is conserved.
            var contested=add(server,4,512,512);
            int leave=one.mark(); two.send(new PickupItemMessage(contested.id())); two.abort();
            await(() -> { synchronized(server.players) { return server.players.find(b)==null; } },"disconnect completes");
            one.barrier();
            // Inspect the exact player's stack and the contested item, avoiding unrelated rewards.
            int returned=server.items.snapshot().stream().filter(i -> i.id()==first.id() || i.id()==race.id() || i.id()==contested.id()).mapToInt(i -> i.stack().count()).sum();
            require(returned==25,"pickup/disconnect conserves 21+4 gold");
            require(one.since(leave).stream().anyMatch(m -> m instanceof AppearItemMessage i && i.item().stack().count()>=21),"survivor sees disconnected gold");
            try(Peer rejoin=new Peer(server.port(),"item-two")) {
                require(rejoin.id()!=b,"fresh connection identity");
                rejoin.barrier(); require(rejoin.since(0).stream().noneMatch(m -> m instanceof InventoryAddMessage),"rejoin has empty inventory");
            }
            System.out.println("PASS: items network port="+server.port()+"; late/500 boundary, owner-only deltas, 7+5 canonical merge, invalid/repeat/far/overflow, two-player race, PvP death/drop/repick, single monster reward, escape/reset, pickup/RST conservation, rejoin");
        } finally { server.close(); accept.join(5000); require(!accept.isAlive(),"accept stopped"); }
    }
    private static void place(JavaServer server,long id,int x,int y) {
        synchronized(server.players) { server.players.find(id).place(x,y); server.visibility.update(); }
    }
}
