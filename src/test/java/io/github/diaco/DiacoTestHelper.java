package io.github.diaco;

import io.github.diaco.core.Config;

public class DiacoTestHelper {

    private static Diaco diacoZero;
    private static Diaco diacoOne;
    private static Diaco diacoTwo;

    private DiacoTestHelper() {}

    public static Diaco getDiacoZeroInstance() {
        if(diacoZero == null) {
            Config config = Config.newConfig();
            config.setProperty(Config.SCHEDULER_THREAD_POOL_SIZE, "2");
            config.setProperty(Config.ACTOR_SPAWNING_DEPTH, "2");
            config.setProperty(Config.NODE_LOGGER, "none");
            diacoZero = Diaco.newInstance(config);
            return diacoZero;
        } else {
            return diacoZero;
        }

    }

    public static Diaco getDiacoOneInstance() {
        if(diacoOne == null) {
            Config config = Config.newConfig();
            config.setProperty(Config.SCHEDULER_THREAD_POOL_SIZE, "2");
            config.setProperty(Config.ACTOR_SPAWNING_DEPTH, "2");
            config.setProperty(Config.NODE_NAME, "diaco-one");
            config.setProperty(Config.NODE_COOKIE, "secret");
            config.setProperty(Config.NODE_MEMBERS, "localhost");
            config.setProperty(Config.NODE_LOGGER, "none");
            diacoOne = Diaco.newInstance(config);
            return diacoOne;
        } else {
            return diacoOne;
        }
    }

    public static Diaco getDiacoTwoInstance() {
        if(diacoTwo == null) {
            Config config = Config.newConfig();
            config.setProperty(Config.SCHEDULER_THREAD_POOL_SIZE, "2");
            config.setProperty(Config.ACTOR_SPAWNING_DEPTH, "2");
            config.setProperty(Config.NODE_NAME, "diaco-two");
            config.setProperty(Config.NODE_COOKIE, "secret");
            config.setProperty(Config.NODE_MEMBERS, "localhost");
            config.setProperty(Config.NODE_LOGGER, "none");
            diacoTwo = Diaco.newInstance(config);
            return diacoTwo;
        } else {
            return diacoTwo;
        }
    }

}
