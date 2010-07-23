package client;

import java.awt.Point;

abstract public class Unit {
    private MovementAnimation moveAnim;
    private StandAnimation standAnim;

    private Movement move;

    public Unit(int x, int y, Direction d, String set) {
        // TODO X Y
        moveAnim = new MovementAnimation(set);
        standAnim = new StandAnimation(set);
        standAnim.run(d, ServerInteraction.serverStartTime);
        move = new Movement();
    }

    public Unit(String set) {
        moveAnim = new MovementAnimation(set);
        standAnim = new StandAnimation(set);
        standAnim.run(Direction.SOUTH, ServerInteraction.serverStartTime);
        move = new Movement();
    }

    public Unit() {
        moveAnim = new MovementAnimation("desu");
        standAnim = new StandAnimation("desu");
        standAnim.run(Direction.SOUTH, ServerInteraction.serverStartTime);
        move = new Movement();
    }

    public void changeSpriteSet(String spriteSet) {
        moveAnim = new MovementAnimation(spriteSet);
        standAnim = new StandAnimation(spriteSet);
        standAnim.run(Direction.SOUTH, ServerInteraction.serverStartTime);
    }

    public Sprite getSprite() {
        return move.isMove() ?
            moveAnim.getSprite(move.getCurPos()) :
            standAnim.getSprite(System.currentTimeMillis() - ServerInteraction.serverStartTime, move.getCurPos());
    }

    public Point getCurPos() {
        return move.getCurPos();
    }

    public void move(Point beg, Point end, long begTime, double speed) {
        move.move(beg, end, begTime, speed);
        moveAnim.run(beg, end, 10.0);
        standAnim.run(moveAnim.getDirection(), move.getEndTime());
    }
}
