package io.github.diaco.actor;

import io.github.diaco.core.Node;
import io.github.diaco.core.Scheduler;
import io.github.diaco.message.Envelope;
import io.github.diaco.message.Message;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class BaseActor<StateBodyType> implements Actor<StateBodyType>, Comparable<Actor> {

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
    private boolean isAlive;
    private BlockingQueue<Envelope> mailbox;
    private Status status;
    private State<StateBodyType> state;
    private Map<Integer, Reference> linkedBy;
    private Map<Integer, Reference> monitoredBy;


    protected BaseActor() {
        this(DEFAULT_PRIORITY, DEFAULT_MAILBOX_SIZE);
    }

    protected BaseActor(int priority, int mailboxSize) {
        this.status = Status.STARTING;
        this.priority = priority;
        this.reduction = 0;
        this.identifier = identifier;
        this.trapExit = false;
        this.isAlive = true;
        this.mailbox = new PriorityBlockingQueue<Envelope>(mailboxSize);
        this.linkedBy = new HashMap<Integer, Reference>();
        this.monitoredBy = new HashMap<Integer, Reference>();
    }

    public State<StateBodyType> init() {
        return new State<StateBodyType>();
    }

    public State<StateBodyType> receive(Message message, State<StateBodyType> state) {
        return state;
    }

    public void terminate(State<StateBodyType> state) {}

    public synchronized final void send(Reference reference, Message message) {
        this.reference.send(reference, message);
    }

    public synchronized final void link(Reference reference) {
        this.reference.link(reference);
    }

    public synchronized final void unlink(Reference reference) {
        this.reference.unlink(reference);
    }

    public synchronized final void monitor(Reference reference) {
        this.reference.monitor(reference);
    }

    public synchronized final void unmonitor(Reference reference) {
        this.reference.unmonitor(reference);
    }

    public synchronized final void exit(Reference reference) {
        this.reference.exit(reference);
    }

    public synchronized final void exited(Reference reference) {
        this.reference.exited(reference);
    }

    public synchronized void putIntoMailbox(Envelope envelope) {
        try {
            this.mailbox.put(envelope);
            this.scheduler.putIntoRunQueue(this);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void putIntoLinkedBy(Integer actorIdentifier, Reference reference) {
        this.linkedBy.put(actorIdentifier, reference);
    }

    public synchronized void putIntoMonitoredBy(Integer actorIdentifier, Reference reference) {
        this.monitoredBy.put(actorIdentifier, reference);
    }

    public synchronized void removeFromLinkedBy(Integer actorIdentifier) {
        this.linkedBy.remove(actorIdentifier);
    }

    public synchronized void removeFromMonitoredBy(Integer actorIdentifier) {
        this.monitoredBy.remove(actorIdentifier);
    }

    public final void setScheduler(Scheduler scheduler) {
        if(this.scheduler != null) return;
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

    private synchronized void internalReceive(Envelope envelope) {
        this.status = Status.RUNNING;
        Message message = envelope.getMessage();
        Reference senderReference = envelope.getFrom();

        if(message.getPriority() == 0) {
            switch(message.getType()) {
                case EXIT:
                    if(trapExit) {
                        this.state = receive(message, this.state);
                        if(!this.state.getNext()){
                            this.status = Status.EXITING;
                            this.isAlive = false;
                            return;
                        }
                    } else {
                        this.status = Status.EXITING;
                        this.isAlive = false;
                        return;
                    }
                    break;
                case EXITED:
                    this.state = receive(new Message.Builder()
                            .type(Message.Type.EXITED)
                            .from(senderReference.toString())
                            .build(), this.state);
                    if(!this.state.getNext()){
                        this.status = Status.EXITING;
                        this.isAlive = false;
                        return;
                    }
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
            this.state = receive(message, this.state);
            if(!this.state.getNext()){
                this.status = Status.EXITING;
                this.isAlive = false;
            }
        }
    }

    private synchronized void internalInit() {
        this.state = init();
    }

    private synchronized void internalLoop() {

        reduction++;

        // TODO: check if this function is tail-recursive

        this.status = Status.WAITING;
        Envelope envelope = mailbox.poll();
        if(envelope != null) {
            internalReceive(envelope);
            if (this.status == Status.RUNNING) {
                internalLoop();
            }
        }
    }

    private synchronized void internalTerminate() {
        Reference reference = new Reference(this.getIdentifier(), this.getNode().getName());
        for(Map.Entry<Integer, Reference> entry : this.monitoredBy.entrySet()) {
            reference.exited(entry.getValue());
        }

        for(Map.Entry<Integer, Reference> entry : this.linkedBy.entrySet()) {
            reference.exit(entry.getValue());
        }

        terminate(this.state);
    }

    public synchronized void run() {
        if(this.status == Status.STARTING) {
            internalInit();
            this.isAlive = true;
        }
        internalLoop();
        if(this.status == Status.EXITING) {
            internalTerminate();
            this.isAlive = false;
        }
    }

    public synchronized void stop() {
        this.status = Status.EXITING;
        this.isAlive = false;
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

    public boolean isAlive() {
        return this.isAlive;
    }

    public Reference getReference() {
        return this.reference;
    }

    public Map<Integer, Reference> listLinkedBy() {
        return this.linkedBy;
    }

    public Map<Integer, Reference> listMonitoredBy() {
        return this.monitoredBy;
    }
}
