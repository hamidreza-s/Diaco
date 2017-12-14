package io.github.diaco;

import io.github.diaco.actor.Actor;
import io.github.diaco.actor.RawActor;
import io.github.diaco.message.Message;
import io.github.diaco.message.DataMessage;
import io.github.diaco.message.SignalMessage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import sun.text.resources.cldr.ms.FormatData_ms;

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

        final Actor<Message<String>, String> actorTester = new RawActor<Message<String>, String>() {
            @Override
            public void receive(Message<String> message, List<Message<String>> state) {
                // FIXME: get DataMessage instead of Message
                state.add(message);
                if(state.size() == 2) {
                    terminate(state);
                }
            }
            @Override
            public void terminate(List<Message<String>> state) {
                assertEquals("actor:two:started-actor:two:terminated", state.get(0).getBody());
                assertEquals("actor:one:started-actor:one:terminated", state.get(1).getBody());
                lock.countDown();
            }
        };

        Actor<String, String> actorOne = new RawActor<String, String>() {
            @Override
            public void init(List<String> state) {
                state.add("actor:one:started");
            };
            @Override
            public void terminate(List<String> state) {
                state.add("actor:one:terminated");
                send(actorTester, new DataMessage<String>(state.get(0) + "-" + state.get(1)));
            }
        };

        Actor<String, String> actorTwo = new RawActor<String, String>() {
            @Override
            public void init(List<String> state) {
                state.add("actor:two:started");
            }
            @Override
            public void terminate(List<String> state) {
                state.add("actor:two:terminated");
                send(actorTester, new DataMessage<String>(state.get(0) + "-" + state.get(1)));
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
