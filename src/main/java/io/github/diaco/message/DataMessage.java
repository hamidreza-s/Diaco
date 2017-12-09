package io.github.diaco.message;

public class DataMessage<BodyType> extends AbstractMessage {

    public static final int DEFAULT_PRIORITY = 1;

    public DataMessage(BodyType body) {
        super(body, DEFAULT_PRIORITY);
    }

}
