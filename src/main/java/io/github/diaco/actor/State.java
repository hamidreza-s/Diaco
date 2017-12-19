package io.github.diaco.actor;

import java.util.ArrayList;
import java.util.List;

public class State<StateBodyType> {

    private List<StateBodyType> body;

    public State() {
        this.body = new ArrayList<StateBodyType>();
    }

    public List<StateBodyType> getBody() {
        return body;
    }

    public void setBody(List<StateBodyType> body) {
        this.body = body;
    }

}
