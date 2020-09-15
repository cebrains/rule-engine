package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage;

import com.alibaba.fastjson.JSONArray;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;

import java.util.Date;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-15 19:18
 **/
public class NodeTriggerMessage implements NodeMessage {
    
    private final NodeId triggerNodeIdV;
    
    private final JSONArray triggerFields;
    
    private final NodeDataMessage nodeDataMessage;
    
    private final Date triggerTime;
    
    public NodeTriggerMessage(NodeId triggerNodeIdV, JSONArray triggerFields, Date triggerTime, NodeDataMessage nodeDataMessage) {
        this.triggerNodeIdV = triggerNodeIdV;
        this.triggerFields = triggerFields;
        this.triggerTime = triggerTime;
        this.nodeDataMessage = nodeDataMessage;
    }
    
    public NodeId getTriggerNodeIdV() {
        return triggerNodeIdV;
    }
    
    public JSONArray getTriggerFields() {
        return triggerFields;
    }
    
    public Date getTriggerTime() {
        return triggerTime;
    }
    
    public NodeDataMessage getNodeDataMessage() {
        return nodeDataMessage;
    }
    
    @Override
    public String toString() {
        return "NodeTriggerMessage{" +
                "triggerNodeIdV=" + this.triggerNodeIdV +
                ", nodeDataMessage=" + this.nodeDataMessage +
                ", triggerTime=" + this.triggerTime +
                '}';
    }
}
