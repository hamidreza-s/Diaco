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

public class DiacoLocalTest extends TestCase {

    public DiacoLocalTest(String testName) {
        super(testName);
    }

    public void testActorLinking() throws InterruptedException {
    }

    public void testActorLinkingWithReference() throws InterruptedException {
        Diaco diaco = DiacoTestHelper.getDiacoOneInstance();
        final CountDownLatch lock = new CountDownLatch(1);

        final Actor<String> actorTester = new RawActor<String>() {
            @Override
            public void receive(Message message, State<String> state) {
                state.getBody().add(message.getTag());
                if(state.getBody().size() == 2) {
                    terminate(state);
                }
            }
            @Override
            public void terminate(State<String> state) {
                assertEquals("actor:two:started-actor:two:terminated", state.getBody().get(0));
                assertEquals("actor:one:started-actor:one:terminated", state.getBody().get(1));
                lock.countDown();
            }
        };

        final Reference actorTesterRef = diaco.spawn(actorTester);

        Actor<String> actorOne = new RawActor<String>() {
            @Override
            public void init(State<String> state) {
                state.getBody().add("actor:one:started");
            };
            @Override
            public void terminate(State<String> state) {
                state.getBody().add("actor:one:terminated");
                String tag = state.getBody().get(0) + "-" + state.getBody().get(1);
                send(actorTesterRef, new Message.Builder().tag(tag).build());

            }
        };

        Actor<String> actorTwo = new RawActor<String>() {
            @Override
            public void init(State<String> state) {
                state.getBody().add("actor:two:started");
            }
            @Override
            public void terminate(State<String> state) {
                state.getBody().add("actor:two:terminated");
                String tag = state.getBody().get(0) + "-" + state.getBody().get(1);
                send(actorTesterRef, new Message.Builder().tag(tag).build());
            }
        };

        Reference actorOneRef = diaco.spawn(actorOne);
        Reference actorTwoRef = diaco.spawn(actorTwo);

        actorOneRef.link(actorTwoRef);
        actorTesterRef.exit(actorTwoRef);

        lock.await();
    }

    public void testMessagePassing() throws InterruptedException {
    }

    public void testMessagePassingWithReference() throws InterruptedException {
        Diaco diaco = DiacoTestHelper.getDiacoOneInstance();
        final CountDownLatch lock = new CountDownLatch(2);

        Actor<String> actorOne = new RawActor<String>() {
            public void receive(Message message, State<String> state) {
                assertEquals("actor:two::actor:one", message.getTag());
                lock.countDown();
            }
        };

        Actor<String> actorTwo = new RawActor<String>() {
            public void receive(Message message, State<String> state) {
                assertEquals("actor:one::actor:two", message.getTag());
                lock.countDown();
            }
        };

        Reference actorOneRef = diaco.spawn(actorOne);
        Reference actorTwoRef = diaco.spawn(actorTwo);

        actorOneRef.send(actorTwoRef, new Message.Builder().tag("actor:one::actor:two").build());
        actorTwoRef.send(actorOneRef, new Message.Builder().tag("actor:two::actor:one").build());

        lock.await();
    }

    public void testActorMonitoring() throws InterruptedException {
    }
}
