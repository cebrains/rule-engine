package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actorservice;

import com.uama.microservices.provider.ruleengine.mongo.service.DeadLetterMongoDbService;
import com.uama.microservices.provider.ruleengine.mongo.service.EndPointMongoDbService;
import com.uama.microservices.provider.ruleengine.service.rulechain.IIotRuleChainService;
import com.uama.microservices.provider.ruleengine.service.rulenode.IIotRuleNodeService;
import com.uama.microservices.provider.ruleengine.service.rulerelation.IIotRuleRelationService;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-10 17:30
 **/
public interface ActorDependenceService {
    
    IIotRuleRelationService getIotRuleRelationService();
    
    IIotRuleChainService getIotRuleChainService();
    
    IIotRuleNodeService getIotRuleNodeService();
    
    EndPointMongoDbService getEndPointMongoDbService();
    
    DeadLetterMongoDbService getDeadLetterMongoDbService();
}
