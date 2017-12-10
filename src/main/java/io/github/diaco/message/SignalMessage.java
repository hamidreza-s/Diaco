package io.github.diaco.message;

public class SignalMessage extends AbstractMessage {

    public enum Type {
        EXIT,
        LINK,
        MONITOR
    }

    public static final Integer DEFAULT_PRIORITY = 0;

    public SignalMessage(Type type) {
        super(type, DEFAULT_PRIORITY);
    }

}
