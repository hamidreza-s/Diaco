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

    public static final int DEFAULT_PRIORITY = 0;
    public static final int DEFAULT_MAILBOX_SIZE = 1024;
    private Node node;
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
        this.identifier = Scheduler.getFreeActorIdentifier();
        this.trapExit = false;
        this.mailbox = new PriorityBlockingQueue<Message>(mailboxSize);
        this.linkedBy = new HashMap<Integer, Actor>();
        this.monitoredBy = new HashMap<Integer, Actor>();
    }

    public abstract void init(State<StateBodyType> state);

    public abstract void receive(Message message, State<StateBodyType> state);

    public abstract void terminate(State<StateBodyType> state);

    public final void send(Actor actor, Message message) {
        if(actor.getNode().equals(this.getNode())) {
            AbstractActor abstractActor = (AbstractActor) actor;
            abstractActor.putIntoMailbox(message);
        } else {
            // TODO: remote actor!
            actor.getNode().send(message);
        }
    }

    private void putIntoMailbox(Message message) {
        try {
            this.mailbox.put(message);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public final void node(Node node) {
        this.node = node;
    }

    public final void link(Actor actor) {
        this.linkedBy.put(actor.getIdentifier(), actor);
        this.send(actor, new Message.Builder().type(Message.Type.LINK).priority(0).from(this).build());
    }

    public final void unlink(Actor actor) {
        this.linkedBy.remove(actor.getIdentifier());
        this.send(actor, new Message.Builder().type(Message.Type.UNLINK).priority(0).from(this).build());
    }

    public final void monitor(Actor actor) {
        this.send(actor, new Message.Builder().type(Message.Type.MONITOR).priority(0).from(this).build());
    }

    public final void unmonitor(Actor actor) {
        this.send(actor, new Message.Builder().type(Message.Type.UNMONITOR).priority(0).from(this).build());
    }

    public final void exit(Actor actor) {
        this.send(actor, new Message.Builder().type(Message.Type.EXIT).priority(0).from(this).build());
    }

    public final void exited(Actor actor) {
        this.send(actor, new Message.Builder().type(Message.Type.EXITED).priority(0).from(this).build());
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
                    this.linkedBy.put(message.getFrom().getIdentifier(), message.getFrom());
                    break;
                case UNLINK:
                    this.linkedBy.remove(message.getFrom().getIdentifier());
                    break;
                case MONITOR:
                    this.monitoredBy.put(message.getFrom().getIdentifier(), message.getFrom());
                    break;
                case UNMONITOR:
                    this.monitoredBy.remove(message.getFrom().getIdentifier());
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
        for(Map.Entry<Integer, Actor> entry : this.monitoredBy.entrySet()) {
            this.exited(entry.getValue());
        }

        for(Map.Entry<Integer, Actor> entry : this.linkedBy.entrySet()) {
            this.exit(entry.getValue());
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
