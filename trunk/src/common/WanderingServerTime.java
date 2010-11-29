package common;

/**
 * This class is a Server Time Service what used both on clients and on servers.
 */
public class WanderingServerTime {

    private static WanderingServerTime me = null;

    /**
     * Server time.
     */
    private long serverTime;

    public static WanderingServerTime getInstance() {
        if (me == null) {
            me = new WanderingServerTime();
        }

        return me;
    }

    protected WanderingServerTime() {
        serverTime = 0L;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }
}
