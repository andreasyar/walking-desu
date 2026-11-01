package server.javatestserver;

import common.WanderingServerTime;
import java.awt.Point;
import java.util.Iterator;
import java.util.Random;

/** Wave state is private here; all entities and mutations belong to world. */
final class MonsterController extends WorldTask {
    private final long spawnDelay;
    private final double[] speeds = {0.08, 0.07, 0.06};
    private int curWaveMonLoss;
    private int monStrMult;
    private int monCountAdd;
    private long wave;
    private final Random random = new Random();
    private final VisibleManager visibility;
    private final MessageService messages;
    private final CombatController combat;

    MonsterController(WorldState world, VisibleManager visibility, MessageService messages,
                      CombatController combat, long spawnDelay) {
        super(world);
        this.visibility = visibility;
        this.messages = messages;
        this.combat = combat;
        this.spawnDelay = spawnDelay;
        wave = WanderingServerTime.getInstance().getTimeSinceStart() / spawnDelay;
    }

    @Override void step() {
        advanceTo(WanderingServerTime.getInstance().getTimeSinceStart() / spawnDelay);
    }

    /** Explicit wave index also allows deterministic checks without changing the global clock. */
    void advanceTo(long currentWave) {
        if (currentWave > wave) {
            wave = currentWave;
            world.tdMonsterLoss += curWaveMonLoss;
            if (world.tdMonsterLoss >= world.tdMonsterLossLimit) {
                world.tdMonsterLoss = 0;
                curWaveMonLoss = 0;
                monStrMult = 0;
                monCountAdd = 0;
                for (Tower tower : world.towers) {
                    combat.cancelFor(tower.getID());
                    visibility.removeTower(tower);
                    world.units.remove(tower);
                }
                world.towers.clear();
            }
            // Preserve the baseline: first wave has five monsters; a cleared wave strengthens the next.
            if (curWaveMonLoss == 0) {
                monStrMult++;
                monCountAdd += 5;
            }
            status(world.tdMonsterLoss);
            curWaveMonLoss = 0;
            Point end = new Point(705, 755);
            for (int i = 0; i < monCountAdd; i++) {
                Point start = new Point(544 + random.nextInt(287), random.nextInt(64));
                // Preserve historical selection of the first two speed variants.
                Monster monster = new Monster(world.nextId(), "Monster", 20 * monStrMult,
                        start.x, start.y, speeds[random.nextInt(2)]);
                monster.setSpriteSetName("poring");
                world.waveMonsters.add(monster);
                world.monsters.add(monster);
                world.units.add(monster);
                monster.move(start.x, start.y, end.x, end.y, WanderingServerTime.getInstance().getTimeSinceStart());
            }
        }
        for (Iterator<Monster> iterator = world.waveMonsters.iterator(); iterator.hasNext();) {
            Monster monster = iterator.next();
            if (!monster.dead() && !monster.isMove()) {
                curWaveMonLoss++;
                status(world.tdMonsterLoss + curWaveMonLoss);
                combat.cancelFor(monster.getID());
                visibility.killMonster(monster);
                world.units.remove(monster);
                world.monsters.remove(monster);
                iterator.remove();
            }
        }
    }

    private void status(int losses) {
        messages.sendToAll(new String[]{"(monsterloss " + losses + " " + world.tdMonsterLossLimit + " " + monStrMult + ")"});
    }
}
