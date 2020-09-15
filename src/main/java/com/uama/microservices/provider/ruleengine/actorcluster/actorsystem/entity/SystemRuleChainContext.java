package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity;

import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.NodeInitMessage;

import java.util.List;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-06-13 14:41
 **/
public class SystemRuleChainContext {
    
    private String ruleChainId;
    
    private List<MIotRuleEngineRuleNodeInitV> startNodeInitVList;
    
    private List<MIotRuleEngineRuleNodeInitV> nodeInitVList;
    
    // 链路即将更新的更新信息
    private NodeInitMessage updateInitMessage;
    
    public String getRuleChainId() {
        return ruleChainId;
    }
    
    public void setRuleChainId(String ruleChainId) {
        this.ruleChainId = ruleChainId;
    }
    
    public List<MIotRuleEngineRuleNodeInitV> getStartNodeInitVList() {
        return startNodeInitVList;
    }
    
    public void setStartNodeInitVList(List<MIotRuleEngineRuleNodeInitV> startNodeInitVList) {
        this.startNodeInitVList = startNodeInitVList;
    }
    
    public List<MIotRuleEngineRuleNodeInitV> getNodeInitVList() {
        return nodeInitVList;
    }
    
    public void setNodeInitVList(List<MIotRuleEngineRuleNodeInitV> nodeInitVList) {
        this.nodeInitVList = nodeInitVList;
    }
    
    public NodeInitMessage getUpdateInitMessage() {
        return updateInitMessage;
    }
    
    public void setUpdateInitMessage(NodeInitMessage updateInitMessage) {
        this.updateInitMessage = updateInitMessage;
    }
    
    @Override
    public String toString() {
        return "SystemRuleChainContext{" +
                "ruleChainId='" + this.ruleChainId + '\'' +
                ", startNodeInitVList=" + this.startNodeInitVList +
                ", nodeInitVList=" + this.nodeInitVList +
                ", updateInitMessage=" + this.updateInitMessage +
                '}';
    }
}
