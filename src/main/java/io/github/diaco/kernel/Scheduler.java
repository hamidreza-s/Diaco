package io.github.diaco.kernel;

public class Scheduler implements Runnable {

    protected RunQueue queue = null;

    public Scheduler(RunQueue queue) {
        this.queue = queue;
    }

    public void run() {
	try {
	    System.out.println(queue.take());
	} catch (InterruptedException e) {
	    // TODO ...
	}
    }
    
}
