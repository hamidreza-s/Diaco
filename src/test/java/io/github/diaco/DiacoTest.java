package io.github.diaco;

import io.github.diaco.actor.Actor;
import io.github.diaco.actor.RawActor;
import io.github.diaco.message.Message;
import io.github.diaco.message.DataMessage;

import io.github.diaco.message.SignalMessage;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DiacoTest extends TestCase {

    public DiacoTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(DiacoTest.class);
    }

    public void testDiaco() {
        Diaco diaco = Diaco.getInstance();

        Actor actor1 = new RawActor() {
            public void init() {
                System.out.println("Actor 1 started");
            };
            public void receive(Message message) {
                System.out.printf(
                        "Actor 1 got message %s in thread %s\n",
                        message.getBody(), Thread.currentThread().getName());
            }
            public void terminate() {
                System.out.println("Actor 1 was terminated");
            }
        };

        Actor actor2 = new RawActor() {
            public void init() {
                System.out.println("Actor 2 started");
            }
            public void receive(Message message) {
                System.out.printf(
                        "Actor 2 got message %s in thread %s\n",
                        message.getBody(), Thread.currentThread().getName());
            }
            public void terminate() {
                System.out.println("Actor 2 was terminated");
            }
        };

        diaco.spawn(actor1);
        diaco.spawn(actor2);

        Message message1 = new DataMessage<String>("hey");
        Message message2 = new DataMessage<String>("hi");

        actor1.send(actor2, message1);
        actor2.send(actor1, message2);

        System.out.printf("Actor 1: priority %d, reduction %d, identifier %d\n",
                actor1.getPriority(), actor1.getReduction(), actor1.getIdentifier());

        System.out.printf("Actor 2: priority %d, reduction %d, identifier %d\n",
                actor2.getPriority(), actor2.getReduction(), actor2.getIdentifier());

        Message message3 = new SignalMessage(SignalMessage.Type.EXIT);
        actor1.send(actor2, message3);

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
