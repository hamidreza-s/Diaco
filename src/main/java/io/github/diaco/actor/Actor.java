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
    public void send(Reference reference, Message message);
    public void terminate(State<StateBodyType> state);
    public void stop();

    public Map<Integer, Reference> listLinkedBy();
    public Map<Integer, Reference> listMonitoredBy();

    public Status getStatus();
    public Integer getPriority();
    public Integer getReduction();
    public Integer getIdentifier();
    public Node getNode();
    public boolean hasNode();

    // TODO: make following api private to diaco
    public void setNode(Node node);
    public void setReference(Reference reference);
    public void setIdentifier(int identifier);
    public void putIntoMailbox(Reference from, Reference to, Message message);
    public void putIntoLinkedBy(Integer actorIdentifier, Reference reference);
    public void putIntoMonitoredBy(Integer actorIdentifier, Reference reference);
    public void removeFromLinkedBy(Integer actorIdentifier);
    public void removeFromMonitoredBy(Integer actorIdentifier);
}
