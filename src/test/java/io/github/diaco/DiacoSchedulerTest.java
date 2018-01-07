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
        config.setProperty(Config.SCHEDULER_THREAD_POOL_SIZE, "4");
        Diaco diaco = Diaco.newInstance(config);
        final CountDownLatch lock = new CountDownLatch(4);

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

        Actor<String> actorThree = new RawActor<String>() {
            public void receive(Message message, State<String> state) {
                assertEquals("actor:four->actor:three", message.getTag());
                lock.countDown();
            }
        };

        Actor<String> actorFour = new RawActor<String>() {
            public void receive(Message message, State<String> state) {
                assertEquals("actor:three->actor:four", message.getTag());
                lock.countDown();
            }
        };

        Reference actorOneRef = diaco.spawn(actorOne).setActorName("actor:one");
        Reference actorTwoRef = diaco.spawn(actorTwo).setActorName("actor:two");
        Reference actorThreeRef = diaco.spawn(actorThree).setActorName("actor:three");
        Reference actorFourRef = diaco.spawn(actorFour).setActorName("actor:four");

        actorOneRef.send(actorTwoRef, new Message.Builder().tag("actor:one->actor:two").build());
        actorTwoRef.send(actorOneRef, new Message.Builder().tag("actor:two->actor:one").build());
        actorThreeRef.send(actorFourRef, new Message.Builder().tag("actor:three->actor:four").build());
        actorFourRef.send(actorThreeRef, new Message.Builder().tag("actor:four->actor:three").build());

        lock.await();
    }
}