package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 20:05
 **/
public interface ActorProcessor {
    
    void dispatchMessage(Object message);
}
