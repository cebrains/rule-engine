package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.NodeMessage;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-14 13:47
 **/
public class NodeMessageCarrier<M extends NodeMessage> implements NodeMessage, com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.MessageCarrier {
    
    private final NodeId targetNodeIdV;
    
    private final NodeId senderNodeIdV;
    
    private final M message;
    
    public NodeMessageCarrier(NodeId targetNodeIdV, NodeId senderNodeIdV, M message) {
        this.targetNodeIdV = targetNodeIdV;
        this.senderNodeIdV = senderNodeIdV;
        this.message = message;
    }
    
    public NodeId getTargetNodeIdV() {
        return targetNodeIdV;
    }
    
    public NodeId getSenderNodeIdV() {
        return senderNodeIdV;
    }
    
    public M getMessage() {
        return message;
    }
    
    @Override
    public Class<M> getMessageClass() {
        return (Class<M>) message.getClass();
    }
    
    @Override
    public String toString() {
        return "NodeMessageCarrier{" +
                "targetNodeIdV=" + this.targetNodeIdV +
                ", senderNodeIdV=" + this.senderNodeIdV +
                ", message=" + this.message +
                '}';
    }
}
