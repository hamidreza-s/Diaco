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

public class DiacoRemoteTest extends TestCase {

    public DiacoRemoteTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(DiacoRemoteTest.class);
    }

    public void testRemoteCommunication() throws InterruptedException {
        Config configOne = Config.newConfig();
        configOne.setProperty(Config.NODE_NAME, "diaco-node-test1-one");
        configOne.setProperty(Config.NODE_COOKIE, "secret");
        Diaco diacoOne = Diaco.newInstance(configOne);

        Config configTwo = Config.newConfig();
        configTwo.setProperty(Config.NODE_NAME, "diaco-node-test1-two");
        configTwo.setProperty(Config.NODE_COOKIE, "secret");
        Diaco diacoTwo = Diaco.newInstance(configTwo);

        final Actor<String> actorOne = new RawActor<String>() {
            @Override
            public void receive(Message message, State<String> state) {
                System.out.println("inside actor one / new message: " + message.getTag());
            }
        };

        final Actor<String> actorTwo = new RawActor<String>() {
            @Override
            public void receive(Message message, State<String> state) {
                System.out.println("inside actor two / new message: " + message.getTag());
            }
        };

        Reference actorOneRef = diacoOne.spawn(actorOne);
        Reference actorTwoRef = diacoTwo.spawn(actorTwo);

        actorOneRef.send(actorTwoRef, new Message
                .Builder()
                .from(actorOne)
                .tag("test-tag")
                .body(new byte[]{1, 2, 3})
                .build());

        diacoOne.stop();
        diacoTwo.stop();
    }

//    public void testActorLinkingWithReference() throws InterruptedException {
//        Config configOne = Config.newConfig();
//        configOne.setProperty(Config.NODE_NAME, "diaco-node-test2-one");
//        configOne.setProperty(Config.NODE_COOKIE, "secret");
//        Diaco diacoOne = Diaco.newInstance(configOne);
//
//        Config configTwo = Config.newConfig();
//        configTwo.setProperty(Config.NODE_NAME, "diaco-node-test2-two");
//        configTwo.setProperty(Config.NODE_COOKIE, "secret");
//        Diaco diacoTwo = Diaco.newInstance(configTwo);
//
//        final CountDownLatch lock = new CountDownLatch(1);
//
//        final Actor<String> actorTester = new RawActor<String>() {
//            @Override
//            public void receive(Message message, State<String> state) {
//                state.getBody().add(message.getTag());
//                if(state.getBody().size() == 2) {
//                    terminate(state);
//                }
//            }
//            @Override
//            public void terminate(State<String> state) {
//                assertEquals("actor:two:started-actor:two:terminated", state.getBody().get(0));
//                assertEquals("actor:one:started-actor:one:terminated", state.getBody().get(1));
//                lock.countDown();
//            }
//        };
//
//        Actor<String> actorOne = new RawActor<String>() {
//            @Override
//            public void init(State<String> state) {
//                state.getBody().add("actor:one:started");
//            };
//            @Override
//            public void terminate(State<String> state) {
//                state.getBody().add("actor:one:terminated");
//                String tag = state.getBody().get(0) + "-" + state.getBody().get(1);
//                send(actorTester, new Message.Builder().tag(tag).build());
//
//            }
//        };
//
//        Actor<String> actorTwo = new RawActor<String>() {
//            @Override
//            public void init(State<String> state) {
//                state.getBody().add("actor:two:started");
//            }
//            @Override
//            public void terminate(State<String> state) {
//                state.getBody().add("actor:two:terminated");
//                String tag = state.getBody().get(0) + "-" + state.getBody().get(1);
//                send(actorTester, new Message.Builder().tag(tag).build());
//            }
//        };
//
//        Reference actorTesterRef = diacoOne.spawn(actorTester);
//        Reference actorOneRef = diacoOne.spawn(actorOne);
//        Reference actorTwoRef = diacoTwo.spawn(actorTwo);
//
//        actorOneRef.link(actorTwoRef);
//        actorTesterRef.exit(actorTwoRef);
//
//        lock.await();
//        diacoOne.stop();
//        diacoTwo.stop();
//    }
}
