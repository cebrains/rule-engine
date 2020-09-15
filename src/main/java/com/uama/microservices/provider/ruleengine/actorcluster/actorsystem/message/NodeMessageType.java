package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-14 15:46
 **/
public enum NodeMessageType {
    NODE_INIT_MESSAGE(NodeInitMessage.class),
    NODE_DATA_MESSAGE(NodeDataMessage.class),
    NODE_TRIGGER_MESSAGE(NodeTriggerMessage.class),
    NODE_ALARM_MESSAGE(NodeAlarmMessage.class),
    NODE_NOT_EXISTS_MESSAGE(NodeNotExistsMessage.class),
    NODE_DELETE_MESSAGE(NodeDeleteMessage.class),
    ROOT_ROUTER_UPDATE_MESSAGE(RootRouterUpdateMessage.class),
    NODE_UPDATE_MESSAGE(NodeUpdateMessage.class),
    NODE_STOP_MESSAGE(NodeStopMessage.class),
    NODE_STOP_DONE_MESSAGE(NodeStopDoneMessage.class)
    ;
    
    private Class<?> messageClass;
    private static final Map<Class<?>, NodeMessageType> messageClassMap = Arrays.stream(NodeMessageType.values()).collect(Collectors.toMap(NodeMessageType::getMessageClass, v -> v));
    
    NodeMessageType(Class<?> messageClass) {
        this.messageClass = messageClass;
    }
    
    public Class<?> getMessageClass() {
        return messageClass;
    }
    
    public static Map<Class<?>, NodeMessageType> messageClassMap() {
        return messageClassMap;
    }
}
