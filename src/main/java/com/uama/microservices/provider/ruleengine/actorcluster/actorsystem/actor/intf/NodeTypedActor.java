package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.intf;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 21:28
 **/
public interface NodeTypedActor {
    
    ActorTypeMapperEnum getType();
}
