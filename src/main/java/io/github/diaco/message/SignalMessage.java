package io.github.diaco.message;

public class SignalMessage extends AbstractMessage<SignalMessage.Type> {

    public enum Type {
        EXIT,
        EXITED,
        LINK,
        UNLINK,
        MONITOR,
        UNMONITOR
    }

    public static final Integer DEFAULT_PRIORITY = 0;

    public SignalMessage(Type type) {
        super(type, DEFAULT_PRIORITY);
    }

}
