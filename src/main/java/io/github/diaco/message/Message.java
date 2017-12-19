package io.github.diaco.message;

import io.github.diaco.actor.Actor;

public class Message<BodyType> implements Comparable<Message> {

    public enum HeadType {
        DEFAULT,
        EXIT,
        EXITED,
        LINK,
        UNLINK,
        MONITOR,
        UNMONITOR
    }

    private Actor from;
    private Actor to;
    private BodyType body;
    private HeadType head;
    private Integer priority;

    public Message(BodyType body) {
        this.head = HeadType.DEFAULT;
        this.body = body;
        this.priority = 1;
    }

    public Message(HeadType head, BodyType body, Integer priority) {
        this.head = head;
        this.body = body;
        this.priority = priority;
    }

    public HeadType getHead() {
        return head;
    }

    public BodyType getBody() {
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

    public void setHead(HeadType head) {
        this.head = head;
    }

    public void setBody(BodyType body) {
        this.body = body;
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