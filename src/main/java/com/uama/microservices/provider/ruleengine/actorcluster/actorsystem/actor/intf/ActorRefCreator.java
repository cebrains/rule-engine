package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.intf;

import akka.actor.ActorRef;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-10 10:23
 **/
public interface ActorRefCreator {
    
    ActorRef create();
}
