package io.github.diaco.actor;

import io.github.diaco.core.Node;
import io.github.diaco.core.Scheduler;
import io.github.diaco.message.Envelope;
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

    private Scheduler scheduler;
    private Node node;
    private Reference reference;

    private Integer priority;
    private Integer reduction;
    private Integer identifier;
    private boolean trapExit;
    private BlockingQueue<Envelope> mailbox;
    private Status status;
    private State<StateBodyType> state;
    private Map<Integer, Reference> linkedBy;
    private Map<Integer, Reference> monitoredBy;


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
        this.mailbox = new PriorityBlockingQueue<Envelope>(mailboxSize);
        this.linkedBy = new HashMap<Integer, Reference>();
        this.monitoredBy = new HashMap<Integer, Reference>();
    }

    public abstract void init(State<StateBodyType> state);

    public abstract void receive(Message message, State<StateBodyType> state);

    public abstract void terminate(State<StateBodyType> state);

    public final void send(Reference reference, Message message) {
        this.reference.send(reference, message);
    }

    public void putIntoMailbox(Envelope envelope) {
        try {
            this.mailbox.put(envelope);
            this.scheduler.putIntoRunQueue(this);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void putIntoLinkedBy(Integer actorIdentifier, Reference reference) {
        this.linkedBy.put(actorIdentifier, reference);
    }

    public void putIntoMonitoredBy(Integer actorIdentifier, Reference reference) {
        this.monitoredBy.put(actorIdentifier, reference);
    }

    public void removeFromLinkedBy(Integer actorIdentifier) {
        this.linkedBy.remove(actorIdentifier);
    }

    public void removeFromMonitoredBy(Integer actorIdentifier) {
        this.monitoredBy.remove(actorIdentifier);
    }

    public final void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
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

    private void internalReceive(Envelope envelope) {
        this.status = Status.RUNNING;
        Message message = envelope.getMessage();
        Reference senderReference = envelope.getFrom();

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
                    this.putIntoLinkedBy(senderReference.getActorIdentifier(), senderReference);
                    break;
                case UNLINK:
                    this.removeFromLinkedBy(senderReference.getActorIdentifier());
                    break;
                case MONITOR:
                    this.putIntoMonitoredBy(senderReference.getActorIdentifier(), senderReference);
                    break;
                case UNMONITOR:
                    this.removeFromMonitoredBy(senderReference.getActorIdentifier());
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

        this.status = Status.WAITING;
        Envelope envelope = mailbox.poll();
        if(envelope != null) {
            internalReceive(envelope);
            if (this.status == Status.RUNNING) {
                internalLoop();
            }
        }
    }

    private void internalTerminate() {
        Reference reference = new Reference(this.getIdentifier(), this.getNode().getName());
        for(Map.Entry<Integer, Reference> entry : this.monitoredBy.entrySet()) {
            reference.exited(entry.getValue());
        }

        for(Map.Entry<Integer, Reference> entry : this.linkedBy.entrySet()) {
            reference.exit(entry.getValue());
        }

        terminate(this.state);
    }

    public void run() {
        internalInit();
        internalLoop();
        if(this.status == Status.EXITING) {
            internalTerminate();
        }
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

    public Map<Integer, Reference> listLinkedBy() {
        return this.linkedBy;
    }

    public Map<Integer, Reference> listMonitoredBy() {
        return this.monitoredBy;
    }
}
