package io.github.diaco.core;

import io.github.diaco.core.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.core.ITopic;
import java.util.Properties;

public class Node implements MessageListener<String> {

    private static Node instance;
    private static final String NODES_NAME_MAP = "nodes-name-map";
    private ReplicatedMap<String, String> nodes;
    private String name;
    private Node(Config config) {

    }

    private void start(Config config) {
        com.hazelcast.config.Config hazelcastConfig = new com.hazelcast.config.Config();
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(hazelcastConfig);
        name = config.getProperty(Config.NODE_NAME);
        nodes = hazelcast.getReplicatedMap(Node.NODES_NAME_MAP);
        nodes.put(name, hazelcast.getName());

        ITopic<String> topic = hazelcast.getTopic(name);
        topic.addMessageListener(this);
    }

    public void stop() {
        nodes.remove(name);
        // TODO: stop node
    }

    public void onMessage(Message<String> message) {

    }

    public static Node getInstance(Config config) {
        if(instance == null) {
            instance = new Node(config);
            instance.start(config);
        }

        return instance;
    }
}