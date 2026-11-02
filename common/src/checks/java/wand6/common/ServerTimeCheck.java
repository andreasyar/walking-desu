package wand6.common;

/** Verifies soft TIMESYNC reconcile does not rewind motion for delivery-sized skew. */
public final class ServerTimeCheck {
    public static void main(String[] args) throws InterruptedException {
        ServerTime clock = ServerTime.getInstance();
        clock.adjust(10_000);
        long before = clock.getTimeSinceStart();
        Thread.sleep(20);
        long softError = clock.reconcile(before); // stamp delayed by ~20ms+
        check(Math.abs(softError) < ServerTime.IGNORE_MS, "delivery-sized skew stays soft");
        long after = clock.getTimeSinceStart();
        check(after >= before + 15, "ignored reconcile does not rewind local clock");

        long t = clock.getTimeSinceStart();
        CurveMovement movement = new CurveMovement(() -> t + 100);
        movement.start(0, 0, 700, 0, t);
        var p1 = movement.position(t + 100);
        long ignored = clock.reconcile(t + 100 - 80);
        check(Math.abs(ignored) < ServerTime.IGNORE_MS, "80ms delivery skew ignored");
        var p2 = movement.position(t + 100);
        check(p1.equals(p2), "soft sync keeps interpolated position stable");

        // Old hard adjust path would rewind ~80ms of travel (~5–6 px at 0.07 px/ms).
        ServerTime victim = ServerTime.getInstance();
        victim.adjust(50_000);
        long local = victim.getTimeSinceStart();
        CurveMovement hard = new CurveMovement(victim::getTimeSinceStart);
        hard.start(0, 0, 700, 0, local);
        var beforeSnap = hard.position(local + 100);
        victim.adjust(local + 100 - 80); // emulate legacy TIMESYNC hard snap
        var afterSnap = hard.position(victim.getTimeSinceStart());
        check(Math.abs(afterSnap.x() - beforeSnap.x()) >= 4,
                "legacy hard snap would jump motion (sanity)");

        long snapped = clock.reconcile(clock.getTimeSinceStart() - 5_000);
        check(Math.abs(snapped) >= ServerTime.IGNORE_MS, "large skew still hard-snaps");
        System.out.println("PASS: ServerTime soft reconcile ignores delivery noise.");
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
