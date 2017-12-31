package io.github.diaco.core;

import io.github.diaco.actor.Actor;
import io.github.diaco.core.Config;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

public class Scheduler {

    private static Integer currentActorIdentifier = 0;
    private ExecutorService executor;
    private BlockingQueue<Actor> runQueue;
    private Config config;

    // TODO: collect scheduler statistics
    public Scheduler(Config config) {
        this.config = config;
        Integer threadPoolSize = Integer.parseInt(config.getProperty(Config.SCHEDULER_THREAD_POOL_SIZE));
        Integer runQueueSize = Integer.parseInt(config.getProperty(Config.SCHEDULER_RUN_QUEUE_SIZE));
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
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
