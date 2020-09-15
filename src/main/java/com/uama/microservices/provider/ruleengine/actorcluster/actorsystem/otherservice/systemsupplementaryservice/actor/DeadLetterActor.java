package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.systemsupplementaryservice.actor;

import akka.actor.DeadLetter;
import akka.japi.pf.ReceiveBuilder;

import java.util.function.Function;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-06-11 10:07
 **/
public class DeadLetterActor extends SystemSupplementaryActor {
    
    private final Function<DeadLetter, Void> callBack;
    
    public DeadLetterActor(Function<DeadLetter, Void> callBack) {
        this.callBack = callBack;
    }
    
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(DeadLetter.class, callBack::apply)
                .build();
    }
    
    @Override
    public SystemSupplementaryType getType() {
        return SystemSupplementaryType.DEAD_LETTER;
    }
}
