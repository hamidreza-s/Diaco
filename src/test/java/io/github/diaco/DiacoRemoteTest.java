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

public class DiacoRemoteTest extends TestCase {

    public DiacoRemoteTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(DiacoRemoteTest.class);
    }

    public void testRemoteCommunication() throws InterruptedException {
        Config configOne = Config.newConfig();
        configOne.setProperty(Config.NODE_NAME, "diaco-node-test-one");
        configOne.setProperty(Config.NODE_COOKIE, "secret");
        Diaco diacoOne = Diaco.newInstance(configOne);

        Config configTwo = Config.newConfig();
        configTwo.setProperty(Config.NODE_NAME, "diaco-node-test-two");
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

        Reference diacoOneRef = diacoOne.spawn(actorOne);
        Reference diacoTwoRef = diacoTwo.spawn(actorTwo);

        actorOne.send(actorTwo, new Message
                .Builder()
                .tag("test-tag")
                .body(new byte[]{1, 2, 3})
                .build());
    }
}