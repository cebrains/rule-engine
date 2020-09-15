package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity;

import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;

import java.util.List;
import java.util.Map;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-23 11:51
 **/
public class NodeBuildPreparedData {
    
    private final List<MIotRuleEngineRuleChainInitV> addedRuleChainVList;
    
    private final Map<String, MIotRuleEngineRuleChainInitV> ruleChainIdMap;
    
    private final Map<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMap;
    
    private final Map<String, List<MIotRuleEngineRuleNodeInitV>> nodesGroupByRuleChainIdMap;
    
    private final Map<String, List<MIotRuleEngineRuleRelationInitV>> relationsGroupByRuleChainIdMap;
    
    public NodeBuildPreparedData(List<MIotRuleEngineRuleChainInitV> addedRuleChainVList,
                                 Map<String, MIotRuleEngineRuleChainInitV> ruleChainIdMap,
                                 Map<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMap,
                                 Map<String, List<MIotRuleEngineRuleNodeInitV>> nodesGroupByRuleChainIdMap,
                                 Map<String, List<MIotRuleEngineRuleRelationInitV>> relationsGroupByRuleChainIdMap) {
        this.addedRuleChainVList = addedRuleChainVList;
        this.ruleChainIdMap = ruleChainIdMap;
        this.chainsGroupByBelongToIdMap = chainsGroupByBelongToIdMap;
        this.nodesGroupByRuleChainIdMap = nodesGroupByRuleChainIdMap;
        this.relationsGroupByRuleChainIdMap = relationsGroupByRuleChainIdMap;
    }
    
    public List<MIotRuleEngineRuleChainInitV> getAddedRuleChainVList() {
        return addedRuleChainVList;
    }
   
    public Map<String, MIotRuleEngineRuleChainInitV> getRuleChainIdMap() {
        return ruleChainIdMap;
    }
    
    public Map<String, List<MIotRuleEngineRuleChainInitV>> getChainsGroupByBelongToIdMap() {
        return chainsGroupByBelongToIdMap;
    }
    
    public Map<String, List<MIotRuleEngineRuleNodeInitV>> getNodesGroupByRuleChainIdMap() {
        return nodesGroupByRuleChainIdMap;
    }
    
    public Map<String, List<MIotRuleEngineRuleRelationInitV>> getRelationsGroupByRuleChainIdMap() {
        return relationsGroupByRuleChainIdMap;
    }
}
