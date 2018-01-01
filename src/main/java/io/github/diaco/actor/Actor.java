package io.github.diaco.actor;

import io.github.diaco.core.Node;
import io.github.diaco.message.Message;
import java.util.Map;

public interface Actor<StateBodyType> extends Runnable {

    public enum Status {
        STARTING,
        WAITING,
        RUNNING,
        EXITING
    }

    public void init(State<StateBodyType> state);
    public void receive(Message message, State<StateBodyType> state);
    public void send(Actor actor, Message message);
    public void link(Actor actor);
    public void unlink(Actor actor);
    public void monitor(Actor actor);
    public void unmonitor(Actor actor);
    public void exit(Actor actor);
    public void exited(Actor actor);
    public void terminate(State<StateBodyType> state);
    public void stop();

    public Map<Integer, Actor> listLinkedBy();
    public Map<Integer, Actor> listMonitoredBy();

    public Status getStatus();
    public Integer getPriority();
    public Integer getReduction();
    public Integer getIdentifier();
    public Node getNode();
    public boolean hasNode();

    // TODO: make following api private to diaco
    public void node(Node node);
}
