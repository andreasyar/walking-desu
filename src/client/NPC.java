package client;

public class NPC extends Unit {
    private MovementAnimation moveAnim;
    private StandAnimation standAnim;

    private Movement move;

    private boolean isMove = false;

    public NPC() {
        moveAnim = new MovementAnimation("desu");
        standAnim = new StandAnimation("desu");
    }

    public NPC(String spriteSet) {
        moveAnim = new MovementAnimation(spriteSet);
        standAnim = new StandAnimation(spriteSet);
    }

    public void changeSpriteSet(String spriteSet) {
        moveAnim = new MovementAnimation(spriteSet);
        standAnim = new StandAnimation(spriteSet);
    }

    public Sprite getSprite() {
        return isMove ? moveAnim.getSprite(move.getCurPos()) : standAnim.getSprite(ServerInteraction.innerTimer, move.getCurPos());
    }
}
