package wand6.server;

import java.util.Timer;
import java.util.TimerTask;
import wand6.common.ServerTime;
import wand6.server.messages.TimeSyncMessage;

class TimeSyncTask extends TimerTask {

    private JavaServer server;
    private Timer timer = new Timer();

    public TimeSyncTask(JavaServer server) {
        this.server = server;
    }

    public void schedule(long period) {
        timer.schedule(this, 0, period);
    }

    @Override
    public void run() {
        server.sendMessage(new TimeSyncMessage(ServerTime.getInstance().getTimeSinceStart()));
    }

    @Override
    public boolean cancel() {
        boolean tmp = super.cancel();
        timer.cancel();
        return tmp;
    }
}
