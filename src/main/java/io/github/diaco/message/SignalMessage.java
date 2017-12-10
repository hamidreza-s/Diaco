package io.github.diaco.message;

public class SignalMessage extends AbstractMessage {

    public static final Integer DEFAULT_PRIORITY = 0;
    public enum Type {
        EXIT,
        LINK,
        MONITOR
    }

    public SignalMessage(Type type) {
        super(type, DEFAULT_PRIORITY);
    }

}
