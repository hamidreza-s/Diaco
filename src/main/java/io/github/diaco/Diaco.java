package io.github.diaco;

import io.github.diaco.core.Scheduler;
import io.github.diaco.core.Node;
import io.github.diaco.core.Config;
import io.github.diaco.actor.Actor;
import java.util.HashMap;
import java.util.Map;

public class Diaco {

    private Scheduler scheduler;
    private Node node;
    private static Map<String, Integer> actorCounter = new HashMap<String, Integer>();

    private Diaco(Config config) {
        try {
            Config.checkRequired(config);

            scheduler = new Scheduler(config);
            scheduler.start();

            if (config.containsKey(Config.NODE_NAME)) {
                node = new Node(config);
                node.start();
            }

        } catch(RuntimeException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void spawn(Actor actor) {
        scheduler.spawn(actor);
    }

    public void stop() {
        // TODO: stop scheduler and node
    }

    public static Diaco newInstance(Config config) {
        return new Diaco(config);
    }
}
