package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-14 15:20
 **/
public interface MessageCarrier<M>{
    
    Class<M> getMessageClass();
}
