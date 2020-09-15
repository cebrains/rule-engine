package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-23 11:46
 **/
public class NodeUpdateMessage implements NodeMessage {
    
    private final NodeInitMessage nodeInitMessage;
    
    private final NodeDeleteMessage nodeDeleteMessage;
    
    public NodeUpdateMessage(NodeInitMessage nodeInitMessage, NodeDeleteMessage nodeDeleteMessage) {
        this.nodeInitMessage = nodeInitMessage;
        this.nodeDeleteMessage = nodeDeleteMessage;
    }
    
    public NodeInitMessage getNodeInitMessage() {
        return nodeInitMessage;
    }
    
    public NodeDeleteMessage getNodeDeleteMessage() {
        return nodeDeleteMessage;
    }
    
    @Override
    public String toString() {
        return "NodeUpdateMessage{" +
                "nodeInitMessage=" + this.nodeInitMessage +
                ", nodeDeleteMessage=" + this.nodeDeleteMessage +
                '}';
    }
}
