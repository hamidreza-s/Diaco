package io.github.diaco.core;

import io.github.diaco.actor.Actor;
import io.github.diaco.actor.Reference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class Scheduler {

    // TODO: collect scheduler statistics
    // TODO: use ExecutorService::submit and store returned Future objects
    // TODO: check Future objects and remove dead ones from LocalReference.actorsMap
    // TODO: implement guava's thread-factory in diaco, then remove its package from depsk

    private static Integer currentActorIdentifier = 0;
    private ExecutorService executor;
    private BlockingQueue<Actor> runQueue;
    private Config config;

    public Scheduler(Config config) {
        this.config = config;
        Integer actorSpawningDepth = Integer.parseInt(config.getProperty(Config.ACTOR_SPAWNING_DEPTH));
        Integer threadPoolSize = Integer.parseInt(config.getProperty(Config.SCHEDULER_THREAD_POOL_SIZE));
        Integer runQueueSize = Integer.parseInt(config.getProperty(Config.SCHEDULER_RUN_QUEUE_SIZE));

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("diaco-worker-%d")
                .build();

        this.executor = Executors.newFixedThreadPool(threadPoolSize + actorSpawningDepth, threadFactory);
        this.runQueue = new PriorityBlockingQueue<Actor>(runQueueSize);
    }

    public Reference spawn(Node node, Actor actor) throws InterruptedException {
        if(actor.getIdentifier() != null)
            throw new RuntimeException("re-spawning actor is not allowed!");

        // @NOTE: puttin actor into run-queue must be the last thing

        int identifier = getFreeActorIdentifier();
        Reference reference = new Reference(identifier, node.getName());
        actor.setScheduler(this);
        actor.setNode(node);
        actor.setReference(reference);
        actor.setIdentifier(identifier);
        Registry.addActor(actor);
        this.putIntoRunQueue(actor);
        return reference;
    }

    public static Integer getFreeActorIdentifier() {
        return currentActorIdentifier++;
    }

    public void start() {
        Thread scheduler = new Thread(new Runnable() {
            public void run() {
                while(true) {
                    try {
                        executor.execute(runQueue.take());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        scheduler.setName("diaco-scheduler");
        scheduler.start();
    }

    public void putIntoRunQueue(Actor actor) throws InterruptedException {
        this.runQueue.put(actor);
    }
}
