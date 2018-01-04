package io.github.diaco;

import io.github.diaco.core.Config;

public class DiacoTestHelper {

    private static Diaco diacoOne;
    private static Diaco diacoTwo;

    private DiacoTestHelper() {}

    public static void startDiacoInstances() {
        getDiacoOneInstance();
        getDiacoTwoInstance();
    }

    public static Diaco getDiacoOneInstance() {
        if(diacoOne == null) {
            Config config = Config.newConfig();
            config.setProperty(Config.NODE_NAME, "diaco-one");
            config.setProperty(Config.NODE_COOKIE, "secret");
            diacoOne = Diaco.newInstance(config);
            return diacoOne;
        } else {
            return diacoOne;
        }
    }

    public static Diaco getDiacoTwoInstance() {
        if(diacoTwo == null) {
            Config config = Config.newConfig();
            config.setProperty(Config.NODE_NAME, "diaco-two");
            config.setProperty(Config.NODE_COOKIE, "secret");
            diacoTwo = Diaco.newInstance(config);
            return diacoTwo;
        } else {
            return diacoTwo;
        }
    }

}
