package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage;

import akka.actor.ActorRef;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-06-12 15:26
 **/
public class NodeStopMessage implements NodeMessage {
    
    private final ActorRef actorRef;
    
    private final MIotRuleEngineRuleNodeInitV nodeInitV;
    
    public NodeStopMessage(ActorRef actorRef, MIotRuleEngineRuleNodeInitV nodeInitV) {
        this.actorRef = actorRef;
        this.nodeInitV = nodeInitV;
    }
    
    public ActorRef getActorRef() {
        return actorRef;
    }
    
    public MIotRuleEngineRuleNodeInitV getNodeInitV() {
        return nodeInitV;
    }
    
    @Override
    public String toString() {
        return "NodeStopMessage{" +
                "actorRef=" + this.actorRef +
                ", nodeInitV=" + this.nodeInitV +
                '}';
    }
}
