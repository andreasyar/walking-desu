package client;

import java.awt.Point;

abstract public class Unit {
    protected MovementAnimation moveAnim;
    protected StandAnimation standAnim;

    protected Movement move;

    public Point getCurPos() {
        return move.getCurPos();
    }

    protected Unit(int x, int y, Direction d, String set) {
        // TODO X Y
        moveAnim = new MovementAnimation(set);
        standAnim = new StandAnimation(set);
        standAnim.run(d, ServerInteraction.innerTimer);
        move = new Movement();
    }

    protected Unit(String set) {
        moveAnim = new MovementAnimation(set);
        standAnim = new StandAnimation(set);
        standAnim.run(Direction.SOUTH, ServerInteraction.innerTimer);
        move = new Movement();
    }

    protected Unit() {
        moveAnim = new MovementAnimation("desu");
        standAnim = new StandAnimation("desu");
        standAnim.run(Direction.SOUTH, ServerInteraction.innerTimer);
        move = new Movement();
    }

    public void changeSpriteSet(String spriteSet) {
        moveAnim = new MovementAnimation(spriteSet);
        standAnim = new StandAnimation(spriteSet);
        standAnim.run(Direction.SOUTH, ServerInteraction.innerTimer);
    }

    public Sprite getSprite() {
        return move.isMove() ? moveAnim.getSprite(move.getCurPos()) : standAnim.getSprite(ServerInteraction.innerTimer, move.getCurPos());
    }
}
