package io.github.diaco.actor;

import io.github.diaco.message.Message;

public class RawActor<StateBodyType> extends AbstractActor<StateBodyType> {

    public void init(State<StateBodyType> state) {}

    public void receive(Message message, State<StateBodyType> state) {}

    public void terminate(State<StateBodyType> state) {}

}
