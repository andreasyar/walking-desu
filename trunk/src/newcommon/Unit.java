package newcommon;

import common.skills.Skill;
import java.awt.Point;
import java.util.ArrayList;

public abstract class Unit extends Entity {

    private int maxHealth;
    private int health;
    private long deathTime;
    private long deathDelay;
    private boolean isDeathDelay;
    private String spriteSetName;
    private final CurveMovement mv = new CurveMovement();
    private String text;
    private final ArrayList<Skill> skills = new ArrayList<Skill>();

    public Unit(long id, String name, int maxHealth, int health, String spriteSetName, String text, long deathDelay) {
        super(id, name);
        this.maxHealth = maxHealth;
        this.health = health;
        this.spriteSetName = spriteSetName;
        this.text = text;
        this.deathDelay = deathDelay;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public String getSpriteSetName() {
        return spriteSetName;
    }

    public void setSpriteSetName(String spriteSetName) {
        this.spriteSetName = spriteSetName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getCurX() {
        return mv.getCurX();
    }

    public int getCurY() {
        return mv.getCurY();
    }

    public boolean isMove() {
        return mv.isMove();
    }

    public void stop() {
        mv.stop();
    }

    public void move(ArrayList<Point> track, long begTime, double speed) {
        mv.start(track, begTime, speed);
    }

    public void move(int begX, int begY, int endX, int endY, long begTime, double speed) {
        mv.start(begX, begY, endX, endY, begTime, speed);
    }

    public abstract boolean isDead();

    public abstract void hit(int dmg);

    public abstract void kill();

    public abstract void resurect();

    public void addSkill(Skill s) {
        synchronized(skills) {
            skills.add(s);
        }
    }

    public void removeSkill(Skill s) {
        synchronized(skills) {
            skills.remove(s);
        }
    }

    public Skill getSkill(long id) {
        synchronized(skills) {
            for (Skill s : skills) {
                if (s.getId() == id) {
                    return s;
                }
            }
        }

        return null;
    }

    public void useSkill(Skill s) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean isUseSkill() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean isStand() {
        return !isMove() && !isDead() && !isUseSkill();
    }

    public long getDeathTime() {
        return deathTime;
    }

    public void setDeathTime(long deathTime) {
        this.deathTime = deathTime;
    }

    public boolean isIsDeathDelay() {
        return isDeathDelay;
    }

    public void setIsDeathDelay(boolean isDeathDelay) {
        this.isDeathDelay = isDeathDelay;
    }

    public long getDeathDelay() {
        return deathDelay;
    }

    public void setDeathDelay(long deathDelay) {
        this.deathDelay = deathDelay;
    }
}
