package io.github.diaco.core;

import io.github.diaco.message.Message;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MessageListener;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.core.ITopic;
import java.util.Map;


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
        // TODO: serialize message to byte
        this.topic.publish(message.getBody());
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String> getNodes() {
        return this.nodes;
    }

    public void onMessage(com.hazelcast.core.Message<byte[]> message) {
        System.out.println("inside node / new message: " + message.getMessageObject());
    }
}