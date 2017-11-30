package io.github.diaco.kernel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

public class RunQueue {

    private BlockingQueue<Runnable> queue = null;
    
    public RunQueue() {
        queue = new ArrayBlockingQueue<Runnable>(1024);
    }

    public void put(Runnable actor) throws InterruptedException {
        queue.put(actor);
    }

    public Runnable take() throws InterruptedException {
        return queue.take();
    }

    public BlockingQueue<Runnable> getQueue() {
        return queue;
    }
    
}
