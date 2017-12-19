package io.github.diaco.message;

public class RawMessage<BodyType> extends AbstractMessage<BodyType> {

    public static final Integer DEFAULT_PRIORITY = 1;

    public RawMessage(BodyType body) {
        super(body, DEFAULT_PRIORITY);
    }

}
