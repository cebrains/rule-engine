package com.uama.microservices.provider.ruleengine.actorcluster;

import akka.actor.ActorRef;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.uama.microservices.provider.ruleengine.actorcluster.actor.ActorTypeEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.ActorSystemContext;

/**
 * @program: uama-iot
 * @description:
 * @author: liwen
 * @create: 2019-09-03 15:35
 **/
public class ActorClusterContext {
    private final ActorSystemContext actorSystemContext;
    
    private final BiMap<ActorTypeEnum, ActorRef> actorBiMap;
    
    private ActorClusterContext(ActorSystemContext actorSystemContext) {
        this.actorSystemContext = actorSystemContext;
        this.actorBiMap = HashBiMap.create();
    }
    
    public static ActorClusterContext create(ActorSystemContext actorSystemContext) {
        return new ActorClusterContext(actorSystemContext);
    }
    
    public ActorSystemContext getActorSystemContext() {
        return actorSystemContext;
    }
    
    public BiMap<ActorTypeEnum, ActorRef> getActorBiMap() {
        return actorBiMap;
    }
}
