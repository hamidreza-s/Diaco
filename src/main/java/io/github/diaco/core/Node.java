package io.github.diaco.core;

import com.hazelcast.nio.ObjectDataInput;
import io.github.diaco.actor.LocalReference;
import io.github.diaco.actor.Reference;
import io.github.diaco.actor.RemoteReference;
import io.github.diaco.message.Message;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MessageListener;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.core.ITopic;
import java.util.Map;
import java.io.*;

public class Node implements MessageListener<byte[]> {

    // TODO: check cookie
    // TODO: use a better way of serialization

    private static final String NODES_NAME_MAP = "nodes-name-map";
    private HazelcastInstance hazelcast;
    private ReplicatedMap<String, String> nodes;
    private ITopic<byte[]> topic;
    private String name;
    private Config config;

    public Node(Config config) {
        this.config = config;
    }

    public void start() {
        com.hazelcast.config.Config hazelcastConfig = new com.hazelcast.config.Config();
        this.hazelcast = Hazelcast.newHazelcastInstance(hazelcastConfig);
        this.name = config.getProperty(Config.NODE_NAME);
        this.nodes = hazelcast.getReplicatedMap(Node.NODES_NAME_MAP);
        this.nodes.put(name, hazelcast.getName());

        this.topic = hazelcast.getTopic(name);
        this.topic.addMessageListener(this);
    }

    public void stop() {
        this.nodes.remove(name);
        // TODO: stop node
    }

    public void send(int senderIdentifer, Reference reference, Message message) {
        try {
            if(this.nodes.containsKey(reference.getNodeName())) {

                byte[] byteMessage = convertToBytes(senderIdentifer, reference.getActorIdentifier(), message);

                if(this.name == reference.getNodeName()) {
                    this.topic.publish(byteMessage);
                } else {
                    ITopic<byte[]> remoteTopic = this.hazelcast.getTopic(reference.getNodeName());
                    remoteTopic.publish(byteMessage);
                }

            } else {
                throw new RuntimeException("remote node is not connected!");
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String> getNodes() {
        return this.nodes;
    }

    public void onMessage(com.hazelcast.core.Message<byte[]> topicMessage) {
        try {
            Tuple tuple = convertFromBytes(topicMessage.getMessageObject());
            int senderIdentifier = tuple.first;
            int recipientIdentifier = tuple.second;
            Object objectMessage = tuple.third;
            Message message = (Message) objectMessage;

            System.out.println("===> inside node / new message:");
            System.out.println("-------> sender-id: " + senderIdentifier);
            System.out.println("-------> recipient-id " + recipientIdentifier);
            System.out.println("-------> tag: " + message.getTag());
            System.out.println("-------> body: " + message.getBody());
            System.out.println("-------> type: " + message.getType());

            Reference senderRef = new RemoteReference(senderIdentifier, "todo");
            Reference recipientRef = new LocalReference(recipientIdentifier, this.name);

            senderRef.send(recipientRef, message);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] convertToBytes(int senderIdentifier, int recipientIdentifier, Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeInt(senderIdentifier);
            out.writeInt(recipientIdentifier);
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    private Tuple convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            int senderIdentifier = in.readInt();
            int recipientIdentifier = in.readInt();
            Object object = in.readObject();

            return new Tuple(senderIdentifier, recipientIdentifier, object);
        }
    }

    private class Tuple {
        public int first;
        public int second;
        public Object third;

        public Tuple(int first, int second, Object third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }
}
