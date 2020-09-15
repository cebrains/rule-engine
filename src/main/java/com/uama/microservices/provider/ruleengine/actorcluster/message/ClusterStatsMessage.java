package com.uama.microservices.provider.ruleengine.actorcluster.message;

/**
 * @program: uama-iot
 * @description:
 * @author: liwen
 * @create: 2019-09-03 16:10
 **/
public class ClusterStatsMessage {
    private final ActorClusterMessageType messageType;
    
    public ClusterStatsMessage(ActorClusterMessageType messageType) {
        this.messageType = messageType;
    }
    
    public ActorClusterMessageType getMessageType() {
        return messageType;
    }
}
