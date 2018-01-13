package io.github.diaco.core;

import java.util.Properties;

public class Config extends Properties {

    public static String ACTOR_SPAWNING_DEPTH = "actor-spawning-depth";
    public static String SCHEDULER_RUN_QUEUE_SIZE = "scheduler-run-queue-size";
    public static String SCHEDULER_THREAD_POOL_SIZE = "scheduler-thread-pool-size";
    public static String NODE_NAME = "node-name";
    public static String NODE_COOKIE = "node-cookie";

    private static String DEFAULT_ACTOR_SPAWNING_DEPTH = "2";
    private static String DEFAULT_SCHEDULER_RUN_QUEUE_SIZE = "1024";
    private static String DEFAULT_SCHEDULER_THREAD_POOL_SIZE = Integer
            .toString(Runtime.getRuntime().availableProcessors());

    private Config(Properties defaultConfig) {
        super(defaultConfig);
    }

    public static Config newConfig() {
        Properties defaultConfig = new Properties();
        defaultConfig.setProperty(ACTOR_SPAWNING_DEPTH, DEFAULT_ACTOR_SPAWNING_DEPTH);
        defaultConfig.setProperty(SCHEDULER_RUN_QUEUE_SIZE, DEFAULT_SCHEDULER_RUN_QUEUE_SIZE);
        defaultConfig.setProperty(SCHEDULER_THREAD_POOL_SIZE, DEFAULT_SCHEDULER_THREAD_POOL_SIZE);
        return new Config(defaultConfig);
    }

    public static void checkRequired(Config config) {}
}
