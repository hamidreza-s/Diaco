package io.github.diaco.message;

import io.github.diaco.actor.Actor;

public abstract class AbstractMessage implements Message {

    // TODO: define message types (signal, etc)
    // TODO: protect getFrom and getTo from NullPointerException

    private Actor from;
    private Actor to;
    private Object body;

    public AbstractMessage(Object body) {
        this.body = body;
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

    public void setFrom(Actor from) {
        this.from = from;
    }

    public void setTo(Actor to) {
        this.to = to;
    }

}
