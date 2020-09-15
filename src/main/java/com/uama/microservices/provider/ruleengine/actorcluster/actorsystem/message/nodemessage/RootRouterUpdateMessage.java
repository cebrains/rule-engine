package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage;

import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;

import java.util.List;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-22 22:51
 **/
public class RootRouterUpdateMessage implements NodeMessage {
    
    private final List<MIotRuleEngineRuleChainInitV> ruleChainVList;
    
    public RootRouterUpdateMessage(List<MIotRuleEngineRuleChainInitV> ruleChainVList) {
        this.ruleChainVList = ruleChainVList;
    }
    
    public List<MIotRuleEngineRuleChainInitV> getRuleChainVList() {
        return ruleChainVList;
    }
    
    @Override
    public String toString() {
        return "RootRouterUpdateMessage{" +
                "ruleChainVList=" + this.ruleChainVList +
                '}';
    }
}
