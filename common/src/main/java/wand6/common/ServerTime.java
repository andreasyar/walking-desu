package wand6.common;

/** Server-relative milliseconds backed by a monotonic local clock. */
public final class ServerTime {
    private static final ServerTime INSTANCE = new ServerTime();
    private volatile long origin = System.nanoTime();
    private ServerTime() { }
    public static ServerTime getInstance() { return INSTANCE; }
    public long getTimeSinceStart() { return (System.nanoTime() - origin) / 1_000_000; }
    public void adjust(long time) { origin = System.nanoTime() - time * 1_000_000; }
}
