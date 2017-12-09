package io.github.diaco.actor;

import io.github.diaco.core.Scheduler;
import io.github.diaco.message.Message;

import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

abstract class AbstractActor implements Actor, Comparable<Actor> {

    // TODO: add API for setting and getting actor options

    private Integer priority;
    private Integer reduction;
    private Integer identifier;
    private BlockingQueue<Message> mailbox;

    protected AbstractActor() {
        // TODO: define default based on configuration
        this(new HashMap<String, String>());
    }

    protected AbstractActor(HashMap<String, String> options) {
        // TODO: parse options for defaults
        // TODO: increment reduction based on different factors
        priority = 0;
        reduction = 0;
        identifier = Scheduler.getFreeActorIdentifier();
        mailbox = new ArrayBlockingQueue<Message>(1024);
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
        // TODO: take messages based on priority
        // TODO: catch signals and take action upon them
        // TODO: catch EXIT signal and call terminate callback
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
