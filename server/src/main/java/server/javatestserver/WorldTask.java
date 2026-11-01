package server.javatestserver;

import java.util.concurrent.ScheduledFuture;

/** A task cannot act until bound. A late Future is cancelled if cancellation already won. */
abstract class WorldTask implements Runnable {
    final WorldState world;
    private ScheduledFuture<?> future;
    private boolean canceled;
    WorldTask(WorldState world) { this.world = world; }
    final void setScheduledFuture(ScheduledFuture<?> future) {
        synchronized (world) {
            if (this.future != null) throw new IllegalStateException("Task already bound");
            this.future = future;
            if (canceled) future.cancel(false);
        }
    }
    void cancel() {
        synchronized (world) {
            canceled = true;
            if (future != null) future.cancel(false);
        }
    }
    @Override public final void run() {
        synchronized (world) {
            if (canceled || future == null) return;
            try { step(); }
            catch (RuntimeException failure) { failed(failure); }
        }
    }
    void failed(RuntimeException failure) { failure.printStackTrace(); }
    abstract void step();
}
