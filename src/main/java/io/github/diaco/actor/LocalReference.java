package io.github.diaco.actor;

import io.github.diaco.message.Message;

import java.sql.Ref;
import java.util.HashMap;
import java.util.Map;

public class LocalReference implements Reference {

    private static Map<Integer, Actor> actorsMap = new HashMap<Integer, Actor>();
    private int actorIdentifier;
    private String nodeName;

    public LocalReference(Actor actor) {
        this.actorIdentifier = actor.getIdentifier();
        if(actor.hasNode()) {
            this.nodeName = actor.getNode().getName();
        }
        actorsMap.put(actorIdentifier, actor);
    }

    public void send(Reference reference, Message message) {
        Actor recipientActor = actorsMap.get(reference.getActorIdentifier());
        Actor senderActor = actorsMap.get(this.getActorIdentifier());
        senderActor.send(recipientActor, message);
    }

    public final void link(Reference reference) {
        Actor recipientActor = actorsMap.get(reference.getActorIdentifier());
        Actor senderActor = actorsMap.get(this.getActorIdentifier());
        senderActor.link(recipientActor);
    }

    public final void unlink(Reference reference) {
        Actor recipientActor = actorsMap.get(reference.getActorIdentifier());
        Actor senderActor = actorsMap.get(this.getActorIdentifier());
        senderActor.unlink(recipientActor);
    }

    public final void monitor(Reference reference) {
        Actor recipientActor = actorsMap.get(reference.getActorIdentifier());
        Actor senderActor = actorsMap.get(this.getActorIdentifier());
        senderActor.monitor(recipientActor);
    }

    public final void unmonitor(Reference reference) {
        Actor recipientActor = actorsMap.get(reference.getActorIdentifier());
        Actor senderActor = actorsMap.get(this.getActorIdentifier());
        senderActor.unmonitor(recipientActor);
    }

    public final void exit(Reference reference) {
        Actor recipientActor = actorsMap.get(reference.getActorIdentifier());
        Actor senderActor = actorsMap.get(this.getActorIdentifier());
        senderActor.exit(recipientActor);
    }

    public final void exited(Reference reference) {
        Actor recipientActor = actorsMap.get(reference.getActorIdentifier());
        Actor senderActor = actorsMap.get(this.getActorIdentifier());
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
