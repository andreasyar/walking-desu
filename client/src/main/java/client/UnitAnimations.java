package client;

import java.awt.Point;

/** One visual state owner per unit. Accessed under the owning WUnit monitor. */
final class UnitAnimations {
    private final MovementAnimation movement;
    private final StandAnimation standing;
    private final UseSkillAnimation attack;
    private final DeathAnimation death;
    private Direction direction;
    private boolean dying;
    private long deathTime;
    private boolean disposed;

    UnitAnimations(String name, Direction direction, long time) {
        if (!java.util.Set.of("desu", "peasant", "peon", "poring", "tower").contains(name)) {
            throw new IllegalArgumentException("Unknown unit sprite set: " + name);
        }
        this.direction = java.util.Objects.requireNonNull(direction);
        movement = new MovementAnimation(name);
        standing = new StandAnimation(name);
        standing.run(direction, time);
        attack = (name.equals("peasant") || name.equals("peon")) ? new UseSkillAnimation(name) : null;
        // peon has no death frames; preserve the established peasant death fallback explicitly.
        death = switch (name) {
            case "peasant", "peon" -> new DeathAnimation("peasant");
            case "poring" -> new DeathAnimation("poring");
            default -> null; // desu/tower: static standing frame, no death animator.
        };
    }

    void move(int bx, int by, int ex, int ey, long endTime) {
        if (dying || disposed) return;
        if (attack != null) attack.stop();
        if (bx != ex || by != ey) {
            movement.run(bx, by, ex, ey, 10.0);
            direction = movement.getDirection();
        }
        standing.run(direction, endTime);
    }

    boolean attack(Direction direction, long time) {
        if (disposed || dying) return false;
        this.direction = direction;
        standing.run(direction, time);
        if (attack != null) attack.run(direction, time);
        return true;
    }

    boolean isAttacking(long time) { return !disposed && !dying && attack != null && attack.isPlaying(time); }

    long nukeDelay() { return attack == null ? 0 : attack.getNukeAnimationDelay(); }

    void die(long time) {
        if (dying || disposed) return;
        dying = true;
        deathTime = time;
        if (attack != null) attack.stop();
        if (death != null) death.run(direction, time);
    }

    void revive(long time) {
        dying = false;
        if (attack != null) attack.stop();
        standing.run(direction, time);
    }

    boolean deathDone(long time) { return death == null || death.isDone(time); }

    Sprite sprite(long time, Point position, boolean moving, boolean dead) {
        if (disposed) return null;
        if (dead) return death == null ? standing.getSprite(deathTime, position) : death.getSprite(time, position);
        if (isAttacking(time)) return attack.getSprite(time, position);
        return moving ? movement.getSprite(position) : standing.getSprite(time, position);
    }

    void dispose() {
        disposed = true;
        if (attack != null) attack.stop();
    }
}
