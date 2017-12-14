package io.github.diaco.actor;

import io.github.diaco.message.Message;

import java.util.List;

public class RawActor<State, MessageBodyType> extends AbstractActor<State, MessageBodyType> {

    public void init(List<State> state) {}

    public void receive(Message<MessageBodyType> message, List<State> state) {}

    public void terminate(List<State> state) {}

}
