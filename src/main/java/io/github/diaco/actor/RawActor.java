package io.github.diaco.actor;

import io.github.diaco.message.Message;
import io.github.diaco.actor.State;


public class RawActor<StateBodyType, MessageBodyType> extends AbstractActor<StateBodyType, MessageBodyType> {

    public void init(State<StateBodyType> state) {}

    public void receive(Message<MessageBodyType> message, State<StateBodyType> state) {}

    public void terminate(State<StateBodyType> state) {}

}
