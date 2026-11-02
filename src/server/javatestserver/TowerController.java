package server.javatestserver;

import java.awt.Point;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import newcommon.Monster;
import newcommon.ServerTime;
import newcommon.exceptions.SkillException;

class TowerController implements Runnable {

    private ScheduledFuture future = null;
    private boolean canceled = false;
    private final ArrayList<Monster> monsters = new ArrayList<Monster>();
    private final ArrayList<Monster> towers = new ArrayList<Monster>();
    private static final double defaultTowerRange = 100.0;

    public TowerController(ScheduledFuture future) {
        this.future = future;
    }

    @Override
    public void run() {
        if (!canceled) {
            try {
                for (Monster tower : towers) {
                    if (!attackNearestMonster(tower)) {
                        selctMonster(tower);
                        attackNearestMonster(tower);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public void cancel() {
        if (future != null) {
            future.cancel(true);
            canceled = true;
        }
    }

    private boolean attackNearestMonster(Monster tower) {
        if (targetInRange(tower)) {
            try {
                if (isReuse(tower)) {
                    long begTime = ServerTime.getInstance().getTimeSinceStart();
                    tower.useSkill("DefaultTowerNuke");
                    NukeAction a = new NukeAction(new NukeBolt((JTSUnit) t, (JTSUnit) t.getTarget(), begTime));
                    ScheduledFuture f = executor.scheduleAtFixedRate(a, 0L, 10L, TimeUnit.MILLISECONDS);
                    a.setScheduledFuture(f);
                    MessageManager.getInstance().sendUseSkillMessage(tower, GameField.getInstance().getSkillId("DefaultTowerNuke"), begTime);
                    return true;
                }
            } catch (SkillException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return false;
        }
        return false;
    }

    private void selctMonster(Monster tower) {
        synchronized(monsters) {
            tower.setTagerId(-1L);
            for (Monster monster : monsters) {
                if (Point.distance(tower.getCurX(), tower.getCurY(), monster.getCurX(), monster.getCurY()) <= defaultTowerRange) {
                    tower.setTagerId(monster.getId());
                    break;
                }
            }
        }
    }

    private boolean targetInRange(Monster tower) {
        synchronized(monsters) {
            for (Monster monster : monsters) {
                if (monster.getId() == tower.getTagerId()) {
                    return Point.distance(tower.getCurX(), tower.getCurY(), monster.getCurX(), monster.getCurY()) <= defaultTowerRange;
                }
            }
            return false;
        }
    }

    private boolean isReuse(Monster tower) throws SkillException {
        return ServerTime.getInstance().getTimeSinceStart() >= tower.getLastSkillUseTime("DefaultTowerNuke") + GameField.getInstance().getSkillReuse("DefaultTowerNuke");
    }
}
