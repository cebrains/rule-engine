package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.systemsupplementaryservice;

import akka.actor.ActorSystem;
import akka.actor.DeadLetter;

import java.util.function.Function;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-06-11 10:37
 **/
public abstract class AbstractSystemSupplementaryService implements SystemSupplementaryService {
    @Override
    public void initDeadLetterActor(ActorSystem actorSystem, Function<DeadLetter, Void> callBack) {
        this.doInitDeadLetterActor(actorSystem, callBack);
    }
    
    protected abstract void doInitDeadLetterActor(ActorSystem actorSystem, Function<DeadLetter, Void> callBack);
}
