package io.github.diaco;

import io.github.diaco.core.Config;
import io.github.diaco.actor.Actor;
import io.github.diaco.actor.RawActor;
import io.github.diaco.actor.Reference;
import io.github.diaco.actor.State;
import io.github.diaco.message.Message;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.junit.*;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DiacoSchedulerTest {

    @BeforeClass
    public static void beforeSuite() throws InterruptedException {
    }

    @Test
    public void testMessagePassingOrder() throws InterruptedException {
        Config config = Config.newConfig();
        Diaco diaco = Diaco.newInstance(config);
        final CountDownLatch lock = new CountDownLatch(8);

        Actor<String> actorOne = new RawActor<String>() {
            public void receive(Message message, State<String> state) {
                assertEquals("actor:two->actor:one", message.getTag());
                lock.countDown();
            }
        };

        Actor<String> actorTwo = new RawActor<String>() {
            public void receive(Message message, State<String> state) {
                assertEquals("actor:one->actor:two", message.getTag());
                lock.countDown();
            }
        };

        Reference actorOneRef = diaco.spawn(actorOne);
        Reference actorTwoRef = diaco.spawn(actorTwo);

        actorOneRef.send(actorTwoRef, new Message.Builder().tag("actor:one->actor:two").build());
        actorTwoRef.send(actorOneRef, new Message.Builder().tag("actor:two->actor:one").build());

        actorOneRef.send(actorTwoRef, new Message.Builder().tag("actor:one->actor:two").build());
        actorTwoRef.send(actorOneRef, new Message.Builder().tag("actor:two->actor:one").build());

        actorOneRef.send(actorTwoRef, new Message.Builder().tag("actor:one->actor:two").build());
        actorTwoRef.send(actorOneRef, new Message.Builder().tag("actor:two->actor:one").build());

        actorOneRef.send(actorTwoRef, new Message.Builder().tag("actor:one->actor:two").build());
        actorTwoRef.send(actorOneRef, new Message.Builder().tag("actor:two->actor:one").build());


        lock.await();
    }
}