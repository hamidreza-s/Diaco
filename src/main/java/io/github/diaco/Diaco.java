package io.github.diaco;

import io.github.diaco.core.Scheduler;
import io.github.diaco.actor.Actor;
import io.github.diaco.node.Node;

public class Diaco {

    // TODO: define core.Configuration

    // TODO: define interface message.Serializer
    // TODO: define class     message.ObjectSerializer

    // TODO: define class     node.Node
    // TODO: define class     node.Registry

    private static Diaco instance;
    private Scheduler scheduler;
    private Node node;

    private Diaco() {
        scheduler = new Scheduler();
        scheduler.start();

        node = new Node();
        node.start();
    }

    public void spawn(Actor actor) {
        scheduler.spawn(actor);
    }

    public static Diaco getInstance() {
        if(instance == null)
            instance = new Diaco();

        return instance;
    }

    public void stop() {
        scheduler.stop();
        node.stop();
    }
}
