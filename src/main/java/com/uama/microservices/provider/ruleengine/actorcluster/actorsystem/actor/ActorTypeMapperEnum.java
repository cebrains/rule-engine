package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.ConstantFields;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor.*;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeholderactor.*;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 20:51
 **/
public enum ActorTypeMapperEnum {
    SYSTEM(SystemNodeHolderActor.class, SystemNodeActor.class, ConstantFields.SYSTEM_DISPATCHER),
    INPUT(InputNodeHolderActor.class, InputNodeActor.class, ConstantFields.INPUT_DISPATCHER),
    MESSAGE_TYPE_SWITCH(MessageTypeSwitchNodeHolderActor.class, MessageTypeSwitchNodeActor.class, ConstantFields.MESSAGE_TYPE_SWITCH_DISPATCHER),
    ROUTER(RouterNodeHolderActor.class, RouterNodeActor.class, ConstantFields.ROUTER_DISPATCHER),
    DATA_FILTER(DataFilterNodeHolderActor.class, DataFilterNodeActor.class, ConstantFields.DATA_FILTER_DISPATCHER),
    SAVE_DB(SaveDBNodeHolderActor.class, SaveDBNodeActor.class, ConstantFields.SAVE_DB_DISPATCHER),
    DATA_CHECK(DataCheckNodeHolderActor.class, DataCheckNodeActor.class, ConstantFields.DATA_CHECK_DISPATCHER),
    CREATE_ALARM(CreateAlarmNodeHolderActor.class, CreateAlarmNodeActor.class, ConstantFields.CREATE_ALARM_DISPATCHER),
    KAFKA(KafkaNodeHolderActor.class, KafkaNodeActor.class, ConstantFields.KAFKA_DISPATCHER)
    ;
    private Class<? extends NodeHolderActor> nodeHolderActorClass;
    private Class<? extends NodeActor> nodeActorClass;
    private String dispatcherName;
    
    ActorTypeMapperEnum(Class<? extends NodeHolderActor> nodeHolderActorClass,
                        Class<? extends NodeActor> nodeActorClass,
                        String dispatcherName) {
        this.nodeHolderActorClass = nodeHolderActorClass;
        this.nodeActorClass = nodeActorClass;
        this.dispatcherName = dispatcherName;
    }
    
    public Class<? extends NodeHolderActor> getNodeHolderActorClass() {
        return nodeHolderActorClass;
    }
    
    public Class<? extends NodeActor> getNodeActorClass() {
        return nodeActorClass;
    }
    
    public String getDispatcherName() {
        return dispatcherName;
    }
}
