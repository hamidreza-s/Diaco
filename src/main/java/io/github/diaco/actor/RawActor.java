package io.github.diaco.actor;

import io.github.diaco.message.Message;

import java.util.List;

public class RawActor<State> extends AbstractActor<State> {

    public void init(List<State> state) {}

    public void receive(Message message, List<State> state) {}

    public void terminate(List<State> state) {}

}
