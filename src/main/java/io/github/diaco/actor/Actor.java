package io.github.diaco.actor;

import io.github.diaco.message.Message;

import java.util.List;
import java.util.Map;


public interface Actor<State, MessageBodyType> extends Runnable {

    public enum Status {
        STARTING,
        WAITING,
        RUNNING,
        EXITING
    }

    public void init(List<State> state);
    public void receive(Message<MessageBodyType> message, List<State> state);
    public void send(Actor actor, Message message);
    public void link(Actor actor);
    public void unlink(Actor actor);
    public void monitor(Actor actor);
    public void unmonitor(Actor actor);
    public void exit(Actor actor);
    public void terminate(List<State> state);
    public void stop();

    public Map<Integer, Actor> listLinkedBy();
    public Map<Integer, Actor> listMonitoredBy();

    public Status getStatus();
    public Integer getPriority();
    public Integer getReduction();
    public Integer getIdentifier();

}
