package common;

/**
 * This class is a common part of Server Time Service what used both on clients
 * and on servers. Common part of service provide functions for store and get
 * server time.
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

    private WanderingServerTime() {
        serverTime = 0;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }
}