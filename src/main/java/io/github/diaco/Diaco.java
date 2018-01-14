package io.github.diaco;

import io.github.diaco.actor.Reference;
import io.github.diaco.core.Scheduler;
import io.github.diaco.core.Node;
import io.github.diaco.core.Config;
import io.github.diaco.actor.Actor;
import java.util.Set;

// TODO: implement a shell class for REPL

public class Diaco {

    private Scheduler scheduler;
    private Node node;

    private Diaco(Config config) {
        try {
            Config.checkRequired(config);

            scheduler = new Scheduler(config);
            scheduler.start();
            node = new Node(config);
            node.start();

        } catch(RuntimeException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Reference spawn(Actor actor) {
        try {
            return scheduler.spawn(node, actor);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Set<String> getNodeNames() {
        return node.getNodes().keySet();
    }

    public boolean isDistributed() {
        return !(this.node == null);
    }

    public void stop() {
        // TODO: stop scheduler and node
    }

    public static Diaco newInstance(Config config) {
        return new Diaco(config);
    }
}
