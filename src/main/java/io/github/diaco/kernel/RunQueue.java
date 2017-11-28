package io.github.diaco.kernel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

public class RunQueue {

    private BlockingQueue<String> queue = null;
    
    public RunQueue() {
	queue = new ArrayBlockingQueue<String>(1024);
    }

    public void put(String actor) throws InterruptedException {
	queue.put(actor);
    }

    public String take() throws InterruptedException {
	return queue.take();
    }
    
}
