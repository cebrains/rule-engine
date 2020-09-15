package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor;

import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.NodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor.DefaultSystemNodeProcessor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;

import java.util.Map;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 20:04
 **/
public class SystemNodeActor extends NodeActor<DefaultSystemNodeProcessor, SystemNodeActor.SystemNodeConfiguration> {
    
    private final SystemNodeConfiguration configuration;
    
    public SystemNodeActor(Map<ActorTypeMapperEnum, ActorRef> actorHolderMap,
                           MIotRuleEngineRuleNodeInitV nodeInitV) {
        super(actorHolderMap, null, null, null, nodeInitV);
        this.configuration = initNodeConfiguration(nodeInitV);
    }
    
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(RootRouterUpdateMessage.class, super.actorProcessor::processRootRouterUpdateMessage)
                .match(NodeDeleteMessage.class, super.actorProcessor::processNodeDeleteMessage)
                .match(NodeNotExistsMessage.class, super.actorProcessor::processNodeNotExistsMessage)
                .match(NodeAlarmMessage.class, super.actorProcessor::processNodeAlarmMessage)
                .match(NodeTriggerMessage.class, super.actorProcessor::processNodeTriggerMessage)
                .match(NodeDataMessage.class, super.actorProcessor::processNodeDataMessage)
                .match(NodeInitMessage.class, super.actorProcessor::processNodeInitMessage)
                .matchAny(super.actorProcessor::processUnknownMessage)
                .build();
    }
    
    @Override
    protected void initActorProcessor() {
        super.actorProcessor = new DefaultSystemNodeProcessor(this);
    }
    
    @Override
    protected SystemNodeConfiguration initNodeConfiguration(MIotRuleEngineRuleNodeInitV nodeInitV) {
        return new SystemNodeConfiguration();
    }
    
    @Override
    public ActorTypeMapperEnum getType() {
        return ActorTypeMapperEnum.SYSTEM;
    }
    
    public SystemNodeConfiguration getConfiguration() {
        return this.configuration;
    }
    
    public static class SystemNodeConfiguration {
        private SystemNodeConfiguration() {}
    }
}
