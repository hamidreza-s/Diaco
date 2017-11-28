package io.github.diaco;

import io.github.diaco.kernel.RunQueue;
import io.github.diaco.kernel.Scheduler;

public class Diaco {

    private static Diaco instance = null;

    private RunQueue runQueue = null;

    private Scheduler scheduler = null;
    
    protected Diaco() {
	runQueue = new RunQueue();
	scheduler = new Scheduler(runQueue);
	new Thread(scheduler).start();
    }

    public void spawn(String actor) {
	try {
	    runQueue.put(actor);
	} catch (InterruptedException e) {
	    // TODO ...
	}
    }

    public void halt() {
	// TODO ...
    }
    
    public static Diaco getInstance() {
	if(instance == null)
	    instance = new Diaco();

	return instance;
    }

}
