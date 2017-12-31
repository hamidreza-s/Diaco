package io.github.diaco.message;

import io.github.diaco.actor.Actor;
import java.io.IOException;

public class Message implements Comparable<Message> {

    public enum Type {
        DEFAULT,
        EXIT,
        EXITED,
        LINK,
        UNLINK,
        MONITOR,
        UNMONITOR
    }

    public static final int DEFAULT_PRIORITY = 1;

    private final Actor from;
    private final Type type;
    private final String tag;
    private final int flag;
    private final int priority;
    private final byte[] body;

    private Message(Builder builder) {
        this.from = builder.from;
        this.type = builder.type;
        this.tag = builder.tag;
        this.flag = builder.flag;
        this.priority = builder.priority;
        this.body = builder.body;
    }

    public Type getType() {
        return type;
    }

    public String getTag() {
        return tag;
    }

    public int getFlag() {
        return flag;
    }

    public Integer getPriority() {
        return priority;
    }

    public byte[] getBody() {
        return body;
    }

    public Actor getFrom() {
        return from;
    }

    public int compareTo(Message other) {
        return this.getPriority().compareTo(other.getPriority());
    }

    public static class Builder {

        private Actor from;
        private Type type;
        private String tag;
        private int flag;
        private int priority;
        private byte[] body;

        public Builder() {
            this.type = Type.DEFAULT;
            this.priority = DEFAULT_PRIORITY;
        }

        public Builder from(Actor from) {
            this.from = from;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder flag(int flag) {
            this.flag = flag;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public Message build() {
            return new Message(this);
        }
    }
}