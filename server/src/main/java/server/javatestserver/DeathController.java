package server.javatestserver;

import common.items.*;
import server.javatestserver.items.ServerEtc;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

/** Called only under world; removes pending hits before restoring a player's life. */
final class DeathController {
    private final WorldState world;
    private final VisibleManager visibility;
    private final MessageService messages;
    private final Random random = new Random();
    private final java.util.function.LongConsumer cancelCombat;
    DeathController(WorldState world, VisibleManager visibility, MessageService messages, java.util.function.LongConsumer cancelCombat) {
        this.world = world; this.visibility = visibility; this.messages = messages; this.cancelCombat = cancelCombat;
    }
    void die(JTSUnit target) {
        if (!target.dead() || world.unit(target.getID()) != target) return;
        cancelCombat.accept(target.getID());
        if (target instanceof Monster monster && world.monsters.remove(monster)) {
            world.units.remove(monster);
            visibility.killMonster(monster);
            if (world.waveMonsters.remove(monster)) {
                Point position = monster.getCurPos();
                ServerEtc coins = new ServerEtc(world.nextId(), "Gold", Items.GOLD, 1 + random.nextInt(99));
                coins.setX(position.x + (random.nextBoolean() ? 10 : -10));
                coins.setY(position.y + (random.nextBoolean() ? 10 : -10));
                world.items.add(coins);
            }
        } else if (target instanceof Player player && world.players.contains(player)) {
            dropGold(player, true);
            player.restoreHitPoints();
            player.teleportToSpawn();
            // Preserve the existing wire order: HIT, inventory delta, teleport, death.
            messages.teleport(player);
            visibility.killPlayer(player);
        } else if (target instanceof Tower tower && world.towers.remove(tower)) {
            world.units.remove(tower);
            visibility.removeTower(tower);
        }
    }
    void dropGold(Player player, boolean notifyOwner) {
        synchronized (world) {
            synchronized (player.getInventory()) {
                ArrayList<Etc> gold = player.getInventory().getEtc(Items.GOLD);
                if (gold.isEmpty()) return;
                Etc stack = gold.get(0);
                player.getInventory().removeEtc(stack.getID(), stack.getCount());
                if (notifyOwner) messages.sendTo(new common.messages.InventoryRemoveEtcItem(stack.getID(), stack.getCount(), stack.getType()), player);
                Point position = player.getCurPos();
                stack.setX(position.x);
                stack.setY(position.y);
                world.items.add(stack);
            }
        }
    }

}
