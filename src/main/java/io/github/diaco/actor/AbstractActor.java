package io.github.diaco.actor;

import io.github.diaco.core.Node;
import io.github.diaco.core.Scheduler;
import io.github.diaco.message.Message;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

abstract class AbstractActor<StateBodyType> implements Actor<StateBodyType>, Comparable<Actor> {

    // TODO: add API for trapExit
    // TODO: put actor's runnable future here

    public static final int DEFAULT_PRIORITY = 0;
    public static final int DEFAULT_MAILBOX_SIZE = 1024;

    private Node node;
    private Reference reference;

    private Integer priority;
    private Integer reduction;
    private Integer identifier;
    private boolean trapExit;
    private BlockingQueue<Message> mailbox;
    private Status status;
    private State<StateBodyType> state;
    private Map<Integer, Actor> linkedBy;
    private Map<Integer, Actor> monitoredBy;


    protected AbstractActor() {
        this(DEFAULT_PRIORITY, DEFAULT_MAILBOX_SIZE);
    }

    protected AbstractActor(int priority, int mailboxSize) {
        this.status = Status.STARTING;
        this.state = new State<StateBodyType>();
        this.priority = priority;
        this.reduction = 0;
        this.identifier = identifier;
        this.trapExit = false;
        this.mailbox = new PriorityBlockingQueue<Message>(mailboxSize);
        this.linkedBy = new HashMap<Integer, Actor>();
        this.monitoredBy = new HashMap<Integer, Actor>();
    }

    public abstract void init(State<StateBodyType> state);

    public abstract void receive(Message message, State<StateBodyType> state);

    public abstract void terminate(State<StateBodyType> state);

    public final void send(Reference reference, Message message) {
        this.reference.send(reference, message);
    }

    public void putIntoMailbox(Message message) {
        try {
            this.mailbox.put(message);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void putIntoLinkedBy(Integer actorIdentifier, Actor actor) {
        this.linkedBy.put(actorIdentifier, actor);
    }

    public void putIntoMonitoredBy(Integer actorIdentifier, Actor actor) {
        this.monitoredBy.put(actorIdentifier, actor);
    }

    public void removeFromLinkedBy(Integer actorIdentifier) {
        this.linkedBy.remove(actorIdentifier);
    }

    public void removeFromMonitoredBy(Integer actorIdentifier) {
        this.monitoredBy.remove(actorIdentifier);
    }

    public final void setNode(Node node) {
        if(this.node != null) return;
        this.node = node;
    }

    public final void setReference(Reference reference) {
        if(this.reference != null) return;
        this.reference = reference;
    }

    public final void setIdentifier(int identifier) {
        if(this.identifier != null) return;
        this.identifier = identifier;
    }

    public final boolean hasNode() {
        return !(this.node == null);
    }

    private void internalReceive(Message message) {
        this.status = Status.RUNNING;

        if(message.getPriority() == 0) {
            switch(message.getType()) {
                case EXIT:
                    if(trapExit) {
                        receive(message, this.state);
                    } else {
                        this.status = Status.EXITING;
                        return;
                    }
                    break;
                case LINK:
                    this.putIntoLinkedBy(message.getFrom().getIdentifier(), message.getFrom());
                    break;
                case UNLINK:
                    this.removeFromLinkedBy(message.getFrom().getIdentifier());
                    break;
                case MONITOR:
                    this.putIntoMonitoredBy(message.getFrom().getIdentifier(), message.getFrom());
                    break;
                case UNMONITOR:
                    this.removeFromMonitoredBy(message.getFrom().getIdentifier());
                    break;
            }

        } else {
            receive(message, this.state);
        }
    }

    private void internalInit() {
        init(this.state);
    }

    private void internalLoop() {

        reduction++;

        try {
            this.status = Status.WAITING;
            Message message = mailbox.take();
            internalReceive(message);
            if(this.status == Status.RUNNING) {
                internalLoop();
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void internalTerminate() {
        Reference reference = new Reference(this.getIdentifier(), this.getNode().getName());
        for(Map.Entry<Integer, Actor> entry : this.monitoredBy.entrySet()) {
            reference.exited(new Reference(entry.getValue().getIdentifier(), entry.getValue().getNode().getName()));
        }

        for(Map.Entry<Integer, Actor> entry : this.linkedBy.entrySet()) {
            reference.exit(new Reference(entry.getValue().getIdentifier(), entry.getValue().getNode().getName()));
        }

        terminate(this.state);
    }

    public void run() {
        internalInit();
        internalLoop();
        internalTerminate();
    }

    public void stop() {
        this.status = Status.EXITING;
    }

    public int compareTo(Actor other) {
        return this.getPriority().compareTo(other.getPriority());
    }

    public Status getStatus() {
        return this.status;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public Integer getReduction() {
        return this.reduction;
    }

    public Integer getIdentifier() {
        return this.identifier;
    }

    public Node getNode() {
        return this.node;
    }

    public Map<Integer, Actor> listLinkedBy() {
        return this.linkedBy;
    }

    public Map<Integer, Actor> listMonitoredBy() {
        return this.monitoredBy;
    }
}
