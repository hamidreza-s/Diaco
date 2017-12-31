package io.github.diaco;

import io.github.diaco.actor.Actor;
import io.github.diaco.actor.RawActor;
import io.github.diaco.actor.State;
import io.github.diaco.core.Config;
import io.github.diaco.message.Message;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.CountDownLatch;

public class DiacoNodeTest extends TestCase {

    public DiacoNodeTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(DiacoNodeTest.class);
    }

    public void testActorLinking() throws InterruptedException {
        Config config = Config.getConfig();
        config.setProperty(Config.NODE_NAME, "diaco-node-test");
        config.setProperty(Config.NODE_COOKIE, "secret");

        Diaco diaco = Diaco.getInstance(config);
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

        Actor<String> actorOne = new RawActor<String>() {
            @Override
            public void init(State<String> state) {
                state.getBody().add("actor:one:started");
            };
            @Override
            public void terminate(State<String> state) {
                state.getBody().add("actor:one:terminated");
                String tag = state.getBody().get(0) + "-" + state.getBody().get(1);
                send(actorTester, new Message.Builder().tag(tag).build());

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
                send(actorTester, new Message.Builder().tag(tag).build());
            }
        };

        diaco.spawn(actorTester);
        diaco.spawn(actorOne);
        diaco.spawn(actorTwo);

        actorOne.link(actorTwo);
        actorTester.exit(actorTwo);

        lock.await();

    }

    public void testActorMonitoring() throws InterruptedException {
    }

    public void testMessagePassing() throws InterruptedException {
    }
}
