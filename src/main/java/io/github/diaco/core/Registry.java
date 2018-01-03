package io.github.diaco.core;

import io.github.diaco.actor.Actor;
import java.util.HashMap;
import java.util.Map;

public class Registry {

    // TODO: remove dead actors from actorsMap
    // TODO: put actor's runnable future in its reference

    private static Map<Integer, Actor> actorsMap = new HashMap<Integer, Actor>();

    public static void addActor(Actor actor) {
        actorsMap.put(actor.getIdentifier(), actor);
    }

    public static Actor getActor(Integer actorIdentifier) {
        return actorsMap.get(actorIdentifier);
    }
}
