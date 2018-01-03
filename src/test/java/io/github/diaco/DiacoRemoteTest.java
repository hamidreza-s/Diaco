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
import java.util.concurrent.CountDownLatch;

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

        final CountDownLatch lock = new CountDownLatch(2);

        final Actor<String> actorOne = new RawActor<String>() {
            @Override
            public void receive(Message message, State<String> state) {
                assertEquals("actor:two->actor:one", message.getTag());

                byte[] expectedBody = new byte[]{4, 5, 6};
                byte[] receivedBody = message.getBody();
                for(int i = 0; i < receivedBody.length; i++)
                    assertEquals(receivedBody[i], expectedBody[i]);
                lock.countDown();
            }
        };

        final Actor<String> actorTwo = new RawActor<String>() {
            @Override
            public void receive(Message message, State<String> state) {
                assertEquals("actor:one->actor:two", message.getTag());
                byte[] expectedBody = new byte[]{1, 2, 3};
                byte[] receivedBody = message.getBody();
                for(int i = 0; i < receivedBody.length; i++)
                    assertEquals(receivedBody[i], expectedBody[i]);
                lock.countDown();
            }
        };

        Reference actorOneRef = diacoOne.spawn(actorOne);
        Reference actorTwoRef = diacoTwo.spawn(actorTwo);

        actorOneRef.send(actorTwoRef,
                new Message.Builder()
                .tag("actor:one->actor:two")
                .body(new byte[]{1, 2, 3})
                .build());

        actorTwoRef.send(actorOneRef,
                new Message.Builder()
                .tag("actor:two->actor:one")
                .body(new byte[]{4, 5, 6})
                .build());

        lock.await();
    }
}
