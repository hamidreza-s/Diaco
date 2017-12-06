package io.github.diaco.core;

import io.github.diaco.actor.Actor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scheduler {

    protected ExecutorService executor;

    // TODO: use configuration for thread pool number
    // TODO: execute actors based on their priority
    // TODO: collect scheduler statistics
    public Scheduler() {
        this.executor = Executors.newFixedThreadPool(10);
    }

    public void spawn(Actor actor) {
        executor.execute(actor);
    }

}
