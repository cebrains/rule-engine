package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage;

import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-06-13 10:07
 **/
public class NodeStopDoneMessage implements NodeMessage {
    
    private final MIotRuleEngineRuleNodeInitV nodeInitV;
    
    public NodeStopDoneMessage(MIotRuleEngineRuleNodeInitV nodeInitV) {
        this.nodeInitV = nodeInitV;
    }
    
    public MIotRuleEngineRuleNodeInitV getNodeInitV() {
        return nodeInitV;
    }
    
    @Override
    public String toString() {
        return "NodeStopDoneMessage{" +
                "nodeInitV=" + this.nodeInitV +
                '}';
    }
}
