package wand6.common;

/** Adapted from stable 3.2 Tower/MonsterController. No item economy until stage 12. */
public final class TowerDefenceRules {
    private TowerDefenceRules() { }
    public static final long WAVE_DELAY = 20_000, TOWER_REUSE = 1000;
    public static final int LOSS_LIMIT = 100, TOWER_DAMAGE = 10, TOWER_RANGE = 200;
    public static final int DEST_X = 705, DEST_Y = 755;
    public static final int BUILD_RANGE = 500, BUILD_SPACING = 32, MAX_TOWERS = 128;
}
