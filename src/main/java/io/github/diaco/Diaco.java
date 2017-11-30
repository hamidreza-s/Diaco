package io.github.diaco;

import io.github.diaco.kernel.RunQueue;
import io.github.diaco.kernel.Scheduler;

public class Diaco {

    private static Diaco instance = null;

    private RunQueue runQueue = null;

    private Scheduler scheduler = null;

    protected Diaco() {
        runQueue = new RunQueue();
        scheduler = new Scheduler(runQueue);
        scheduler.start();
    }

    public void spawn(Runnable actor) {
        try {
            runQueue.put(actor);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void halt() {
        // TODO ...
    }

    public static Diaco getInstance() {
        if(instance == null)
            instance = new Diaco();

        return instance;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public RunQueue getRunQueue() {
        return runQueue;
    }
}
