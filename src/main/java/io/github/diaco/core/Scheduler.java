package io.github.diaco.core;

import io.github.diaco.actor.Actor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

public class Scheduler {

    private static final int DEFAULT_RUN_QUEUE_SIZE = 1024;
    private static Integer currentActorIdentifier = 0;
    private ExecutorService executor;
    private BlockingQueue<Actor> runQueue;

    // TODO: collect scheduler statistics
    public Scheduler() {
        this(
                Runtime.getRuntime().availableProcessors(),
                DEFAULT_RUN_QUEUE_SIZE
        );
    }

    public Scheduler(int poolSize, int runQueueSize) {
        this.executor = Executors.newFixedThreadPool(poolSize);
        this.runQueue = new PriorityBlockingQueue<Actor>(runQueueSize);
    }

    public void spawn(Actor actor) {
        try {
            runQueue.put(actor);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Integer getFreeActorIdentifier() {
        return currentActorIdentifier++;
    }

    public void start() {
        new Thread(new Runnable() {
            public void run() {
                while(true) {
                    try {
                        executor.execute(runQueue.take());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
