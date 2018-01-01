package io.github.diaco.core;

import io.github.diaco.actor.Actor;
import io.github.diaco.actor.LocalReference;
import io.github.diaco.actor.Reference;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

public class Scheduler {

    // TODO: collect scheduler statistics

    private static Integer currentActorIdentifier = 0;
    private ExecutorService executor;
    private BlockingQueue<Actor> runQueue;
    private Config config;

    public Scheduler(Config config) {
        this.config = config;
        Integer threadPoolSize = Integer.parseInt(config.getProperty(Config.SCHEDULER_THREAD_POOL_SIZE));
        Integer runQueueSize = Integer.parseInt(config.getProperty(Config.SCHEDULER_RUN_QUEUE_SIZE));
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
        this.runQueue = new PriorityBlockingQueue<Actor>(runQueueSize);
    }

    public Reference spawn(Node node, Actor actor) throws InterruptedException {
        actor.node(node);
        runQueue.put(actor);
        return new LocalReference(actor);
    }

    public Reference spawn(Actor actor) throws InterruptedException {
        runQueue.put(actor);
        return new LocalReference(actor);
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
