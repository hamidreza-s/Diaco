package io.github.diaco.actor;

import io.github.diaco.core.Registry;
import io.github.diaco.message.Message;

public class RemoteReference implements Reference {

    private int actorIdentifier;
    private String nodeName;

    public RemoteReference(Integer actorIdentifier, String nodeName) {
        this.actorIdentifier = actorIdentifier;
        this.nodeName = nodeName;
    }

    public void send(Reference reference, Message message) {
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        if(senderActor.hasNode()) {
            senderActor.getNode().send(this.getActorIdentifier(), reference, message);
        } else {
            throw new RuntimeException("diaco does not have node!");
        }
    }

    public void link(Reference reference) {
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        if(senderActor.hasNode()) {
            this.send(reference, new Message.Builder().type(Message.Type.LINK).priority(0).from(senderActor).build());
        } else {
            throw new RuntimeException("diaco does not have node!");
        }
    }

    public void unlink(Reference reference) {
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        if(senderActor.hasNode()) {
            this.send(reference, new Message.Builder().type(Message.Type.UNLINK).priority(0).from(senderActor).build());
        } else {
            throw new RuntimeException("diaco does not have node!");
        }
    }

    public void monitor(Reference reference) {
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        if(senderActor.hasNode()) {
            this.send(reference, new Message.Builder().type(Message.Type.MONITOR).priority(0).from(senderActor).build());
        } else {
            throw new RuntimeException("diaco does not have node!");
        }
    }

    public void unmonitor(Reference reference) {
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        if(senderActor.hasNode()) {
            this.send(reference, new Message.Builder().type(Message.Type.UNMONITOR).priority(0).from(senderActor).build());
        } else {
            throw new RuntimeException("diaco does not have node!");
        }
    }

    public void exit(Reference reference) {
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        if(senderActor.hasNode()) {
            this.send(reference, new Message.Builder().type(Message.Type.EXIT).priority(0).from(senderActor).build());
        } else {
            throw new RuntimeException("diaco does not have node!");
        }
    }

    public void exited(Reference reference) {
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        if(senderActor.hasNode()) {
            this.send(reference, new Message.Builder().type(Message.Type.EXITED).priority(0).from(senderActor).build());
        } else {
            throw new RuntimeException("diaco does not have node!");
        }
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public int getActorIdentifier() {
        return this.actorIdentifier;
    }

    @Override
    public String toString() {
        return "<remote." + nodeName + "." + Integer.toString(actorIdentifier) + ">";
    }

}
