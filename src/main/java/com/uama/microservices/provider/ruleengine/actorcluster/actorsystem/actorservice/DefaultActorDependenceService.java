package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actorservice;

import com.uama.microservices.provider.ruleengine.mongo.service.DeadLetterMongoDbService;
import com.uama.microservices.provider.ruleengine.mongo.service.EndPointMongoDbService;
import com.uama.microservices.provider.ruleengine.service.rulechain.IIotRuleChainService;
import com.uama.microservices.provider.ruleengine.service.rulenode.IIotRuleNodeService;
import com.uama.microservices.provider.ruleengine.service.rulerelation.IIotRuleRelationService;
import com.uama.microservices.provider.ruleengine.support.RedisHelperWithNoExpire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-10 17:31
 **/
@Component
public class DefaultActorDependenceService implements ActorDependenceService {
    
    @Autowired
    private IIotRuleRelationService iotRuleRelationService;
    
    @Autowired
    private IIotRuleChainService iotRuleChainService;
    
    @Autowired
    private IIotRuleNodeService iotRuleNodeService;
    
    @Autowired
    private RedisHelperWithNoExpire redisHelper;
    
    @Autowired
    private EndPointMongoDbService endPointMongoDbService;
    
    @Autowired
    private DeadLetterMongoDbService deadLetterMongoDbService;
    
    @Override
    public IIotRuleRelationService getIotRuleRelationService() {
        return iotRuleRelationService;
    }
    
    @Override
    public IIotRuleChainService getIotRuleChainService() {
        return iotRuleChainService;
    }
    
    @Override
    public IIotRuleNodeService getIotRuleNodeService() {
        return iotRuleNodeService;
    }
    
    public RedisHelperWithNoExpire getRedisHelper() {
        return redisHelper;
    }
    
    @Override
    public EndPointMongoDbService getEndPointMongoDbService() {
        return endPointMongoDbService;
    }
    
    @Override
    public DeadLetterMongoDbService getDeadLetterMongoDbService() {
        return deadLetterMongoDbService;
    }
}
