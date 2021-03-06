package io.github.diaco.core;

import io.github.diaco.actor.Actor;
import io.github.diaco.actor.BaseActor;
import io.github.diaco.actor.Reference;
import io.github.diaco.message.Envelope;
import io.github.diaco.message.Message;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MessageListener;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.core.ITopic;

import java.util.ArrayList;
import java.util.List;
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
    private boolean local;

    public Node(Config config) {
        this.config = config;
    }

    public void start() {
        if(config.containsKey(Config.NODE_NAME)) {
            String nodeName = config.getProperty(Config.NODE_NAME);
            String nodeMembers = config.getProperty(Config.NODE_MEMBERS, "127.0.0.1");
            String hazelcastLogger = config.getProperty(Config.NODE_LOGGER, "none");
            com.hazelcast.config.Config hazelcastConfig = new com.hazelcast.config.Config();
            hazelcastConfig.setInstanceName(nodeName);
            com.hazelcast.config.NetworkConfig networkConfig = hazelcastConfig.getNetworkConfig();
            networkConfig.getJoin().getMulticastConfig().setEnabled(false);
            com.hazelcast.config.TcpIpConfig tcpIpConfig = networkConfig.getJoin().getTcpIpConfig();
            List<String> clusterMembers = new ArrayList<String>();
            clusterMembers.add(nodeMembers);
            tcpIpConfig.setMembers(clusterMembers);
            tcpIpConfig.setEnabled(true);
            hazelcastConfig.setProperty("hazelcast.logging.type", hazelcastLogger);

            this.hazelcast = Hazelcast.newHazelcastInstance(hazelcastConfig);
            this.name = nodeName;
            this.local = false;
            this.nodes = hazelcast.getReplicatedMap(Node.NODES_NAME_MAP);
            this.nodes.put(nodeName, hazelcast.getName());
            this.topic = hazelcast.getTopic(nodeName);
            this.topic.addMessageListener(this);
        } else {
            this.local = true;
            this.name = "local";
        }
    }

    public void stop() {
        this.nodes.remove(name);
        // TODO: stop node
    }

    public void send(Envelope envelope) {
        if(local)
            throw new RuntimeException("node is not started!");

        Message message = envelope.getMessage();
        Reference senderReference = envelope.getFrom();
        Reference recipientReference = envelope.getTo();

        try {
            if(this.nodes.containsKey(recipientReference.getNodeName())) {

                byte[] byteEnvelope = convertToBytes(envelope);

                if(this.name == recipientReference.getNodeName()) {
                    this.topic.publish(byteEnvelope);
                } else {
                    ITopic<byte[]> remoteTopic = this.hazelcast.getTopic(recipientReference.getNodeName());
                    remoteTopic.publish(byteEnvelope);
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
            Object objectEnvelope = convertFromBytes(topicMessage.getMessageObject());
            Envelope envelope = (Envelope) objectEnvelope;
            Reference recipientReference = envelope.getTo();
            Actor recipientActor = Registry.getActor(recipientReference.getActorIdentifier());
            BaseActor baseRecipientActor = (BaseActor) recipientActor;
            baseRecipientActor.putIntoMailbox(envelope);
        } catch(Exception e) {
            e.printStackTrace();
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
