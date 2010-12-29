package wand6.common;

public class Player {

    private final long id;
    private String name;
    private String spriteSetName;
    private String text;
    private final CurveMovement movement;

    public Player(long id, String name, String spriteSetName) {
        this.id = id;
        this.name = name;
        this.spriteSetName = spriteSetName;
        movement = new CurveMovement();
    }

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
