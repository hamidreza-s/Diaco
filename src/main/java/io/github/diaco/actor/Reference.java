package io.github.diaco.actor;

import io.github.diaco.core.Registry;
import io.github.diaco.core.Scheduler;
import io.github.diaco.message.Message;

public class Reference {

    // TODO: put message into envelope

    private int actorIdentifier;
    private Object actorFuture; // TODO: implement it!
    private String nodeName;

    public Reference(Actor actor) {
        this.actorIdentifier = actor.getIdentifier();
        this.nodeName = actor.getNode().getName();
        Registry.addActor(actor);
    }

    public Reference(int actorIdentifier, String nodeName) {
        this.actorIdentifier = actorIdentifier;
        this.nodeName = nodeName;
    }

    public final void send(Actor recipientActor, Message message) {
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        if(senderActor.getNode().equals(recipientActor.getNode())) {
            recipientActor.putIntoMailbox(message);
        } else {
            // TODO: send to remote node
        }
    }

    public final void send(Reference reference, Message message) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        this.send(recipientActor, message);
    }

    public final void link(Reference reference) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        senderActor.putIntoLinkedBy(recipientActor.getIdentifier(), recipientActor);
        this.send(recipientActor, new Message
                .Builder()
                .type(Message.Type.LINK)
                .priority(0)
                .from(senderActor)
                .build());
    }

    public final void unlink(Reference reference) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        this.send(recipientActor, new Message
                .Builder()
                .type(Message.Type.UNLINK)
                .priority(0)
                .from(senderActor)
                .build());
    }

    public final void monitor(Reference reference) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        this.send(recipientActor, new Message
                .Builder()
                .type(Message.Type.MONITOR)
                .priority(0)
                .from(senderActor)
                .build());
    }

    public final void unmonitor(Reference reference) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        this.send(recipientActor, new Message
                .Builder()
                .type(Message.Type.UNMONITOR)
                .priority(0)
                .from(senderActor)
                .build());
    }

    public final void exit(Reference reference) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        this.send(recipientActor, new Message
                .Builder()
                .type(Message.Type.EXIT)
                .priority(0)
                .from(senderActor)
                .build());
    }

    public final void exited(Reference reference) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        this.send(recipientActor, new Message
                .Builder()
                .type(Message.Type.EXITED)
                .priority(0)
                .from(senderActor)
                .build());
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public int getActorIdentifier() {
        return this.actorIdentifier;
    }

    @Override
    public String toString() {
        return "<" + nodeName + "." + Integer.toString(actorIdentifier) + ">";
    }

}
