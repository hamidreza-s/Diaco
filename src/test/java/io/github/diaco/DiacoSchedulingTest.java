package io.github.diaco;

import io.github.diaco.actor.Actor;
import io.github.diaco.actor.BaseActor;
import io.github.diaco.actor.Reference;
import io.github.diaco.actor.State;
import io.github.diaco.message.Message;

import java.util.concurrent.CountDownLatch;
import org.junit.*;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DiacoSchedulingTest {

    private static Diaco diaco;

    @BeforeClass
    public static void beforeSuite() throws InterruptedException {
        diaco = DiacoTestHelper.getDiacoZeroInstance();
    }

    @Test
    public void testConcurrentMessagePassing() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(6);

        Actor<String> actorOne = new BaseActor<String>() {
            @Override
            public State<String> receive(Message message, State<String> state) {
                assertEquals("ActorTwo->ActorOne", message.getTag());
                lock.countDown();
                return state;
            }
        };

        Actor<String> actorTwo = new BaseActor<String>() {
            @Override
            public State<String> receive(Message message, State<String> state) {
                assertEquals("ActorOne->ActorTwo", message.getTag());
                lock.countDown();
                return state;
            }
        };

        Actor<String> actorThree = new BaseActor<String>() {
            @Override
            public State<String> receive(Message message, State<String> state) {
                assertEquals("ActorFour->ActorThree", message.getTag());
                lock.countDown();
                return state;
            }
        };

        Actor<String> actorFour = new BaseActor<String>() {
            @Override
            public State<String> receive(Message message, State<String> state) {
                assertEquals("ActorThree->ActorFour", message.getTag());
                lock.countDown();
                return state;
            }
        };

        Actor<String> actorFive = new BaseActor<String>() {
            @Override
            public State<String> receive(Message message, State<String> state) {
                assertEquals("ActorSix->ActorFive", message.getTag());
                lock.countDown();
                return state;
            }
        };

        Actor<String> actorSix = new BaseActor<String>() {
            @Override
            public State<String> receive(Message message, State<String> state) {
                assertEquals("ActorFive->ActorSix", message.getTag());
                lock.countDown();
                return state;
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

    @Test
    public void testActorSpawning() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(1);
        final Reference actorSpawningRef = diaco.spawn(new BaseActor<Object>() {
            @Override
            public State<Object> init() {
                lock.countDown();
                return new State<Object>();
            }
        });

        lock.await();
    }

    @Test
    public void testActorExiting() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(1);
        final Reference actorExitingRef = diaco.spawn(new BaseActor<Object>() {
            @Override
            public void terminate(State<Object> state) {
                lock.countDown();
            }
        });

        diaco.spawn(new BaseActor<Object>() {
            @Override
            public State<Object> init() {
                exit(actorExitingRef);
                return new State<Object>();
            }
        });

        lock.await();
    }

    @Test
    public void testActorSpawningDepth() throws InterruptedException {
        final CountDownLatch outerLock = new CountDownLatch(1);

        diaco.spawn(new BaseActor<Object>() {
            @Override
            public State<Object> init() {

                final CountDownLatch innerLock = new CountDownLatch(1);

                final Reference receivingActorRef = diaco.spawn(new BaseActor<Object>() {
                    @Override
                    public State<Object> receive(Message message, State<Object> state) {
                        innerLock.countDown();
                        return state;
                    }
                });

                diaco.spawn(new BaseActor<Object>() {
                    @Override
                    public State<Object> init() {
                        send(receivingActorRef, new Message.Builder().build());
                        return new State<Object>();
                    }
                });

                try { innerLock.await(); } catch(InterruptedException e) { e.printStackTrace(); }

                outerLock.countDown();
                return new State<Object>();
            }
        });

        outerLock.await();
    }

    @Test
    public void testCheckNullActors() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(1);
        final Reference uninitializedActor = (Reference) null;

        diaco.spawn(new BaseActor<Object>() {
            @Override
            public State<Object> init() {
                send(uninitializedActor, new Message.Builder().build());
                link(uninitializedActor);
                unlink(uninitializedActor);
                monitor(uninitializedActor);
                unmonitor(uninitializedActor);
                exit(uninitializedActor);
                exited(uninitializedActor);
                lock.countDown();
                return new State<Object>();
            }
        });

        lock.await();
    }
}