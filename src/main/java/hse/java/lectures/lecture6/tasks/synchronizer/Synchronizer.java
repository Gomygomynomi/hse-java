package hse.java.lectures.lecture6.tasks.synchronizer;

import java.util.List;
import java.util.stream.IntStream;

public class Synchronizer {
    public static final int DEFAULT_TICKS_PER_WRITER = 10;
    private final List<StreamWriter> tasks;
    private final int ticksPerWriter;

    public Synchronizer(List<StreamWriter> tasks) {
        this(tasks, DEFAULT_TICKS_PER_WRITER);
    }

    public Synchronizer(List<StreamWriter> tasks, int ticksPerWriter) {
        this.tasks = tasks;
        this.ticksPerWriter = ticksPerWriter;
    }

    public void execute() throws InterruptedException {
        int[] ids = tasks.stream().mapToInt(StreamWriter::getId).toArray();
        StreamingMonitor monitor = new StreamingMonitor(ids, ticksPerWriter);

        for (StreamWriter writer : tasks) {
            writer.attachMonitor(monitor);
        }

        for (StreamWriter writer : tasks) {
            Thread worker = new Thread(writer, "stream-writer-" + writer.getId());
            worker.setDaemon(true);
            worker.start();
        }

        monitor.waitForCompletion();
    }
}