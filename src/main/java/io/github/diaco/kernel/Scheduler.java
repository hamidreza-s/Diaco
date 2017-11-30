package io.github.diaco.kernel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scheduler {

    protected RunQueue queue = null;

    protected ExecutorService executor = null;

    public Scheduler(RunQueue queue) {
        this.queue = queue;
        this.executor = Executors.newFixedThreadPool(10);
    }

    public void start() {
        new Thread(new Runnable() {
            public void run() {
                while(true) {
                    try {
                        executor.execute(queue.take());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
