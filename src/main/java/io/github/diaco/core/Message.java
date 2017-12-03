package io.github.diaco.core;

public class Message {

    // TODO: define message types (signal, etc)
    // TODO: protect getFrom and getTo from NullPointerException

    private Actor from;
    private Actor to;
    private Object body;

    public Message(Object body) {
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
