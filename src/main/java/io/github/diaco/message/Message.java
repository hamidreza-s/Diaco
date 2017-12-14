package io.github.diaco.message;

import io.github.diaco.actor.Actor;

public interface Message<BodyType> {

    public BodyType getBody();
    public Actor getFrom();
    public Actor getTo();
    public Integer getPriority();
    public void setFrom(Actor from);
    public void setTo(Actor to);

}
