package io.github.diaco.core;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class Actor implements Runnable {

    // TODO: add API for setting and getting actor options

    public Integer priority;
    public Integer reduction;
    public String id;
    public BlockingQueue<Message> mailbox;

    public Actor() {
        // TODO: define defauls based on configuration
        this(new HashMap<String, String>());
    }

    public Actor(HashMap<String, String> options) {
        // TODO: parse options for defaults
        // TODO: use incremental number for actor id
        // TODO: increment reduction based on different factors
        priority = 0;
        reduction = 0;
        id = "random-actor-id";
        mailbox = new ArrayBlockingQueue<Message>(1024);
    }

    public abstract void init();

    public abstract void receive(Message message);

    public abstract void terminate();

    public final void send(Actor actor, Message message) {
        message.setFrom(this);
        message.setTo(actor);

        try {
            actor.mailbox.put(message);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void loop() {
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

}
