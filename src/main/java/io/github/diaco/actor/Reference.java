package io.github.diaco.actor;

import io.github.diaco.core.Registry;
import io.github.diaco.message.Envelope;
import io.github.diaco.message.Message;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

    public Reference(int actorIdentifier, String actorName, String nodeName) {
        this.actorIdentifier = actorIdentifier;
        this.actorName = actorName;
        this.nodeName = nodeName;
    }

    private void send(Actor recipientActor, Reference recipientReference, Message message) {
        Envelope envelope = new Envelope(this, recipientReference, message);
        Actor senderActor = Registry.getActor(this.getActorIdentifier());

        // NOTE:
        // It happens when a recipient actor is not initialized yet
        // due to race condition between worker threads.
        // Also, the use of isValid method checks the reference as well.
        // The sender must check availability of recipient actor before
        // sending a message.
        if(recipientActor == null)
            return;

        if (senderActor.getNode().equals(recipientActor.getNode())) {
            if(!recipientActor.isAlive() || recipientActor.getStatus() == Actor.Status.EXITING) {
                return;
            } else {
                recipientActor.putIntoMailbox(envelope);
            }
        } else {
            senderActor.getNode().send(envelope);
        }
    }

    public final void send(Reference recipientReference, Message message) {
        if(!this.isValid(recipientReference)) return;
        Actor recipientActor = Registry.getActor(recipientReference.getActorIdentifier());
        this.send(recipientActor, recipientReference, message);
    }

    public final void link(Reference recipientReference) {
        if(!this.isValid(recipientReference)) return;
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
        if(!this.isValid(recipientReference)) return;
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
        if(!this.isValid(recipientReference)) return;
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
        if(!this.isValid(recipientReference)) return;
        Actor recipientActor = Registry.getActor(recipientReference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        this.send(recipientActor, recipientReference,
                new Message
                .Builder()
                .type(Message.Type.UNMONITOR)
                .priority(0)
                .build());
    }

    private boolean isValid(Reference reference) {
        if(reference == null) {
            return false;
        }

        return true;
    }

    public final void exit(Reference recipientReference) {
        if(!this.isValid(recipientReference)) return;
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
        if(!this.isValid(recipientReference)) return;
        Actor recipientActor = Registry.getActor(recipientReference.getActorIdentifier());
        Actor senderActor = Registry.getActor(this.getActorIdentifier());
        this.send(recipientActor, recipientReference,
                new Message
                .Builder()
                .type(Message.Type.EXITED)
                .priority(0)
                .build());
    }

    public Actor getActor() {
        return Registry.getActor(this.getActorIdentifier());
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public int getActorIdentifier() {
        return this.actorIdentifier;
    }

    public boolean isAlive() {
        return Registry.getActor(this.getActorIdentifier()).isAlive();
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

    public static Reference fromString(String referenceString) {
        Pattern pattern = Pattern.compile("<(.*)\\.(.*)\\.(\\d*)>");
        Matcher matcher = pattern.matcher(referenceString);
        if(matcher.find() && matcher.groupCount() == 3) {
            String nodeName = matcher.group(1);
            String actorName = matcher.group(2);
            Integer actorIdentifier = Integer.parseInt(matcher.group(3));
            return new Reference(actorIdentifier, actorName, nodeName);
        }
        throw new RuntimeException("bad formatted reference!");
    }

}