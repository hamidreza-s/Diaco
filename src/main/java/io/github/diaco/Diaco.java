package io.github.diaco;

import io.github.diaco.core.Scheduler;
import io.github.diaco.core.Node;
import io.github.diaco.core.Config;
import io.github.diaco.actor.Actor;

public class Diaco {

    private static Diaco instance;
    private Scheduler scheduler;
    private Node node;

    private Diaco(Config config) {
        scheduler = Scheduler.getInstance(config);

        if(config.containsKey(Config.NODE_NAME))
            node = Node.getInstance(config);

    }

    public void spawn(Actor actor) {
        scheduler.spawn(actor);
    }

    public static Diaco getInstance(Config config) {
        if(instance == null)
            instance = new Diaco(config);

        return instance;
    }

    public void stop() {
        // TODO: stop scheduler and node
    }
}
