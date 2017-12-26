package io.github.diaco.node;

import java.io.IOException;
import java.net.InetSocketAddress;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class Node implements Runnable {

    private final static String ADDRESS = "127.0.0.1";
    private final static int PORT = 1881;
    private final static long TIMEOUT = 10000;

    private ServerSocketChannel serverChannel;
    private Selector selector;
    private Map<SocketChannel, byte[]> dataTracking = new HashMap<SocketChannel, byte[]>();

    public Node() {
        init();
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    private void init() {
        if(selector != null || serverChannel != null) return;

        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(ADDRESS, PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while(!Thread.currentThread().isInterrupted()) {
                selector.select(TIMEOUT);
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while(keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if(!key.isValid()) {
                        continue;
                    }

                    if(key.isAcceptable()) {
                        accept(key);
                    }

                    if(key.isWritable()) {
                        write(key);
                    }

                    if(key.isReadable()) {
                        read(key);
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_WRITE);
        byte[] intro = "Here is node protocol intro".getBytes();
        dataTracking.put(socketChannel, intro);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        byte[] data = dataTracking.get(channel);
        dataTracking.remove(channel);
        channel.write(ByteBuffer.wrap(data));
        key.interestOps(SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        int read;
        readBuffer.clear();
        try {
            read = channel.read(readBuffer);
        } catch(IOException e) {
            key.cancel();
            channel.close();
            return;
        }
        if(read == -1) {
            channel.close();
            key.cancel();
            return;
        }
        readBuffer.flip();
        byte[] data = new byte[1000];
        readBuffer.get(data, 0, read);
        echo(key, data);
    }

    private void close() {
        if(selector != null) {
            try {
                selector.close();
                serverChannel.socket().close();
                serverChannel.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        // TODO: stop node
        close();
    }

    private void echo(SelectionKey key, byte[] data) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        dataTracking.put(socketChannel, data);
        key.interestOps(SelectionKey.OP_WRITE);
    }
}
