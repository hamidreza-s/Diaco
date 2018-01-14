package io.github.diaco;

import io.github.diaco.actor.Actor;
import io.github.diaco.actor.RawActor;
import io.github.diaco.actor.Reference;
import io.github.diaco.actor.State;
import io.github.diaco.message.Message;
import java.util.concurrent.CountDownLatch;
import org.junit.*;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DiacoSchedulerTest {

    private static Diaco diaco;

    @BeforeClass
    public static void beforeSuite() throws InterruptedException {
        diaco = DiacoTestHelper.getDiacoZeroInstance();
    }

    @Test
    public void testEchoMessagePassing() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(1);
        Actor<Object> actorEchoing = new RawActor<Object>() {
            public void receive(Message message, State<Object> state) {
                Reference senderActor = Reference.fromString(message.getFrom());
                String senderTag = message.getTag();
                send(senderActor, new Message.Builder().tag(senderTag).build());
            }
        };

        final Reference actorEchoingRef = diaco.spawn(actorEchoing);

        diaco.spawn(new RawActor<Object>() {
            private String echo = "foo";
            public void init(State<Object> state) {
                send(actorEchoingRef, new Message.Builder().tag(echo).from(this.getReference().toString()).build());
            }
            public void receive(Message message, State<Object> state) {
                assertEquals(message.getTag(), echo);
                lock.countDown();
            }
        });

        lock.await();
    }

    @Test
    public void testMassiveMessagePassing() throws InterruptedException {
        final Integer messageNumber = 1000;
        final CountDownLatch lockOne = new CountDownLatch(messageNumber);
        final CountDownLatch lockTwo = new CountDownLatch(messageNumber);

        Actor<Object> actorOne = new RawActor<Object>() {
            @Override
            public void receive(Message message, State<Object> state) {
                lockOne.countDown();
            }
        };

        Actor<Object> actorTwo = new RawActor<Object>() {
            @Override
            public void receive(Message message, State<Object> state) {
                lockTwo.countDown();
            }
        };

        final Reference actorOneRef = diaco.spawn(actorOne);
        final Reference actorTwoRef = diaco.spawn(actorTwo);

        diaco.spawn(new RawActor<Object>() {
            @Override
            public void init(State<Object> state) {
                for(int i = 0; i < messageNumber; i++) {
                    send(actorOneRef, new Message.Builder().build());
                    send(actorTwoRef, new Message.Builder().build());
                }
            }
        });

        lockOne.await();
        lockTwo.await();
    }

    @Test
    public void testConcurrentMessagePassing() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(6);

        Actor<String> actorOne = new RawActor<String>() {
            @Override
            public void receive(Message message, State<String> state) {
                assertEquals("ActorTwo->ActorOne", message.getTag());
                lock.countDown();
            }
        };

        Actor<String> actorTwo = new RawActor<String>() {
            @Override
            public void receive(Message message, State<String> state) {
                assertEquals("ActorOne->ActorTwo", message.getTag());
                lock.countDown();
            }
        };

        Actor<String> actorThree = new RawActor<String>() {
            @Override
            public void receive(Message message, State<String> state) {
                assertEquals("ActorFour->ActorThree", message.getTag());
                lock.countDown();
            }
        };

        Actor<String> actorFour = new RawActor<String>() {
            @Override
            public void receive(Message message, State<String> state) {
                assertEquals("ActorThree->ActorFour", message.getTag());
                lock.countDown();
            }
        };

        Actor<String> actorFive = new RawActor<String>() {
            @Override
            public void receive(Message message, State<String> state) {
                assertEquals("ActorSix->ActorFive", message.getTag());
                lock.countDown();
            }
        };

        Actor<String> actorSix = new RawActor<String>() {
            @Override
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

    @Test
    public void testActorExiting() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(1);
        final Reference actorExitingRef = diaco.spawn(new RawActor<Object>() {
            @Override
            public void terminate(State<Object> state) {
                lock.countDown();
            }
        });

        diaco.spawn(new RawActor<Object>() {
            @Override
            public void init(State<Object> state) {
                exit(actorExitingRef);
            }
        });

        lock.await();
    }

    @Test
    public void testActorLinking() throws InterruptedException {
        final CountDownLatch outerLock = new CountDownLatch(1);

        diaco.spawn(new RawActor<Object>() {
            @Override
            public void init(State<Object> state) {

                final CountDownLatch innerLock = new CountDownLatch(2);

                Reference actorOneRef = diaco.spawn(new RawActor<Object>() {
                    @Override
                    public void terminate(State<Object> state) {
                        innerLock.countDown();
                    }
                });

                Reference actorTwoRef = diaco.spawn(new RawActor<Object>() {
                    @Override
                    public void terminate(State<Object> state) {
                        innerLock.countDown();
                    }
                });

                actorOneRef.link(actorTwoRef);
                exit(actorTwoRef);

                try { innerLock.await(); } catch(InterruptedException e) { e.printStackTrace(); }

                assertFalse(actorTwoRef.isAlive());
                assertFalse(actorOneRef.isAlive());

                outerLock.countDown();
            }
        });

        outerLock.await();
    }

    @Test
    public void testActorMonitoring() throws InterruptedException {
        final CountDownLatch outerLock = new CountDownLatch(1);

        diaco.spawn(new RawActor<Object>() {
            @Override
            public void init(State<Object> state) {

                final CountDownLatch innerLock = new CountDownLatch(2);

                Reference actorOneRef = diaco.spawn(new RawActor<Object>() {
                    @Override
                    public void receive(Message message, State<Object> state) {
                        assertEquals(Message.Type.EXITED, message.getType());
                        innerLock.countDown();
                    }
                });

                Reference actorTwoRef = diaco.spawn(new RawActor<Object>() {
                    @Override
                    public void terminate(State<Object> state) {
                        innerLock.countDown();
                    }
                });

                actorOneRef.monitor(actorTwoRef);
                exit(actorTwoRef);

                try { innerLock.await(); } catch(InterruptedException e) { e.printStackTrace(); }

                assertFalse(actorTwoRef.isAlive());

                outerLock.countDown();
            }
        });

        outerLock.await();
    }

    @Test
    public void testActorSpawningDepth() throws InterruptedException {
        final CountDownLatch outerLock = new CountDownLatch(1);

        diaco.spawn(new RawActor<Object>() {
            @Override
            public void init(State<Object> state) {

                final CountDownLatch innerLock = new CountDownLatch(1);

                final Reference receivingActorRef = diaco.spawn(new RawActor<Object>() {
                    @Override
                    public void receive(Message message, State<Object> state) {
                        innerLock.countDown();
                    }
                });

                diaco.spawn(new RawActor<Object>() {
                    @Override
                    public void init(State<Object> state) {
                        send(receivingActorRef, new Message.Builder().build());
                    }
                });

                try { innerLock.await(); } catch(InterruptedException e) { e.printStackTrace(); }

                outerLock.countDown();
            }
        });

        outerLock.await();
    }

    @Test
    public void testCheckNullActors() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(1);
        final Reference uninitializedActor = (Reference) null;

        diaco.spawn(new RawActor<Object>() {
            @Override
            public void init(State<Object> state) {
                send(uninitializedActor, new Message.Builder().build());
                link(uninitializedActor);
                unlink(uninitializedActor);
                monitor(uninitializedActor);
                unmonitor(uninitializedActor);
                exit(uninitializedActor);
                exited(uninitializedActor);
                lock.countDown();
            }
        });

        lock.await();
    }
}