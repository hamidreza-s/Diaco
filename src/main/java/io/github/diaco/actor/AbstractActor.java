package io.github.diaco.actor;

import io.github.diaco.core.Scheduler;
import io.github.diaco.message.Message;
import io.github.diaco.message.SignalMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

abstract class AbstractActor<State, MessageBodyType> implements Actor<State, MessageBodyType>, Comparable<Actor> {

    // TODO: add API for trapExit

    public static final int DEFAULT_PRIORITY = 0;
    public static final int DEFAULT_MAILBOX_SIZE = 1024;
    private Integer priority;
    private Integer reduction;
    private Integer identifier;
    private boolean trapExit;
    private BlockingQueue<Message> mailbox;
    private Status status;
    private List<State> state;
    private Map<Integer, Actor> linkedBy;
    private Map<Integer, Actor> monitoredBy;

    protected AbstractActor() {
        this(DEFAULT_PRIORITY, DEFAULT_MAILBOX_SIZE);
    }

    protected AbstractActor(int priority, int mailboxSize) {
        this.status = Status.STARTING;
        this.state = new ArrayList<State>();
        this.priority = priority;
        this.reduction = 0;
        this.identifier = Scheduler.getFreeActorIdentifier();
        this.trapExit = false;
        this.mailbox = new PriorityBlockingQueue<Message>(mailboxSize);
        this.linkedBy = new HashMap<Integer, Actor>();
        this.monitoredBy = new HashMap<Integer, Actor>();
    }

    public abstract void init(List<State> state);

    public abstract void receive(Message<MessageBodyType> message, List<State> state);

    public abstract void terminate(List<State> state);

    // FIXME: what if more than one actor are sending one message concurrently?
    // TODO: messages must be passed by value (copy)
    public final void send(Actor actor, Message message) {
        message.setFrom(this);
        message.setTo(actor);
        AbstractActor abstractActor = (AbstractActor) actor;
        abstractActor.putIntoMailbox(message);
    }

    public final void link(Actor actor) {
        this.linkedBy.put(actor.getIdentifier(), actor);
        this.send(actor, new SignalMessage(SignalMessage.Type.LINK));
    }

    public final void unlink(Actor actor) {
        this.linkedBy.remove(actor.getIdentifier());
        this.send(actor, new SignalMessage(SignalMessage.Type.UNLINK));
    }

    public final void monitor(Actor actor) {
        this.send(actor, new SignalMessage(SignalMessage.Type.MONITOR));
    }

    public final void unmonitor(Actor actor) {
        this.send(actor, new SignalMessage(SignalMessage.Type.UNLINK));
    }

    public final void exit(Actor actor) {
        this.send(actor, new SignalMessage(SignalMessage.Type.EXIT));
    }

    private void internalReceive(Message message) {
        this.status = Status.RUNNING;
        if(message instanceof SignalMessage) {

            switch((SignalMessage.Type) message.getBody()) {
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

    private void putIntoMailbox(Message message) {
        try {
            this.mailbox.put(message);
         } catch(InterruptedException e) {
             e.printStackTrace();
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
            this.send(entry.getValue(), new SignalMessage(SignalMessage.Type.EXITED));
        }

        for(Map.Entry<Integer, Actor> entry : this.linkedBy.entrySet()) {
            this.send(entry.getValue(), new SignalMessage(SignalMessage.Type.EXIT));
        }

        terminate(this.state);
    }

    public void run() {
        internalInit();
        internalLoop();
        internalTerminate();
    }

    public void stop() {
        this.status = status.EXITING;
    }

    public int compareTo(Actor other) {
        return this.getPriority().compareTo(other.getPriority());
    }

    public Status getStatus() {
        return this.getStatus();
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

    public Map<Integer, Actor> listLinkedBy() {
        return this.linkedBy;
    }

    public Map<Integer, Actor> listMonitoredBy() {
        return this.monitoredBy;
    }

}
