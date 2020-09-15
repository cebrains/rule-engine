package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage;

import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-13 19:55
 **/
public class NodeInitMessage implements NodeMessage {
    
    private final Map<String, List<MIotRuleEngineRuleRelationInitV>> relationGroupByFromIdMap;
    
    private final Map<String, List<MIotRuleEngineRuleRelationInitV>> relationGroupByToIdMap;
    
    private final Map<String, MIotRuleEngineRuleNodeInitV> ruleNodeIdMap;
    
    private final Map<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMap;
    
    private final Map<String, MIotRuleEngineRuleChainInitV> ruleChainIdMap;
    
    private final long initTimeStamp;
    
    public NodeInitMessage(Map<String, List<MIotRuleEngineRuleRelationInitV>> relationGroupByFromIdMap,
                           Map<String, List<MIotRuleEngineRuleRelationInitV>> relationGroupByToIdMap,
                           Map<String, MIotRuleEngineRuleNodeInitV> ruleNodeIdMap,
                           Map<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMap,
                           Map<String, MIotRuleEngineRuleChainInitV> ruleChainIdMap,
                           long initTimeStamp) {
        this.relationGroupByFromIdMap = relationGroupByFromIdMap;
        this.relationGroupByToIdMap = relationGroupByToIdMap;
        this.ruleNodeIdMap = ruleNodeIdMap;
        this.chainsGroupByBelongToIdMap = chainsGroupByBelongToIdMap;
        this.ruleChainIdMap = ruleChainIdMap;
        this.initTimeStamp = initTimeStamp;
    }
    
    public Map<String, List<MIotRuleEngineRuleRelationInitV>> getRelationGroupByFromIdMap() {
        return relationGroupByFromIdMap;
    }
    
    public Map<String, List<MIotRuleEngineRuleRelationInitV>> getRelationGroupByToIdMap() {
        return relationGroupByToIdMap;
    }
    
    public Map<String, MIotRuleEngineRuleNodeInitV> getRuleNodeIdMap() {
        return ruleNodeIdMap;
    }
    
    public Map<String, List<MIotRuleEngineRuleChainInitV>> getChainsGroupByBelongToIdMap() {
        return chainsGroupByBelongToIdMap;
    }
    
    public Map<String, MIotRuleEngineRuleChainInitV> getRuleChainIdMap() {
        return ruleChainIdMap;
    }
    
    public long getInitTimeStamp() {
        return initTimeStamp;
    }
    
    @Override
    public String toString() {
        return "NodeInitMessage{" +
                "ruleNodeIdMap=" + this.ruleNodeIdMap +
                ", initTimeStamp=" + new Date(this.initTimeStamp) +
                '}';
    }
}
