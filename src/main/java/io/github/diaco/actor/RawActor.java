package io.github.diaco.actor;

import io.github.diaco.message.Message;
import org.apache.zookeeper.data.Stat;

public class RawActor<StateBodyType> extends AbstractActor<StateBodyType> {

    public State<StateBodyType> init() {
        return new State<StateBodyType>();
    }

    public State<StateBodyType> receive(Message message, State<StateBodyType> state) {
        return state;
    }

    public void terminate(State<StateBodyType> state) {}

}
