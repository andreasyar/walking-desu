package newcommon;

/**
 * Класс времени сервера.
 */
public class ServerTime {

    private static ServerTime me = null;

    /**
     * Время запуска сервера. UNIX timestamp.
     */
    private long serverStartTime;

    public static ServerTime getInstance() {
        if (me == null) {
            me = new ServerTime();
        }

        return me;
    }

    protected ServerTime() {
        serverStartTime = System.currentTimeMillis();
    }

    public void setServerTime(long serverTime) {
        this.serverStartTime = serverTime;
    }

    public long getServerStartTime() {
        return serverStartTime;
    }

    public long getTimeSinceStart() {
        return System.currentTimeMillis() - serverStartTime;
    }
}
