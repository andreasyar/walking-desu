package newcommon;

public class Monster extends Unit {

    public Monster(long id, String name, int maxHealth, int health, String spriteSetName, String text, long deathDelay) {
        super(id, name, maxHealth, health, spriteSetName, text, deathDelay);
    }

    public boolean isDead() {
        return getHealth() <= 0;
    }

    public void hit(int dmg) {
        setHealth(getHealth() - dmg);
        if (getHealth() < 0) {
            setHealth(0);
        }
    }

    public void kill() {
        setHealth(0);
        stop();
    }

    public void resurect() {
        setHealth(getMaxHealth());
    }
}
