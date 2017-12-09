package io.github.diaco;

import io.github.diaco.core.Scheduler;
import io.github.diaco.actor.Actor;

public class Diaco {

    private static Diaco instance;
    private Scheduler scheduler;

    protected Diaco() {
        scheduler = new Scheduler();
        scheduler.start();
    }

    public void spawn(Actor actor) {
        scheduler.spawn(actor);
    }

    public static Diaco getInstance() {
        if(instance == null)
            instance = new Diaco();

        return instance;
    }

}
