package io.github.diaco.message;

import io.github.diaco.actor.Actor;

abstract class AbstractMessage implements Message, Comparable<Message> {

    // TODO: protect getFrom and getTo from NullPointerException

    private Actor from;
    private Actor to;
    private Object body;
    private Integer priority;

    public AbstractMessage(Object body, Integer priority) {
        this.body = body;
        if(priority == 0 && !(this instanceof SignalMessage)) {
            throw new RuntimeException("Priority 0 is reserved for SignalMessage!");
        }
        this.priority = priority;
    }

    public Object getBody() {
        return body;
    }

    public Actor getFrom() {
        return from;
    }

    public Actor getTo() {
        return to;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setFrom(Actor from) {
        this.from = from;
    }

    public void setTo(Actor to) {
        this.to = to;
    }

    public int compareTo(Message other) {
        return this.getPriority().compareTo(other.getPriority());
    }

}
