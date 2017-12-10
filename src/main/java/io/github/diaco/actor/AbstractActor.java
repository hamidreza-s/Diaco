package io.github.diaco.actor;

import io.github.diaco.core.Scheduler;
import io.github.diaco.message.Message;
import io.github.diaco.message.SignalMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

abstract class AbstractActor implements Actor, Comparable<Actor> {

    public enum Status {
        STARTING,
        WAITING,
        RUNNING,
        EXITING
    }

    public static final int DEFAULT_PRIORITY = 0;
    public static final int DEFAULT_MAILBOX_SIZE = 1024;
    private Integer priority;
    private Integer reduction;
    private Integer identifier;
    private boolean trapExit;
    private BlockingQueue<Message> mailbox;
    private Status status;

    protected AbstractActor() {
        this(DEFAULT_PRIORITY, DEFAULT_MAILBOX_SIZE);
    }

    protected AbstractActor(int priority, int mailboxSize) {
        this.status = Status.STARTING;
        this.priority = priority;
        this.reduction = 0;
        this.identifier = Scheduler.getFreeActorIdentifier();
        this.trapExit = false;
        this.mailbox = new PriorityBlockingQueue<Message>(mailboxSize);
    }

    public abstract void init();

    public abstract void receive(Message message);

    public abstract void terminate();

    public final void send(Actor actor, Message message) {
        message.setFrom(this);
        message.setTo(actor);
        AbstractActor abstractActor = (AbstractActor) actor;
        abstractActor.putIntoMailbox(message);
    }

    private void handleMessage(Message message) {
        this.status = Status.RUNNING;
        if(message instanceof SignalMessage) {

            switch((SignalMessage.Type) message.getBody()) {
                case EXIT:
                    if(trapExit) {
                        receive(message);
                    } else {
                        this.status = Status.EXITING;
                        return;
                    }
                    break;
                case LINK:
                    // TODO: link actor
                    break;
                case MONITOR:
                    // TODO: monitor actor
                    break;
            }

        } else {
            receive(message);
        }
    }

    private void putIntoMailbox(Message message) {
        try {
            this.mailbox.put(message);
         } catch(InterruptedException e) {
             e.printStackTrace();
         }
    }

    private void loop() {

        reduction++;

        try {
            this.status = Status.WAITING;
            Message message = mailbox.take();
            handleMessage(message);
            if(this.status == Status.RUNNING) {
                loop();
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        init();
        loop();
        terminate();
    }

    public int compareTo(Actor other) {
        return this.getPriority().compareTo(other.getPriority());
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
}
