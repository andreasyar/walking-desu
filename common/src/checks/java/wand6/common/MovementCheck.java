package wand6.common;

public final class MovementCheck {
    public static void main(String[] args) {
        long[] clock = {1000};
        CurveMovement movement = new CurveMovement(() -> clock[0]);
        movement.place(10, 20);
        movement.start(220, 300, 1000);
        check(movement.getEndTime() == 6000, "3-4-5 duration at 70 px/sec");
        check(movement.position(0).equals(new CurveMovement.Position(10, 20)), "before start clamps");
        check(movement.position(3500).equals(new CurveMovement.Position(115, 160)), "diagonal interpolation");
        check(movement.position(6000).equals(new CurveMovement.Position(220, 300)), "exact arrival");
        check(movement.position(Long.MAX_VALUE).equals(new CurveMovement.Position(220, 300)), "no overshoot");
        clock[0] = 3500;
        movement.start(185, 160, clock[0]);
        check(movement.getBegX() == 115 && movement.getBegY() == 160, "retarget starts at current point");
        clock[0] = 4000;
        movement.stop();
        check(movement.position(100000).equals(new CurveMovement.Position(150, 160)) && !movement.isMoving(), "stop stable");
        movement.start(150, 160, 4000);
        check(!movement.isMoving(), "zero length does not divide by zero");
        check(!movement.isMovingAt(0), "zero segment remains standing before timestamp, including clock resync");
        movement.start(MapFragment.maxX, MapFragment.maxY, 0, 0, 4000);
        check(movement.position(4000).x() == MapFragment.maxX, "large coordinates");
        check(MapFragment.contains(MapFragment.maxX, MapFragment.maxY), "last coordinate valid");
        check(!MapFragment.contains(MapFragment.maxX + 1, 0) && !MapFragment.contains(-1, 0), "world bounds");
        check(Terrain.passable(512, 512), "spawn on land");
        check(!Terrain.passable(-1, 0) && !Terrain.passable(Integer.MAX_VALUE, 0), "invalid terrain");
        System.out.println("PASS: movement interpolation/time, retarget, stop, zero length, world bounds.");
    }
    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
