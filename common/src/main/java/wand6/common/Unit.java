package wand6.common;

public class Unit {

    public enum Kind { PLAYER, TOWER, MONSTER }
    private final Kind kind;
    private final int maxHp;
    public Kind getKind() { return kind; }
    public int getMaxHp() { return maxHp; }
    private int hp;
    private long life;
    private long lastAttack = -CombatRules.REUSE;
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public long getLife() { return life; }
    public void setLife(long life) { this.life = life; }
    public long getLastAttack() { return lastAttack; }
    public void setLastAttack(long time) { lastAttack = time; }
    private final long id;
    private String name;
    private String spriteSetName;
    private String text;
    private final CurveMovement movement;

    public Unit(long id, String name, String spriteSetName) {
        this(id, name, spriteSetName, Kind.PLAYER, CombatRules.MAX_HP, CurveMovement.SPEED);
    }

    public Unit(long id, String name, String spriteSetName, Kind kind, int maxHp, double speed) {
        this.kind = kind; this.maxHp = maxHp; this.hp = maxHp;
        this.id = id;
        this.name = name;
        this.spriteSetName = spriteSetName;
        movement = new CurveMovement(speed);
    }

    public CurveMovement getMovement() { return movement; }

    public void place(int x, int y) { movement.place(x, y); }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpriteSetName() {
        return spriteSetName;
    }

    public void setSpriteSetName(String spriteSetName) {
        this.spriteSetName = spriteSetName;
    }

    public int getCurX() {
        return movement.getCurX();
    }

    public int getCurY() {
        return movement.getCurY();
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void move(int x, int y, long begTime) {
        movement.start(x, y, begTime);
    }

    public int getEndX() {
        return movement.getEndX();
    }

    public int getEndY() {
        return movement.getEndY();
    }

    public long getMovementBegTime() {
        return movement.getBegTime();
    }
}
