package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.systemsupplementaryservice.actor;

import akka.actor.AbstractActor;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-06-11 10:05
 **/
public abstract class SystemSupplementaryActor extends AbstractActor {
    
    public abstract SystemSupplementaryType getType();
}
