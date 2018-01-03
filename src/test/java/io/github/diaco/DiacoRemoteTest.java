package io.github.diaco;

import io.github.diaco.actor.Actor;
import io.github.diaco.actor.RawActor;
import io.github.diaco.actor.Reference;
import io.github.diaco.actor.State;
import io.github.diaco.core.Config;
import io.github.diaco.message.Message;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DiacoRemoteTest extends TestCase {

    public DiacoRemoteTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(DiacoRemoteTest.class);
    }

    public void testRemoteCommunicationWithReference() throws InterruptedException {
        Diaco diacoOne = DiacoTestHelper.getDiacoOneInstance();
        Diaco diacoTwo = DiacoTestHelper.getDiacoTwoInstance();

        final Actor<String> actorOne = new RawActor<String>() {
            @Override
            public void receive(Message message, State<String> state) {
                System.out.println("inside actor one / new message: " + message.getTag());
            }
        };

        final Actor<String> actorTwo = new RawActor<String>() {
            @Override
            public void receive(Message message, State<String> state) {
                System.out.println("inside actor two / new message: " + message.getTag());
            }
        };

        Reference actorOneRef = diacoOne.spawn(actorOne);
        Reference actorTwoRef = diacoTwo.spawn(actorTwo);

        actorOneRef.send(actorTwoRef, new Message
                .Builder()
                .tag("test-tag")
                .body(new byte[]{1, 2, 3})
                .build());
    }
}
