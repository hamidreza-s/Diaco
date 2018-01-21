package io.github.diaco.actor;

import java.util.ArrayList;
import java.util.List;

public class State<StateBodyType> {

    // TODO: use algebraic type (composite type) for state

    private StateBodyType body;
    private boolean next;

    public State() {
        this.next = true;
        this.body = null;
    }

    public State(StateBodyType body) {
        this.next = true;
        this.body = body;
    }

    public State(StateBodyType body, boolean next) {
        this.next = next;
        this.body = body;
    }

    public StateBodyType getBody() {
        return body;
    }

    public boolean getNext() {
        return next;
    }
}
