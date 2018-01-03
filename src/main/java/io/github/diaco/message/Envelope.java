package io.github.diaco.message;

import io.github.diaco.actor.Reference;
import java.io.Serializable;

public class Envelope implements Comparable<Envelope>, Serializable {

    private Reference from;
    private Reference to;
    private Message message;

    public Envelope(Reference from, Reference to, Message message) {
        this.from = from;
        this.to = to;
        this.message = message;
    }

    public Reference getFrom() {
        return this.from;
    }

    public Reference getTo() {
        return this.to;
    }

    public Message getMessage() {
        return this.message;
    }

    public int compareTo(Envelope other) {
        return this.getMessage().getPriority().compareTo(other.getMessage().getPriority());
    }
}
