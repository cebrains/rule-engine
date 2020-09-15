package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-22 10:55
 **/
public class NodeNotExistsMessage implements NodeMessage {
    
    private final NodeId nodeIdV;
    
    public NodeNotExistsMessage(NodeId nodeIdV) {
        this.nodeIdV = nodeIdV;
    }
    
    public NodeId getNodeIdV() {
        return nodeIdV;
    }
    
    @Override
    public String toString() {
        return "NodeNotExistsMessage{" +
                "nodeIdV=" + this.nodeIdV +
                '}';
    }
}
