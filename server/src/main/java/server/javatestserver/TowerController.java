package server.javatestserver;

/** Preserves range, damage, reuse and first eligible monster selection. */
final class TowerController extends WorldTask {
    private final CombatController combat;
    TowerController(WorldState world, CombatController combat) {
        super(world); this.combat = combat;
    }
    @Override void step() {
        for (Tower tower : world.towers) {
            if (!attack(tower)) {
                tower.selectMonster(world.monsters);
                attack(tower);
            }
        }
    }
    private boolean attack(Tower tower) {
        if (tower.getTarget() == null || !world.monsters.contains(tower.getTarget()) || !tower.targetInRange() || tower.reuse()) return false;
        tower.setLastAttackTime(System.currentTimeMillis() - JavaTestServer.serverStartTime);
        combat.attack(tower, tower.getTarget().getID(), false);
        return true;
    }
}
