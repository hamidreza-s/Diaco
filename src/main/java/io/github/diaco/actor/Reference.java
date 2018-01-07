package io.github.diaco.actor;

import io.github.diaco.core.Registry;
import io.github.diaco.message.Envelope;
import io.github.diaco.message.Message;
import java.io.Serializable;

public class Reference implements Serializable {

    // TODO: put message into envelope
    // TODO: put actor's Future here

    private int actorIdentifier;
    private String actorName;
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

    private void send(Actor recipientActor, Reference recipientReference, Message message) {
        Envelope envelope = new Envelope(this, recipientReference, message);
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        if(senderActor.getNode().equals(recipientActor.getNode())) {
            recipientActor.putIntoMailbox(envelope);
        } else {
            senderActor.getNode().send(envelope);
        }
    }

    public final void send(Reference recipientReference, Message message) {
        Actor recipientActor = Registry.getActor(recipientReference.getActorIdentifier());
        this.send(recipientActor, recipientReference, message);
    }

    public final void link(Reference recipientReference) {
        Actor recipientActor = Registry.getActor(recipientReference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        senderActor.putIntoLinkedBy(recipientActor.getIdentifier(), recipientReference);
        this.send(recipientActor, recipientReference,
                new Message
                .Builder()
                .type(Message.Type.LINK)
                .priority(0)
                .build());
    }

    public final void unlink(Reference recipientReference) {
        Actor recipientActor = Registry.getActor(recipientReference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        senderActor.removeFromLinkedBy(recipientReference.getActorIdentifier());
        this.send(recipientActor, recipientReference,
                new Message
                .Builder()
                .type(Message.Type.UNLINK)
                .priority(0)
                .build());
    }

    public final void monitor(Reference recipientReference) {
        Actor recipientActor = Registry.getActor(recipientReference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        this.send(recipientActor, recipientReference,
                new Message
                .Builder()
                .type(Message.Type.MONITOR)
                .priority(0)
                .build());
    }

    public final void unmonitor(Reference recipientReference) {
        Actor recipientActor = Registry.getActor(recipientReference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        this.send(recipientActor, recipientReference,
                new Message
                .Builder()
                .type(Message.Type.UNMONITOR)
                .priority(0)
                .build());
    }

    public final void exit(Reference recipientReference) {
        Actor recipientActor = Registry.getActor(recipientReference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        this.send(recipientActor, recipientReference,
                new Message
                .Builder()
                .type(Message.Type.EXIT)
                .priority(0)
                .build());
    }

    public final void exited(Reference recipientReference) {
        Actor recipientActor = Registry.getActor(recipientReference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        this.send(recipientActor, recipientReference,
                new Message
                .Builder()
                .type(Message.Type.EXITED)
                .priority(0)
                .build());
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public int getActorIdentifier() {
        return this.actorIdentifier;
    }

    public Reference setActorName(String name) {
        this.actorName = name;
        return this;
    }

    @Override
    public String toString() {
        String actorName = (this.actorName == null) ? "no-name" : this.actorName;
        return "<" + nodeName + "." + actorName + "." + Integer.toString(actorIdentifier) + ">";

    }

}