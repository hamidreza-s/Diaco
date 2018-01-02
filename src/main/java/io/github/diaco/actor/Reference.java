package io.github.diaco.actor;

import io.github.diaco.message.Message;

public interface Reference {

    public void send(Reference reference, Message message);
    public void link(Reference reference);
    public void unlink(Reference reference);
    public void monitor(Reference reference);
    public void unmonitor(Reference reference);
    public void exit(Reference reference);
    public void exited(Reference reference);
    public int getActorIdentifier();
    public String getNodeName();
}
