package io.github.diaco.actor;

import io.github.diaco.message.Message;

import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public interface Actor extends Runnable {

    public void init();
    public void receive(Message message);
    public void send(Actor actor, Message message);
    public void terminate();

    public Integer getPriority();
    public Integer getReduction();
    public Integer getIdentifier();

}
