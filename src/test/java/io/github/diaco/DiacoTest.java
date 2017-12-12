package io.github.diaco;

import io.github.diaco.actor.Actor;
import io.github.diaco.actor.RawActor;
import io.github.diaco.message.Message;
import io.github.diaco.message.DataMessage;
import io.github.diaco.message.SignalMessage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DiacoTest extends TestCase {

    public DiacoTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(DiacoTest.class);
    }

    public void testActorLinking() throws InterruptedException {
        Diaco diaco = Diaco.getInstance();
        final CountDownLatch lock = new CountDownLatch(1);

        final Actor<Message> actorTester = new RawActor<Message>() {
            public void init(List<Message> state) {};
            public void receive(Message message, List<Message> state) {
                state.add(message);
                if(state.size() == 2) {
                    terminate(state);
                }
            }
            public void terminate(List<Message> state) {
                // TODO: use assert to compare state with expected state
                System.out.println(state);
                lock.countDown();
            }
        };

        Actor<String> actorOne = new RawActor<String>() {
            public void init(List<String> state) {
                state.add("actor:one:started");
            };
            public void receive(Message message, List<String> state) {}
            public void terminate(List<String> state) {
                state.add("actor:one:terminated");
                send(actorTester, new DataMessage<List<String>>(state));
            }
        };

        Actor<String> actorTwo = new RawActor<String>() {
            public void init(List<String> state) {
                state.add("actor:two:started");
            }
            public void receive(Message message, List<String> state) {}
            public void terminate(List<String> state) {
                state.add("actor:two:terminated");
                send(actorTester, new DataMessage<List<String>>(state));
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
