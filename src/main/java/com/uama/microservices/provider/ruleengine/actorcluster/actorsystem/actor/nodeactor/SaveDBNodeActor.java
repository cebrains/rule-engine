package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor;

import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.NodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor.DefaultSaveDBNodeProcessor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;

import java.util.List;
import java.util.Map;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 20:04
 **/
public class SaveDBNodeActor extends NodeActor<DefaultSaveDBNodeProcessor, SaveDBNodeActor.SaveDBNodeConfiguration> {
    
    private final SaveDBNodeConfiguration configuration;
    
    protected Object[] args;
    
    public SaveDBNodeActor(Map<ActorTypeMapperEnum, ActorRef> actorHolderMap,
                           Map<String, MIotRuleEngineRuleChainInitV> chainIdMap,
                           List<MIotRuleEngineRuleRelationInitV> toRelationList,
                           List<MIotRuleEngineRuleRelationInitV> fromRelationList,
                           MIotRuleEngineRuleNodeInitV nodeInitV,
                           Object... args) {
        super(actorHolderMap, chainIdMap, toRelationList, fromRelationList, nodeInitV);
        this.configuration = initNodeConfiguration(nodeInitV);
        this.args = args;
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
        super.actorProcessor = new DefaultSaveDBNodeProcessor(this);
    }
    
    @Override
    protected SaveDBNodeConfiguration initNodeConfiguration(MIotRuleEngineRuleNodeInitV nodeInitV) {
        return new SaveDBNodeConfiguration();
    }
    
    @Override
    public ActorTypeMapperEnum getType() {
        return ActorTypeMapperEnum.SAVE_DB;
    }
    
    public SaveDBNodeConfiguration getConfiguration() {
        return this.configuration;
    }
    
    public static class SaveDBNodeConfiguration {
        private SaveDBNodeConfiguration() {}
    }
}
