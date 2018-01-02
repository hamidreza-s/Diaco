package io.github.diaco.actor;

import io.github.diaco.core.Registry;
import io.github.diaco.core.Scheduler;
import io.github.diaco.message.Message;

public class LocalReference implements Reference {

    private int actorIdentifier;
    private String nodeName;

    public LocalReference(Actor actor) {
        this.actorIdentifier = actor.getIdentifier();
        if(actor.hasNode()) {
            this.nodeName = actor.getNode().getName();
        }
        Registry.addActor(actor);
    }

    public LocalReference(int actorIdentifier, String nodeName) {
        this.actorIdentifier = actorIdentifier;
        this.nodeName = nodeName;
    }

    public final void send(Reference reference, Message message) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        senderActor.send(recipientActor, message);
    }

    public final void link(Reference reference) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        senderActor.link(recipientActor);
    }

    public final void unlink(Reference reference) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        senderActor.unlink(recipientActor);
    }

    public final void monitor(Reference reference) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        senderActor.monitor(recipientActor);
    }

    public final void unmonitor(Reference reference) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        senderActor.unmonitor(recipientActor);
    }

    public final void exit(Reference reference) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        senderActor.exit(recipientActor);
    }

    public final void exited(Reference reference) {
        Actor recipientActor = Registry.getActor(reference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        senderActor.exited(recipientActor);
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public int getActorIdentifier() {
        return this.actorIdentifier;
    }

    @Override
    public String toString() {
        return "<local." + nodeName + "." + Integer.toString(actorIdentifier) + ">";
    }

}
