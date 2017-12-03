package io.github.diaco;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import io.github.diaco.core.Actor;
import io.github.diaco.core.Message;

public class DiacoTest extends TestCase {

    public DiacoTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(DiacoTest.class);
    }

    public void testDiaco() {
        Diaco diaco = Diaco.getInstance();

        Actor actor1 = new Actor() {
            public void init() {
                System.out.println("Actor 1 started");
            };
            public void receive(Message message) {
                System.out.printf(
                        "Actor 1 got message %s in thread %s\n",
                        message, Thread.currentThread().getName());
            }
            public void terminate() {
                System.out.println("Actor 1 was terminated");
            }
        };

        Actor actor2 = new Actor() {
            public void init() {
                System.out.println("Actor 2 started");
            }
            public void receive(Message message) {
                System.out.printf(
                        "Actor 2 got message %s in thread %s\n",
                        message, Thread.currentThread().getName());
            }
            public void terminate() {
                System.out.println("Actor 2 was terminated");
            }
        };

        diaco.spawn(actor1);
        diaco.spawn(actor2);

        Message message1 = new Message("hey");
        Message message2 = new Message("hi");

        actor1.send(actor2, message1);
        actor2.send(actor1, message2);

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
