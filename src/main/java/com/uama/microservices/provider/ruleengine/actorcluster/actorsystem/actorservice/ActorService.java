package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actorservice;

import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.ActorSystemContext;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.NodeMessage;

import java.util.List;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 19:42
 **/
public interface ActorService {
    
    void initActorSystem();
    
    /**
     * actor system 唯一入口, 数据会从此处流入system
     * @param message
     */
    void input(NodeMessage message);

    void addRuleChainsToActorSystem(List<String> ruleChainIdList);

    void deleteRuleChainsFromActorSystem(List<MIotRuleEngineRuleChainInitV> ruleChainVList);

    void updateRuleChainsInActorSystem(List<String> ruleChainIdList);
    
    ActorSystemContext getActorSystemContext();
}
