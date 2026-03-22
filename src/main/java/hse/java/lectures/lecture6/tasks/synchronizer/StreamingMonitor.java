package hse.java.lectures.lecture6.tasks.synchronizer;

import java.util.Arrays;

public class StreamingMonitor {
    private final int[] sortedIds;
    private final int[] remaining;
    private final int totalTicks;
    private int ticksDone;
    private int currentIdx;
    private boolean finished;

    public StreamingMonitor(int[] ids, int ticksPerWriter) {
        this.sortedIds = ids.clone();
        Arrays.sort(this.sortedIds);
        this.remaining = new int[this.sortedIds.length];
        for (int i = 0; i < this.sortedIds.length; i++) {
            remaining[i] = ticksPerWriter;
        }
        this.totalTicks = ids.length * ticksPerWriter;
        this.ticksDone = 0;
        this.currentIdx = 0;
        this.finished = false;
    }

    public synchronized void awaitTurn(int id) throws InterruptedException {
        while (!finished && (currentIdx >= sortedIds.length || sortedIds[currentIdx] != id)) {
            wait();
        }
        while (finished) {
            wait();
        }
    }


    public synchronized void tickCompleted(int id) {
        ticksDone++;
        int idx = findIndex(id);
        remaining[idx]--;

        if (ticksDone == totalTicks) {
            finished = true;
            notifyAll();
            return;
        }

        int next = (currentIdx + 1) % sortedIds.length;
        while (remaining[next] == 0 && next != currentIdx) {
            next = (next + 1) % sortedIds.length;
        }
        currentIdx = next;
        notifyAll();
    }


    public synchronized void waitForCompletion() throws InterruptedException {
        while (!finished) {
            wait();
        }
    }

    private int findIndex(int id) {
        for (int i = 0; i < sortedIds.length; i++) {
            if (sortedIds[i] == id) return i;
        }
        throw new IllegalArgumentException("Unknown writer id: " + id);
    }
}