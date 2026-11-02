package wand6.common;

/**
 * Server-relative milliseconds backed by a monotonic local clock.
 *
 * <p>Periodic one-way {@code TIMESYNC} stamps are already stale by the delivery
 * delay when applied. A hard {@link #adjust} on every message rewinds every
 * moving unit by roughly that delay (plus any EDT wait), which looks like a
 * hitch while the FPS counter still reads ~25. After the initial welcome snap,
 * small skew is treated as delivery noise; only serious drift forces a resync.
 */
public final class ServerTime {
    /** Skew below this (ms) is ignored on soft reconcile — typical one-way jitter. */
    public static final long IGNORE_MS = 250;
    private static final ServerTime INSTANCE = new ServerTime();
    private volatile long origin = System.nanoTime();
    private volatile long lastErrorMs;
    private ServerTime() { }
    public static ServerTime getInstance() { return INSTANCE; }
    public long getTimeSinceStart() { return (System.nanoTime() - origin) / 1_000_000L; }
    /** Last soft-reconcile error in ms (remote − local at apply time). */
    public long lastErrorMs() { return lastErrorMs; }

    /** Hard snap used for WELCOME / serious skew. */
    public void adjust(long time) {
        origin = System.nanoTime() - time * 1_000_000L;
        lastErrorMs = 0;
    }

    /**
     * Soft sync for periodic TIMESYNC. Returns the observed error in ms.
     * Ignores |error| &lt; {@link #IGNORE_MS}; otherwise hard-snaps.
     */
    public long reconcile(long remoteTime) {
        long now = System.nanoTime();
        long local = (now - origin) / 1_000_000L;
        long error = remoteTime - local;
        lastErrorMs = error;
        if (Math.abs(error) < IGNORE_MS) {
            return error;
        }
        origin = now - remoteTime * 1_000_000L;
        return error;
    }
}
