package io.github.diaco.core;

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

    private static final String NODES_NAME_MAP = "nodes-name-map";
    private ReplicatedMap<String, String> nodes;
    private ITopic<byte[]> topic;
    private String name;
    private Config config;

    public Node(Config config) {
        this.config = config;
    }

    public void start() {
        com.hazelcast.config.Config hazelcastConfig = new com.hazelcast.config.Config();
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(hazelcastConfig);
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

    public void send(Message message) {
        try {
            byte[] byteMessage = convertToBytes(message);
            this.topic.publish(byteMessage);
        } catch(IOException ex) {
            ex.printStackTrace();
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
            Object objectMessage = convertFromBytes(topicMessage.getMessageObject());
            Message message = (Message) objectMessage;
            System.out.println("inside node / new message: " + message.getTag());
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private byte[] convertToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    private Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }
}
