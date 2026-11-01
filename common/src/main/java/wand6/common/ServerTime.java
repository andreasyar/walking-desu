package wand6.common;

public class ServerTime {

    private static ServerTime me = null;

    private long serverStartTime;

    public static ServerTime getInstance() {
        if (me == null) {
            me = new ServerTime();
        }

        return me;
    }

    private ServerTime() {
        serverStartTime = System.currentTimeMillis();
    }

    public long getServerTime() {
        return serverStartTime;
    }

    public long getTimeSinceStart() {
        return System.currentTimeMillis() - serverStartTime;
    }

    public void adjust(long time) {
        serverStartTime = System.currentTimeMillis() - time;
    }
}
