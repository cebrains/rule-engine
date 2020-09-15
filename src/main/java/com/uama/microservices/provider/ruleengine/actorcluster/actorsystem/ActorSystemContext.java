package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.typesafe.config.Config;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 20:12
 **/
public class ActorSystemContext {
    private final ActorSystem actorSystem;
    private final BiMap<ActorTypeMapperEnum, ActorRef> actorHolderMap;
    private final Config config;
    
    private ActorSystemContext(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
        this.config = actorSystem.settings().config();
        this.actorHolderMap = HashBiMap.create();
    }
    
    public static ActorSystemContext create(String actorSystemName, Config config) {
        return null == config ? new ActorSystemContext(ActorSystem.create(actorSystemName)) : new ActorSystemContext(ActorSystem.create(actorSystemName, config));
    }
    
    public ActorSystem getActorSystem() {
        return actorSystem;
    }
    
    public Config getConfig() {
        return config;
    }
    
    public BiMap<ActorTypeMapperEnum, ActorRef> getActorHolderMap() {
        return actorHolderMap;
    }
}
