package io.github.diaco.actor;

import io.github.diaco.core.Scheduler;
import io.github.diaco.message.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

abstract class AbstractActor implements Actor, Comparable<Actor> {

    public static final int DEFAULT_PRIORITY = 0;
    public static final int DEFAULT_MAILBOX_SIZE = 1024;
    private Integer priority;
    private Integer reduction;
    private Integer identifier;
    private BlockingQueue<Message> mailbox;

    protected AbstractActor() {
        this(DEFAULT_PRIORITY, DEFAULT_MAILBOX_SIZE);
    }

    protected AbstractActor(int priority, int mailboxSize) {
        this.priority = priority;
        this.reduction = 0;
        this.identifier = Scheduler.getFreeActorIdentifier();
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

    private void putIntoMailbox(Message message) {
        try {
            this.mailbox.put(message);
         } catch(InterruptedException e) {
             e.printStackTrace();
         }
    }

    private void loop() {
        // TODO: catch signals and take action upon them
        // TODO: catch EXIT signal and call terminate callback

        reduction++;

        try {
            Message message = mailbox.take();
            receive(message);
            loop();
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
