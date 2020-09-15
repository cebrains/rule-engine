package com.uama.microservices.provider.ruleengine.actorcluster.message;


import com.uama.microservices.iot.actorsystem.message.SerializableNodeMessage;

import java.io.Serializable;

/**
 * @program: uama-iot
 * @description:
 * @author: liwen
 * @create: 2019-09-02 17:49
 **/
public class BroadCastMessage implements SerializableNodeMessage {
    
    private final ActorClusterMessageType messageType;
    
    private final Serializable message;
    
    public BroadCastMessage(ActorClusterMessageType messageType, Serializable message) {
        this.messageType = messageType;
        this.message = message;
    }
    
    public ActorClusterMessageType getMessageType() {
        return messageType;
    }
    
    public Object getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return "BroadCastMessage{" +
                "messageType=" + messageType +
                ", message=" + message +
                '}';
    }
}
