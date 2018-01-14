package io.github.diaco;

import io.github.diaco.actor.Actor;
import io.github.diaco.actor.RawActor;
import io.github.diaco.actor.Reference;
import io.github.diaco.actor.State;
import io.github.diaco.message.Message;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.*;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DiacoRemoteActorTest {

    private static Diaco diacoOne;
    private static Diaco diacoTwo;

    @BeforeClass
    public static void beforeSuite() throws InterruptedException {
        diacoOne = DiacoTestHelper.getDiacoOneInstance();
        diacoTwo = DiacoTestHelper.getDiacoTwoInstance();
    }

    @Test
    public void testSimpleMessagePassing() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(2);

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

        Reference actorOneRef = diacoOne.spawn(actorOne);
        Reference actorTwoRef = diacoTwo.spawn(actorTwo);

        actorOneRef.send(actorTwoRef, new Message.Builder().tag("ActorOne->ActorTwo").build());
        actorTwoRef.send(actorOneRef, new Message.Builder().tag("ActorTwo->ActorOne").build());

        lock.await();
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

        final Reference actorEchoingRef = diacoOne.spawn(actorEchoing);

        diacoTwo.spawn(new RawActor<Object>() {
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

        final Reference actorOneRef = diacoOne.spawn(actorOne);
        final Reference actorTwoRef = diacoOne.spawn(actorTwo);

        diacoTwo.spawn(new RawActor<Object>() {
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
    public void testActorLinking() throws InterruptedException {
        final CountDownLatch outerLock = new CountDownLatch(1);

        diacoOne.spawn(new RawActor<Object>() {
            @Override
            public void init(State<Object> state) {

                final CountDownLatch innerLock = new CountDownLatch(2);

                Reference actorOneRef = diacoTwo.spawn(new RawActor<Object>() {
                    @Override
                    public void terminate(State<Object> state) {
                        innerLock.countDown();
                    }
                });

                Reference actorTwoRef = diacoTwo.spawn(new RawActor<Object>() {
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

        diacoOne.spawn(new RawActor<Object>() {
            @Override
            public void init(State<Object> state) {

                final CountDownLatch innerLock = new CountDownLatch(2);

                Reference actorOneRef = diacoTwo.spawn(new RawActor<Object>() {
                    @Override
                    public void receive(Message message, State<Object> state) {
                        assertEquals(Message.Type.EXITED, message.getType());
                        innerLock.countDown();
                    }
                });

                Reference actorTwoRef = diacoTwo.spawn(new RawActor<Object>() {
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
    public void testActorState() throws InterruptedException {
        final CountDownLatch outerLock = new CountDownLatch(1);

        diacoOne.spawn(new RawActor<Object>() {
            @Override
            public void init(State<Object> state) {
                final CountDownLatch innerLock = new CountDownLatch(1);

                Reference actorTarget = diacoTwo.spawn(new RawActor<String>() {
                    @Override
                    public void init(State<String> state) {
                        List<String> stateBody = state.getBody();
                        stateBody.add("Init");
                        state.setBody(stateBody);
                    }

                    @Override
                    public void receive(Message message, State<String> state) {
                        List<String> stateBody = state.getBody();
                        stateBody.add("Recv");
                        state.setBody(stateBody);
                        if(state.getBody().size() == 4)
                            stop();
                    }

                    @Override
                    public void terminate(State<String> state) {
                        assertEquals("Init", state.getBody().get(0));
                        assertEquals("Recv", state.getBody().get(1));
                        assertEquals("Recv", state.getBody().get(2));
                        assertEquals("Recv", state.getBody().get(3));
                        innerLock.countDown();
                    }
                });

                for(int i = 0; i < 3; i++) {
                    send(actorTarget, new Message.Builder().build());
                }

                try { innerLock.await(); } catch(InterruptedException e) { e.printStackTrace(); }

                outerLock.countDown();
            }
        });

        outerLock.await();

    }
}
