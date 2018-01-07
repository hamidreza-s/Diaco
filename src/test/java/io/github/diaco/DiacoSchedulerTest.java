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
    public void testSimpleMessagePassing() throws InterruptedException {
        Diaco diaco = DiacoTestHelper.getDiacoZeroInstance();
        final CountDownLatch lock = new CountDownLatch(6);

        Actor<String> actorOne = new RawActor<String>() {
            public void receive(Message message, State<String> state) {
                assertEquals("ActorTwo->ActorOne", message.getTag());
                lock.countDown();
            }
        };

        Actor<String> actorTwo = new RawActor<String>() {
            public void receive(Message message, State<String> state) {
                assertEquals("ActorOne->ActorTwo", message.getTag());
                lock.countDown();
            }
        };

        Actor<String> actorThree = new RawActor<String>() {
            public void receive(Message message, State<String> state) {
                assertEquals("ActorFour->ActorThree", message.getTag());
                lock.countDown();
            }
        };

        Actor<String> actorFour = new RawActor<String>() {
            public void receive(Message message, State<String> state) {
                assertEquals("ActorThree->ActorFour", message.getTag());
                lock.countDown();
            }
        };

        Actor<String> actorFive = new RawActor<String>() {
            public void receive(Message message, State<String> state) {
                assertEquals("ActorSix->ActorFive", message.getTag());
                lock.countDown();
            }
        };

        Actor<String> actorSix = new RawActor<String>() {
            public void receive(Message message, State<String> state) {
                assertEquals("ActorFive->ActorSix", message.getTag());
                lock.countDown();
            }
        };

        Reference actorOneRef = diaco.spawn(actorOne).setActorName("ActorOne");
        Reference actorTwoRef = diaco.spawn(actorTwo).setActorName("ActorTwo");
        Reference actorThreeRef = diaco.spawn(actorThree).setActorName("ActorThree");
        Reference actorFourRef = diaco.spawn(actorFour).setActorName("ActorFour");
        Reference actorFiveRef = diaco.spawn(actorFive).setActorName("ActorFive");
        Reference actorSixRef = diaco.spawn(actorSix).setActorName("ActorSix");

        actorOneRef.send(actorTwoRef, new Message.Builder().tag("ActorOne->ActorTwo").build());
        actorTwoRef.send(actorOneRef, new Message.Builder().tag("ActorTwo->ActorOne").build());
        actorThreeRef.send(actorFourRef, new Message.Builder().tag("ActorThree->ActorFour").build());
        actorFourRef.send(actorThreeRef, new Message.Builder().tag("ActorFour->ActorThree").build());
        actorFiveRef.send(actorSixRef, new Message.Builder().tag("ActorFive->ActorSix").build());
        actorSixRef.send(actorFiveRef, new Message.Builder().tag("ActorSix->ActorFive").build());

        lock.await();
    }
}